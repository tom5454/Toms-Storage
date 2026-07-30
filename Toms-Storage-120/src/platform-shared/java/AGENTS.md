# Minecraft 1.20.1 Platform Layer

This layer owns mapped Minecraft 1.20.1 recipe, screen-widget, menu, and `SavedData` signature differences shared across loaders where possible.

- Minecraft and project abstractions are allowed; Forge, NeoForge, Fabric, capabilities, and loader lifecycle imports are not.
- Inspect corresponding 1.20.2/1.20.4 platform layers and final adapters before changing mapped signatures; do not copy newer data-component APIs here.
- Preserve recipe placement, menu synchronization, client-only boundaries, and `SavedData` compatibility.
