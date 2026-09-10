# dev.49 — Crying Obsidian random-tick dispatch fix

## Why dev.48 crashed

The dev.48 repair retargeted the nucleation injection to `Block#randomTick`. In Minecraft 1.21.1 that method is inherited from `BlockBehaviour` rather than declared on `Block`, so Mixin could not resolve an injection target and aborted bootstrap.

## dev.49 design

The random-tick eligibility hook and the actual nucleation hook now live together on `BlockBehaviour.BlockStateBase`:

- `isRandomlyTicking` returns true for vanilla Crying Obsidian;
- `randomTick(ServerLevel, BlockPos, RandomSource)` directly calls `CryingObsidianGrowthHooks.tryNucleate(...)` for Crying Obsidian states.

This is the state-level dispatch point used by the server random-tick scheduler and avoids relying on a vanilla Crying Obsidian block override that does not exist.

The broken standalone `CryingObsidianGrowthMixin` is removed from both source and mixin configuration.

## QA

1. Confirm the game reaches the title screen.
2. Place lava source / Crying Obsidian / air (or water) vertically.
3. Use a high tick rate for QA.
4. Confirm a small bud appears below the Crying Obsidian.
5. Confirm small -> medium -> large -> cluster growth still works.
