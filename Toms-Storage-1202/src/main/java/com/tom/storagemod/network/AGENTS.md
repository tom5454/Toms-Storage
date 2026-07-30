# Final Network Adapters

- Owns 1.20.2 `DataPacket` transport and channel registration.
- Keep active-menu checks, `IDataReceiver` dispatch, compact NBT keys, and server authority intact; malformed client data must not mutate arbitrary state.
- Inspect loader-shared `network/OpenTerminalPacket`, inherited shared menus/screens, and 1.20.4's equivalent network package before changing protocol behavior.
- Validate packet encode/decode, wrong-menu rejection, terminal actions, wireless opening, and dedicated-server startup.
