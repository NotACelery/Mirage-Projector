# Mirage Projector

**Want holograms in your Minecraft builds?** Mirage Projector is a NeoForge 1.21.1 mod by **Celerbi** that turns images, GIFs, items, banners, creatures and players into customizable projections. Start with a compact projector and grow into larger or specialized displays as your build needs them.

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.244+**
- Java **21**
- Network protocol **27**

EMI and JEI are optional. When installed, Mirage Projector exposes its custom projector-upgrade recipes and Crying Obsidian guidance directly in the recipe viewer.

## Projector chassis

| Chassis | Nominal projection geometry | Lift | Float | PU multiplier |
|---|---:|---:|---:|---:|
| Mirage Projector | 10×10 | 32 | 4 | ×1.00 |
| Mirage Display | 32×32 | 48 | 12 | ×1.50 |
| Mirage Field Projector | 128×128 | 144 | 24 | ×4.00 |
| Wide Mirage Projector | 80×32 | 64 | 12 | ×2.00 |
| Tall Mirage Projector | 32×80 | 96 | 16 | ×2.00 |
| Mirage Prism | 48×48 baseline | 96 | 12 | ×2.00 |

The nominal envelope is an efficiency target rather than a hard render cap. Sufficient Projection Power can push a projector into Overdrive.

Crafting progression preserves the projector's stored state:

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

Supports PNG, JPG/JPEG, static WebP, BMP and animated GIF. Wide and Tall can use optional four-source image layouts, Prism supports independent cardinal faces, and Field uses one continuous large plane. Imported assets are content-addressed and synchronized through the server/client asset pipeline.

### Item

Stores a virtual serialized snapshot; the real inventory item remains with the player. Blocks use volumetric block/item rendering where applicable, while ordinary items use Minecraft's item renderer.

### Banner

Copies banner appearance virtually without consuming the real banner. Plane chassis render banner cloth without a physical pole; Prism supports independent cardinal banner faces.

### Entity

Entity Scan Cards store frozen visual data rather than live ticking entities. Supported state includes generic living entities, Players, Humanoid equipment, bodyless equipment rigs, Horse Saddle/Body Armor, custom names, pose presets and per-channel equipment visibility. Passenger/vehicle composite scans are intentionally rejected until a dedicated format exists.

## Presentation controls

Shared controls include Scale, Lift, rotation, rotation period/direction/offset, Floating, float amplitude/timing, world lighting or Fullbright, Ghost/opacity, Tint, image flip and scanlines. The active projection source and projector ON/OFF state are stored independently, so turning a projector off never destroys its configured source data.
Tall projector workspaces automatically expose vertical scrolling when the selected Minecraft GUI scale leaves less vertical room than the panel needs. At normal window heights the original centered layout is unchanged.

## Projection Power

| Core material | Base PU | Core Booster amplification |
|---|---:|---:|
| Glass | 32 | ×1.50 |
| Quartz | 48 | ×1.50 |
| Amethyst | 64 | ×1.50 |
| Diamond | 96 | ×1.50 |
| Netherite | 128 | ×1.50 |

Effective capacity is:

```text
floor(Base PU × chassis multiplier × Core amplification)
```

A `Core Booster` accepts Glass, Quartz, Amethyst Shard, Diamond or Netherite Ingot. Loaded Boosters retain their material when correctly mined and provide material-specific Beacon/Mirage-light behavior. Empty Boosters are not valid projector Cores.

## Crying Obsidian ecosystem

Version 1.0.0 includes:

- Crying Obsidian Shards and shard-based Crying Obsidian crafting;
- renewable downward crystal growth from Crying Obsidian with lava directly above;
- Small → Medium → Large → Cluster growth stages;
- Silk Touch preservation and fixed shard drops without Fortune multiplication;
- Beacon interaction and material-specific Core Booster relay effects;
- energized Mature Cluster Mirage lighting;
- residual purple optical rays;
- Obsidian Spike trap blocks.

The static Mirage Light Engine is server-authoritative. Clients receive resolved per-chunk Mirage light sections and combine them with vanilla block light at read time using `max(vanilla, Mirage)`. Static Mirage values are never fed back into vanilla propagation as new emitters.

## EMI / JEI

With EMI installed:

- all six projector chassis expose their crafting progression;
- Crying Obsidian exposes both shard-based crafting variants;
- the Crying Obsidian growth mechanic has an icon-only World Interaction tutorial;
- crystal Block Drops are ordered Small → Medium → Large → Cluster.

With JEI installed, custom projector-upgrade recipes are exposed through the normal Crafting category and Crying Obsidian/projector guidance is available through ingredient information.

## Documentation

Start with:

- `docs/CURRENT-IMPLEMENTATION.md` — stable 1.0.0 behavior;
- `docs/ARCHITECTURE.md` — runtime architecture and extension boundaries;
- `docs/REGISTRY-INVENTORY.md` — canonical registry surface;
- `docs/MIRAGE-LIGHT-ENGINE.md` — static Mirage Light architecture;
- `docs/ROADMAP.md` — post-1.0 roadmap;
- `docs/QA-REGRESSION.md` — reusable regression gates.

Historical development notes and handoffs are preserved under `docs/history/`; they are not current product authority.

## Build from source

On Windows, run `build.bat` with Java 21 available. The project targets NeoForge 21.1.244 and uses the version declared in `gradle.properties`.

Before release/build handoff, run:

```text
python tools/verify_current_line.py
```

The verification suite checks the current registry/resource surface, Mirage Light regression contracts, projector state/serialization architecture, item presentation, optional EMI/JEI integration and release metadata.

## Version scope

**1.0.0** is the stable fixed-projector release. Portable lanterns/projectors, rechargeable Glow Dust, Scan Codex and Dragon Egg / End Resonance gameplay belong to **1.1.0**. Direct grab/free-rotate hologram interaction belongs to **1.2.0**.
