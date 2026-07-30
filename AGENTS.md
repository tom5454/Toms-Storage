# AGENTS.md

## Repository Overview

Tom's Simple Storage Mod is a Java Minecraft mod that provides storage and crafting terminals, inventory connectors, cables, wireless access, filters, hoppers, and inventory links. The mod ID and resource namespace are `toms_storage`; Java code uses the `com.tom.storagemod` package.

This repository is a collection of independent version/loader projects. It is **not** a Gradle multi-project build. There is no root `build.gradle`, root Gradle wrapper, aggregate test task, or CI workflow. Always choose one target project before editing or running Gradle.

## Project Matrix

| Directory | Loader | Minecraft | Java | Notes |
| --- | --- | --- | --- | --- |
| `Toms-Storage-120` | Forge | 1.20.1 | 17 | Primary 1.20.1 Forge line |
| `Toms-Storage-1202` | NeoForge | 1.20.2 | 17 | Reuses parts of the 1.20.1 source line |
| `Toms-Storage-1204` | NeoForge | 1.20.4 | 17 | Self-contained shared/platform sources |
| `Toms-Storage-1206` | NeoForge | 1.20.6 | 21 | Best reference for current NeoForge architecture |
| `Toms-Storage-Fabric-1.20` | Fabric | 1.20.1 | 17 | Fabric loader implementation |
| `Toms-Storage-Fabric-1.20.2` | Fabric | 1.20.2 | 17 | Reuses older shared and loader sources |
| `Toms-Storage-Fabric-1.20.4` | Fabric | 1.20.4 | 17 | Fabric loader implementation |
| `Toms-Storage-Fabric-1.20.6` | Fabric | 1.20.6 | 21 configured, `--release 17` | Resolve this contradiction before using Java 21 syntax |
| `Toms-Storage-LexForge-1202` | Forge | 1.20.2 | 17 | Legacy Forge compatibility port |
| `Toms-Storage-LexForge-1204` | Forge | 1.20.4 | 17 | Legacy Forge compatibility port |

The highest Minecraft version is not necessarily the highest mod release version. Do not infer the target from version numbers alone. State both the Minecraft version and loader in every task, change summary, and validation report.

## Source Composition

Each project composes its `main` source set from several physical layers instead of Gradle subprojects:

- `src/shared/java`: loader-neutral gameplay, blocks, items, menus/screens, terminal synchronization, recipes, and optional recipe-viewer integrations.
- `src/loader-shared/java`: code shared within a loader family, including entry points, configuration, networking, block entities, storage/capability wrappers, tags, and model hooks.
- `src/platform-shared/java`: Minecraft-version-specific abstractions that can still be shared across loaders.
- `src/main/java`: final loader-specific adapters, especially `com.tom.storagemod.platform.Platform` and client container helpers.
- `src/shared/resources`: common assets and data packs: block states, models, textures, translations, recipes, loot tables, advancements, and tags.
- `src/loader-shared/resources`: loader-sensitive tags, models, and integration resources.
- `src/platform-shared/resources`: version-specific common resources when present.
- `src/main/resources`: loader metadata, `pack.mcmeta`, access transformers, or access wideners.

Treat these as overlay layers. A file's physical location indicates its portability boundary:

1. Put loader-neutral behavior in `shared` when the target source composition supports it.
2. Put Forge/NeoForge capability code or Fabric transfer/storage code in `loader-shared`.
3. Put mapped Minecraft API differences in `platform-shared`.
4. Keep loader-family lifecycle and event wiring in `loader-shared`; reserve `main` for final adapters that differ between composed projects, such as registry and interaction implementations in `Platform`.

Do not import NeoForge APIs into common Fabric-consumed sources or Fabric APIs into Forge-consumed sources. Use `Platform`, `GameObject`, storage wrappers, and existing interfaces at the boundary.

### Important Source-Path Caveat

Several `build.gradle` files reference sibling directories named `TomsStorage-*` and `TomsStorageFabric-*`, while this checkout contains `Toms-Storage-*` and `Toms-Storage-Fabric-*`. This affects all four Fabric projects, both LexForge projects, and `Toms-Storage-1202`. Gradle silently accepts a missing source directory, so a build can omit expected shared code before eventually failing or, in some cases, provide misleading validation. In the checked-in layout, only `Toms-Storage-120`, `Toms-Storage-1204`, and `Toms-Storage-1206` have complete local source composition.

