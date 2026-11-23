plugins {
    id("net.neoforged.moddev") version "2.0.148" apply false
    // for normal there should be "me.modmuss50.mod-publish-plugin", here just for self use.
    id("me.afk.mod-publish-plugin") version "2.2.1" apply false
}

stonecutter active "1.21.1"


// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${properties["mod.version"]}\";"
    swaps["minecraft"] = "\"${current.version}\";"
    constants["release"] = properties["mod.id"] != "template"

    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
    }
}
