# Final Legacy Forge Java Layer

This local layer owns the final Minecraft 1.20.2 Forge channel and platform adapters; gameplay and loader-family code are intended to arrive from configured sibling overlays.

- Forge 1.20.2 APIs are allowed; NeoForge and Fabric APIs are not. Keep reusable behavior in its actual shared owner rather than duplicating it here.
- The configured no-hyphen shared/platform siblings are absent, so inspect `build.gradle` and compare hyphenated Forge 1.20.1 and NeoForge 1.20.2 counterparts without assuming either is compiled.
- Preserve packet discriminators, registry IDs, capability semantics, mapped signatures, and client/server class separation.
