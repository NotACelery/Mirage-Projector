# Mirage Projector

Mirage Projector is a NeoForge 1.21.1 mod for configurable holographic projections: images/GIFs, items, banners and frozen entity snapshots rendered from six projector chassis.

> **Current line:** `0.1.0-dev.54` — Core Booster redesign source candidate. Five user-facing Improved Core variants are consolidated into one stateful Core Booster with manually insertable Glass / Quartz / Amethyst / Diamond / Netherite material, persistent stateful drops and optional Jade readout.

For authoritative status/order, start with:

- `docs/DOCUMENTATION-AUTHORITY-dev44.md`
- `docs/CURRENT-STATE-ROADMAP-dev44.md`
- `docs/CURRENT-IMPLEMENTATION-AUDIT-dev44.md`
- `docs/NEXT-CHAT-HANDOFF-dev44.md` — compact migration entry point

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

# Current physical rework / QA status

## Core Chamber

Implemented in dev.42 source candidate: every chassis has a universal 4×4×4 Glass Core Chamber and the BER renders the **real installed ItemStack** inside it with slow rotation, subtle bob and fullbright lighting. The old material-block substitution has been removed.

## Chassis optical material

Implemented in dev.42 source candidate: broad Glass/Pane sheets are gone. Chassis now separate solid structural geometry from translucent Crying-Obsidian emitter geometry, while real Glass is reserved for the central Core Chamber.

These two fixes still require in-game angle/depth QA before they are called stable.

## Render QA still owed

Before stable release, deeply test:

- Entity/projector depth;
- Ghost/water ordering;
- overlapping translucent entities;
- eyes/glint/special RenderTypes;
- GIF stress/multiplayer transfer;
- Prism mixed-aspect faces.

---

# Crying Obsidian ecosystem

Authority: `docs/CRYING-OBSIDIAN-ECOSYSTEM-dev44.md`.

## Crying Obsidian Shard

**Implemented since dev.42.** Final shared item name: **Crying Obsidian Shard**.

```text
Stonecutter:
1 Crying Obsidian -> 4 Crying Obsidian Shards
```

```text
8 shards + Fire Charge OR Magma Cream -> 1 Crying Obsidian
```

dev.43 adds uncommon shard loot through data-driven Global Loot Modifiers: Ruined Portals (40%, 1–3), Abandoned Mineshafts (20%, 1–2) and village armorer/toolsmith/weaponsmith chests (12%, 1–2).

## Renewable crystal growth

**Implemented in dev.43 source candidate.** The old proposed normal-Obsidian dripstone/cauldron conversion is **retired**.

New loop:

```text
Lava (source or flowing)
Crying Obsidian
      ↓ grows downward
Small Bud -> Medium Bud -> Large Bud -> Mature Crying Obsidian Cluster
```

- Small-Bud nucleation accepts both source and flowing lava directly above the Crying Obsidian;
- the initial success gate is 1/5 per eligible random tick;
- later stages grow progressively faster;
- breaking without Silk Touch drops exactly 1/2/3/4 shards by age;
- Silk Touch drops the actual stage;
- Fortune does not increase shard counts initially;
- naive automation can harvest Small for 1 shard, while patient harvest reaches 4.

## Beacon-powered crystal

**Implemented in dev.43 source candidate, pending in-game render/balance QA.** As age increases:

- crystal absorbs more of the vertical beam;
- powered light increases;
- residual purple refraction becomes stronger.

Target visual transmission Small/Medium/Large/Mature:

```text
75% / 50% / 25% / 0% beam continues upward
```

Mature Cluster visually stops the beam while keeping Beacon gameplay active.

Future intended powered light with optional extended-light support:

```text
14 / 18 / 23 / 28
```

Current dev.43 vanilla fallback uses 14 / 15 / 15 / 15. Optional >15 integration is not bundled yet.

Powered stages can emit one narrow Crying-Obsidian-purple residual beam at a time; Mature has the strongest/longest leak:

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

Authority: `docs/CORES-AND-UPGRADES-dev44.md`.

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

Projector upgrades preserve the complete canonical persistent state, including Core, assets/GIFs, cards, Entity/Humanoid/Horse snapshots, Item/Banner snapshots and all Projection Settings.

dev.45 implements this with stateful projector ItemStacks (`minecraft:block_entity_data`) plus a custom `projector_upgrade` recipe serializer. Ordinary Survival mining now returns one packed projector item instead of ejecting Core/card/staging contents separately. The implemented progression is Mirage -> Display -> Wide/Tall/Prism/Field.

Do not replace this with ordinary shaped outputs that lose components/NBT.

---

# Core Booster

One user-facing block/item with five loadable materials:

- Core Booster shell (empty after crafting)
- Inserted material: Glass / Quartz / Amethyst / Diamond / Netherite
- Loaded Booster amplification: ×1.50
- Right-click inserts one valid material
- Shift + right-click removes the inserted material

Power design:

- same Base PU as standard material;
- target amplification ~×1.50;
- still uses normal chassis multiplier/PU/Overdrive.

Visual design:

- placeable decorative block;
- exact four-cube nested geometry supplied in `anidado.json`;
- cubes 2 and 4 are geometrically rotated by 45°;
- all shells use the Crying-Obsidian glass palette;
- inserted material floats and spins inside the smallest cube.

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
- max four effective Core Boosters;
- target final width cap ~2× vanilla.

---

# Development order from dev.45

1. **dev.43:** renewable Crying crystal growth + shard loot + Beacon light/refraction/residual beam.
2. **dev.44:** Obsidian Spike. Implemented source candidate.
3. **dev.45:** canonical state-preserving projector upgrade recipes/progression. **Current source candidate.**
4. **dev.46–54:** Improved Core prototype evolved into the single stateful Core Booster + projector amplification.
5. **Next:** Core-Booster Beacon relay/effects and powered-crystal coupling.
6. feature/render freeze QA.
7. controlled code refactor.
8. 0.1.0 release preparation.
9. **1.1.0+:** Mirage Scan Codex + scan-copy station.

Full roadmap: `docs/CURRENT-STATE-ROADMAP-dev44.md`.

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
