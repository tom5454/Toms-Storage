# Main Java Layer

- This layer contains final Minecraft 1.20.1 Fabric adapters, not shared gameplay implementations.
- Fabric API, Fabric Loader, and Trinkets calls may be used here; do not leak them into the inherited shared or platform-shared contracts.
- Keep client screen helpers client-only and keep registry, interaction, NBT buffer, and extra-slot behavior aligned with the loader-shared entry points.
- Validate compilation, dedicated-server class loading, menu opening, creative-tab registration, and Trinkets-present/absent behavior.
