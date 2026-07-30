# Legacy Forge Models

Own Forge 1.20.4 baked-model behavior for painted blocks.

- Keep rendering client-only and preserve model data, tint, particle, transform, and fallback behavior without dedicated-server class-loading leaks.
- Inspect `StorageModClient`, local painted block entities, loader resource models/states, and Forge 1.20.1 plus NeoForge 1.20.4 model counterparts.
- Validate painted variants in world/inventory, resource reload, missing-model logs, and dedicated-server startup.
