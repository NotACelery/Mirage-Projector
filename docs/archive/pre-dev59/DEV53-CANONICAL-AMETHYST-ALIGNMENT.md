# dev.53 — Canonical amethyst alignment for Crying Obsidian crystals

## Purpose

Apply the user-provided vanilla Amethyst item JSONs and 16x16 textures as the canonical source for Crying Obsidian buds and cluster visuals.

## Included

1. Rebuilt the Crying Obsidian bud and cluster textures directly from the uploaded vanilla Amethyst sprites, preserving their exact 16x16 silhouette and shading ranks, then recoloring them into the Crying Obsidian palette.
2. Replaced the Crying Obsidian item models with the same display/transforms as the uploaded vanilla item JSONs, adapted only to the `mirage_projector` namespace.
3. Left the dev.51/dev.52 growth tuning unchanged because the user reported the progression timing now feels acceptable.
4. Refined the Improved Core rotated middle frame so it sits further inside the outer shell and uses a more visible frame texture.

## QA targets

- Crying Obsidian bud/cluster item sprites should now match the vanilla Amethyst family silhouette exactly, only recolored.
- Bud/cluster transforms should feel identical to vanilla Amethyst.
- Improved Core middle frame should no longer poke outside the block and should keep more visible borders.
