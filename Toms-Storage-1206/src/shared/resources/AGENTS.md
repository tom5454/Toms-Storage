# Shared Resources

- Owns loader-neutral `toms_storage` assets and data: language, textures, models, blockstates, recipes, loot, advancements, tags, and the icon.
- Preserve registry/resource IDs and dotted filenames. Synchronize content/component changes with `Content`, blocks/items, menus, painted variants, recipes, loot, tags, and `en_us.json`.
- Keep loader-specific Curios and NeoForge overlays out of this layer; review rather than blindly accept `runData` output.
- Validate resource/data loading, component-backed item models/tooltips, recipes, drops, translations, models/textures, and missing-resource logs.
