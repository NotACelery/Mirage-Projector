# Mirage Projector — Item / Armor Snapshot Contract

Status: **dev.17: Item snapshots are non-consuming; standalone armor uses equipped geometry; Humanoid Entity has a bodyless six-channel render rig; Item/Entity/Humanoid now share a projection-local Tint/Ghost buffer path. Pose presets, pose-aware clearance and final glint/custom-RenderType QA remain pending.**

This document freezes the ownership, identity and rendering rules for 3D object sources so later Effigy / Prism / multi-source work does not accidentally turn the projector into item storage.

## 1. Core rule

A Mirage Projector displays **snapshots of objects**, not the objects themselves.

```text
real ItemStack
    |
    | capture
    v
Virtual Snapshot Slot
    |
    +--> 1-count serialized ItemStack copy
    +--> Mirage snapshot UUID
    
real ItemStack remains with player
```

The snapshot exists only to reconstruct the visual state of the source. It is not an inventory entry and it must never become an obtainable duplicate.

## 2. Why the snapshot has its own UUID

Minecraft does not guarantee a universal UUID for every arbitrary ItemStack. Mirage therefore generates a **projection snapshot UUID** when an object is captured.

The UUID identifies that visual capture, not the original physical item.

Consequences:

- recapturing the same sword creates a new snapshot UUID;
- replacing one armor slot changes only that slot's snapshot UUID;
- a future Effigy/loadout preset may have its own compound UUID in addition to per-slot UUIDs;
- no renderer should attempt to find the original item later by scanning inventories for a matching UUID.

A snapshot is self-contained render data.

## 3. Item Mode — implemented in dev.12

The old physical Item Slot becomes a **Virtual Snapshot Slot**.

### Capture

- click an ItemStack onto the virtual slot: copy one item into snapshot data;
- shift-click a normal non-Core item: capture it without consuming/moving it;
- number-key / offhand swap into the virtual slot: capture the referenced stack;
- source stack count, slot and ownership stay unchanged.

### Clear

- click the populated virtual slot with an empty cursor;
- shift-click/throw on the virtual slot can clear the snapshot;
- clearing deletes only the visual snapshot.

### Forbidden inventory behavior

The virtual snapshot:

- cannot be picked up;
- cannot be cloned from the slot in Creative;
- cannot be dragged into other slots;
- cannot be piped/hopper-extracted;
- cannot be used in crafting;
- does not drop when the projector is broken;
- never counts as a real item stored by the block.

The physical **Projection Core** remains a real inventory object and still drops normally.

## 4. Snapshot content

Mirage copies a one-count ItemStack and therefore keeps the ItemStack state that survives normal serialization.

Expected preserved visual state includes, when represented by ItemStack components:

- enchantments / glint inputs;
- trims;
- leather dye;
- custom name;
- damage state;
- custom model data/components;
- mod-defined serializable components;
- block/item identity.

A modded renderer that depends on external runtime state not serialized in the ItemStack may require a compatibility adapter or fallback.

## 5. Snapshot is frozen, not live-linked

After capture, the projector does not track the original object.

Example:

1. capture a damaged trimmed Netherite Chestplate;
2. repair it and change its trim on the real item;
3. the projection still shows the old captured state;
4. recapture to refresh the projection.

This is intentional. It makes the projector deterministic and prevents inventory scanning / ownership tracking.

## 6. Security model

The snapshot system solves **item theft from the projector**:

- another player can open the GUI;
- there is no real armor, sword, shield, tool or block inside to steal;
- the owner can immediately re-equip/use the original item after capture.

It does **not** by itself solve display vandalism. Until permissions/ownership locking exist, another player may still replace or clear the visual configuration. That is a separate access-control feature and must not be conflated with item ownership.

## 7. dev.11 migration

Pre-dev.12 development worlds physically stored one projected ItemStack.

Migration behavior:

- create a render snapshot from the old stored stack;
- keep the old real stack as a hidden **legacy return item**;
- when that projector is broken, the legacy real stack drops once;
- new dev.12 captures never create legacy return items.

This prevents development-world equipment loss while moving to the non-storage architecture.

