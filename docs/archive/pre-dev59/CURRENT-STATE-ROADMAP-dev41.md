# Mirage Projector — current state, pending work and implementation roadmap (dev.41)

> **Purpose:** this is the primary next-chat handoff/roadmap for the end of dev.41. It answers three questions: **what exists now, what is still missing/broken, and in what order should development continue?**
>
> For exact implemented behavior, source code + `CURRENT-IMPLEMENTATION-AUDIT-dev41.md` remain authoritative. For focused planned mechanics, follow the linked contracts.

---

# 1. Baseline status

Current source line:

- Minecraft 1.21.1
- NeoForge 21.1.244
- Java 21
- Gradle 9.2.1
- Parchment 2024.11.17
- Mod version **0.1.0-dev.41**
- Network protocol **18**

Known QA boundary:

- dev.40 was executed in-game;
- Piglin dimension/zombification shaking fix is confirmed solved by user QA;
- dev.41 is primarily consolidation/documentation/code hygiene and is intended to be behavior-neutral relative to dev.40;
- dev.41 still requires Windows build/regression confirmation before calling it build-clean.

---

# 2. Implemented today

## 2.1 Six chassis

Implemented gameplay blocks:

1. Mirage Projector / Compact
2. Mirage Display
3. Wide Mirage Projector
4. Tall Mirage Projector
5. Mirage Field Projector
6. Mirage Prism

All six:

- have horizontal placement facing like a Furnace;
- expose the common Projection Settings system;
- show the idle floating vanilla book when no renderable source exists.

Current nominal ranges are efficiency targets, not hard caps:

| Chassis | Nominal W×H | Lift | Float | PU multiplier |
|---|---:|---:|---:|---:|
| Mirage | 10×10 | 32 | 4 | ×1.00 |
| Display | 32×32 | 48 | 12 | ×1.50 |
| Wide | 80×32 | 64 | 12 | ×2.00 |
| Tall | 32×80 | 96 | 16 | ×2.00 |
| Field | 128×128 | 144 | 24 | ×4.00 |
| Prism | adaptive face baseline 48×48 | 96 | 12 | ×2.00 |

## 2.2 Power system

Implemented dev.38 architecture:

```text
Effective PU = floor(Base Core PU × Chassis multiplier × Core amplification)
```

Standard Core Base PU:

- Glass 32
- Quartz 48
- Amethyst 64
- Diamond 96
- Netherite 128

Current standard amplification = ×1.00.

Implemented:

- dynamic PU-aware Scale/Lift/Float slider maxima;
- nominal-range Overdrive with quadratic cost above nominal;
- geometry/Lift/Float/source-complexity/presentation PU breakdown;
- tiny non-abusable Ghost rebate;
- Power capacity/load UI and detailed breakdown hotspot.

## 2.3 Image/GIF

Implemented formats:

- PNG static
- JPEG/JPG static
- WebP static
- BMP static
- GIF animated

Implemented safety:

- content sniffing rather than trusting filename extension;
- renamed GIF remains GIF;
- Animated WebP/APNG are detected and explicitly rejected rather than silently flattened;
- bounded GIF dimensions/frame count/frame-pixels/timing;
- content-addressed `.asset` transport with legacy `.png` compatibility.

## 2.4 Chassis Image behavior

Mirage / Display:

- one continuous Plane.

Wide:

- SINGLE = one continuous image/GIF with aspect preserved;
- MULTI = exactly four independent square cells in 4×1.

Tall:

- SINGLE = one continuous image/GIF with aspect preserved;
- MULTI = exactly four independent square cells in 1×4.

Field:

- one continuous large Plane;
- **no 3×3 grid**. The accidental dev.33 design is retired.

Prism:

- four N/E/S/W lateral faces;
- one source per face;
- static/GIF support;
- no Wide/Tall stacking;
- adaptive nominal Wide-like / Tall-like / near-square face behavior.

## 2.5 Item/Banner/Entity

