# Final NeoForge Resources

- Owns 1.20.4 `pack.mcmeta`, NeoForge metadata under `META-INF`, and the version-specific access transformer.
- Keep metadata ranges aligned with `gradle.properties` and mapped access-transformer signatures; do not add guidance files inside `META-INF`.
- Coordinate metadata class names and optional dependencies with loader initialization and Gradle integration behavior.
- Validate `processResources`/build output, client and dedicated-server discovery, and transformed access at runtime.
