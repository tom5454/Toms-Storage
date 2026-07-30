# Menus And Screens

- Owns terminal/filter/link/emitter menus and screens, virtual storage slots, controls, and client search/sort behavior.
- Server menus must validate NBT messages and remain authoritative. Preserve compact keys, slot mapping, shift-click/remainders, incremental sync, and client-only separation.
- Inspect block entities, network packets, `TerminalSyncManager`, platform GUI adapters, and JEI/REI/EMI handlers before changes.
- Validate full/partial inventories, shift-click, crafting transfer, filtering, reconnects, sync ordering, malformed packets, and dedicated-server startup.