Before trusting one of these builds:

- inspect that project's `sourceSets` block;
- verify every referenced sibling directory exists in the current checkout/layout;
- do not rewrite paths or create symlinks as an unrelated cleanup;
- report a missing shared-source layout as a validation blocker unless the task explicitly includes fixing it.

## Architecture

### Initialization And Registration

`Content.java` is the canonical declaration point for blocks, items, block entities, menus, and, in 1.20.6, persistent data components. Static declarations are wrapped in `GameObject`; `Content.init()` forces initialization.

- NeoForge/Forge discovers `StorageMod` through `@Mod` and then registers lifecycle listeners, configs, networking, content, capabilities, and platform registries.
- Fabric discovers entry points through `fabric.mod.json`. `StorageMod.onInitialize()` registers common content, Fabric-only painted variants, block entities, payload handlers, config synchronization, lifecycle hooks, and tags.
- Client initialization is separate in `StorageModClient`: screens, key bindings, render layers, models/colors, and tooltips belong there.
- JEI, REI, and EMI are independently discovered plugins/entry points. Keep their annotations/interfaces, Fabric metadata entries, and optional source exclusions aligned; do not initialize them unconditionally from `StorageModClient`.
- `Platform.java` translates shared registration and interaction requests into the selected loader's APIs.

Adding content is not complete when Java compiles. Coordinate the applicable registrations with block states, models, item models, textures, loot tables, recipes, tags, advancements, translations, and loader metadata. Preserve historical dotted registry IDs such as `ts.storage_terminal`; registry IDs are world compatibility contracts.

### Storage Network Flow

The central runtime path is:

1. An inventory connector scans adjacent trims/cables and inventory providers every 20 server ticks.
2. It resolves loader storage APIs into an aggregate handler (`MultiItemHandler` on Forge/NeoForge, corresponding wrappers on Fabric), applies priorities, and rejects obvious recursive networks.
3. A storage terminal reads the adjacent aggregate, snapshots non-empty stacks as `StoredItemStack` totals, and performs insertion/extraction on the server.
4. `StorageTerminalMenu.broadcastChanges()` sends incremental state through `TerminalSyncManager`.
5. The client renders virtual storage slots rather than ordinary container slots.
6. Client search, sort, and click actions return compact NBT payloads through `DataPacket`; the active server menu implements `IDataReceiver` and performs the actual mutation.

All inventory mutations must remain server-authoritative. Preserve simulation/remainder semantics, item component/NBT equality, player inventory updates, and push-or-drop fallback behavior. Duplication and deletion bugs are the highest-risk regressions in this code.

### Wireless And Remote Links

- The wireless key path sends `OpenTerminalPacket`; the server locates a valid wireless terminal in player inventory and optional Curios/Trinkets slots before opening a menu.
- Wireless range depends on server config, beacon level, dimension rules, and terminal binding state.
- Inventory-link channels are stored in overworld `SavedData` under `toms_storage_rc`.
- Channel owner, visibility, and display name persist; loaded connector positions are only an in-memory cache and repopulate as connectors register.
- Remote lookup deliberately checks `isLoaded` and must not force-load chunks.

### Configuration

- Forge/NeoForge uses common and server config specs. Server values are world-specific under `saves/<world>/serverconfig`; common config includes multiblock inventory declarations.
- Fabric uses Cloth AutoConfig/Gson and synchronizes the server configuration to clients during login.
- Validation ranges and ownership differ by loader. A new option usually requires separate declaration, loading, synchronization, cache invalidation, and config-screen consideration.
- There are no `.env` contracts.
- `-DuseLib=true` enables optional REI/Cloth/Architectury dependencies in Forge/NeoForge builds. It is a presence/truthiness-tested JVM system property, not `-PuseLib=true`; even `-DuseLib=false` enables it, so omit the property to disable it.
- `-DmavenDir=/absolute/path` selects a filesystem Maven publication destination.

### External Integrations

