# Mirage Projector — next-chat handoff after dev.53

Current source candidate: **0.1.0-dev.53**.

## What changed

This wave treated the user-uploaded vanilla Amethyst textures and item JSONs as the canonical law for Crying Obsidian crystal visuals.

### Crying Obsidian buds / cluster
- Rebuilt textures from the exact uploaded vanilla Amethyst 16x16 sprites.
- Preserved the silhouette/shading layout and recolored only into the Crying Obsidian palette.
- Replaced item models/transforms with namespace-adapted copies of the uploaded vanilla JSON definitions.

### Improved Core
- Tightened the rotated middle frame dimensions so it stays further inside the outer shell.
- Increased frame texture opacity/highlights to reduce border loss from different angles.

## User QA targets

1. Verify Crying Obsidian bud/cluster items now mirror vanilla Amethyst silhouettes exactly.
2. Verify the in-world Crying Obsidian crystal presentation is acceptable with the canonical recolored sprites.
3. Verify the Improved Core middle frame no longer escapes the shell and is more visually stable.
