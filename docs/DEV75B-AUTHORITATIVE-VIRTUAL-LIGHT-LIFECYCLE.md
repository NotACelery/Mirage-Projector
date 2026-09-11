# Mirage Projector dev.75b — authoritative virtual-light lifecycle

Status: source candidate. `dev.75b` completes the first static-world authority handoff started in dev.75a. It does not claim Windows/NeoForge build-clean until the user compiles with Java 21 and completes in-game QA.

## Authority contract

An energized Mature Crying Obsidian Cluster is represented by a `MirageLightSource`. The causal fixed-point solver owns the extended field. Current runtime never creates `mirage_projector:crying_light_node`; that block remains registered only to load and migrate old development worlds.

Effective reads are `max(vanilla, Mirage)` at the Level/combined-light/render-region boundary. Mirage values are intentionally not inserted into vanilla `BlockLightEngine`, so solved voxels cannot recursively become omnidirectional secondary emitters.

## Chunk lifecycle

- Server `ChunkEvent.Load` only queues the chunk. At `LevelTickEvent.Post`, still-live chunks are palette-scanned for orphan legacy nodes, scanned for Mature sources, then all surviving sources touching the arrived geometry are rebuilt once.
- Unloading a source-origin chunk unregisters/removes that source and retracts it from clients.
- Destination chunks may unload without deleting the source. When they return, the source is rebuilt against the newly available geometry.
- Client chunk load/unload events are coalesced until client tick post and rebuild only local source descriptors whose radius touches the changed chunk.
- Every client rebuild dirties the union of old/new Mirage field sections so block lighting recompiles without a full-world rerender.

## Network lifecycle

Protocol: **20**.

Only source descriptors are synchronized. Players receive descriptors while at least one watched chunk intersects that source radius. `ChunkWatchEvent.Sent` adds watched chunks, `UnWatch` retracts descriptors no longer needed, source removal retracts previously delivered descriptors, and logout drops server tracking state.

`syncAll` sends CLEAR. For same-level respawn it preserves the server's known watched chunk set and immediately reconciles descriptors, avoiding dependence on vanilla resending chunks that may remain watched. Dimension changes create a new level-scoped tracking state and incoming chunk sends repopulate it.

## Geometry invalidation

The server coalesces terrain mutations and solves at most once per impacted source per tick. Covered mutations are:

- player/entity single and multi-block placement;
- player block breaking;
- fluid-driven block placement;
- crop growth;
- feature growth such as trees/fungi/mushrooms/azalea;
- piston movement path;
- explosion affected blocks;
- Core Booster changes through the existing nearby-source refresh path.

This list is intentionally event-driven rather than a global `Level#setBlock` mixin so unrelated high-volume world mutation does not become a permanent hot hook.

## Legacy migration

Loaded chunk sections first use `LevelChunkSection.maybeHas(...)` to avoid full voxel scans unless their palette may contain Mirage's legacy node. Matching sections are walked and only `ModBlocks.CRYING_LIGHT_NODE` is removed. `minecraft:light` and other mods' blocks are never touched.

The legacy node block's own scheduled tick also self-removes, providing a second cleanup route for already scheduled nodes.

## Performance choices

- source fields remain sparse section maps;
- visible aggregate reads remain O(1);
- replacing a source field rebuilds each touched aggregate section once over the union of old/new field sections;
- source descriptors are tracking-scoped instead of dimension-wide;
- server chunk-load work is deferred/coalesced;
- client chunk geometry rebuilds are deferred/coalesced;
- no voxel-light network packets exist.

## Known boundary after dev.75b

The authoritative static backend intentionally does not inject virtual values into the low-level vanilla block-light propagation listener. A third-party mod that explicitly bypasses `BlockAndTintGetter`, `LevelLightEngine#getRawBrightness` and render-region brightness and reads the raw vanilla block-light storage can still report vanilla-only values. Add targeted compatibility only for a concrete failing consumer; do not poison vanilla propagation globally.

Moving/portable sources, directional/frustum/plane profiles and RGB-preserving visual light remain later Mirage Light Engine phases.

## Required QA

1. Windows Java 21 `build.bat` must succeed.
2. With no Booster, verify `15,15,14,14,...,1,1` and no physical Crying Light Nodes.
3. Build/remove a full wall and confirm causal shadow/recovery in the same tick.
4. Put a Cluster field across a chunk border; unload/reload destination chunks and verify the field fills only after geometry is present.
5. Unload/reload the source chunk and verify teardown/rediscovery.
6. Relog, respawn and change dimensions; verify no stale or missing client fields.
7. In multiplayer, move a second client into/out of tracking range and verify descriptor appearance/retraction without affecting the first client.
8. Load a dev.73/74 world with orphan `crying_light_node` blocks and verify loaded chunks remove only those legacy nodes.
9. Change a nearby wall using piston/fluid/explosion/tree/crop paths and verify the field rebuilds.
10. Stress several overlapping Mature Clusters and record solve time/frame/server-tick impact before any further optimization.
