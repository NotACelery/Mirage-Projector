# dev.76 — Server-authoritative STATIC_WORLD section channel

## Why this exists
The dev.75 architecture synchronized compact source descriptors and asked every client to solve the same field against its own currently attached chunks. QA proved that client chunk/section attachment timing can differ across logins and produce partial/non-deterministic fields. Retrying and settling could reduce symptoms but could not make local geometry timing authoritative.

## New authority contract
- `STATIC_WORLD`: server-only solver.
- Server stores per-source fixed-point fields and the max aggregate layer exactly as before.
- Network transports the **aggregate final visible section**, not source descriptors.
- Client stores an authoritative Mirage section mirror; it never runs `MirageLightSolver` for static sources.
- Effective block light remains `max(vanilla, Mirage)`. Mirage values are not inserted into vanilla propagation.
- Multiple static sources are already resolved/aggregated before the client receives a section.

## Wire format
`MirageLightSectionSyncPayload` actions:
- `SET_SECTION`
- `CLEAR_SECTION`
- `CLEAR_CHUNK`
- `CLEAR_ALL`

A section has 4096 visible values in range 0..15. Two values are packed per byte, producing 2048 bytes for a full non-empty section. Protocol is 22.

## Tracking/lifecycle
`ChunkWatchEvent.Sent` records the watched chunk and immediately sends every current Mirage aggregate section in that chunk. A later source/terrain/chunk rebuild broadcasts current values for touched sections only to players watching those chunks. `UnWatch` sends a chunk tombstone. Client chunk unload also clears locally as a safety path. Login/dimension/respawn session reset uses `CLEAR_ALL`.

## Light consumers
Installing/removing a client authoritative section calls `ClientChunkCache.onLightUpdate(BLOCK, section)` and marks the render section dirty. This is the generic invalidation bridge for vanilla rendering and other light consumers such as the private LightLevelSimple port.

## Future 1.1 split
`DYNAMIC_VISUAL` stays independent. Lanterns, handheld projectors and other moving sources must not send 2 KiB static-section packets every movement tick. dev.76 establishes the boundary: STATIC_WORLD is deterministic section state; DYNAMIC_VISUAL will be a moving/render-oriented backend with its own update model.
