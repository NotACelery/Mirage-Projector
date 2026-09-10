# Mirage Projector — Current Implementation Audit (dev.42)

> This document describes the dev.42 source candidate. Anything explicitly marked future is not implemented yet.

## Implemented foundations

- Six registered chassis: Compact/Mirage, Display, Wide, Tall, Field, Prism.
- Horizontal placement facing.
- Dynamic PU/Overdrive Power System.
- Standard raw-material Cores: Glass/Quartz/Amethyst/Diamond/Netherite.
- Image static formats + GIF; content-based format detection.
- Wide SINGLE/4×1, Tall SINGLE/1×4, Field single plane, Prism four cardinal faces.
- Item/Banner virtual snapshots.
- Entity Scan, Humanoid/Horse equipment, Player snapshot fidelity.
- Piglin/Hoglin dimension shaking normalization.
- Debug Handbook.

## New in dev.42

- `Crying Obsidian Shard` registered and translated.
- 1 Crying Obsidian -> 4 shards Stonecutter recipe.
- 8 shards + Fire Charge -> Crying Obsidian.
- 8 shards + Magma Cream -> Crying Obsidian.
- custom shard sprite and Crying-Obsidian optical emitter texture.
- all six physical models converted to NeoForge composite base/emitter/chamber structure.
- broad old Glass/Pane optical plates removed.
- universal 4×4×4 central Glass Core Chamber.
- actual installed Core ItemStack rendered in chamber with rotation/bob/fullbright.
- fake material-block Core substitution removed from active code.
- legacy per-chassis Core width/height/depth fields removed; only chamber center remains.
- Field visually adopts a Crying Obsidian base.

## Still future

### dev.43 target

- Small/Medium/Large/Mature Crying Obsidian crystal growth.
- Lava over Crying Obsidian downward nucleation.
- 1/2/3/4 shard harvest model, Silk Touch, no Fortune initially.
- structure loot for shards.
- energized crystal variants.
- Beacon attenuation/termination by age.
- residual purple escape beam.
- lighting integration abstraction and extended-light library decision.

### dev.44 target

- Obsidian Spike.

### dev.45 target

- state-preserving chassis upgrade recipe infrastructure + progression.

### dev.46 target

- five Improved Cores.

### dev.47 target

- Improved-Core Beacon relay and its coupling into powered crystals.

## Deprecated/removed active concepts

Do not restore:

- Field 3×3 active grid;
- raw material Core rendered as material block;
- broad Glass/Glass-Pane sheets as projector optical system;
- per-Core hard Scale/Lift/Float caps;
- EFFIGY/COLOSSAL placeholder chassis;
- normal Obsidian -> Crying Obsidian dripstone/cauldron conversion;
- `Cut Obsidian Shard` as final item name.

