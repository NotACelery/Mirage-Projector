# NEXT CHAT HANDOFF — 0.1.0-dev.75f

## Trigger
Live dev.75d QA found two static Mirage Light lifecycle bugs:

1. breaking a Mature Cluster inherited from older development worlds could leave an immortal virtual field;
2. after leaving/re-entering a world, illumination could stop exactly at chunk borders (reproduced toward SE/SW) until a manual block update inside that chunk forced another solve.

## Root cause
`ChunkEvent.Load` can fire before the loaded chunk is actually queryable through the live `Level`. The Mirage solver gates traversal with `level.hasChunkAt(nextPos)`. Older lifecycle code could consume the one chunk-load event before that predicate became true; the field was solved with the chunk treated as unloaded and no later event remained to repair it. Load order made the defect directional/repeatable.

## dev.75f fix
### Server
- `PENDING_LOADED_CHUNKS` keeps each load event until `level.getChunkSource().getChunkNow(x,z) != null`.
- Unavailable load entries are never discarded and are retried on later post-ticks.
- Ready chunks are discovered/legacy-cleaned in a batch, then intersecting Mature sources are force-resolved.
- An unload removes any unresolved load entry for that chunk.

### Client
- Separate `PENDING_LOAD_CHUNKS` and `PENDING_UNLOAD_CHUNKS`.
- LOAD remains pending until `ClientLevel.hasChunkAt()` succeeds for that chunk, matching the solver readiness predicate.
- UNLOAD cancels an unresolved LOAD and triggers rebuild immediately.
- Source UPSERT still requests one short delayed full stabilization pass; subsequent real chunk arrivals continue to rebuild normally.

### Stale-source / old-world hardening
- `removeSourceNow()` now forces an origin-authoritative REMOVE packet even if the canonical server descriptor was already missing.
- `MirageLightWorld.removeSourcesAtOrigin()` clears every descriptor anchored at the same physical source position, including migration-era ids/kinds.
- Client REMOVE is also origin-authoritative.
- Server and client audit loaded Mature source origins once per second and prune fields if the energized Mature Cluster no longer exists.

## Visual comparison line
The dev.75e six `*_alt` projector models remain accumulated in this source snapshot for side-by-side model/inventory/hand QA.

## Required Windows/in-game QA
1. Build Java 21.
2. Put an energized Mature Cluster close enough that its 30-block field crosses several chunk borders.
3. Toggle F3+G and confirm all quadrants cross borders symmetrically.
4. Save/quit/re-enter repeatedly; no border may remain clipped after its chunk is actually loaded.
5. Move across view-distance boundaries and back.
6. Break the Mature Cluster; all Mirage light must disappear without requiring relog/block update.
7. Repeat with an old-world cluster and with Quartz/Diamond Booster range.
8. Confirm no legacy `crying_light_node` remains after the relevant chunks load.

## Build status
Source snapshot only; this environment has no usable Gradle installation/wrapper jar, so Windows Java 21 build is still the acceptance gate.
