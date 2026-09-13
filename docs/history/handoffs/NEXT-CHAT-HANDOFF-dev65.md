# Mirage Projector — next-chat handoff dev.65

Current source candidate: **0.1.0-dev.65**. Network protocol remains **18**.

## Baseline

dev.65 is built directly on dev.64. dev.64 remains under live QA for the Create Netherite Backtank synthetic chest. dev.65 intentionally does not change that renderer path.

## New implementation

Powered Crying Obsidian useful-range lighting is now implemented without a global light-engine rewrite. Energized Mature clusters derive a field tier from the incoming Core Booster relay. Width gain contributes up to two tiers and each effective Quartz/Radiance Booster adds one tier, capped at four. Internal invisible light nodes are distributed on three Manhattan shells at distances 8, 16 and 24. Tier profiles are `[12,0,0]`, `[14,7,0]`, `[15,10,4]`, and `[15,13,7]`; the strongest profile targets about 30 blocks useful reach.

Nodes use `mirage_projector:crying_light_node`, which has no BlockItem, recipe, loot or Creative exposure. It is replaceable, non-colliding, invisible and self-validates every 40 ticks. The source refreshes every existing 20-tick crystal optics cycle. Nodes only occupy air and path sampling refuses fully light-blocking terrain.

Small/Medium/Large buds still have zero world light. All energized crystal stages now use emissive rendering. Relay width/radiance shortens residual-ray cycles with a capped excitation factor.

## QA order

1. Build dev.65 on Windows.
2. Finish dev.64 Backtank QA first and report whether the synthetic chest now follows Ghost.
3. Test Mature Cluster directly in an active Beacon with no Booster; verify ordinary vanilla-radius light.
4. Test one Glass or non-Quartz Booster; verify modest range increase.
5. Test one, two, three and four Quartz Boosters; verify progressively larger field and max reach near 30 blocks.
6. Remove the Beacon base/Booster/cluster and confirm extended light contracts/disappears within a few seconds.
7. Place walls between cluster and candidate field directions; confirm the system does not plant light through fully opaque obstruction paths.
8. Verify buds are emissive but do not raise block light.
9. Verify no `crying_light_node` item appears in Creative and breaking/replacing space never produces a drop.

## Next pending feature after lighting QA

Per-component Equipment visibility remains the next P1 implementation: Humanoid Head/Chest/Legs/Feet/Main Hand/Off Hand plus Horse Saddle/Body Armor, render-only and preserving stored snapshots.
