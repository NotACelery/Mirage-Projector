# dev.64 — Create Netherite Backtank synthetic chest single-surface Ghost

## Finding

Create 6.0.10 registers the Netherite Backtank as `BacktankItem.Layered`. One equipped chest item is therefore not one rendered surface. Create's custom armor path draws an inner `netherite_diving_layer_2` Humanoid model and an outer `netherite_diving_layer_1` Humanoid model, while `BacktankArmorLayer` independently draws the tank/cogs block-model geometry.

dev.63 normalized the two synthetic armor alpha values as a pair, but the projected Humanoid body remained a third translucent surface under the chest. The combined chest region could therefore remain materially more opaque than the separately rendered tank and still show overlapping-surface instability.

## dev.64 policy

When the active projected entity has `create:netherite_backtank` in CHEST and Ghost transparency is active:

1. the synthetic inner `netherite_diving_layer_2` pass is discarded;
2. an immediately associated inner armor-glint pass is discarded if Create emits one;
3. the outer `netherite_diving_layer_1` pass is rendered once with the projection's requested opacity;
4. that outer surface uses the late Entity depth-stable Ghost RenderType;
5. the separate Backtank tank/cogs renderer remains unchanged;
6. at 100% opacity Mirage does not apply this compatibility policy and Create renders both layers natively.

The compatibility path identifies the texture when the Composite RenderType exposes it. It also has a deterministic first/second armor-pass fallback while the active projected chest item is specifically `create:netherite_backtank`, avoiding a hard compile/runtime dependency on Create classes.

## Acceptance

The chestpiece and physical tank should fade at comparable rates, the chest should not accumulate multiple Ghost layers over the body, and orbiting the projection should not produce synthetic-chest z-fighting. The existing dev.61 no-crash guarantee for Backtank base + foil consumers must remain intact.
