# Forge Models

Own Forge baked-model behavior for painted blocks.

- Keep rendering client-only and preserve model-data, tint, particle, transform, and fallback behavior without server class-loading leaks.
- Inspect `StorageModClient`, shared painted blocks/items, loader resource models/block states, and legacy Forge model counterparts.
- Validate all painted variants, inventory and world rendering, resource reload, missing-model logs, and dedicated-server startup.
