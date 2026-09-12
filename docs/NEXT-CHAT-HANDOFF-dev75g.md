# NEXT CHAT HANDOFF — 0.1.0-dev.75g

## Focus
Source-centric chunk convergence for the Mirage Light Engine after dev.75f still clipped fields at chunk borders on world reload.

## Live QA evidence that motivated this
- After join/reload, the field could collapse to a roughly vanilla-sized square entirely contained in the source chunk.
- The clipped edge aligned exactly with F3+G chunk borders.
- Updating a block beyond the edge advanced the field by only one chunk, proving the lifecycle was progressing chunk-by-chunk rather than converging the whole source field.
- dev.75f successfully removed old ghost sources; that fix remains.

## dev.75g fix
- Solver readiness now uses `ChunkSource#getChunkNow` directly.
- Added per-source queryable chunk footprint snapshots.
- Client compares the full footprint every tick and rebuilds the entire source if it changes, independent of ChunkEvent ordering.
- Server mirrors this with a 2-tick audit fallback and authoritative whole-source refresh.
- Chunk events remain as a fast path, but are no longer the sole correctness mechanism.

## QA priority
1. Enter/re-enter the same test world repeatedly with F3+G visible.
2. Do NOT touch any blocks; verify the light crosses every already-loaded border automatically.
3. Verify it no longer starts as a one-chunk vanilla-sized island.
4. Walk far enough to unload/reload footprint chunks and return.
5. Repeat with Quartz/Diamond range.
6. Break the cluster and confirm the dev.75f ghost-source cleanup still works.

## Projector visual line
The six `*_alt` comparison projectors from dev.75e remain accumulated unchanged.