Implemented:

- virtual Item snapshots;
- virtual Banner snapshots;
- Entity Scan Cards/snapshots;
- Player skin/model-part/dominant-arm snapshot support;
- Humanoid six-slot equipment snapshots;
- bodyless humanoid equipment rig after card removal;
- Horse saddle/body-armor handling;
- incompatible virtual equipment purged when entity family changes and corresponding GUI slots disappear;
- projection entities are client render-only and never spawned/ticked/AI-driven;
- Piglin/Hoglin dimension-conversion shaking normalized on temporary projection clones.

## 2.6 Documentation/maintenance

Implemented in dev.41:

- documentation authority hierarchy;
- active-vs-compat ImageSourceBank terminology;
- removal of EFFIGY/COLOSSAL placeholder enum values;
- removal/renaming of several dead/legacy source concepts;
- explicit current implementation and code-quality audits.

---

# 3. Known problems / incomplete stable systems

## 3.1 Current physical Core renderer — must be replaced

Observed:

- installed Core can disappear from certain camera angles;
- raw Core item currently becomes an artificial material-block visual (`Netherite Ingot -> Netherite Block`, etc.);
- non-uniform/block-like presentation is visually misleading.

Planned replacement:

- universal ~4×4×4 Glass Core Chamber;
- real installed ItemStack/model floating/rotating inside;
- corrected bounds/culling.

Authority: `CORES-AND-UPGRADES-dev41.md`.

## 3.2 Broad chassis Glass layers — must be replaced

Observed:

- thin/broad Glass/Pane areas lose vanilla texture borders;
- appear as translucent ghost layers depending on camera angle;
- do not communicate material thickness.

Planned replacement:

- Obsidian structural language;
- Crying-Obsidian/Shards emitter/optical language;
- real Glass mostly confined to Core Chamber;
- Field gains stronger whole-Crying-Obsidian identity.

## 3.3 Crafting progression absent

Current resources do not yet provide the real six-chassis progression.

Planned:

```text
Mirage -> Display -> Wide/Tall/Prism/Field
```

Upgrade recipes must preserve all projector state.

## 3.4 Improved Cores absent

Amplification exists architecturally but no Improved Core item/block/model/recipe/Beacon behavior exists yet.

## 3.5 Crying Obsidian ecosystem absent

Not implemented yet:

- Crying Obsidian Shard;
- shard loot;
- renewable Crying-Obsidian Bud/Cluster growth;
- Beacon-powered crystal light/refraction;
- residual escape beams;
- Obsidian Spike.

Authority: `CRYING-OBSIDIAN-ECOSYSTEM-dev41.md`.

## 3.6 Render QA still owed

Before stable release, regression-test:

- Entity hologram depth vs multiple physical projectors;
- Ghost Entity vs water in front/behind;
- overlapping translucent Entity projections;
- eyes/glint/beams/special/modded RenderTypes;
- heavy GIF workloads;
- multiplayer GIF/image asset transport;
- Prism mixed-aspect/multi-face rendering and PU.

---

# 4. Explicitly retired/deprecated current-design ideas

Do not revive these from older docs/chat summaries:

- Field active 3×3 / nine-image grid;
- independent direct recipe for every projector;
- broad Glass/Glass-Pane sheets as final chassis art;
- raw Core material displayed as a full material block;
- Core-owned hard Scale/Lift/Float caps;
- chassis nominal range as an absolute hard wall;
- EFFIGY/COLOSSAL placeholder chassis in active enums;
- four named Improved-Core variant families per material;
- normal Obsidian -> Crying Obsidian via Pointed Dripstone + Cauldron and two intermediate infusion blocks;
- Jade progress UI for that retired Obsidian conversion;
- `Cut Obsidian Shard` as final item name. Use `Crying Obsidian Shard`.

---

# 5. New Crying Obsidian progression

Frozen high-level loop:

