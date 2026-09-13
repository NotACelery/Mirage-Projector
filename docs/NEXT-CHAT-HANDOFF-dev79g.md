# NEXT CHAT HANDOFF — 0.1.0-dev.79g

## Recovery baseline

Use **`0.1.0-dev.79g`** as the latest recoverable source snapshot.

Platform/runtime:

- Minecraft 1.21.1;
- NeoForge 21.1.244+;
- Java 21;
- Gradle 9.2.1;
- network protocol **26**.

`dev.76h` remains the QA-confirmed STATIC_WORLD Mirage Light Engine baseline. `dev.77` differentiates Core Booster identities. `dev.78–79e` are the current projector-model/UX stabilization line.

## Current accumulated state

### Static Mirage Light

- server-authoritative STATIC_WORLD solver;
- revisioned chunk snapshots + manifests/requests;
- dependency-window readiness before publication;
- real occlusion/opacity/partial-block handling;
- exact open-air conceptual curve `15,15,14,14,...,1,1`;
- section-boundary invalidation from dev.76h;
- no current runtime path creates physical `crying_light_node` relays.

Repeated relog/chunk-load QA around Mature Clusters was accepted on dev.76h with stable convergence and no visible FPS regression.

### Projector models

The six temporary `*_alt` blocks remain Creative-visible for comparison. Accepted identities remain:

- Compact / Mirage Projector Alt;
- Display Alt low-corner identity;
- Wide Alt stepped horizontal V;
- Tall Alt continuous obsidian dome + purple inner chamber + magenta outer glass;
- Field Alt broad platform identity;
- Prism Alt accepted/frozen identity.

`dev.79` reconciled visible geometry with VoxelShapes. `dev.79a` finalized Tall. `dev.79b–d` attacked Compact/Wide/Field render overlap and introduced active-state UX.

## dev.79g delta

### Compact striped-artifact fix

In-game QA after dev.79d showed that Compact Alt still produced a horizontal purple striped artifact at the front/interior when viewed from above/oblique angles.

The remaining artifact was isolated to the **visible top/side surfaces of the extremely thin decorative emitter cuboids**, not to another solid-volume intersection. Their tiny default UV footprint aliased into the striped surface shown in QA.

Current Compact emitter contract:

- emitter child remains `minecraft:cutout`;
- front emitter keeps its accepted exterior position but renders **north face only**;
- rear emitter keeps its accepted exterior position but renders **south face only**;
- no emitter top/bottom/east/west face is rendered;
- emitter/chamber volumes remain free of solid-chassis intersection;
- accepted Compact VoxelShape bounds remain synchronized with the model.

This makes the decorative emitter a true outward-facing plate and removes the surface that generated the circled stripes.

### Source-workspace header layout

All four source workspaces now reserve two real header rows:

1. row 1: workspace title only;
2. row 2: `Use <mode> mode` / disabled `Mode currently Active` + `Back`.

Image, Item, Entity and Banner content was shifted downward together with any real container/menu slots so buttons no longer cover titles or inventories.

Important invariants retained from dev.79d:

- `open workspace != active source != projection enabled`;
- editing/saving Image does not silently activate Image;
- `Use <mode> mode` activates that source and turns projection ON;
- `TURN OFF` is non-destructive;
- main Image/Item/Entity/Banner button for the actually active source receives a white outline;
- no source gets the active outline while OFF;
- dormant End Resonance guard rejects TURN OFF when the future Dragon Egg resonance state owns controls and emits localized `This doesn't seem to work...`.

### Image workspace cleanup retained

The imported-image resolution/sizing debug text that previously overlapped preview labels remains removed.

## Verification

Run:

```text
python tools/verify_current_line.py
```

Current artifact-side result:

```text
CURRENT-LINE VERIFICATION PASS (17 gates)
```

The dev.79g gate additionally verifies:

- explicit persisted/synchronized projection ON/OFF state;
- End Resonance shutdown guard hook;
- Image workspace does not implicitly change SourceMode;
- white active-source outline + TURN OFF payload;
- image debug sizing line remains absent;
- Compact front/rear emitters expose only outward faces;
- Compact emitters do not intersect solid chassis volume;
- Image/Item/Entity/Banner action controls are on the second header row;
- menu slot coordinates were shifted with Item/Banner/Entity content;
- version `0.1.0-dev.79g`, protocol 26.

Static verification is not a substitute for the Windows NeoForge build.

## Immediate QA

1. Run `build.bat` on Windows/Java 21.
2. Recheck **Compact Alt at the exact blue-circled front/overhead angles** from QA. The horizontal purple striped surface should no longer exist.
3. Open Image, Item, Entity and Banner workspaces and confirm:
   - title is unobstructed;
   - mode/back row is directly below it;
   - `Mode currently Active` disables correctly;
   - slots/previews/inventory remain aligned.
4. Smoke-test `TURN OFF`, reactivation through each `Use <mode> mode`, and the white active-source outline.
5. Recheck Wide/Field/Tall/Display/Prism for visual regressions.

## Remaining 1.0.0 work

After model/header QA:

- choose winning Alt visuals and merge them into canonical projector IDs;
- explicitly remove/migrate temporary comparison IDs;
- renderer/release QA across opacity, water/clouds, equipment, trims/glint, nameplates and all source modes;
- multiplayer/asset stress;
- finish future-proofing seams such as the namespaced projection-source registry/provider boundary.

## Preserved 1.1.0 scope

1.1.0 remains the portable/dynamic-light and projector expansion:

- `DYNAMIC_VISUAL` light backend;
- Glow Dust battery/recharge;
- Focus/Flood/Ambient/Off lantern modes;
- handheld/presentation projectors;
- Mirage Scan Codex with multiple independent snapshots of the same species;
- Duplicating Lectern / physical Entity Scan Cards;
- hidden Dragon Egg **End Resonance** behavior for Field/Prism.

While End Resonance is active, normal source/shutdown controls are frozen. Pressing `TURN OFF` changes nothing and emits:

> `This doesn't seem to work...`

Normal controls return only after physically removing the Dragon Egg and restoring the suspended projector state.
