# Mirage Projector 0.1.0-dev.17 — 3D Ghost/Tint Render Pass

## Scope

This wave connects the existing Projection presentation settings to volumetric sources:

- ordinary Item Mode;
- block items rendered as full 3D models;
- standalone equipped armor Item Mode;
- scanned Entity Mode;
- Humanoid Entity bodies;
- the bodyless virtual Humanoid mannequin;
- projected armor and both hand items rendered as entity layers;
- the selective base nameplate.

Image/Banner-style face rendering keeps its existing direct-vertex alpha/tint path.

## Safety rule

Do **not** use a persistent/global shader-colour mutation for 3D projection alpha. Entity renderers can enqueue multiple RenderTypes and share world buffers with unrelated renderers. A leaked global alpha/tint would contaminate the HUD/world and repeat the kind of global-render-state bug already avoided elsewhere in the user's mods.

`ProjectionRenderBuffers` therefore wraps only the `MultiBufferSource` passed to the current Mirage render call.

## Render path

```text
ProjectionSettings
  |- Tint RGB
  |- Ghost Effect -> legacy OpacityPercent
  v
ProjectionRenderBuffers
  |- per-vertex RGB multiplication
  |- per-vertex alpha multiplication
  |- common opaque/cutout textured layers -> translucent equivalent when Ghost > 0
  |- already-translucent/special layers -> keep original RenderType
  v
vanilla/modded ItemRenderer or EntityRenderDispatcher
```

The wrapper is local to the projection. It does not write persistent RenderSystem shader colour.

## Why RenderTypes are selectively remapped

Multiplying vertex alpha is not enough for an opaque `entity_cutout`/`entity_solid` RenderType because that RenderType does not enable normal alpha blending. For the common vanilla textured body/armor layers, Ghost Effect therefore changes the requested buffer to a translucent textured entity RenderType while preserving culling where possible.

Known special render families are deliberately **not** replaced wholesale:

- glint;
- emissive eyes;
- energy swirl;
- beams/lightning;
- already-translucent layers;
- text/nameplate paths;
- other unusual custom RenderTypes.

Those keep their structural RenderType and still pass through the colour/alpha wrapper when their vertex format exposes colour. This is intentionally fail-safe for modded renderers: an unknown custom layer should remain visually recognizable rather than being forced into an incompatible vertex format/shader.

## Texture lookup boundary

Minecraft 1.21.1 exposes no public generic `RenderType -> ResourceLocation` accessor. To avoid:

- reflection into private RenderType internals;
- Mixins/access transformers for one presentation effect;
- split-package classes under `net.minecraft.*`;

the dev.17 bridge parses only the first canonical `texture[...]` fragment from the RenderType diagnostic string for known opaque entity families. Vanilla 1.21.1 formats that entry as `texture[Optional[namespace:path](...)]`; Mirage explicitly unwraps that narrow `Optional[...]` representation and otherwise fails closed. If a custom RenderType does not expose that normal CompositeState string, Mirage simply leaves the RenderType intact and applies the safe vertex-colour fallback.

This is version-pinned behavior and must be checked whenever the Minecraft/NeoForge baseline changes.

## GUI preview

`EntityProjectionPreviewRenderer` no longer calls the vanilla inventory helper directly. It mirrors the small vanilla inventory transform locally so the preview can inject `ProjectionRenderBuffers`.

As a result, Entity/Humanoid preview now reflects:

- current Tint;
- current Ghost Effect;
- projected/active equipment;
- bodyless mannequin equipment;
- cursor-facing inventory-style rotation;
- existing auto-fit/scissor bounds.

Item Mode preview also changes:

- standalone armor still uses equipped geometry on the invisible rig;
- ordinary items/blocks now render with `ItemDisplayContext.FIXED` in a 3D preview instead of only the flat inventory icon;
- the 3D item preview receives Tint/Ghost Effect through the same projection buffer.

## Nameplate

The selective projector-base nameplate multiplies its colour by the same Tint and Ghost Effect. Name visibility policy is unchanged:

- Player -> player name;
- renamed non-player -> captured custom name;
- unnamed non-player -> no nameplate.

## Known QA-sensitive layers

The following must be tested before calling the implementation universal/build-clean:

1. armor trims and dyed leather;
2. enchanted armor/entity glint at Ghost 0/25/50/90%;
3. enchanted held items and shields;
4. Ender Dragon eyes and other emissive layers;
5. Creeper charged layer / energy-swirl style renderers;
6. modded armor with custom NeoForge armor models/textures;
7. modded entities with custom RenderTypes;
8. transparent block/item models;
9. GUI preview after repeated open/close to catch render-state leakage;
10. multiple projectors with different Tint/Ghost values in the same frame.

Glint and custom RenderTypes that do not expose a normal colour channel may not fade identically to the base model in this first pass. They are kept structurally intact on purpose rather than replaced with a potentially incompatible shader. If QA shows visible opaque glint residue at high Ghost values, the next refinement should target glint as a dedicated projection layer rather than reintroducing global state.

## Snapshot invariant

This renderer pass is recoverable independently of compile/QA status. Do not remove or postpone source snapshots while experimenting with later pose/clearance work.
