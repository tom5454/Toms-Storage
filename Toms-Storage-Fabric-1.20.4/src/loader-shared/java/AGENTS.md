# Loader-Shared Java Layer

- This is the Minecraft 1.20.4 Fabric layer for lifecycle, legacy networking, Transfer API storage, block entities, models, configuration, and tags.
- Fabric API and loader-family integrations belong here; intended shared gameplay and platform-shared abstractions must remain free of Fabric imports.
- Preserve raw packet channel IDs, NBT payload shape, transaction semantics, and client/server class-loading boundaries.
- Compare changes with intended `TomsStorage-1204` common/platform code and the final local `platform` adapter; first prove all source roots are included.
