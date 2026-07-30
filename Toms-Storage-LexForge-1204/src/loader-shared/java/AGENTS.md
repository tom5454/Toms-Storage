# Legacy Forge-Shared Java Layer

This local layer owns Minecraft 1.20.4 Forge lifecycle, configuration, capabilities, block entities, storage wrappers, tags, models, and packets.

- Forge 1.20.4 APIs are allowed; NeoForge and Fabric APIs are not. Keep final adapters in `src/main/java`; intended loader-neutral and mapped platform behavior belongs to the configured sibling source owner.
- Compare Forge 1.20.1 and NeoForge 1.20.4 counterparts before adapting signatures. The configured shared/platform sibling is absent, so compilation cannot validate their contracts here.
- Preserve capability simulation, scan limits, loaded-chunk checks, NBT persistence, optional integration isolation, and dedicated-server safety.
