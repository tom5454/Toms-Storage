# Forge Network Adapter

Own the Forge `SimpleChannel`, packet discriminators, `DataPacket` serialization, and server dispatch.

- Preserve channel ID/version, discriminator order, compact NBT keys, active-menu checks, enqueue direction, and server-authoritative validation.
- Inspect shared `gui`/`util`, loader-shared `OpenTerminalPacket`, main `platform`, and legacy Forge network adapters.
- Validate malformed and stale menu packets, insert/extract/crafting actions, wireless opening, client/server compatibility, and dedicated-server startup.
