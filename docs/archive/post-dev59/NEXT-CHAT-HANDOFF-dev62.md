# Mirage Projector — next-chat handoff after dev.62

Current source candidate: **0.1.0-dev.62**. Network protocol remains 18.

## Baseline

- dev.59: full audit/rake baseline.
- dev.60: Core Booster Beacon relay; runnable in-game after stale-source cleanup.
- dev.61: Create Backtank/foil multi-buffer crash fixed and confirmed by live QA.
- dev.62: fixes the late Ghost transform loss and stabilizes Entity self-depth/buffer ordering.

## Confirmed dev.61 result

The previous `IllegalStateException: Not building!` no longer occurs with the Create Netherite Backtank.

## New root cause fixed in dev.62

Opacity below 100% moved Entity projections to `AFTER_LEVEL`. NeoForge 1.21.1 dispatches that stage after `LevelRenderer` has popped the camera/world model-view stack, and the stage `PoseStack` is fresh/identity. Mirage was translating camera-relative coordinates without restoring the event's model-view matrix, so the late projection disappeared.

dev.62 restores the supplied model-view only around the late flush. The late Entity Ghost RenderTypes also write depth after level composition, disable translucent quad sorting, use base-before-glint fixed-buffer ordering and flush fixed buffers per Entity. Image/Banner/Item Ghost paths remain no-depth-write.

## Next QA

- Create Backtank at 0%, 1%, 10%, 50% and 90% transparency;
- arbitrary camera yaw/pitch at every non-zero transparency;
- Backtank/body/armor z-fighting;
- foil/glint active;
- clouds intersecting/behind a giant projection;
- water in front and behind;
- vanilla armor, trims, held items and special eye/emissive layers.

Do not move past P0 renderer stabilization until the opacity disappearance and z-fighting regressions are checked live.
