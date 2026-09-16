# Documentation Authority — Mirage Projector 1.0.32

This file defines which documents describe the current stable product.

## Current authority

Use these documents for current 1.0.32 behavior and 1.0.x maintenance:

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
16. `docs/RELEASE-1.0.0-AUDIT.md` — historical initial-release cleanup/audit record.
17. `docs/RELEASE-1.0.1-PLACEMENT.md` — initial placement-foundation snapshot.
18. `docs/RELEASE-1.0.2-PRISM-PLACEMENT.md` — historical Prism placement correction.
19. `docs/RELEASE-1.0.3-COMPILE-HOTFIX.md` — compile-only repair over 1.0.2.
20. `docs/RELEASE-1.0.4-FIXED-TAB-PLACEMENT.md` — fixed-tab placement/UI contract.
21. `docs/RELEASE-1.0.5-DYNAMIC-LIGHT-FOUNDATION.md` — current dynamic/mobile Mirage Light foundation contract.
22. `docs/RELEASE-1.0.6-PRISM-COMPACTION.md` — Mirage Prism spacing and four-tab UI refinement.
23. `docs/RELEASE-1.0.7-GLOW-DUST.md` — historical first rechargeable Glow Dust / Beacon charging foundation.
24. `docs/RELEASE-1.0.8-BATTERY-POLISH.md` — rechargeable-media / Light Battery foundation and recovered charging fixes.
25. `docs/RELEASE-1.0.9-LIGHT-PROJECTOR-FOUNDATION.md` — current physical light-projector / first DYNAMIC_VISUAL consumer contract.
26. `docs/RELEASE-1.0.10-LANTERN.md` — handheld Mirage Lantern / player-following DYNAMIC_VISUAL consumer contract.
27. `docs/RELEASE-1.0.11-HANDHELD-PROJECTOR.md` — handheld hologram projector / copied portable profile foundation.
28. `docs/RELEASE-1.0.12-PERSISTENT-PORTABLE-STATE.md` — persistent inventory-active portable state / multiplayer sync / Creative Battery contract.
29. `docs/RELEASE-1.0.13-SHOULDER-EQUIPMENT.md` — Mirage Equipment / Arm Strap / Shoulder Slot foundation.
30. `docs/RELEASE-1.0.14-SHOULDER-BATTERY-POUCH.md` — Arm Strap Battery Pouch / upgrade / Auto Battery Swap foundation.
31. `docs/RELEASE-1.0.15-CHARGING-STATION.md` — dedicated directional Beacon Charging Station / batch logistics foundation.
32. `docs/RELEASE-1.0.16-WAR-BANNER.md` — portable overhead War Banner presentation / directional and per-viewer billboard controls.
33. `docs/RELEASE-1.0.17-SCAN-CODEX.md` — persistent Scan Codex library / metadata browser / exact scan-selection foundation.
34. `docs/RELEASE-1.0.18-MASSIVE-STABILIZATION.md` — QA-driven portable/Shoulder/Charging/Codex stabilization and GUI contract.
35. `docs/RELEASE-1.0.19-QA-FOLLOWUP.md` — runtime-QA interaction, GUI, Creative inventory, Charging Station, Codex background and initial visual-light renderer bridge corrections.
36. `docs/RELEASE-1.0.20-SODIUM-LIGHT-BRIDGE-HOTFIX.md` — Sodium `LevelSlice` crash correction for the packed-light visual bridge.
37. `docs/RELEASE-1.0.21-RUNTIME-QA-CORRECTIONS.md` — visual-light mesh invalidation, Shoulder Device transform and Hand Projector rendering corrections.
38. `docs/RELEASE-1.0.22-DUPLICATING-LECTERN.md` — Scan Codex selected-capture to physical-card copy foundation.
39. `docs/RELEASE-1.0.23-VANILLA-LECTERN-CODEX.md` — vanilla Lectern Codex workstation / two-page browser correction.
40. `docs/RELEASE-1.0.24-CODEX-LIBRARY-CARD-REWORK.md` — current Codex/card/import/portable-UI contract.
41. `docs/RELEASE-1.0.25-ANCHOR-CHASSIS-FOUNDATION.md` — Table/Wall anchor and capability foundation.
42. `docs/RELEASE-1.0.26-PRESENTATION-CONTROL.md` — current automatic deck / paired Data-show remote control contract.
43. `docs/RELEASE-1.0.30-UX-RUNTIME-WAVE.md` — portable-source, Shoulder interaction, live Wall placement, remote input, Entity workspace and Codex runtime-QA contract.
44. `docs/RELEASE-1.0.31-TABLE-RUNTIME-REBUILD.md` — current canonical workspace routing, Table runtime/menu authority and Codex render correction contract.
45. `docs/RELEASE-1.0.32-SURVIVAL-PROGRESSION.md` — Survival recipe, Flashlight identity/placeable form and wall-illumination chassis contract.

## Historical material

Everything under `docs/history/` and `docs/archive/` is non-authoritative historical material. It may intentionally describe removed IDs, temporary implementations, failed experiments, old protocol versions or superseded behavior.

Historical files are preserved for archaeology/migration context only. Do not use them to infer current runtime behavior unless a current authority document explicitly references a compatibility reason.

## Conflict rule

If active documents disagree:

1. current source/runtime behavior wins;
2. `CURRENT-IMPLEMENTATION.md` is the primary written authority;
3. specialized active documents override older/general wording in their own domain;
4. `ROADMAP.md` describes future intent and must never be read as current functionality.

