# NeoForge Loader-Shared Java Layer

This layer owns NeoForge-family lifecycle, configuration, payload registration, capabilities, block entities, storage wrappers, tags, and client model wiring for 1.20.6.

- NeoForge APIs are allowed; Fabric APIs are not. Keep reusable gameplay, components, and payload definitions in `src/shared/java`.
- Client imports belong behind client initialization or in explicitly client-only classes.
- Register shared custom payload codecs and data components without changing their IDs or wire/persistence formats.
- Validate build, client startup, dedicated-server startup, payload registration, and focused inventory behavior.
