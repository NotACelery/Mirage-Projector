# Mirage Projector

Mirage Projector is a NeoForge 1.21.1 mod for configurable holographic projections: images/GIFs, items, banners and frozen entity snapshots rendered from six projector chassis.

> **Current line:** `0.1.0-dev.41` — consolidation/documentation baseline. dev.41 is intended to preserve dev.40 gameplay while defining the next material/progression roadmap. New Crying-Obsidian crystal/core/crafting systems described below are planned, not implemented yet.

For authoritative status/order, start with:

- `docs/DOCUMENTATION-AUTHORITY-dev41.md`
- `docs/CURRENT-STATE-ROADMAP-dev41.md`
- `docs/CURRENT-IMPLEMENTATION-AUDIT-dev41.md`
- `docs/NEXT-CHAT-HANDOFF-dev41.md` — compact migration entry point

---

# Current implemented systems

## Platform

- Minecraft 1.21.1
- NeoForge 21.1.244
- Java 21
- Gradle 9.2.1
- Parchment 2024.11.17
- Network protocol 18

## Current chassis

| Chassis | Nominal geometry | Lift | Float | PU multiplier |
|---|---:|---:|---:|---:|
| Mirage Projector | 10×10 | 32 | 4 | ×1.00 |
| Mirage Display | 32×32 | 48 | 12 | ×1.50 |
| Wide Mirage Projector | 80×32 | 64 | 12 | ×2.00 |
| Tall Mirage Projector | 32×80 | 96 | 16 | ×2.00 |
| Mirage Field Projector | 128×128 | 144 | 24 | ×4.00 |
| Mirage Prism | adaptive 48×48 baseline | 96 | 12 | ×2.00 |

Nominal values are efficiency ranges, not hard caps. Sufficient PU can pay Overdrive.

All six current blocks use horizontal placement facing and render the idle floating book when they have no renderable source.

## Power

Standard Base PU:

- Glass 32
- Quartz 48
- Amethyst 64
- Diamond 96
- Netherite 128

```text
Effective PU = floor(Base Core PU × Chassis multiplier × Core amplification)
```

Standard amplification = ×1.00.

Implemented Power features:

- dynamic Scale/Lift/Float maxima generated from actual PU budget;
- quadratic Overdrive above chassis nominal ranges;
- explicit load breakdown;
- tiny Ghost efficiency rebate;
- capacity/load UI.

## Image / GIF

Static formats:

- PNG
- JPG/JPEG
- WebP
- BMP

Animated:

- GIF

The importer detects format from file bytes, not filename extension. Renamed GIFs remain animated; unsupported Animated WebP/APNG are identified and rejected instead of silently flattening.

Current layouts:

- Mirage/Display: one continuous Plane;
- Wide: SINGLE continuous image/GIF or four 4×1 sources;
- Tall: SINGLE continuous image/GIF or four 1×4 sources;
- Field: one continuous large Plane — **never the old 3×3 grid**;
- Prism: four independent N/E/S/W faces, each static/GIF, no stacking mode.

## Item / Banner / Entity

Implemented:

- virtual Item snapshots;
- virtual Banner cloth/pattern snapshots;
- Entity Scan Cards and frozen render-only entities;
- Player appearance snapshot support;
- Humanoid armor/hands snapshots and bodyless rig;
- Horse equipment handling;
- cleanup of virtual equipment when entity family changes;
- Piglin/Hoglin dimension-conversion shaking normalization.

Piglin shaking was confirmed solved in-game in dev.40.

---

# Current known visual/QA problems

## Core renderer

The current installed Core visual is legacy and can disappear from certain camera angles. Raw items are temporarily represented as artificial material blocks.

Planned replacement: small universal Glass Core Chamber containing the **real installed ItemStack** floating/rotating at uniform scale.

## Chassis Glass ghost layers

Current broad thin Glass/Pane model surfaces frequently lose texture borders and appear as translucent ghost sheets.

Planned replacement: Obsidian structure + Crying-Obsidian/Shards emitter material; real Glass mainly reserved for the Core Chamber.

## Render QA still owed

Before stable release, deeply test:

- Entity/projector depth;
- Ghost/water ordering;
- overlapping translucent entities;
- eyes/glint/special RenderTypes;
- GIF stress/multiplayer transfer;
- Prism mixed-aspect faces.

---

# Planned Crying Obsidian ecosystem

Authority: `docs/CRYING-OBSIDIAN-ECOSYSTEM-dev41.md`.

## Crying Obsidian Shard

Final planned shared item name: **Crying Obsidian Shard**.

```text
Stonecutter:
1 Crying Obsidian -> 4 Crying Obsidian Shards
```

```text
8 shards + Fire Charge OR Magma Cream -> 1 Crying Obsidian
```

Small shard stacks are also planned for Ruined Portals, Mineshafts, smith-related chests and reviewed Obsidian/Crying-Obsidian loot pools.

## Renewable crystal growth

The old proposed normal-Obsidian dripstone/cauldron conversion is **retired**.

New loop:

```text
Lava source
Crying Obsidian
      ↓ grows downward
Small Bud -> Medium Bud -> Large Bud -> Mature Crying Obsidian Cluster
```

- first Small-Bud nucleation is slowest;
- later stages grow progressively faster;
- breaking without Silk Touch drops exactly 1/2/3/4 shards by age;
- Silk Touch drops the actual stage;
- Fortune does not increase shard counts initially;
- naive automation can harvest Small for 1 shard, while patient harvest reaches 4.

## Beacon-powered crystal

As age increases:

- crystal absorbs more of the vertical beam;
- powered light increases;
- residual purple refraction becomes stronger.

Target visual transmission Small/Medium/Large/Mature:

```text
75% / 50% / 25% / 0% beam continues upward
```

Mature Cluster visually stops the beam while keeping Beacon gameplay active.

Intended powered light with optional extended-light support:

```text
14 / 18 / 23 / 28
```

Vanilla fallback necessarily caps normal block light at 15.

Mature Cluster can emit one narrow Crying-Obsidian-purple residual beam at a time:

- roughly half vanilla Beacon-inner-beam width;
- ~3–4 block maximum;
- extends outward strongly;
- holds at full length ~1 second;
- retracts while fading to transparent;
- changes pseudo-random direction after a cooldown.

## Obsidian Spike

Planned recipe budget:

- 3 Crying Obsidian Shards;
- 2 String;
- 1 Stick.

Planned block:

- approximately half-block high;
- nine sharp tips;
- thinner/taller central tip;
- bush-like movement hindrance;
- 2.0 damage points (1 heart) per successful hurt event.

---

# Planned projector/Core progression

Authority: `docs/CORES-AND-UPGRADES-dev41.md`.

## Chassis progression

```text
Mirage Projector
      ↓
Mirage Display
   ├─ Wide
   ├─ Tall
   ├─ Prism
   └─ Field
```

Only the base Mirage is crafted from raw materials. Advanced chassis consume the previous projector and must preserve all imported/state data.

Base Mirage recipe:

```text
S S S
S G S
O O O
```

`S = Crying Obsidian Shard`, `G = Glass Block`, `O = Obsidian`.

Mirage -> Display:

```text
Q S A
S M S
A S Q
```

Display -> Prism:

```text
S G S
G D G
S G S
```

Wide/Tall must have equal total costs; Field must be substantially more expensive and use whole Crying Obsidian.

## State preservation

Projector upgrades must preserve the complete canonical persistent state, including Core, assets/GIFs, cards, Entity/Humanoid/Horse snapshots, Item/Banner snapshots and all Projection Settings.

Do not implement upgrades as ordinary shaped recipes that lose components/NBT.

---

# Planned Improved Cores

Exactly five:

- Improved Glass
- Improved Quartz
- Improved Amethyst
- Improved Diamond
- Improved Netherite

Power design:

- same Base PU as standard material;
- target amplification ~×1.50;
- still uses normal chassis multiplier/PU/Overdrive.

Visual design:

- placeable decorative block;
- three nested dark-purple transparent shells;
- middle shell **genuinely rotated geometrically**;
- corresponding material floating at center.

Beacon identities:

| Core | Effect |
|---|---|
| Glass | Diffusion — strongest widening |
| Quartz | Radiance — brighter beam |
| Amethyst | Resonance — faster rotation |
| Diamond | Focus — more intense/defined inner beam |
| Netherite | Inversion — reversed rotation |

Universal relay:

- incoming beam reaches Core mid-plane `Y+0.5`;
- outgoing beam begins there and becomes wider;
- stained-glass hue is preserved;
- max four effective Improved Cores;
- target final width cap ~2× vanilla.

---

# Development order after dev.41

Short version:

1. **dev.42:** Crying Obsidian Shard + new chassis visual language + Core Chamber/core-culling fix.
2. **dev.43:** renewable Crying crystal growth + shard loot + Beacon light/refraction/residual beam.
3. **dev.44:** Obsidian Spike.
4. **dev.45:** canonical state-preserving projector upgrade recipes/progression.
5. **dev.46:** five Improved Cores + projector amplification.
6. **dev.47:** Improved-Core Beacon relay/effects.
7. feature/render freeze QA.
8. controlled code refactor.
9. 0.1.0 release preparation.
10. **1.1.0+:** Mirage Scan Codex + scan-copy station.

Full roadmap: `docs/CURRENT-STATE-ROADMAP-dev41.md`.

---

# Explicit non-requirements / brainstorms

- Glowstone currently has **no assigned role**; do not replace Glass Core or recipes without a new design decision.
- Future unrefined Crying Crystal/Cluster Core is only a backlog concept; it may add intentional purple refractive distortion to projections.
- Animated WebP/APNG playback remains future work.
- true RGB purple world lighting is not a vanilla feature and would require compatibility with a colored-light/shader system.

---

# Build

Requirements:

- Java 21;
- Windows: run `build.bat` from project root.

Source snapshots must not include `.gradle`, `.gradle-dist`, `build`, `run` or IDE/download caches.
