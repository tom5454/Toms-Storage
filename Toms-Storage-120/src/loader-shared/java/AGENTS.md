# Forge-Shared Java Layer

This layer owns Forge-family lifecycle, configuration, capabilities, block entities, storage wrappers, tags, models, and packets reusable by compatible Forge overlays.

- Forge and Minecraft 1.20.1 APIs are allowed; NeoForge and Fabric APIs are not. Keep final registry/network adapters in `src/main/java` when they vary by target.
- Shared Java may be consumed through sibling source sets, so compare legacy Forge counterparts before changing signatures or mapped APIs.
- Preserve capability simulation, scan limits, loaded-chunk checks, NBT persistence, optional integration isolation, and dedicated-server safety.
