# Mirage Light Engine — 1.0.69

Network protocol: **44**

The static Mirage Light implementation remains server-authoritative and is currently used by energized Mature Crying Obsidian Clusters. Since 1.0.5, moving/portable emitters have a separate operational `DYNAMIC_VISUAL` client lifecycle. Mirage Flashlight, Mirage Light Projector and Mirage Wall Projector are current consumers; Shoulder-mounted Flashlights reuse the same client-local source manager. The temporary placed Flashlight form reuses the placed-light BlockEntity/runtime instead of introducing another light engine.

## Core rule

Mirage uses a virtual fixed-point light field rather than planting vanilla light-emitting relay blocks.

Visible light is read as:

```text
max(vanilla block light, Mirage light)
```

Mirage values are never fed back into vanilla block-light propagation as new emitters.

## Solver profile

The Mature Cluster uses an omnidirectional `EXTEND` profile with two fixed-point substeps per visible light level.

A conceptual level-15 source therefore produces the open-space curve:

```text
15, 15, 14, 14, 13, 13, ... , 2, 2, 1, 1
```

The solver walks the six cardinal neighbours only. Open-air movement uses the normal half-decay cost; geometry-forced backtracking can add extra detour cost.

## Occlusion

Each edge delegates opacity/face-shape semantics to vanilla light helpers. Destination opacity uses the real destination state's light-block value rather than a constant fallback.

Consequences:

- full opaque walls block direct propagation;
- partial blocks use vanilla shape/opacity semantics;
- finite walls can be routed around;
- routes that exist only because geometry forced a detour are weaker than equivalent open-space paths.

## Overlap

Multiple Mirage sources aggregate by maximum contribution, not addition. Removing one source cannot erase a stronger contribution still supplied by another source.

## Core Booster influence

At most four loaded Core Boosters participate in the Beacon relay feeding a Mature Cluster.

Static-field identities:

- **Glass / Diffusion** — softens detour shadowing but trades away straight-line reach when unopposed;
- **Quartz / Radiance** — strongest pure static-reach amplifier; each Quartz adds one conceptual tier, equivalent to about two extra open blocks under half-decay;
- **Amethyst / Resonance** — changes optical/residual behavior but grants no free static reach;
- **Diamond / Focus** — strengthens geometric shadowing; two Diamonds add one conceptual static-reach tier;
- **Netherite / Inversion** — reverses optical rotation and grants no free static reach.

The static conceptual level is clamped to the engine's safe profile bounds and visible output remains capped to vanilla-compatible 0..15.

## Server authority

`STATIC_WORLD` geometry is solved only on the server.

A source is publishable only when its complete horizontal dependency window is queryable. The solver never force-loads chunks. If the window is incomplete, publication is deferred and the previously complete field remains authoritative until a complete replacement can be solved.

This prevents partial/mosaic light states during login, chunk attachment and boundary transitions.

## Chunk synchronization

Clients receive atomic per-chunk Mirage-light snapshots containing packed 16×16×16 section data. Chunk revision manifests act as lightweight watchdogs; a client requests a full snapshot only when its local revision is absent or stale.

When an authoritative snapshot is installed, affected render/light sections are invalidated even if Mirage bytes are unchanged, because external consumers may have cached vanilla-only values before the snapshot arrived.

## Terrain invalidation

Relevant terrain/lifecycle changes coalesce and revalidate impacted source fields against committed server geometry. Coverage includes normal placement/break events plus fluid, growth, piston, explosion and Core Booster changes used by the current implementation.

Source-centered dependency tracking also catches chunk attach/detach changes. Rebuilds are performed from the source rather than treating a changed chunk as an independent emitter.

## Runtime storage

`MirageLightEngine`/`MirageLightWorld` own authoritative solved source fields and aggregate section state. The old physical `mirage_projector:crying_light_node` block is migration-only; current runtime never creates it.

Legacy relay positions are cleaned only when they are known Mirage migration data. The cleanup path must never remove vanilla `minecraft:light` or unrelated mod blocks.

## Diagnostic commands

The `/miragelight` command family provides diagnostics such as:

- `stats`
- `probe`
- `axis`
- `rebuild`

