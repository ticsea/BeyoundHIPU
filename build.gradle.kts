import dev.kikugie.stonecutter.controller.task.StonecutterTaskMutexService
import java.util.Properties

plugins {
    id("net.neoforged.moddev")
    // for normal there should be "me.modmuss50.mod-publish-plugin", here just for self use.
    id("me.afk.mod-publish-plugin") // 如果你用的是本地或特定作者版本，请确保仓库里有
}

// ==========================================
// 1. 基础配置与变量提取
// ==========================================
version = "${sc.properties["mod.version"]}+${sc.current.version}"
base.archivesName.set(sc.properties["mod.id"])

// 从 properties 提取所需变量
val mcVersion = sc.current.version

val modId = sc.properties["mod.id"] as String
val modVersion = sc.properties["mod.version"] as String
val modName = sc.properties["mod.name"] as String
val modGroup = sc.properties["mod.group"] as String
val modDesc = sc.properties["mod.description"] as String ?: ""
val modAuthors = sc.properties["mod.authors"] as String ?: ""
val mcCompat = sc.properties["mod.mc_compat"] as String
val modLicense = sc.properties["mod.license"] as String
val requiredMcReleases = sc.properties.rawOrNull("mod:mc_releases")?.to<List<String>>() ?: emptyList()
// 判断所需 Java 版本
val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

// Changelog 解析函数
fun parseChangelog(full: String, version: String)
        = Regex("(?:#\\s+${version.replace(".", "\\.")}\\s*)(\\S[\\s\\S]*?)(?:\\s*(?:# |$))", RegexOption.IGNORE_CASE).find(full)?.groupValues?.get(1)
val fullChangelog = rootProject.file("CHANGELOG.md").readText()
val versionChangelog = parseChangelog(fullChangelog, modVersion) ?: parseChangelog(fullChangelog, "work in progress") ?: ""

// 读取本地发布凭据
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

// ==========================================
// 2. 仓库与依赖配置
// ==========================================
repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

// 在这里添加其他依赖，比如：
dependencies {
    // implementation("...")
        val beyondDimensionsVersionId = sc.properties["deps.beyond_dimensions_version_id"] as String
    implementation("curse.maven:beyounddimensions-1222890:${beyondDimensionsVersionId}")
//    val beyondDimensionsVersion = sc.properties["deps.beyond_dimensions_version"] as String
//    val kffVersion = sc.properties["deps.kff_version"] as String
//    implementation("com.wintercogs.beyonddimensions:beyonddimensions-${mcVersion}-neoforge:${beyondDimensionsVersion}")
//    runtimeOnly("maven.modrinth:kotlin-for-forge:${kffVersion}")
}

// ==========================================
// 3. NeoForge & Java 配置
// ==========================================
neoForge {
    version = sc.properties["deps.neo_loader"] as String

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = file("../../run/")
            client()
        }
        register("server") {
            gameDirectory = file("../../run/")
            server()
        }
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

// ==========================================
// 4. 任务处理与资源替换
// ==========================================
tasks {
    processResources {
        // 替换 neoforge.mods.toml
        val props = mapOf(
            "id" to modId,
            "name" to modName,
            "version" to modVersion,
            "minecraft" to mcCompat,
            "description" to modDesc,
            "authors" to modAuthors,
            "license" to modLicense,
            "mixinFileName" to "${modId}.mixins.json"
        )
        inputs.properties(props)
        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }

        // 替换 mixins.json 里的 Java 版本占位符
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
        usesService(StonecutterTaskMutexService.create(gradle, name))
    }

    // 将 LICENSE 文件打包进 jar
    withType<Jar> {
        val localModId = modId
        inputs.property("mod_id", localModId)
        from("../../LICENSE") { rename { "$it-$localModId" } }
    }
}

