# Mirage Projector

Mirage Projector is a NeoForge 1.21.1 mod by **Celerbi** for configurable decorative holographic projections. The current development line supports images/GIFs, items, banners and frozen entity snapshots across six state-preserving projector chassis.

**Current source line:** `0.1.0-dev.75d` — authoritative static Mirage Light Engine with causal vanilla-compatible occlusion plus obstacle-only detour decay. Open Mature light keeps exact half-decay; extra path forced by walls is penalized more strongly so shadows remain gradual without an abrupt mode switch.

Start with `docs/DOCUMENTATION-AUTHORITY.md`. Current behavior is documented in `docs/CURRENT-IMPLEMENTATION.md`; genuinely pending work lives only in `docs/ROADMAP.md`.

## Platform

- Minecraft 1.21.1
- NeoForge 21.1.244+
- Java 21
- Gradle 9.2.1
- Parchment 2024.11.17
- Network protocol 21

## Projector chassis

| Chassis | Nominal projection geometry | Lift | Float | PU multiplier |
|---|---:|---:|---:|---:|
| Mirage Projector | 10×10 | 32 | 4 | ×1.00 |
| Mirage Display | 32×32 | 48 | 12 | ×1.50 |
| Wide Mirage Projector | 80×32 | 64 | 12 | ×2.00 |
| Tall Mirage Projector | 32×80 | 96 | 16 | ×2.00 |
| Mirage Prism | 48×48 baseline | 96 | 12 | ×2.00 |
| Mirage Field Projector | 128×128 | 144 | 24 | ×4.00 |

The nominal envelope is an efficiency target rather than a hard render cap. The Power system can enter Overdrive when enough PU is available.

Crafting progression is state-preserving:

```text
Mirage Projector
      |
      v
Mirage Display
   |    |    |    |
   v    v    v    v
 Wide  Tall Prism Field
```

## Projection sources

### Image / GIF

Supported current import families include PNG, JPG/JPEG, static WebP, BMP and animated GIF. Wide and Tall support optional four-source image layouts, Prism uses independent cardinal faces, and Field remains one continuous large Plane.

Image assets are content-addressed and synchronized through the Mirage server/client asset pipeline rather than assuming every client already owns the imported file.

### Item

The Item workspace stores a virtual serialized snapshot. The real inventory item stays with the player. Blocks render as volumetric block/item models where applicable and ordinary items use the Minecraft item renderer.

### Banner

Banner appearance is copied virtually. The real banner is never consumed by the projector. Plane chassis render banner cloth without a physical pole; Prism supports independent cardinal banner faces.

### Entity

Entity Scan Cards capture frozen visual data rather than keeping a live ticking entity. Current support includes generic living entities, Players, Humanoid equipment, bodyless equipment rigs, Horse Saddle/Body Armor, per-channel render visibility, custom names and pose presets. Hiding equipment suppresses only rendering; the virtual snapshot remains stored and can be restored immediately. Passenger/vehicle composite scans remain intentionally rejected until a dedicated format exists.

## Presentation controls

Current shared controls include Scale, Lift, rotation, rotation period/direction/offset, Floating, float amplitude/timing, world lighting or Fullbright, Ghost/opacity, Tint, image flip and scanlines.

## Projection Power

| Core material | Base PU | Standard | Loaded Core Booster |
|---|---:|---:|---:|
| Glass | 32 | ×1.00 | ×1.50 |
| Quartz | 48 | ×1.00 | ×1.50 |
| Amethyst | 64 | ×1.00 | ×1.50 |
| Diamond | 96 | ×1.00 | ×1.50 |
| Netherite | 128 | ×1.00 | ×1.50 |

Effective capacity is:

```text
floor(Base PU × chassis multiplier × Core amplification)
```

## Core Booster

There is one user-facing `mirage_projector:core_booster`.

A placed empty Booster accepts Glass, Quartz, Amethyst Shard, Diamond or Netherite Ingot with right-click. Shift + right-click returns the stored material. Loaded Boosters preserve material when correctly mined and only stack with identical stored state. Empty Boosters are not valid projector Cores.

Five old `improved_*_core` block IDs remain only as migration shims for development worlds. They have no BlockItems, recipes or active Creative exposure and must not be treated as separate gameplay products.

## Crying Obsidian ecosystem

Implemented systems include:

- Crying Obsidian Shard crafting/loot loop;
- downward Small → Medium → Large → Mature renewable crystal growth from Crying Obsidian with lava above;
- Amethyst-family visual geometry recolored into the Crying Obsidian palette;
- exact stage harvesting with Silk Touch preservation and no Fortune multiplier;
- Beacon attenuation at roughly 75% / 50% / 25% / 0%;
- energized Mature Cluster lighting and residual purple ray behavior;
- Obsidian Spike trap.

