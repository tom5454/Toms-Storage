# Final Legacy Forge Java Layer

This layer owns final Minecraft 1.20.4 Forge channel and platform adapters; gameplay and mapped common code are intended to arrive from sibling overlays.

- Forge 1.20.4 APIs are allowed; NeoForge and Fabric APIs are not. Do not duplicate shared behavior locally to mask the absent sibling path.
- Inspect local loader-shared contracts and Forge 1.20.1 plus NeoForge 1.20.4 final adapters before changing implementations.
- Preserve packet discriminators, registry IDs, capability semantics, mapped signatures, and client/server class separation.
