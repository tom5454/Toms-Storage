# Forge 1.20.1 Project

Inherit repository rules from `../AGENTS.md`. This is the primary Minecraft 1.20.1 Forge 47.1.0 project and targets Java 17.

- `main` overlays local `src/shared`, `src/loader-shared`, `src/platform-shared`, and default `src/main` Java/resources. The Java source layers exist locally, but configured `src/platform-shared/resources` is absent; there is no no-hyphen sibling-path blocker here.
- Run commands here: `bash gradlew build`, `bash gradlew tasks`, and the project-specific client/server/data tasks shown by `tasks`. Networked dependency resolution is normally required.
- Omitting JVM property `-DuseLib` excludes `com/tom/storagemod/rei/**`; any present value enables REI/Cloth/Architectury. JEI and EMI compile paths remain included. Keep Polymorph code aligned with its required dependency.
- Strongest validation is `bash gradlew build`, then client and dedicated-server smoke tests for affected behavior. The custom `test` dependency names a mismatched client task and is not a reliable gate.
- Preserve Forge 1.20.1 mappings, NBT formats, registry IDs, packet ordering, and resource locations; do not substitute NeoForge or newer data-component APIs.
