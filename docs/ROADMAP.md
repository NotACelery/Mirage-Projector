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

## Implemented/stabilized in dev.67–73 — Crying Obsidian optics and physical-light experiments

The optical behavior remains retained: Small/Medium/Large are zero-block-light attenuation stages, Mature is the full Beacon stop/world-light stage, Core Booster activation begins at `8.5/16`, bud continuation begins at the stage silhouette heights, and residual-ray behavior preserves the reflected material identities.

The physical `crying_light_node` experiments from dev.65–73 are no longer the forward architecture. Live numbered-floor and wall QA proved that every relay becomes an independent omnidirectional vanilla emitter after placement, which can create cross-shaped overfill, wall leakage and an unreliable final half-decay field even if candidate placement itself is source-occlusion-aware. dev.74 retains those nodes only as temporary visible-light scaffolding while the replacement runs in parallel.

## Implemented in dev.74 — Mirage Light Engine foundation

dev.74 adds a feature-independent shadow-light subsystem:

- source/profile/runtime contracts;
- fixed-point scalar energy with exact half-decay math;
- causal six-neighbour voxel propagation;
- vanilla edge-occlusion reuse;
- primitive/bucketed solver work queues;
- sparse 16³ per-source sections;
- per-Level overlapping-source max aggregation;
- debug commands for virtual-vs-vanilla inspection;
- same-tick geometry rebuild integration for Mature sources;
- future shape/RGB/dynamic-source reservation without claiming runtime support.

This is intentionally shadow-only. Visible Simple Light Level output remains the dev.73 physical backend in dev.74.

## P0 — dev.75 Cluster Virtual Light Backend

Before further Cluster tuning or mobile light work, make the dev.74 virtual field authoritative safely. Required work:

- integrate final virtual contribution into client rendering/light lookup **without** making every virtual voxel a new vanilla emission source;
- define/synchronize server gameplay-light semantics where relevant;
- remove Mature runtime dependence on physical `crying_light_node`;
- migrate/clean old relay blocks without touching unrelated operator/mod lights;
- add automatic chunk-load invalidation/synchronization;
- preserve overlap removal/downgrade behavior;
- rerun exact numbered-floor `15→1`, full-wall, finite-wall corner-wrap, Booster and performance QA;
- profile several overlapping radius-30/38 fields before optimizing partial invalidation.

Do not reintroduce source-to-target relay lattices as the primary backend.

## P1 — dev.76 dynamic/mobile light source foundation

After static virtual light is stable, separate `STATIC_WORLD` from `DYNAMIC_VISUAL` consumption so moving projectors do not rebuild server block-light state every visual frame. Initial target is a controlled moving test source, not a full portable-projector feature. This layer is intended to support later portable projectors, moving holograms and projected-map/display illumination.

## Backlog — advanced Mirage light profiles

Reserved profiles/shapes include:

- `CONCENTRATE`;
- `DIRECTIONAL_SPOT`;
- `ROTATING_DIRECTIONAL_SPOT`;
- directional cone;
- rectangular frustum;
- plane/projected-surface emission;
- RGB-preserving visual lighting.

These should reuse the dev.74 source/profile/storage architecture rather than create feature-specific physical emitters.

## Implemented in dev.71 — per-component Equipment visibility

Humanoid Head/Chest/Legs/Feet/Main Hand/Off Hand and Horse Saddle/Body Armor now expose persistent render-only visibility toggles in Entity Workspace. Hidden channels keep their virtual snapshots and snapshot UUIDs intact, do not return physical items, remain part of saved/state-transferred projector data, and reappear immediately when shown again. Clearing a snapshot resets that channel visibility to the default visible state. The renderer and 3D preview share the same visibility-aware reconstruction path.

Network protocol is 19 because `EntityWorkspaceActionPayload.Action` gains a new appended action and mixed dev.70/dev.71 peers must not silently disagree about its ordinal.

## Implemented in dev.72 — Entity envelope/frustum/nameplate hardening

dev.72 closes the generic first-pass bounds/culling work without introducing quality-reducing LOD:

- Entity bounds now reserve conservative extra width/height for visible Humanoid hand/chest/head equipment and Horse Body Armor in addition to species/pose dimensions;
- preview fitting and clearance inherit the same bounds because they share `EntityProjectionBounds`;
- projected nameplates are restored above `Lift + Float + projected height` instead of sitting near the chassis, and inherit Tint/Ghost opacity;
- the BER render AABB now unions the physical machine with the full displaced projection plus nameplate footprint;
- `shouldRenderOffScreen` is no longer forced true, allowing vanilla frustum culling to skip giant projectors whose complete envelope cannot contribute to the current frame.

## Implemented in dev.73 — explicit Mature half-decay relays

Simple Light Level QA on the accumulated dev.72 line showed that the sparse odd-distance relay lattice produced the intended doubled persistence only around levels 14–11; level 15 and the lower 10–1 tail fell too quickly. dev.73 removes that interpolation assumption. Every unobstructed axial air cell now receives the exact profile target, yielding `15,15,14,14,...,1,1` over 30 blocks without a Booster. Quartz/Diamond conceptual reinforcement extends the saturated plateau/tail while remaining capped to real light level 15. dev.70 branch occlusion and same-tick invalidation are preserved.

## P2 — renderer/entity hardening

- targeted compatibility adapters for backpacks/Curios/accessories/cosmetic armor only where the generic Ghost buffer path cannot cover a mod safely;
- renderer-specific/species-specific bounds refinements where a concrete renderer exceeds the conservative generic envelope;
- stress overlapping translucent projected entities and verify deferred-sort/depth stability;
- verify offline/frozen Player skin behavior under reconnect/cache conditions;
- profile giant overdriven projections after frustum hardening before considering any actual LOD policy.

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


## dev.70 closed occlusion/refresh items

- Terrain place/break changes around indexed active Mature sources rebuild at the end of the same server tick.
- Opaque blockers hard-stop downstream axial relay placement; stale nodes reconcile immediately.
- dev.69 long Glass face-diagonal world-light relays are removed because their omnidirectional vanilla emission leaked behind walls.
- Legacy dev.69 diagonal nodes are swept on first dev.70 source refresh/removal.
- Glass still affects residual reflected-ray geometry; a future static Diffusion topology must preserve occlusion before it can return.
- Windows build and in-game wall/unblock/overlap QA remain open before build-clean acceptance.
