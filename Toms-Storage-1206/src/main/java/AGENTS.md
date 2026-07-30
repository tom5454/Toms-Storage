# Final NeoForge Java Layer

This layer contains final 1.20.6 NeoForge adapters for registration, interactions, item handlers, data components, and container screens.

- NeoForge and mapped 1.20.6 imports are allowed; Fabric imports are not.
- Keep client-only screen code out of dedicated-server loading paths and reusable logic in higher shared layers.
- Preserve item-handler simulation/remainders, component registration/codecs, registry IDs, and payload contracts.
- Validate compilation, client and dedicated-server startup, registration, menu opening, component round trips, and inventory transfers.
