# Legacy Forge 1.20.2 Resources

Own local `pack.mcmeta`, Forge metadata, and access-transformer inputs; common assets/data are intended to come from absent configured sibling layers.

- Keep `mods.toml`, Forge/Minecraft ranges, mod ID, and mapped access-transformer signatures aligned with exact Minecraft 1.20.2 Forge behavior.
- Synchronize metadata and transformer changes with local adapters and inherited registrations/resources once source composition is valid; do not add guides inside `META-INF`.
- Validate source paths first, then `bash gradlew build`, packaged resource contents, client metadata loading, and dedicated-server transformer behavior.
