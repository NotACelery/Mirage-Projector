# QA Regression Matrix — Mirage Projector 1.0.0

Use this matrix after any bugfix or compatibility change on the stable branch, and as the baseline when opening the 1.1.0 development line.

## Build / startup

- Build with Java 21 and NeoForge 21.1.244+.
- Launch with neither EMI nor JEI installed.
- Launch with EMI only.
- Launch with JEI only.
- Launch with EMI + JEI together.
- Confirm client/server protocol agreement at protocol 27.

## Projector chassis

For each of the six chassis:

- place/orient the block;
- open the GUI;
- install/remove a Core;
- verify model ↔ VoxelShape alignment;
- verify inventory/held/dropped item presentation;
- verify save/reload and chunk unload/reload.

Chassis order:

1. Mirage Projector
2. Mirage Display
3. Mirage Field Projector
4. Wide Mirage Projector
5. Tall Mirage Projector
6. Mirage Prism

## Projection source matrix

Smoke-test every chassis with:

- Image/GIF;
- Item;
- Banner;
- Entity.

For every source:

- `Use <mode> mode` selects and activates it;
- active-source feedback is correct;
- `TURN OFF` hides the projection without deleting state;
- reopening workspaces does not silently change active source;
- Scale/Lift/Float/rotation/tint/ghost controls persist;
- large Lift/Scale projections recover correctly when entering/leaving view.

## Image / GIF

- PNG/JPG/static WebP/BMP import.
- animated GIF playback.
- server/client asset synchronization.
- cache miss/refetch.
- Wide/Tall multi-source layout.
- Prism cardinal faces.
- Field continuous plane.
- flip/scanlines/front-back behavior.

## Item / Banner

- virtual snapshot does not consume the source item/banner;
- block items render volumetrically where expected;
- clearing virtual state does not delete real inventory content;
- Prism banner cardinal faces remain independent.

## Entity

- Player skin/model parts;
- generic living entity snapshots;
- Humanoid armor/held items;
- Horse Saddle/Body Armor;
- pose presets;
- nameplates/custom names;
- per-channel equipment visibility;
- Ghost opacity/tint at 99/90/50/10%;
- glint/trims/eyes;
- Create Netherite Backtank compatibility;
- water/cloud/weather composition;
- conservative culling at large Lift/Scale.

Passenger/vehicle composite scans must remain rejected rather than partially serialized.

## Projection Power

- Glass/Quartz/Amethyst/Diamond/Netherite base Core capacity.
- dynamic feasible Scale/Lift/Float limits.
- Overdrive behavior with sufficient PU.
- Core Booster ×1.50 amplification.
- loaded Booster material persistence and extraction.

## Crying Obsidian

- shard crafting and Crying Obsidian reconstruction recipes;
- lava-above-Crying-Obsidian growth sequence;
- Small → Medium → Large → Cluster ordering;
- Silk Touch preserves stage;
- normal harvest produces fixed shard drops;
- Fortune does not multiply shards;
- Beacon attenuation/energization;
- material-specific Core Booster relay effects;
- Obsidian Spike damage/placement.

## Mirage Light Engine

- open curve `15,15,14,14,...,1,1`;
- 1/2/3-block walls;
- L-corner detours;
- slabs/stairs/partial opacity;
- chunk boundaries;
- unload/reload/relog/respawn/dimension changes;
- overlapping sources use maximum contribution;
- several simultaneous sources do not visibly stall the server;
- Glass diffusion, Quartz reach, Amethyst resonance, Diamond focus and Netherite inversion identities;
- two-client tracking/synchronization.

No current gameplay path may create `mirage_projector:crying_light_node`; that block is migration-only.

## EMI / JEI

### EMI

- Crafting tab appears for every projector chassis.
- Crying Obsidian has both shard-based crafting variants.
- World Interaction page opens from Crying Obsidian, all buds, Cluster and Shard.
- growth arrows/icons do not overlap.
- harvest examples show Silk Touch → Cluster and normal pickaxe → Shards.
- Block Drops order is Small → Medium → Large → Cluster.

### JEI

- custom projector upgrades appear under Crafting;
- Crying Obsidian shaped recipes appear normally;
- projector/Crying Obsidian ingredient information is present.

## Persistence / migration

- current-world save/reload preserves all four source families and transforms;
- legacy numeric projection-source saves migrate to stable namespaced IDs;
- migration-only Improved Core blocks convert safely;
- legacy physical Mirage light nodes are cleaned without deleting unrelated blocks;
- unknown future source IDs/payloads are preserved where supported.

## Release packaging

- `logo.png` appears in NeoForge metadata without blur;
- public version is correct;
- README/current docs describe stable behavior rather than development history;
- source-side suite passes:

```text
python tools/verify_current_line.py
```
