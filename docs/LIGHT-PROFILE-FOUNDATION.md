# Mirage Projector — light architecture authority (dev.74)

## Current authority split

Two lighting implementations coexist intentionally in dev.74:

1. **Legacy visible backend** — dev.73 `mirage_projector:crying_light_node` blocks still provide actual Minecraft block light so the foundation can be tested without blacking out worlds.
2. **Mirage Light Engine shadow backend** — `celerbi.mirageprojector.light.engine` computes the future authoritative field without placing emitters or modifying vanilla light queries.

The second architecture is the forward contract. The physical relay strategy is compatibility/testing scaffolding scheduled for removal from active Mature lighting in dev.75.

## Why physical relays are being retired

A physical relay can be correctly placed on a causal source path and still become wrong immediately afterward: vanilla treats it as a new omnidirectional light source. It can then radiate sideways and around geometry independently of the Mature Cluster that created it.

Live QA exposed three consequences:

- cross-shaped over-illumination around the six relay axes;
- strong light persisting behind walls despite source occlusion checks;
- unreliable full-range `15,15,14,14,...,1,1` behavior because relay emission and vanilla falloff compete.

The new rule is therefore:

> Mirage owns the solved light field. A solved voxel is a final Mirage contribution, not a new emitter for vanilla to propagate again.

## New source/profile contract

`MirageLightSource` describes a source independently of the feature that created it. Current/future source consumers include Mature Crying Obsidian, stationary projectors, portable projectors and projected-display effects.

`MirageLightProfile` carries:

- conceptual source power;
- fixed-point substeps per visible light level;
- air traversal cost;
- maximum radius;
- decay mode;
- geometric shape;
- direction/cone parameters;
- RGB metadata;
- static-world versus future dynamic-visual runtime intent through the source.

Only `OMNIDIRECTIONAL` with `VANILLA`/`EXTEND`-style scalar propagation is runtime-solved in dev.74. Directional cone, rectangular frustum and plane shapes are reserved in the contract but must not claim runtime support yet.

## Fixed-point decay

The solver does not propagate only integer light levels. It propagates internal energy units and converts the final result to 0–15.

For Mature half-decay:

```text
substeps per visible level = 2
air edge cost              = 1 internal unit
```

Conceptual 15 starts at internal energy 31. After crossing successive air edges, the visible sequence is exactly:

```text
15 15 14 14 13 13 12 12 11 11 10 10 9 9 8 8 7 7 6 6 5 5 4 4 3 3 2 2 1 1
```

Conceptual power above 15 extends the saturated 15 plateau instead of creating illegal vanilla light levels above 15.

## Causal voxel propagation

`MirageLightSolver` walks adjacent cardinal voxels only. Every accepted voxel therefore has a predecessor path back to its source.

This gives the required geometry behavior:

- a fully separating barrier disconnects downstream voxels;
- a finite wall may still be routed around through a real edge;
- that detour costs additional path length and therefore produces weaker light;
- there is no source-to-target teleport and no secondary emitter that can refill the hidden region afterward.

Per-edge obstruction delegates to vanilla `LightEngine.getLightBlockInto(...)` so slabs, stairs, face occlusion and compatible modded states follow Minecraft's own light-blocking classification instead of a Mirage-maintained block whitelist.

## Section storage and overlap

Each solved source stores fixed-point energy sparsely by 16×16×16 sections. Current fixed-point energy fits in one unsigned byte per allocated voxel slot.

`MirageLightWorld` keeps source contributions separate and materializes a scalar aggregate max layer:

```text
aggregate(pos) = max(sourceA(pos), sourceB(pos), ...)
```

Removing one source therefore reveals the strongest surviving contribution instead of requiring blind node deletion.

The aggregate layer stores final Mirage values; it must never be fed back into vanilla as a set of new emission points.

## Performance contract

Whole-source rebuild is acceptable for the foundation because it is event-driven, not per-frame/per-tick continuous work.

The solver uses:

- packed long positions;
- fastutil primitive maps;
- energy-bucket FIFO queues;
- sparse section allocation;
- source equality caching to skip unchanged periodic refreshes.

Terrain changes remain coalesced at the end of the server tick. Before expanding to many mobile sources, profile solve time and field counts must be measured in-game.

## Static versus dynamic future backends

`STATIC_WORLD` is intended for persistent sources whose light may matter to both visual and gameplay queries.

`DYNAMIC_VISUAL` is reserved for moving/animated sources such as portable projectors. These must not rebuild server block-light state every render tick. They will share source/profile concepts while using a client-oriented dynamic backend.

## RGB reservation

Profiles already carry 24-bit RGB metadata. dev.74 still aggregates scalar brightness only. This reservation prevents the solver/source contract from becoming monochrome-only before projected maps, holographic tint or Crying-purple lighting are explored.

A future visual backend may preserve RGB while gameplay compatibility collapses the source to ordinary 0–15 luminance.

## dev.74 diagnostic boundary

The shadow layer is inspectable through:

- `/miragelight stats`
- `/miragelight probe`
- `/miragelight axis <direction>`
- `/miragelight rebuild`

Simple Light Level still displays the legacy physical backend in dev.74. Use the commands to judge the new solver until dev.75 performs the backend handoff.

## dev.75 requirements before authority handoff

Before physical Mature relays are retired, dev.75 must define and test:

- client synchronization/reconstruction of static virtual fields;
- render/light-query integration that uses final Mirage contribution without making it a vanilla emission source;
- server gameplay-light query policy;
- automatic chunk-load invalidation;
- legacy `crying_light_node` cleanup/migration;
- exact half-decay, wall, corner-wrap, overlap and Booster QA;
- performance with several overlapping Mature sources.

## Reserved advanced profiles

`CONCENTRATE`, `DIRECTIONAL_SPOT` and `ROTATING_DIRECTIONAL_SPOT` remain future contracts. The new solver architecture is specifically intended to make these possible without physical relay lattices. They are not runtime promises in dev.74.
