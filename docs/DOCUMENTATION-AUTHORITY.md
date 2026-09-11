# Mirage Projector — Documentation authority

Current documentation line: **0.1.0-dev.75d**.

This directory was reorganized in dev.59 so current implementation, current backlog and historical design notes are no longer mixed together.

## Authority order

When two documents disagree, use this order:

1. current source code and data/resource files;
2. `CURRENT-IMPLEMENTATION.md`;
3. focused current contracts in this directory;
4. `ROADMAP.md` for work that is genuinely still pending;
5. `CHANGELOG.md` for chronology;
6. `archive/pre-dev59/` and `archive/post-dev59/` only as historical evidence.

Historical files do not become current requirements merely because they contain the word "frozen" or "authoritative". Their authority ended when dev.59 archived them.

## Current active documents

- `CURRENT-IMPLEMENTATION.md`: what exists now.
- `ARCHITECTURE.md`: code/system boundaries.
- `REGISTRY-INVENTORY.md`: active and compatibility-only registry entries.
- `POWER-AND-CHASSIS.md`: chassis geometry and PU rules.
- `CORE-BOOSTER-AND-UPGRADES.md`: Core Booster and state-preserving crafting.
- `CRYING-OBSIDIAN.md`: shard, crystal growth, Beacon absorption and Obsidian Spike.
- `MIRAGE-LIGHT-ENGINE.md`: **current focused light authority** — source/profile/solver math, occlusion, detour decay, chunk/network lifecycle, diagnostics, migration, performance and dev.76 boundary.
- `LIGHT-PROFILE-FOUNDATION.md`: compact profile/reserved-mode compatibility contract; defer to `MIRAGE-LIGHT-ENGINE.md` for current runtime details.
- `ENTITY-AND-SNAPSHOTS.md`: Item/Banner/Entity virtual snapshot semantics.
- `ASSET-PIPELINE.md`: image/GIF import, storage and multiplayer transfer.
- `ROADMAP.md`: only work that remains genuinely unimplemented or unresolved.
- `QA-REGRESSION.md`: reusable regression suite.
- `DEV59-AUDIT.md`: audit/rake results retained as the maintenance baseline.
- `DEV60-CORE-BOOSTER-BEACON-RELAY.md`: current relay implementation and QA contract.
- `DEV61-MODDED-EQUIPMENT-RENDER-SAFETY.md`: confirmed crash diagnosis/fix contract for modded foil equipment in the deferred Entity renderer.
- `DEV62-LATE-ENTITY-DEPTH-STABILIZATION.md`: opacity disappearance/model-view/depth-order correction contract.
- `DEV63-CREATE-LAYERED-BACKTANK-COMPAT.md`: Create Netherite Backtank layered-armor Ghost compatibility history feeding the current renderer candidate.
- `DEV65-POWERED-CRYING-LIGHT-FIELD.md`: historical first physical extended-light implementation; runtime topology is superseded by the dev.74–75d virtual Mirage Light Engine.
- `DEV67-BEACON-OPTICS-STABILIZATION.md`: current stage offsets, Core Booster activation plane and beam geometry baseline.
- `DEV68-CORE-EXTRACTION-AND-INSTANT-LIGHT-TEARDOWN.md`: current Core extraction guard and immediate source-removal reconciliation.
- `DEV69-OCCLUSION-AWARE-REFLECTED-LIGHT.md`: historical first occlusion-aware reflected-light pass; its long Glass face-diagonal world-light branches are superseded by dev.70.
- `DEV70-INSTANT-OCCLUSION-REBUILD.md`: historical physical-relay occlusion pass; its same-tick invalidation idea survives in the current event-driven virtual-field lifecycle, while axial-node topology is superseded.
- `DEV71-EQUIPMENT-VISIBILITY.md`: current render-only Humanoid/Horse per-channel visibility and persistence contract.
- `DEV72-ENTITY-ENVELOPE-AND-FRUSTUM.md`: current Entity bounds/nameplate/frustum hardening contract.
- `DEV73-EXPLICIT-HALF-DECAY-RELAYS.md`: historical final physical-relay half-decay attempt; live QA motivated the dev.74 architecture change.
- `DEV74-MIRAGE-LIGHT-ENGINE-FOUNDATION.md`: validated foundation/history for the causal fixed-point solver.
- `DEV75A-AUTHORITATIVE-VIRTUAL-LIGHT-MIDPOINT.md`: recoverable midpoint/history retained because dev.75 was intentionally split.
- `DEV75B-AUTHORITATIVE-VIRTUAL-LIGHT-LIFECYCLE.md`: dev.75b lifecycle implementation history retained and accumulated.
- `DEV75C-OCCLUSION-OPACITY-FIX.md`: dev.75c vanilla destination-opacity correction.
- `DEV75D-DETOUR-PENALTY-AND-CONSOLIDATION.md`: current static-light refinement/consolidation before dev.76.
- `DEV75D-STATIC-QA.md`: static verifier/resource/package checks and the explicit boundary before build-clean acceptance.
- `NEXT-CHAT-HANDOFF-dev64.md`: renderer continuation point for the current Create synthetic-chest QA branch.
- `NEXT-CHAT-HANDOFF-dev65.md`: dev.65 implementation/QA continuation point.
- `NEXT-CHAT-HANDOFF-dev66.md`: dev.66 continuation history.
- `NEXT-CHAT-HANDOFF-dev67.md`: historical optics continuation point.
- `NEXT-CHAT-HANDOFF-dev68.md`: historical interaction/teardown continuation point.
- `NEXT-CHAT-HANDOFF-dev69.md`: historical reflected-light continuation point.
- `NEXT-CHAT-HANDOFF-dev70.md`: historical occlusion/refresh QA continuation point.
- `NEXT-CHAT-HANDOFF-dev71.md`: historical equipment-visibility continuation point.
- `NEXT-CHAT-HANDOFF-dev72.md`: historical P2 renderer/entity continuation point.
- `NEXT-CHAT-HANDOFF-dev73.md`: historical continuation point that recorded the physical-relay QA failures.
- `NEXT-CHAT-HANDOFF-dev74.md`: historical foundation-to-authority continuation point.
- `NEXT-CHAT-HANDOFF-dev75b.md`: historical lifecycle continuation point.
- `NEXT-CHAT-HANDOFF-dev75c.md`: historical wall-opacity correction handoff.
- `NEXT-CHAT-HANDOFF-dev75d.md`: **current continuation point for dev.76**.
- `CHANGELOG.md`: chronology.
- `DEVELOPMENT.md`: project/build conventions.
- `THIRD_PARTY_NOTICES.md`: bundled third-party notices.

## Historical archive

`archive/pre-dev59/` contains the pre-dev.59 wave notes and former authority files. `archive/post-dev59/` holds superseded continuation/handoff material from the audited line. Keep both for archaeology and migration reasoning, not for planning new work.

## Reference assets

`docs/reference/` contains design/reference files rather than behavioral authority. In particular:

- `docs/reference/core-booster/anidado.json` is the canonical nested Core Booster geometry supplied by the user;
- `docs/reference/canonical-amethyst/` contains the vanilla Amethyst item JSON/texture references supplied by the user and used as the visual law for Crying Obsidian buds/clusters.


## Versioned roadmap consolidation — post-dev.75d

`ROADMAP.md` is now the index rather than one monolithic waitlist. Active backlog authority is split across:

1. `WAITLIST-GENERAL.md`;
2. `WAITLIST-1.0.0.md`;
3. `WAITLIST-1.1.0.md`;
4. `WAITLIST-1.2.0.md`;
5. `CREATE-BRIDGE-ROADMAP.md` for the optional Create addon;
6. `FUTURE-PROOFING-1.0.0.md` for extension seams that should be established before stable 1.0.0.

`VERSION-SCOPE.md` is authoritative for which public expansion owns a feature when historical documents disagree.
