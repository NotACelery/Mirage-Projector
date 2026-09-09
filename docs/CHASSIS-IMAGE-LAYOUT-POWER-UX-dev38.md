# Chassis / Image Layout / Placement / GUI Contract — dev.38

> **AUTHORITATIVE CURRENT CONTRACT.** This document supersedes the mandatory-grid interpretation recorded in dev.33 and all older references that call the values below absolute chassis limits. Power mathematics live in `POWER-SYSTEM-REWORK-dev38.md`.

## 1. Current chassis table

| Chassis | Image contract | Nominal W×H | Nominal Lift | Nominal Float | PU multiplier |
|---|---|---:|---:|---:|---:|
| Mirage Projector / Compact | one continuous Plane | 10×10 px | 32 px | 4 px | ×1.00 |
| Mirage Display | one continuous Plane | 32×32 px | 48 px | 12 px | ×1.50 |
| Wide | one continuous wide Plane **or** optional 4×1 | 80×32 px | 64 px | 12 px | ×2.00 |
| Tall | one continuous tall Plane **or** optional 1×4 | 32×80 px | 96 px | 16 px | ×2.00 |
| Mirage Field | **one continuous large Plane; no image grid** | 128×128 px | 144 px | 24 px | ×4.00 |
| Mirage Prism | four lateral cardinal faces | 48×48 px/face | 96 px | 12 px | ×2.00 |

`Nominal` means the range where that chassis component pays no Overdrive penalty. With enough Effective PU a chassis may exceed nominal values; the PU cost rises quadratically beyond nominal.

## 2. Mirage Projector / Compact

Purpose:

- small decorations;
- item-like or icon-like floating displays;
- low material footprint;
- cheap installations where a large physical chassis is unnecessary.

Nominal geometry is 10×10 px. A strong Core may overdrive it beyond that, but Compact becomes progressively inefficient and therefore does not replace Display/Field.

## 3. Mirage Display

Purpose:

- normal single-image displays;
- roughly one-to-two-block visual pieces;
- higher Lift and Float headroom than Compact without buying a large-field chassis.

Nominal geometry is 32×32 px with 48 px Lift and 12 px Float.

## 4. Wide

Image Workspace exposes:

```text
Image layout: Single image / 4 images
```

### SINGLE

One continuous Plane. Intended for panoramas, signs, long illustrations and other wide sources. Aspect ratio is preserved.

### MULTI

Four independent sources in a 4×1 strip:

```text
[1][2][3][4]
```

The four cells are equal squares. The same source therefore receives the same cell scale as Tall MULTI at the same global Scale.

At Scale 80:

```text
Wide MULTI total = 80×20
cell = 20×20
```

MULTI currently supports Front, Mirrored and Readable rear behaviour from the same four-source bank. A true Independent rear multi-bank is deferred rather than faked.

## 5. Tall

Image Workspace exposes the same toggle:

```text
Image layout: Single image / 4 images
```

### SINGLE

One continuous tall Plane. Intended especially for large vertical illustrations/banner-like imagery without requiring Field.

### MULTI

Four independent sources in a 1×4 cascade:

```text
[1]
[2]
[3]
[4]
```

At Scale 80:

```text
Tall MULTI total = 20×80
cell = 20×20
```

This symmetry is mandatory: Tall must not magnify the same source more than Wide merely because its strip is vertical.

## 6. Mirage Field

Field is a **single massive continuous 2D Plane**.

It does not expose an ImageLayoutMode toggle and must never expose a 3×3 source picker.

Purpose:

- very large single artworks;
- large square/wide/tall sources while preserving aspect ratio;
- installations where Display/Wide/Tall would need excessive Overdrive;
- high Lift (144 px nominal) and larger Float (24 px nominal).

The dev.33 3×3/nine-source interpretation is historical and explicitly invalid as current design.

## 7. Mirage Prism

Prism is an open four-sided projection body:

- North;
- East;
- South;
- West.

Each side can have an independent Image/Banner source. There is no top or bottom projection face.

`Same Source on All Faces` may copy North's source reference to the other sides without inventing duplicated asset files.

Prism face names are **world-cardinal**, not relative to how the block was placed.

## 8. Plane front/back modes

