# dev.76 STATIC_WORLD authority update

`STATIC_WORLD` no longer synchronizes source descriptors for client-side re-solving. The server is the only geometry authority. It runs the existing causal fixed-point solver, aggregates overlapping sources by maximum visible Mirage level, and synchronizes final 16×16×16 section values to clients already watching the corresponding vanilla chunk. Levels are nibble-packed (4 bits/voxel, 2048 bytes/full section). Clients mirror the section and answer effective block light as `max(vanilla, Mirage)`.

This intentionally does **not** inject Mirage values into vanilla `BlockLightEngine` propagation, so virtual values cannot become recursive secondary emitters. Client section install/removal publishes `onLightUpdate(BLOCK, section)` for render/cache consumers.

`DYNAMIC_VISUAL` remains a distinct future backend for lanterns, handheld projectors and other moving emitters; it must not rebuild/synchronize static section voxels every frame. Network protocol: **22**.

---

# Mirage Projector — Mirage Light Engine authority

Current line: **0.1.0-dev.75d**. Network protocol: **21**.

This document is the focused authority for Mirage-owned world/dynamic lighting. Historical physical-relay documents remain useful archaeology, but they do not override this contract.

## 1. Design invariant

Mirage computes a **final virtual light field**. It does not manufacture a lattice of vanilla light-emitting blocks and does not inject solved Mirage voxels back into `BlockLightEngine` as new emissions.

Effective scalar block light is read as:

```text
effectiveBlockLight = max(vanillaBlockLight, mirageBlockLight)
```

This preserves vanilla light as an independent lower layer while preventing Mirage cells from recursively becoming omnidirectional secondary sources.

`mirage_projector:crying_light_node` remains registered only so old development worlds can load and migrate. Current runtime never creates it.

## 2. Major runtime pieces

### `MirageLightSource`

Immutable feature-independent emitter descriptor:

- stable `MirageLightSourceId`;
- world origin;
- `MirageLightProfile`;
- runtime mode (`STATIC_WORLD` today, `DYNAMIC_VISUAL` reserved).

The first active producer is an energized Mature Crying Obsidian Cluster. Future stationary/portable projectors and projected-map/display effects must reuse this source contract rather than create their own physical light blocks.

### `MirageLightProfile`

Current fields:

- `conceptualLight`: source energy tier; may exceed visible vanilla 15;
- `substepsPerLightLevel`: fixed-point precision;
- `airStepCostUnits`: ordinary open-space traversal cost;
- `detourExtraCostUnits`: additional cost for path length that exists only because geometry forced a detour;
- `maxRadius`;
- decay mode;
- shape;
- direction / cone angle;
- reserved RGB metadata.

The profile is synchronized as part of the source descriptor. dev.75d adds `detourExtraCostUnits`, so protocol **21** is intentionally incompatible with dev.75c/protocol-20 peers.

### `MirageLightSolver`

A causal six-neighbour weighted flood solver. Every solved voxel has a cardinal predecessor chain to the source. It uses packed `long` positions, `Long2ByteOpenHashMap` for best fixed-point energy and energy buckets (`LongArrayFIFOQueue[]`) rather than one heap/node object per voxel.

### `MirageLightOcclusion`

Edge transmission delegates vanilla face/opacity semantics to `LightEngine.getLightBlockInto(...)`. dev.75c fixed the destination-opacity contract: `toState.getLightBlock(level, toPos)` is supplied instead of the erroneous constant `1`; destinations with opacity >=15 are rejected directly.

This means a solid wall is not traversable. Light behind a finite wall must arrive through a real path over/under/around its edge.

### `MirageLightSection` / `MirageLightWorld`

Per-source fixed-point contributions are stored sparsely by 16³ section. `MirageLightWorld` keeps immutable solved source fields and a materialized aggregate visible max layer for O(1) reads. Removing one source reveals the strongest surviving contribution instead of blindly clearing a region.

## 3. Mature Cluster profile

An energized Mature Cluster is a baseline static world-light source even with zero Core Boosters.

Current no-Booster profile:

```text
conceptualLight       = 15
substepsPerLightLevel = 2
airStepCostUnits      = 1
detourExtraCostUnits  = 1
maxRadius             = 30
shape                 = OMNIDIRECTIONAL
runtime                = STATIC_WORLD
RGB metadata          = 0xA84CFF (reserved for future colored visual backend)
```

Open-space distances 1–30 are therefore exactly:

```text
15 15 14 14 13 13 12 12 11 11
10 10  9  9  8  8  7  7  6  6
 5  5  4  4  3  3  2  2  1  1
```

Visible scalar output remains capped to 15. Conceptual power 16–19 extends the saturated 15 plateau and total tail instead of inventing illegal vanilla levels 16–19.

