# Mirage Projector

**Want holograms in your Minecraft builds?** Mirage Projector is a NeoForge 1.21.1 mod by **Celerbi** that turns images, GIFs, items, banners, creatures and players into customizable projections. Start with a compact projector and grow into larger or specialized displays as your build needs them.

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.244+**
- Java **21**
- Network protocol **30**

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

The main projector screen uses a fixed settings area with four tabs: Geometry, Placement, Rotation and Floating, so changing option groups never moves the source workspaces, PU panel or inventory. Geometry also owns the visual controls (lighting, Ghost/opacity and Tint). Shared controls include Scale, Lift, **Tilt**, rotation and Floating. The active projection source and projector ON/OFF state are stored independently, so turning a projector off never destroys its configured source data.

Lift is the single vertical placement control and never goes below zero. Tilt angles projections through a full ±90°, including high-mounted displays intended to be viewed from below. Mirage Prism additionally exposes **Prism Distance** for Image/Banner faces: +0 px means the four-face cross is packed exactly to its collision-safe boundary, while positive values expand the carousel without scaling its content. Tilt automatically raises that minimum only when required to keep Prism faces from overlapping. Prism Rotation remains a carousel around the projector center; independent horizontal/vertical translation is intentionally not a projector control.

Projector screens automatically expose vertical scrolling when the selected Minecraft GUI scale leaves less vertical room than the panel needs. At normal window heights the original centered layout is unchanged.

## Portable illumination and projection

The current 1.0.x expansion line now includes a placed **Mirage Light Projector**, a handheld **Mirage Lantern**, and the first handheld hologram device: **Mirage Hand Projector**. The light projector and lantern reuse the same Focus / Flood / Ambient / Off profiles and rechargeable-media contract instead of maintaining separate light engines. The lantern carries one removable Glow Dust or Light Battery stack internally, preserves its exact charge/components, drains only while held and emitting, and projects through the client-local `DYNAMIC_VISUAL` path while following the player's movement and aim.

The Mirage Hand Projector is the first portable hologram consumer. It stores one removable rechargeable cell plus one copied portable hologram profile taken from a configured placed Mirage Projector. Portable copies preserve the active source family (Image / Item / Banner / Entity) but normalize the output to a smaller compact handheld budget so moving projections stay readable without behaving like a full stationary chassis. Since 1.0.12 an explicitly enabled handheld projector remains active while stored anywhere in the player inventory, using Mirage-owned device-state sync so remote players can still see it. A Creative Battery with infinite charge is available for QA/admin/temporary game modes and has no Survival progression path.

Since 1.0.13, **Mirage Equipment** adds a dedicated Shoulder Strap + Shoulder Slot without consuming armor or offhand space. In 1.0.18 the Strap becomes the real owner of its mounted device, battery pouch and upgrades: a packed Strap can be removed/stored/swapped while retaining its contents, and the right-side inventory panel exposes only the slots currently unlocked. The base Strap has six power-cell slots plus two upgrades; **Shoulder Strap Slot Expansion** raises that to nine cells plus a third upgrade. Auto Battery Swap remains shoulder-only. Since 1.0.15 the directional Charging Station provides four queued inputs, one active charger, four outputs and front-face logistics; 1.0.18 tightens it to incomplete normal rechargeable media, enlarges its GUI and visualizes queued/charging/output stacks in-world. Since 1.0.16 Banner-profile Hand Projectors can use the smaller pole-free overhead War Banner presentation with Directional or per-viewer billboard facing.

Since 1.0.17, the **Mirage Scan Codex** stores multiple distinct frozen entity captures in a server-backed persistent library. Its browser synchronizes summary metadata only, with search, filters, favorites and exact scan selection; 1.0.18 makes that browser non-pausing and removes the blurred/dim background pass. Physical Entity Scan Card duplication remains reserved for the later Duplicating Lectern workflow.

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

**1.0.18** is the current implementation snapshot. The 1.0.x expansion line now includes rechargeable Glow Dust/Light Batteries, GUI-configured placed/handheld Mirage lighting, persistent portable hologram projectors, packed Shoulder Straps with Shoulder Device/Battery Pouch/upgrades, the directional Beacon Charging Station, War Banner presentation and the persistent **Mirage Scan Codex** library. The Codex stores full frozen scans server-side, exposes a metadata-only searchable/filterable/favoritable browser and persists exact scan selection for the future Duplicating Lectern. Final 1.1.0 work still includes scan-card duplication, remaining projector chassis/anchor semantics, End Resonance, recipes/balance and release polish. The UV ecosystem and direct grab/free-rotate hologram interaction belong to **1.2.0**.
