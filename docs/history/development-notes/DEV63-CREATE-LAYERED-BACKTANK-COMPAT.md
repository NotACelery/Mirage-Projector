# dev.63 — Create layered Backtank Ghost compatibility

**Superseded by dev.64 after live QA.** The pair-alpha strategy did not fully normalize the chest region because the projected Humanoid body remained another translucent surface beneath Create's two synthetic armor layers.

## Scope

dev.63 is a targeted render-compatibility pass for Create 6.0.10's Netherite Backtank. It does not introduce a hard Create dependency and does not alter the normal 100% opacity renderer.

## Create render structure verified

The Netherite Backtank is registered as `BacktankItem.Layered` with the `netherite_diving` armor texture family.

Create uses two independent visual paths for that one equipped chest item:

1. `BacktankArmorLayer` renders the physical tank/cogs/shaft from cached block geometry.
2. `LayeredArmorItem` replaces the normal Humanoid armor piece path and renders two synthetic Humanoid armor models:
   - `create:textures/models/armor/netherite_diving_layer_2.png` on the inner armor model;
   - `create:textures/models/armor/netherite_diving_layer_1.png` on the outer armor model.

Those two chest models are not separate inventory equipment snapshots. They are extra visual passes generated from the same equipped Netherite Backtank.

## Why the Ghost result was wrong

The physical tank is one Ghost surface, while the synthetic diving chestpiece contributes two overlapping armor surfaces. Applying the requested opacity independently to both synthetic passes makes their combined opacity substantially higher than a normal single armor layer.

The dev.62 late Entity path also gave every remapped armor surface a depth-writing Ghost RenderType. That is useful for ordinary body/equipment self-depth, but the two very close synthetic chest layers can then compete unnecessarily for late depth.

## dev.63 behavior

Only the two `netherite_diving_layer_1/2` textures receive the compatibility rule.

When Entity opacity is below 100%:

- the separate tank geometry remains on the normal late depth-stable Ghost path;
- the two synthetic diving chest layers use the colour-only Ghost Entity RenderType during the late pass;
- the per-layer opacity is transformed with:

```text
layerOpacity = 1 - sqrt(1 - requestedOpacity)
```

Two overlapping layers using that value compose to approximately the requested opacity of one ordinary armor layer:

```text
1 - (1 - layerOpacity)^2 = requestedOpacity
```

This keeps both Create texture layers instead of deleting one, while preventing the pair from receiving the full requested opacity twice.

At 100% opacity Mirage does not remap these layers, so Create's native appearance is preserved exactly.

## Compatibility boundary

The rule is texture-keyed and uses no Create Java class imports. Mirage therefore remains independently loadable when Create is absent.

The rule is intentionally not generalized to every custom armor renderer. Future adapters should be added only when live QA shows a specific renderer cannot be represented correctly by the generic Ghost buffer path.

## Required QA

Use the same scanned Humanoid/Player wearing the Netherite Backtank and compare:

- 0%, 1%, 10%, 50% and 90% transparency;
- tank geometry versus synthetic diving chestpiece fade rate;
- high/low/front/back camera angles;
- foil/glint active and inactive;
- clouds and water behind the projection;
- whether chest layers flicker or swap depth order while rotating the camera.

Acceptance requires no crash, no disappearance, no obvious synthetic-chest z-fighting, and a substantially closer visual opacity match between the tank and diving chestpiece.
