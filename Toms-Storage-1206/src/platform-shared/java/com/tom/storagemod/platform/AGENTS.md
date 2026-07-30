# Minecraft 1.20.6 Abstractions

- Owns mapped block base, recipe, recipe-book button, edit-box, and menu differences shared across loaders.
- Keep loader APIs out and preserve behavior expected by shared blocks, menus, screens, recipes, components, and payload callers.
- Inspect final `Platform`, shared callers, loader block entities, and adjacent version implementations before mapped changes.
- Validate block lifecycle, recipe lookup/transfer, widget behavior, and menu interaction through each consuming loader build.
