# Minecraft 1.20.2 Platform-Shared Java Layer

This layer isolates mapped 1.20.2 recipe, screen-widget, JEI transfer, and `SavedData` API differences.

- Minecraft API imports and loader-neutral project abstractions are allowed; NeoForge- or Fabric-specific imports are not.
- Keep behavior portable across loaders for this Minecraft version and avoid moving NBT-era code toward 1.20.6 components/codecs.
- Compare both loader adapters and adjacent Minecraft-version implementations before changing mapped signatures.
- Validate every composed loader target that consumes a changed abstraction; for this project, first account for its missing inherited source path.