## 4. dev.75d obstacle-detour decay

The Cluster does **not** switch an entire shadow region abruptly into a separate vanilla mode after one collision. Instead, each candidate voxel receives the best weighted causal route from the source.

For an endpoint:

```text
directDistance = Manhattan distance(source, endpoint)
pathLength     = cardinal steps in the chosen real path
detour         = pathLength - directDistance

weightedCost = directDistance * airStepCostUnits
             + detour * (airStepCostUnits + detourExtraCostUnits)
```

For the current half-decay profile (`air=1`, `detourExtra=1`):

```text
ordinary/direct step  -> 1 internal unit = 1/2 visible light level
obstacle-only extra step -> 2 internal units = 1 full visible light level
```

The implementation does not need to store complete paths. In a cardinal grid, one step either increases or decreases Manhattan distance by one. A monotonic open-space path only moves outward. If geometry forces an overshoot, the later inward/backtracking edge reduces Manhattan distance while path length still increases; that edge represents two accumulated detour steps, so the solver adds `2 * detourExtraCostUnits` there.

Consequences:

- open-space half-decay is bit-for-bit unchanged;
- touching a wall does not create a hard lighting seam;
- a finite wall may be wrapped around naturally;
- deeper shadow pockets become progressively darker because reaching them requires more detour;
- a completely separating barrier still disconnects the source entirely;
- obstacle cost is profile data, so future light types may choose different shadow behavior without changing the solver architecture.

Example model used by the dev.75d verifier:

```text
source=(0,0), probe=(6,0)
open shortest path = 6 steps -> visible 13
3-high finite wall at x=3 forces 8-step route
2 extra detour steps receive stronger cost -> visible 11
```

This example is a regression model, not a promise that every 3-D wall arrangement produces exactly level 11; the actual value depends on source position, wall dimensions, partial opacity, alternate routes and competing vanilla/Mirage sources.

## 5. Occlusion and partial blocks

For every `from -> to` edge:

1. source/destination chunks must already be loaded;
2. destination build height/radius must be valid;
3. destination block-light opacity is resolved from the actual BlockState;
4. opacity >=15 blocks the edge;
5. vanilla face-shape occlusion decides whether the two states' facing surfaces block transfer;
6. partial opacity adds whole-visible-level fixed-point penalty;
7. dev.75d detour penalty is then applied only if that movement is part of geometric backtracking relative to the source.

Do not add slab/stair/mod-specific `instanceof` rules unless a concrete vanilla-helper incompatibility is proven. The default contract is to reuse Minecraft's own state/shape semantics.

## 6. Core Booster reflection into Mature light

The Beacon column is resolved first. At most four loaded Boosters contribute.

Static virtual-light power intentionally does not collapse all Booster materials into one generic width tier:

- **Quartz / Radiance**: +1 conceptual light tier per effective Quartz;
- **Diamond / Focus**: +1 conceptual tier per two effective Diamond (`ceil(diamond/2)` via `(tier+1)/2`);
- combined static boost is clamped to +4, so conceptual light is at most 19 and nominal radius at most 38;
- **Glass / Diffusion**: residual reflected-ray widening; no independent static side-light lattice;
- **Amethyst / Resonance**: residual excitation/rotation activity; no free static range;
- **Netherite / Inversion**: reverses reflected rotation; no free static range.

Quartz/Diamond reinforce the same causal solver and therefore inherit wall occlusion and dev.75d detour penalty automatically.

## 7. Source lifecycle

### Activation / refresh

A Mature Cluster registers when it is Mature **and** Beacon-energized. It owns no BlockEntity; chunk-load discovery finds the block state and schedules normal crystal reevaluation. The source ID is stable from its block position.

Core Booster material changes refresh nearby Mature sources so conceptual power updates without requiring the Cluster to be replaced.

### Terrain invalidation

Server terrain changes are coalesced and solved after the committed world state is available. Covered event families include:

- block place;
- multi-place;
- block break;
- fluid block placement;
- crop growth;
- feature/tree/fungus growth;
- piston movement;
- explosions;
- explicit Booster-driven source refresh.

An impacted source is rebuilt at most once for the coalesced tick batch.

### Removal / de-energization

Removing or de-energizing the source removes its virtual contribution immediately. Aggregate light is rebuilt from surviving sources; overlapping light is therefore downgraded rather than indiscriminately erased.

## 8. Chunk lifecycle

The solver never force-loads chunks.

Server:

- `ChunkEvent.Load` queues newly available geometry;
- work is coalesced to `LevelTickEvent.Post`;
- loaded chunks are scanned for Mature sources and legacy Mirage Light Nodes;
- sources touching newly available chunk geometry rebuild once;
- unloading the source-origin chunk unregisters that source;
- destination chunk unload does not destroy the source; arrival later triggers a new solve.

