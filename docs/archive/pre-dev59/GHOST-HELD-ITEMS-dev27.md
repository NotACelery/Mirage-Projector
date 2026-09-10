# dev.27 — Held-item Ghost depth repair

## Finding

After dev.26, base LivingEntity bodies and Humanoid armor correctly used Mirage-owned colour-only Ghost RenderTypes, but items held in Main Hand / Off Hand could still make water disappear behind their geometry. The issue was isolated to the third-person held-item pipeline.

## Cause

`ItemInHandRenderer` can ask ItemRenderer for raw block/chunk RenderTypes in addition to the entity sheet RenderTypes handled by the general projection wrapper. A sword/tool/block that retained one of those depth-writing passes could still mask water even while its owning entity was rendered correctly.

## Repair

`ItemInHandRendererMixin` is active only inside `ProjectionRenderContext`. It replaces the held item's `MultiBufferSource` with a small normalizer that maps raw solid/cutout/translucent block layers to `ProjectionRenderTypes.ghostItem(BLOCK_ATLAS)`. The underlying projection buffer remains responsible for Tint/alpha so the effect is applied exactly once.

Covered raw layers:

- solid
- cutout mipped
- cutout
- translucent
- translucent moving block
- solid/cutout/translucent item sheets
- shield/banner/trims and compatible texture-backed entity/item layers through the existing fallback

## Contract

A held projection item must depth-test against opaque world geometry but must not write world depth when Ghost < 100% opacity. Main Hand and Off Hand use the same rule.

## QA

Test at minimum sword, pickaxe, full cube block, cutout block/item, shield and one modded held item with water directly behind and an opaque wall in front.
