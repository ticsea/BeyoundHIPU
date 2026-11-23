# Stonecutter Nroforge Multi-version Mod Template

## Setup
1. Review the supported Minecraft versions in `settings.gradle.kts`.
   For new entries, add version metadata in `stonecutter.properties.toml` with the same keys as other versions.
2. Change `mod.group`, `mod.id` and `mod.name` properties in `stonecutter.properties.toml`.
3. Rename the base package `com.example` in `src/main/java` to match your mod's group and id convention.
4. Rename `src/main/resources/template.mixins.json` to use your mod's id, and update the internal `package` field to match your actual package path.
5. Update `rootProject.name` in `settings.gradle.kts`.
6. Review the `LICENSE` file.
   See the [license decision diagram](https://docs.codeberg.org/getting-started/licensing/#license-decision-diagram) for common open-source license options.
7. Review and update `src/main/resources/META-INF/neoforge.mods.toml` to keep all manifest properties up-to-date.
8. Align your asset folder: rename `src/main/resources/assets/template` to `assets/<mod.id>` to match your mod id.
9. Update the mixin entry inside `<yourmodid>.mixins.json` to point to your renamed mixin JSON file.
10. After all modifications, reload Gradle projects in IDEA to apply Stonecutter configuration.
11. Confirm your `.gitignore` excludes `build/`, `run/`, `.gradle/` and other generated directories.

## Usage
- Use the `Set active project to ...` Gradle tasks to switch the active Minecraft version, which updates the source set visible in `src/` and IDE indexing.
- Run `runClient` / `runServer` Gradle tasks after switching versions to launch the game for testing.
- Use the `buildAndCollect` Gradle task to build release JARs; artifacts will be collected into `build/libs/`.
- To publish releases to Modrinth and CurseForge: enable the `mod-publish-plugin` in `stonecutter.gradle.kts` and `build.gradle.kts`, then fill in the corresponding platform configuration blocks.
- To publish artifacts to a personal Maven repository: enable `maven-publish` in `build.gradle.kts` and configure the publication block.
- Stonecutter uses preprocessor syntax (`//?`, `//$`) for version-specific code branches. See the wiki for details.
- If IDE code inspection breaks after switching versions, reload Gradle and reindex the project.

## Useful links
- [Stonecutter Official Wiki](https://stonecutter.kikugie.dev/wiki/start/): Core documentation for the multi-version build plugin. It is highly recommended to read through to understand the preprocessing mechanism.
- [NeoForge Discord Server](https://discord.neoforged.net/): For NeoForge mod development help.
- [Stonecutter Discord Server](https://discord.kikugie.dev/): For Stonecutter and Gradle troubleshooting.
- [How To Ask Questions The Smart Way](http://www.catb.org/esr/faqs/smart-questions.html): A classic guide to asking effective technical questions; also available in [video form](https://www.youtube.com/results?search_query=How+To+Ask+Questions+The+Smart+Way).
- [Mod publish plugin](https://modmuss50.github.io/mod-publish-plugin/)
  > Note: Some external links (Discord invites, third-party guides) may require corresponding platform access or network conditions to open normally.
