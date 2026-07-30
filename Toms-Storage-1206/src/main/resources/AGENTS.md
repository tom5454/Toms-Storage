# Final NeoForge Resources

- Owns 1.20.6 `pack.mcmeta`, `META-INF/neoforge.mods.toml`, and the version-specific access transformer.
- Keep metadata ranges aligned with `gradle.properties` and mapped access-transformer signatures; do not add guidance files inside `META-INF`.
- Coordinate metadata class names and optional dependencies with loader initialization, payload/component registration, and Gradle integration behavior.
- Validate `processResources`/build output, client and dedicated-server discovery, and transformed access at runtime.
