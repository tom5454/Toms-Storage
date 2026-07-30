# Forge REI Bridge

Own the Forge-side REI plugin bridge used only by the optional `-DuseLib` source composition.

- Keep class/package names aligned with Gradle exclusions and shared `rei`; core or client initialization must not reference this bridge when REI is absent.
- Inspect shared `rei`, `StorageModClient`, Gradle optional dependencies, and the legacy Forge 1.20.4 counterpart.
- Validate default and `-DuseLib=true` builds, REI discovery, recipe transfer, and dedicated-server startup without REI.
