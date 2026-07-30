# REI Integration

Own REI plugin, ghost ingredients, and crafting-terminal transfer code compiled only when `-DuseLib` is present.

- Keep package paths aligned with the Gradle exclusion and loader-shared `REIPlugin_`; never make core initialization load REI classes when absent.
- Inspect shared `gui`/`util`, loader-shared `rei`, main networking, and optional REI/Cloth/Architectury dependencies.
- Validate `bash gradlew build -DuseLib=true`, the default exclusion build, recipe transfer edge cases, and dedicated-server startup without REI.
