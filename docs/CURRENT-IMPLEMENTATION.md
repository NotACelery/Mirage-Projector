# Mirage Projector — Current implementation

Version line: **0.1.0-dev.74**  
Minecraft: **1.21.1**  
NeoForge: **21.1.244**  
Java: **21**  
Network protocol: **19**

Status: live Simple Light Level QA on dev.70–73 proved that physical auxiliary Light Nodes cannot reliably represent the desired Mature Cluster field: once placed, each node becomes an independent omnidirectional vanilla source and can refill hidden regions or distort the target curve. dev.74 therefore adds the new Mirage Light Engine as a server-side shadow solver/storage layer. It computes causally connected fixed-point voxel fields and aggregate virtual light without placing emitters, while the dev.73 physical backend remains active temporarily for visible/gameplay safety. dev.70 same-tick invalidation, dev.71 equipment visibility and dev.72 Entity envelope/frustum hardening remain accumulated. Windows `build.bat` plus `/miragelight` shadow-solver QA are required before dev.75 can make the virtual field authoritative.

## Projector family

Six playable chassis exist:

- Mirage Projector / Compact;
- Mirage Display;
- Wide Mirage Projector;
- Tall Mirage Projector;
- Mirage Prism;
- Mirage Field Projector.

All six share the same BlockEntity family and source/presentation state. Horizontal placement orientation is persisted. The physical emitter/chamber geometry varies by chassis.

Crafting progression is:

```text
Mirage Projector
      |
      v
Mirage Display
   |    |    |    |
   v    v    v    v
 Wide  Tall Prism Field
```

Upgrade recipes preserve projector state instead of creating an empty machine.

## Projection sources

Four active source families are implemented:

### Image

- PNG;
- JPG/JPEG;
- static WebP;
- BMP;
- animated GIF.

Image layouts:

- Compact/Display/Field: one continuous Plane;
- Wide: SINGLE or four-cell 4×1 MULTI;
- Tall: SINGLE or four-cell 1×4 MULTI;
- Prism: independent North/East/South/West faces.

Plane back behavior supports Front, Back, Mirrored, Readable and Independent semantics as applicable.

### Item

The Item workspace stores a virtual serialized copy. The original inventory item never becomes projector inventory. Blocks use their 3D item/block representation; ordinary items use the item renderer; standalone wearable equipment can use the projection rig path.

### Banner

Banner appearance is copied virtually. The real banner stays with the player. Plane chassis render cloth without the physical pole. Prism stores independent cardinal faces.

### Entity

Entity Scan Cards store frozen render data rather than a live ticking entity. Current support includes:

- generic living entities;
- Players with frozen profile/skin information;
- Humanoid equipment snapshots;
- bodyless Humanoid equipment projection;
- Horse Saddle and Body Armor channels;
- persistent per-channel render visibility for Head/Chest/Legs/Feet/Main Hand/Off Hand/Saddle/Body Armor without deleting snapshots;
- pose presets;
- species/pose/equipment-aware preview, clearance and render-envelope foundations;
- frozen custom names/nameplates positioned above the projected envelope and tinted/faded with the hologram;
- Piglin/Hoglin conversion-shake normalization for projection-only client entities.

Mounted/passenger composite scans remain intentionally rejected.

## Presentation controls

Implemented shared presentation state includes:

- Scale;
- Lift;
- rotation on/off;
- rotation period;
- clockwise/counter-clockwise;
- orientation offset;
- Floating on/off;
- time-based or rotation-synced floating;
- float amplitude and timing;
- world lighting or Fullbright;
- Ghost Effect / opacity;
- Tint;
- Image vertical flip;
- Image scanlines;
- development chassis-overdrive override for Creative testing.

## Power

Standard material Base PU:

| Material | Base PU | Standard amplification | Core Booster amplification |
|---|---:|---:|---:|
| Glass | 32 | ×1.00 | ×1.50 |
| Quartz | 48 | ×1.00 | ×1.50 |
| Amethyst | 64 | ×1.00 | ×1.50 |
| Diamond | 96 | ×1.00 | ×1.50 |
| Netherite | 128 | ×1.00 | ×1.50 |

Effective capacity:

```text
floor(Base PU × chassis multiplier × Core amplification)
```

Scale/Lift/Float slider maxima are solved against the current PU budget. Chassis nominal envelopes are efficiency targets; overdrive is allowed when PU can pay its quadratic penalty.

## Core Booster

One user-facing block/item exists: `mirage_projector:core_booster`.

The empty Booster accepts exactly one of:

- Glass;
- Quartz;
- Amethyst Shard;
- Diamond;
- Netherite Ingot.

Right-click inserts a valid material. Shift + right-click extracts it. Loaded Boosters persist their material, display the central material visually, and only stack with identical stored state. Empty Boosters are not valid Projection Cores.

Five old `improved_*_core` block IDs remain registered only for old dev-world migration. They have no user-facing BlockItems or recipes and are not active gameplay variants.

## Crying Obsidian ecosystem

Implemented:

- Crying Obsidian Shard;
- Stonecutter conversion: 1 Crying Obsidian → 4 shards;
- 8 shards + Fire Charge or Magma Cream → 1 Crying Obsidian;
- uncommon shard chest loot;
- renewable downward Small → Medium → Large → Mature crystal growth when lava is above Crying Obsidian;
- exact no-Silk drops 1/2/3/4 shards;
- Silk Touch stage recovery;
- no Fortune multiplier;
- directional decorative placement;
- Beacon excitation/attenuation;
- residual purple rays;
- Obsidian Spike block, recipe, movement hindrance and damage.

Current Beacon crystal behavior:

- Small transmits roughly 75%;
- Medium 50%;
- Large 25%;
- Mature 0%;
- buds do not become block-light sources merely because they intersect a Beacon;
- energized Mature is the current vanilla-level light source;
- a Mature directly over an active Beacon suppresses the Beacon's visible vertical beam and attempts to suppress the Beacon block's own light while the cluster remains present;
- occasional side rays originate at centered X/Z and approximately pixel Y=2, appear instantly at full length, hold about one second, then retract while fading;
- younger stages scale ray width, length and frequency down from Mature.

Core Booster Beacon relay modifiers are implemented in dev.60. Glass = Diffusion (+35 percentage points width), Quartz = Radiance (+25 points plus hotter beam color), Amethyst = Resonance (+25 points and ×1.25 rotation speed per effective Amethyst, capped near ×2), Diamond = Focus (+25 points with a tighter inner beam), and Netherite = Inversion (+25 points and reversed outgoing rotation). A maximum of four loaded Boosters contribute and total incoming-beam width is capped near ×2 vanilla. Generic incoming-beam width is not converted into a world-light range tier. Quartz reinforces the static reflected field, Diamond adds a smaller focused axial bonus, Glass broadens residual reflected-ray geometry only in dev.70, Amethyst increases residual-ray resonance, and Netherite preserves reflected rotation inversion. dev.69's long Glass face-diagonal static relays are intentionally removed because each auxiliary node emits scalar omnidirectional vanilla block light and could brighten the dark side of walls. All actual block/node emission remains vanilla-capped at level 15.

## Stateful drops and upgrades

Normal Survival projector breaking creates one stateful projector item carrying the BlockEntity payload. Upgrading through the custom projector recipe migrates/sanitizes that payload into the destination chassis and preserves current persistent systems rather than copying an arbitrary field list manually.

## Current render stabilization point

dev.57 was tested in-game and did not solve cloud/entity composition. dev.58 moved semi-transparent Entity projections to `AFTER_LEVEL`, but that stage is dispatched after Minecraft pops the world model-view matrix; using its identity `PoseStack` without restoring the supplied matrix made Ghost Entity projections disappear once opacity dropped below 100%. dev.60 live QA exposed the Create Backtank foil/glint multi-consumer crash; dev.61 fixed that crash with independent deferred builders. dev.62 restored the world model-view matrix around the late flush, added Entity-only late Ghost depth writes after world composition, disabled late translucent sorting, mirrored important vanilla fixed sheets and flushes fixed buffers per projected Entity. dev.63 adds a targeted compatibility rule for Create's Netherite Backtank: the equipped item is `BacktankItem.Layered`, Create replaces vanilla chest armor rendering with two synthetic Humanoid armor passes (`netherite_diving_layer_2` and `_layer_1`) and separately renders the physical tank geometry through `BacktankArmorLayer`. During Ghost rendering Mirage keeps the tank on the late depth-stable path, but renders the two synthetic chest layers without competing depth writes and distributes their alpha so the pair composes to approximately one ordinary armor layer at the requested opacity. Image/Banner/Item Ghost rendering retains the historical no-depth-write contract. Live dev.63 QA showed that keeping both Create synthetic diving layers translucent still made the chest region over-opaque because those two surfaces also stack over the projected Humanoid body. dev.64 replaces that experiment: while Ghost is active on `create:netherite_backtank`, the synthetic inner `netherite_diving_layer_2` pass is discarded, its paired glint is discarded when present, and the outer `netherite_diving_layer_1` pass is rendered once using the requested opacity and late Entity depth writes. The separate Backtank model remains on the standard modded-equipment path, and 100% opacity remains untouched.


