# Mirage Projector 1.0.32 — Dedicated Table Runtime Hotfix

This hotfix replaces the remaining shared Table presentation semantics after runtime QA showed that
Image visibility still depended on in-plane Rotation relative to the viewer. The user-facing Table
settings GUI is intentionally unchanged: `MirageTableProjectorScreen` is a 1:1 copy of the existing
fixed-projector screen layout and controls, but it is now routed only for the TABLE chassis.

## Dedicated Table rules

The Table now owns a dedicated `MirageTableProjectorLogic` runtime contract:

- Image/Banner are true horizontal planar presentation surfaces.
- Item/Entity remain upright volumetric projections above the same anchor.
- X/Z are world-space tabletop offsets; Y continues to be Lift.
- Rotation is in-plane presentation yaw and must not decide whether a zero-Tilt image exists.
- Tilt uses the persisted ProjectionTransform quaternion.
- front/back classification transforms the local +Z normal through the exact same
  `Yaw -> horizontal -90° X -> persisted orientation` quaternion chain used for rendering.
- render bounds are Table-owned and include signed X/Z offsets, Rotation-safe planar radius,
  Tilt, Lift and Float.
- Table source dispatch is separated from the canonical fixed-projector dispatcher.

The canonical `MirageProjectorRenderer` retains only the low-level drawing primitives and delegates
Table placement/front-side/bounds to the dedicated Table runtime. The old shared
`applyTableSurfaceOffsets` helper was removed.

## Compatibility

The save/storage contract remains unchanged; the main-menu opening payload changes intentionally:

- registry IDs are unchanged;
- Table still uses the established `MirageProjectorBlockEntity` and `MirageProjectorMenu` storage
  contract so existing worlds do not require a BlockEntity migration;
- main-menu opening data now includes the authoritative chassis ordinal, so network protocol advances to **43**;
- `ProjectionSettings` serialization remains **4**.

This is therefore a runtime ownership split plus a menu-opening transport correction, not a save-format fork.