- JEI, REI, and EMI integrate recipe transfer and ghost ingredients with crafting terminals.
- Curios on Forge/NeoForge and Trinkets on Fabric expose accessory slots for advanced wireless terminals.
- Fabric additionally uses Fabric API, Cloth Config, Mod Menu, Cardinal Components, and Architectury.
- Optional integration code must not load when its mod/API is absent. Keep metadata entry points and Gradle source exclusions synchronized with package/class changes.

## Compatibility Contracts

Treat the following as externally persistent or cross-side protocols. Do not rename or reinterpret them without an explicit migration plan:

- block, item, block-entity, menu, and data-component registry IDs;
- NBT keys written by block entities, items, menus, and `SavedData`;
- `WorldPos` and item data-component codecs;
- `toms_storage_rc` SavedData ID and inventory-link channel fields;
- compact network keys such as `s`, `c`, and `d` shared by screens and menus;
- packet IDs and `fabric.mod.json` entry-point class names;
- recipe, tag, model, translation, and loot resource locations.

Minecraft 1.20.6 moved important item state to data components. Do not backport component-based code mechanically to NBT-based older versions. Likewise, mapped signatures in access transformers/wideners are version-specific.

## Change Workflow

Before editing:

1. Identify the exact target matrix cell: Minecraft version plus loader.
2. Read that project's `build.gradle`, `gradle.properties`, and loader metadata.
3. Trace its `sourceSets`; determine whether the authoritative file is local or inherited from a sibling project.
4. Search the same class/resource name across all ports to understand loader and version differences.
5. Decide whether the fix is loader-neutral, loader-family-specific, Minecraft-version-specific, or final-platform-specific.

While editing:

- Make the smallest correct change in the highest reusable source layer that actually feeds the target builds.
- Do not bulk-copy a patch across all same-named files. Minecraft mappings, APIs, item persistence, payload registration, and capabilities differ by version and loader.
- If behavior should be consistent across ports, list every affected target explicitly and adapt each implementation deliberately.
- Preserve tabs and the existing compact Java style; opening braces stay on declaration lines.
- Keep client-only classes out of dedicated-server class-loading paths.
- Preserve the `toms_storage` namespace and existing dotted filename/registry conventions.
- Update `en_us.json` first when adding translatable content, then update other locales only when accurate translations are available.
- Never commit `.gradle/`, `build/`, `run/`, logs, local worlds, IDE metadata, or built JARs.

For a new block or item, check all applicable surfaces:

- `Content` and loader-specific registrations;
- block entity, capability/storage exposure, menu, and screen registration;
- block state, block/item model, texture, and painted-model handling;
- loot table, recipe, advancement, and item/block tags;
- English translation and creative-tab visibility;
- server/client networking and config if behavior is interactive;
- JEI/REI/EMI integration when crafting or ghost ingredients are involved.

## Performance And Safety Hotspots

- Connector scanning runs every 20 ticks and may traverse a large configurable area. Avoid forced chunk loads, repeated global scans, unbounded recursion, and avoidable allocations in this path.
- Recursive storage networks are guarded by handler resolution and depth checks. Preserve cycle detection when changing wrappers or proxies.
- Menu packets are weakly typed NBT. Validate active menu/state and never trust client-provided counts, slots, ownership, positions, or channel IDs.
- Inventory transfers must correctly handle partial insertion/extraction and remainders. Test full inventories, mixed item components, double chests, filtered connectors, nested handlers, and disconnects.
- Client/server separation is mandatory. Dedicated-server validation catches accidental references to screens, rendering, key bindings, or client-only integration classes.
- Do not change remote links to load chunks. Unloaded endpoints should remain unavailable until naturally loaded.
- Configurable scan limits and duplicate-network protections are intentional safeguards; do not bypass them for convenience.

## Build And Run

Run commands from the selected project directory, never from the repository root.

NeoForge 1.20.6 example:

```bash
cd Toms-Storage-1206
bash gradlew build
bash gradlew runClient
bash gradlew runServer
bash gradlew runGameTestServer
bash gradlew runData
```

Fabric 1.20.6 example, only after its sibling source paths are present or corrected as part of the task:

```bash
cd Toms-Storage-Fabric-1.20.6
bash gradlew build
bash gradlew runClient
bash gradlew runServer
```

