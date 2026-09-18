# Mirage Projector — Remaining 1.1.0 waves

Baseline after this checkpoint: **1.0.34**
Network protocol: **43**
ProjectionSettings format: **4**

This file is the recovery-oriented implementation order. `WAITLIST-1.1.0.md` remains the detailed requirement source; this document answers **what comes next and what is intentionally last**.

## Delivered foundations that must not be re-opened casually

- Survival recipes + native JEI/EMI exposure for current Survival devices.
- Mirage Flashlight rename/energy loop/temporary placement and 1.0.34 visual/Ambient cleanup.
- Table dedicated runtime, offsets/tilt/central pivot and workspace-navigation vs active-mode separation.
- Presentation **Mirage Wall Projector** + deck/autoplay/remote. There is no separate wall illumination chassis in current scope.
- Player scan identity/layers cleanup, Scan Codex library/card/Lectern foundations.
- End Resonance state/control/render foundation for Dragon Egg on Field/Prism.

## Wave 1 — End Resonance transfer

- Shared server collision envelope matching Field 2×3 and Prism 2×3×2 geometry.
- Players, ordinary living entities and dropped items.
- Overworld/other dimensions → The End, and End → appropriate safe return destination.
- Per-entity cooldown/anti-loop.
- Safe destination resolution and obstruction handling.
- Save/reload/chunk-boundary QA.

## Wave 2 — End Resonance edge entities & safety

- Projectiles where dimension transfer is semantically safe.
- Primed TNT preserving fuse.
- FallingBlockEntity preserving block/state.
- Vehicles/passengers only if the full graph can transfer without duplication/desync; otherwise explicitly unsupported for 1.1.
- Automation, break/explosion, hopper insertion/extraction and Dragon Egg anti-dup audit.
- Multiplayer/restart stress QA.

## Wave 3 — Dynamic Mirage Light completeness

- Geometry invalidation when terrain changes around a stationary emitter.
- Focus/Flood/Ambient balance pass.
- Many-emitter performance and multiplayer stress tests.
- Sodium/vanilla renderer regression QA.

## Wave 4 — Projection source architecture completion

- Replace hard-coded Image/Item/Entity/Banner workspace enumeration with compatible registered-source enumeration.
- Freeze the capability/compatibility contract for third-party/addon sources.
- Keep Create Blueprint support optional; do not make it a 1.1 blocker.

## Wave 5 — Presentation/Codex release polish

- Dedicated Scan Codex item art (no vanilla-book placeholder).
- Large-library Codex performance QA.
- Final Table/Wall Projector art/presentation polish.
- Decide ceiling support: explicitly document Table-as-ceiling if sufficient, otherwise create a dedicated family only with a concrete gameplay reason.
- Optional slideshow transitions remain non-blocking.

## Wave 6 — Mirage Light Projector aiming — deliberately last

- Visible horizontal yaw.
- Visible vertical pitch/tilt.
- Physical head/model follows configured direction.
- Emission direction and visible lens stay perfectly synchronized.
- Ambient remains upward/multi-face and bypasses directional head aiming as defined.

This wave is intentionally last because it is a visual/physical behavior layer on top of an otherwise complete illumination system and must not delay core End Resonance/runtime architecture work.

## 1.1.0 release candidate

- Full current-line gates and clean Windows build.
- JEI-only, EMI-only and JEI+EMI QA.
- Dedicated server + multiplayer QA.
- Save migration / overlay cleanup / registry inventory audit.
- Final `WAITLIST-1.1.0.md`, `CURRENT-IMPLEMENTATION.md`, `ROADMAP.md`, README and release notes reconciliation.
- Promote to **1.1.0** only after every required item above is either delivered or explicitly removed from scope by a documented design decision.
