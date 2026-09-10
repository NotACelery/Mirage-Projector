# dev.54 — Core Booster redesign

## User-facing replacement

The five separately crafted Improved Core blocks are replaced by one **Core Booster** block/item.
The old registry IDs remain hidden only as dev-world compatibility entries; they have no new recipes and are not shown in Mirage creative listings.

## Core Booster interaction

- Crafting creates an **empty** Core Booster shell.
- Right-click an empty placed Booster with exactly one of:
  - Glass
  - Quartz
  - Amethyst Shard
  - Diamond
  - Netherite Ingot
- The inserted item becomes the Booster's material and produces the corresponding x1.50 improved Projection Core profile.
- Sneak + right-click removes the real inserted material.
- Empty Boosters are not accepted by projector Core sockets.
- Loaded Boosters are accepted as improved Projection Cores.

## Persistence and stacking

The material is stored in vanilla `BLOCK_ENTITY_DATA` on the Core Booster ItemStack.
This provides the desired inventory behavior without custom stack hacks:

- empty stacks with empty;
- Glass stacks with Glass;
- Diamond stacks with Diamond;
- different materials do not stack together.

Correct-tool player mining packs the Core Booster and its material into one stateful item.
No Silk Touch is required, but the block uses `requiresCorrectToolForDrops` and the pickaxe mineable tag.

## Visual contract

`anidado.json` supplied by the user is now the canonical Core Booster geometry:

1. 0..16 outer cube;
2. 2.343..13.657 cube rotated 45 degrees;
3. 4..12 cube;
4. 5.172..10.828 cube rotated 45 degrees.

All four shells use the current crying-obsidian-glass palette texture. The inserted raw Core item is rendered inside the smallest cube as a single clockwise rotating, slightly tilted, full-bright hologram.

## Chunk-load fix

Unlike the dev.46 Improved Core renderer, material identity is persistent BlockEntity state with explicit update tag/packet sync. The center item therefore does not depend on an unrelated neighboring block update to become known to the client.

## Jade

Jade support is optional and compile-only. When Jade is installed, the Core Booster tooltip shows its inserted material and x1.50 amplification. Mirage Projector does not require Jade to load.
