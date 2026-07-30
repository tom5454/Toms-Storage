# Model Package

- Implements Fabric client baked-model handling for painted blocks.
- Keep rendering code out of server initialization and synchronize model IDs and variants with loader-shared blockstates and inherited common models.
- Related code is `StorageModClient`, painted blocks/entities, and the intended common/platform model hooks.
- Validate a client launch, resource reload, every painted variant, and logs for missing models or textures; also smoke-test a dedicated server.
