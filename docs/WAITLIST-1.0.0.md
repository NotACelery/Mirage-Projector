# WAITLIST — 1.0.0 Core Mirage Projector

Goal: ship a stable first public release while deliberately leaving extension seams for 1.1.0 and 1.2.0. Do **not** pull the large portable/Codex feature set into 1.0.0.

## P0 — dev.75d acceptance

- Windows Java 21 / NeoForge 21.1.244 build.
- In-game exact open Mature curve: `15,15,14,14,...,1,1`.
- 1/2/3-high walls and L-corner obstacle-detour measurements.
- slabs/stairs/partial-opacity behavior.
- Quartz/Diamond reflected-range regression.
- chunk border/load/unload, relog, respawn and dimension-change lifecycle.
- overlapping sources and several-source performance.
- multiplayer tracking/sync.
- legacy `crying_light_node` cleanup without recreating physical relays.

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