Single-plane Image mode supports:

- `Front`: Front side only;
- `Back`: Back side only;
- `Mirrored`: reuse Front on rear, mirrored physically;
- `Readable`: reuse Front on rear while keeping readable orientation;
- `Independent`: real Front and real Back assets.

Do not fabricate an Independent Back fallback when no Back asset exists.

Wide/Tall MULTI currently have one four-source bank and therefore intentionally expose only rear behaviours that can be represented honestly with that bank.

## 9. Scale semantics

For one continuous Image Plane:

- Scale targets the source's largest rendered dimension;
- source aspect ratio is preserved;
- actual W×H is used for PU Overdrive and clearance.

For Wide/Tall MULTI:

- Scale controls the strip/stack longest axis;
- total geometry uses four equal square cells;
- each source is aspect-fitted and centered in its own cell;
- populated cells cost PU; empty cells simply do not draw.

For Prism:

- each face preserves its own aspect ratio;
- Scale is shared presentation state;
- PU sums populated face geometry.

## 10. Field migration from dev.33-dev.37

The old `ImageSourceBank` persists nine slots only as a recovery container for saves produced during the accidental mandatory-grid period.

Current behaviour:

- unsupported `MULTI` on Field is coerced to `SINGLE`;
- if a migrated projector has no continuous Front image, legacy slot 0 is recovered as Front;
- old slots 4–8 are preserved in serialized data instead of being destroyed merely by opening/applying the corrected workspace;
- current Field rendering/GUI never consumes those slots.

Wide/Tall use only slots 0–3 when in MULTI.

## 11. Furnace-style placement facing

Every current projector block has horizontal `FACING`.

New placement behaves like a furnace: the projector's front faces the player who places it.

Plane-like rendering uses that block orientation as its base angle, then applies user Rotation/Orientation settings.

Exception:

- Prism Image/Banner N/E/S/W are true world directions and are not renamed/remapped when the physical block faces another direction.

Old worlds retain the blockstate's compatible default orientation until replaced/rotated.

## 12. Dynamic controls

Scale/Lift/Float endpoints are calculated from current Effective PU and all other active settings. Nominal chassis values are not hard slider endpoints; overdrive may extend them if the installed Core can pay for it.

The player should never need to deliberately drag into an invalid region and then search backwards pixel by pixel.

Power UI displays:

- Core base PU;
- chassis multiplier;
- Core amplification;
- Effective capacity;
- current final load;
- current dimensions vs nominal geometry;
- Scale/Lift/Float current vs effective slider maximum;
- nominal Lift/Float reference;
- Overdrive status when active.

Hovering the section exposes the exact PU breakdown.

## 13. Workspace layout ownership

The primary Projection Settings GUI owns global presentation only:

- Scale;
- Lift;
- Rotation;
- Floating;
- Lighting;
- Ghost;
- Tint;
- Core/Power;
- Clearance.

Source workspaces own their source data.

A preview renderer must render preview content only; it must not independently draw labels that collide with the Screen's layout. dev.38 specifically removes the duplicate internal `ITEM PREVIEW` title that was overlapping Item Snapshot UI text.

## 14. QA contract for dev.38

Required in-game checks before build-clean status:

1. Field: one square/wide/tall image; no replication and no 3×3 GUI.
2. Wide SINGLE panorama; Wide MULTI 4×1; toggle both directions and reopen world.
3. Tall SINGLE vertical source; Tall MULTI 1×4; compare same source/Scale against Wide cell size.
4. Compact/Display/Field: verify nominal markers and that a strong Core can enter Overdrive instead of hitting the old hard wall.
5. Swap Glass→Quartz→Amethyst→Diamond→Netherite and verify slider endpoints recalculate immediately.
6. Verify Float never exceeds Lift.
7. Verify Ghost only changes PU endpoint by the tiny documented rebate.
8. Place each chassis facing N/E/S/W; Plane follows block facing.
9. Prism N/E/S/W remain world-cardinal regardless of physical placement facing.
10. Item Workspace labels/previews do not overlap at normal GUI scales.
11. Re-run dev.37 Entity/projector/water depth-order tests; dev.38 must not regress the isolated render pipeline.