Useful project-local commands:

```bash
bash gradlew tasks
bash gradlew test
bash gradlew build -DuseLib=true
bash gradlew publish -DmavenDir=/absolute/output/path
```

Builds resolve Minecraft, loader, and third-party artifacts from remote Maven repositories and therefore normally require network access. Standard outputs are project-local `.gradle/`, `build/`, and `run/`. Forge/NeoForge data generation writes to `src/generated/resources`; review generated changes before keeping them.

## Validation Expectations

There are currently no repository unit tests and no configured lint/formatter tool. GameTest run configurations exist, but no substantive GameTest suite is present. Forge 1.20.1's custom `test` dependency uses a mismatched generated client-task name in this checkout and is broken; `test` is not a reliable universal gate.

Use the strongest practical validation for the changed target:

1. Verify source-set paths exist and the edited file is included in the selected build.
2. Run `bash gradlew build` in every explicitly affected project.
3. For common gameplay or networking changes, smoke-test `runClient` and `runServer` where feasible.
4. For client registration/rendering changes, launch a client and inspect missing-model/texture logs.
5. For inventory changes, exercise insert, extract, shift-click, partial/full inventory, reconnect, filtered, and wireless paths.
6. For persistence changes, load an existing world/item and verify round-trip save/reload behavior.
7. For resource generation, inspect the diff; do not accept unrelated generated churn.

If a build cannot run because expected sibling source directories are absent, report that precise blocker. Do not claim validation based only on a project that did not compile the modified shared source.

## Release Metadata

- Each project owns its `mod_version` in `gradle.properties`. Fabric artifact names are also properties; Forge/NeoForge artifact names are generally set in `build.gradle`.
- Forge/NeoForge metadata lives under `src/main/resources/META-INF`; Fabric metadata lives in `src/main/resources/fabric.mod.json`.
- Root `version-check.json` and `version-check-nf.json` are manually maintained update/release metadata.
- There is no repository release automation, container deployment, or GitHub Actions pipeline.
- `publish -DmavenDir=...` targets an explicit filesystem Maven repository; the standard `publishToMavenLocal` task can also write outside the workspace to the user's Maven cache.

Version bumps and update JSON edits are release work. Do not include them in an ordinary bug fix unless requested. For metadata work, verify the declared Minecraft and loader ranges against the exact mapped target; several existing manifests use broad lower-bounded ranges, so do not copy or broaden compatibility ranges without deliberate testing.

## Key Reference Files

Use the 1.20.6 implementations as architecture references, then compare against the actual target port:

- `Toms-Storage-1206/src/shared/java/com/tom/storagemod/Content.java`: canonical content and component declarations.
- `Toms-Storage-1206/src/loader-shared/java/com/tom/storagemod/StorageMod.java`: NeoForge initialization.
- `Toms-Storage-Fabric-1.20.6/src/loader-shared/java/com/tom/storagemod/StorageMod.java`: Fabric initialization and config sync.
- `Toms-Storage-1206/src/main/java/com/tom/storagemod/platform/Platform.java`: NeoForge platform adapter.
- `Toms-Storage-Fabric-1.20.6/src/main/java/com/tom/storagemod/platform/Platform.java`: Fabric platform adapter.
- `Toms-Storage-1206/src/loader-shared/java/com/tom/storagemod/tile/InventoryConnectorBlockEntity.java`: network scan and aggregation.
- `Toms-Storage-1206/src/loader-shared/java/com/tom/storagemod/tile/StorageTerminalBlockEntity.java`: terminal inventory operations and wireless checks.
- `Toms-Storage-1206/src/shared/java/com/tom/storagemod/gui/StorageTerminalMenu.java`: virtual slots and server-authoritative interactions.
- `Toms-Storage-1206/src/shared/java/com/tom/storagemod/util/TerminalSyncManager.java`: incremental terminal synchronization.
- `Toms-Storage-1206/src/loader-shared/java/com/tom/storagemod/util/RemoteConnections.java`: inventory-link persistence and loaded-chunk behavior.
- `Toms-Storage-1206/build.gradle`: NeoForge source composition and run tasks.
- `Toms-Storage-Fabric-1.20.6/build.gradle`: Fabric dependencies and sibling source composition.
