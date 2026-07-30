# Shared Java Layer

This local layer owns gameplay, blocks, items, menus/screens, terminal synchronization, and optional recipe-viewer adapters reused by this project's overlays.

- Keep code loader-neutral: Minecraft APIs and project abstractions are allowed; Forge, NeoForge, Fabric, and loader lifecycle imports are not.
- Route registration, networking, capabilities, and platform signatures through existing abstractions and inspect `src/loader-shared/java`, `src/platform-shared/java`, and `src/main/java` before changing a boundary.
- Preserve server authority, NBT/protocol keys, registry IDs, client/server separation, and inventory remainder semantics.