```text
vanilla/exploration Crying Obsidian
        |
        +-> Stonecutter -> 4 Crying Obsidian Shards
        |
        +-> Lava source above source block
             -> downward Small Bud
             -> Medium Bud
             -> Large Bud
             -> Mature Crying Obsidian Cluster
             -> break without Silk: 1/2/3/4 shards

8 shards + Fire Charge OR Magma Cream
        -> 1 Crying Obsidian
```

This means:

- player must acquire at least one authentic Crying Obsidian source first;
- after that, it can be multiplied slowly through crystal growth;
- the source Crying Obsidian itself remains intact;
- naive automation can harvest early but gets poor 1-shard efficiency;
- patient/manual harvesting receives 4 shards from mature growth.

Beacon interaction:

- Small/Medium/Large progressively absorb more vertical beam;
- Mature absorbs 100% visually and stops the beam above it while Beacon gameplay remains active;
- powered light target with extended-light integration = 14/18/23/28;
- vanilla fallback caps at 15;
- mature crystal occasionally emits a thin purple residual escape beam 3–4 blocks long, approximately half Beacon inner-beam width;
- beam grows outward, holds at maximum ~1 s, then retracts while becoming transparent.

Full authority: `CRYING-OBSIDIAN-ECOSYSTEM-dev41.md`.

---

# 6. Improved Core progression

Five planned Improved Cores:

- Improved Glass
- Improved Quartz
- Improved Amethyst
- Improved Diamond
- Improved Netherite

Projector behavior:

- same Base PU as standard counterpart;
- target amplification ~×1.50;
- existing chassis multiplier/Power/Overdrive remain authoritative.

Block visual:

- three nested dark-purple transparent shells;
- middle shell genuinely rotated geometrically;
- corresponding material at center.

Beacon identities:

| Core | Secondary Beacon role |
|---|---|
| Glass | Diffusion — strongest widening |
| Quartz | Radiance — stronger luminance |
| Amethyst | Resonance — faster rotation |
| Diamond | Focus — more defined/intense inner beam |
| Netherite | Inversion — reversed rotation |

Common Beacon rule:

- relay starts at Y+0.5;
- outgoing beam wider;
- stained-glass hue preserved;
- max four effective Improved Cores;
- final width target cap ~2× vanilla.

Authority: `CORES-AND-UPGRADES-dev41.md`.

---

# 7. Recommended development order after dev.41

The order below minimizes rework and keeps each wave independently testable.

## Wave 1 — dev.42: material + visual foundation

Implement:

1. Crying Obsidian Shard item/sprite;
2. Stonecutter 1 Crying Obsidian -> 4 shards;
3. 8 shards + Fire Charge / Magma Cream -> Crying Obsidian;
4. new Crying-Obsidian emitter texture/material language;
5. remove broad Glass ghost layers from all six chassis;
6. universal Core Chamber;
7. render actual Core ItemStack floating/rotating;
8. fix Core camera-angle disappearance through proper bounds/culling.

Do **not** implement Improved Cores yet.

## Wave 2 — dev.43: renewable Crying crystal ecosystem

Implement:

1. Small/Medium/Large/Mature Crying Obsidian crystal blocks;
2. Lava-above-Crying-Obsidian downward growth;
3. increasing growth rates by age;
4. exact 1/2/3/4 no-Silk shard drops;
5. Silk Touch stage drops; no Fortune bonus;
6. shard loot injections;
7. energized crystal models/textures;
8. Beacon beam attenuation/termination by age;
9. powered light fallback + compatibility abstraction for >15 provider;
10. residual purple escape-beam animation/collision.

This wave replaces the retired Obsidian->Crying dripstone conversion completely.

## Wave 3 — dev.44: Obsidian Spike

Implement:

- 3 shards + 2 String + 1 Stick;
- half-block-ish nine-tip model;
- bush-like slowdown;
- 2.0 damage per successful hurt event;
- player/mob/collision QA.

