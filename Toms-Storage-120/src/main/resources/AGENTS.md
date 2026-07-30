# Forge 1.20.1 Metadata Resources

Own `pack.mcmeta` and Forge metadata/access-transformer inputs for the final 1.20.1 artifact.

- Keep `mods.toml`, Forge/Minecraft ranges, access-transformer mapped signatures, mod ID, and update metadata aligned with this exact Forge 1.20.1 target.
- Synchronize access changes with Java call sites and verify optional integrations remain optional; do not create guidance files inside `META-INF`.
- Validate `bash gradlew build`, inspect the packaged JAR inputs, and launch client and dedicated server for metadata or transformer changes.
