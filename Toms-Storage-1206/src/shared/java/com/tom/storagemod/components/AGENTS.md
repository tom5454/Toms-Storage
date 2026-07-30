# Item Data Components

- Defines codec-backed filter state used by 1.20.6 items and registered through `Content`/`Platform`.
- Treat component IDs, codec fields, defaults, equality, and malformed-data behavior as persistence contracts; do not fall back to legacy item NBT.
- Inspect item/filter consumers, loader filter handlers, `Content`, platform registration, network payloads, and 1.20.4 NBT predecessors.
- Validate encode/decode round trips, world and inventory reload, copied/stacked items, empty/default state, and malformed persisted data.
