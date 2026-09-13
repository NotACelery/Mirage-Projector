# WAITLIST — 1.0.0 Core Mirage Projector

Goal: ship a stable first public release while deliberately leaving extension seams for 1.1.0 and 1.2.0. Do **not** pull the large portable/Codex feature set into 1.0.0.

## P0 — current static-light acceptance

QA-confirmed on the dev.76h architecture and retained through dev.80:

- [x] Windows Java 21 / NeoForge 21.1.244 builds have reached in-game QA on the current post-light line.
- [x] Open Mature curve is the intended `15,15,14,14,...,1,1`; pure-math/current-source gates pass.
- [x] Chunk-border/relog convergence is stable after the dev.76h section-boundary invalidation fix. Repeated world logins settle in roughly a second or two near an energized source with no visible FPS regression.
- [x] Old ghost light from previous physical-relay development builds is cleaned rather than recreated.
- [ ] Re-run the final 1/2/3-high wall and L-corner obstacle-detour matrix on the release candidate.
- [ ] Re-run slabs/stairs/partial-opacity behavior on the release candidate.
- [ ] Re-run all five dev.77 Core Booster light identities in-game, especially Glass diffusion vs Diamond focus and Quartz pure reach.
- [ ] Overlapping sources and several-source performance stress.
- [ ] Multiplayer tracking/sync with at least two clients.

## P0 — projector model closure

- [x] dev.79 non-Prism alternate model ↔ VoxelShape reconciliation accepted in-game; core chambers/hitboxes line up correctly.
- [x] Wide V-shaped identity accepted; Prism accepted and locked.
- [x] Compact/Display/Field comparison shapes accepted after dev.79 reconciliation.
- [x] dev.79i final model QA accepted: Compact is free of Z-fighting and Tall has matching front/rear low translucent boxes with matching hitbox.
- [x] dev.80 promotes the six accepted comparison models to canonical IDs and removes the temporary `*_alt` runtime registrations/resources.
- [ ] Replace/refine the six projector inventory/item icons; the current block-model-derived silhouettes are too thin/abstract for final inventory presentation.

## P0 — projector active-state / shutdown UX

This is part of the current fixed-projector UX closure and should land before stable 1.0.0. It is not a 1.1.0 portable feature.

- [x] Add an explicit `TURN OFF` / projector-off action to the projector GUI.
- [x] Turning a projector off must stop rendering the projection **without deleting or resetting** any configured Image/Item/Banner/Entity data, presentation settings, snapshots, banks/faces, Core state or transform state.
- [x] Applying `Use <mode> mode` from any source workspace re-enables the projector and makes that source the currently active projection.
- [x] Treat these as distinct state concepts: **workspace currently open**, **source currently selected/active**, and **projection enabled/disabled**. Merely opening a workspace must never activate it.
- [x] Every source workspace/tab must visibly indicate when its source is the one currently being projected after `Use <mode> mode` is accepted.
- [x] On the main source menu, the active Image / Item / Entity / Banner button receives a clear **white outline**. The outline represents the source actually being projected, not the workspace currently being viewed.
- [x] When projection is OFF, no source button should be presented as currently active. The last/remembered source may remain stored internally so the configuration is preserved, but the GUI must not claim that it is presently being projected.
- [x] Persist/synchronize the enabled state safely across save/reload, chunk reload and multiplayer GUI viewers.

Architecture requirement: do not encode OFF as a fake fifth `SourceMode`. Keep projection activation independent from source identity (conceptually `projectionEnabled` + active source descriptor/current source). This separation is required for 1.1.0 End Resonance, portable devices and future registered source types.

## P0/P1 — renderer and projection acceptance

- Re-run the current Create Backtank/Ghost acceptance matrix at 99/90/50/10% opacity.
- Verify clouds/water no longer create historical depth overwrite/hole regressions.
- Verify vanilla armor, held items, glint, trims, eyes and nameplates.
- Validate high Lift/large Scale frustum bounds and recovery when entering/leaving view.
- Verify Player skin freeze/cache behavior.
- Smoke-test all six chassis and Image/Item/Banner/Entity after the lighting work.

## P1 — 1.0.0 extensibility pass

Implement only the **seams required to avoid future rewrites**, not the 1.1.0 user-facing features themselves:

- projection-source registry/descriptor instead of a forever-closed source enum;
- central projector capability profile used by menus/screens/render dispatch;
- content/presentation-transform separation with forward-compatible serialization;
- source renderer/provider dispatch that an optional addon can register against;
- generic interaction hit/envelope hook sufficient for 1.2.0 to attach a grab controller later;
- dynamic-light backend boundary remains separate from static authoritative world light; a controlled moving test source may be used for validation but no shipping lantern feature is required;
- generic energy-consumer boundary must not assume every future device owns a fixed-projector Core socket;
- unknown/future registered source data should fail safely rather than crash or be destroyed during state transfer.

## P1 — release hardening

- recipe/progression/balance review for the six current chassis and Cores/Boosters;
- handbook/public text cleanup;
- image/GIF transport stress and cache refetch tests;
- save/reload + old-world migration suite;
- multiplayer smoke tests;
- performance profile of large/overdriven projections and static light fields;
- final removal/archival of stale docs that contradict current runtime.

## Explicitly out of 1.0.0

- player-held lantern gameplay;
- rechargeable Glow Dust battery loop;
- portable projector items;
- wall/table/ceiling presentation-projector family;
- Scan Codex and duplicating lectern;
- direct grab/free-rotate hologram interaction;
- Blueprint/Create schematic source.
