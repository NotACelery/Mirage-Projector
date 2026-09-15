# Mirage Projector 1.0.10 — Handheld Mirage Lantern

1.0.10 turns the 1.0.5–1.0.9 mobile-light foundation into a real player-held device while keeping `STATIC_WORLD` untouched.

## Device

Registry ID: `mirage_projector:mirage_lantern`.

The Mirage Lantern is non-stackable and owns one removable rechargeable-cell slot. The exact Glow Dust or Light Battery ItemStack is serialized inside the lantern ItemStack, so partial charge and future per-cell components survive insertion, inventory moves, save/reload and extraction.

A lightweight duplicated summary (`cell present` + percentage) exists only so held-device rendering/HUD paths can cheaply decide whether the lantern currently emits. The nested rechargeable ItemStack remains authoritative.

## Interaction

- normal right-click while powered cycles exactly `Focus -> Flood -> Ambient -> Off -> Focus`;
- default mode is Focus;
- sneak + right-click uses the opposite hand as the service hand;
- when empty, a rechargeable cell in the opposite hand inserts one exact cell;
- when loaded, an empty opposite hand extracts the exact installed cell;
- a non-empty non-serviceable opposite hand does not destroy or silently replace the installed cell;
- depletion never resets the selected mode.

## Drain

Server-side item ticking drains only a lantern that is actually held in main/off hand. A lantern merely present elsewhere in inventory does not drain.

Current shared QA values remain:

| Mode | Drain / second |
|---|---:|
| Focus | 4 |
| Flood | 2 |
| Ambient | 1 |
| Off | 0 |

These remain tuning values, not final 1.1.0 balance.

## Moving light / multiplayer

`ClientHeldLanterns` scans tracked client players and derives at most one source per hand. Source identity is stable by player UUID + hand kind. Position and aim come from vanilla tracked player transforms; mode and active-cell summary come from the tracked held ItemStack. No dedicated movement-light payload is introduced.

Focus and Flood submit directional profiles along player look direction. Ambient submits the shared omnidirectional profile. Off, empty and depleted lanterns explicitly remove their dynamic source. All fields use `ClientDynamicMirageLightManager` and `DYNAMIC_VISUAL`; none enter server-authoritative static chunk light.

## HUD

While the local player holds a Mirage Lantern, the actionbar is refreshed with device name, selected mode and battery percentage. Empty/depleted state uses the localized `Discharged` / `Sin Cargar` wording.

## Deliberately unresolved

- survival recipe;
- final profile/range/drain balance;
- additional moving-entity light consumers;
- battery-backed hologram projection PU/Lift/light behavior;
- stationary dynamic-emitter geometry invalidation optimization.

Network protocol remains **28**. `ProjectionSettings` remains format **3**.