// ==========================================
// 5. 收集产物 (注意：新插件下没有 remapJar)
// ==========================================
// 因为使用 moddev，最终产物是 jar，所以直接拿 jar 任务作为输出
val finalJarTask = tasks.named<Jar>("jar")
// 复制到 out 目录
val copyJars = tasks.register<Copy>("copyJars") {
    val dir = "../../out/v${modVersion}/"
    project.delete(fileTree(mapOf("dir" to dir, "include" to listOf("$modId-*.jar"))))
    from(finalJarTask)
    into(dir)
}
tasks.named("build") { finalizedBy(copyJars) }

val copyLatest = tasks.register<Copy>("copyLatest") {
    val dir = "../../out/latest/"
    // 清理旧文件
    project.delete(fileTree(mapOf("dir" to dir, "include" to listOf("$modId-*.jar"))))

    // 只复制主 Jar，不复制 sourcesJar
    from(finalJarTask)
    into(dir)

    // 精确重命名（排除 -sources）
    rename { originalName ->
        if (originalName.contains("sources")) return@rename originalName
        "$modId-${modVersion}+mc${sc.current.version}.jar"
    }
}
tasks.named("build") { finalizedBy(copyLatest) }

// ==========================================
// 6. 发布配置 (Publishing)
// ==========================================
publishMods {
    val isDryRun = sc.properties["publish.enabled"]?.toString()?.trim()?.lowercase() != "true"
    if (isDryRun) logger.info("publish.enabled is not set to true, dry run!")

    // 注意：直接取 jar 任务的输出文件
    file.set(finalJarTask.flatMap { it.archiveFile })

    dryRun = isDryRun
    version = "${modVersion}+${sc.current.version}"
    displayName = "${modName} ${modVersion} for ${sc.current.version}"
    changelog = versionChangelog
    type = when (sc.properties["mod.version_type"]?.toString()?.lowercase()) {
        "alpha" -> ALPHA
        "beta" -> BETA
        "release" -> STABLE
        else -> throw Exception("Invalid version type")
    }
    modLoaders.add("neoforge")

    val modrinthToken = localProperties.getProperty("publish.modrinth.token", "")
    val curseforgeToken = localProperties.getProperty("publish.curseforge.token", "")
    val xyebbsKey = localProperties.getProperty("publish.xyebbs.key")
    val xyebbsSecret = localProperties.getProperty("publish.xyebbs.secret")

    if (isDryRun || !modrinthToken.isNullOrBlank()) modrinth {
        projectId = sc.properties["publish.modrinth.id"] as String
        accessToken = modrinthToken
        // 将实际发布的目标版本添加进去
        requiredMcReleases.forEach { minecraftVersions.add(it) }

        // Optionally set the Modrinth environment for the uploaded version
        environment = CLIENT_ONLY
    }

    if (isDryRun || !curseforgeToken.isNullOrBlank()) curseforge {
        projectId = sc.properties["publish.curseforge.id"] as String
        accessToken = curseforgeToken
        requiredMcReleases.forEach { minecraftVersions.add(it) }
        changelogType = "markdown"

        client = true   // 或 clientRequired = true (旧版本插件)
        server = true   // 或 serverRequired = true (旧版本插件)

        // 如果有依赖关系可以开启
        // requires("cloth-config")
    }

    if (isDryRun || !xyebbsKey.isNullOrBlank() || !xyebbsSecret.isNullOrBlank()) xyebbs {
        accessToken = "xye"
        projectId = sc.properties["publish.xyebbs.id"] as String
        xyeKey = xyebbsKey
        xyeSecret = xyebbsSecret
        requiredMcReleases.forEach { minecraftVersions.add(it) }

        label = sc.properties["mod.version"] as String
        environmentType = "CLIENT_AND_SERVER" // or CLIENT
        orderNumber = 0

        curseForgeLink("https://www.curseforge.com/minecraft/mc-mods/beyondhipu")
        modrinthLink("https://modrinth.com/mod/beyondhipu")
    }
}

// Pull up relevant tasks.
listOf(tasks.build, tasks.clean, tasks.publishMods).forEach {
    it.get().group = "_project"
}