# Mirage Projector — Active roadmap / waitlist

This is the only active backlog authority after dev.67. Items already implemented have been removed from the waitlist instead of being left as stale future work.

## P0 — stabilize the current renderer

### Semi-transparent Entity composition

dev.61 removed the Create Backtank `Not building!` crash. dev.62 restored the late `AFTER_LEVEL` camera transform and added projection-local depth handling. dev.63 then confirmed a second Create-specific problem: the Netherite Backtank is one equipped chest item but Create renders three visual contributions — two synthetic Humanoid diving-armor surfaces plus the separate tank/cog geometry. Pair-normalizing only the two armor alphas still left the ordinary projected body underneath them, so the chest region remained substantially more opaque than the tank and retained unstable overlapping surfaces.

Live dev.64/dev.67 QA now confirms the renderer can fade the Create Netherite Backtank fully under Ghost. The retained compatibility contract:

- keeps the separate Backtank block-model layer on the standard late Ghost path;
- recognizes the active `create:netherite_backtank` from the projected entity without linking against Create classes;
- treats the first synthetic diving-armor pass as an internal underlay and discards it only while Ghost transparency is active;
- renders the outer `netherite_diving_layer_1` surface once at the requested projection opacity using the late depth-stable Entity Ghost type;
- suppresses the matching discarded inner glint pass when present while preserving the outer glint path;
- uses the texture path when recoverable and a deterministic two-pass fallback for 1.21.1 RenderType representations where the texture is not exposed in the string;
- leaves 100% opacity entirely native, so Create still renders both diving layers normally when Ghost is disabled;
- retains dev.61 independent buffers for simultaneous Backtank base + foil consumers.

Required acceptance:

- 99%, 90%, 50% and 10% opacity all remain visible from arbitrary camera yaw/pitch;
- the Netherite Backtank tank and diving chestpiece fade at visually comparable rates;
- the chest no longer looks like several translucent copies stacked over the player body;
- the synthetic chest no longer introduces obvious z-fighting or camera-dependent depth flips;
- clouds no longer visually overwrite parts of a translucent projection;
- water does not regain the historical depth-hole bug;
- vanilla armor, held items, glint, trims and eyes remain sane;
- Create Backtank with foil/glint still does not crash;
- 100% opacity retains Create's untouched native two-layer appearance.

If z-fighting remains after dev.64, isolate whether it belongs to the separate Backtank block geometry, ordinary Humanoid body/equipment, or a different modded RenderType before making another broad renderer change.

## Implemented in dev.60 — Core Booster Beacon relay

The loaded Core Booster relay branch is no longer waitlisted. dev.60 implements the five material identities, stained-glass hue preservation, additive width scaling, four-effective-Booster cap, approximate ×2 global width cap, Amethyst rotation acceleration, Diamond inner-beam focus and Netherite rotation inversion.

The downstream useful-lighting consequences of all five relay identities are implemented by the dev.69 reflected-light contract below; Quartz/Radiance remains the dominant static range amplifier.

## Implemented/stabilized in dev.67 — powered Crying Obsidian optics and lighting

dev.67 keeps Small/Medium/Large buds as zero-block-light stages and restores their missing residual-ray behavior. Only an energized Mature Cluster becomes a world-light source and drives the Mirage-owned half-decay auxiliary field. Its baseline decay target is approximately `5,5,4,4,3,3...`, doubling useful persistence without exceeding vanilla light level 15. dev.69 replaces the old width-derived relay tier with material-specific reflection: Quartz reinforces radiance/range, Diamond focuses axial reach, Glass adds bounded diagonal diffusion, Amethyst drives resonance and Netherite inversion. Nodes remain invisible, replaceable, non-item, occlusion-aware and self-cleaning.

The optical path is also stabilized: Core Booster effects begin at the internal-core plane `8.5/16`; Small/Medium/Large continuation resumes at `4.5/16`, `6.5/16`, `8.5/16`; Mature fully terminates the vertical beam; residual bursts ignore the source crystal's own collider; and the custom beam quad topology now matches vanilla to avoid accidental diagonal N/X faces.

External dynamic-light providers remain optional ecosystem compatibility rather than a requirement for Mirage. A global custom light-engine rewrite remains explicitly rejected.

## Backlog placeholders — advanced light profiles

