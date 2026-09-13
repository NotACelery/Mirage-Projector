# Mirage Projector — next-chat handoff after dev.61

Current source candidate: **0.1.0-dev.61**. Network protocol remains 18.

## Baseline

- dev.59: full audit/rake baseline.
- dev.60: Core Booster Beacon relay; reached a runnable in-game build after stale-source cleanup.
- dev.61: targeted runtime fix for Create Backtank/foil multi-buffer crash in deferred Ghost Entity rendering.

## Root cause fixed in dev.61

The late Entity Ghost pass previously used one shared byte builder. Create's Backtank can use a simultaneous foil/glint + base-model `VertexMultiConsumer`; requesting the second RenderType caused the shared BufferSource to finish the first builder while Create still wrote to it, producing `IllegalStateException: Not building!`. dev.61 reserves independent buffers for glint and known Ghost atlas render types.

## Next QA

- Create Backtank projection with Ghost enabled;
- same test with foil/glint active;
- Backtank must inherit Ghost/Tint;
- clouds must not overwrite translucent projections;
- water must not regain depth holes;
- vanilla armor/glint/trims/held items must remain sane.

No new gameplay feature should supersede this P0 renderer regression until the above is checked.
