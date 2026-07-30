# Final NeoForge Resources

- Owns 1.20.2 `pack.mcmeta`, NeoForge metadata under `META-INF`, and the version-specific access transformer.
- Keep metadata ranges aligned with `gradle.properties` and mapped access-transformer signatures; do not add guidance files inside `META-INF`.
- Coordinate metadata class names and dependencies with loader initialization and Gradle dependency/exclusion behavior.
- Validate `processResources`/build output, dedicated-server discovery, and any transformed access at runtime after resolving source composition.
