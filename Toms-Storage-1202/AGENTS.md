# NeoForge 1.20.2 Project

Inherit repository-wide rules from `../AGENTS.md`; this file covers only Minecraft 1.20.2 on NeoForge 20.2 with Java 17.

## Source Composition

- `main` combines local `src/main/java`, `src/loader-shared/java`, and `src/platform-shared/java` with shared Java from `../TomsStorage-120/src/shared/java`.
- Shared resources likewise come from `../TomsStorage-120/src/shared/resources`; the local loader resource root overlays them, while configured `src/platform-shared/resources` is absent.
- This checkout has `Toms-Storage-120`, not the referenced no-hyphen `TomsStorage-120`. The inherited shared source is therefore absent from this build. Treat compilation as blocked or incomplete until that exact source-set path exists; do not silently repair it outside the task scope.
- This is an NBT-era port. Item bindings, filters, menu messages, and persistence use the 1.20.2 conventions; do not introduce 1.20.6 data components or payload codecs.

## Integrations

- With `useLib` absent, `com/tom/storagemod/rei/**` is excluded. `jei/**` and `emi/**` are always excluded from Java compilation here.
- `-DuseLib=true` enables REI, Cloth Config, and Architectury. Curios API is compile-only and Curios is present at runtime.

## Commands And Validation

- Run locally: `bash gradlew build`, `bash gradlew runClient`, `bash gradlew runServer`, `bash gradlew runGameTestServer`, and `bash gradlew runData`.
- First verify every `sourceSets` path exists. Once the shared-source blocker is resolved, the strongest general gate is `bash gradlew build`, followed by client and dedicated-server smoke tests for gameplay, networking, or registration changes.
- Validate optional code both without `useLib` and with `bash gradlew build -DuseLib=true` when REI-related paths change.
