# Final NeoForge Java Layer

This layer contains the final 1.20.4 NeoForge platform adapters for registration, interactions, storage capabilities, and container screens.

- NeoForge and mapped 1.20.4 imports are allowed; Fabric imports are not.
- Keep client-only screen code out of dedicated-server loading paths and reusable logic in higher shared layers.
- Preserve capability simulation/remainder behavior, registry IDs, and NBT-era contracts.
- Validate compilation, client and dedicated-server startup, registration, menu opening, and inventory transfer behavior.
