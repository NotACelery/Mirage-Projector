# Mirage Projector — Current Implementation Audit (dev.43)

> **Status:** source candidate. This file describes what is actually present in dev.43 source, not what is merely planned. dev.43 is not build-clean until Windows `build.bat` succeeds and the in-game QA checklist passes.

## 1. Platform / protocol

- Minecraft 1.21.1
- NeoForge 21.1.244
- Java 21
- Gradle 9.2.1
- Mod version `0.1.0-dev.43`
- Network protocol `18`
- Entity Scan data version remains unchanged from the current dev line.

No Mirage networking or projector-persistence schema changes were introduced by dev.43.

## 2. Projector foundations already implemented before dev.43

The six active chassis remain:

- Mirage Projector / Compact
- Mirage Display
- Wide Mirage Projector
- Tall Mirage Projector
- Mirage Field Projector
- Mirage Prism

Current major implemented systems retained from the baseline:

- horizontal placement orientation;
- per-chassis Power multiplier + Base-PU Core model;
- dynamic Scale/Lift/Float sliders;
- nominal chassis envelopes + quadratic Overdrive;
- tiny Ghost PU rebate;
- Image static formats + GIF animation;
- content-based image-format detection;
- Wide Single/4×1 and Tall Single/1×4;
- Field as one continuous Plane;
- Prism N/E/S/W faces with adaptive aspect behavior;
- Item snapshots;
- Banner snapshots;
- Entity scans / Player snapshots / Humanoid equipment / Horse channels;
- incompatible Entity-family snapshot cleanup;
- Piglin/Hoglin dimension-shaking normalization;
- deferred Entity projection rendering work from the current renderer line;
- Debug Handbook;
- dev.42 Crying Obsidian Shard, composite chassis optical material language and universal Core Chamber with the real installed ItemStack.

## 3. New blocks/items in dev.43

### 3.1 Crystal blocks

Four `CryingObsidianCrystalBlock` registrations exist:

1. `small_crying_obsidian_bud`
2. `medium_crying_obsidian_bud`
3. `large_crying_obsidian_bud`
4. `crying_obsidian_cluster`

They inherit Amethyst-Cluster directional/waterlogged placement behavior and add:

- fixed Mirage age metadata via `CryingObsidianCrystalStage`;
- `energized` boolean BlockState;
- age-specific vanilla fallback light emission;
- scheduled Beacon-optics rechecks;
- random-tick growth for non-mature stages.

All four have BlockItems and appear in Mirage's creative tab plus Natural Blocks.

### 3.2 Art/resources

Each stage has:

- normal 16×16 texture;
- energized 16×16 texture;
- cutout cross block model;
- directional blockstate variants;
- item model.

The current artwork is a first implementation candidate and remains subject to in-game visual polish without changing the growth contract.

## 4. Renewable growth implementation

Natural generation is intentionally narrow:

```text
Lava source
Crying Obsidian
      ↓
free/water block
```

Rules present in source:

- the Lava must be a **source**, not flowing Lava;
- generator must be vanilla Crying Obsidian;
- growth location is directly below;
- natural Small Bud is `FACING=DOWN`;
- water at the target can become a waterlogged Bud;
- other occupied targets do not grow;
- removing Lava/Crying Obsidian does not regress an existing crystal;
- only DOWN-facing crystals still attached to a valid generator advance naturally.

Current QA-provisional random-tick probabilities:

- Empty -> Small: 1/32 eligible source random tick;
- Small -> Medium: 1/24 eligible crystal random tick;
- Medium -> Large: 1/16;
- Large -> Mature: 1/12.

These deliberately make initial nucleation the bottleneck.

### 4.1 Vanilla Crying Obsidian random ticks

Because vanilla Crying Obsidian does not normally random-tick, dev.43 contains two narrow common Mixin hooks:

- its BlockState reports random-tick eligibility;
- the base block random-tick path invokes `CryingObsidianGrowthHooks` only when the state is vanilla Crying Obsidian.

This is deliberately scoped to the one vanilla block and must be regression-tested on a dedicated server before release.

## 5. Harvest implementation

Loot tables implement:

| Stage | Without Silk Touch | With Silk Touch |
|---|---:|---|
| Small | 1 shard | Small Bud |
| Medium | 2 shards | Medium Bud |
| Large | 3 shards | Large Bud |
| Mature | 4 shards | Mature Cluster |

Fortune is intentionally absent. The four crystal blocks are added to the pickaxe-mineable tag.

## 6. Structure loot implementation

NeoForge Global Loot Modifiers add uncommon shard opportunities without replacing vanilla tables:

