# JEI Integration

Own JEI discovery, ghost ingredients, and crafting-terminal recipe transfer for Forge 1.20.1.

- Keep JEI optional at runtime and preserve menu slot mapping, ingredient counts, and server-authoritative transfer through normal terminal packets.
- Inspect shared `gui`/`util`, platform-shared `PlatformRecipeTransferHandler`, main networking, and JEI Gradle dependencies.
- Validate with and without JEI, ghost placement, partial ingredients, full player inventory, and dedicated-server class loading.
