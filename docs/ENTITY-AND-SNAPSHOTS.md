# Mirage Projector — Entity, Item and Banner snapshots

## Core rule

Projected content is a visual snapshot, not inventory storage and not a live entity.

## Item snapshots

The Item workspace stores a serialized ItemStack copy with its own snapshot identity. The player's real item is not removed.

The virtual slot is non-pickup/non-draggable. Clearing it deletes the snapshot only. Historical migrations that once stored a physical item are handled separately so old worlds do not duplicate or lose it.

## Banner snapshots

Banner appearance is copied virtually. The physical banner stays with the player.

Plane chassis use one banner cloth presentation. Prism supports independent North/East/South/West virtual banner snapshots and a copy-primary-to-all operation.

## Entity Scan Card

The scan card stores a frozen representation of one LivingEntity/Player and deliberately strips unsafe/runtime-only state. It has an independent scan UUID and source UUID metadata.

Passenger/vehicle composites are rejected in the current format.

## Player appearance

Player scans preserve profile name/UUID and cached texture property/model information where available. Projection-only reconstructed players do not represent a live server player and do not tick as one.

## Humanoid equipment

Humanoid projection state has six channels:

- Head;
- Chest;
- Legs;
- Feet;
- Main Hand;
- Off Hand.

The Entity workspace separates physical Incoming staging from virtual Projected snapshots. Accepting a staged item copies the visual state and returns the physical item rather than trapping it. Leftover staging is returned safely when leaving the workspace.

A bodyless Humanoid rig can render equipment without the scanned body.

## Horse equipment

Horse-family state exposes:

- Saddle;
- Body Armor.

Switching to an incompatible entity family must clear incompatible virtual equipment so hidden old state cannot later reappear unexpectedly.

## Pose system

Humanoid, Horse and Generic pose foundations are separate. Humanoid pose presets affect the projection rig only and do not modify the source entity/card snapshot.

Preview fitting and clearance use conservative pose/species-aware bounds.

## Nameplates

Frozen custom names are preserved separately from the projection body's internal vanilla visibility state. Mirage renders the projected label at the projected entity's actual scale/pose-aware height.

## Current render stabilization

The Ghost pipeline supports tint/alpha-aware rendering for many vanilla body/equipment/item layers. Semi-transparent Entity projections are scheduled after level composition so clouds cannot be composited later over the hologram. dev.62 restores the world camera model-view for that late hook and gives the late Entity Ghost pass a projection-local depth-write contract, while Image/Banner/Item Ghost rendering remains colour-only.

Create's Netherite Backtank is a required regression target because it uses two different custom paths. `BacktankArmorLayer` renders the tank block geometry, while `BacktankItem.Layered` replaces the normal Humanoid chest-armor pass with two synthetic armor-model layers. dev.61 fixed the simultaneous base + foil consumer crash in the tank path; dev.62 stabilized the late Entity transform/depth path; dev.63 proved that pair-normalizing those two synthetic layers was insufficient because the projected body remained another translucent surface below them. dev.64 therefore keeps only the outer synthetic diving surface during Ghost rendering, with the normal requested opacity and late depth writes, while leaving 100% opacity native.

## Explicit remaining entity work

See `ROADMAP.md`. Important pending areas include:

- per-channel render visibility toggles that hide Head/Chest/Legs/Feet/Main/Off without deleting stored snapshots;
- equivalent Saddle/Body Armor visibility for Horse;
- mounted/composite/jockey projection design;
- targeted adapters for accessories/backpacks/Curios/cosmetic equipment only when generic RenderType normalization is insufficient;
- remaining renderer-specific bounds/species edge cases.
