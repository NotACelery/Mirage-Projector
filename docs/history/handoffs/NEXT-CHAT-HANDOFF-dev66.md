# Mirage Projector — next-chat handoff dev.66

Current source candidate: **0.1.0-dev.66**. Network protocol remains **18**.

## What dev.66 changes

Adds a reusable advanced-light profile contract without changing the active dev.65 Crying Obsidian placement algorithm.

New code:

- `light/LightDecayMode.java`;
- `light/LightProfile.java`;
- `light/LightProfileMath.java`.

Current runtime-enabled modes are `VANILLA` and `EXTEND`. Reserved placeholders are `CONCENTRATE`, `DIRECTIONAL_SPOT` and `ROTATING_DIRECTIONAL_SPOT`.

The reserved modes are intentionally not connected to world mutation. Concentrated and directional lighting need a future source-suppression contract because additive nodes cannot subtract ordinary source light. Dynamic directional lighting also needs a bounded/delta-based node planner rather than full cube rescans.

## Active Crying Obsidian behavior

The dev.65 field remains authoritative: Mirage-owned invisible nodes, at most 54 candidates per Mature source, loaded-chunk/build-height checks, obstacle sampling, strongest-overlap resolution and self-cleaning validation. `profileForRelay(...)` now exposes the same relay result as a reusable `LightProfile` without altering those rings.

## Parallel QA still open

1. Build dev.66 on Windows.
2. Validate dev.65 Mature lighting tiers and cleanup.
3. Validate dev.64 Create Netherite Backtank Ghost behavior carried unchanged through dev.66.
4. If renderer QA passes, the next scheduled gameplay branch remains per-component Equipment visibility.

## Tooling/document layout

No file was removed or moved in dev.66. `build.bat` and the cleaner are intentionally unchanged. `README.md` remains the only primary Markdown document at project root; project documentation remains under `/docs`.
