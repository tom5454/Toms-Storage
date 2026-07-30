# Network Package

- Sends legacy 1.20.1 play traffic on stable raw `ResourceLocation` channels using `FriendlyByteBuf` and `CompoundTag` payloads.
- Preserve packet IDs and compact NBT keys; validate the active server menu and never trust client counts, slots, positions, ownership, or channel data.
- Related receivers are registered by `StorageMod`; common menus and `IDataReceiver` define payload meaning, while `platform` provides NBT buffer compatibility.
- Validate client-to-server clicks/search, server-to-client terminal sync, malformed/stale menu state, wireless open, reconnect, and dedicated-server startup.
