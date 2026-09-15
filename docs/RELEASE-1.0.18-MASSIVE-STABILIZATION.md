# Mirage Projector 1.0.18 — Massive Stabilization

1.0.18 is a stabilization/rearchitecture snapshot driven by the first broad in-game QA pass over the 1.0.9–1.0.17 portable-light, Shoulder Equipment, Charging Station and Scan Codex foundations. It intentionally adds no new feature family. The goal is to make the existing 1.1.0 foundations behave like coherent player-facing systems before Duplicating Lectern or later chassis work resumes.

## Portable device configuration

Mirage Lantern and Mirage Hand Projector now use a real shared container screen with one rechargeable-cell slot. Battery insertion/extraction is GUI-only; the old opposite-hand service gestures are removed. Lantern normal right-click opens configuration and sneak + right-click cycles `Off -> Focus -> Flood -> Ambient -> Off`. Hand Projector normal right-click remains the explicit projection ON/OFF action, while sneak + right-click opens its configuration screen. The Hand Projector screen also owns target-profile copy and the War Banner presentation/facing/size/height controls.

Mirage Light Projector is also a real `MenuProvider`. Normal block interaction opens its battery/mode screen and no longer inserts/extracts cells or cycles mode directly. The old block-entity renderer that displayed the installed battery below the physical projector is no longer registered.

Lantern and Hand Projector override re-equip animation comparison so the once-per-second battery-state mutation does not look like the held item is being replaced every time charge changes. The old continuous local actionbar refresh path is removed; action feedback is emitted only by explicit actions or relevant depletion transitions.

## Dynamic Mirage Light correction

The narrow directional cone no longer accepts/rejects a voxel only by its center point. The solver now approximates intersection between the cone and the voxel volume, allowing a narrow Focus beam to leave the origin consistently at arbitrary yaw/pitch instead of disappearing for most aim angles. Handheld/shoulder sources are seeded slightly in front of the player and placed Light Projectors seed just outside their own chassis to avoid self-occlusion on the first propagation edge.

This is a targeted fix for the QA symptom where level-15 light appeared only at very specific look angles. Runtime visual verification remains required.

## Shoulder Strap ownership and inventory UX

The user-facing Arm Strap name becomes **Shoulder Strap** while retaining the historical registry ID `mirage_projector:arm_strap` for save compatibility.

The player attachment now owns only one real Mirage equipment socket: the currently equipped Shoulder Strap. The Shoulder Strap ItemStack itself stores its Shoulder Device, Battery Pouch and upgrade sockets through the vanilla `DataComponents.CONTAINER` component. A packed strap can therefore be removed, carried, stored and exchanged without losing its batteries/upgrades. Multiple prepared straps may coexist in normal inventory.

Legacy 1.0.13–1.0.17 attachment saves are migrated on load: the old device, nine potential batteries and three potential upgrades are folded into the Strap ItemStack, then the player attachment is normalized to its one-slot 1.0.18 format.

The inventory extension now opens to the **right** of the vanilla inventory. Its toggle is anchored below the crafting-result area. An empty extension exposes only the Shoulder Strap socket. Inserting a strap reveals its Shoulder Device, six base power-cell positions and two base upgrades. Expansion reveals the final three battery positions plus the third upgrade; inactive positions are not drawn as locked/X slots. Replacing a mounted device follows normal cursor swap semantics instead of dropping the old device into the world.

Holding Shift over a packed Shoulder Strap in normal inventory exposes a compact contents preview so prepared travel straps can be distinguished before equipping them.

## Rechargeable-media cleanup

Full custom Mirage Glow Dust now normalizes back into vanilla `minecraft:glowstone_dust` when a Beacon charger completes it. Partial/depleted custom Glow Dust therefore cannot accidentally participate in vanilla Glowstone, Redstone Lamp or brewing ingredient semantics, while a fully restored unit returns to the vanilla item and regains those semantics.

Vanilla Glowstone Dust is treated as a fully charged rechargeable medium only when inserted into a Mirage device, where it is normalized to the device-owned custom charge representation. Both Charging Station and Core Booster normalize completed custom Glow Dust back to vanilla output.

