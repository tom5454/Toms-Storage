# Platform Package

- Bridges inherited 1.20.1 Fabric/common behavior to Minecraft 1.20.2 registries, interactions, bounded NBT reads, Trinkets, and screens.
- Preserve registry IDs, nullable/PASS interaction semantics, server authority, NBT identity, and optional Trinkets loading; do not introduce 1.20.4 or data-component APIs.
- Related code is expected from `TomsStorage-120`, `TomsStorageFabric-1.20`, and `TomsStorage-1202`; all are configured no-hyphen siblings and absent in this checkout.
- Validate the complete composed source set, NBT packet decoding, menu opening, registrations, and wireless lookup with and without Trinkets.
