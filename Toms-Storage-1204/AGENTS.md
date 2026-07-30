# NeoForge 1.20.4 Project

Inherit repository-wide rules from `../AGENTS.md`; this file covers only Minecraft 1.20.4 on NeoForge 20.4 with Java 17.

## Source Composition

- `main` combines local default `src/main/java` and resources with `src/shared`, `src/loader-shared`, and `src/platform-shared`; all referenced source roots exist locally except the configured but absent `src/platform-shared/resources`.
- Shared is loader-neutral, loader-shared owns NeoForge-family code, platform-shared isolates 1.20.4 mappings, and main supplies final NeoForge adapters.
- This is an NBT-era port. Keep item bindings, filters, menu messages, and persistence on 1.20.4 NBT conventions; do not backport 1.20.6 data components or payload codecs.

## Integrations

- With `useLib` absent, only `com/tom/storagemod/rei/**` is excluded. JEI and EMI source remains compiled; JEI has runtime support while EMI is API-only by default.
- `-DuseLib=true` enables REI, Cloth Config, and Architectury. Curios is API-only in the default dependency set.

## Commands And Validation

- Run locally: `bash gradlew build`, `bash gradlew runClient`, `bash gradlew runServer`, `bash gradlew runGameTestServer`, and `bash gradlew runData`.
- The strongest general gate is `bash gradlew build`, followed by client and dedicated-server smoke tests for gameplay, networking, registration, or client changes.
- Validate optional code both with a normal build and `bash gradlew build -DuseLib=true` when REI-related paths change; inspect generated resource diffs after `runData`.
