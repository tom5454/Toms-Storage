# Minecraft 1.20.2 Abstractions

- Owns mapped recipe, recipe-book button, edit-box, menu, and `SavedData` factory differences shared across loaders.
- Keep loader APIs out and preserve behavior expected by shared menus, screens, recipes, and remote-link persistence.
- Inspect final `Platform`, inherited shared GUI/recipe callers, local remote connections, and adjacent version implementations.
- Validate recipe lookup/transfer, widget behavior, menu interaction, and save/reload through each consuming loader build.
