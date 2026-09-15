# WAITLIST — 1.2.0 Interactive Holograms & UV Ecosystem

1.2.0 owns two deliberately larger feature families that are excluded from the 1.1 portable-equipment release: direct manipulation of projections and the Crying-Obsidian/UV gameplay ecosystem.

## A. Direct hologram interaction

### Primary interaction — grab + free rotate

- Aim at a hologram and hold RMB to grab/manipulate it.
- Mouse/camera movement rotates the hologram freely in arbitrary 3D orientation, deliberately allowing playful "spin it around" interaction.
- Use a stable pivot and quaternion-ready orientation model; do not accumulate unrestricted Euler deltas as authoritative state.
- releasing RMB commits/ends the manipulation cleanly.
- interaction must work with the projection's real rendered envelope rather than the physical projector block alone.

### Interaction architecture

- server-authoritative ownership/permission rules for persistent changes;
- multiplayer feedback/interpolation without fighting between simultaneous users;
- manipulation should operate on generic `ProjectionTransform`, not source-specific data;
- source types can opt into/limit manipulation through capabilities;
- rendering and hit-testing remain separate from the interaction controller.

### Potential follow-ups

- axis/pivot visualization while held;
- optional snapping (e.g. 15°/45°) without removing free mode;
- position/pivot manipulation after rotation is stable;
- source-specific handles only where genuinely useful;
- direct manipulation hooks exposed for optional addons.

## B. UV Shoulder Light

The UV Shoulder Light is a late 1.2.0 `ShoulderMountableDevice`, not a 1.1 lantern variant. It is intentionally power-hungry and combines normal Mirage portable illumination with a dedicated UV defensive/reveal mode.

### Device / recipe direction

Conceptual 3×3 recipe skeleton:

```text
I S I
? C ?
? G ?
```

Where:

- `I` = Iron Ingot;
- `S` = Crying Obsidian Shard;
- `C` = Crying Obsidian Cluster;
- `G` = Glass Block;
- `?` = additional casing/handle/optics material to freeze later.

Possible additional ingredients include more Crying Obsidian Shards for a visible dark-purple handle/casing. Copper is only appropriate if it improves the visual/material language; the feature must not invent an electricity system merely to justify conductive ingredients.

The UV device should reuse the existing Focus/Flood/Ambient light-mode geometry for normal illumination and UV targeting rather than defining unrelated shapes.

### UV energy identity

- UV consumes dramatically more rechargeable energy than normal portable light — it should feel like it is **burning through batteries**;
- Creative Battery remains infinite for QA/admin/minigame testing;
- exact drain multipliers belong to balance QA, not hard-coded assumptions in the generic shoulder framework.

## C. UV target detection / Auto UV upgrade

A future **Auto UV Patch** is a shoulder-upgrade family designed for the generic upgrade sockets established during 1.1.0.

- detect UV-vulnerable targets inside the actual active light geometry;
- target range starts around **5 blocks** for automatic defensive switching, subject to balance;
- Focus uses a narrow/strong cone;
- Flood uses a wider/weaker cone;
- Ambient evaluates a local radius around the player;
- prefer a data-driven/entity-tag or damage-tag-compatible `UV vulnerable` contract rather than scattering hard-coded Zombie/Skeleton class checks;
- Smite-vulnerable/undead semantics are the intended starting population, but exact vanilla/NeoForge hooks must be evaluated before freezing implementation;
- automatic UV switching must **not activate under direct sunlight/daylight conditions** where ordinary sunlight is already burning vulnerable undead, preventing the feature from accidentally killing mobs the player is intentionally capturing/scanning;
- when Auto UV Patch is installed and its prerequisite shoulder-upgrade configuration is present, the Shoulder Strap panel exposes an explicit **Auto UV ON/OFF toggle** near the upper device/upgrade controls;
- disabling Auto UV never removes the patch; it only suspends automatic switching.

## D. UV combat behavior

UV is defensive utility, not a primary weapon replacement.

### Focus UV

- strongest per-target UV exposure;
- narrow cone;
- highest battery drain;
- small direct damage plus the longest fire duration among UV modes.

### Flood UV

- wider cone / more targets;
- lower per-target damage/fire than Focus;
- high battery drain, but less concentrated than Focus.

### Ambient UV

- short-radius area around the player;
- low damage;
- short fire application;
- mild slow on vulnerable mobs to create breathing room rather than functioning as an AoE damage aura;
- extreme battery cost remains appropriate because it protects in every direction.

All damage/fire/slow values require explicit gameplay QA. The system must respect normal PvE/PvP damage rules and should only affect entities declared UV-vulnerable unless a future feature intentionally broadens the contract.

## E. UV Marks / hidden marking system

UV Marks are consumable hidden decals/marks attached to block faces for scavenger hunts, hidden routes, RP and creepy/decorative builds.

### Visibility

- normally invisible or effectively unreadable without UV illumination;
- revealed when a UV field reaches the marked surface;
- Focus UV should reveal them at the strongest brightness/contrast;
- Flood reveals them clearly but less intensely;
- Ambient may reveal nearby marks more softly;
- visual language may use green/purple emissive accents while under UV.

### Placement / persistence

- using a mark on a block consumes the mark item;
- the mark stores the target block position, face and orientation;
- arrow-style marks choose orientation according to the nearest relevant clicked edge/placement direction so up/down/left/right arrows are intuitive;
- breaking/replacing the host block destroys the mark permanently;
- the mark never drops itself when its host is broken;
- hidden marks must cleanly disappear when chunks/world state invalidate their host.

### Symbol library

Target families include:

- directional arrows;
- letters/numbers;
- simple geometric symbols;
- occult/creepy/RP symbols;
- components intended to combine into larger floor/wall patterns.

Do not require one completely separate runtime subsystem per symbol. Use a generic mark definition/ID with symbol-specific assets/metadata.

## F. UV marking material / authoring station

A dedicated UV-marking material and small authoring/crafting station may let the player select which symbol a consumable mark represents.

Goals:

- avoid dozens of unrelated recipes for every arrow/letter;
- provide a searchable/selectable symbol surface;
- output a real consumable UV Mark item carrying the selected symbol ID;
- preserve ordinary recipe-viewer discoverability where practical.

Exact station block, recipe and UI remain open until the mark data model is prototyped.

## G. Crying Obsidian UV resonance / projector reveal

Crying Obsidian Cluster may later act as a UV resonator/source for projectors:

- a compatible projector/core/resonance configuration can reveal nearby UV Marks without a shoulder light;
- reveal can be area-based for permanent installations, puzzle rooms, mansions, bases and RP builds;
- multiple marks can combine into large floor/wall compositions, including ritual circles/pentagram-style decoration;
- this is a reveal/light behavior, not permission for 1.1 code to treat Crying Obsidian as UV before 1.2 begins.

The exact relationship between Cluster-as-core, normal Projection Cores and special resonance providers must use the generalized energy/resonance boundary rather than ad-hoc item checks.

## H. Create-bridge relevance

Free-rotation interaction remains a prerequisite/enabler for inspecting 3D schematic holograms naturally. The core mod still does not parse Create schematics itself.

UV features are unrelated to the optional Create bridge and must not create a dependency between them.

## 1.2.0 release boundary

1.2.0 should not begin by modifying 1.1's generic shoulder framework specifically for UV. The required generic extension points — shoulder-mountable devices, rechargeable energy media and shoulder upgrade families/sockets — should already exist from 1.1. UV then plugs into those contracts as a new device/upgrade family.
