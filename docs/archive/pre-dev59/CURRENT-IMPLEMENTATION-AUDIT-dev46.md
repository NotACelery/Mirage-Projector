# Mirage Projector — implementation audit (dev.46)

## Scope

dev.46 builds directly on the Windows build-clean dev.45 stateful-upgrade baseline. It adds the five Improved Projection Cores as physical blocks/items and connects their x1.50 grade multiplier to the existing power architecture. Beacon relay behavior remains intentionally outside this wave.

## Build baseline

- **dev.45 compile status:** Windows `build.bat` confirmed successful by the user after the compile-import repair.
- **dev.46 status:** source candidate; Windows build and in-game QA pending.
- Network protocol remains **18**.

## Core registry / Power behavior

`ProjectionCoreProfile` now recognizes ten usable Core profiles: five standard and five Improved.

Standard Base PU stays unchanged: 32 / 48 / 64 / 96 / 128.

Improved variants keep the matching Base PU and apply `x1.50` amplification. No alternate power system exists; `ProjectionPower.effectiveCapacity` still computes:

```text
floor(Base PU × chassis multiplier × Core amplification)
```

Expected Improved material output before chassis multiplication: 48 / 72 / 96 / 144 / 192 PU.

## Physical blocks/items

Registered blocks/items:

- `improved_glass_core`
- `improved_quartz_core`
- `improved_amethyst_core`
- `improved_diamond_core`
- `improved_netherite_core`

They are placeable full-collision decorative blocks with non-occluding translucent models and self-drop loot tables. They are also accepted directly by the existing one-slot physical Core inventory. Because projector persistence stores the physical Core ItemStack wholesale, dev.45 packing/re-placement/chassis-upgrade infrastructure automatically preserves these new Core items.

## Model contract implemented

The shared model family uses:

1. aligned outer dark-purple translucent shell;
2. smaller middle shell as a **real model cuboid rotated 45 degrees around Y**;
3. smaller aligned inner shell;
4. centered crossed material/item cards for the matching base material.

The shells use different physical extents so they are not coplanar. NeoForge `minecraft:translucent` model rendering is requested explicitly.

## Crafting candidate

All five use the playable QA candidate:

```text
G S G
S C S
G S G
```

- `G` Glass
- `S` Crying Obsidian Shard
- `C` matching standard Core material/item

The exact balance remains provisional even though the recipe is now implemented for testing.

## UI / handbook

The empty Core-slot tooltip now includes Improved Cores and sorts entries by effective material output rather than enum declaration order. Handbook text no longer describes Improved Cores as future; it states the dev.46 x1.50 implementation.

## Explicitly not implemented

- Beacon beam relay through Improved Cores;
- Diffusion / Radiance / Resonance / Focus / Inversion effects;
- stacking/caps for Improved-Core Beacon columns;
- extended-range lighting integration;
- crystal response to amplified beams.

Those remain the dev.47 branch.

## Metadata invariant

Public author/developer identity is **Celerbi**. Packaged mod metadata and descriptive text contain no repository reference.
