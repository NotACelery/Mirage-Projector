# Mirage Projector — Current implementation

Version line: **0.1.0-dev.80**  
Minecraft: **1.21.1**  
NeoForge: **21.1.244**  
Java: **21**  
Network protocol: **26**

Status: static Mature lighting remains server-authoritative and QA-confirmed through `dev.76h`. `dev.77` differentiates Core Booster identities. `dev.78`–`dev.79i` completed the alternate-projector comparison/refinement line, including model↔VoxelShape reconciliation, Compact Z-fighting cleanup, Tall front/rear symmetry and the fixed-projector ON/OFF/active-source UX. **dev.80 closes that comparison line:** the six accepted dev.79i models are promoted to the six canonical projector IDs, all temporary `*_alt` blocks/items/resources are removed from runtime registration, and the old canonical model JSONs are archived outside `assets` under `docs/history/projector-models-pre-dev80/`. Physical `crying_light_node` relays remain migration-only.

## dev.75b authoritative virtual-light lifecycle

The dev.75 authority handoff is complete and remains accumulated in dev.75d:

- server alone solves STATIC_WORLD fields; clients receive packed final visible levels per chunk section;
- source delivery is scoped to watched chunks and retracts when no watched chunk needs the source;
- same-level respawn can CLEAR/repopulate without depending on vanilla chunk retransmission;
- server chunk/terrain geometry changes re-solve relevant static sources and publish affected aggregate sections;
- source-origin unload removes that source; destination chunk arrival can refill the surviving field;
- client changes dirty old/new render sections;
- terrain invalidation covers block place/multi-place/break, fluids, crop/feature growth, pistons and explosions;
- Core Booster swaps refresh nearby Mature sources;
- loaded chunks palette-scan for orphan legacy `crying_light_node`; current runtime never creates one;
- effective reads are `max(vanilla, Mirage)` without injecting Mirage into vanilla propagation.

dev.75c then fixed the destination-opacity argument passed to vanilla edge occlusion. dev.75d adds the profile-level detour penalty described below. Historical dev.75 source descriptors used protocol 21; current STATIC_WORLD transport is revisioned chunk snapshots/manifests under protocol **26**.

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


## Canonical projector model line

As of dev.80 there is only one runtime model/registry identity per projector chassis. The accepted dev.79i comparison visuals are now the canonical models for:

- Mirage Projector;
- Mirage Display;
- Mirage Field Projector;
- Wide Mirage Projector;
- Tall Mirage Projector;
- Mirage Prism.

The temporary `*_alt` block/item IDs, blockstates, item models, block models and loot tables are no longer registered or shipped as active resources. The six pre-dev.80 canonical block-model JSONs are retained only under `docs/history/projector-models-pre-dev80/legacy-canonical/` for archaeology; they are not runtime assets. Canonical VoxelShapes and renderer anchor layouts now directly use the promoted geometry.

The custom Mirage creative tab and vanilla Functional Blocks tab list the six projectors in this order: Mirage Projector, Mirage Display, Mirage Field Projector, Wide Mirage Projector, Tall Mirage Projector, Mirage Prism.

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

## Projector active-state / shutdown UX

`dev.79d` implements the fixed-projector active-state/shutdown contract, retained in dev.80:

- projectors have an explicit persisted/synchronized ON/OFF state independent from source identity;
- `TURN OFF` stops the visible projection without deleting configured source or presentation state; the inserted Core remains physically visible;
- `Use <mode> mode` both selects the workspace source as the active source and re-enables projection;
- opening Image/Item/Entity/Banner workspaces is navigation only and must not silently change the active projection;
- each Image/Item/Entity/Banner workspace changes `Use <mode> mode` to disabled `Mode currently Active` only when that mode is actually ON;
- the main source-selection menu draws a white outline around the Image/Item/Entity/Banner button that is **actually active**;
- while the projector is OFF, none of those source buttons is shown as active, even though the last source/configuration remains stored for later reuse.

The implementation should therefore preserve the distinction `open workspace != active source != projection enabled`. OFF must not be represented by extending the closed source enum with a fake source value.

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

Core Booster Beacon relay modifiers are implemented in dev.60. Glass = Diffusion (+35 percentage points width), Quartz = Radiance (+25 points plus hotter beam color), Amethyst = Resonance (+25 points and ×1.25 rotation speed per effective Amethyst, capped near ×2), Diamond = Focus (+25 points with a tighter inner beam), and Netherite = Inversion (+25 points and reversed outgoing rotation). A maximum of four loaded Boosters contribute and total incoming-beam width is capped near ×2 vanilla. Generic incoming-beam width is not converted into a world-light range tier. Quartz reinforces the static reflected field, Diamond adds a smaller focused axial bonus, Glass broadens residual reflected-ray geometry, Amethyst increases residual-ray resonance, and Netherite preserves reflected rotation inversion. dev.69's long Glass face-diagonal static relays are historical and remain removed because each physical auxiliary node became an omnidirectional vanilla emitter. Current Mature world light is virtual; exposed scalar block-light output remains capped to level 15.

## Stateful drops and upgrades

