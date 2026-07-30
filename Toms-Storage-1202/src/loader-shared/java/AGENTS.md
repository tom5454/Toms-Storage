# NeoForge Loader-Shared Java Layer

This layer owns NeoForge-family lifecycle, configuration, capabilities, networking hooks, block entities, storage wrappers, tags, and client model wiring for 1.20.2.

- NeoForge APIs are allowed; Fabric APIs are not. Keep generally reusable gameplay in the inherited shared layer.
- Client-only imports belong behind client initialization or in explicitly client-only classes.
- Preserve NBT-era persistence and packet behavior; do not use 1.20.6 data-component or payload-codec APIs.
- Validate with build, client startup, dedicated-server startup, and focused inventory/network smoke tests after the inherited-source blocker is resolved.