## Wave 4 — dev.45: state-preserving projector crafting progression

First implement canonical projector-state transfer.

Then:

- base Mirage recipe;
- Mirage -> Display;
- Display -> Prism;
- equal-cost Wide/Tall upgrades;
- expensive Field upgrade with whole Crying Obsidian;
- recipe-book/JEI/EMI behavior if applicable;
- upgrade/save/reload/multiplayer QA.

Do not write plain shaped recipes that erase NBT/components.

## Wave 5 — dev.46: Improved Cores

Implement:

- five block/items;
- nested shells with genuinely rotated middle shell;
- recipes after progression-cost balance;
- projector amplification;
- chamber miniaturized visuals;
- decorative placement.

## Wave 6 — dev.47: Improved-Core Beacon relay

Implement:

- Y+0.5 beam relay;
- width amplification;
- five material identities;
- stacking max four;
- global width cap;
- mixed-material behavior;
- stained-glass color compatibility.

## Wave 7 — render/feature freeze QA

No major content additions.

Deep-test:

- Image/GIF;
- Item;
- Banner;
- Entity/Humanoid/Horse;
- Ghost/water/depth;
- Prism;
- Core Chamber;
- crystals/Beacon;
- projectors upgraded with populated state;
- multiplayer asset/state synchronization.

## Wave 8 — technical refactor after behavior freeze

Large current hotspots should be split only after rendering/gameplay behavior is stable, one recoverable refactor at a time.

Candidates include:

- `MirageProjectorRenderer`;
- `MirageProjectorBlockEntity`;
- `ProjectionPower`;
- `ImageProjectorScreen`;
- `MirageProjectorScreen`;
- `EntityProjectorScreen`.

Do not combine a massive class decomposition with new core/block/render mechanics.

## Wave 9 — 0.1.0 release preparation

After feature/render freeze and refactor QA:

- recipes/balance finalization;
- handbook/docs final pass;
- migration compatibility review;
- build matrix;
- release packaging.

## 1.1.0+ — Mirage Scan Codex

Future UX rework:

- scan entity once into persistent searchable/filterable Codex;
- dedicated workstation copies stored scan to Paper/physical Entity Scan Card;
- current projector card-slot architecture stays compatible.

Not part of current 0.1.x closure.

---

# 8. Optional/backlog ideas that are not current requirements

## 8.1 Unrefined Crying Crystal Core

Possible future Core using mature Cluster directly and intentionally adding subtle purple refractive distortion/chromatic ghosting to projections.

Not part of first Improved-Core wave; stats/recipe TBD.

## 8.2 Glowstone

Brainstorm only. No current role/recipe/Core replacement is defined.

## 8.3 Animated WebP / APNG playback

Currently detected/rejected safely. Playback remains future work.

## 8.4 Colored block lighting

Purple visual glow is achievable; true RGB world lighting requires an external colored-light/shader integration and is not base scope.

---

# 9. Next-chat recovery checklist

A new development chat should begin by reading in this order:

1. `docs/DOCUMENTATION-AUTHORITY-dev41.md`
2. `docs/CURRENT-STATE-ROADMAP-dev41.md`
3. `docs/CURRENT-IMPLEMENTATION-AUDIT-dev41.md`
4. `docs/CRYING-OBSIDIAN-ECOSYSTEM-dev41.md`
5. `docs/CORES-AND-UPGRADES-dev41.md`
6. focused current contracts for Power/Image/GIF/Entity as needed.

Important continuation facts:

- version remains dev.41 until a real implementation wave begins;
- Piglin shaking is confirmed fixed;
- current Core-angle disappearance and Glass ghost layers are still unresolved and intentionally targeted by dev.42;
- normal Obsidian->Crying conversion is retired;
- Shards are now `Crying Obsidian Shards` and renewable through crystal growth;
- do not start Improved Cores before shard/crystal/chassis/crafting foundations exist;
- always create a recoverable source snapshot for every wave even if QA is pending.
