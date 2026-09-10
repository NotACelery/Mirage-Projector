# Mirage Projector — next-chat handoff after dev.51

Current source candidate: **0.1.0-dev.51**, protocol 18.

## Carried-forward confirmed state

- dev.45 compiled successfully on the user Windows setup and Wide/Tall projection envelopes were verified visually in-game.
- dev.50 boots successfully after removal of the invalid RenderLayer redirect.
- dev.50 proved the Crying Obsidian random-tick dispatch now executes: a Medium bud eventually appeared, revealing that the remaining problem was eligibility/balance rather than a dead tick hook.

## dev.51 changes

- flowing + source lava accepted via `FluidTags.LAVA`;
- initial nucleation 1/5 eligible random tick;
- vanilla-amethyst-style cross models restored for all Crying Obsidian growth stages;
- prismarine-shard silhouette restored for Crying Obsidian Shard, with purple gradient retained and texture centered;
- Obsidian Spike inventory icon scaled down;
- Improved Core outer shell expanded to full block;
- Improved Cores converted to `ImprovedCoreBlock` + stateless `ImprovedCoreBlockEntity`;
- `ImprovedCoreRenderer` renders one actual center material item, clockwise rotating and full-bright, instead of three static planes.

## Branding invariant

Public mod credit is **Celerbi**. Public mod metadata/description must not contain a repository/hosting link.

## Pending user QA

- Windows build;
- source/flowing lava nucleation speed;
- bud/cluster model appearance;
- shard centering and silhouette;
- spike inventory bounds;
- Improved Glass/Quartz center visibility;
- Improved Core rotating-center behavior.
