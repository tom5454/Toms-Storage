# Final Forge Java Layer

This layer owns final Minecraft 1.20.1 Forge adapters not suitable for shared overlays: channel registration, registry/platform access, screens, and Polymorph integration.

- Forge 1.20.1 and integration APIs are allowed; NeoForge and Fabric APIs are not. Do not move broadly reusable gameplay into this layer.
- Inspect shared, loader-shared, and platform-shared contracts before changing implementations.
- Preserve packet discriminators, registry IDs, capability semantics, optional integration loading, and client/server class separation.
