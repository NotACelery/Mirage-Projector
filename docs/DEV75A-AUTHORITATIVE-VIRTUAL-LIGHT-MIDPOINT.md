# Mirage Projector dev.75a — authoritative virtual-light midpoint

## Why this snapshot exists

dev.75 was intentionally split after repeated long generation failures. dev.75a is a recoverable midpoint: enough of the authority handoff is implemented to compile/test independently before chunk lifecycle and delivery optimization are added in dev.75b.

## Implemented in dev.75a

- Network protocol `19 -> 20`.
- `MirageLightSourceSyncPayload` synchronizes source descriptors, never solved voxel arrays.
- Server Mature sources broadcast upsert/remove descriptors to players in the same dimension.
- Login, respawn and dimension changes perform a simple full source resync.
- Client reconstructs the field with the same deterministic solver and marks old/new affected render sections dirty.
- `Level` brightness queries merge block light as `max(vanillaStorage, Mirage)`.
- `RenderChunkRegion` mirrors that merge so chunk compilation can consume Mirage block light.
- `MirageLightWorld` can resolve a field from the owning `LevelLightEngine`, allowing render regions to share the same client field cache.
- Physical `mirage_projector:crying_light_node` blocks are no longer created by the Mature light runtime.
- Loaded legacy axial/dev.69 diagonal relay positions are removed around active/removed Mature sources.
- Legacy node scheduled ticks now self-delete instead of re-establishing old relay fields.
- `/miragelight probe` reports `mirage`, raw vanilla block-light storage and merged `effective`.

## Deliberate dev.75a limitations

These are dev.75b work, not hidden claims:

1. No chunk-load/unload rebuild hook yet.
2. If a source is solved while destination chunks are absent, those chunks stay absent from that solved field until another forced rebuild.
3. Source packets are sent to every player in the same dimension rather than only relevant chunk trackers.
4. Legacy-node cleanup is source-centered and loaded-chunk-only; orphan relay scanning by loaded chunk is still pending.
5. Direct consumers that bypass `BlockAndTintGetter` and read a vanilla `LayerLightEventListener` directly are not yet separately intercepted.
6. No dynamic/mobile/RGB render backend yet.

## QA for this midpoint

1. Build with Windows Java 21.
2. Existing dev.74 world: confirm loaded `crying_light_node` relays disappear around the Mature source.
3. Confirm no new `crying_light_node` blocks appear.
4. Compare `/miragelight probe`: `effective == max(mirage, vanillaStorage)`.
5. Use Simple Light Level and visual world rendering to verify whether their query paths consume the new bridge.
6. Build/break a complete wall and verify same-tick forced solve still changes the virtual field.
7. Join/rejoin and change dimension with an already-active Mature source.
8. Multiplayer smoke test if available.
9. Record any consumer that still displays raw vanilla only; dev.75b can add a narrower bridge instead of feeding Mirage into vanilla propagation.
