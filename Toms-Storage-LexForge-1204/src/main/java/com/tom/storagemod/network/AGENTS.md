# Legacy Forge Network Adapter

Own the Forge 1.20.4 `SimpleChannel`, packet registration, `DataPacket` serialization, and server dispatch.

- Preserve channel ID/version, discriminator order, compact NBT keys, direction/enqueue behavior, active-menu checks, and server authority.
- Inspect local `OpenTerminalPacket`, intended inherited menus/receivers, and Forge 1.20.1 plus NeoForge 1.20.4 network adapters.
- After restoring valid sources, validate malformed/stale packets, terminal/crafting actions, wireless opening, client/server compatibility, and dedicated-server startup.
