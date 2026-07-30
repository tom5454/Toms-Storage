# Legacy Forge Storage Utilities

Own Forge 1.20.4 `IItemHandler` aggregation/filtering, registration wrappers, remote links, filters, and inventory-link contracts.

- Preserve simulation/remainders, handler ordering, cycle detection, `toms_storage_rc`, owner/visibility fields, and in-memory loaded-position caching without forced chunk loads.
- Inspect local `tile`, final `platform`, intended shared items/utilities, and Forge 1.20.1 plus NeoForge 1.20.4 counterparts before changing interfaces.
- Validate partial handlers, priorities, recursion, endpoint unload/reload, world save/reload, channel permissions, and missing endpoints.
