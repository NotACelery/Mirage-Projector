# Roadmap

Current implementation snapshot: **1.0.7**
Network protocol: **28**

1.0.0 remains the initial stable fixed-projector baseline. Normal development now advances through monotonically increasing `1.0.x` implementation snapshots; the second component changes to `1.1.0` only when the complete planned expansion is ready. 1.0.1–1.0.4 established the fixed-projector placement/UI groundwork. 1.0.5 made `DYNAMIC_VISUAL` operational on the client and added directional-cone solving. 1.0.6 tightens Mirage Prism carousel spacing, caps user radial expansion at ten blocks and consolidates the fixed settings UI to four tabs. 1.0.7 begins the rechargeable Glow Dust gameplay loop and Beacon/Core-Booster charging path.

## 1.1.0 — Portable light, projector expansion and Scan Codex

Primary themes:

- Dynamic/Mobile Mirage Light consumers built on the operational `DYNAMIC_VISUAL` client runtime delivered in 1.0.5;
- portable lanterns with Focus/Flood/Ambient/Off modes;
- Glow Dust rechargeable energy/battery loop;
- portable projectors plus wall/table/ceiling/presentation variants;
- Mirage Scan Codex: persistent searchable/filterable scan library, favorites and multiple distinct snapshots of the same entity type;
- dedicated scan-copy station/lectern workflow that creates physical projector-facing Entity Scan Cards;
- Dragon Egg / End Resonance special Core semantics for compatible Field/Prism chassis.

Detailed requirements live in `WAITLIST-1.1.0.md`.

## 1.2.0 — Interactive holograms

Primary theme: direct manipulation of rendered projections.

- grab/hold interaction;
- free 3D rotation extending the quaternion orientation used by the 1.0.x Tilt control;
- stable pivot/ownership/multiplayer rules;
- optional snapping and later position/pivot manipulation;
- source/chassis capability hooks rather than source-specific mouse math.

Detailed requirements live in `WAITLIST-1.2.0.md`.

## Optional/addon direction

Create Blueprint/schematic projection remains an optional bridge/addon direction rather than a required core feature. The 1.0 source registry/render-provider seams are intended to allow additions such as `BLUEPRINT` without rewriting the four built-in source families.

## Version progression rule

Normal implementation work advances through monotonically increasing `1.0.x` snapshots, even while pieces of the future 1.1 feature set are being built. The version becomes **1.1.0** only when that feature set is complete enough to ship as the next feature release. Internal `dev-X` labels are reserved for exceptional recovery/build snapshots rather than normal development. Preserve every shipped/relevant 1.0.x snapshot as a recoverable migration baseline.
