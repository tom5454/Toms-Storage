# NBT Network Transport

- Owns 1.20.4 channel registration, generic `DataPacket` dispatch, and wireless terminal open requests.
- Preserve compact NBT keys, encode/decode symmetry, active-menu checks, server authority, and server-side wireless item/range/ownership resolution.
- Inspect shared GUI and `IDataReceiver`, wireless items, terminal block entities, config, and 1.20.2/1.20.6 network counterparts.
- Validate malformed packets, wrong-menu rejection, terminal actions, wireless/Curios paths, and dedicated-server startup.
