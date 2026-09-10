# Mirage Projector

Mirage Projector is a NeoForge 1.21.1 mod by **Celerbi** for configurable decorative holographic projections. The current development line supports images/GIFs, items, banners and frozen entity snapshots across six state-preserving projector chassis.

**Current source line:** `0.1.0-dev.74` — Mirage Light Engine foundation running as a parallel shadow solver over the dev.73 physical Mature-light backend.

Start with `docs/DOCUMENTATION-AUTHORITY.md`. Current behavior is documented in `docs/CURRENT-IMPLEMENTATION.md`; genuinely pending work lives only in `docs/ROADMAP.md`.

## Platform

- Minecraft 1.21.1
- NeoForge 21.1.244+
- Java 21
- Gradle 9.2.1
- Parchment 2024.11.17
- Network protocol 19

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

dev.74 introduces the forward **Mirage Light Engine** architecture after live QA proved that physical auxiliary emitters cannot preserve source causality: each accepted relay becomes an independent omnidirectional vanilla source and can refill hidden regions. The new shadow solver propagates fixed-point energy voxel-to-voxel through vanilla-compatible edge occlusion and stores final contributions sparsely by 16³ section. Its no-Booster mathematical target is exactly `15,15,14,14,...,1,1`.

For safety, dev.74 does **not** switch visible/gameplay lighting yet. The dev.73 `crying_light_node` backend remains active while the virtual field is calculated in parallel and inspected with `/miragelight`. dev.75 is the planned authority handoff after wall/decay/performance QA.

## Current stabilization point

The lighting branch is now an architectural migration rather than another relay tweak. dev.74 must prove the virtual solver itself before it is allowed to replace world light in dev.75. Entity P2 hardening from dev.72 and equipment visibility from dev.71 remain accumulated and must not regress while this work proceeds.

For dev.74 light QA, Simple Light Level still shows the legacy physical result. Use `/miragelight axis`, `/miragelight probe`, `/miragelight stats` and `/miragelight rebuild` to inspect the shadow field.

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
- `docs/LIGHT-PROFILE-FOUNDATION.md`
- `docs/DEV74-MIRAGE-LIGHT-ENGINE-FOUNDATION.md`
- `docs/CHANGELOG.md`
- `docs/DEVELOPMENT.md`
- `docs/THIRD_PARTY_NOTICES.md`
- `docs/NEXT-CHAT-HANDOFF-dev65.md`
- `docs/NEXT-CHAT-HANDOFF-dev66.md`
- `docs/NEXT-CHAT-HANDOFF-dev74.md`

Superseded documentation is retained under `docs/archive/pre-dev59/` and `docs/archive/post-dev59/` for historical/migration archaeology only. It is not current authority.

## Build

On Windows, run `build.bat` with Java 21 available. dev.74 is a source candidate until Windows compilation and shadow-solver QA pass. The virtual layer intentionally does not alter visible Minecraft lighting yet; dev.75 will perform that handoff only after the foundation is measured.

Release/runtime caches and generated directories are intentionally excluded from source snapshots.
