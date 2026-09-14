# WAITLIST — 1.1.0 Portable Illumination, Capture & Projection Expansion

1.1.0 is intentionally a large expansion. It grows Mirage from fixed projectors into a portable illumination/capture/projection ecosystem.

## Pre-1.1 groundwork already delivered in 1.0.x

The fixed-projector placement foundation is no longer a 1.1.0 task. The 1.0.x line provides Lift as the single vertical axis, full ±90° quaternion-backed Tilt, reset controls, and clearance/culling support. Mirage Prism additionally owns collision-safe radial Distance for Image/Banner faces; +0 px is the tight safe carousel baseline and larger values expand the cross without changing content scale. This is a Prism chassis capability, not a universal translation contract. Every new 1.1.0 chassis should explicitly declare which placement capabilities it supports.

Remaining placement architecture for 1.1.0:

- freeze a formal **anchor/pivot semantic** for portable, wall, table and ceiling chassis;
- decide whether the table/horizontal chassis also covers ceiling mounting or whether a dedicated ceiling projector is required;
- make source selection UI enumerate compatible registered sources/capabilities instead of permanently hard-coding the four 1.0 built-ins;
- define the optional Blueprint/Create bridge capability contract explicitly: Table/Horizontal is the preferred 3D host, Wall may expose it as a presentation source where appropriate, and Handheld must reject it.

## A. Dynamic/Mobile Mirage Light Engine

Foundation delivered in **1.0.5**:

- operational client-only `DYNAMIC_VISUAL` fields that never publish into authoritative `STATIC_WORLD`;
- moving-source snapshots with stable identity, position, profile, solve cadence, camera culling and stale cleanup;
- UUID/key source identities suitable for future player/entity/device attachments;
- shared voxel-solver support for `DIRECTIONAL_CONE`;
- generic ambient/directional dynamic profile factories;
- render-section invalidation and retry of fields clipped by temporarily unavailable chunks.

Still required before 1.1.0:

- actual lantern/item/entity consumers that submit moving transforms;
- final Focus/Flood/Ambient profile balance and shape tuning;
- remote-player/device synchronization and visibility policy;
- geometry invalidation for nearby block changes while a stationary dynamic emitter remains active;
- battery/charge integration and performance QA under many simultaneous moving emitters.

## B. Rechargeable Glow Dust battery ecosystem

Foundation delivered in **1.0.7**:

- Glow Dust stores persistent partial charge and can be discharged/recharged without consuming the material.
- depleted/partial state visibly darkens the vanilla-inspired dust silhouette; tooltip and item bar expose charge status.
- Core Boosters own a separate one-cell charging cradle while retaining their normal Core-material socket.
- an active Beacon column recharges inserted Glow Dust over time.
- each actively charging cell subtracts 20 percentage points from outgoing beam transmission; a clear column supports at most five simultaneous chargers.
- Crying Obsidian optics and the custom Beacon renderer consume the same attenuated transmission.

Still required before 1.1.0:

- final survival recipe/progression for Glow Dust;
- lantern HUD/hotbar feedback with device mode/name + battery percentage; empty battery reads `Sin Cargar` / `Discharged`;
- actual device discharge/consumption policy and final charge-rate balance;
- optional very-low-power projection-energy adapter if Glow Dust is accepted as a projection medium, without consuming the dust item itself.

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

## H. Dragon Egg / End Resonance Easter Egg

### Product intent

`End Resonance` is a deliberately hidden 1.1.0 Easter egg that gives the vanilla Dragon Egg a rare, world-significant use without turning it into a normal progression material.

The Dragon Egg acts as a **special resonance Core**, not as a normal PU Core and not as a consumable ingredient. Because a survival world normally exposes only one naturally obtained Dragon Egg, the scarcity of the item naturally makes an End Resonance installation feel unique/community-significant without Mirage enforcing an artificial one-per-world rule.

This feature is **not a 1.0.0 blocker**. It belongs to 1.1.0 after the generalized energy/source abstractions are established.

### Compatible chassis

Only these fixed chassis accept a Dragon Egg as an End Resonance Core:

- **Mirage Field Projector** — creates a planar End aperture.
- **Mirage Prism** — creates a volumetric End anomaly.

The following must reject Dragon Egg resonance mode:

- Mirage Projector / Compact;
- Mirage Display;
- Wide Mirage Projector;
- Tall Mirage Projector;
- handheld/portable projector family;
- any future chassis that does not explicitly expose the `END_RESONANCE` capability.

### Field behavior — planar aperture

When a Dragon Egg is inserted into a Mirage Field Projector:

- suspend the normal projection source;
- render a fixed **2 blocks wide × 3 blocks tall** vertical End aperture;
- the aperture is a 2D plane, not a volumetric box;
- orientation follows the projector/chassis facing chosen at activation time;
- no obsidian frame is created and no vanilla End Portal blocks are placed in the world;
- the visual language should reuse/recreate the characteristic depth, star-field/parallax and mystical appearance of the vanilla End portal while remaining a Mirage-rendered effect;
- crossing the active plane transfers compatible entities through the aperture.

Design identity: **Field = doorway**. It should read as a deliberate, practical dimensional gate created by Mirage technology.

### Prism behavior — volumetric anomaly

When a Dragon Egg is inserted into a Mirage Prism:

- suspend the normal Prism projection;
- render a fixed **2 × 3 × 2 block** End Resonance volume;
- the volume should visually read as a portal cube/chamber rather than a flat portal repeated four times;
- exterior faces should use the End-portal visual language so walking around it preserves the impossible/deep spatial effect;
- no physical vanilla End Portal blocks are placed;
- any compatible entity whose bounding box meaningfully enters the active resonance volume may trigger dimensional transfer.

Design identity: **Prism = anomaly**. It should feel more mysterious and spatially impossible than the practical Field doorway, as though the Prism is forcing open a small three-dimensional region connected to the End.

### Dragon Egg Core semantics

The Dragon Egg is a special resonance catalyst:

- it is **never consumed**;
- it does not convert into PU and does not participate in normal Core-capacity arithmetic;
- it should not be accepted as a generic recipe material for this feature;
- it should render visibly as the installed Core/resonance object where practical;
- Prism/Field emitters may use a special intensified violet/resonance visual state while the egg is installed;
- automation that inserts/removes the egg must execute the same activation/restoration contract as direct player interaction;
- state-copy/upgrade/crafting flows must never duplicate the physical Dragon Egg.

Architecturally this belongs under a generalized special-energy/resonance contract rather than an `if dragon_egg` exception scattered through projector logic. Conceptual future-facing shape:

```text
Energy / activation providers
├── PROJECTOR_CORE        -> normal PU
├── GLOW_DUST             -> rechargeable portable energy
└── SPECIAL_RESONANCE
    └── DRAGON_EGG        -> END_RESONANCE
```

Exact class/interface names are implementation details; the separation of semantics is the requirement.

### ProjectionState snapshot and restoration

Activation must be completely non-destructive to the projector's previous configuration.

When the Dragon Egg is inserted:

1. serialize/copy the complete current projector-facing state into an `End Resonance restore snapshot`;
2. preserve all source-specific state necessary to reproduce the previous projection exactly;
3. enter `END_RESONANCE` mode;
4. suspend the normal projection without mutating its saved settings.

The restore snapshot should include, where applicable:

- selected source family and source identity/data;
- scale;
- lift;
- float mode/amplitude/phase-relevant configuration;
- rotation/orientation settings;
- tint/colour state;
- Ghost/opacity state;
- image workspace configuration and selected banks/faces;
- Item/Banner state;
- Entity snapshot/card selection, pose, equipment visibility and related projector-facing settings;
- any future generic `ProjectionTransform` or source descriptor fields introduced before 1.1.0.

While End Resonance is active, changing ordinary projection controls must **not** mutate either the portal or the stored restore snapshot.

When the Dragon Egg is removed:

1. immediately remove the End Resonance portal/anomaly;
2. return/preserve the exact Dragon Egg item;
3. restore the complete saved projector state;
4. resume the previous source as though End Resonance had never modified it.

The restore snapshot must persist in BlockEntity save data while the egg is installed. World save/reload, chunk unload/reload, server restart or client relog must not lose the suspended projector configuration.

### UI lock/suspension contract

While `END_RESONANCE` is active:

- Scale is locked;
- Lift is locked;
- Float is locked;
- rotation controls are locked;
- Tint/Ghost controls are locked;
- Overdrive is unavailable;
- normal source replacement/import actions are unavailable;
- Entity tabs/workspaces are blocked;
- controls that could mutate the suspended source are disabled/read-only rather than silently editing hidden state.
- the normal projector ON/OFF state is overridden by End Resonance for as long as the Dragon Egg remains installed;
- `TURN OFF` remains visible/attemptable as deliberate feedback, but **must not** disable End Resonance, stop the aperture/anomaly, alter the restore snapshot or change the suspended source;
- pressing `TURN OFF` while the Dragon Egg is installed produces the event message exactly: `This doesn't seem to work...`;
- normal shutdown/source activation controls regain authority only after the Dragon Egg is physically removed and the suspended ProjectionState is restored.

