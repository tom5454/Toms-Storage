# Network Package

- Sends legacy 1.20.4 play traffic on stable raw `ResourceLocation` channels using `FriendlyByteBuf` and `CompoundTag` payloads.
- Preserve packet IDs and compact NBT keys; validate the active server menu and reject untrusted client counts, slots, positions, ownership, and channel data.
- Related receivers are in `StorageMod`; intended common menus and `IDataReceiver` define payload meaning, while `platform` handles mapped NBT reads.
- Validate click/search traffic, terminal sync, stale menu state, wireless open, reconnect, malformed data, and dedicated-server startup.
