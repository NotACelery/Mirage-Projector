# dev.70 — Instant occlusion rebuild and strict axial relays

## Why dev.70 exists

In-game QA of dev.69 exposed two separate problems:

1. terrain edits could remain visually stale until the Mature source's 20-tick refresh and/or the Light Node's 40-tick self-validation;
2. Glass/Diffusion's long face-diagonal auxiliary sources could remain on paths that were individually visible and then radiate vanilla block light omnidirectionally onto the far side of a wall.

The second issue is a fundamental consequence of using ordinary scalar Minecraft block-light sources: an auxiliary node has no memory of which direction its energy came from.

## Runtime contract

Static Mature world light now uses only the six axial half-decay branches. Quartz/Radiance and Diamond/Focus keep their axial reach contributions. Glass still modifies reflected residual-ray geometry, brightness/radius/excitation helpers, but it does not create long secondary static-light branches in dev.70.

Within each axial branch, candidates are ordered from the source outward. A full opaque blocker anywhere in the sampled path marks that branch blocked for the rest of the refresh. A full opaque block exactly on a candidate position is also treated as a hard endpoint. Every existing downstream Mirage Light Node is reconciled immediately; it is removed if no other valid Mature source wants it or downgraded to the strongest surviving contribution.

## Same-tick terrain invalidation

Active energized Mature source positions are indexed per `ServerLevel` when their field refreshes. `BlockEvent.EntityPlaceEvent` and `BlockEvent.BreakEvent` queue changed positions. The queue is consumed from `LevelTickEvent.Post`, after placement/break state is final.

For each level tick:

- all changed positions are coalesced;
- every active Mature source inside the maximum candidate envelope is selected at most once;
- selected sources rebuild immediately from the final world state;
- stale downstream nodes are reconciled in that same rebuild.

The normal 20-tick crystal optics tick and 40-tick node self-validation remain fallback safety mechanisms for non-event-driven world changes.

## dev.69 migration

The first dev.70 refresh/removal of a Mature source scans the exact old dev.69 face-diagonal candidate set once. Any surviving `mirage_projector:crying_light_node` there is reconciled against the dev.70 six-axis authority. Positions still legitimately required by another axial source survive; diagonal-only leftovers disappear.

## Known engine boundary

Vanilla block light is scalar, not directional. dev.70 can prevent Mirage from seeding a source beyond an opaque axial blocker, but it does not implement a custom directional light engine. Weak illumination may still naturally travel around the physical edge of a finite wall through Minecraft's own propagation. That is allowed; a Mirage-owned node directly behind the blocked branch is not.

## Acceptance QA

See `QA-REGRESSION.md`, section **dev.70 instant occlusion rebuild**. Windows Java 21 compilation and in-game wall/unblock/Glass migration QA are required before marking build-clean/runtime-clean.
