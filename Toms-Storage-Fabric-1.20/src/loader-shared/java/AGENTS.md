# Loader-Shared Java Layer

- This is the Minecraft 1.20.1 Fabric implementation layer for lifecycle, legacy networking, Transfer API storage, block entities, models, configuration, and tags.
- Fabric API and loader-family integrations belong here; shared gameplay and platform-shared abstractions must remain free of Fabric imports.
- Preserve raw channel IDs, NBT packet shape, transaction commit/simulation behavior, and dedicated-server separation.
- Compare changes with the intended `TomsStorage-120` common/platform layers and the final local `platform` adapter; validate build inclusion before relying on tests.
