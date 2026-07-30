# Loader-Neutral Java Layer

This layer owns gameplay declarations, components, payload definitions, blocks, items, menus/screens, synchronization, and recipe-viewer integrations portable across loaders for Minecraft 1.20.6.

- Minecraft and project abstraction imports are allowed; NeoForge and Fabric APIs are not.
- Cross loader boundaries through `Platform`, `GameObject`, storage interfaces, and existing wrappers.
- Use registered data components for defined item state and typed custom payload codecs for network transport; preserve codec compatibility.
- Validate every consuming loader build when shared behavior changes, plus client/server smoke tests and persistence/network round trips.