## dev.65–73 physical light-field history

dev.65 introduced Mirage-owned `crying_light_node` blocks. dev.67 made only energized Mature Crying Obsidian drive the slow-decay world-light field; dev.69 separated Core Booster reflected identities; dev.70 added same-tick terrain invalidation and removed unsafe long Glass diagonal relay branches; dev.73 placed explicit per-cell axial relays after Simple Light Level exposed sparse-lattice decay errors.

Further dev.73 QA showed the physical-relay architecture itself is the remaining problem. Every accepted `crying_light_node` is still a real omnidirectional vanilla emitter after placement. As a result, six valid relay lines can create a cross-shaped filled region, light can be re-propagated behind geometry, and the final measured field is not guaranteed to match Mirage's intended per-voxel curve. These relays remain active in dev.74 only as a temporary compatibility/visible-light backend while the replacement is tested in shadow mode.

The stabilized optical behavior from this line remains accumulated:

- Core Booster relay effects begin at `8.5/16`;
- Small/Medium/Large continuation heights are `4.5/16`, `6.5/16`, `8.5/16`;
- Mature remains the complete vertical Beacon stop;
- residual-ray collision ignores the source crystal's own collider but not later obstacles;
- Small/Medium/Large remain optical-only and zero block-light;
- Quartz/Radiance is the main static power reinforcement, Diamond/Focus a smaller reinforcement, Glass/Diffusion visual widening, Amethyst/Resonance dynamic activity and Netherite/Inversion reversed rotation;
- terrain edits remain coalesced to `LevelTickEvent.Post`;
- Booster material swaps explicitly refresh nearby Mature fields;
- source removal/de-energization still reconciles the legacy backend while dev.74 is transitional.

## dev.74 Mirage Light Engine foundation

The forward architecture is now `celerbi.mirageprojector.light.engine`.

- `MirageLightSource` gives each light source a stable identity, origin, profile and runtime intent.
- `MirageLightProfile` stores conceptual power, fixed-point substeps, traversal cost, radius, shape/direction and future RGB metadata.
- `MirageLightSolver` propagates through six adjacent voxels, so every solved voxel remains causally connected to the source.
- `MirageLightOcclusion` delegates per-edge block/face obstruction to vanilla `LightEngine.getLightBlockInto(...)`.
- `MirageLightSection` stores per-source fixed-point energy sparsely in 16×16×16 sections.
- `MirageLightWorld` keeps source contributions separate and exposes an aggregate max layer for O(1) virtual-light lookup.
- the solver uses packed long positions, primitive fastutil maps and energy-bucket FIFO queues instead of object-heavy per-voxel queue nodes.
- Level unload clears the per-Level virtual state.

For the no-Booster Mature profile, the pure fixed-point result at outward distances 1–30 is exactly:

```text
15 15 14 14 13 13 12 12 11 11 10 10 9 9 8 8 7 7 6 6 5 5 4 4 3 3 2 2 1 1
```

Conceptual Booster power above 15 extends the saturated 15 plateau while final visible values remain capped to 15. A complete barrier can disconnect a region; a finite obstacle may still be routed around by a longer/weaker path, preserving vanilla-style grid behavior without teleporting new emitters.

dev.74 is intentionally **shadow-only**. It does not inject into vanilla/client light queries and does not remove the physical `crying_light_node` backend yet. Use `/miragelight stats`, `/miragelight probe`, `/miragelight axis <direction>` and `/miragelight rebuild` to inspect the new field. Simple Light Level continues to display the legacy physical result until dev.75.

Network protocol remains 19 and no new projector NBT contract is introduced by the foundation.

## Advanced Mirage Light Engine boundary

Only omnidirectional VANILLA/EXTEND-style scalar propagation is runtime-solved in dev.74. The source/profile contract reserves, but does not yet execute:

- `CONCENTRATE`;
- `DIRECTIONAL_SPOT`;
- `ROTATING_DIRECTIONAL_SPOT`;
- directional cone / rectangular frustum / plane shapes;
- `DYNAMIC_VISUAL` moving-source behavior;
- RGB/color-light rendering.

The next authority step is dev.75: make the virtual static field visible/gameplay-authoritative without feeding solved virtual voxels back into vanilla as new block emitters, add chunk-load invalidation/synchronization and migrate away from active physical relays.

