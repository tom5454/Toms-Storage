# Block Package

- Provides Fabric-specific framed/painted cable and inventory-proxy block behavior for 1.20.4.
- Preserve block/entity IDs, placement state, painted NBT, server-authoritative mutation, and sided storage exposure; never lose or duplicate contents.
- Related behavior is in intended `TomsStorage-1204` common blocks/resources, local `tile` entities, and final `platform` registration.
- Validate placement/break drops, painting, proxy direction, block-state models, and client/server interaction.
