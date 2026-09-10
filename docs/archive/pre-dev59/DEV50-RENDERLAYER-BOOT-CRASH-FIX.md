# dev.50 — RenderLayer boot-crash fix

## QA evidence

The dev.49 pack launch progressed beyond the previous Crying Obsidian `Block#randomTick` failure, but failed later during client mod construction. The actual fatal cause was Mirage Projector's dev.47 `RenderLayerMixin`:

`Redirector mirageProjector$ghostColoredCutout ... failed injection check, (0/1) succeeded. Scanned 0 target(s).`

The same Mirage injection failure then surfaced while unrelated mods such as Curios and Hearth & Harvest were being loaded. Those mods are collateral victims, not the root cause.

## Fix

- Remove `RenderLayerMixin` from `mirage_projector.mixins.json`.
- Delete the invalid mixin source.
- Do not replace it with `require = 0`; that would only hide a dead implementation.
- Keep dev.49's Crying Obsidian `BlockStateBase` random-tick implementation unchanged.

## QA target

1. Confirm Minecraft reaches the title screen / world normally.
2. Then test Crying Obsidian nucleation again using the same lava-source setup and accelerated tick rate.
3. Cat-collar layer ordering is still pending and should be evaluated only after boot/growth are stable.
