# Loader-Shared Java Layer

- This is the Minecraft 1.20.6 Fabric layer for lifecycle, custom payload registration, Transfer API storage, block entities, components, models, config, and tags.
- Fabric API and loader integrations belong here; intended shared gameplay and platform-shared abstractions must remain free of Fabric imports.
- Register payload types before receivers, preserve stream codecs/IDs and server authority, and preserve component-aware item identity and transaction semantics.
- Resolve Java 21 versus `--release 17`; compare with intended `TomsStorage-1206` counterparts and prove source inclusion before validation.
