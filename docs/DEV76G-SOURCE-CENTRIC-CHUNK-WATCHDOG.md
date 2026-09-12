# dev.76g — Source-centric chunk watchdog

## Why

After dev.75f-dev.76f, repeated login QA still produced one or two chunk columns with stale/missing Mirage light even after moving STATIC_WORLD solving to the server and making chunk snapshots atomic/revisioned. The fix line had become increasingly dependent on global chunk event ordering.

## New rule

Each energized Mature Crying Obsidian Cluster owns responsibility for validating the chunk window that can influence its static field.

- Radius ~30: minimum watch radius +/-2 chunks (5x5).
- Boosted radius ~38: watch radius automatically grows to +/-3 chunks (7x7).
- The watchdog probes only `getChunkNow()` / local epochs. It never creates tickets or force-loads chunks.
- Chunk load/unload and relevant block-geometry changes update a per-chunk epoch only when near an active source.
- Every server tick each active source computes a tiny watch signature. A signature change force-rebuilds the complete causal field from the Cluster origin.
- Walls are **not** bypassed by the watchdog. The watchdog only requests a rebuild; `MirageLightSolver` remains the sole authority for vanilla face occlusion, opacity and detour penalty.

## Self-healing client mirror

A full chunk snapshot can be kilobytes, so dev.76g does not spam full snapshots continuously. Every energized source sends a compact manifest once per second containing, for each currently queryable watched chunk:

- packed chunk position,
- authoritative chunk revision,
- authoritative Mirage section count.

The client compares the manifest against its installed mirror. It requests a chunk only when:

- its revision is older/missing, or
- its local Mirage section count differs from the server.

Requests have a 40-tick retry cooldown, so a response that is delayed cannot permanently suppress recovery.

## Intended 1.1 foundation

This watchdog is specifically for `STATIC_WORLD` sources. Future lanterns/handheld projectors remain `DYNAMIC_VISUAL` and must not rebuild or synchronize chunk-light sections while moving.
