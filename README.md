# Mirage Projector

**Want holograms in your Minecraft builds?** Mirage Projector is a NeoForge 1.21.1 mod by **Celerbi** that turns images, GIFs, items, banners, creatures and players into customizable projections. Start with a compact projector and grow into larger or specialized displays as your build needs them.

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.244+**
- Java **21**
- Network protocol **44**

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
| Mirage Table Projector | 48×48 | 64 | 12 | ×1.50 |
| Mirage Wall Projector / Data-show | wall-targeted | — | — | ×1.50 |

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

The main projector screen uses a fixed settings area with four tabs: Geometry, Placement, Rotation and Floating, so changing option groups never moves the source workspaces, PU panel or inventory. Geometry also owns the visual controls (lighting, Ghost/opacity and Tint). Shared controls include Scale, Lift, **Tilt**, rotation and Floating. The active projection source and projector ON/OFF state are stored independently, so turning a projector off never destroys its configured source data.

Lift is the single vertical placement control and never goes below zero. Tilt angles projections through a full ±90°, including high-mounted displays intended to be viewed from below. Mirage Prism additionally exposes **Prism Distance** for Image/Banner faces: +0 px means the four-face cross is packed exactly to its collision-safe boundary, while positive values expand the carousel without scaling its content. Tilt automatically raises that minimum only when required to keep Prism faces from overlapping. Prism Rotation remains a carousel around the projector center; independent horizontal/vertical translation is intentionally not a projector control.

Projector screens automatically expose vertical scrolling when the selected Minecraft GUI scale leaves less vertical room than the panel needs. At normal window heights the original centered layout is unchanged.

## Portable illumination and projection

The current 1.0.x expansion line includes a floor-standing **Mirage Light Projector**, a wall-mounted **Mirage Wall Projector**, the handheld **Mirage Flashlight**, and the portable hologram **Mirage Hand Projector**. The three illumination devices reuse the same Focus / Flood / Ambient / Off profiles and rechargeable-media contract instead of maintaining separate light engines. The Flashlight carries one removable Glow Dust or Light Battery stack internally, preserves its exact charge/components, drains only while emitting, follows the player's movement/aim through `DYNAMIC_VISUAL`, and can be placed temporarily on a block top without losing its cell or selected mode.

The Mirage Hand Projector is a direct portable hologram consumer with its own physical Projection Core, rechargeable energy cell and Image / Item / Entity / Banner source controls. It normalizes output to the Compact 10 px scale budget instead of copying another projector's profile. An explicitly enabled handheld projector remains active while stored anywhere in the player inventory, using Mirage-owned device-state sync so remote players can still see it. A Creative Battery with infinite charge remains available only for QA/admin/temporary game modes and has no Survival progression path.

Since 1.0.13, **Mirage Equipment** adds a dedicated Shoulder Strap + Shoulder Slot without consuming armor or offhand space. In 1.0.18 the Strap becomes the real owner of its mounted device, battery pouch and upgrades: a packed Strap can be removed/stored/swapped while retaining its contents, and the right-side inventory panel exposes only the slots currently unlocked. The base Strap has six power-cell slots plus two upgrades; **Shoulder Strap Slot Expansion** raises that to nine cells plus a third upgrade. Auto Battery Swap remains shoulder-only. Since 1.0.15 the directional Charging Station provides four queued inputs, one active charger, four outputs and front-face logistics; 1.0.18 tightens it to incomplete normal rechargeable media, enlarges its GUI and visualizes queued/charging/output stacks in-world. Since 1.0.16 Banner-profile Hand Projectors can use the smaller pole-free overhead War Banner presentation with Directional or per-viewer billboard facing.

The handheld **Mirage Entity Scanner** is the exclusive way to capture a living entity: insert a Scan Codex, hold RMB while aiming at the target within four blocks for 1.5 seconds, and the frozen record is saved directly to that Codex. The Scanner supports underwater capture, pauses progress when aim is lost, and gives capture priority over living-entity interactions. Since 1.0.17, the **Mirage Scan Codex** stores those distinct frozen captures in a server-backed persistent library with global search, Favorites, previews and a 25-per-entity-type cap. Entity Scan Cards are transport containers rather than scanners. Mounting the Codex on a vanilla Lectern unlocks duplication into a blank Entity Scan Card and a permanent destructive import panel for filled Mirage cards; optional Easy Mob Farm capture cards are accepted when that mod is present.

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