## 8. 3D Item rendering contract

A captured Item snapshot is one **volumetric 3D object**.

- BlockItem -> full block/model geometry;
- sword/tool/shield/trident -> full item geometry;
- modded baked model -> normal modded model where compatible;
- rotation rotates the whole object continuously;
- Lift and Floating transform the whole object;
- multi-face projector geometry does not split a single item into North/East/South/West faces.

### Standalone armor special case

A captured helmet/chestplate/leggings/boots in Item Mode uses its **equipped geometry**, not an inventory sprite or generic hand transform.

From dev.16 onward this is implemented through the same invisible humanoid equipment rig used by Humanoid Entity Mode. Ordinary blocks/tools/items continue through their full 3D ItemRenderer model. dev.17 also changes the Item GUI preview to the same volumetric/equipped semantics instead of a flat inventory icon.

## 9. Armor / Effigy Mode — frozen future data model

Armor Mode uses an invisible humanoid rig with six virtual snapshot channels:

```text
HEAD
CHEST
LEGS
FEET
MAIN_HAND
OFF_HAND
```

Each channel stores:

```text
snapshot UUID
1-count render-only ItemStack copy
```

No channel owns the real item.

Example display:

```text
Head:      trimmed Netherite Helmet
Chest:     trimmed Netherite Chestplate
Legs:      trimmed Netherite Leggings
Feet:      trimmed Netherite Boots
Main Hand: Netherite Hoe
Off Hand:  Netherite Block
```

After capture, all six real objects may be immediately re-equipped or removed from the GUI. The Effigy continues to show the frozen captured appearance.

### Equipped-loadout capture

Armor/Effigy UI should also expose a **Capture Equipped Loadout** action. It snapshots the player's current Head, Chest, Legs, Feet, Main Hand and Off Hand directly into the six virtual channels in one server-authoritative operation. It must not unequip, move, lock or alter any real ItemStack.

This is the preferred workflow for displaying a complete actively-used gear set: capture it while worn/held, close the projector and continue using the exact same equipment. Individual virtual channels remain independently replaceable afterward.

## 10. Armor slot validation

Future virtual slots should guide the user without becoming unnecessarily restrictive:

- Head/Chest/Legs/Feet prefer equipment valid for that body slot;
- Chest must allow compatible alternatives such as Elytra where the equipment pipeline supports them;
- Main/Off Hand accept any renderable ItemStack: swords, shields, tools, blocks, modded items, etc.;
- invalid/unrenderable custom cases should fail visibly or fall back, never consume an item.

## 11. Pose is separate from snapshots

Pose data belongs to the virtual mannequin, not to the six ItemStack snapshots.

Changing pose must not require recapturing equipment.

Planned pose controls include presets equivalent in spirit to Armor Stand redstone poses, such as:

- Neutral;
- Guard;
- Hero;
- Combat;
- Raised Main Hand;
- Raised Shield;
- Dual Wield;
- future custom poses.

The whole rig then receives Mirage-wide transforms:

- Scale;
- Projection Lift;
- Rotation;
- Floating;
- Ghost Effect / Transparency;
- lighting/presentation controls where compatible.

## 12. Ghost Effect for 3D snapshots

Required final behavior:

Ghost Effect must apply to the **entire 3D scene**, including:

- armor base layers;
- armor trims;
- dyed layers;
- enchantment glint;
- Elytra/equipment layers;
- items in both hands;
- compatible modded equipment layers.

This is not safe to implement with one global shader alpha. It needs an alpha-aware render-buffer path that does not leak state into other world rendering.

From dev.17 the shared path exists as `ProjectionRenderBuffers`:

- Image/Plane Ghost Effect keeps its existing direct projection-vertex alpha path;
- Item/Entity/Humanoid/Effigy-style renders use a **projection-local** `MultiBufferSource` wrapper;
- Tint and opacity multiply per emitted vertex;
- common opaque/cutout entity and block-atlas layers are rerouted to translucent equivalents while Ghost Effect is active;
- special/custom RenderTypes fail closed to their original shader/state rather than being destructively replaced;
- no persistent global shader colour is used.

