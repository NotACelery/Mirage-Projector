# Mirage Projector — Roadmap index / backlog authority

This file is now an index. Detailed active work is split by scope so current-release blockers are not mixed with future expansion ideas.

## Read first

- `VERSION-SCOPE.md` — release boundaries and product arc.
- `WAITLIST-GENERAL.md` — cross-version architecture, compatibility and maintenance.
- `WAITLIST-1.0.0.md` — what must be finished/future-proofed before the first stable release.
- `WAITLIST-1.1.0.md` — portable illumination, Glow Dust battery ecosystem, Scan Codex, copy lectern, portable/presentation projectors and the hidden Dragon Egg / End Resonance Easter egg specification.
- `WAITLIST-1.2.0.md` — direct hologram grab/free-rotation interaction.
- `CREATE-BRIDGE-ROADMAP.md` — optional Create schematic/Blueprint addon.
- `FUTURE-PROOFING-1.0.0.md` — concrete extension seams to establish now.

## Current implementation line

Current development line is **0.1.0-dev.82**, protocol **27**. The dev.76h static-light architecture and the dev.60–80 renderer/projection stabilization line are QA-accepted. dev.80f closes projector models/textures/item presentation. dev.82 is the architectural freeze pass: stable namespaced projection-source IDs, content/render provider registries, chassis compatibility queries, unknown-source payload preservation, quaternion-ready transform persistence and a non-Core-specific energy boundary.

The remaining 1.0.0 work is release hardening/document cleanup rather than another renderer/light feature wave.

## Scope rule

A future idea is not automatically a 1.0.0 blocker. If 1.0.0 exposes the correct generic contract, the feature itself belongs to its planned expansion.