The 1.0.x line includes:

- Crying Obsidian Shards and shard-based Crying Obsidian crafting;
- renewable downward crystal growth from Crying Obsidian with lava directly above;
- Small → Medium → Large → Cluster growth stages;
- Silk Touch preservation and fixed shard drops without Fortune multiplication;
- Beacon interaction and material-specific Core Booster relay effects;
- energized Mature Cluster Mirage lighting;
- residual purple optical rays;
- Obsidian Spike trap blocks.

The static Mirage Light Engine is server-authoritative. Clients receive resolved per-chunk Mirage light sections and combine them with vanilla block light at read time using `max(vanilla, Mirage)`. Static Mirage values are never fed back into vanilla propagation as new emitters.

Since 1.0.5, the project contains the **Dynamic Mirage Light** runtime: client-local moving fields, camera culling, update cadence and directional-cone solving. Since 1.0.7/1.0.8, rechargeable Glow Dust, Light Battery and Beacon/Core-Booster charging form the portable-energy foundation. **1.0.9** adds the first real consumer: a placed Mirage Light Projector with a rechargeable cell and Focus/Flood/Ambient/Off modes.

## EMI / JEI

With EMI installed:

- all six projector chassis expose their crafting progression;
- Crying Obsidian exposes both shard-based crafting variants;
- the Crying Obsidian growth mechanic has an icon-only World Interaction tutorial;
- crystal Block Drops are ordered Small → Medium → Large → Cluster.

With JEI installed, custom projector-upgrade recipes are exposed through the normal Crafting category and Crying Obsidian/projector guidance is available through ingredient information.

The 1.0.32 Survival-progression recipes (Light Battery, Mirage Flashlight, Light/Wall projectors, Shoulder Strap + patches, Charging Station, Hand Projector, Scan Codex, Table Projector and Wall Projector) are ordinary vanilla `crafting_shaped` recipes. Both JEI and EMI therefore expose them directly in their normal Crafting category from the shared RecipeManager rather than through duplicate Mirage-specific wrappers. The Light Battery entry expands `mirage_projector:glow_dust_media`, so the five Glow Dust slots accept both full vanilla Glowstone Dust and rechargeable Mirage Glow Dust.

## Documentation

Start with:

- `docs/CURRENT-IMPLEMENTATION.md` — current 1.0.x behavior;
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

**1.0.69** is the current implementation snapshot. It carries the tested End Resonance portal flow, Entity Scanner/Codex workflow and runtime-QA corrections toward the 1.1.0 release candidate. Network protocol is **44** and `ProjectionSettings` format remains **4**.

**1.0.24** established the current Scan Codex/Card and Hand Projector portable UI contracts. It keeps the vanilla Lectern as the physical Scan Codex workstation while replacing Paper-copy semantics with blank Entity Scan Cards, adding destructive successful imports from filled Mirage cards and optional Easy Mob Farm capture cards, expanding the Codex into a scrollable categorized detail browser, and compacting the Hand Projector into a lite portable UI whose Forward/War Banner, Directional/Billboard, size and height controls update from live device state. Reverse Mirage → Easy Mob Farm card export remains deferred; if added later its cost is measured in experience levels rather than raw XP. End Resonance is implemented and runtime-tested; the remaining 1.1.0 work is release documentation, final compatibility/QA and any explicitly retained polish. The UV ecosystem and direct grab/free-rotate hologram interaction belong to **1.2.0**.

### 1.0.34 roadmap/cleanup checkpoint

1.0.34 restores **Mirage Wall Projector** as the sole wall-projector identity (the presentation/Data-show chassis), fixes Table workspace navigation vs active-mode state, updates portable-projector empty-state wording, and performs the Flashlight/Light Projector visual and Ambient-orientation cleanup. The remaining 1.1 implementation order is frozen in `docs/NEXT-WAVES-1.1.0.md`.