The GUI should visibly switch to a resonance-specific status panel rather than looking broken. Suggested information hierarchy:

```text
END RESONANCE
Projection parameters suspended.

Dragon Egg resonance detected.
```

Chassis-specific status can expose:

```text
FIELD
Aperture: PLANAR
Dimensions: 2 × 3
```

or:

```text
PRISM
Aperture: VOLUMETRIC
Dimensions: 2 × 3 × 2
```

The text is UX guidance, not frozen final localization copy.

### Dimensional transfer behavior

End Resonance is intended to be a **functional portal**, not only a shader/decorative projection.

Baseline destination semantics should follow the intuitive vanilla End-portal loop where practical:

- outside The End -> transfer to The End;
- from The End -> return toward the Overworld/appropriate vanilla-style return destination.

The implementation must use Mirage's own portal collision/transfer envelope; do not place hidden vanilla `end_portal` blocks merely to obtain teleportation.

Compatible transferable entities should intentionally include more than players:

- players;
- living mobs/animals;
- dropped item entities;
- compatible projectiles where vanilla dimensional transfer rules permit them;
- boats/minecarts or other vehicles when their transfer semantics are safe and well-defined;
- **Primed TNT**, preserving its active fuse state through transfer where vanilla entity serialization/teleport semantics allow it;
- **FallingBlockEntity / gravity-block entities**, enabling technical transport setups involving sand, red sand, gravel, concrete powder, anvils and other gravity-driven blocks where their normal entity form is transferable.

This is intentionally useful for multiplayer infrastructure and technical play. A shared End Resonance installation can become a central world transport point, mob/item routing endpoint or component in technical contraptions.

Mirage should guarantee **normal compatible entity transfer**, not a specific item-duplication exploit. If a particular Minecraft version permits a technical farm/duplication setup through vanilla falling-block or portal behavior, that remains version-dependent behavior rather than a Mirage API guarantee.

### Safety / edge cases to QA

Before 1.1.0 release, explicitly test:

- survival Dragon Egg insertion/removal without item loss or duplication;
- world save/reload while resonance is active;
- chunk unload/reload and server restart;
- projector break while the egg is installed;
- explosion/destruction/automation extraction paths;
- inventory-full extraction fallback/drop behavior;
- Prism/Field upgrade/state-transfer interactions while resonance is active;
- multiple End Resonance portals created through Creative/admin duplication;
- multiplayer simultaneous crossing;
- player, mob, item, projectile, Primed TNT and FallingBlockEntity transfer;
- entity cooldown/anti-loop behavior so an entity does not ping-pong dimensions every tick;
- vehicles/passengers only if their vanilla transfer contract is safe;
- destination collision/safe placement;
- client render cleanup after teleport, dimension change, projector removal and egg extraction;
- no Dragon Egg duplication through state copies, recipes, drops or projector cloning;
- no hidden mutation of the suspended ProjectionState while UI controls are locked.
- `TURN OFF` repeatedly returns `This doesn't seem to work...` and never changes End Resonance state while the Dragon Egg remains installed.

### Visual/FX direction

- Reuse the visual language of vanilla End Portal depth/star/parallax effects where technically appropriate, but render through Mirage so arbitrary vertical/volumetric geometry is possible.
- Do not build a Nether-style obsidian frame.
- The projector remains visibly responsible for sustaining the aperture/anomaly.
- Violet Mirage emitters/resonance accents should visually connect the Dragon Egg to the active portal.
- Field should remain readable as a clean planar gateway.
- Prism should emphasize the uncanny 3D chamber/cube effect and reward walking around it as decoration even when nobody is actively using the portal.

### Discovery philosophy

This should remain an Easter egg rather than a tutorialized progression feature:

- do not expose a normal recipe/tutorial that explicitly tells the player `Dragon Egg -> End portal`;
- avoid spelling the mechanic out in the public changelog;
- subtle handbook/lore hints may be considered later, but should preserve discovery;
- once discovered, the GUI can clearly explain the active `END RESONANCE` state and how to safely remove the egg.

### 1.1.0 changelog teaser

Preferred final-line teaser for the public 1.1.0 changelog:

> **Some projections may have an End after all...**

Use `End` capitalized exactly as written. It hints at the dimension and the projector connection without naming the Dragon Egg, Prism/Field behavior, portal geometry or teleport capability.

Fallback candidate if the preferred line feels too explicit during final release editing:

> **There might be an End to this after all...**

Do **not** add a normal changelog bullet explicitly describing End Resonance unless the feature is intentionally declassified later.

