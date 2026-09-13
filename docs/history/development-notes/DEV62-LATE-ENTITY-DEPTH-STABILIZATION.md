# dev.62 — Late Entity projection transform + depth stabilization

## Live QA entering this wave

dev.61 removed the Create Netherite Backtank crash. The same regression test then exposed two renderer failures:

1. visible depth/z-order fighting in the projected equipment stack;
2. changing Entity transparency from 0% to even 1% made the whole projection disappear until transparency returned to 0%.

## Root cause of the opacity disappearance

Semi-transparent Entity projections are queued for NeoForge's `AFTER_LEVEL` stage so they render after cloud/weather framebuffer composition. In Minecraft/NeoForge 1.21.1, `LevelRenderer` pushes the camera/world model-view matrix while rendering the level and pops it before returning to `GameRenderer`. `AFTER_LEVEL` is then dispatched with a null stage `PoseStack`, which becomes a fresh identity stack, while the event separately carries the original world model-view matrix.

dev.58-dev.61 used the late stage's identity stack but never restored that matrix. The projection coordinates were still translated relative to the camera, but they were no longer transformed by camera yaw/pitch, so enabling any Ghost opacity switched the Entity onto a late pass with the wrong transform.

## dev.62 correction

`ClientRuntimeEvents` now temporarily pushes Minecraft's model-view stack, multiplies the matrix supplied by `RenderLevelStageEvent`, applies it, flushes Mirage's late Entity queue, then restores the previous model-view state. This is scoped only to the late Mirage pass.

## Depth stabilization

The general Ghost RenderTypes remain colour-only because Image/Banner/Item projections can render while water and other translucent world geometry still need their normal depth behavior.

Semi-transparent Entity projections are different: their Ghost pass occurs only after level composition has finished. dev.62 adds late Entity/Item Ghost RenderTypes that write colour and depth, still depth-test with `LEQUAL`, and disable translucent quad sorting. World water/cloud colour has already been rendered before these depth writes, and first-person hand rendering clears depth after `AFTER_LEVEL`. The extra depth therefore stabilizes the hologram's own body/armor/custom-equipment surfaces without punching historical holes into water behind the projection.

## Deferred buffer ordering

The projection-owned fixed-buffer set now mirrors the important vanilla fixed sheets used by entity/equipment renderers. Base surfaces are registered before glint overlays. The buffer source is also ended after every projected Entity rather than once after the entire queue. This keeps one hologram's block-sheet/armor/glint layers together and prevents fixed geometry from several projectors being held until a later global flush.

## Required QA

1. Re-run the exact Create Netherite Backtank setup that crashed before dev.61.
2. Test transparency 0%, 1%, 10%, 50% and 90%. Every value must remain visible.
3. Rotate the camera around the projection at non-zero transparency to confirm the restored late transform is correct.
4. Watch the Backtank, body and armor edges for z-fighting/flicker.
5. Repeat with foil/glint active.
6. Put clouds behind/intersecting a large projection; they must no longer overwrite the hologram.
7. Put water behind and in front of the projection; no historical depth holes may return.
8. Re-test armor trims, held shield/tool/block and one special eyes/emissive entity layer.

Network protocol remains 18.
