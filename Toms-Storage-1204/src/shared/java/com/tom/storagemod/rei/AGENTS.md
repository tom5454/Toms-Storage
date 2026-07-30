# REI Integration

- Provides REI crafting-terminal transfer and ghost ingredients when `useLib` enables this package.
- Do not leak REI classes into unconditional initialization; preserve slot/count mapping and server authority.
- Inspect loader-shared `REIPlugin_`, crafting menus/screens, JEI/EMI equivalents, and Gradle exclusion/dependency logic.
- Validate a normal build without `useLib` and `bash gradlew build -DuseLib=true`, then smoke-test transfer with REI present.