Glow Dust tooltips show `Discharged` at zero, percentage while partial and no redundant `Fully charged` line at full. Light Battery tooltips keep only charge/discharged state; debug/explanatory capacity and charging prose is no longer emitted by the item.

A crafting hook is prepared for the final Light Battery recipe: when a Light Battery result consumes exactly five Glow Dust media, its resulting charge is the average charge fraction of those five inputs. The remaining recipe ingredients are deliberately not invented by this stabilization snapshot.

## Shoulder upgrades

`Auto Battery Swap Patch` no longer adds explanatory prose that merely repeats its name. The historical `battery_pouch_expansion_patch` registry item is now displayed as **Shoulder Strap Slot Expansion** with one concise tooltip: it adds three inventory slots to the Shoulder Strap. Registry IDs are unchanged for save compatibility.

## Charging Station polish

The Charging Station now accepts only incomplete Mirage Glow Dust / vanilla Glowstone Dust-normalized media / Light Batteries as charge inputs. Full cells and Creative Battery are rejected from the charging queue. Its screen is enlarged to separate Input Queue, Charging and Output labels and to leave more breathing room around the player inventory and third-party inventory utility buttons.

A block-entity renderer now mirrors the machine inventory physically: the four queue positions, the single active charging item and the four output positions are represented in the world and rotate with the station's horizontal facing.

## Scan Codex UX

The Scan Codex browser is now inventory-like rather than pause-screen-like. `isPauseScreen()` returns false and the screen no longer invokes vanilla's blurred/dim background pass; the world continues running behind the Codex panel.

## Presentation polish

Shoulder Strap receives a new 16x16 item model/texture pass. Light Battery frame/charge layers and Creative Battery positioning were recentered for inventory presentation.

## Integrity / deprecated-code cleanup

The stabilization pass also closes implementation debt exposed by the QA migration. Active Java terminology is now `ShoulderStrapItem`, `SHOULDER_STRAP` and `SHOULDER_STRAP_SLOT_EXPANSION`; the old registry IDs `arm_strap` and `battery_pouch_expansion_patch` remain deliberately unchanged because they are persistent world/save identities, not stale implementation names.

The superseded `ShoulderDeviceScreen` / `ShoulderDeviceControlPayload` pair was removed in favor of `PortableDeviceScreen` / `PortableDeviceActionPayload`. The unregistered Light Projector renderer that previously drew a physical battery below the chassis, the unused `LightProfileMath` helper and the no-longer-called periodic Hand Projector HUD updater were also deleted.

Active source no longer uses the deprecated explicit `EventBusSubscriber.Bus` selector. Client MOD-bus listeners are registered from a `Dist.CLIENT` mod entrypoint. Obsolete localization from opposite-hand battery servicing, verbose debug battery prose and the deleted shoulder-device GUI was pruned from all locales.

A dedicated integrity verifier now checks current public/internal naming versus intentionally stable registry IDs, all concrete payload registrations, all declared mixin classes, removed legacy files, language parity, Portable Device screen separation and source-snapshot hygiene.

## Compatibility/versioning

- Mod version: **1.0.18**
- Minecraft: **1.21.1**
- NeoForge: **21.1.244+**
- Java: **21**
- Network protocol: **34**
- `ProjectionSettings` serialization: **3**

Protocol advances 33 -> 34 because portable-device configuration introduces new play payloads and menu/action surfaces. No UV mechanics from 1.2.0 are implemented early.

## Validation status

The source-side 1.0.18 stabilization and integrity gates cover the QA contracts above, language parity, pixel-art centering sanity, deprecated-code removal, network/mixin registration integrity and source hygiene. The full current-line suite contains **42 gates**; the 1.0.18 release audit sees **113 JSON resources, 20 blocks, 26 items and 577 translation keys per locale**. The historical regression chain remains required. This environment does not have a working Gradle/NeoForge dependency classpath, so the authoritative compile/runtime gate remains Windows `build.bat` under Java 21 followed by in-game QA, especially dynamic light propagation, menu synchronization, Shoulder Strap migration and Charging Station world rendering.
