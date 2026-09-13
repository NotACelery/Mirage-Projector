# NEXT CHAT HANDOFF — Mirage Projector dev.75b

Baseline: **0.1.0-dev.75b source candidate**. Network protocol: **20**.

## What is now implemented

The Mature Crying Obsidian Cluster uses the authoritative virtual Mirage Light Engine. No current runtime path creates physical `crying_light_node` relays. Server and clients solve compact synchronized source descriptors locally and merge Mirage light at read time with vanilla. dev.75b adds tracked-chunk source delivery, source-origin unload teardown, server/client chunk-arrival rebuilds, render-section dirtying, orphan legacy-node chunk scans, same-level respawn resync and broader event-driven terrain invalidation. Aggregate source replacement is one-pass across touched sections.

## Do not regress

- fixed-point no-Booster profile: `15,15,14,14,...,1,1` over outward distances 1–30;
- six-neighbor causal solver + vanilla edge occlusion;
- no Mirage values fed into vanilla `BlockLightEngine` propagation;
- physical Crying Light Nodes migration-only;
- protocol 20 source descriptor payload;
- dev.70 same-tick wall invalidation behavior;
- dev.71 equipment visibility;
- dev.72 Entity bounds/nameplate/frustum hardening.

## Immediate next action

User must run Windows Java 21 `build.bat`. If clean, do the dev.75b QA matrix in `DEV75B-AUTHORITATIVE-VIRTUAL-LIGHT-LIFECYCLE.md`, especially Simple Light Level, full walls, chunk borders/source unload, relog/dimension/respawn and multiplayer tracking.

If a third-party light overlay still shows vanilla-only values, first identify its exact query path. Do not hook low-level vanilla `LayerLightEventListener#getLightValue` globally because returning Mirage there can re-enter vanilla light propagation and recreate the secondary-emitter problem.

After static backend QA is accepted, next major lighting work can start `DYNAMIC_VISUAL`/portable sources and directional/projected-map profiles. P2 renderer/entity compatibility work remains in the active roadmap in parallel.
