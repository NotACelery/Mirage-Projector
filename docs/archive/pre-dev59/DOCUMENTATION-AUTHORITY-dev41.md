# Documentation authority map — dev.41

This file prevents historical dev notes, abandoned experiments and superseded roadmap ideas from silently becoming current design again.

---

# 1. Authority order

When two sources disagree, use this order:

1. **current source code** for behavior that is already implemented;
2. `docs/CURRENT-IMPLEMENTATION-AUDIT-dev41.md` for implemented-vs-planned status;
3. focused authoritative contracts listed below;
4. `docs/CURRENT-STATE-ROADMAP-dev41.md` for development order / next-chat continuation;
5. `docs/NEXT-CHAT-HANDOFF-dev41.md` as a compact recovery summary only;
6. README / DEVELOPMENT current summaries;
7. CHANGELOG and older `devXX` documents only for historical traceability.

A planned design document may intentionally describe behavior that is not in source yet. Such a document must state `planned/not implemented` clearly.

---

# 2. Authoritative current implementation contracts

- `POWER-SYSTEM-REWORK-dev38.md` — current PU architecture, nominal ranges, Overdrive, Ghost rebate and dynamic limits.
- `CHASSIS-IMAGE-LAYOUT-POWER-UX-dev38.md` — current six-chassis geometry/layout foundations, except where later Image/Prism contracts override it.
- `ENTITY-WORKSPACE-LIFETIME-dev38.md` — Entity workspace snapshot lifetime/cleanup.
- `GIF-ANIMATED-IMAGE-dev39.md` — GIF pipeline, timing, caching and safety.
- `IMAGE-FORMAT-IMPORT-CONTRACT-dev39.md` — content-sniffed static/GIF import and rejection of unsupported animated formats.
- `WIDE-TALL-SINGLE-ASPECT-dev39.md` — Wide/Tall SINGLE sizing semantics.
- `PRISM-ADAPTIVE-ASPECT-dev39.md` — Prism four-face adaptive aspect/PU behavior.
- `ENTITY-DIMENSION-NORMALIZATION-dev40.md` — projection-only Piglin/Hoglin dimension normalization.
- `CURRENT-IMPLEMENTATION-AUDIT-dev41.md` — exact implemented/planned inventory at the end of dev.41.

---

# 3. Authoritative planned design contracts

## 3.1 Crying Obsidian ecosystem

`CRYING-OBSIDIAN-ECOSYSTEM-dev41.md`

Authority for:

- `Crying Obsidian Shard` naming and acquisition;
- Stonecutter conversion;
- 8-shard + Fire Charge/Magma Cream re-form recipe;
- renewable Small/Medium/Large/Mature Crying Obsidian crystal growth;
- growth speed philosophy;
- Silk Touch/no-Fortune harvesting;
- shard structure loot;
- Beacon-powered crystal transmission/absorption;
- powered light targets/fallback;
- residual purple escape-beam behavior;
- Obsidian Spike relationship;
- optional future unrefined crystal Core.

This document **supersedes** the earlier plan for normal Obsidian -> Crying Obsidian conversion using Pointed Dripstone/Cauldron/intermediate blocks.

## 3.2 Cores and projector upgrades

`CORES-AND-UPGRADES-dev41.md`

Authority for:

- chassis material language;
- universal Core Chamber;
- removal of broad Glass ghost layers;
- base Mirage recipe;
- mandatory Mirage -> Display -> specialized chassis progression;
- state-preserving custom upgrade recipes;
- five Improved Cores;
- Improved-Core nested-shell model;
- projector amplification target architecture;
- Improved-Core Beacon relay / material-specific effects / stacking;
- future Scan Codex note.

## 3.3 Overall continuation order

`CURRENT-STATE-ROADMAP-dev41.md`

Authority for:

- what is implemented now;
- known unresolved bugs/QA;
- what is planned but absent;
- recommended dev.42+ implementation order;
- next-chat recovery checklist.

It summarizes focused contracts but does not override their detailed mechanics.

---

# 4. Historical documents that must not govern current behavior

Useful for archaeology only, in whole or in part:

- `GUI-ARCHITECTURE-dev19.md` — contains obsolete Core PU/hard-cap values;
- `POWER-POSE-UX-dev29.md` — predates dev.38 Power rework;
- `MULTI-SOURCE-IMAGE-LAYOUTS-dev33.md` — accidental Field 3×3 interpretation;
- `ENTITY-NAMEPLATE-DEPTH-ORDER-dev36.md` — failed dev.36 render-order approach;
- `RENDER-ORDER-HANDBOOK-RECOVERY-dev37.md` — render-order history with old Image expectations;
- `CURRENT-IMPLEMENTATION-AUDIT-dev38.md`, `...dev39.md`, `...dev40.md` — version snapshots, not current authority after dev.41;
- all version-specific `DEVxx-*QA.md` files — QA history/checklists only;
- BUILD-FIX documents — historical recovery only.

Older chat summaries may also contain superseded planning. The files above/current source win.

---

# 5. Explicitly retired design ideas

Do not restore these without a new explicit design decision:

- Field as an active 3×3 / nine-image layout;
- unrelated direct recipes for all six projectors;
- broad vanilla Glass/Glass-Pane chassis sheets as final art;
- installed raw Core material displayed as a full material block;
- Core-owned hard Scale/Lift/Float caps;
- nominal chassis dimensions as absolute gameplay walls;
- separate Amplified/Focused/Stabilized/Resonant Improved-Core families per material;
- EFFIGY/COLOSSAL placeholder chassis as active enum values without real blocks/design;
- **normal Obsidian -> Crying Obsidian via lava + Pointed Dripstone + Cauldron**;
- two intermediate Crying-infusion Obsidian blocks / Jade progress for that retired conversion;
- `Cut Obsidian Shard` as final item name — use `Crying Obsidian Shard`;
- Glowstone as a defined Core/recipe replacement — currently only an uncommitted brainstorm.

---

# 6. Maintenance rule

Whenever a new system supersedes an older contract:

1. update this authority map;
2. update `CURRENT-IMPLEMENTATION-AUDIT`;
3. update `CURRENT-STATE-ROADMAP` if development order changes;
4. mark the older focused document historical/superseded if confusion is likely;
5. keep chronology in CHANGELOG instead of duplicating obsolete implementation text in README/DEVELOPMENT;
6. remove dead source placeholders when they are not required for save/network compatibility;
7. never silently convert a brainstorm into a frozen implementation requirement.
