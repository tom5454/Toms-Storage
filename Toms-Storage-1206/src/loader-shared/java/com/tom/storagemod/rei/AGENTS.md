# Optional REI Bootstrap

- Bridges NeoForge discovery to shared REI integration and is compiled only when `useLib` is present.
- Do not make core or client initialization load REI classes when the package is excluded or REI is absent.
- Inspect shared `rei`, Gradle exclusions/dependencies, metadata discovery, and adjacent version implementations together.
- Validate a normal build without `useLib` and `bash gradlew build -DuseLib=true`.
