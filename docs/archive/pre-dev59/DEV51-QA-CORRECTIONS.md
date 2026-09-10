# dev.51 — QA corrections

This wave responds to the in-game dev.50 screenshots and accelerated random-tick test.

## Crying Obsidian growth

The production bug was twofold:

1. the growth hook finally ran in dev.50, but the eligibility helper checked the concrete `Fluids.LAVA` source fluid rather than the Lava fluid tag, so flowing lava was rejected;
2. the 1/32 initial nucleation gate was much too slow for the intended renewable loop.

dev.51 uses `FluidTags.LAVA` and accepts source **or flowing** lava directly above Crying Obsidian. Small-Bud nucleation is 1/5 per eligible Crying-Obsidian random tick. Existing Small -> Medium -> Large -> Mature rates remain 1/24, 1/16 and 1/12 for this QA wave.

## Visual corrections

- Buds and cluster restore the exact pre-regression amethyst-style `minecraft:block/cross` + `minecraft:cutout` model family with Mirage textures; their item models again inherit those block models.
- Shard uses the prismarine-shard silhouette, centered, with the approved purple gradient.
- Obsidian Spike inventory transform is reduced to stay inside slot borders.
- Improved Core outer shell fills the full block volume.
- Placed Improved Cores no longer fake the central material with three orthogonal planes. A new stateless block entity + renderer draws one actual material item rotating clockwise at the center with the same 18° tilt and 0.32 scale used by the projector chamber.

## QA targets

1. Flowing lava and source lava should both nucleate downward buds.
2. At `/tick rate 2000`, a valid test array should produce Small Buds quickly enough to observe without waiting minutes.
3. Bud/cluster geometry should visually match vanilla amethyst geometry, only recolored/retextured.
4. Crying Obsidian Shard should match the prismarine-shard silhouette and sit centered in the slot.
5. Obsidian Spike should no longer touch slot borders.
6. All five placed Improved Cores should fill the block footprint and render exactly one rotating center item. Glass and Quartz must be visible.
