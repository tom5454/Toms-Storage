# Platform Package

- Implements the final Fabric registry, item-group, block-use, legacy NBT sync-buffer, Trinkets lookup, and container-screen adapters requested by common code.
- Preserve registry IDs, nullable/PASS interaction semantics, server authority, NBT identity, and optional Trinkets loading; transfer operations must preserve simulation and remainders.
- Related code lives in loader-shared `StorageMod`, `GameObject`, and storage wrappers, plus the intended `TomsStorage-120` shared/platform counterparts configured by `sourceSets`.
- Validate registrations and menus on client/server, NBT round trips, and wireless lookup both with and without Trinkets.
