# NeoForge 1.20.6 Project

Inherit repository-wide rules from `../AGENTS.md`; this file covers only Minecraft 1.20.6 on NeoForge 20.6 with Java 21.

## Source Composition

- `main` combines local default `src/main/java` and resources with `src/shared`, `src/loader-shared`, and `src/platform-shared`; all referenced source roots exist locally except the configured but absent `src/platform-shared/resources`.
- Shared is loader-neutral, loader-shared owns NeoForge-family code, platform-shared isolates 1.20.6 mappings, and main supplies final NeoForge adapters.
- This port stores important item state in registered data components and uses custom payload types/codecs. Do not replace these with older item-NBT or ad hoc packet-channel patterns; block entities and `SavedData` still retain their defined persistence formats.

## Integrations

- With `useLib` absent, only `com/tom/storagemod/rei/**` is excluded. JEI and EMI source remains compiled and both have runtime dependencies.
- `-DuseLib=true` enables REI, Cloth Config, and Architectury. Curios has compile and runtime dependencies.

## Commands And Validation

- Run locally: `bash gradlew build`, `bash gradlew runClient`, `bash gradlew runServer`, `bash gradlew runGameTestServer`, and `bash gradlew runData`.
- The strongest general gate is `bash gradlew build`, followed by client and dedicated-server smoke tests for gameplay, payload, registration, or client changes.
- Validate optional code both with a normal build and `bash gradlew build -DuseLib=true` when REI-related paths change; inspect generated resource diffs after `runData`.
