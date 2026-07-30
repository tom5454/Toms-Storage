# Main Java Layer

- This layer contains final Minecraft 1.20.4 Fabric adapters, not common gameplay or loader lifecycle code.
- Fabric API, Fabric Loader, and Trinkets calls may be used here; do not leak them into inherited shared or platform-shared contracts.
- Keep client screen helpers client-only and preserve legacy NBT sync while adapting mapped Minecraft signatures.
- Validate compilation, dedicated-server class loading, menus, registry/item-group behavior, NBT reads, and Trinkets-present/absent behavior.
