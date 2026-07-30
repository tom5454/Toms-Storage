# Forge Block Entities

Own connector scans, aggregate storage access, terminals, crafting, hoppers, emitters, proxies, painting, and cable connector state.

- Highest risk is item duplication/deletion: preserve capability simulation and remainders, cycle/depth guards, 20-tick scan behavior, loaded-chunk checks, NBT keys, and server authority.
- Inspect shared `block`/`gui`/`item`, loader-shared `util`, main `platform`/`network`, `Content`, and equivalent legacy Forge block entities.
- Validate partial/full insert/extract, double chests, filters/priorities, nested proxies, disconnects, wireless access, save/reload, and unloaded endpoints.
