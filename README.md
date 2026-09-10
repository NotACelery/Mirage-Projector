# Mirage Projector

Mirage Projector is a NeoForge 1.21.1 mod by **Celerbi** for configurable decorative holographic projections. The current development line supports images/GIFs, items, banners and frozen entity snapshots across six state-preserving projector chassis.

**Current source line:** `0.1.0-dev.69` — occlusion-aware Mature Cluster reflected light plus material-specific Core Booster reflection over the dev.68 interaction/teardown baseline.

Start with `docs/DOCUMENTATION-AUTHORITY.md`. Current behavior is documented in `docs/CURRENT-IMPLEMENTATION.md`; genuinely pending work lives only in `docs/ROADMAP.md`.

## Platform

- Minecraft 1.21.1
- NeoForge 21.1.244+
- Java 21
- Gradle 9.2.1
- Parchment 2024.11.17
- Network protocol 18

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

Entity Scan Cards capture frozen visual data rather than keeping a live ticking entity. Current support includes generic living entities, Players, Humanoid equipment, bodyless equipment rigs, Horse Saddle/Body Armor, custom names and pose presets. Passenger/vehicle composite scans remain intentionally rejected until a dedicated format exists.

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
- energized mature-cluster light and residual purple ray behavior;
- Obsidian Spike trap.

Loaded Core Boosters relay active Beacon columns. Glass provides Diffusion, Quartz Radiance, Amethyst Resonance, Diamond Focus and Netherite Inversion. Up to four loaded Boosters contribute; extra Boosters remain physical but do not add further relay modifiers. Only an energized Mature Crying Obsidian Cluster emits the slower-decay world-light field; younger energized stages remain optical-only. dev.69 preserves the six axial half-decay branches, adds bounded Glass-driven face-diagonal diffusion, makes Quartz the dominant reflected-range amplifier, lets Diamond trade diffusion for focused axial reach, and keeps Amethyst/Netherite as reflected-ray dynamics rather than generic extra range. Fully opaque geometry terminates downstream node placement and partial light blockers add attenuation before Minecraft's normal block-light/AO propagation handles the final local shading. The Core Booster optical modifier begins at `8.5/16` inside the Booster, and bud beam continuation begins at stage-specific silhouette heights. dev.66's reusable `LightProfile` contract remains the architectural base for future concentrated/static-directional/rotating-directional profiles.

## Current stabilization point

The immediate P0 issue remains semi-transparent Entity composition. dev.62 restored the late world transform and projection-local depth handling. Live dev.63 QA proved that normalizing alpha across Create's two synthetic diving-armor surfaces was insufficient because the projected body remained another translucent surface underneath. dev.64 therefore treats the Netherite Backtank chestpiece as one projected outer surface during Ghost rendering: the synthetic inner diving layer is discarded only while transparency is active, the outer diving layer uses the requested Ghost opacity with late depth stabilization, and the separate tank geometry remains untouched. Windows build/in-game acceptance is still required.

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
- `docs/CHANGELOG.md`
- `docs/DEVELOPMENT.md`
- `docs/THIRD_PARTY_NOTICES.md`
- `docs/NEXT-CHAT-HANDOFF-dev65.md`
- `docs/NEXT-CHAT-HANDOFF-dev66.md`

Superseded documentation is retained under `docs/archive/pre-dev59/` and `docs/archive/post-dev59/` for historical/migration archaeology only. It is not current authority.

## Build

On Windows, run `build.bat` with Java 21 available. dev.69 is a source candidate until Windows compilation and in-game occlusion/reflection QA pass. Live QA has already confirmed the Create Netherite Backtank can fade completely under Ghost; the new light-field work must not regress that renderer path.

Release/runtime caches and generated directories are intentionally excluded from source snapshots.
