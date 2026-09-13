# Roadmap

Current stable release: **1.0.0**
Network protocol: **27**

1.0.0 is feature-frozen. New gameplay belongs to later release lines; the stable branch should receive only concrete bug fixes, compatibility fixes and documentation corrections.

## 1.1.0 — Portable light, projector expansion and Scan Codex

Primary themes:

- Dynamic/Mobile Mirage Light consumers built on the existing `DYNAMIC_VISUAL` boundary;
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
- free 3D rotation using quaternion-ready `ProjectionTransform` orientation;
- stable pivot/ownership/multiplayer rules;
- optional snapping and later position/pivot manipulation;
- source/chassis capability hooks rather than source-specific mouse math.

Detailed requirements live in `WAITLIST-1.2.0.md`.

## Optional/addon direction

Create Blueprint/schematic projection remains an optional bridge/addon direction rather than a required core feature. The 1.0 source registry/render-provider seams are intended to allow additions such as `BLUEPRINT` without rewriting the four built-in source families.

## Stable-branch rule

Do not pull 1.1/1.2 features back into the 1.0.0 release snapshot. Preserve the stable release as a recoverable baseline for future bugfixes and migration work.
