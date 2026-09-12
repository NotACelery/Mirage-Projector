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
- per-channel Entity equipment render visibility, stored separately from snapshots;
- migration fields required by older development saves.

### `ProjectionSettings`

Owns shared presentation and Image-facing state. Wire/NBT input is sanitized before use.

### `EntityProjectionState`

Owns frozen Entity identity/render state and pose/equipment projection state independently from a live source entity. dev.71 keeps equipment visibility as presentation metadata inside this state while `VirtualEquipmentSnapshots` remains the authoritative snapshot store.

### `CoreBoosterBlockEntity`

Owns the loaded Core Booster material for item persistence/migration while `CoreBoosterBlock.MATERIAL` is the placed-block visual authority.

### Mirage Light Engine

The authoritative light subsystem lives under `celerbi.mirageprojector.light.engine` and is feature-independent. `MirageLightSource` describes an emitter; `MirageLightProfile` describes conceptual power, fixed-point precision, open-air cost, obstacle-detour extra cost, radius, shape/direction and reserved RGB metadata; `MirageLightSolver` computes a causally connected field; `MirageLightSection` stores per-source energy by 16³ section; `MirageLightWorld` owns fields and aggregate max light.

The solver walks six adjacent voxels. `MirageLightOcclusion` delegates destination opacity and face-shape blocking to vanilla `LightEngine.getLightBlockInto(...)`. dev.75d adds weighted detour semantics: monotonic open travel keeps the profile's ordinary cost, while steps that prove the route had to overshoot/backtrack because of geometry receive `detourExtraCostUnits`. This creates gradual shadows without a hard mode switch behind walls.

Final Mirage values merge with vanilla at read time and are never fed back into vanilla block-light propagation. `crying_light_node` is migration-only. Since dev.76/protocol 22, `STATIC_WORLD` is solved only on the server and synchronized as final packed 16×16×16 light sections to watched clients. Clients do not run the static solver. `DYNAMIC_VISUAL` is deliberately separate for future moving/portable emitters.

`STATIC_WORLD` is implemented for Mature Clusters. `DYNAMIC_VISUAL`, directional/frustum/plane shapes and RGB rendering are the dev.76+ boundary; moving visual sources must not turn into per-frame server gameplay-light rebuilds. See `MIRAGE-LIGHT-ENGINE.md`.

## Renderer families

Do not collapse all source types into one quad renderer.

- Image/Banner are plane/cardinal-face geometry.
- Item is a real item/block model.
- Entity is a reconstructed render-only client entity plus visibility-filtered equipment layers.
- Core/Booster centers use real item models.

`MirageProjectorRenderer` is still the largest rendering coordinator. It delegates/supports source-specific helpers but remains a future refactor hotspot. `EntityProjectionBounds` is the shared conservative Entity envelope authority for preview/clearance/world culling. The BER render AABB must include both the chassis and displaced projection/nameplate; dev.72 therefore allows normal vanilla frustum culling instead of forcing off-screen submission.

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
