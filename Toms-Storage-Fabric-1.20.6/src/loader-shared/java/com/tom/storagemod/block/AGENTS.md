# Block Package

- Provides 1.20.6 Fabric framed/painted cable and inventory-proxy block behavior and component-era block-entity handoff.
- Preserve IDs, placement state, serialized painted data, component-bearing stacks, server authority, and sided storage exposure; never lose or duplicate contents.
- Related behavior is in intended `TomsStorage-1206` common blocks/resources, local `tile` entities, and final component-aware `platform` registration.
- Validate placement/break with component-bearing items, painting, proxy direction, models, save/reload, and client/server interaction.
