# NeoForge Block Entities

- Owns storage network scanning, capability aggregation, terminals, connectors, hoppers, emitters, proxies, and painted block entities.
- Inventory mutation is server-authoritative. Preserve simulation/remainders, 20-tick scan limits, cycle detection, NBT keys, loaded-chunk checks, and push-or-drop behavior.
- Inspect inherited shared blocks/menus/items, local util handlers, `Content`, `Platform`, and equivalent 1.20.4 block entities before changes.
- Validate insert/extract and partial/full inventories, reconnects, filters, recursive networks, wireless range, save/reload, and unloaded remote endpoints.
