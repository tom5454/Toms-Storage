# Loader Resources

- Contains Fabric-only painted blockstates/models, recipes/loot, item/block tags, and Trinkets entity and belt-slot data for 1.20.1.
- Keep IDs synchronized with Java registrations, inherited common assets/data, and `src/main/resources/fabric.mod.json`; keep Trinkets resources aligned with optional wireless-slot lookup.
- If entry points or widened members change, update `fabric.mod.json` or the mapped access widener in main resources together; do not copy mappings from another Minecraft version.
- Validate JSON/resource loading in a client, missing-model logs, recipes/loot/tags, Trinkets-equipped wireless access, and a server data-pack load.
