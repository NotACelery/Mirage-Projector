# NEXT CHAT HANDOFF — 0.1.0-dev.77

## Baseline
`dev.76h` is considered the QA-confirmed static Mirage-light stabilization baseline: repeated relogs converged correctly within roughly <=2 seconds near an energized Mature Cluster, with no observed FPS regression. Do not remove the section-boundary invalidation fix that invalidates both the changed light section and the section immediately below.

## dev.77 scope
Post-fix cleanup and Core Booster identity polish only. The server-authoritative STATIC_WORLD architecture, atomic source publication, revisioned chunk snapshots, client chunk handshake and source watchdog remain intact. Protocol remains 25.

## Core Booster naming
- Empty item: `Core Booster`.
- Loaded item: `<Material> Core Booster` (localized).
- Packed drops/pick-block/Creative preloaded stacks inherit the dynamic name automatically from BLOCK_ENTITY_DATA.

## Mature static-light identity
At most four Beacon Boosters are effective.
- Glass / Diffusion: -1 conceptual tier (-2 open blocks) per effective unopposed Diffusion tier; if Diffusion dominates Focus, detourExtraCost goes 1 -> 0 for softer wrapping around finite obstacles.
- Quartz / Radiance: +1 conceptual tier (+2 open blocks) per Quartz.
- Amethyst / Resonance: no static reach bonus.
- Diamond / Focus: stronger detour shadows immediately; +1 conceptual tier only per 2 Diamonds.
- Netherite / Inversion: no static reach bonus.

Base no-Booster profile remains exact half-decay. Glass and Diamond continue to counteract through reflectedDiffusionTier = max(glass - diamond, 0).

## Beacon visual identity
Per-Booster width increments are no longer generic: Glass +0.35, Quartz +0.15, Amethyst +0.20, Diamond +0.08, Netherite +0.20 before existing caps/secondary modifiers. Quartz brightness, Amethyst resonance speed, Diamond inner focus and Netherite reversal remain.

## Conservative optimization
- allDependencyChunksQueryable() directly loops exact chunk bounds instead of allocating a temporary Set on every pending retry.
- watchdog signature and revision manifest scan exact origin±radius chunk bounds rather than oversized symmetric +/-N windows.
- No heartbeat/retry cadence was changed yet; behavior proven by dev.76h QA is intentionally preserved.

## Required QA
1. Windows Java 21 build.
2. Empty/loaded Core Booster names in inventory, pick-block and drops.
3. One Glass / Quartz / Amethyst / Diamond / Netherite Booster individually on a Beacon below a Mature Cluster.
4. Confirm open reach identities and Diamond/Glass obstacle behavior.
5. Mixed Glass + Diamond cancellation behavior.
6. Several relogs to ensure dev.76h light convergence did not regress.
