# NeoForge Loader-Shared Java Layer

This layer owns NeoForge-family lifecycle, configuration, networking, capabilities, block entities, storage wrappers, tags, and client model wiring for 1.20.4.

- NeoForge APIs are allowed; Fabric APIs are not. Keep reusable gameplay in `src/shared/java`.
- Client imports belong behind client initialization or in explicitly client-only classes.
- Preserve NBT-era persistence and packet behavior; do not use 1.20.6 data-component or payload-codec APIs.
- Validate build, client startup, dedicated-server startup, and focused inventory/network behavior.
