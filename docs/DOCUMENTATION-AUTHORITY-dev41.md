# Documentation authority map — dev.41

This file exists to prevent historical dev notes from silently becoming current design again.

## Current authority order

When documents disagree, use this order:

1. current source code for **implemented behavior**;
2. `docs/CURRENT-IMPLEMENTATION-AUDIT-dev41.md` for what is actually implemented vs planned;
3. focused current contracts listed below;
4. README / DEVELOPMENT current summaries;
5. CHANGELOG and older `devXX` documents only as historical traceability.

A planned design document may intentionally define the *next* implementation and therefore differ from source. Such a section must say `planned/not implemented` explicitly.

## Authoritative current implementation contracts

- `POWER-SYSTEM-REWORK-dev38.md` — current PU architecture, nominal ranges, overdrive, Ghost rebate.
- `CHASSIS-IMAGE-LAYOUT-POWER-UX-dev38.md` — current six chassis geometry/layout intent, with later Prism/Image addenda below taking precedence where applicable.
- `ENTITY-WORKSPACE-LIFETIME-dev38.md` — Entity workspace snapshot lifetime/cleanup.
- `GIF-ANIMATED-IMAGE-dev39.md` — GIF pipeline.
- `IMAGE-FORMAT-IMPORT-CONTRACT-dev39.md` — content-sniffed static/GIF import and animated WebP/APNG rejection.
- `WIDE-TALL-SINGLE-ASPECT-dev39.md` — Wide/Tall SINGLE sizing.
- `PRISM-ADAPTIVE-ASPECT-dev39.md` — Prism four-face adaptive aspect/PU behavior.
- `ENTITY-DIMENSION-NORMALIZATION-dev40.md` — projection-only dimension normalization for Piglin/Hoglin.
- `CURRENT-IMPLEMENTATION-AUDIT-dev41.md` — current source inventory/status.

## Authoritative planned progression contract

- `CORES-AND-UPGRADES-dev41.md` — Cut Obsidian Shards, Crying Obsidian natural conversion, Obsidian Spike, chassis crafting progression, state-preserving upgrades, Improved Cores, Beacon relay behavior and future Scan Codex note.

This design is not automatically implemented merely because it is authoritative for future work.

## Historical documents that must not govern current behavior

The following remain useful for archaeology but describe superseded states in whole or in part:

- `GUI-ARCHITECTURE-dev19.md` — contains old Core PU/hard-cap values;
- `POWER-POSE-UX-dev29.md` — predates dev.38 Power rework;
- `MULTI-SOURCE-IMAGE-LAYOUTS-dev33.md` — accidental Field 3x3 layout, explicitly superseded;
- `ENTITY-NAMEPLATE-DEPTH-ORDER-dev36.md` — dev.36 render-order attempt failed QA;
- `RENDER-ORDER-HANDBOOK-RECOVERY-dev37.md` — useful render-order history but contains old image-layout expectations;
- `CURRENT-IMPLEMENTATION-AUDIT-dev38.md`, `...dev39.md`, `...dev40.md` — snapshots of those versions, not current authority after dev.41;
- all `DEVxx-*QA.md` files — version-specific QA checklists only;
- BUILD-FIX documents — historical build recovery only.

## Explicitly retired design ideas

- Field as a 3x3 / nine-image active layout;
- direct independent recipe for each of the six current projectors;
- broad vanilla Glass/Glass-Pane chassis layers as final art language;
- installed raw Core material rendered as a full material block as final visual design;
- Core-owned hard Scale/Lift/Float caps;
- chassis nominal size as an absolute gameplay wall;
- separate Improved-Core item families such as Amplified/Focused/Stabilized/Resonant. Material identity now carries the distinct Improved-Core behavior;
- EFFIGY/COLOSSAL placeholder chassis as active source-code enum values before their designs exist.

## Maintenance rule

Whenever a new system supersedes an old contract:

1. update this authority map;
2. mark the old focused doc historical/superseded at its top if confusion is likely;
3. keep history in CHANGELOG instead of duplicating full old implementation sections into README/DEVELOPMENT;
4. update `CURRENT-IMPLEMENTATION-AUDIT` with implemented/planned status;
5. remove dead source placeholders when they are not needed for save/network compatibility.
