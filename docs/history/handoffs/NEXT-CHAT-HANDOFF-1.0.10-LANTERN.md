# NEXT CHAT HANDOFF — Mirage Projector 1.0.10 Handheld Lantern

Date: 2026-09-13
Baseline: `1.0.10`
Minecraft: 1.21.1
NeoForge: 21.1.244+
Java: 21
Network protocol: `28`
ProjectionSettings format: `3`

## Snapshot purpose

This snapshot closes the first complete **mobile/handheld consumer** of the Mirage dynamic light foundation introduced before 1.0.9 and first consumed by the placed Mirage Light Projector in 1.0.9.

The implementation deliberately reuses the shared contracts rather than introducing a second lighting or battery system:

- `PortableLightMode`
- `RechargeableEnergyItem`
- `ClientDynamicMirageLightManager`
- `MirageLightEngine`
- `DYNAMIC_VISUAL`

## Delivered in 1.0.10

### Mirage Lantern

New item:

- `mirage_projector:mirage_lantern`

Properties:

- non-stackable;
- supports Glow Dust and Light Battery through `RechargeableEnergyItem`;
- stores the **exact ItemStack** of the inserted cell inside lantern `CUSTOM_DATA`;
- preserves cell charge/components across insertion, extraction, save/load and inventory movement;
- keeps a compact synchronized summary (`cell present`, `cell percent`, `mode`) for cheap client rendering/HUD access.

### Controls

While holding the lantern:

- normal RMB cycles `Focus -> Flood -> Ambient -> Off -> Focus` when usable power is available;
- sneak + RMB services the lantern using the **opposite hand**;
- empty lantern + rechargeable cell in opposite hand inserts one cell;
- loaded lantern + empty opposite hand extracts the exact stored cell;
- loaded lantern + occupied opposite hand refuses extraction and shows a hint rather than deleting/replacing anything.

The selected mode remains stored when the battery is removed or depleted.

### Energy consumption

Server-side drain happens only while the lantern ItemStack is actually in main hand or offhand.

Drain cadence is once per second and delegates to shared `PortableLightMode#chargePerSecond()`.

Current QA balance remains shared with the placed light projector:

| Mode | Range | Drain |
|---|---:|---:|
| Focus | 24 | 4/s |
| Flood | 16 | 2/s |
| Ambient | 12 | 1/s |
| Off | 0 | 0/s |

A depleted cell is retained. The lantern stops emitting but keeps its selected mode.

### Mobile dynamic light source

New client runtime:

- `ClientHeldLanterns`

For every visible player in the client level it examines main hand and offhand independently.

Stable source IDs:

- `MirageLightSourceId.entity("lantern_main", player.getUUID())`
- `MirageLightSourceId.entity("lantern_off", player.getUUID())`

The source follows vanilla-synchronized player state:

- origin near eye position;
- direction from current look vector;
- shared `PortableLightMode.profile(look)`;
- submitted to `ClientDynamicMirageLightManager`;
- source removed immediately when lantern is absent, Off or discharged.

No custom per-tick position/look packet was added. Remote clients reconstruct the mobile light from vanilla player tracking plus the synchronized held ItemStack state.

### HUD / tooltip

While held, the lantern refreshes compact actionbar feedback with:

- device name;
- selected mode;
- battery percentage;
- discharged state.

Spanish uses `Sin Cargar` for the discharged state.

Tooltip includes mode, battery state and service/use hints.

The vanilla item durability-style bar is repurposed to visualize stored battery percentage without consuming the lantern itself.

### Assets

Added:

- `assets/mirage_projector/models/item/mirage_lantern.json`
- `assets/mirage_projector/textures/item/mirage_lantern.png`

Texture is a true 16x16 RGBA pixel-art asset.

## Synchronization / compatibility decisions

### Network protocol remains 28

1.0.10 adds no Mirage custom payload format and therefore **does not bump** `NETWORK_PROTOCOL`.

