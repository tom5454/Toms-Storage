# Fabric 1.20.6 Project

- Target Minecraft 1.20.6 with Fabric Loader 0.15.11 and Fabric API 0.98.0+1.20.6.
- Gradle declares Java 21 source/target but forces every Java compile to `--release 17`; resolve this conflict before using Java 21 syntax or claiming a Java-level validation.
- This line uses data components for important item state and Fabric custom payload types/stream codecs, while packet bodies still carry compact NBT where defined.
- `sourceSets` expects absent no-hyphen sibling `../TomsStorage-1206` for shared and platform-shared Java/resources. Validate every configured path before running local `bash gradlew build`, `bash gradlew runClient`, or `bash gradlew runServer`.
- Strongest validation is a complete composed build plus client/server payload tests, transfer edge cases, component-preserving sync, and world/item save-reload compatibility.
