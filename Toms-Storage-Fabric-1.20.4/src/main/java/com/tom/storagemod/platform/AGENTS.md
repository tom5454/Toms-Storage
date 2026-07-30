# Platform Package

- Implements final 1.20.4 Fabric registries, item groups, block-use hooks, bounded NBT reads, Trinkets lookup, and screens.
- Preserve IDs, nullable/PASS interaction semantics, NBT identity, server authority, and optional Trinkets loading; transfer callers must retain transaction and remainder semantics.
- Related code lives in local loader-shared lifecycle/storage classes and intended `TomsStorage-1204` shared/platform counterparts.
- Validate registrations and menus on client/server, legacy NBT round trips, and wireless lookup both with and without Trinkets.