Some 1.0.9 documentation incorrectly stated protocol 29 even though source remained 28. Active documentation was corrected in this snapshot.

### Exact nested cell vs lightweight summary

Canonical battery identity and charge live in the nested serialized cell ItemStack.

The duplicated summary fields are only a client-friendly cache for rendering/HUD decisions. Server mutations always refresh that summary.

This keeps arbitrary future `RechargeableEnergyItem` implementations compatible without flattening their components into lantern-specific fields.

## Main source files touched

- `src/main/java/celerbi/mirageprojector/item/MirageLanternItem.java`
- `src/main/java/celerbi/mirageprojector/client/ClientHeldLanterns.java`
- `src/main/java/celerbi/mirageprojector/client/ClientRuntimeEvents.java`
- `src/main/java/celerbi/mirageprojector/light/PortableLightMode.java`
- `src/main/java/celerbi/mirageprojector/registry/ModItems.java`
- `src/main/java/celerbi/mirageprojector/MirageProjector.java`
- `src/main/java/celerbi/mirageprojector/registry/ModCreativeTabs.java`

Resources/languages/docs/verifiers were updated accordingly.

## Automated verification

Current line result at snapshot close:

```text
MIRAGE PROJECTOR 1.0.10 VERIFICATION PASS (24 gates)
```

Release audit:

```text
Mirage Projector 1.0.10 release audit PASS
103 JSON
19 blocks
19 items
494 lang keys
```

Specific handheld gate:

```text
Mirage Projector 1.0.10 handheld lantern verification PASS
```

## Compile limitation of this environment

The source snapshot cannot be compiled here because:

- no system Gradle is installed;
- the project Windows launcher downloads Gradle 9.2.1;
- this container has no DNS/network access to `services.gradle.org`.

Therefore **Windows + Java 21 NeoForge build remains the compile gate**.

## Required in-game QA

Prioritize these before balancing/polish:

1. Build with Java 21 / NeoForge 21.1.244+.
2. Give yourself `mirage_projector:mirage_lantern`, Glow Dust and Light Battery.
3. Verify sneak+RMB insertion from opposite hand.
4. Verify exact partial charge survives insert/extract.
5. Verify normal RMB cycles Focus/Flood/Ambient/Off.
6. Verify one-second drain matches mode values.
7. Verify no drain when lantern is merely elsewhere in inventory.
8. Verify empty/depleted battery emits no light and is not destroyed.
9. Verify main-hand source follows player look and movement.
10. Verify offhand source works independently.
11. Two-client QA: remote player lantern follows remote movement/look and battery/mode changes without stale fields.
12. Verify putting lantern away / switching Off removes light immediately.
13. Verify walls/occlusion/detours still behave through the shared Mirage Light Engine.
14. Verify actionbar/HUD does not become noisy during normal play.

## Best next implementation chunk

The strongest next expansion chunk is to keep building on the now-complete stationary + handheld light foundation rather than starting an unrelated subsystem.

Recommended direction:

### Portable projector / handheld projection foundation

Reuse the same held-item synchronization and stable entity source identity pattern for projection devices:

- handheld Mirage projector item;
- carry a saved projection source/configuration;
- explicitly activate/deactivate without deleting config;
- derive pose/origin from player hand/look;
- preserve existing SourceMode contracts (Image / Item / Entity / Banner);
- share energy-cell contract with Glow Dust / Light Battery;
- consume power according to projection load;
- leave hooks for later Scan Codex copies/cards.

Alternative if QA exposes light-engine issues first: fix handheld remote tracking/performance before expanding.

## Do not regress

- Do not create a second dynamic-light engine for handheld devices.
- Do not flatten rechargeable cells into a lantern-only integer charge value.
- Do not consume Glow Dust/Light Battery ItemStacks as disposable fuel when the contract is rechargeable energy.
- Do not bump network protocol unless a custom Mirage payload contract actually changes.
- Do not make `DYNAMIC_VISUAL` persistent block light.
- Keep source cleanup deterministic when an item disappears, changes mode or loses charge.
