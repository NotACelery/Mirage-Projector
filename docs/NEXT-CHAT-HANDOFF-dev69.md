# Mirage Projector — next-chat handoff dev.69

Current source candidate: **0.1.0-dev.69**. Network protocol remains **18**.

Recovered baseline: dev.68 guards Core Booster extraction and performs immediate overlap-aware Mature Cluster light teardown. dev.69 then implements the reflected-light definition that was being discussed when the previous chat ended.

The Mature Cluster remains the only Crying Obsidian world-light source. With zero Boosters it keeps the six-axis half-decay baseline (~29-block unobstructed axial reach). Reflected node acceptance is now source/geometry aware: fully opaque blockers terminate downstream candidates and partial light blockers add attenuation. Mirage deliberately does not implement per-face light incidence; Minecraft's own block-light propagation, AO and face shading remain authoritative after accepted nodes are placed.

Core Booster reflection is material-specific. Generic incoming-beam `widthScale` no longer becomes a static light tier. Glass = bounded diagonal Diffusion; Quartz = dominant Radiance/range; Amethyst = reflected Resonance/cadence; Diamond = smaller focused axial reach plus Diffusion cancellation; Netherite = reflected rotation Inversion. Residual ray radius, brightness, length and excitation use dedicated reflected relay helpers.

Performance change: one Mature refresh resolves its Beacon relay once into a `SourceFieldSpec`; it is reused for all candidates instead of rescanning the Beacon column once per node.

Important QA before build-clean acceptance: compile on Windows/Java 21; wall/backside shadow test; partial-opacity attenuation test; Glass diagonal coverage; Quartz vs Diamond range identity; Amethyst/Netherite no-static-range check; mixed Booster contraction; dev.68 immediate teardown/overlap; residual ray clipping; Create Netherite Backtank Ghost regression.

After the reflected-light branch is accepted, return to the existing P1: render-only per-component Equipment visibility without deleting stored snapshots.
