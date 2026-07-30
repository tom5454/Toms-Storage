# Tile Package

- Implements 1.20.4 Fabric Transfer API entities for connectors, terminals, hoppers, crates, proxies, emitters, crafting, and painting.
- Preserve transaction atomicity, simulation/remainders, sided access, cycle checks, loaded-chunk guards, server authority, and persisted NBT keys.
- Related common block/menu logic is intended from `TomsStorage-1204`; local `util` wrappers and `block` classes form the Fabric boundary.
- Validate full/partial transfers, filters, recursive networks, disconnects, wireless access, chunk unloads, and save/reload.
