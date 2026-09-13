# Documentation Authority — Mirage Projector 1.0.0

This file defines which documents describe the current stable product.

## Current authority

Use these documents for 1.0.0 behavior and maintenance:

1. `README.md` — public overview, requirements and feature summary.
2. `docs/CURRENT-IMPLEMENTATION.md` — canonical runtime behavior.
3. `docs/ARCHITECTURE.md` — internal architecture and extension boundaries.
4. `docs/REGISTRY-INVENTORY.md` — canonical registered IDs and migration-only IDs.
5. `docs/POWER-AND-CHASSIS.md` — chassis profiles and Projection Power.
6. `docs/CORE-BOOSTER-AND-UPGRADES.md` — Core Booster semantics and relay identities.
7. `docs/CRYING-OBSIDIAN.md` — renewable crystal/Beacon/light behavior.
8. `docs/ENTITY-AND-SNAPSHOTS.md` — Entity Scan/state contracts.
9. `docs/ASSET-PIPELINE.md` — image/GIF asset synchronization.
10. `docs/MIRAGE-LIGHT-ENGINE.md` — static Mirage Light architecture.
11. `docs/PROJECTOR-ACTIVE-STATE-UX.md` — ON/OFF and active-source behavior.
12. `docs/QA-REGRESSION.md` — reusable regression gates.
13. `docs/ROADMAP.md` — post-1.0 release roadmap.
14. `docs/WAITLIST-1.1.0.md`, `docs/WAITLIST-1.2.0.md`, `docs/WAITLIST-GENERAL.md` — future work only.
15. `docs/CHANGELOG.md` — chronological history.
16. `docs/RELEASE-1.0.0-AUDIT.md` — final cleanup, static-audit and release-boundary record.

## Historical material

Everything under `docs/history/` and `docs/archive/` is non-authoritative historical material. It may intentionally describe removed IDs, temporary implementations, failed experiments, old protocol versions or superseded behavior.

Historical files are preserved for archaeology/migration context only. Do not use them to infer current runtime behavior unless a current authority document explicitly references a compatibility reason.

## Conflict rule

If active documents disagree:

1. current source/runtime behavior wins;
2. `CURRENT-IMPLEMENTATION.md` is the primary written authority;
3. specialized active documents override older/general wording in their own domain;
4. `ROADMAP.md` describes future intent and must never be read as current functionality.

