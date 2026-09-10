# Mirage Projector — next-chat handoff after dev.58

Current source candidate: **0.1.0-dev.58**.

## Main changes

- dev.57 framebuffer-only approach was insufficient for cloud ordering.
- Semi-transparent ENTITY projections now flush at `AFTER_LEVEL` to draw after cloud/weather/transparency composition.
- Opaque ENTITY projections continue flushing at `AFTER_TRIPWIRE_BLOCKS`.
- Ghost RenderTypes use `MAIN_TARGET` again.
- Projection buffer remapping was broadened to raw chunk/block-atlas layers and custom-named texture-backed RenderTypes so modded custom armor can inherit ghost alpha.
- No new mixins.

## QA priorities

1. Reproduce the cloud-altitude giant entity test.
2. Re-test Create Netherite Backtank.
3. Check water ordering from both sides.
4. Confirm 100% opacity entity projections still render.

## Build state

Source candidate only until Windows `build.bat` is run. Protocol remains 18.
