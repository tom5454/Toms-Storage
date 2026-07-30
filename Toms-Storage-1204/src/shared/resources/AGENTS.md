# Shared Resources

- Owns loader-neutral `toms_storage` assets and data: language, textures, models, blockstates, recipes, loot, advancements, tags, and the icon.
- Preserve registry/resource IDs and dotted filenames. Synchronize content changes with `Content`, blocks/items, menus, painted variants, recipes, loot, tags, and `en_us.json`.
- Keep loader-specific Curios and NeoForge overlays out of this layer; do not hand-edit generated resources here as a substitute for reviewing `runData` output.
- Validate resource/data loading, recipes, drops, translations, models/textures, and missing-resource logs on client and server.
