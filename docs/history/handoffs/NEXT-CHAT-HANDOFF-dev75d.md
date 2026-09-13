# NEXT CHAT HANDOFF — dev.75d -> dev.76

Baseline: **0.1.0-dev.75d source candidate**. Network protocol: **21**.

## What is now closed architecturally

The energized Mature Crying Obsidian Cluster uses the authoritative virtual Mirage Light Engine. Physical `crying_light_node` is migration-only. Current static engine includes exact fixed-point open half-decay, causal six-neighbour propagation, real vanilla opacity/face occlusion, obstacle-detour penalty, source overlap max aggregation, chunk lifecycle, tracking-scoped source sync, terrain invalidation, render-section dirtying, debug commands and legacy cleanup.

Current Mature no-Booster profile:

```text
conceptual=15
substeps=2
air=1
detourExtra=1
radius=30
```

Open: `15,15,14,14,...,1,1`.

A wall is never traversed directly. Finite walls may be routed around. Only route distance beyond the Manhattan minimum receives additional penalty; therefore there is no hard "switch to vanilla" boundary behind a wall.

Quartz adds static Radiance tier; Diamond adds smaller Focus tier; total conceptual static boost maxes at +4 (15 -> 19, nominal open radius 30 -> 38). Glass/Amethyst/Netherite remain reflected visual identities rather than free range.

## dev.75d QA gate before declaring build-clean

1. Windows Java 21 `build.bat`.
2. Open numbered floor: exact 15..1 pairs.
3. 1/2/3-high straight walls plus 3-high L wall: verify gradual detour shadows, no penetration.
4. Slab/stair/partial-opacity and at least one modded shape.
5. `/miragelight probe`: open cells extra=0 (unless opacity), hidden cells extra>0 where detour/opacity exists.
6. Quartz/Diamond with same wall suite.
7. Chunk borders/source unload/reload; relog, same-level respawn, dimension change.
8. Overlap and several-source performance.
9. Legacy dev.73/74 node cleanup.
10. Smoke dev.71 equipment visibility + dev.72 high-Lift/frustum/Create Backtank so light work did not regress renderer systems.

If any of these reveals a concrete static-light defect, fix it without reintroducing physical relays.

## dev.76 implementation target

**Dynamic/mobile Mirage light source foundation.** Start with one controlled moving test source, not a full portable projector feature.

Required design/implementation decisions:

- define `DYNAMIC_VISUAL` ownership and lifecycle;
- source transform updates at high frequency without rebuilding static server gameplay fields each frame;
- client update cadence/interpolation and distance/frustum culling;
- relationship between static scalar gameplay light and moving visual light;
- stopping/anchoring a moving source without stale light;
- profile reuse: conceptual power, open decay, detour policy, shape/direction and RGB metadata;
- first directional/shape primitive only if needed to prove the backend; do not bundle every advanced profile at once;
- preserve Mature `STATIC_WORLD` as an independent regression baseline.

Future consumers after foundation: portable/held projectors, entity-attached emitters, directional spots, projected maps/displays, plane/frustum illumination and RGB-preserving visual light.

## Documentation authority

Read in this order:

1. source/resources;
2. `CURRENT-IMPLEMENTATION.md`;
3. `MIRAGE-LIGHT-ENGINE.md`;
4. `DEV75D-STATIC-QA.md` for what is statically proven vs still in-game QA;
5. focused docs (`CRYING-OBSIDIAN.md`, `CORE-BOOSTER-AND-UPGRADES.md`, etc.);
6. `ROADMAP.md`;
7. this handoff for immediate continuation.

Older dev.65–75c notes are history/diagnosis, not permission to restore superseded physical relay behavior.
