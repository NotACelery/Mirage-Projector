# dev.74 — Mirage Light Engine foundation

Version: **0.1.0-dev.74**  
Network protocol: **19**  
Runtime status: **shadow/foundation only**

## Why this exists

Live Simple Light Level QA on dev.70–73 proved that the physical `crying_light_node` relay strategy cannot become the long-term Mirage lighting backend.

Even when a relay is created only on a valid path from the Mature Cluster, Minecraft subsequently treats that relay as a new omnidirectional block-light emitter. The relay therefore loses the causal relationship to the original source and can spread sideways, fill a cross-shaped area, reach behind walls through secondary propagation and distort the intended `15,15,14,14,...,1,1` half-decay curve.

The design correction is fundamental:

> Mirage computes a light field. It does not manufacture secondary block emitters and ask vanilla to approximate that field.

The Mature Crying Obsidian Cluster is the first consumer of the new subsystem, not its owner. Future consumers can include portable projectors, moving holograms, directional lights and projected-map/display effects.

## dev.74 safety boundary

dev.74 deliberately does **not** replace visible/gameplay block light yet.

- dev.73 physical `crying_light_node` relays remain the authoritative world-light backend;
- the new Mirage solver runs in parallel on the server as a shadow field;
- no mixin changes `LightEngine`, `LevelLightEngine`, `BlockAndTintGetter` or renderer brightness queries in this snapshot;
- the shadow field can be inspected with `/miragelight` diagnostics;
- dev.75 is the planned backend handoff after the solver itself passes QA.

This boundary makes it possible to validate math, occlusion, storage and performance without risking a snapshot that simply leaves the world dark.

## New subsystem boundary

New package:

```text
celerbi.mirageprojector.light.engine
```

Main responsibilities:

### `MirageLightSource`

Immutable description of one source:

- stable source ID;
- world origin;
- solver profile;
- runtime intent (`STATIC_WORLD` or future `DYNAMIC_VISUAL`).

The current Mature Cluster source ID is derived from its block position and the `mature_crying_cluster` source kind.

### `MirageLightProfile`

Solver-facing propagation contract:

- conceptual light power;
- fixed-point substeps per visible light level;
- air-edge cost;
- maximum propagation radius;
- decay mode;
- geometric shape;
- direction/cone data reserved for directional profiles;
- RGB metadata reserved for a future colored visual backend.

Current factory profiles include vanilla-rate scalar propagation and generic extended-rate propagation. The Mature Cluster uses `halfDecayExtended(..., 2 substeps)`.

### `MirageLightSolver`

Calculates one source contribution with causal voxel propagation.

The solver:

- walks only the six adjacent cardinal neighbours;
- never teleports from source to a distant relay;
- requires every accepted voxel to have a real predecessor path back to the source;
- uses Manhattan/grid path length, matching the fundamental topology of Minecraft block-light propagation;
- can route around a finite obstacle only by paying the extra path length;
- cannot cross a completely light-occluding edge;
- does not place blocks or notify the vanilla light engine.

### `MirageLightOcclusion`

Translates vanilla per-edge light occlusion into Mirage fixed-point traversal cost.

Rather than hard-code slabs, stairs or individual modded blocks, the foundation delegates face-shape/opacity classification to `LightEngine.getLightBlockInto(...)`. A fully blocked vanilla edge is rejected. Partial opacity adds extra fixed-point loss.

### `MirageLightSection`

Stores one source's solved fixed-point energy in a 16×16×16 section-local byte array.

Fixed-point energy currently requires more than four bits, so per-source solved energy remains one unsigned byte per voxel. Empty sections are never allocated.

### `MirageLightWorld`

Owns per-Level source fields and the aggregate virtual block-light layer.

- source contributions remain separate so removing one source does not erase another;
- aggregate visible light is the maximum contribution at each voxel;
- aggregate lookups are section-local O(1);
- aggregate sections reference solved source sections instead of duplicating a second 4096-byte visible array for every source/section pair;
- Level state is released on world unload.

The aggregate layer is scalar 0–15 in dev.74. RGB remains source/profile metadata and does not yet alter Minecraft rendering.

## Exact fixed-point half-decay

The Mature Cluster no-Booster profile uses two internal energy units per one visible Minecraft light level.

For conceptual power 15:

```text
source internal energy = 31

distance:  1  2  3  4  5  6 ... 27 28 29 30
energy:    30 29 28 27 26 25 ...  4  3  2  1
visible:   15 15 14 14 13 13 ...  2  2  1  1
```

Visible conversion is effectively:

```text
ceil(energy / substepsPerLightLevel)
```

and is always clamped to vanilla-compatible `0..15`.

Conceptual Booster power above 15 therefore does not create illegal light levels. It extends the saturated 15 plateau before the same paired falloff continues.

At conceptual power 19 the pure model is:

```text
15 15 15 15 15 15 15 15 15 15
14 14 13 13 12 12 11 11 10 10
 9  9  8  8  7  7  6  6  5  5
 4  4  3  3  2  2  1  1
```

## Obstacle semantics

The new solver intentionally does not mean strict camera-style line of sight.

A complete separating barrier can disconnect the downstream field. A finite wall can still receive light around a real edge, but the route is longer and therefore arrives weaker. This preserves the behavior the user originally requested: geometry matters, opposite/hidden regions are not fed by teleporting emitters, but natural Minecraft-style corner wrapping remains possible.

