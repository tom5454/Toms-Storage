# Tile Package

- Implements Fabric Transfer API block entities for connectors, terminals, hoppers, crates, proxies, emitters, crafting state, and painted state.
- Preserve transaction atomicity, simulation/remainders, sided access, cycle checks, loaded-chunk guards, server authority, and all persisted NBT keys.
- Related common block/menu logic comes from intended `TomsStorage-120`; local `util` wrappers and `block` classes provide the Fabric storage boundary.
- Validate full/partial insert and extract, filtered and recursive networks, disconnects, wireless access, chunk unloads, and save/reload.
