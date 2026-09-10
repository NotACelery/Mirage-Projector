# dev.26 — Ghost depth + armor repair

## Problem reproduced in dev.25

In-game QA confirmed two independent regressions after the first Entity Ghost rewrite:

1. LivingEntity base bodies received Ghost alpha, but vanilla Humanoid armor remained visually opaque.
2. Looking through the translucent projected body caused water behind the projection to disappear.

The second symptom is a depth/composition bug, not an alpha-value bug. Reusing vanilla `entityTranslucent` for a BlockEntity-owned hologram allowed the projection pass to affect depth in a way that masked other translucent world geometry.

## Renderer contract

Ghost projection passes must satisfy all of these rules:

- depth-test against the world (`LEQUAL`) so a hologram behind a wall remains hidden;
- blend source alpha and Mirage Ghost alpha;
- write colour only, **not depth**;
- remain local to the temporary Mirage render scope;
- never modify global renderer state or normal world entities;
- avoid remapping effect-only/special RenderTypes when their semantics are unknown.

`ProjectionRenderTypes` therefore owns two cached client RenderTypes:

- `ghostEntity(texture)` — NEW_ENTITY + entity translucent shader + no cull + lightmap/overlay + normal alpha blending + MAIN_TARGET + `COLOR_WRITE`;
- `ghostItem(texture)` — NEW_ENTITY + item/entity translucent-cull shader + cull + lightmap/overlay + normal alpha blending + MAIN_TARGET + `COLOR_WRITE`.

Both still depth-test but do not write depth.

## Armor

Changing `LivingEntityRenderer#getRenderType` only affects the entity body's model. Vanilla humanoid armor is drawn later by `HumanoidArmorLayer`, which selects `armorCutoutNoCull` independently.

`HumanoidArmorLayerMixin` redirects that RenderType selection only while `ProjectionRenderContext` is active. Real Skeletons, Players, Zombies, Piglins, armor stands and other world entities remain untouched.

Armor trims continue through the wrapped `MultiBufferSource`; both decal/non-decal trim sheets are normalized to the Mirage ghost pass while Ghost is active. Glint remains native for now because its blend/texturing semantics are special and must not be forced through the generic alpha shader.

## Held items and secondary layers

- block/item atlas sheets -> `ghostItem(BLOCK_ATLAS)`;
- shields -> `ghostEntity(SHIELD_SHEET)`;
- banners -> `ghostEntity(BANNER_SHEET)`;
- armor trims -> `ghostEntity(ARMOR_TRIMS_SHEET)`;
- compatible entity solid/cutout/translucent layers with recoverable textures -> `ghostEntity(texture)`;
- item/entity translucent layers -> `ghostItem(texture)`;
- unknown/modded/special passes fail closed and keep their native RenderType.

The existing vertex wrapper still applies Mirage tint and alpha to vertex colours.

Image/Prism faces also select `ghostEntity(texture)` whenever Ghost opacity is below 100%, so the no-depth-write rule is shared by every projection family rather than only Item/Entity.

## Required Windows / in-game QA

1. Build with `build.bat` on Java 21 / NeoForge 21.1.244.
2. Skeleton without armor: Ghost 0%, 50%, 90%.
3. Skeleton or Player mannequin with full diamond/netherite armor: body and all four armor pieces must share Ghost alpha.
4. Repeat with dyed leather armor and at least one armor trim.
5. Hold sword/tool/block/shield and verify alpha is consistent with the body.
6. Put water directly behind the projection and look through body, armor and held item. Water must remain visible.
7. Put an opaque block behind the projection. The projection must not render through the block.
8. Regression: Piglin, Horse and one modded LivingEntity.
9. Compare world renderer and Entity Workspace preview.
10. Glint/eyes/emissive special layers: confirm there is no depth corruption. They may remain visually stronger than the base Ghost pass until a dedicated special-layer implementation is added.

## Non-goals of this pass

- perfect order-independent transparency inside overlapping model parts;
- forcing unknown modded RenderTypes into alpha blending;
- rewriting vanilla glint/eyes/emissive shaders.

Those are deliberately separate from fixing the confirmed opaque-armor and water-depth bugs.
