# Mirage Projector — next-chat handoff dev.70

Current source candidate: **0.1.0-dev.70**. Network protocol remains **18**.

dev.70 is the corrective pass after live dev.69 QA showed slow block-change response and wall leakage from long Glass diagonal Light Nodes.

Current static Mature world-light contract:

- only an energized Mature Cluster emits/relays world light;
- six axial half-decay branches only;
- no-Booster unobstructed axial baseline remains about 29 blocks;
- Quartz/Radiance is the dominant static reach amplifier;
- Diamond/Focus adds smaller axial reach;
- Glass/Diffusion remains in reflected residual-ray geometry but does not create long static diagonal relays;
- Amethyst/Resonance and Netherite/Inversion remain residual-ray dynamics;
- full opaque blockers hard-stop the rest of an axial branch;
- stale downstream nodes reconcile immediately and overlap keeps the strongest surviving source.

Terrain edits from block place/break events are coalesced until `LevelTickEvent.Post`, then every impacted indexed Mature source refreshes once from the final world state. This is intended to remove the old visible 20–40 tick response delay. The periodic crystal/node ticks remain fallbacks.

dev.70 also performs one-time reconciliation of old dev.69 diagonal nodes when a Mature source first refreshes/removes, so upgrading the same QA world should not leave stale side relays behind.

Required QA before build-clean: Windows Java 21 compile; place/remove opaque wall at close and far axial distances; verify same-tick source-node teardown/rebuild; Glass must not create static side/diagonal nodes; Quartz/Diamond range identity; multiple overlapping Mature sources; legacy dev.69 world migration; Create Netherite Backtank Ghost regression.

After this light-field correction is accepted, return to the existing P1: render-only per-component Equipment visibility without deleting stored snapshots.
