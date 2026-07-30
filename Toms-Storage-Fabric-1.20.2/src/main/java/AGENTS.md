# Main Java Layer

- This layer contains only final Minecraft 1.20.2 Fabric adapters over inherited 1.20.1 shared/loader code and 1.20.2 platform-shared code.
- Fabric API, Fabric Loader, and Trinkets calls may be used here; do not push mapped 1.20.2 details into inherited common contracts.
- Preserve the legacy NBT protocol while adapting changed Minecraft signatures such as bounded NBT reads.
- Validate only after inherited source paths resolve; then test compilation, dedicated-server class loading, registration, menus, NBT sync, and Trinkets-present/absent behavior.
