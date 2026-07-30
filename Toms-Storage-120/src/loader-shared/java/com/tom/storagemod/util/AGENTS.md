# Forge Storage Utilities

Own Forge `IItemHandler` aggregation/filtering, registrations, remote links, inventory-link contracts, filters, and info helpers.

- Preserve simulation/remainder behavior, handler ordering, cycle detection, `toms_storage_rc` persistence, owner/visibility fields, and the in-memory-only loaded-position cache.
- Inspect loader-shared `tile`, shared `item`/`util`, main `platform`, config, and legacy Forge counterparts before changing interfaces.
- Validate partial handlers, priorities, recursive networks, endpoint unload/reload, world save/reload, channel permissions, and no forced chunk loads.
