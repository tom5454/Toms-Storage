# Client Models

- Owns NeoForge baked-model handling for painted blocks.
- Keep registration client-only and preserve source model transforms, render types, and paint texture resolution.
- Inspect `StorageModClient`, painted block/item classes, and loader/shared blockstate and model resources together.
- Validate client startup, resource reload, inventory rendering, placed rendering, and missing-model/texture logs.