This is different from dev.73 physical relays, where a valid upstream relay could become an independent omnidirectional source and refill an unrelated region.

## Booster integration in dev.74

The Mature shadow source derives its conceptual scalar power from the already-defined reflected-light identities:

- Quartz / Radiance remains the dominant static power extension;
- Diamond / Focus contributes a smaller axial/scalar power bonus under the current compatibility bridge;
- Glass / Diffusion does not create static side sources;
- Amethyst / Resonance remains dynamic optical behavior;
- Netherite / Inversion remains dynamic optical behavior.

The solver foundation itself does not hard-code Core Booster materials. It receives an already-resolved `MirageLightProfile`, allowing future light sources to use unrelated profiles.

Changing a Core Booster material explicitly asks nearby Mature sources to rebuild immediately, because the blockstate change does not necessarily pass through the normal player placement invalidation path.

## Terrain invalidation

The dev.70 end-of-server-tick block-change coalescing remains in place.

When terrain changes near an active Mature source:

1. the final block states are allowed to commit;
2. each affected source is refreshed at most once for that batch;
3. dev.74 forces a new shadow solve against current geometry;
4. the legacy physical field is still refreshed as before for dev.74 compatibility.

Periodic Mature refreshes do not continuously re-solve an unchanged shadow source; source equality allows the cached field to remain in place.

Loaded-chunk boundaries are intentionally conservative in the solver: unloaded neighbours are never force-loaded. A manual `/miragelight rebuild` is available during dev.74 QA after a relevant chunk becomes available. Automatic chunk-load invalidation is a dev.75 backend prerequisite before the virtual layer becomes authoritative.

## Performance design

A radius-30 Manhattan ball contains roughly 38k candidate voxels; radius 38 is roughly 76k. The foundation therefore avoids an object-heavy `HashMap<BlockPos,...> + PriorityQueue<Node>` design.

The current solver uses:

- packed `BlockPos` longs;
- fastutil `Long2ByteOpenHashMap` for best fixed-point energy;
- one FIFO bucket per possible remaining energy value;
- no Java queue-node object allocation per traversed voxel;
- no `PriorityQueue` `O(log N)` churn;
- sparse 16³ section allocation for solved storage.

This is still intentionally a whole-source rebuild model. dev.74 measures real solve cost before introducing more complex partial invalidation.

## Debug commands

The commands inspect the **new virtual shadow field**, not the dev.73 physical relay result.

```text
/miragelight stats
```

Reports solved source count, allocated aggregate section count and aggregate lit-voxel count.

```text
/miragelight probe
```

Reports at the player's block position:

- aggregate virtual Mirage light;
- nearest source contribution;
- nearest source position;
- current vanilla block light.

The two values are expected to disagree in dev.74 because the physical dev.73 backend still controls actual Minecraft lighting.

```text
/miragelight axis east
/miragelight axis west
/miragelight axis north
/miragelight axis south
/miragelight axis up
/miragelight axis down
```

Prints the nearest source's virtual contribution along that axis and solve statistics.

```text
/miragelight rebuild
```

Forces only the nearest source's **shadow field** to solve again against current loaded geometry. It does not place/remove legacy nodes and is mainly useful after chunk loading during foundation QA.

## Pure-model verification

Two repository tools cover solver invariants without needing a running Minecraft client:

- `tools/verify_mirage_light_curve.py`
- `tools/verify_mirage_light_solver_model.py`

The second verifies:

- exact `15,15 ... 1,1` open-air decay;
- complete causal barrier separation;
- weaker routing around a finite obstacle;
- max aggregation semantics for overlapping sources.

These tests do not replace Windows/NeoForge compilation or in-game shape/opacity QA.

## Explicitly not implemented in dev.74

- no virtual-light injection into vanilla/client light queries;
- no removal of `crying_light_node` from active runtime;
- no client synchronization of solved static fields;
- no `DYNAMIC_VISUAL` mobile-source backend;
- no RGB/color-light renderer;
- no directional/cone/frustum/plane solver path yet;
- no partial/region-only source rebuild algorithm;
- no automatic chunk-load field invalidation yet.

Those boundaries are intentional. dev.74 exists to make the solver/storage contract testable before it becomes authoritative.

## dev.75 handoff target

Once `/miragelight` confirms the virtual field handles the user's numbered-floor and wall tests correctly, dev.75 should:

1. provide the client/server virtual-light backend needed for authoritative static Cluster light;
2. remove Mature runtime dependence on physical `crying_light_node` relays;
3. clean/migrate legacy relay blocks safely;
4. integrate virtual aggregate results without feeding them back into vanilla propagation as new emitters;
5. add automatic chunk-load invalidation/synchronization;
6. rerun exact `15→1`, wall, overlap, Booster and performance QA before any mobile source work begins.

## Snapshot static QA

Before packaging the dev.74 source candidate:

- both pure-model Mirage Light verification tools pass;
- 103 JSON resources parse successfully;
- `en_us`, `es_es` and `es_cl` contain 439 matching translation keys each;
- public Java top-level type names match their filenames;
- active source contains no TODO/FIXME/HACK markers;
- delimiter/syntax sanity found no unbalanced Java braces/brackets/parentheses;
- no `.gradle`, `.gradle-dist`, `build`, `run`, `.class`, `javac*.args` or Python cache artifacts are packaged.

This environment cannot resolve/download the Gradle distribution and NeoForge dependency graph, so dev.74 remains a **source candidate**, not a build-clean snapshot, until the Windows Java 21 build is run.
