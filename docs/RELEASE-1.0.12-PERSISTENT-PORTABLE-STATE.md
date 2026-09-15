# Mirage Projector 1.0.12 — Persistent Portable Projector State

1.0.12 turns the handheld projector from a held-only visual into a real player-owned portable device lifecycle. It also adds the Creative Battery requested for QA/admin/minigame use.

## Persistent inventory projector

A Mirage Hand Projector that has been explicitly turned ON now stays logically active when moved from the player's hand into any normal player-inventory slot. Moving the item between hotbar/inventory positions does not reset the projector or create a new logical source.

Each handheld projector owns a stable UUID stored on the ItemStack. The UUID identifies the device independently from inventory slot position.

Battery drain continues once per second while the projector is ON and has valid portable source content, even when the item is not selected or held. Turning the device OFF remains explicit: storing it no longer acts as an implicit power switch.

## Multiplayer synchronization

1.0.11 could reconstruct remote handheld projections from vanilla tracked held ItemStacks, but that cannot work once a projector is hidden inside another player's inventory. 1.0.12 therefore adds Mirage-owned portable-device state synchronization.

The server publishes active portable-projector state by:

- owner player UUID;
- stable handheld-projector UUID;
- ON/OFF state;
- synchronized portable custom-data snapshot.

Vanilla entity tracking still owns player position and orientation. Mirage does **not** send player coordinates every tick.

Clients cache active device state and render the portable hologram in front of the corresponding tracked player through the existing `MirageProjectorRenderer`. Entity holograms continue through the existing deferred entity-projection pass.

State cache entries expire if the server stops publishing them, covering dropped/deleted projectors and player lifecycle changes without leaving permanent ghost projections.

## Creative Battery

Registry ID: `mirage_projector:creative_battery`.

The Creative Battery implements the normal `RechargeableEnergyItem` contract but always reports 100% charge and ignores discharge. Any Mirage consumer that already accepts generic rechargeable media can therefore use it without special device-side code.

Product rules:

- no survival recipe;
- no loot/progression path;
- Creative/debug/admin utility;
- suitable for temporary minigame/PvP loadouts;
- may be granted/removed by server commands or future mode controllers;
- does not define normal Survival balance.

## War Banner relationship

The final War Banner renderer/presentation controls remain a 1.1.0 waitlist feature. 1.0.12 deliberately implements the hard prerequisite first: an active handheld projector can now remain synchronized and visible while stored in inventory.

The waitlist now freezes the intended War Banner contract: smaller pole-less overhead hologram, Directional and Always-Face-Viewer/Billboard options, portable-specific size/height bounds and multiplayer team-identification intent.

## Versioning

1.0.12 adds the first new custom play payload since protocol 28, so the network protocol advances to **29**.

`ProjectionSettings` remains format **3**; fixed-projector save/wire settings are unchanged.
