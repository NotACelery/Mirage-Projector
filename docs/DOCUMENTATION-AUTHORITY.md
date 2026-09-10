# Mirage Projector — Documentation authority

Current documentation line: **0.1.0-dev.69**.

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
- `LIGHT-PROFILE-FOUNDATION.md`: current light-profile architecture plus explicitly reserved advanced-light placeholders.
- `ENTITY-AND-SNAPSHOTS.md`: Item/Banner/Entity virtual snapshot semantics.
- `ASSET-PIPELINE.md`: image/GIF import, storage and multiplayer transfer.
- `ROADMAP.md`: only work that remains genuinely unimplemented or unresolved.
- `QA-REGRESSION.md`: reusable regression suite.
- `DEV59-AUDIT.md`: audit/rake results retained as the maintenance baseline.
- `DEV60-CORE-BOOSTER-BEACON-RELAY.md`: current relay implementation and QA contract.
- `DEV61-MODDED-EQUIPMENT-RENDER-SAFETY.md`: confirmed crash diagnosis/fix contract for modded foil equipment in the deferred Entity renderer.
- `DEV62-LATE-ENTITY-DEPTH-STABILIZATION.md`: opacity disappearance/model-view/depth-order correction contract.
- `DEV63-CREATE-LAYERED-BACKTANK-COMPAT.md`: Create Netherite Backtank layered-armor Ghost compatibility history feeding the current renderer candidate.
- `DEV65-POWERED-CRYING-LIGHT-FIELD.md`: historical first extended-light implementation; topology/tier values are superseded by dev.67–69.
- `DEV67-BEACON-OPTICS-STABILIZATION.md`: current stage offsets, Core Booster activation plane and beam geometry baseline.
- `DEV68-CORE-EXTRACTION-AND-INSTANT-LIGHT-TEARDOWN.md`: current Core extraction guard and immediate source-removal reconciliation.
- `DEV69-OCCLUSION-AWARE-REFLECTED-LIGHT.md`: current Mature Cluster occlusion, reflected Booster identity and residual-ray coupling contract.
- `NEXT-CHAT-HANDOFF-dev64.md`: renderer continuation point for the current Create synthetic-chest QA branch.
- `NEXT-CHAT-HANDOFF-dev65.md`: dev.65 implementation/QA continuation point.
- `NEXT-CHAT-HANDOFF-dev66.md`: dev.66 continuation history.
- `NEXT-CHAT-HANDOFF-dev67.md`: historical optics continuation point.
- `NEXT-CHAT-HANDOFF-dev68.md`: historical interaction/teardown continuation point.
- `NEXT-CHAT-HANDOFF-dev69.md`: current reflected-light continuation/QA point.
- `CHANGELOG.md`: chronology.
- `DEVELOPMENT.md`: project/build conventions.
- `THIRD_PARTY_NOTICES.md`: bundled third-party notices.

## Historical archive

`archive/pre-dev59/` contains the pre-dev.59 wave notes and former authority files. `archive/post-dev59/` holds superseded continuation/handoff material from the audited line. Keep both for archaeology and migration reasoning, not for planning new work.

## Reference assets

`docs/reference/` contains design/reference files rather than behavioral authority. In particular:

- `docs/reference/core-booster/anidado.json` is the canonical nested Core Booster geometry supplied by the user;
- `docs/reference/canonical-amethyst/` contains the vanilla Amethyst item JSON/texture references supplied by the user and used as the visual law for Crying Obsidian buds/clusters.
