# Mirage Projector — next chat handoff (dev.41)

This file is a compact recovery entry point for continuing development after the long dev.36–dev.41 design/recovery conversations.

## Read order

1. `DOCUMENTATION-AUTHORITY-dev41.md`
2. `CURRENT-STATE-ROADMAP-dev41.md`
3. `CURRENT-IMPLEMENTATION-AUDIT-dev41.md`
4. `CRYING-OBSIDIAN-ECOSYSTEM-dev41.md`
5. `CORES-AND-UPGRADES-dev41.md`
6. focused Power/Image/GIF/Entity contracts only when touching those systems.

## Current baseline

- version: `0.1.0-dev.41`
- Minecraft 1.21.1
- NeoForge 21.1.244
- Java 21
- protocol 18
- dev.40 ran in-game
- Piglin/Hoglin projection dimension-shake normalization is confirmed for Piglin
- dev.41 is a consolidation/documentation/code-hygiene source line intended to be behavior-neutral

## Current known visual bugs to fix first

1. installed standard Core visual can disappear from certain camera angles;
2. current raw Core -> material-block visual is deprecated/misleading;
3. broad thin Glass/Glass-Pane model surfaces appear as translucent ghost layers.

Planned dev.42 replacement:

- Crying Obsidian Shard material;
- refreshed chassis art;
- universal ~4×4×4 Glass Core Chamber;
- actual installed Core ItemStack floating/rotating;
- corrected culling/bounds.

## Implemented major systems

- six chassis: Mirage, Display, Wide, Tall, Field, Prism;
- Furnace-like horizontal facing;
- idle floating book on all six;
- dev.38 PU system with chassis multipliers, dynamic sliders and Overdrive;
- static PNG/JPEG/WebP/BMP + animated GIF;
- content sniffing / renamed-file failsafe;
- Wide SINGLE/4×1, Tall SINGLE/1×4;
- Field one continuous large Plane — never 3×3;
- Prism four cardinal adaptive faces;
- Item/Banner virtual snapshots;
- Entity scans, Player appearance, Humanoid/Horse virtual gear;
- incompatible gear cleanup when entity family changes;
- render-only/no-tick projection entities.

## Current Power baseline

Standard Base PU:

- Glass 32
- Quartz 48
- Amethyst 64
- Diamond 96
- Netherite 128

```text
Effective PU = floor(Base PU × chassis multiplier × Core amplification)
```

Chassis multipliers:

- Mirage ×1.00
- Display ×1.50
- Wide ×2.00
- Tall ×2.00
- Prism ×2.00
- Field ×4.00

Standard amplification ×1.00. Improved target later ~×1.50.

## Crying Obsidian design that replaces the old conversion plan

The old `normal Obsidian + lava + Pointed Dripstone + Cauldron -> Crying Obsidian` system is retired.

New renewable system:

```text
Lava source
Crying Obsidian
      ↓
Small Bud -> Medium -> Large -> Mature Cluster
```

- grows downward from Crying Obsidian;
- first nucleation is slowest;
- later stages increasingly faster;
- no-Silk drops exactly 1/2/3/4 Crying Obsidian Shards;
- Silk Touch gets the current stage;
- no Fortune bonus initially;
- Stonecutter still converts 1 Crying Obsidian -> 4 Shards;
- 8 Shards + Fire Charge or Magma Cream -> 1 Crying Obsidian;
- small shard loot planned for Ruined Portals/Mineshafts/smith-related pools.

Final planned item name is **Crying Obsidian Shard**, not Cut Obsidian Shard.

## Beacon + Crying Obsidian crystals

Visual vertical transmission by age:

- Small ~75%
- Medium ~50%
- Large ~25%
- Mature 0%

Beacon gameplay remains active; mature crystal only terminates the visual beam.

Powered-light intended extended curve:

- 14 / 18 / 23 / 28

Vanilla fallback caps at 15. Optional extended-light compatibility must remain optional.

Residual leak beam:

- one active per crystal;
- Crying-Obsidian purple;
- about half vanilla Beacon inner-beam width;
- mature target 3–4 blocks;
- pseudo-random 3D direction excluding near-main-axis directions;
- world-collision/raycast limited;
- Phase A: extend strongly from cluster;
- Phase B: hold max length ~1 s;
- Phase C: retract toward cluster while fading to transparent;
- random cooldown, older crystals leak more strongly/frequently.

## Obsidian Spike

Planned:

- 3 Crying Obsidian Shards + 2 String + 1 Stick;
- about half-block height;
- nine tips, thin/tall center tip;
- bush-like slowdown;
- 2.0 damage points / 1 heart per successful hurt event.

## Projector crafting progression

Frozen graph:

```text
Mirage -> Display -> Wide/Tall/Prism/Field
```

Base Mirage:

```text
S S S
S G S
O O O
```

Display:

```text
Q S A
S M S
A S Q
```

Prism from Display:

```text
S G S
G D G
S G S
```

Wide/Tall must have equal cost. Field is much more expensive and uses whole Crying Obsidian.

Hard rule: upgrades preserve **all projector persistent state** via canonical state transfer, not hand-copied fields/plain shaped recipe output.

## Improved Cores

Exactly five:

- Improved Glass = Diffusion
- Improved Quartz = Radiance
- Improved Amethyst = Resonance
- Improved Diamond = Focus
- Improved Netherite = Inversion

Projector behavior:

- same Base PU;
- ~×1.50 amplification target;
- normal PU/Overdrive remains.

Block visual:

- three nested dark-purple transparent shells;
- middle shell truly rotated geometry;
- matching material in center.

Beacon relay:

- incoming terminates at Y+0.5;
- outgoing starts at Y+0.5;
- beam wider;
- stained-glass color preserved;
- max four effective Improved Cores;
- target width cap ~2× vanilla.

## Explicit brainstorms/backlog, not requirements

- Glowstone currently has no assigned role. Do not replace Glass Core/crafting with it without a new decision.
- possible future Unrefined Crying Crystal Core using mature cluster and intentionally distorting projections;
- Animated WebP/APNG playback;
- true RGB colored world light;
- Scan Codex/copy station in 1.1.0+.

## Recommended next snapshots

- dev.42: Shard + chassis visual/Core Chamber fix.
- dev.43: renewable crystals + Beacon refraction/light/residual beams + loot.
- dev.44: Obsidian Spike.
- dev.45: state-preserving crafting upgrades.
- dev.46: Improved Cores.
- dev.47: Improved-Core Beacon relay.
- then feature/render freeze QA, controlled refactor, 0.1.0 release prep.

## Never revive silently

- Field 3×3 grid;
- broad Glass ghost sheets;
- raw Core material blocks as final visual;
- Core hard Scale/Lift/Float caps;
- normal Obsidian dripstone/cauldron conversion to Crying Obsidian;
- multiple named Improved-Core variant families per material;
- EFFIGY/COLOSSAL placeholders as active chassis;
- Cut Obsidian Shard final naming.