Client:

- chunk load/unload geometry changes are coalesced to client tick post;
- locally known source descriptors touching those chunks are re-solved;
- old/new affected render sections are dirtied so chunk lighting recompiles.

## 9. Network lifecycle

Protocol **21**.

Mirage synchronizes source descriptors, never solved voxel arrays. Client and server run the deterministic solver against their own loaded geometry.

Delivery is chunk-tracking scoped:

- a player receives a source while at least one watched chunk intersects its radius;
- `UnWatch`/source removal retract descriptors no longer needed;
- same-level respawn retains known watched chunks across CLEAR and reconciles immediately;
- dimension changes start a new level-scoped tracking state;
- logout removes tracking state.

The synchronized profile includes dev.75d's `detourExtraCostUnits`, which is why protocol moved 20 -> 21.

## 10. Read-time authority

Current bridges merge Mirage with vanilla without modifying vanilla propagation:

- `Level` block-light brightness;
- `LevelLightEngine#getRawBrightness` path;
- client `RenderChunkRegion` brightness used during chunk compilation.

A third-party consumer that intentionally reads raw vanilla block-light storage directly can still see vanilla-only values. Add a narrow compatibility bridge only for a confirmed consumer; never feed Mirage values into vanilla `BlockLightEngine` to make such a consumer happy.

## 11. Debug commands

### `/miragelight stats`

Reports authoritative source count, aggregate section count and lit virtual voxel count for the current server level.

### `/miragelight axis <north|south|east|west|up|down>`

Uses the nearest loaded Mirage source and prints its solved visible sequence along one cardinal axis plus solve time, voxel count, section count and blocked-edge count. In unobstructed no-Booster terrain it must show the exact 30-cell half-decay sequence.

### `/miragelight probe`

At the player's block position reports:

- aggregate Mirage block light;
- nearest-source contribution and source position;
- dev.75d weighted path cost for that nearest field, split into minimum direct cost and additional cost (`extra` includes detour and any partial-opacity contribution);
- raw vanilla block-light storage;
- effective merged value.

Use this to distinguish a solver problem from a rendering/consumer compatibility problem.

### `/miragelight rebuild`

Forces a solve of the nearest loaded server source and rebroadcasts its descriptor if rebuilt. It reports solved voxel/section count and solve milliseconds. This remains a diagnostic; normal chunk/terrain lifecycle should not require manual rebuilding.

## 12. Legacy-node migration

`mirage_projector:crying_light_node` has no forward runtime role.

- no current Java path creates its default block state;
- source-centered cleanup removes old axial/dev.69 diffuse positions around known sources;
- loaded chunk sections use palette prefiltering and only scan voxel-by-voxel if the legacy block may exist;
- only Mirage's own node is removed; `minecraft:light` and other mods' light blocks are untouched;
- legacy node scheduled ticks self-delete.

The registry/model can remain until old development-world compatibility is intentionally dropped.

## 13. Performance contract

Current deliberate choices:

- source solve occurs on relevant mutation/chunk changes, not on every light query;
- reads from the aggregate field are O(1);
- packed positions + primitive byte energy map;
- descending energy buckets instead of object `PriorityQueue` nodes;
- sparse 16³ source sections;
- aggregate section replacement operates over the union of old/new touched sections;
- source sync is tracking-scoped and descriptor-only;
- chunk/terrain changes are coalesced.

Before further solver optimization, profile real worlds with several overlapping radius-30/38 sources. Do not implement partial/incremental graph invalidation unless measured solve/tick cost justifies the complexity.

## 14. Boundary for dev.76+

Static Mature world light is now an authoritative consumer of the engine. dev.76 begins **dynamic/mobile light source foundation**, not another rewrite of the static solver.

Reserved work:

- `DYNAMIC_VISUAL` client-oriented moving sources;
- portable projectors / held or entity-attached emitters;
- directional cone / spotlight;
- rotating directional spotlight;
- rectangular frustum;
- plane/projected-surface emission for displays/maps;
- RGB-preserving visual light while scalar gameplay light can continue collapsing to 0–15;
- concrete third-party brightness compatibility bridges if real QA proves they are needed.

Dynamic visual light must not rebuild server gameplay-light fields every render frame. It should reuse source/profile semantics while using a backend appropriate to moving visual emitters.

## Atomic publication (dev.76c)

Server-authoritative does not mean partial authoritative. A `STATIC_WORLD` source is publishable only when its complete horizontal dependency window is queryable on the server. Missing dependencies place the source into a pending rebuild set. Pending sources are retried every tick without force-loading chunks. The solver also refuses to commit any candidate that reports `unloadedEdges > 0`, preserving the previous complete field until a full replacement is available.
