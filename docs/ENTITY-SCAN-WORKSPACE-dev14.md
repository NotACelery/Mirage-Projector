# Mirage Projector dev.14 — Empty Scan Template + Entity Workspace

This document is the implementation handoff for the entity-projection work introduced in `0.1.0-dev.14`. The normative product rules remain in `ENTITY-PROJECTION-CONTRACT.md`.

## 1. Player workflow

1. Craft one **Empty Scan Template** using eight Iron Nuggets around one Paper.
2. Hold the template and **Shift + right-click** one LivingEntity/Player.
3. The item becomes `Entity Scan: <display name>` and receives a new Mirage scan UUID.
4. Put the populated scan into the center slot of **Mirage Entity Workspace**.
5. Mirage copies the body snapshot into projector-owned state. The physical card remains only a staging/reference item.
6. Editable scanned equipment appears on LEFT Incoming.
7. Use the ✓ beside each desired channel to send that snapshot to RIGHT Projected.
8. If RIGHT already differs, explicitly Replace or Cancel that one channel.
9. Right-click a RIGHT virtual cell to remove only that hologram equipment piece.
10. Put real equipment on LEFT when a custom replacement snapshot is desired, ✓ it, then use `Return inserted gear` to reclaim the real item.

## 2. Data ownership

| Data | Physical? | Lootable? | Persists in projector? |
|---|---:|---:|---:|
| populated scan template in center | yes | yes | while staged |
| imported entity body snapshot | no | no | yes |
| card-derived Incoming equipment | no | no | yes |
| physical LEFT staging gear | yes | yes | yes until returned/broken |
| RIGHT Projected equipment | no | no | yes |
| Core | yes | yes | yes |

Breaking the projector returns real physical contents once. Virtual appearance snapshots never produce drops.

## 3. Humanoid channel mapping

Fixed baseline order:

1. Head
2. Chest
3. Legs
4. Feet
5. Main Hand
6. Off Hand

Both LEFT and RIGHT use all six. Removing the Humanoid card does not destroy these channels or their projected snapshots. A body-less invisible humanoid rig remains a supported target state.

## 4. Conflict semantics

`Apply` with empty RIGHT: accept immediately.

`Apply` with visually equivalent RIGHT: no destructive action required.

`Apply` with a different RIGHT: return Conflict; UI shows explicit Replace/Cancel. Replacement affects exactly one channel.

No scanned loadout gets blanket overwrite authority.

## 5. Horse special state

Horse context exposes Saddle + Body Armor only. These controls are not persistent baseline Humanoid rows. Horse virtual state is cleared on context teardown.

Physical Horse staging is protected: the scan card cannot be removed normally while Saddle/Body real items are staged. The player must return them first. This guarantees a dynamic GUI cannot hide a real stored item.

## 6. Generic entity state

Generic entities expose the frozen body only. Mirage must not infer arbitrary editable equipment slots from visual appearance. Special modded back/accessory systems are future opt-in adapters, not baseline behavior.

## 7. Nameplate

The scan records projection text only when:

- the source is a Player → player name; or
- a non-player has a Custom Name → that custom name.

Unnamed mobs get no under-projection caption. Vanilla overhead custom-name tags are stripped from the frozen body to prevent a future double label.

Actual world text rendering is pending the Entity world renderer.

## 8. Preview

The dev.14 screen reserves the exterior right-side preview region and reads active body/nameplate state, but does not yet pretend to have final entity rendering. The next renderer wave must create a client-only preview entity, restore safe visual state, equip RIGHT Projected snapshots, auto-fit by bounds and clip to the preview rectangle.

## 9. Safety / invalidation rules

- Reject source entity if it is a passenger or has passengers.
- Cap stored entity visual NBT.
- Strip position/motion/UUID/passenger/player-inventory/brain and other non-display state.
- Never use source UUID as a runtime dependency.
- Network actions validate nearby projector position on server.
- Dynamic Horse controls cannot delete physical staging.
- Virtual equipment has Mirage-owned snapshot UUIDs.

## 10. Next implementation queue

P0: client-only entity reconstruction + fitted 3D preview.

P0: equipped armor renderer that preserves trims/dyes/glint/modded equipment hooks and can be shared with standalone armor Item Mode.

P1: world Entity/Humanoid projection, under-projection nameplate, poses and base animations.

P1: alpha-safe Ghost Effect for the entire 3D scene and pose/model-aware clearance.

Later: optional backpack/accessory/artifact/trinket adapters.

Far future idea only: Entity Catalog / Scan Binder.


## dev.15 clarification — card/body lifetime

Dev.15 tightens the body lifetime rule without changing the six Humanoid channels documented above. Removing a Humanoid scan card clears the card-supplied body but keeps Humanoid Incoming/Projected snapshots; the GUI therefore becomes a bodyless virtual mannequin. Removing Horse context clears its body and Horse-only virtual Saddle/Body state after any physical staging has been safely returned.
