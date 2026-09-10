# Entity layout / pose / Ghost pipeline — dev.24

## Scope

dev.24 is a corrective wave on top of dev.23. It does not resume Banner scope yet. It addresses the in-game findings from dev.23 before additional projection families are layered on top.

## Entity workspace layout

The Entity workspace now uses a larger single panel with four independent regions:

1. Entity Source
2. Incoming / Channel / Projected equipment
3. Actions + status
4. Player inventory

The player inventory uses the normal 9×3 grid plus hotbar spacing and is centered across the full workspace. Physical staging slots, their painted frames, apply buttons, channel labels and projected previews now share one coordinate system instead of separate hand-tuned offsets.

The Item Snapshot workspace was widened for the same reason: its title, Projection Settings button, virtual snapshot source, 3D preview and player inventory no longer share text space.

## Incoming is a queue, not storage

Accepted equipment is represented only on Projected. After Apply/Replace:

- virtual Incoming is cleared;
- a real staging source is returned immediately to the player;
- overflow is dropped at the player;
- legacy/saved Incoming entries that are visually identical to Projected are pruned when state loads;
- Capture Equipped Loadout does not restage channels that already match Projected.

This keeps the left rail meaningful: an occupied Incoming position means there is still an action to take.

## Horse poses

Horse rearing is no longer permanent. Horse state has an independent persisted pose preset:

- Idle
- Rearing

The same contextual Pose button used by Humanoid cycles the Horse pose when the active scan is Horse. Scan cards and equipment snapshots are not mutated.

## Ghost pipeline

The dev.23 approach depended too heavily on parsing `RenderType#toString()` and also sent item/block sheets through a translucent block render path. That made behavior entity-dependent and could interfere visually with water/depth.

dev.24 splits responsibilities:

- A projection-local thread context exists only while Mirage calls `EntityRenderDispatcher`.
- A client mixin on `LivingEntityRenderer#getRenderType` forces the **base body** of that one Mirage entity through vanilla `entityTranslucent` when Ghost is active.
- The `MultiBufferSource` wrapper still multiplies tint/alpha per vertex for body layers, equipment and items.
- Opaque/cutout item sheets are remapped to `Sheets.translucentItemSheet()`, not the world/block translucent sheet.
- Common armor/entity cutout layers are remapped to projection-local entity translucency when their texture can be resolved.
- Shield/banner atlas-backed held layers get a local translucent entity pass.
- Glint, eyes, beams, shadows, water masks, text and other effect-specific passes keep their original RenderType instead of being forced into an incompatible pipeline.
- Unknown modded special RenderTypes fail closed: they may remain partly opaque, but Mirage will not rewrite them into a render target that can corrupt unrelated world translucency.

This is intended to make Skeleton-class renderers and normal armor obey Ghost consistently while removing the water artefact seen with held items in dev.23. Ender Dragon and other non-`LivingEntityRenderer` special pipelines remain separate QA cases.

## Wire compatibility

Protocol is bumped from `11` to `12` because Horse pose state and Entity workspace behavior changed materially across the dev.23/dev.24 boundary.
