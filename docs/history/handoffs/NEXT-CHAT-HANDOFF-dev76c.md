# NEXT CHAT HANDOFF — 0.1.0-dev.76c

## Purpose
Atomic publication for server-authoritative `STATIC_WORLD` Mirage light.

## QA finding that triggered this pass
`dev.76b` removed client-side static solving, but the same chunk-shaped artifact still appeared briefly and converged faster. This proves the authoritative server itself was publishing clipped solves while dependency chunks were still attaching.

## New contract
A STATIC_WORLD source is never published from a partial dependency window.

1. Build the complete chunk dependency window from source origin +/- real profile radius.
2. If any dependency chunk is not queryable, mark the source pending and do not solve/publish it.
3. Pending sources are retried every server tick (roughly 25 probes for a normal radius-30 Cluster). No chunk tickets are created and nothing is force-loaded.
4. Once the full window is queryable, solve from the source and atomically replace/publish all changed aggregate sections.
5. As a final guard, any solver candidate reporting `unloadedEdges > 0` is discarded and the previous complete field remains authoritative.

## Expected visible behavior
On login there may be a short period of vanilla light while the 5x5-ish dependency window attaches. There must no longer be progressive chunk-by-chunk Mirage shapes. The first Mirage publication should already be the complete field.

## Retained
- server-authoritative section transport / protocol 22
- max(vanilla, Mirage) read path
- half-decay, occlusion, detour penalty, boosters, overlaps
- stale-source cleanup
- automatic cleanup-before-build pipeline
- alternate projector comparison blocks

## QA
1. Build on Windows Java 21.
2. Enter the existing iron-platform world and do not touch blocks.
3. Observe LightLevelSimple: vanilla-only may show briefly; Mirage must appear as one complete field, not as a mosaic of chunks.
4. Repeat 10 relogs.
5. Remove/re-place the cluster and verify the replacement is also atomic.
6. Run `/miragelight stats`; during startup it may show `0 solved, 1 pending`, then `1 solved, 0 pending`.
