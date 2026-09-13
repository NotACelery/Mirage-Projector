# Mirage Projector — Version scope authority

This scope authority was introduced as a documentation-only consolidation on dev.75d and remains authoritative on the current **0.1.0-dev.80** line. It defines product boundaries; it does not itself imply a runtime/protocol change.

This document freezes the intended boundaries between the first public release, the portable expansion, the interaction expansion and the optional Create bridge. The purpose is to prevent 1.0.0 implementation choices from hard-coding assumptions that 1.1.0/1.2.0 would immediately need to dismantle.

## Product arc

### 1.0.0 — Core Mirage Projector

The stable foundation: six fixed projector chassis, Image/Item/Banner/Entity projection, Core/PU system, Core Boosters, Crying Obsidian ecosystem, authoritative static Mirage Light Engine, snapshot/entity fidelity, multiplayer asset transport and the existing presentation controls.

The remaining 1.0.0 work is mostly acceptance, renderer hardening, release QA and **extension seams**. 1.0.0 must not ship portable lantern/projector gameplay or Scan Codex merely to prove the seams work. Projector UX closure also includes a non-destructive ON/OFF control and clear active-source feedback: workspace navigation, active projection source and device enabled state remain distinct.

### 1.1.0 — Portable Illumination, Capture & Projection Expansion

The first major expansion. It introduces moving/directional light, rechargeable Glow Dust energy, lanterns, portable projectors, presentation/table/wall projectors, the Mirage Scan Codex and the scan-copy/duplicating lectern flow.

### 1.2.0 — Interactive Holograms

The tactile interaction expansion. Projected content can be grabbed and manipulated directly, beginning with hold-RMB free rotation around a stable pivot using quaternion-ready transforms rather than accumulating fragile Euler-only state.

### Optional addon after the core contracts are stable

Working names:

- **Create: Mirage Schematics**
- **Create: Projected Plans**
- **Create: Projected Schematics**

The addon adds Create schematic/blueprint content as a separately registered projection source/tab without making the base mod depend on Create.

## Compatibility rule

Future features should normally add a source type, chassis capability, renderer/interaction provider, energy consumer or light profile. They should not require editing every existing projector with a new chain of hard-coded `if (mode == ...)` checks.
