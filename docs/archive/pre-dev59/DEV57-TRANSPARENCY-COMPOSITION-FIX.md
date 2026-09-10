# dev.57 — Projection transparency composition fix

## QA report

With partially transparent projected entities/armor, clouds could visually composite in front of parts of the hologram.

## Cause

Mirage-owned ghost RenderTypes were routed to `MAIN_TARGET`. Under Minecraft's shader/Fabulous transparency pipeline, clouds use their own target and are composited separately. That allowed the cloud framebuffer to be combined in an order that could make clouds appear on top of the projected entity.

## Fix

- `ProjectionRenderTypes.ghostEntity(...)` now uses `ITEM_ENTITY_TARGET`.
- `ProjectionRenderTypes.ghostItem(...)` now uses `ITEM_ENTITY_TARGET`.
- The write mask remains `COLOR_WRITE`, so projections still do **not** stamp depth and the previous water/translucent-world hole fix is preserved.
- Depth testing remains `LEQUAL_DEPTH_TEST`.

No entity mixin or cloud renderer hook was added; this is a render-target composition correction rather than a global cloud modification.

## QA targets

1. Semi-transparent entity bodies should remain visually in front of clouds when geometrically in front.
2. Armor and held items should follow the same ordering.
3. Water behind a translucent projection must remain visible and must not develop depth holes.
4. Verify both normal graphics and Fabulous/shader-transparency mode if available.
