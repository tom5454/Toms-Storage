# Model Package

- Implements Fabric client baked-model handling for painted 1.20.4 blocks.
- Keep rendering code out of server initialization and synchronize model IDs/variants with loader-shared blockstates and intended common models.
- Related code is `StorageModClient`, painted blocks/entities, and the intended common/platform model hooks.
- Validate client launch and resource reload, every painted variant, missing-model/texture logs, and dedicated-server startup.