These commands inspect Mirage, vanilla and effective light and are intended for regression/debugging rather than gameplay progression.

## DYNAMIC_VISUAL runtime

`DYNAMIC_VISUAL` is deliberately separate from `STATIC_WORLD`. Since 1.0.5, the client owns a moving-source manager that accepts `MirageDynamicLightSnapshot` submissions and controls solve cadence, camera culling, stale cleanup and render-section invalidation. Dynamic sources are solved locally into the Mirage aggregate and are never published as authoritative server sections.

The shared solver supports `DIRECTIONAL_CONE` in addition to omnidirectional fields. A directional profile still propagates causally through adjacent voxels and still obeys normal opacity/face occlusion; cells outside the declared cone are rejected before readiness accounting. Generic profile factories leave actual Focus/Flood/Ambient balance values to the device layer. In 1.0.9 the placed Mirage Light Projector became the first real consumer. **1.0.18 corrects the narrow-cone acceptance test from voxel-center-only sampling to approximate cone/voxel-volume intersection**, preventing Focus/Flood from disappearing at ordinary yaw/pitch angles merely because no adjacent voxel center fell inside the mathematical cone. Device source origins are also seeded slightly outside the player/projector body to prevent the first propagation edge from self-occluding inside its own emitter geometry.

A submitted moving source is identified independently from its current block position. Repositioning or redirecting it updates the same source contribution rather than leaving transient emitters behind. Sources outside their camera cull distance are removed from the local aggregate until visible again, and consumers that stop submitting expire automatically. Fields clipped by temporarily unavailable chunks retry at their declared cadence.

Placed-device battery integration is active in 1.0.9 and the first handheld/player-following consumer is active in 1.0.10. Held-lantern state is reconstructed from vanilla tracked player transform/equipment plus the lantern ItemStack summary, avoiding a second movement-light packet path. 1.0.11 reuses that design lesson for portable holograms: handheld projector visuals are likewise reconstructed client-side from tracked held ItemStacks plus player transform rather than adding a separate movement/projector payload. The stationary and portable lighting behavior is runtime-tested against the defined terrain-change rules; remaining 1.1.0 work is release-candidate performance, multiplayer and renderer-compatibility QA.

## Release invariants

For the static 1.0.x path:

- open level-15 half-decay is exactly `15,15,14,14,...,1,1`;
- opaque geometry cannot be crossed as air;
- finite obstacles may be routed around with additional decay;
- overlap uses maximum contribution;
- static solving is server-only;
- publication is atomic across dependency windows;
- clients recover through chunk snapshots/revisions;
- physical legacy relays are migration-only.

## Packed-light visual bridge

The solver/storage/query path and the baked world-vertex light path are separate concerns. Runtime QA in 1.0.18 proved that Simple Light Level could observe correct Mirage virtual values while terrain still appeared visually dark. 1.0.19 therefore introduced a merge of `MirageLightEngine.virtualBlockLight(...)` into both 1.21.1 `LevelRenderer.getLightColor(...)` packed-light overloads, preserving the existing sky channel with `LightTexture.pack(block, sky)`. 1.0.20 keeps that bridge but makes it Sodium-safe: the return hook queries the active `ClientLevel` rather than `BlockAndTintGetter#getLightEngine()`, because Sodium's chunk-meshing `LevelSlice` intentionally rejects that accessor. Section invalidation remains responsible for forcing affected compiled sections to rebuild. 1.0.21 corrects the mesh-dependency side of that contract: static/authoritative changes invalidate the full one-section halo, while DYNAMIC_VISUAL changes compare pre/post aggregate boundary bytes and invalidate only neighboring render sections that can sample a changed face/edge/corner. This prevents stale lit/dark walls from persisting until a block update without rebuilding a full 3x3x3 halo every two ticks for moving Lanterns.

This bridge is client-visual only and does not publish `DYNAMIC_VISUAL` values into authoritative vanilla block-light propagation. Visible terrain illumination is runtime-confirmed as of the 1.0.20 QA pass; 1.0.21 runtime QA focuses on stale-mesh convergence while moving/turning emitters and across section boundaries.
