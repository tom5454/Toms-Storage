# Legacy Forge 1.20.2 Project

Inherit repository rules from `../AGENTS.md`. This is the legacy Minecraft 1.20.2 Forge 48.1.0 compatibility port and targets Java 17; it is not the NeoForge 1.20.2 project.

- Default `src/main` is local. Configured shared Java/resources come from `../TomsStorage-120`, and platform Java/resources from `../TomsStorage-1202`; those no-hyphen siblings are absent in this checkout, so inherited code is omitted and validation is blocked.
- Run commands here: `bash gradlew tasks` to identify renamed run tasks and `bash gradlew build` only after verifying every source-set path exists. Do not create symlinks or rewrite paths unless that is the task.
- Omitting JVM property `-DuseLib` excludes inherited `com/tom/storagemod/rei/**`; any present value enables REI/Cloth/Architectury. JEI and EMI are otherwise inherited without exclusions.
- Strongest validation is a source-path audit, `bash gradlew build`, then affected client and dedicated-server smoke tests. A build that omitted absent inherited sources is not evidence of correctness.
- Preserve Forge 1.20.2 mappings, NBT and packet contracts, and registry/resource IDs; compare the Forge 1.20.1 source owner and the separate NeoForge 1.20.2 port deliberately.
