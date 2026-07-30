# Fabric 1.20.4 Project

- Target Minecraft 1.20.4 with Fabric Loader 0.15.6, Fabric API 0.95.0+1.20.4, and Java 17 (`--release 17`).
- This line has local Fabric loader code but retains legacy raw-channel `FriendlyByteBuf`/NBT play networking and NBT item persistence.
- `sourceSets` expects absent no-hyphen sibling `../TomsStorage-1204` for shared and platform-shared Java and resources. Do not trust Gradle results until every configured source directory exists.
- Only after source-layout validation, run commands here: `bash gradlew build`, then `bash gradlew runClient` and `bash gradlew runServer` for gameplay or networking changes.
- Strongest validation is a complete build plus client/server smoke tests; transfer changes require transaction edge cases and persistence changes require save/reload and sync tests.
