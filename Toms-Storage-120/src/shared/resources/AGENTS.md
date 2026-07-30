# Shared Resources

Own loader-neutral `toms_storage` assets and data: block states, models, textures, translations, loot, recipes, advancements, and tags.

- Preserve dotted resource locations and registry-linked paths. Synchronize content changes with `Content`, blocks/items, loot, recipes, tags, models, textures, and `en_us.json` first.
- Do not place Forge-only Curios data or loader metadata here. Check loader-shared overlays for deliberate overrides.
- Validate resource loading in client logs, block/item rendering, recipes, drops, tags, translations, and generated-resource diffs.
