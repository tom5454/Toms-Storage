# JEI Version Adapter

- Adapts crafting-terminal recipe transfer to the 1.20.2 JEI/Minecraft API boundary.
- Preserve server-authoritative crafting, slot mapping, ingredient counts, and menu validity; this project excludes `jei/**` from compilation.
- Inspect inherited shared JEI handlers, crafting menus, platform recipe abstractions, and a target that actually compiles this adapter before edits.
- Validation here requires a consuming build with JEI enabled; this project's ordinary build is not evidence because the package is excluded.
