# Typed Payloads

- Defines loader-neutral 1.20.6 custom payload records/types/codecs for terminal data and wireless open requests.
- Treat payload IDs, codec order/types, bounds, and menu dispatch as cross-side contracts. Validate all client input server-side and never trust positions, counts, ownership, or range.
- Inspect loader-shared `NetworkHandler`, GUI send/receive sites, wireless items, terminal block entities, and 1.20.4 NBT packet predecessors.
- Validate codec round trips, malformed/truncated data, wrong-menu rejection, terminal actions, wireless authorization, and client/server version agreement.
