# Current Implementation — Mirage Projector 1.0.0

Version: **1.0.0**
Minecraft: **1.21.1**
NeoForge: **21.1.244+**
Network protocol: **27**

This document describes the stable runtime behavior of Mirage Projector 1.0.0. Historical development notes are archived under `docs/history/` and are not current authority.

## Canonical projector family

The runtime exposes six projector chassis:

1. Mirage Projector
2. Mirage Display
3. Mirage Field Projector
4. Wide Mirage Projector
5. Tall Mirage Projector
6. Mirage Prism

The project no longer registers alternate/comparison projector IDs. Each chassis has one canonical block/item identity, model, VoxelShape and renderer layout.

Crafting upgrades preserve stored projector state. Mirage Projector upgrades into Mirage Display, which branches into Wide, Tall, Prism and Field variants.

## Projector state

A fixed projector keeps these concepts separate:

- **projection enabled/disabled**;
- **active projection source**;
- **workspace currently open**;
- **source-specific content**;
- **shared presentation transform**;
- **installed Core / power state**.

`TURN OFF` disables rendering without deleting Image, Item, Banner or Entity data. Selecting `Use <mode> mode` activates that source and re-enables projection. GUI source buttons indicate the source that is actually active, not merely the workspace currently being viewed.

## Projection source architecture

Built-in source IDs are stable namespaced identifiers:

- `mirage_projector:image`
- `mirage_projector:item`
- `mirage_projector:entity`
- `mirage_projector:banner`

Source identity is ordinal-free. Save/network settings are versioned, legacy ordinal saves migrate, and unknown registered source IDs/payloads are preserved rather than destructively coerced into a built-in type.

`ProjectionSourceRegistry` owns common source definitions/content semantics. `ProjectionSourceRenderRegistry` owns client render dispatch. Chassis/source compatibility is queried centrally instead of being hard-coded independently into each screen/renderer path.

## Projection transforms

Shared presentation state is separated from source content through `ProjectionTransform`. Current UI exposes the established scale/lift/rotation/float/tint/ghost controls, while persisted orientation already carries normalized quaternion fields for future direct-manipulation work.

## Image / GIF

Supported import families:

- PNG
- JPG/JPEG
- static WebP
- BMP
- animated GIF

Imported assets are content-addressed and synchronized through the Mirage asset pipeline. Wide/Tall can use multi-source layouts, Prism supports independent cardinal faces and Field uses one continuous plane.

## Item

The Item workspace stores a virtual serialized snapshot. The source inventory item is not consumed or physically stored inside the projector. Blocks use volumetric rendering when applicable; ordinary items use Minecraft's item renderer.

## Banner

Banner appearance is copied virtually. Plane chassis render cloth without a physical banner pole. Prism stores independent North/East/South/West banner snapshots and can copy the North source to the remaining faces.

## Entity

Entity Scan Cards contain frozen projection data rather than live entities. Supported state includes:

- generic living entities;
- Players and Player skin/model-part state;
- Humanoid equipment and held items;
- bodyless equipment rigs;
- Horse Saddle and Body Armor;
- custom names/nameplates;
- supported pose presets;
- per-channel projected-equipment visibility.

Entity preview fitting, clearance and world culling use conservative pose/species/equipment-aware bounds. Passenger/vehicle composite scans remain rejected because 1.0.0 does not define a composite snapshot format.

## Projection Power

`ProjectionPower` is the authority for capacity, component cost, overdrive and feasible slider limits. Fixed projectors obtain energy through `ProjectionEnergySource` backed by the installed Projection Core; the energy boundary itself is not tied to a Core socket so future portable devices can use another backend.

Core base PU:

| Material | Base PU |
|---|---:|
| Glass | 32 |
| Quartz | 48 |
| Amethyst | 64 |
| Diamond | 96 |
| Netherite | 128 |

Effective capacity is:

```text
floor(Base PU × chassis multiplier × Core amplification)
```

A loaded Core Booster contributes ×1.50 Core amplification and retains material-specific Beacon/Mirage-light identity.

## Crying Obsidian ecosystem

The renewable crystal loop is:

```text
Lava source
    ↓
Crying Obsidian
    ↓
Small Bud → Medium Bud → Large Bud → Cluster
```

The space below Crying Obsidian must be available for growth. Silk Touch preserves the current bud/cluster stage. Normal harvesting produces Crying Obsidian Shards; Fortune does not multiply shard drops.

Crying Obsidian can be crafted from shards using either Fire Charge or Magma Cream recipes. EMI/JEI integrations expose the crafting chain and growth guidance when those viewers are installed.

## Core Booster / Beacon relay

There is one user-facing `core_booster` block/item. It accepts Glass, Quartz, Amethyst Shard, Diamond or Netherite Ingot and preserves loaded material state when properly mined.

Material relay identities:

- Glass — Diffusion
- Quartz — Radiance
- Amethyst — Resonance
- Diamond — Focus
- Netherite — Inversion

At most four effective loaded Boosters participate in Beacon relay calculations. Only an energized Mature Crying Obsidian Cluster publishes static Mirage world light.

## Mirage Light Engine

Static Mature Cluster lighting is server-authoritative. The causal six-neighbour solver uses vanilla destination opacity/face occlusion, exact fixed-point half-decay in open space, additional obstacle-detour cost, overlap-by-maximum aggregation and chunk-aware dependency windows.

Clients do not solve `STATIC_WORLD` geometry. They install server-resolved packed Mirage light sections and read effective light as:

```text
max(vanilla block light, Mirage light)
```

Mirage virtual light is never fed back into vanilla block-light propagation as a new emitter.

`DYNAMIC_VISUAL` remains a separate backend boundary for future moving/portable emitters.

Physical `mirage_projector:crying_light_node` exists only as migration compatibility for old development worlds and is not created by current gameplay.

## Recipe viewers

### EMI

EMI integration exposes:

- all custom projector upgrade recipes under Crafting;
- both Crying Obsidian shard recipes;
- icon/tool-tip-based Crying Obsidian World Interaction guidance;
- age-ordered crystal Block Drops.

### JEI

JEI integration exposes custom projector upgrade recipes through the vanilla Crafting category and supplies ingredient information for projector progression and renewable Crying Obsidian growth.

Both integrations are optional. Mirage Projector loads normally when either or both recipe viewers are absent.

## Public handbook

The in-game `Mirage Handbook` documents General behavior plus one section for each of the six chassis. The registry ID remains `debug_handbook` for save compatibility, but the public display name and content are release-facing.

## Compatibility and migration

1.0.0 retains explicit compatibility/migration surfaces where removing them would damage existing worlds:

- old `crying_light_node` relay blocks are migration-only and self-remove;
- five historical `improved_*_core` block IDs remain migration shims without BlockItems/recipes/Creative exposure;
- legacy numeric projection-source saves migrate to namespaced source IDs;
- unknown future source IDs/payloads are preserved where possible.

## Deferred to later releases

1.0.0 does not include:

- portable lantern/projector gameplay;
- rechargeable Glow Dust batteries;
- Scan Codex;
- Dragon Egg / End Resonance gameplay;
- direct grab/free-rotate hologram manipulation;
- Create Blueprint projection source.

See `ROADMAP.md`, `WAITLIST-1.1.0.md` and `WAITLIST-1.2.0.md`.
