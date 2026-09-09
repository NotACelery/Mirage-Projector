# Current implementation audit — Mirage Projector 0.1.0-dev.41

## Status

`dev.41` is a **consolidation/documentation/code-hygiene source candidate** built from dev.40.

Confirmed by user QA before/following this audit:

- dev.40 executed in-game;
- Piglin `isShaking` / dimension-zombification jitter is solved;
- current physical Core visual can disappear from certain camera angles;
- broad Glass/Glass-Pane chassis surfaces read as translucent ghost layers and are not acceptable final art.

Network protocol remains **18**. dev.41 intentionally does not change packet/NBT semantics or core gameplay systems relative to dev.40.

---

# 1. dev.41 source changes actually present

- version advanced to `0.1.0-dev.41`;
- protocol token centralized as `MirageProjector.NETWORK_PROTOCOL`;
- unused EFFIGY/COLOSSAL `ProjectionChassisProfile` placeholders removed; only six registered gameplay chassis remain;
- `ImageSourceBank` terminology split into:
  - `ACTIVE_MULTI_SLOTS = 4`;
  - `PERSISTED_COMPAT_SLOTS = 9` for dev.33-dev.37 migration only;
- unused `sourceCapacity` removed;
- unused raw-English Core display name removed;
- temporary raw-Core-to-material-block visual renamed `legacyBlockVisualStack()`;
- old per-chassis Core visual sizes marked `legacyCore...`;
- unused planned Improved-amplification code constant removed; future target remains documentation-only until gameplay exists;
- documentation hierarchy, current roadmap, code audit and planned progression contracts consolidated.

No PU formula, Image/GIF pipeline, Entity persistence, SourceMode, asset transport, render-order stage or network payload is intentionally changed by dev.41.

---

# 2. Implemented authoritative systems

## Platform/network

- Minecraft 1.21.1;
- NeoForge 21.1.244;
- Java 21;
- Gradle 9.2.1;
- Parchment 2024.11.17;
- network protocol 18;
- current assets `<sha256>.asset`, with legacy `<sha256>.png` readability.

## Six chassis

1. Mirage Projector / Compact
2. Mirage Display
3. Wide Mirage Projector
4. Tall Mirage Projector
5. Mirage Field Projector
6. Mirage Prism

All use horizontal `FACING`. Plane orientation follows placement. Prism Image/Banner faces retain world-cardinal N/E/S/W semantics.

Current nominal efficiency targets:

| Chassis | W×H nominal | Lift | Float | Chassis PU multiplier |
|---|---:|---:|---:|---:|
| Mirage | 10×10 | 32 | 4 | ×1.00 |
| Display | 32×32 | 48 | 12 | ×1.50 |
| Wide | 80×32 | 64 | 12 | ×2.00 |
| Tall | 32×80 | 96 | 16 | ×2.00 |
| Field | 128×128 | 144 | 24 | ×4.00 |
| Prism | adaptive face baseline 48×48 | 96 | 12 | ×2.00 |

These are nominal efficiency ranges, not hard caps.

## Power

Current standard Base PU:

| Core | Base PU | Amplification |
|---|---:|---:|
| Glass | 32 | ×1.00 |
| Quartz | 48 | ×1.00 |
| Amethyst | 64 | ×1.00 |
| Diamond | 96 | ×1.00 |
| Netherite | 128 | ×1.00 |

Formula:

```text
Effective PU = floor(Base Core PU × Chassis multiplier × Core amplification)
```

Implemented:

- dynamic PU-aware Scale/Lift/Float maxima;
- quadratic Overdrive above nominal geometry/Lift/Float;
- centralized load breakdown;
- source/presentation surcharges;
- deliberately tiny Ghost rebate;
- Power capacity/load UI and detailed `?` breakdown.

## Image/GIF

Implemented static formats:

- PNG;
- JPEG/JPG;
- WebP;
- BMP.

Implemented animated format:

- GIF.

Format is sniffed from bytes, not filename. Renamed GIF remains GIF. Animated WebP/APNG are detected and rejected explicitly.

Wide/Tall:

- SINGLE = one continuous aspect-preserving source;
- MULTI = exactly four active square cells, Wide 4×1 / Tall 1×4.

Field:

- one continuous Plane;
- no active 3×3 grid.

Prism:

- four lateral N/E/S/W faces;
- one source per face;
- no 4×1/1×4 stacking;
- adaptive horizontal/vertical/near-square nominal face envelope;
- per-face geometry overdrive summed into PU.

## Item/Banner/Entity

- Item projections use virtual snapshots;
- Banner projections use virtual cloth/pattern snapshots;
- Entity projection clones are render-only client entities and are not spawned/ticked/AI-driven;
- Player appearance metadata is frozen for current vanilla skin/model-part behavior;
- Humanoid six-slot virtual equipment exists and can render bodyless;
- Horse saddle/body-armor snapshots exist;
- switching entity family purges virtual slot groups that disappear;
- Piglin/Hoglin temporary clones are normalized against dimension zombification; Piglin user QA is confirmed.

