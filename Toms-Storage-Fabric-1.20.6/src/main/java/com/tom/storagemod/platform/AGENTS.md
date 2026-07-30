# Platform Package

- Implements final 1.20.6 Fabric registries, including data-component types, item groups, use hooks, Trinkets lookup, NBT-buffer compatibility, and screens.
- Preserve IDs, component values, nullable/PASS interaction semantics, server authority, and optional Trinkets loading; transfer callers must preserve transactions and exact amounts.
- Related code lives in local loader-shared lifecycle/storage code and intended `TomsStorage-1206` shared/platform component and menu contracts.
- Validate component registration and round trips, client/server menus, wireless lookup with/without Trinkets, and compilation under the resolved Java target.
