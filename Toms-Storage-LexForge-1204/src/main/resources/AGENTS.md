# Legacy Forge 1.20.4 Resources

Own local `pack.mcmeta`, Forge metadata, and access-transformer inputs; common assets/data are intended to come from the absent configured sibling.

- Keep exact Forge 1.20.4 mapped transformer signatures and mod ID stable. Treat current broad Forge 48/Minecraft 1.20.2 lower bounds as deliberate compatibility metadata requiring validation, not copyable defaults.
- Synchronize metadata and transformer changes with Java call sites and packaged inherited resources once composition is valid; do not add guides inside `META-INF`.
- Validate source paths first, then `bash gradlew build`, packaged resources, client metadata loading, and dedicated-server transformer behavior.
