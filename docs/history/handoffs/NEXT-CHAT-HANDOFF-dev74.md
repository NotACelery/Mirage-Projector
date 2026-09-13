# Mirage Projector — next-chat handoff after dev.74

Current source candidate: **0.1.0-dev.74 — Mirage Light Engine Foundation**  
Minecraft 1.21.1 / NeoForge 21.1.244 / Java 21  
Network protocol: **19**

## Critical context

Live Simple Light Level testing invalidated the physical auxiliary-emitter architecture used by dev.65–73. `crying_light_node` relays become independent omnidirectional vanilla sources after placement, producing cross-shaped fill, wall leakage and an unreliable full half-decay curve.

The accepted architectural correction is:

> Mirage computes and stores its own causal light field; it does not create secondary vanilla emitters to approximate that field.

## What dev.74 adds

New `celerbi.mirageprojector.light.engine` foundation:

- `MirageLightSource` + stable `MirageLightSourceId`;
- `MirageLightProfile` fixed-point propagation profile with RGB metadata;
- `MirageLightShape` contract (OMNIDIRECTIONAL runtime now; directional/frustum/plane reserved);
- `MirageLightRuntimeMode` (`STATIC_WORLD`, future `DYNAMIC_VISUAL`);
- `MirageLightSolver` six-neighbour causal flood using bucketed fixed-point energy;
- `MirageLightOcclusion` delegating edge blocking/opacity to vanilla `LightEngine.getLightBlockInto`;
- sparse `MirageLightSection` 16³ per-source storage;
- `MirageLightWorld` per-Level source ownership and max aggregate layer;
- Level-unload cache cleanup;
- `/miragelight stats|probe|axis <dir>|rebuild` diagnostics.

The Mature Cluster registers a **parallel shadow source** using the existing reflected Quartz/Diamond scalar power bridge. Terrain edits force the shadow field to rebuild in the same dev.70 coalesced update pass. Core Booster material swaps explicitly trigger nearby source refresh.

## Very important dev.74 boundary

**Visible/gameplay light is still dev.73 physical relay light.**

Do not judge the new solver by Simple Light Level alone in dev.74. Use `/miragelight` to inspect the virtual field. The old field deliberately remains active so this foundation cannot black out worlds while the new backend is unproven.

No `LightEngine#getLightValue`/render query mixin is part of dev.74.

## Fixed-point contract

No-Booster Mature profile:

```text
distance 1..30
15 15 14 14 13 13 12 12 11 11 10 10 9 9 8 8 7 7 6 6 5 5 4 4 3 3 2 2 1 1
```

Conceptual 19 extends the saturated 15 plateau and then uses the same paired tail; real visible values stay capped to 15.

Pure-model verification scripts pass this contract plus barrier/corner/overlap invariants.

## QA to run on dev.74

1. Windows Java 21 `build.bat`.
2. Place an energized Mature Cluster, wait for its normal refresh, run `/miragelight axis east` (and other directions). In open air the shadow sequence must be exact.
3. Stand at numbered floor cells and use `/miragelight probe`; compare `aggregate/nearest` to the old `vanilla` result. Disagreement is expected until dev.75.
4. Build a broad/tall wall, let dev.70 terrain invalidation rebuild, then probe the hidden side. A sealed/full separating barrier must not receive a disconnected virtual contribution. A finite wall may receive weaker light by a real longer route around its edge.
5. Use `/miragelight rebuild` after loading nearby chunks if the test crosses a previously unloaded boundary.
6. Test zero/four Quartz and Diamond combinations; confirm conceptual extra power extends the virtual tail without values >15.
7. Place several Mature sources and run `/miragelight stats`; record solve time from `/miragelight axis`/`rebuild` and watch server hitching.

## Known intentional limitations

- `crying_light_node` remains active until dev.75;
- virtual field currently exists server-side only;
- no automatic chunk-load shadow rebuild; `/miragelight rebuild` exists for QA;
- only OMNIDIRECTIONAL VANILLA/EXTEND-style profiles are solver-enabled;
- RGB is metadata only;
- no moving/DYNAMIC_VISUAL lights yet.

## Next implementation

**dev.75 — Cluster Virtual Light Backend**

Priority order:

1. make virtual static field authoritative without turning virtual voxels into vanilla emissions;
2. client synchronization/render-light query integration;
3. server gameplay light-query policy;
4. automatic chunk-load invalidation;
5. legacy `crying_light_node` migration/cleanup;
6. exact numbered-floor + wall + overlap QA;
7. only after stability, proceed to dev.76 dynamic/mobile sources.

Do not resurrect source-to-target LOS relays or six-axis physical emitters as the primary design. The whole point of dev.74 is preserving causal voxel connectivity and final-field ownership.

## Packaging QA

Static checks before packaging: both light model tools PASS; 103 JSON valid; 439 identical keys in `en_us`/`es_es`/`es_cl`; no active TODO/FIXME/HACK; no generated/cache directories or class files. Full NeoForge compilation was not available in the packaging environment, so Windows Java 21 build remains required.
