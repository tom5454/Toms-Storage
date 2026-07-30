# NeoForge Loader-Shared Resources

- Owns NeoForge/Curios-sensitive recipes, tags, Curios slot data, and the painted-trim blockstate overlay.
- Preserve `toms_storage` IDs and dotted filenames; synchronize with loader blocks/items, `Content`, shared models/loot/translations, and Curios lookup code.
- Avoid duplicating loader-neutral assets that belong in `src/shared/resources`.
- Validate data/resource loading, recipes and tags, Curios belt behavior, painted trim rendering, and missing-resource logs.
