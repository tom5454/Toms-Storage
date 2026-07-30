# Main Java Layer

- This layer contains final Minecraft 1.20.6 Fabric adapters, including data-component registration support, not common gameplay implementations.
- Fabric API, Fabric Loader, and Trinkets calls may be used here; do not leak them into inherited shared/platform contracts.
- Preserve component identity through registry, item, sync, and optional-slot paths; do not reintroduce legacy item-tag assumptions.
- Resolve Java 21 versus `--release 17` before new language/API use; validate client/server loading, menus, registrations, components, and Trinkets absence.
