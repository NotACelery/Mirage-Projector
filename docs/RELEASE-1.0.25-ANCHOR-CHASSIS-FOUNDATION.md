# Mirage Projector 1.0.25 — Table + Data-show Presentation Foundation

1.0.25 is the first physical-anchor/presentation expansion wave for the 1.1.0 projector family. It advances the network protocol to **37** because `ProjectionChassisProfile` is transmitted by ordinal and the Image Workspace now carries Wall/Data-show playlist state. The two new chassis enum values remain appended after the historical six, preserving every older ordinal. `ProjectionSettings` advances from format **3** to **4** so the previously reserved horizontal/vertical placement fields become real signed X/Y wall-image offsets.

## Mirage Table Projector

`mirage_projector:mirage_table_projector` is the horizontal/tabletop chassis. It requires support below. Image and Banner plane sources lie parallel to the table surface; Item and Entity sources remain upright as volumetric holograms above the same anchor. It keeps Lift, Tilt, Rotation and Floating support.

Sneak + empty-hand right-click packs the complete projector state into one Table Projector item and removes the placed block. If its support disappears, the same full-state packed item is dropped. Both paths preserve Core, source snapshots/assets, settings, scan/staging state and future canonical BlockEntity data instead of spilling or duplicating internals.

## Mirage Wall Projector — Data-show, not wall-mounted

`mirage_projector:mirage_wall_projector` is a **low-profile data-show projector** inspired by a real video projector. It sits on top of a complete flat supporting block, faces North/East/South/West and projects **forward onto a real wall**. It is not attached to that wall. Slabs, stairs and other partial collision-top supports are intentionally rejected. The physical VoxelShape/model is below slab height and uses an Obsidian body with Crying Obsidian/emitter accents.

The Wall chassis is Image-only. Item, Entity and Banner workspaces are incompatible rather than pretending to be holograms on a wall.

### Actual-image wall validation

Wall regularity is evaluated against the **actual aspect-correct image rectangle**, after the requested Scale plus independent X and Y offsets are applied. Mirage does not validate a square nominal envelope around the image.

Consequences:

- a tall image may use a narrow clean vertical strip even when unused left/right parts of a nominal square are irregular;
- a 16:9 image may use a clean horizontal strip even when unused space above/below is irregular;
- only wall cells touched by the real image rectangle must form one complete regular plane;
- any irregular/partial block, recess, protrusion or obstruction touched by that rectangle cancels that candidate projection;
- geometry outside the real image rectangle is irrelevant.

The resolver searches forward along the optical axis for a real planar wall. It may reduce the requested Scale until the aspect-correct image fits the available regular surface and power budget, but it never stretches/crops the image to fill an unrelated square envelope.

### Distance and Projection Power

Distance to the wall is gameplay state, not a cosmetic translation. Wall projection uses normal chassis/source PU plus a distance surcharge. The current QA balance is **+1 PU per full block beyond the first block**. If the requested image is too large or too distant for the installed Core, the resolver attempts a smaller scale; if no valid powered scale remains, projection is cancelled.

The settings Placement tab exposes independent signed **X Offset** and **Y Offset** controls. These shift the real image rectangle across the detected wall and therefore participate directly in surface validation.

### Presentation playlist

The Wall Image Workspace owns an ordered **9-slot presentation playlist** using Mirage's existing image asset bank. Images may be imported/replaced, cleared and reordered. One slot is the current slide; Previous/Next change the active image while the GUI stays open and immediately update the real projection. The current slide is persisted with the projector state and survives packing/upgrading/reload.

This is the first PowerPoint-like presentation foundation. Automatic timings/transitions are intentionally not invented in this snapshot.

## Shared chassis capability contract

`ProjectionChassisProfile` declares an `Anchor` plus explicit placement capabilities. The Table uses `TABLE_HORIZONTAL`; the Wall/Data-show uses `WALL_TARGET` plus `WALL_XY_OFFSET`. Source compatibility is also chassis-aware: Wall enumerates only Image rather than exposing inert Item/Entity/Banner controls.

Renderer placement, target-wall validation, Projection Power, source-workspace visibility and settings controls consume those contracts. Generic hologram `ProjectionClearance` explicitly does **not** validate Wall, because Wall uses `WallProjectionSurface` and its real image footprint instead.

## Scope intentionally left open

No Survival recipe is frozen for Table or Wall in this snapshot. Table art remains provisional; Wall receives a first low-profile Data-show model but still needs visual runtime QA. Optional Create Blueprint presentation, automatic slide timing, End Resonance and final 1.1.0 balance remain later work.

## QA focus

Runtime QA should verify:

1. Table horizontal Image/Banner and upright Item/Entity behavior.
2. Full-state Table/Wall Shift+empty-hand pickup and support-loss round trips.
3. Wall placement on full flat supports and rejection on slabs/stairs.
4. N/E/S/W Wall targeting.
5. Tall, square and 16:9 actual-image footprint validation on deliberately irregular walls.
6. X/Y offsets moving the validated footprint rather than a nominal square.
7. automatic scale reduction when wall space or PU is insufficient.
8. greater wall distance increasing PU usage and eventually preventing projection.
9. playlist import/reorder/current/Previous/Next persistence.
10. existing Compact/Display/Wide/Tall/Field/Prism behavior remains unchanged.
