# Mirage Projector — Current implementation

Version line: **0.1.0-dev.69**  
Minecraft: **1.21.1**  
NeoForge: **21.1.244**  
Java: **21**  
Network protocol: **18**

Status: dev.69 refines the energized Mature Cluster light field so world-light reflection is occlusion-aware and Core Booster identities remain distinct after the Beacon reaches Crying Obsidian. Live QA confirms the dev.64 Create Netherite Backtank path can fade fully under Ghost. Windows `build.bat` plus in-game reflected-light/occlusion QA remain required before build-clean/runtime-clean acceptance.

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
- pose presets;
- species/pose-aware preview and clearance foundations;
- frozen custom names/nameplates;
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

Core Booster Beacon relay modifiers are implemented in dev.60. Glass = Diffusion (+35 percentage points width), Quartz = Radiance (+25 points plus hotter beam color), Amethyst = Resonance (+25 points and ×1.25 rotation speed per effective Amethyst, capped near ×2), Diamond = Focus (+25 points with a tighter inner beam), and Netherite = Inversion (+25 points and reversed outgoing rotation). A maximum of four loaded Boosters contribute and total incoming-beam width is capped near ×2 vanilla. dev.69 no longer turns that generic width into a world-light range tier. Reflected world light now maps material identity explicitly: Quartz reinforces the full reflected field, Diamond adds a smaller focused axial bonus and counteracts Glass diffusion, Glass activates bounded secondary face-diagonal coverage, Amethyst increases residual-ray resonance, and Netherite preserves reflected rotation inversion. All actual block/node emission remains vanilla-capped at level 15.

## Stateful drops and upgrades

Normal Survival projector breaking creates one stateful projector item carrying the BlockEntity payload. Upgrading through the custom projector recipe migrates/sanitizes that payload into the destination chassis and preserves current persistent systems rather than copying an arbitrary field list manually.

## Current render stabilization point

dev.57 was tested in-game and did not solve cloud/entity composition. dev.58 moved semi-transparent Entity projections to `AFTER_LEVEL`, but that stage is dispatched after Minecraft pops the world model-view matrix; using its identity `PoseStack` without restoring the supplied matrix made Ghost Entity projections disappear once opacity dropped below 100%. dev.60 live QA exposed the Create Backtank foil/glint multi-consumer crash; dev.61 fixed that crash with independent deferred builders. dev.62 restored the world model-view matrix around the late flush, added Entity-only late Ghost depth writes after world composition, disabled late translucent sorting, mirrored important vanilla fixed sheets and flushes fixed buffers per projected Entity. dev.63 adds a targeted compatibility rule for Create's Netherite Backtank: the equipped item is `BacktankItem.Layered`, Create replaces vanilla chest armor rendering with two synthetic Humanoid armor passes (`netherite_diving_layer_2` and `_layer_1`) and separately renders the physical tank geometry through `BacktankArmorLayer`. During Ghost rendering Mirage keeps the tank on the late depth-stable path, but renders the two synthetic chest layers without competing depth writes and distributes their alpha so the pair composes to approximately one ordinary armor layer at the requested opacity. Image/Banner/Item Ghost rendering retains the historical no-depth-write contract. Live dev.63 QA showed that keeping both Create synthetic diving layers translucent still made the chest region over-opaque because those two surfaces also stack over the projected Humanoid body. dev.64 replaces that experiment: while Ghost is active on `create:netherite_backtank`, the synthetic inner `netherite_diving_layer_2` pass is discarded, its paired glint is discarded when present, and the outer `netherite_diving_layer_1` pass is rendered once using the requested opacity and late Entity depth writes. The separate Backtank model remains on the standard modded-equipment path, and 100% opacity remains untouched.


## dev.65 extended light field
Historical dev.65 introduced Mirage-owned `crying_light_node` blocks for energized Mature Crying Obsidian. That first 8/16/24-shell implementation is no longer the live runtime and is retained only as implementation history.

