# Final NeoForge Java Layer

This layer contains the 1.20.2 NeoForge-specific network and platform adapters that complete the composed source set.

- NeoForge and mapped 1.20.2 imports are allowed here; Fabric imports are not.
- Keep client-only screen adapters out of dedicated-server loading paths.
- Preserve the NBT-based packet contract and delegate reusable behavior to inherited shared or local loader-shared code.
- Validate with compilation plus client/server packet and interaction smoke tests after resolving the project source-set blocker.
