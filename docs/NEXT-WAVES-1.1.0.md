# Mirage Projector — Remaining 1.1.0 waves

Baseline after this checkpoint: **1.0.69**
Network protocol: **44**
ProjectionSettings format: **4**

This file is the recovery-oriented implementation order. `WAITLIST-1.1.0.md` remains the detailed requirement source; this document answers **what comes next and what is intentionally last**.

## Delivered foundations that must not be re-opened casually

- Survival recipes + native JEI/EMI exposure for current Survival devices.
- Mirage Flashlight rename/energy loop/temporary placement and 1.0.34 visual/Ambient cleanup.
- Table dedicated runtime, offsets/tilt/central pivot and workspace-navigation vs active-mode separation.
- Presentation **Mirage Wall Projector** + deck/autoplay/remote. There is no separate wall illumination chassis in current scope.
- Player scan identity/layers cleanup, Scan Codex library/card/Lectern foundations.
- End Resonance portal, transfer and runtime QA foundation for Dragon Egg on Field/Prism/Table.
- Mirage Entity Scanner, persistent Codex capture flow, scanner HUD and scanner inventory safety.

## Wave 1 — End Resonance transfer — delivered and runtime-tested

- Shared server collision envelope matching Field 2×3, Prism 2×3×2 and Table 3×3 geometry.
- Players, ordinary living entities and dropped items.
- End platform arrival and Overworld return, with per-entity anti-loop cooldown.
- Runtime-tested safe-platform arrival across multiple worlds.

## Wave 2 — End Resonance edge-entity release QA

- Projectiles, Primed TNT, FallingBlockEntity and vehicle/passenger graphs: document the supported set after dedicated multiplayer/restart QA.
- Automation, break/explosion, hopper insertion/extraction and Dragon Egg anti-dup audit.

## Wave 3 — Dynamic Mirage Light release QA

- Re-run Focus/Flood/Ambient behavior, many-emitter and multiplayer stress tests after release-candidate packaging.
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

## Wave 6 — Mirage Light Projector aiming — retained only if it remains a release requirement

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
