# Shared Blocks

Own loader-neutral block behavior for terminals, cables, connectors, trims, hoppers, emitters, and crates.

- Preserve registry IDs, block-state properties, paint behavior, menu opening, and cable/trim connectivity; avoid chunk loads or bypassing network-cycle safeguards.
- Inspect `Content`, matching block entities in `src/loader-shared/java/com/tom/storagemod/tile`, platform registration, block states, models, loot, recipes, and tags.
- Validate placement/removal, rotations and states, terminal opening, cable reconnection, painted variants, drops, and a dedicated-server launch.
