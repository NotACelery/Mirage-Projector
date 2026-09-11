# Mirage Projector — Version scope authority

Documentation-only consolidation on top of **0.1.0-dev.75d**. No runtime code/version/protocol change is introduced by this file set.

This document freezes the intended boundaries between the first public release, the portable expansion, the interaction expansion and the optional Create bridge. The purpose is to prevent 1.0.0 implementation choices from hard-coding assumptions that 1.1.0/1.2.0 would immediately need to dismantle.

## Product arc

### 1.0.0 — Core Mirage Projector

The stable foundation: six fixed projector chassis, Image/Item/Banner/Entity projection, Core/PU system, Core Boosters, Crying Obsidian ecosystem, authoritative static Mirage Light Engine, snapshot/entity fidelity, multiplayer asset transport and the existing presentation controls.

The remaining 1.0.0 work is mostly acceptance, renderer hardening, release QA and **extension seams**. 1.0.0 must not ship portable lantern/projector gameplay or Scan Codex merely to prove the seams work.

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
