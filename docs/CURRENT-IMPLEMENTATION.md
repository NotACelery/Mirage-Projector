# Current Implementation — Mirage Projector 1.0.7

Version: **1.0.7**
Minecraft: **1.21.1**
NeoForge: **21.1.244+**
Network protocol: **28**

This document describes the current implementation behavior of Mirage Projector 1.0.7. Historical development notes are archived under `docs/history/` and are not current authority.

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


## Main projector settings UI

The main projector screen uses a fixed option area with four mutually exclusive tabs: **Geometry**, **Placement**, **Rotation** and **Floating**. Geometry also contains Lighting, Ghost/opacity and Tint. Switching tabs only changes the controls inside that area; Source Workspaces, Power / Capacity, Core slot, inventory and Apply / Cancel stay anchored. The compact 412-pixel panel fits 1920×1080 at GUI Scale 2 without responsive scrolling, while the existing scrollbar remains available at smaller effective heights.

## Projection source architecture

Built-in source IDs are stable namespaced identifiers:

- `mirage_projector:image`
- `mirage_projector:item`
- `mirage_projector:entity`
- `mirage_projector:banner`

Source identity is ordinal-free. Save/network settings are versioned, legacy ordinal saves migrate, and unknown registered source IDs/payloads are preserved rather than destructively coerced into a built-in type.

`ProjectionSourceRegistry` owns common source definitions/content semantics. `ProjectionSourceRenderRegistry` owns client render dispatch. Chassis/source compatibility is queried centrally instead of being hard-coded independently into each screen/renderer path.

## Projection transforms

Shared presentation state is separated from source content through `ProjectionTransform`. The shared fixed-projector transform exposes Scale/Lift, animated yaw and quaternion-backed **Tilt**. Lift is the single non-negative vertical placement axis; independent horizontal/vertical translation is intentionally not a user-facing projector control.

Tilt supports the full **-90° to +90°** range. Mirage Prism applies Tilt independently to each radial face while preserving carousel rotation around the projector center.

Mirage Prism Image/Banner projection additionally uses **Prism Distance**. The UI value is extra radial separation above the no-tilt collision-safe radius: **+0 px** packs adjacent face boundaries as tightly as possible without overlap. `PrismProjectionSpacing` derives the internal absolute radius from each adjacent pair of active faces. At +0 px, equal square faces meet at their lower corners without crossing. Positive/outward Tilt keeps that compact lower-edge baseline; negative/inward Tilt raises the minimum only by the inward reach required to avoid overlap. User-controlled extra distance is capped at **+160 px (10 blocks)**. If an inward angle would need more room than that budget, the UI refuses that angle. Additional or Tilt-required radial separation consumes a small amount of PU.

`ProjectionSettings` network format remains version 3 and network protocol remains 28. Legacy horizontal/vertical offset slots are retained only for wire/NBT compatibility: horizontal sanitizes to zero, while a positive legacy Vertical Offset is absorbed into Lift and then sanitized to zero. Worlds from 1.0.0 remain compatible.

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

Entity preview fitting, clearance and world culling use conservative pose/species/equipment-aware bounds. Passenger/vehicle composite scans remain rejected because the 1.0.x line does not define a composite snapshot format.

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

`DYNAMIC_VISUAL` is now an operational client-only runtime for moving/portable emitters. Consumers submit moving-source snapshots with stable identity, position, profile, update cadence, camera-cull distance and stale timeout. Directional-cone geometry is solved by the same causal voxel engine, while dynamic fields remain local and never enter the authoritative `STATIC_WORLD` publication channel. No lantern consumes this runtime yet; Glow Dust battery/recharge gameplay now exists independently as the portable-energy foundation.

Physical `mirage_projector:crying_light_node` exists only as migration compatibility for old development worlds and is not created by current gameplay.

## Rechargeable Glow Dust foundation

`mirage_projector:glow_dust` now stores a persistent charge value from 0–1000 units. Fresh/default stacks are full; partial/depleted charge is retained in stack custom data, shown through tooltip/status and the vanilla item charge bar, and drives a client tint so depleted dust looks duller.

Core Boosters now have a separate single-item Glow Dust charging cradle in addition to their existing Core-material socket. Right-click with Glow Dust inserts one cell; sneak-right-click with an empty hand removes the charging cell before the normal Core-material extraction path. A Booster inside a live Beacon column recharges its inserted cell over time. Each actively charging cell removes 0.20 from the outgoing beam transmission, so a clear column naturally tops out at five simultaneous charging cells. Crying Obsidian crystals consume the same attenuated transmission.

The current charge cadence is an implementation/balance baseline (1000-unit capacity, 10 units every 10 ticks while actively charging) and may be tuned before 1.1.0. Glow Dust intentionally has no committed survival recipe yet and is Creative/QA-facing until the lantern/device progression is finalized.

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

The 1.0.x line retains explicit compatibility/migration surfaces where removing them would damage existing worlds:

- old `crying_light_node` relay blocks are migration-only and self-remove;
- five historical `improved_*_core` block IDs remain migration shims without BlockItems/recipes/Creative exposure;
- legacy numeric projection-source saves migrate to namespaced source IDs;
- unknown future source IDs/payloads are preserved where possible.

## Deferred to later releases

1.0.7 does not include:

- portable lantern/projector gameplay;
- Scan Codex;
- Dragon Egg / End Resonance gameplay;
- direct grab/free-rotate hologram manipulation;
- Create Blueprint projection source.

See `ROADMAP.md`, `WAITLIST-1.1.0.md` and `WAITLIST-1.2.0.md`.
