# Network Package

- Sends 1.20.6 `CustomPacketPayload` records registered through `PayloadTypeRegistry`, with stable IDs and stream codecs; `DataPacket` semantics still use compact NBT.
- Register codecs on the correct S2C/C2S side before receivers; validate active menus and reject untrusted client counts, slots, positions, ownership, and channel data.
- Related payload records and `IDataReceiver` contracts are in intended common code; `StorageMod` owns registration/receivers and `platform` supports mapped buffer reads.
- Validate codec round trips, click/search and terminal sync, stale menus, wireless open, reconnect, malformed data, and dedicated-server startup.
