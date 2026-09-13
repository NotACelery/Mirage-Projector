# Mirage Projector — next-chat handoff after dev.60

Current source candidate: **0.1.0-dev.60**.

## Added in this wave

Loaded Core Boosters now modify active Beacon columns. The relay state is cumulative, limited to four effective Boosters and preserves stained-glass hue. Glass = Diffusion, Quartz = Radiance, Amethyst = Resonance, Diamond = Focus, Netherite = Inversion.

## Still pending

P0 remains acceptance/fix of semi-transparent Entity composition with clouds and modded armor such as Create Netherite Backtank.

After relay QA:

1. useful-range energized Crying Obsidian lighting;
2. per-component Equipment visibility;
3. renderer/entity hardening;
4. release hardening;
5. Scan Codex in 1.1.0+.

## QA gate

dev.60 is not build-clean until Windows `build.bat` succeeds and the five relay identities plus stacking/caps are verified in-game.