Loaded Core Boosters relay active Beacon columns. Glass provides Diffusion, Quartz Radiance, Amethyst Resonance, Diamond Focus and Netherite Inversion, with at most four effective Boosters. Only an energized Mature Cluster is a static world-light source.

The forward **Mirage Light Engine** is now authoritative for energized Mature Cluster world light. dev.74 introduced causal fixed-point virtual fields; dev.75a/75b moved authority away from physical relays, synchronized compact source descriptors, added chunk/tracking lifecycle and legacy-node cleanup; dev.75c corrected vanilla opacity handoff; dev.75d adds profile-level obstacle detour penalty. Open no-Booster light remains exactly `15,15,14,14,...,1,1`, while distance that exists only because light had to route around geometry decays at a stronger vanilla-like rate.

Physical `crying_light_node` blocks are migration-only and are never created by current runtime. Effective scalar light is `max(vanilla, Mirage)` at read time; Mirage virtual cells are never fed back into vanilla propagation as new emitters. See `docs/MIRAGE-LIGHT-ENGINE.md` for the complete runtime, networking, chunk, occlusion, debugging and future-backend contract.

## Current stabilization point

`dev.75d` is the static-light closure candidate before dev.76. The Mature Cluster virtual field now has exact open-space half-decay, real opaque/partial-block edge handling, natural route-around-wall behavior with extra detour penalty, chunk lifecycle, tracking-scoped source sync, overlap max aggregation and legacy-node migration.

Required acceptance before calling this line build-clean is Windows Java 21 compilation plus in-game QA of open curve, 1/2/3-block and L-shaped walls, slabs/stairs/partial opacity, chunk borders/unload/reload, relog/respawn/dimension, overlapping sources, Quartz/Diamond reinforcement and several simultaneous fields. dev.71 equipment visibility and dev.72 Entity envelope/frustum work remain accumulated and must not regress.

## Documentation

Current active documents:

- `docs/DOCUMENTATION-AUTHORITY.md`
- `docs/CURRENT-IMPLEMENTATION.md`
- `docs/ARCHITECTURE.md`
- `docs/REGISTRY-INVENTORY.md`
- `docs/POWER-AND-CHASSIS.md`
- `docs/CORE-BOOSTER-AND-UPGRADES.md`
- `docs/CRYING-OBSIDIAN.md`
- `docs/ENTITY-AND-SNAPSHOTS.md`
- `docs/ASSET-PIPELINE.md`
- `docs/ROADMAP.md`
- `docs/QA-REGRESSION.md`
- `docs/DEV59-AUDIT.md`
- `docs/DEV60-CORE-BOOSTER-BEACON-RELAY.md`
- `docs/DEV61-MODDED-EQUIPMENT-RENDER-SAFETY.md`
- `docs/DEV62-LATE-ENTITY-DEPTH-STABILIZATION.md`
- `docs/DEV63-CREATE-LAYERED-BACKTANK-COMPAT.md`
- `docs/DEV64-CREATE-SYNTHETIC-CHEST-SINGLE-SURFACE.md`
- `docs/DEV65-POWERED-CRYING-LIGHT-FIELD.md`
- `docs/MIRAGE-LIGHT-ENGINE.md`
- `docs/LIGHT-PROFILE-FOUNDATION.md`
- `docs/DEV74-MIRAGE-LIGHT-ENGINE-FOUNDATION.md`
- `docs/DEV75A-AUTHORITATIVE-VIRTUAL-LIGHT-MIDPOINT.md`
- `docs/DEV75B-AUTHORITATIVE-VIRTUAL-LIGHT-LIFECYCLE.md`
- `docs/DEV75C-OCCLUSION-OPACITY-FIX.md`
- `docs/DEV75D-DETOUR-PENALTY-AND-CONSOLIDATION.md`
- `docs/DEV75D-STATIC-QA.md`
- `docs/CHANGELOG.md`
- `docs/DEVELOPMENT.md`
- `docs/THIRD_PARTY_NOTICES.md`
- `docs/NEXT-CHAT-HANDOFF-dev65.md`
- `docs/NEXT-CHAT-HANDOFF-dev66.md`
- `docs/NEXT-CHAT-HANDOFF-dev74.md`
- `docs/NEXT-CHAT-HANDOFF-dev75d.md`

Superseded documentation is retained under `docs/archive/pre-dev59/` and `docs/archive/post-dev59/` for historical/migration archaeology only. It is not current authority.

## Build

On Windows, run `build.bat` with Java 21 available. dev.75d is the pre-dev.76 static-light closure candidate. Validate build, exact open half-decay, natural obstacle detour shadows, partial blocks, chunk lifecycle, relog/dimension changes, multiplayer tracking, overlap/performance and legacy-node cleanup before marking it build-clean.

Release/runtime caches and generated directories are intentionally excluded from source snapshots.
