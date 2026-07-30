# Fabric 1.20.1 Project

- Target Minecraft 1.20.1 with Fabric Loader 0.16.10, Fabric API 0.92.3+1.20.1, and Java 17 (`--release 17`).
- This is the legacy Fabric implementation: gameplay item state and play packets use NBT; networking uses raw `ResourceLocation` channels and `FriendlyByteBuf`.
- `sourceSets` expects absent no-hyphen sibling `../TomsStorage-120` for shared and platform-shared Java and resources. Do not trust Gradle results until every configured source directory exists.
- Only after source-layout validation, run commands here: `bash gradlew build`, then `bash gradlew runClient` and `bash gradlew runServer` for gameplay or networking changes.
- Strongest validation is a complete build plus client/server smoke tests; transfer changes also require partial/full insert and extract tests, and NBT changes require save/reload and client sync tests.