The remaining risk is **visual QA**, especially enchantment glint and custom/modded RenderTypes whose vertex format/shader does not consume a normal colour channel. Those cases must be refined with dedicated projection-aware layers if needed, not by leaking global render state.

## 13. Multi-face distinction

Image/Banner sources and Item/Armor sources must never share face semantics accidentally.

### Image/Banner

```text
2 faces: Front / Back
4 faces: North / East / South / West
```

Rotation exposes different 2D faces as the geometry turns.

### Item/Armor

```text
one 3D object / one posed 3D rig
```

Rotation rotates the model itself. A Prism does not turn a sword into four different sprites.

## 14. GUI identity / thumbnails

Images already receive individual Front/Back thumbnails.

Future object GUIs should follow the same principle:

- every virtual item/armor channel shows its own visual preview;
- show short snapshot UUID when useful for debugging/identification;
- do not rely on filenames or registry IDs alone to distinguish sources;
- a future four-face Image/Prism editor must show one thumbnail per image face;
- a future Effigy editor must show one preview per armor/hand channel plus a composed 3D mannequin preview.

## 15. Current implementation boundary

Implemented now:

- Item Mode virtual non-consuming capture;
- per-capture snapshot UUID;
- non-obtainable snapshot slot semantics;
- snapshot NBT persistence / BlockEntity sync;
- renderer consumes snapshot copy;
- no projected item drop on block break;
- dev.11 physical-item migration safety;
- GUI indicates snapshot identity and that the source item stays with the player.

Implemented additionally by dev.16:

- equipped-geometry rendering for standalone Head/Chest/Legs/Feet Item snapshots;
- six-channel Humanoid Entity GUI with incoming/projected conflict resolution;
- invisible humanoid rig renderer when the body card is absent;
- composed 3D Humanoid GUI preview driven by Projected/Active state;
- Item GUI preview uses equipped geometry for wearable armor snapshots.

Still waiting:

- pose presets/custom pose controls;
- final hand transforms/presentation tuning where vanilla defaults are insufficient;
- Elytra/special/modded equipment QA;
- Ghost/Tint 3D QA/refinement for glint and custom/modded RenderTypes that do not consume normal vertex colour;
- Humanoid clearance based on current pose/bounds;
- optional accessory/backpack compatibility adapters.

---

## 16. dev.13 terminology and Entity-system supersession

From dev.13 onward the final public/system name for the former **Armor Mode / Effigy Mode** workflow is **Humanoid Entity Mode**.

The six virtual channels remain exactly the same:

```text
HEAD
CHEST
LEGS
FEET
MAIN_HAND
OFF_HAND
```

However, their editor is now defined by `docs/ENTITY-PROJECTION-CONTRACT.md`:

- six incoming/staging channels on the left;
- six projected/active snapshot channels on the right;
- both hands exist on **both** sides;
- each incoming row has its own ✓ Apply action;
- scanned humanoid equipment never silently overwrites active projected equipment;
- removing a humanoid scan card does not remove the six equipment rows or erase active projected snapshots;
- Player/Zombie/Skeleton/etc. body snapshots are optional body sources layered onto the same invisible humanoid rig.

Future backpack/accessory/artifact slots are optional compatibility adapters and are explicitly outside the baseline Humanoid Entity Mode scope.


## dev.14 Humanoid Entity workspace integration

The virtual-snapshot rule now has a concrete editor implementation. In Humanoid Entity Mode:

- LEFT physical slots are temporary capture sources only;
- LEFT card-derived visuals are virtual Incoming snapshots;
- RIGHT cells are virtual Projected/Active snapshots only;
- applying a physical source copies one-count visual state into Mirage memory and does not consume the real item;
- `Return inserted gear` returns physical left-side sources;
- right-click on a RIGHT cell clears that projected snapshot without creating/dropping an item;
- a scanned humanoid's armor/hands are Incoming by default and require per-channel acceptance before replacing active equipment.

The baseline six channels remain Head, Chest, Legs, Feet, Main Hand and Off Hand. Backpack/accessory/artifact/trinket channels are explicitly deferred to optional compatibility adapters after the base equipped renderer is stable.
