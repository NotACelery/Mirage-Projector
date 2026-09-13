# Mirage Light Engine — 1.0.0

Network protocol: **27**

The 1.0.0 static Mirage Light implementation is server-authoritative and is currently used by energized Mature Crying Obsidian Clusters. Moving/portable emitters are intentionally reserved for the separate `DYNAMIC_VISUAL` lifecycle.

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

## DYNAMIC_VISUAL boundary

`DYNAMIC_VISUAL` is deliberately separate from `STATIC_WORLD`. Future lanterns, handheld projectors or other rapidly moving emitters must use a lifecycle designed for high-frequency visual changes rather than rebuilding server-authoritative static sections every frame.

## Release invariants

For 1.0.0:

- open level-15 half-decay is exactly `15,15,14,14,...,1,1`;
- opaque geometry cannot be crossed as air;
- finite obstacles may be routed around with additional decay;
- overlap uses maximum contribution;
- static solving is server-only;
- publication is atomic across dependency windows;
- clients recover through chunk snapshots/revisions;
- physical legacy relays are migration-only.
