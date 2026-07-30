# Minecraft 1.20.6 Platform-Shared Java Layer

This layer isolates mapped 1.20.6 recipe, screen-widget, block base, and JEI transfer API differences shared across loaders.

- Minecraft API imports and loader-neutral project abstractions are allowed; NeoForge and Fabric APIs are not.
- Keep behavior portable across loaders and compatible with 1.20.6 components and payload callers.
- Compare both loader adapters and adjacent Minecraft-version implementations before changing mapped signatures.
- Validate every consuming loader build plus focused recipe, screen, block, and integration behavior.
