# Mirage Projector — handoff after dev.50

Current source candidate: **0.1.0-dev.50**.

## Immediate history

- dev.48 crashed at bootstrap because the growth mixin targeted `Block#randomTick`, which is not a valid injection target there.
- dev.49 moved Crying Obsidian random-tick eligibility + nucleation dispatch into `BlockBehaviour.BlockStateBase`; the next QA launch progressed past that previous failure.
- dev.49 then exposed a different pre-existing dev.47 client mixin problem: `RenderLayerMixin` failed its redirect injection with 0 targets found during client mod construction.

## dev.50

- Removed `RenderLayerMixin` from the mixin config.
- Deleted the invalid redirect source instead of weakening it to `require = 0`.
- Preserved dev.49's Crying Obsidian random-tick dispatch code unchanged.
- Cat collar / secondary projected-layer ordering remains pending; it needs a verified rendering approach rather than another guessed generic redirect.

## Next QA order

1. Boot the game.
2. Enter a world.
3. Re-test lava source -> crying obsidian -> air/water below with `/tick rate 2000`.
4. If a small bud appears, verify medium -> large -> cluster growth.
5. Resume projector visual QA (chamber shell, core hologram, entity collar ordering, UI overlap).

## Branding constraints

The mod must present development credit as **Celerbi**. Do not include repository/hosting links in the mod metadata or description.
