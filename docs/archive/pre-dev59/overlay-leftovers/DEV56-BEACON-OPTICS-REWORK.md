# dev.56 — Beacon optics rework

## Contract implemented

- Residual Crying Obsidian rays originate from the exact center of the crystal.
- Residual rays are 50% narrower and approximately 66% shorter than the previous mature-cluster rays.
- Residual rays appear instantly at full length, hold for 20 ticks, then retract while fading for 20 ticks.
- Stage scaling below MATURE is exactly 75% / 50% / 25% for ray width and length, with matching frequency reduction.
- Vanilla beacon beam is cut from pixel 0 (the bottom face) of every Crying Obsidian bud/cluster block and never renders through the crystal model.
- Buds attenuate the beam above them to 75% / 50% / 25% transmission, including visible brightness/alpha attenuation.
- Buds no longer emit block light while energized.
- A MATURE cluster directly above an active Beacon suppresses the vertical beacon beam completely and suppresses the Beacon block light while the energized cluster remains present. The mature cluster itself becomes the light source.
- Energized mature cluster texture has a stronger purple/lavender palette.

## QA targets

1. Verify all residual ray origins are visually centered.
2. Verify no vertical beacon pixels pass through any Crying Obsidian stage.
3. Verify bud attenuation is clearly visible.
4. Verify direct mature-cluster placement turns off the Beacon's own light and beam while the cluster remains luminous.
5. Verify removing the mature cluster immediately restores Beacon light.
