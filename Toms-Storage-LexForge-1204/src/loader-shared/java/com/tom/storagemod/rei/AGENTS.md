# Legacy Forge REI Bridge

Own the Forge 1.20.4 REI bridge compiled only when `-DuseLib` is present.

- Keep package paths aligned with the Gradle exclusion and intended shared `rei`; no core path may load REI classes when absent.
- Inspect `StorageModClient`, Gradle optional dependencies, Forge 1.20.1 and NeoForge 1.20.4 REI counterparts, and inherited shared handlers once available.
- Validate default and `-DuseLib=true` builds, plugin discovery, transfer behavior, and dedicated-server startup without REI.
