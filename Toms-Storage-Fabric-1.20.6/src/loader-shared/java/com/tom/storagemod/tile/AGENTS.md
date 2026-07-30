# Tile Package

- Implements 1.20.6 Fabric Transfer API entities for connectors, terminals, hoppers, crates, proxies, emitters, crafting, painting, and component-era persistence.
- Preserve transaction atomicity, exact remainders, component-aware `ItemVariant` equality, sided access, cycle checks, loaded-chunk guards, server authority, and persisted keys.
- Related common block/menu/component logic is intended from `TomsStorage-1206`; local `util` wrappers and `block` classes form the Fabric boundary.
- Validate partial/full transfers of component-distinct stacks, filters, recursion, disconnects, wireless access, chunk unloads, and save/reload.
