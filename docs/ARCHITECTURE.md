# Mirage Projector — Architecture

## Major state owners

### `MirageProjectorBlockEntity`

Owns the persistent projector state:

- Projection Settings;
- physical Core slot;
- image asset references/source bank;
- virtual Item snapshot;
- virtual Banner snapshots;
- frozen Entity projection state;
- incoming physical Entity-equipment staging;
- virtual projected equipment;
- migration fields required by older development saves.

### `ProjectionSettings`

Owns shared presentation and Image-facing state. Wire/NBT input is sanitized before use.

### `EntityProjectionState`

Owns frozen Entity identity/render state and pose/equipment projection state independently from a live source entity.

### `CoreBoosterBlockEntity`

Owns the loaded Core Booster material for item persistence/migration while `CoreBoosterBlock.MATERIAL` is the placed-block visual authority.

### Light profile layer

`LightProfile` and `LightDecayMode` define a reusable world-light profile contract. The active dev.69 Crying Obsidian runtime maps the Mature Cluster to an `EXTEND`-style reflected field while keeping Mirage-owned node lifecycle, overlap resolution and source causality authoritative. Reflected relay semantics are material-specific instead of being inferred from generic Beacon width: Glass = diffusion coverage, Quartz = radiance, Diamond = focus, Amethyst = resonance and Netherite = inversion. `CONCENTRATE`, `DIRECTIONAL_SPOT` and `ROTATING_DIRECTIONAL_SPOT` remain reserved schema placeholders only. `LightProfileMath` contains pure future-facing falloff/cone calculations and performs no world mutation.

## Renderer families

Do not collapse all source types into one quad renderer.

- Image/Banner are plane/cardinal-face geometry.
- Item is a real item/block model.
- Entity is a reconstructed render-only client entity plus equipment layers.
- Core/Booster centers use real item models.

`MirageProjectorRenderer` is still the largest rendering coordinator. It delegates/supports source-specific helpers but remains a future refactor hotspot.

## Entity render isolation

Projection Entity rendering uses a projection-owned `MultiBufferSource`/RenderType normalization layer. Shared global shader state must not be left modified across Minecraft batches.

Deferred Entity rendering is split by opacity:

- fully opaque projections use the normal deferred stage;
- semi-transparent projections are drawn at `AFTER_LEVEL` so cloud/weather framebuffer composition has already completed.

`AFTER_LEVEL` is outside `LevelRenderer`'s pushed world model-view state. dev.62 therefore restores the matrix supplied by `RenderLevelStageEvent` only for the duration of the Mirage late flush. The late Ghost Entity path normally uses colour + depth writes and no translucent quad sorting. This depth-write exception is safe only because the level, including water/cloud/weather composition, has already rendered and GameRenderer clears depth before first-person hand rendering. Image/Banner/Item Ghost paths continue to use colour-only writes.

dev.64 replaces the dev.63 pair-alpha experiment with a single-surface compatibility rule for `create:netherite_backtank`. During Ghost rendering the synthetic `netherite_diving_layer_2` underlay is discarded, the outer `netherite_diving_layer_1` surface receives the requested opacity and late Entity depth writes, and the separate Backtank geometry keeps its normal late Ghost path. At 100% opacity Mirage does not intervene and Create retains its native two-layer chest rendering.

The late path remains under QA until cloud, water, z-order and modded-armor regression checks pass.

## Assets

Client import creates a content-addressed SHA-256 asset ID. Static images normalize to PNG; GIF stays animated. The importing client uploads the asset to server storage; other clients request missing assets and cache them locally.

Asset filenames are not network identity.

## Power

`ProjectionPower` is the only authority for effective capacity, per-component cost, overdrive and dynamic feasible slider limits. Rendering code must not invent parallel power limits.

## Projector upgrades

`ProjectorStateTransfer` is the canonical state transfer bridge. `ProjectorUpgradeRecipe` and `ProjectorUpgradePath` use it for Compact → Display → specialist crafting.

## Compatibility-only registry layer

The old five Improved Core block IDs are retained as legacy blocks because deleting registry IDs would damage old QA saves. Their BlockEntity schedules conversion into a materialized Core Booster. They must not regain recipes, BlockItems or Creative exposure.

## Mixins

The mixin package root is `celerbi.mirageprojector.mixin`.

Common/server-capable logic:

- Crying Obsidian random-tick/light interception.

Client-only mixins:

- Humanoid model pose/render support;
- vanilla Humanoid armor RenderType support;
- LivingEntity render integration;
- held-item render integration;
- Beacon renderer interception and cumulative loaded-Core-Booster relay state through `BeaconRelayState`.

Mixin changes are high-risk. A failed injection can prevent Minecraft from starting, so new mixins require exact mapped-target verification and isolated QA.

## Refactor hotspots

Current largest maintainability hotspots remain:

- `MirageProjectorRenderer`;
- `MirageProjectorBlockEntity`;
- `ImageProjectorScreen`;
- `ProjectionPower`;
- `MirageProjectorScreen`;
- `EntityProjectorScreen`.

Do not combine their structural decomposition with new gameplay/render mechanics. Split one responsibility at a time after the current renderer behavior stabilizes.