The current field is described by dev.67–69: an energized Mature source always has the slow-decay baseline, even with no Core Booster; younger stages remain zero-block-light. Mirage nodes remain internal-only, replaceable, non-item, no-collision and overlap-aware.

## dev.67 Beacon/Crying Obsidian stabilization

- Core Booster relay effects begin at `8.5/16` inside each Booster block. The lower portion of the incoming Beacon beam is rendered with the pre-Booster relay state; the outgoing segment above that plane uses the newly applied material effect.
- Small/Medium/Large bud continuation heights are `4.5/16`, `6.5/16` and `8.5/16` respectively. The incoming beam still stops at the bud base; only the attenuated continuation restarts at the visible bud silhouette height. Mature remains a full vertical stop.
- Custom Beacon quad ordering now matches the vanilla Beacon renderer topology, removing the accidental diagonal face that could produce N/X-shaped beams after relay or bud continuation.
- Residual-ray collision tests ignore the source crystal's own collision volume while retaining the visual ray origin at pixel 2. This reconnects the burst renderer without allowing rays through later obstacles.
- Small/Medium/Large buds remain zero-block-light stages even when energized; their Beacon interaction is optical attenuation plus residual-ray emission. Only an energized Mature Cluster emits world light and drives Mirage-owned auxiliary nodes that reproduce a half-speed falloff along the field axes (`5,5,4,4,3,3...`). dev.69 supersedes the old relay-width tier mapping with material-specific reflected-light behavior.
- dev.66 `LightProfile` placeholders remain unchanged and disconnected unless already runtime-supported.

## dev.66 advanced light-profile foundation
dev.66 introduced `LightProfile`, `LightDecayMode` and `LightProfileMath` as a reusable contract for later lighting features. `VANILLA` and `EXTEND` are currently runtime-supported. `CONCENTRATE`, `DIRECTIONAL_SPOT` and `ROTATING_DIRECTIONAL_SPOT` are reserved placeholders and are intentionally disconnected from world mutation.

The profile contract records base light, maximum radius, direction, cone angle, refresh interval and optional rotation period. `CryingObsidianLightField.profileForRelay(...)` exposes the current reflected axial reach through that contract; the live node topology, material mapping and occlusion behavior are governed by dev.69.

The placeholder modes preserve the central constraint established during the dev.68→69 lighting discussion: faster-than-vanilla or truly directional world lighting requires suppressing/replacing the source's ordinary omnidirectional block-light emission. Additive lower-level nodes cannot subtract light already propagated by an active vanilla source.

## dev.68 interaction/light safety
Loaded Core Booster extraction is restricted to Shift + right-click with an empty hand or a held item matching the installed material. Different held items do not trigger or cancel Mirage extraction.

The Mature Cluster slow-decay field is active whenever Beacon-energized, with or without a Core Booster. Breaking or de-energizing the Mature source performs immediate overlap-aware node reconciliation instead of waiting for node scheduled ticks.


## dev.69 reflected-light occlusion and Booster identity

The six axial half-decay branches remain the baseline. Source-to-node tracing now distinguishes full and partial light blockers: a fully opaque path terminates that candidate, while partial blockers contribute extra attenuation instead of being treated as transparent. Because no Mirage node is placed beyond a fully blocked source path, the node network does not teleport full-strength light through walls; Minecraft still owns local propagation, corner wrap, ambient occlusion and face shading around the surviving nodes.

Core Booster reflection is split by material rather than collapsed into `widthScale`. Quartz/Radiance is the primary conceptual-light/range amplifier. Diamond/Focus contributes a smaller axial reach bonus and cancels one Glass diffusion tier per Diamond. Glass/Diffusion can activate twelve bounded face-diagonal branch directions with deliberately shorter/dimmer coverage than the primary axes. Amethyst/Resonance accelerates reflected residual activity, and Netherite/Inversion reverses reflected ray rotation without granting static range. Residual ray width, brightness, length and cadence now use dedicated reflected-relay helpers.

Per-source refresh computes the Beacon relay once and reuses the resulting field specification for all candidates, avoiding the old repeated full Beacon-column scan once per node.