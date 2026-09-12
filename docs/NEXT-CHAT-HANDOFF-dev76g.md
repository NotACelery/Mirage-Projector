# NEXT CHAT HANDOFF — dev.76g

Baseline: `0.1.0-dev.76g`, network protocol 25.

## Primary change

Each energized Mature Cluster now owns a source-centric chunk watchdog. Normal radius-30 light watches a 5x5 window; boosted fields grow the watch window from their real radius. Chunk lifecycle/geometry changes trigger a complete rebuild from the source. A lightweight revision+section-count manifest is sent every second so clients request only stale/missing chunk snapshots.

## QA gate

Use the existing iron light-grid world and repeat world login several times without touching blocks. The complete field should self-heal without walking away, breaking a block, or manually replacing the Cluster. `/miragelight chunks` now reports the source watchdog radius in chunks.

If a chunk remains missing for more than ~2 seconds, capture the screenshot and run `/miragelight chunks` and `/miragelight stats` before changing any block.

## Important invariant

The watchdog does not decide whether light crosses a wall. It only detects when source dependencies changed. `MirageLightSolver` still enforces causal propagation, vanilla face/opacity occlusion, and detour penalty.