## Idle marker

All six chassis show the idle floating vanilla book when no renderable source exists, independent of Core presence.

---

# 3. Known current implementation problems

## Core physical renderer

Current code still renders a legacy artificial material-block visual for raw standard Core inputs.

Observed:

- disappears from certain camera angles;
- misleading visual scale (`Netherite Ingot -> Netherite Block` etc.).

Final planned replacement:

- universal ~4×4×4 Glass Core Chamber;
- actual installed ItemStack/model;
- uniform small scale, slow rotation and subtle bob;
- corrected culling/bounds.

Authority: `CORES-AND-UPGRADES-dev41.md`.

## Broad Glass chassis layers

Current block models still contain thin broad translucent Glass/Pane geometry that loses texture borders and appears as ghost sheets.

Final planned art uses:

- Obsidian structural base;
- Crying-Obsidian/Shards emitter language;
- real Glass mainly for Core Chamber;
- more visible whole Crying Obsidian in Field.

## Remaining render QA

Still explicitly regression-test before stable 0.1.x:

- Entity hologram depth vs physical projectors;
- Ghost Entity vs water front/behind;
- overlapping translucent Entity projections;
- eyes/glint/beams/special/modded RenderTypes;
- heavy GIF stress and multiplayer transfer;
- Prism mixed-aspect/multi-face PU/rendering.

---

# 4. Planned but not implemented in dev.41

## Crying Obsidian ecosystem

None of the following exist in source yet:

- Crying Obsidian Shard item;
- Stonecutter/re-form recipes;
- shard structure loot;
- Small/Medium/Large/Mature Crying Obsidian buds/clusters;
- renewable Lava-above-Crying-Obsidian growth;
- Silk/no-Silk drops;
- Beacon absorption/partial transmission;
- powered crystal light;
- residual purple escape beams;
- Obsidian Spike.

Authority: `CRYING-OBSIDIAN-ECOSYSTEM-dev41.md`.

Important: the previous planned normal-Obsidian -> Crying-Obsidian dripstone/cauldron conversion is **retired** and should not be implemented.

## Projector crafting/upgrades

Current resources do not yet implement the real chassis progression.

Planned graph:

```text
Mirage -> Display -> Wide/Tall/Prism/Field
```

Upgrade recipes must preserve all persistent projector state.

Authority: `CORES-AND-UPGRADES-dev41.md`.

## Improved Cores

No Improved Core item/block/model/recipe/Beacon integration exists yet.

Planned:

- five material-based Improved Cores;
- same Base PU + amplification target ~×1.50;
- three nested shells with genuinely rotated middle shell;
- decorative placement;
- material-specific Beacon relay effects;
- max four effective relays, width cap ~2×.

---

# 5. Explicitly retired concepts

Removed/retired from active design:

- EFFIGY/COLOSSAL active placeholder enum values;
- Field active nine-image/3×3 layout;
- nine slots as active Image capacity;
- raw material block Core visual as final contract;
- broad Glass sheets as final chassis art;
- hard Core Scale/Lift/Float limits;
- six unrelated direct chassis recipes;
- four named Improved-Core variant families per material;
- normal Obsidian -> Crying Obsidian via lava/dripstone/cauldron;
- two intermediate Crying-infusion Obsidian blocks/Jade progress for that system;
- final item name `Cut Obsidian Shard`; use `Crying Obsidian Shard` in new work.

---

# 6. Important open design decisions

These are genuinely unresolved and must not be guessed silently:

1. Does a newly crafted base Mirage Projector start with a free removable Glass Core or an empty socket?
2. Final Wide/Tall shaped recipe pair (preferred equal-cost candidates exist).
3. Final Field cost after renewable Crying Obsidian is playable.
4. Final Improved Core recipe/material counts.
5. Exact Improved amplification after ×1.50 testing.
6. Optional extended-light provider/API for powered crystals above vanilla level 15.
7. Any future Glowstone role — currently none.

---

# 7. Next implementation order

The authoritative overall sequence is `CURRENT-STATE-ROADMAP-dev41.md`.

Short form:

1. dev.42: Crying Obsidian Shard + chassis visual/Core Chamber replacement;
2. dev.43: renewable Crying crystal growth + Beacon refraction/light/residual beams + shard loot;
3. dev.44: Obsidian Spike;
4. dev.45: canonical state-preserving upgrade crafting + recipe graph;
5. dev.46: five Improved Cores + projector amplification;
6. dev.47: Improved-Core Beacon relay;
7. deep render/feature-freeze QA;
8. controlled refactor of oversized renderer/state/UI classes;
9. 0.1.0 release prep;
10. 1.1.0+: Scan Codex/copy station.
