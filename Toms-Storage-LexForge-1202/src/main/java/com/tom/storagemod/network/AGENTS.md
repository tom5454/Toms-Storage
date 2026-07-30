# Legacy Forge Network Adapter

Own the Forge 1.20.2 `SimpleChannel`, packet registration, `DataPacket` serialization, and server dispatch.

- Preserve channel ID/version, discriminator order, compact NBT keys, direction/enqueue behavior, active-menu checks, and server authority.
- Inspect the Forge 1.20.1 and NeoForge 1.20.2 adapters plus the intended inherited `OpenTerminalPacket`, menus, and `IDataReceiver`; account for the missing sibling composition.
- After restoring valid sources, validate malformed/stale menu packets, terminal and crafting actions, wireless opening, client/server compatibility, and dedicated-server startup.
