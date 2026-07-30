# Minecraft 1.20.4 Abstractions

- Owns mapped recipe, recipe-book button, edit-box, menu, and `SavedData` factory differences shared across loaders.
- Keep loader APIs out and preserve behavior expected by shared menus, screens, recipes, and remote-link persistence.
- Inspect final `Platform`, shared GUI/recipe callers, loader remote connections, and adjacent version implementations.
- Validate recipe lookup/transfer, widget behavior, menu interaction, and save/reload through each consuming loader build.
