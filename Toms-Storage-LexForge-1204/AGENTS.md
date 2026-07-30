# Legacy Forge 1.20.4 Project

Inherit repository rules from `../AGENTS.md`. This is the legacy Minecraft 1.20.4 Forge 49.0.13 compatibility port and targets Java 17; it is not the NeoForge 1.20.4 project.

- Local `src/loader-shared` and default `src/main` are overlaid with shared/platform Java/resources from `../TomsStorage-1204`. That no-hyphen sibling is absent in this checkout, so inherited code is omitted and validation is blocked.
- Run commands here: `bash gradlew tasks` to identify renamed run tasks and `bash gradlew build` only after verifying every source-set path exists. Do not create symlinks or rewrite paths unless requested.
- Omitting JVM property `-DuseLib` excludes inherited `rei`; `emi` is always excluded. Any present `-DuseLib` value enables REI/Cloth/Architectury; JEI remains included.
- Strongest validation is a source-path audit, `bash gradlew build`, then affected client and dedicated-server smoke tests. A build omitting absent shared/platform sources is not evidence.
- Preserve Forge 1.20.4 mappings and NBT-era contracts. Metadata currently uses broad lower bounds (`Forge 48`, Minecraft `1.20.2`); do not copy or broaden them without deliberate compatibility validation.
