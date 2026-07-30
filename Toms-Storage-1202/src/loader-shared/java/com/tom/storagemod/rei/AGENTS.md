# Optional REI Bootstrap

- Bridges NeoForge discovery to the inherited REI integration and is compiled only when the JVM system property `useLib` is present.
- Do not make core or client initialization load REI classes when the integration is excluded or REI is absent.
- Inspect the inherited shared `rei` package, Gradle exclusions, and optional dependency declarations together.
- Validate a normal build without `useLib` and `bash gradlew build -DuseLib=true` once the source-set blocker is resolved.
