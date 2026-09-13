# Mirage Projector dev.75d — obstacle detour penalty and pre-dev.76 consolidation

Status: source candidate. This snapshot completes the current static Mature-light design pass and performs a full documentation/backlog consolidation before dev.76 dynamic/mobile-light work.

## Live-QA motivation

dev.75c fixed real wall traversal: Mirage light could no longer cross opaque iron. QA then showed a subtler effect. Because open Mature propagation intentionally decays at half vanilla speed, a route that climbed over a wall, crossed it and descended also paid only half-decay. At radius 30 this made wall wrapping mathematically correct but visually much more generous than ordinary Minecraft shadows.

The chosen behavior is **not** an abrupt switch to vanilla decay for an entire region behind a wall. Instead, only path distance that exists because geometry forced a detour receives additional cost.

## Profile change

`MirageLightProfile` adds:

```text
detourExtraCostUnits
```

Current Mature half-decay profile:

```text
substepsPerLightLevel = 2
airStepCostUnits      = 1
detourExtraCostUnits  = 1
```

So direct/open travel remains 1/2 visible level per block. Extra path caused by obstacle routing costs 1 full visible level per extra block overall.

`MirageLightProfile.vanilla(...)` uses `detourExtraCostUnits=0`, because a vanilla profile already charges a full visible level for every real travelled step and needs no second penalty.

## Solver implementation

For endpoint path length `P` and direct Manhattan distance `D`:

```text
detour = P - D
cost   = D*air + detour*(air + detourExtra)
```

The solver does not store `P` for every candidate. In a cardinal graph, a step toward the source reduces Manhattan distance by one while actual path length still grows by one. That one inward step therefore increases `P-D` by two and is charged:

```text
normal edge cost + 2*detourExtraCostUnits
```

Open optimal routes contain no inward/backtracking edge, so their sequence is exactly unchanged.

## Regression model

No-Booster open axis remains:

```text
15 15 14 14 13 13 12 12 11 11 10 10 9 9 8 8 7 7 6 6 5 5 4 4 3 3 2 2 1 1
```

A 2-D regression fixture uses source `(0,0)`, probe `(6,0)` and a three-cell-tall wall at x=3. The open path is 6 steps and reads level 13. The finite wall forces an 8-step route (2 detour steps); dev.75d weighted cost is 10 fixed units and the probe reads level 11. A monotonic side probe unaffected by the wall keeps its open-space result.

The fixture verifies semantics, not an exact promise for every 3-D wall; real values depend on alternate routes, partial opacity, source height and overlapping sources.

## Network change

Source descriptors must carry `detourExtraCostUnits` so server and client solve exactly the same weighted graph. Network protocol therefore intentionally changes:

```text
20 -> 21
```

No projector NBT schema changes are introduced.

## Debug change

`/miragelight probe` now also reports the nearest field's weighted cost decomposition:

```text
cost=<weighted> (direct=<minimum direct cost>, extra=<detour/opacity extra>)
```

This helps QA distinguish open-distance decay from obstacle/opacity loss.

## Accumulated dev.74–75d static-light closure

The current authoritative line now includes:

- dev.74 feature-independent source/profile/solver/section architecture;
- exact fixed-point `15,15 ... 1,1` half-decay;
- causal six-neighbour propagation;
- dev.75a read-time authority and source-descriptor synchronization;
- retirement of runtime physical Light Nodes;
- dev.75b server/client chunk lifecycle, tracking-scoped delivery, render-section dirtying and orphan legacy-node migration;
- terrain invalidation for place/break/fluid/growth/piston/explosion plus Booster changes;
- dev.75c real destination opacity + vanilla face-shape handoff;
- dev.75d obstacle-only detour penalty;
- overlap max aggregation and source-specific removal/downgrade;
- debug commands and solver regression scripts.

## What dev.75d deliberately does not implement

- dynamic/moving light;
- directional/frustum/plane runtime shapes;
- RGB-colored world rendering;
- mounted/composite Entity scans;
- generic accessory APIs;
- new partial/incremental light-solver invalidation;
- broad low-level compatibility hooks for mods that intentionally bypass normal brightness APIs.

These remain separate future work, primarily beginning with dev.76.