dev.66 reserves three non-runtime lighting profiles behind the reusable `LightProfile` contract:

- `CONCENTRATE`: faster useful falloff than vanilla. Activation requires suppressing/replacing the source block's normal omnidirectional emission; weaker additive nodes cannot subtract already-propagated light.
- `DIRECTIONAL_SPOT`: static cone lighting using a direction vector and cone angle. Runtime candidate generation must be bounded and obstruction-aware rather than scanning an entire radius cube.
- `ROTATING_DIRECTIONAL_SPOT`: dynamic directional field with a rotation period. A future implementation must update only node-set deltas at a bounded server cadence rather than clear/recreate the full field every visual frame.

These are architectural placeholders, not scheduled release promises. `VANILLA` and `EXTEND` are the only modes currently allowed to affect world lighting. See `LIGHT-PROFILE-FOUNDATION.md`.

## P1 — per-component Equipment visibility

Add render-only visibility toggles without deleting stored snapshots.

Humanoid:

- Head;
- Chest;
- Legs;
- Feet;
- Main Hand;
- Off Hand.

Horse:

- Saddle;
- Body Armor.

Turning a channel off must only suppress its renderer. Re-enabling must restore the already stored virtual snapshot immediately. It must not return/delete/recapture the equipment.

## P2 — renderer/entity hardening

- targeted compatibility adapters for backpacks/Curios/accessories/cosmetic armor only where the generic Ghost buffer path cannot cover a mod safely;
- renderer-specific/species-specific bounds and clearance refinements;
- performance/frustum/LOD review for giant overdriven projections;
- stress overlapping translucent projected entities;
- verify offline/frozen Player skin behavior under reconnect/cache conditions.

## P2 — mounted/composite entities

Current scan format rejects passengers/vehicles. A future composite design must define:

- root + passenger snapshot structure;
- relative transforms;
- equipment/pose ownership;
- preview fitting;
- clearance and renderer ordering;
- size/safety limits.

Do not simply stop rejecting jockeys until this contract exists.

## P2 — release hardening toward 0.1.0

- complete the reusable QA matrix in `QA-REGRESSION.md`;
- multiplayer image/GIF transfer stress;
- save/reload and old-world migration testing;
- recipe/balance review;
- render performance review;
- final public handbook/text cleanup;
- controlled decomposition of hotspot classes only after behavior freezes.

## 1.1.0+ — Mirage Scan Codex

Keep the physical Entity Scan Card as the projector-facing format, but allow scanning once into a persistent searchable library.

Planned flow:

```text
Living Entity / Player
        |
        v
Mirage Scan Codex entry
        |
        +-- search / filters / favorites / preview metadata
        |
        v
copy/printing station + Paper
        |
        v
physical Entity Scan Card
        |
        v
existing projector workflow
```

Desired organization includes Player vs mob/category filtering, searchable stored names/types and repeated card printing without finding the original entity again.

## Backlog concept — Unrefined Crying Crystal Core

Retain only as a design concept, not scheduled work. Possible identity: an intentionally unstable/refracted projection signature using a raw Crying Obsidian crystal. Stats, recipe, tier and whether it is useful or novelty remain undefined.

## Explicitly not current promises

- Effigy and Colossal are not current chassis and must not be reintroduced from historical docs without a fresh design decision.
- Wide/Tall Banner MULTI is not implied by Image MULTI.
- Glowstone has no assigned role merely because old brainstorming mentioned it.


## dev.68 closed maintenance items

- Core Booster accidental extraction guard: implemented.
- Immediate Mature Cluster light-field teardown on source removal/de-energization: implemented; requires in-game QA.
- Baseline Mature half-decay field remains independent of Core Boosters.

## dev.69 closed reflected-light items

- Full opaque blockers terminate downstream reflected node placement; partial blockers add attenuation rather than acting as perfect air.
- Local face shading/corner behavior remains delegated to vanilla block-light/AO instead of introducing a custom per-face light engine.
- Generic Beacon `widthScale` no longer grants static reflected range.
- Glass/Quartz/Amethyst/Diamond/Netherite now retain Diffusion/Radiance/Resonance/Focus/Inversion identities after Crying Obsidian reflection.
- Mature source refresh resolves its Beacon relay once per refresh rather than once per candidate node.
- Requires Windows compile plus in-game occlusion/mixture QA before build-clean acceptance.