Normal Survival projector breaking creates one stateful projector item carrying the BlockEntity payload. Upgrading through the custom projector recipe migrates/sanitizes that payload into the destination chassis and preserves current persistent systems rather than copying an arbitrary field list manually.

## Current render stabilization point

dev.57 was tested in-game and did not solve cloud/entity composition. dev.58 moved semi-transparent Entity projections to `AFTER_LEVEL`, but that stage is dispatched after Minecraft pops the world model-view matrix; using its identity `PoseStack` without restoring the supplied matrix made Ghost Entity projections disappear once opacity dropped below 100%. dev.60 live QA exposed the Create Backtank foil/glint multi-consumer crash; dev.61 fixed that crash with independent deferred builders. dev.62 restored the world model-view matrix around the late flush, added Entity-only late Ghost depth writes after world composition, disabled late translucent sorting, mirrored important vanilla fixed sheets and flushes fixed buffers per projected Entity. dev.63 adds a targeted compatibility rule for Create's Netherite Backtank: the equipped item is `BacktankItem.Layered`, Create replaces vanilla chest armor rendering with two synthetic Humanoid armor passes (`netherite_diving_layer_2` and `_layer_1`) and separately renders the physical tank geometry through `BacktankArmorLayer`. During Ghost rendering Mirage keeps the tank on the late depth-stable path, but renders the two synthetic chest layers without competing depth writes and distributes their alpha so the pair composes to approximately one ordinary armor layer at the requested opacity. Image/Banner/Item Ghost rendering retains the historical no-depth-write contract. Live dev.63 QA showed that keeping both Create synthetic diving layers translucent still made the chest region over-opaque because those two surfaces also stack over the projected Humanoid body. dev.64 replaces that experiment: while Ghost is active on `create:netherite_backtank`, the synthetic inner `netherite_diving_layer_2` pass is discarded, its paired glint is discarded when present, and the outer `netherite_diving_layer_1` pass is rendered once using the requested opacity and late Entity depth writes. The separate Backtank model remains on the standard modded-equipment path, and 100% opacity remains untouched.


## dev.65–73 physical light-field history

dev.65–73 used physical `crying_light_node` relays. Those experiments established the desired Mature half-decay and Core Booster identities but live numbered-floor/wall QA proved the architecture itself unsafe: every relay becomes an independent omnidirectional vanilla source, producing cross-shaped overfill, wall leakage and a final field that cannot remain source-causal.

The retained lessons are now implemented by the virtual engine: exact fixed-point half-decay, source ownership, same-tick/coalesced invalidation, Quartz/Diamond static reinforcement, Glass/Amethyst/Netherite reflected visual identities and immediate source teardown. Physical relays have no current gameplay authority and exist only for old-world migration.

## Mirage Light Engine runtime

The authoritative runtime is documented exhaustively in `MIRAGE-LIGHT-ENGINE.md`. Core behavior in dev.75d:

- six-neighbour causal weighted flood;
- fixed-point no-Booster open curve exactly `15,15,14,14,...,1,1` over 30 blocks;
- real destination `getLightBlock(...)` opacity plus vanilla face-shape occlusion;
- fully opaque edges are impossible; finite walls can only be reached around real geometry;
- `detourExtraCostUnits` separates open decay from obstacle-only extra route cost; current Mature profile uses substeps=2, air=1, detourExtra=1;
- an optimal open route remains half-decay; every extra block of route forced by geometry costs a full visible level overall;
- sparse per-source 16³ sections and aggregate max layer provide O(1) reads;
- static section payloads are tracking-scoped to watched chunks; protocol is **22**;
- `/miragelight probe` reports Mirage/nearest/vanilla/effective plus weighted `direct` and `extra` cost;
- physical legacy nodes are cleanup-only.

Quartz/Radiance and Diamond/Focus increase conceptual Mature power; visible output remains capped at 15. Glass/Amethyst/Netherite keep reflected visual roles and do not create independent static side emitters.

## Advanced Mirage Light Engine boundary

The static Mature consumer is now the reference implementation. dev.76 begins dynamic/mobile-light foundation rather than reopening the physical-relay design. Reserved runtime work includes `DYNAMIC_VISUAL`, moving/portable sources, directional/rotating spotlights, rectangular frustum, plane/projected-surface emission and RGB-preserving visual lighting.

The scalar static engine may continue serving gameplay/light-level semantics, while future moving visual emitters must avoid rebuilding server world-light every render frame. Third-party consumers that bypass normal brightness APIs receive targeted compatibility only after concrete QA demonstrates a need.

## Post-dev.75d roadmap consolidation

Runtime static-light architecture is dev.76h/protocol 26. The project roadmap is now split by release scope. 1.0.0 retains the current fixed-projector feature set and must establish extension seams for projection-source registration, chassis capabilities, forward-compatible presentation transforms, generic renderer/interaction providers, dynamic-vs-static light backend separation and non-Core energy consumers. User-facing lanterns/Glow Dust batteries/Scan Codex/portable projectors belong to 1.1.0; direct hologram grab/free rotation belongs to 1.2.0; Create Blueprint projection remains an optional bridge addon.
