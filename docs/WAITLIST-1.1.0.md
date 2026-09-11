# WAITLIST — 1.1.0 Portable Illumination, Capture & Projection Expansion

1.1.0 is intentionally a large expansion. It grows Mirage from fixed projectors into a portable illumination/capture/projection ecosystem.

## A. Dynamic/Mobile Mirage Light Engine

- `DYNAMIC_VISUAL` backend for moving sources without rebuilding authoritative server block-light fields every frame.
- player/item/entity-attached source transforms;
- directional cone/spot and wider flood profiles;
- profile-controlled open decay, detour policy, radius and shape;
- update cadence, culling and multiplayer visibility appropriate to moving emitters;
- no contamination of the static Mature Cluster backend.

## B. Rechargeable Glow Dust battery ecosystem

Glow Dust receives a committed gameplay role in 1.1.0; older docs saying it has no role are historical.

- Glow Dust stores charge and can be partially discharged.
- depleted state has a visibly dull/opaque variant of the vanilla-inspired dust sprite.
- item tooltip exposes charge percentage/status.
- lantern HUD/hotbar feedback shows device mode/name + battery percentage; empty battery reads `Sin Cargar` / `Discharged`.
- Glow Dust is consumed as **charge**, not destroyed as a material; depleted dust remains rechargeable.
- Core Booster + active Beacon beam can recharge Glow Dust.
- each actively charging dust attenuates the outgoing beam by 20%; target limit is five simultaneous dust charges per beam path.
- Glow Dust may serve as a very-low-power projection Core/energy medium, but hologram use must not consume the dust itself the way lantern discharge consumes stored charge.

## C. Lantern family / modes

Portable hand-held lights toggle with right click and expose mode cycling without requiring placement.

Planned modes:

- **Focus** — long, concentrated forward beam; high central intensity, strong lateral falloff, higher battery draw.
- **Flood** — broader lower-intensity illumination; shorter/softer range and reasonable decay.
- **Ambient** — player-following torch-like field with enhanced useful reach; profile may repeat high levels less aggressively than Mature while remaining omnidirectional/local.
- **Off** — no light and no drain.

The exact Focus kernel (3×3/cross/corners), ranges and charge-per-second are balance/QA values, not hard-coded architectural assumptions.

## D. Mirage Scan Codex

- Scan living entities/Players into a persistent library.
- categories, search, filters, favorites and preview metadata.
- **multiple independent captures of the same species/type are allowed**. Example: baby Zombie in full Gold and a different baby Zombie in full Diamond remain separate snapshots with their own identity/data.
- entries freeze the same projector-facing entity information required to reproduce the appearance later.
- the library is not a binary "species unlocked" Pokédex.

## E. Duplicating Lectern / scan-copy station

Flow:

```text
Entity -> Scan Codex entry
Codex entry + Paper -> Duplicating Lectern / copy station
                     -> physical Entity Scan Copy/Card
                     -> projector card/source workflow
```

- selecting one stored capture chooses exactly which snapshot is printed;
- Paper is consumed; Codex entry remains;
- repeated copies do not require finding/scanning the original entity again;
- keep the current physical Entity Scan Card/copy as the projector-facing interoperability format where practical.

## F. Portable projector family

### Handheld projector

- visually descended from the lantern with a larger lens;
- Image/Item/Entity/Map-style content as supported by the final source registry;
- **no Blueprint source**;
- moving projection with reduced range/size compared with fixed projectors;
- substantially higher Glow Dust charge consumption than a lantern;
- default minimum Ghost of about 10% to communicate "portable/lite" instability and reduce deceptive PvP readability;
- works while the player moves/aims.

### Horizontal/table projector

- intended for floor/table placement;
- Shift + empty-hand interaction may pick it back up, following the portable-placement usability model;
- projects a liftable horizontal plane for maps, planning surfaces, tablecloth-like images and decorative ceiling projection when Lift is raised high enough;
- this chassis is the preferred future host for 3D Blueprint content from the Create bridge.

### Wall / data-show projector

- presentation-oriented wall projection;
- ordered multi-slide image presentation;
- source-agnostic slide architecture so optional registered content can appear between image slides later;
- eventual Create bridge may insert an interactive Blueprint slide without teaching the base mod about Create.

## G. Presentation/source architecture requirements

- slides reference generic projection-source descriptors, not image-only filenames;
- compatible chassis decide which source types can be selected;
- handheld projectors explicitly reject Blueprint capability even if the addon is installed;
- portable/fixed power models share presentation/source contracts without pretending Core PU and battery charge are identical resources.
