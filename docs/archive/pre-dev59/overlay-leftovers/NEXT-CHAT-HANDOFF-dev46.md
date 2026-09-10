# Mirage Projector — next-chat handoff after dev.46

## Baseline

Current source candidate: **0.1.0-dev.46**. Protocol **18**.

**dev.45 is Windows build-clean.** The user confirmed `build.bat` succeeds after the ClipContext/Block import repair. dev.46 has not yet received Windows compile or in-game QA.

## dev.46 implementation

Five Improved Projection Cores now exist as blocks/items:

- Glass: 32 Base PU ×1.50;
- Quartz: 48 Base PU ×1.50;
- Amethyst: 64 Base PU ×1.50;
- Diamond: 96 Base PU ×1.50;
- Netherite: 128 Base PU ×1.50.

They reuse the existing `ProjectionCoreProfile` / `ProjectionPower` formula and the existing physical Core slot. No new networking is needed.

Models use three separated translucent dark-purple shells. The middle shell is an actual cuboid rotated 45° around Y. The matching source material/item is represented at the exact center.

Recipe candidate implemented for all five:

```text
G S G
S C S
G S G
```

where `G=Glass`, `S=Crying Obsidian Shard`, `C=matching standard material/item`.

## Critical persistence consequence

Do not create special Improved-Core persistence code. The Core slot already serializes its ItemStack inside the projector BlockEntity. dev.45 stateful drops and `ProjectorStateTransfer` therefore carry Improved Cores automatically.

## Metadata invariant

The public developer/author identity is **Celerbi**. `a different account identity` must not be shown as author. Packaged metadata/description must contain no repository reference.

## Next wave

**dev.47 = Improved-Core Beacon relay and crystal coupling.**

Planned identities from the frozen contract:

- Glass — Diffusion;
- Quartz — Radiance;
- Amethyst — Resonance;
- Diamond — Focus;
- Netherite — Inversion.

Implement relay/width/color continuity and capped stacking before wiring effects into powered Crying Obsidian Bud/Cluster response. Extended-light range should use a maintained integration if available; avoid invasive global light-engine rewrites.

## dev.46 QA before dev.47 is considered stable

- Windows build;
- five recipes;
- five block models/items;
- x1.50 values;
- nested-shell visual QA;
- Core Chamber render;
- packed drop/re-placement;
- Display/specialist upgrade preservation;
- standard Core regression.
