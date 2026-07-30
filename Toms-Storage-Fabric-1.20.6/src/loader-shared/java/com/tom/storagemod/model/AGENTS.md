# Model Package

- Implements Fabric client baked-model handling for painted 1.20.6 blocks.
- Keep rendering code out of server initialization and synchronize model IDs/variants with loader blockstates and intended common models.
- Related code is `StorageModClient`, painted blocks/entities, component-bearing item displays, and intended common/platform model hooks.
- Validate client launch/resource reload, painted variants and component-bearing stacks, missing-model logs, and dedicated-server startup.
