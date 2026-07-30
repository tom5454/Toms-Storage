# Forge-Shared Network

Own the wireless terminal open request consumed by the final Forge channel.

- Treat the request as untrusted: verify the active player, item/binding, range, dimension, config, and optional Curios location server-side.
- Inspect main `network`, shared wireless items, terminal block entities, config, and legacy Forge packet counterparts.
- Validate inventory and Curios keys, missing/invalid bindings, out-of-range and cross-dimension denial, and absent Curios.
