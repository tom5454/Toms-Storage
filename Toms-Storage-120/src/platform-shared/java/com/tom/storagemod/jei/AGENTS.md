# JEI Platform Adapter

Own the Minecraft 1.20.1-specific JEI recipe transfer bridge.

- Preserve crafting slot indices, ingredient quantities, remainder handling, and the boundary between JEI callbacks and server-authoritative menu actions.
- Inspect shared `jei`, shared `gui`/`util`, main networking, and later-version platform transfer handlers.
- Validate shaped/shapeless transfers, missing and partial ingredients, full inventory, ghost-only behavior, and absence of JEI at runtime.
