# Block Package

- Provides Fabric-specific framed/painted cable and inventory-proxy block behavior and bridges interactions to block entities.
- Preserve block/entity registration IDs, placement state, painted NBT, server-authoritative mutation, and sided storage exposure; never lose or duplicate inventory contents.
- Related behavior is in intended `TomsStorage-120` common blocks/resources, local `tile` entities, and final `platform` registration.
- Validate placement/break drops, painting, proxy direction, block-state models, and client/server interaction.
