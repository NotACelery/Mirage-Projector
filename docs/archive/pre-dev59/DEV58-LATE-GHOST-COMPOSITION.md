# dev.58 — Late ghost composition and modded armor coverage

## QA failures addressed

1. Clouds remained visually in front of semi-transparent projected entities even after dev.57 moved ghost RenderTypes to `ITEM_ENTITY_TARGET`.
2. Custom-rendered armor/equipment could bypass the ghost RenderType entirely. The user reproduced this with Create's Netherite Backtank.

## Rendering strategy

Entity projections were already deferred by Mirage. dev.58 splits that existing queue by opacity instead of adding another mixin:

- **Opaque projection (100%)**: flush at `AFTER_TRIPWIRE_BLOCKS`, preserving the proven normal-world rendering path.
- **Ghost projection (<100%)**: flush at `AFTER_LEVEL`, after Minecraft has completed level rendering and framebuffer composition.

Ghost RenderTypes use `MAIN_TARGET`, `LEQUAL_DEPTH_TEST`, translucent blending, and `COLOR_WRITE` only.

## Modded equipment coverage

The projection-owned `MultiBufferSource` now converts raw block-atlas RenderTypes (`solid`, `cutout`, `cutoutMipped`, `translucent`, `translucentMovingBlock`) into Mirage's ghost item pass. It also accepts custom-named textured RenderTypes when their texture can be recovered from the RenderType description.

This is intended to cover armor systems that receive our projection buffer but skip vanilla `HumanoidArmorLayer.renderModel`.

## QA checklist

- Large semi-transparent entity intersecting cloud altitude: clouds must visually remain behind the hologram.
- Vanilla armor at several opacity values.
- Create Netherite Backtank: every visible piece must inherit hologram opacity/tint.
- Held block/item, shield and armor trim regression check.
- Water behind and in front of the hologram: confirm the old depth-hole bug does not return.
- Opaque (100%) entity projection still renders normally.
