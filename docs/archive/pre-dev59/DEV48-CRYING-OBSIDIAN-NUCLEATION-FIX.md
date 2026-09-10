# dev.48 — Crying Obsidian nucleation fix

## Problem

User QA on dev.47 showed that even after extended accelerated ticking (`/tick rate 2000`), vanilla Crying Obsidian under a lava source was not spawning any Crying Obsidian buds.

## Cause

The nucleation hook mixin was targeting `BlockBehaviour.randomTick`. In practice, the Crying Obsidian runtime path was not invoking that injected site for the real random tick dispatch we needed, so `CryingObsidianGrowthHooks.tryNucleate(...)` never ran.

## Fix

- Retargeted the mixin to `Block.randomTick`.
- Kept the existing `BlockStateBase#isRandomlyTicking` override so vanilla Crying Obsidian is still eligible for random ticking.

## Expected result

When a vanilla Crying Obsidian block has a lava source directly above it and air/water directly below it, the block should now actually nucleate a small Crying Obsidian bud during random ticks.
