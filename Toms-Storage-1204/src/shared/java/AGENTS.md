# Loader-Neutral Java Layer

This layer owns gameplay declarations, blocks, items, menus/screens, synchronization utilities, and recipe-viewer integrations portable across loaders for Minecraft 1.20.4.

- Minecraft and project abstraction imports are allowed; NeoForge and Fabric APIs are not.
- Cross loader boundaries through `Platform`, `GameObject`, storage interfaces, and existing wrappers.
- Keep NBT-based item and menu protocols compatible; do not introduce 1.20.6 data components or payload codecs.
- Validate every consuming loader build when shared behavior changes, plus client/server smoke tests appropriate to the feature.
