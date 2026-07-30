# Fabric 1.20.2 Project

- Target Minecraft 1.20.2 with Fabric Loader 0.14.22, Fabric API 0.89.0+1.20.2, and Java 17 (`--release 17`).
- This is an inherited bridge: local code only adapts the platform and metadata while `sourceSets` expects 1.20.1 shared/loader code and 1.20.2 platform-shared code.
- All configured no-hyphen siblings are absent: `../TomsStorage-120`, `../TomsStorageFabric-1.20`, and `../TomsStorage-1202`. Gradle may omit them silently, so do not trust results until every path exists.
- Only after source-layout validation, run commands here: `bash gradlew build`, then `bash gradlew runClient` and `bash gradlew runServer` for bridge or networking changes.
- Strongest validation is a build that demonstrably includes all inherited sources, plus client/server smoke tests, legacy NBT packet/menu tests, and save/reload tests.
