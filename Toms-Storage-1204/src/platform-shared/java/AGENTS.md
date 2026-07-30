# Minecraft 1.20.4 Platform-Shared Java Layer

This layer isolates mapped 1.20.4 recipe, screen-widget, JEI transfer, and `SavedData` API differences shared across loaders.

- Minecraft API imports and loader-neutral project abstractions are allowed; NeoForge and Fabric APIs are not.
- Keep behavior portable across loaders for this Minecraft version and retain NBT-era contracts.
- Compare both loader adapters and adjacent Minecraft-version implementations before changing mapped signatures.
- Validate every consuming loader build plus focused recipe, screen, and persistence behavior.