| Loot source | Chance | Count |
|---|---:|---:|
| Ruined Portal | 40% | 1–3 |
| Abandoned Mineshaft | 20% | 1–2 |
| Village Armorer | 12% | 1–2 |
| Village Toolsmith | 12% | 1–2 |
| Village Weaponsmith | 12% | 1–2 |

These values are balance candidates, not immutable release numbers.

## 7. Beacon excitation implementation

`CryingObsidianCrystalOptics` scans the same X/Z column downward from a crystal:

- lower Mirage crystals attenuate the incoming fraction;
- an active Beacon BlockEntity establishes the source;
- no active Beacon -> not energized;
- a lower Mature Cluster transmits 0 and therefore prevents higher crystals from energizing from that column.

Each crystal rechecks every 20 ticks and updates `ENERGIZED` server-side.

Current vertical-transmission values:

- Small 0.75
- Medium 0.50
- Large 0.25
- Mature 0.00

Current vanilla powered block light:

- Small 14
- Medium 15
- Large 15
- Mature 15

The richer intended 14/18/23/28 curve requires an optional extended-light provider and is **not** currently active.

## 8. Beacon visual interception implementation

A client `BeaconRenderer` Mixin delegates active crystal columns to `CryingObsidianBeaconRenderer`.

Important invariants:

- Mirage does not make the crystal logically opaque to the Beacon;
- Beacon effects/levels remain vanilla;
- vanilla stained-glass `BeaconBeamSection` colors remain the source hue;
- Mirage splits visual beam segments at crystal centers (`Y + 0.5`);
- transmission is multiplied after each crystal;
- Mature visually terminates the vertical beam.

This path is new and requires direct Fabulous/Fancy/Fast graphics QA and shader/mod compatibility QA.

## 9. Residual-beam implementation

Each energized crystal can render at most one deterministic residual leak at a time.

Current constants:

- inner radius: `0.10` blocks;
- outer radius: `0.125` blocks;
- vanilla inner Beacon radius reference: `0.20` blocks;
- color: bright lavender inner + dark Crying-Obsidian-purple outer;
- direction: deterministic pseudo-random yaw + ±25° pitch;
- solid-block ray clipping;
- no gameplay damage/interactions.

Animation:

1. 20 ticks extension at full alpha;
2. 20 ticks full-length hold;
3. 20 ticks retract + fade;
4. stage-specific inactive cooldown inside a 180/160/140/120-tick cycle.

Maximum stage length:

- Small 1.25 blocks;
- Medium 2 blocks;
- Large 3 blocks;
- Mature 4 blocks.

Actual chosen length varies within a stage range and is clipped by collision.

## 10. Extended-light provider status

Not integrated in dev.43.

A compatible NeoForge 1.21.1 extended-light library should be evaluated before the Improved-Core/Beacon coupling wave. The base mod must remain functional without it. If no safe provider is adopted, the fallback design is to investigate broader effective light reach/decay without claiming block-light values above vanilla's supported range.

Do not patch/rewrite Minecraft's global light engine inside Mirage solely for this feature.

## 11. Still future after dev.43

### dev.44

- Obsidian Spike block/model/damage/slow behavior.

### dev.45

- projector upgrade crafting;
- canonical state-preserving transfer;
- Mirage -> Display -> Wide/Tall/Prism/Field recipes.

### dev.46

- five Improved Cores;
- real nested/tesseract-like models;
- Power amplification.

### dev.47

- Improved-Core Beacon relay;
- Diffusion/Radiance/Resonance/Focus/Inversion;
- resolved-beam -> powered-crystal coupling.

### 1.1.0+

- Mirage Scan Codex + searchable/filterable scan library;
- Paper-based card-copy station.

## 12. Known QA / technical risk list

Highest-priority dev.43 checks:

- Mixin signatures on actual NeoForge 21.1.244 build;
- dedicated-server classloading for common crystal growth hooks;
- natural nucleation actually fires from vanilla Crying Obsidian;
- age order feels faster after nucleation;
- Silk/no-Silk/Fortune behavior;
- GLM data paths load without datapack errors;
- energized state updates when Beacon starts/stops;
- Mature stops visual beam but not Beacon potion effects;
- stained-glass beam colors survive;
- stacked age transmission behaves correctly;
- residual beam is centered, half-width, collision-clipped and deterministic;
- no duplicate/double-offset residual beam geometry;
- renderer behaves under Fast/Fancy/Fabulous and common shader conditions;
- dev.42 Core Chamber/chassis visual regression;
- pre-existing Entity/Water/Ghost depth QA still remains part of release hardening.
