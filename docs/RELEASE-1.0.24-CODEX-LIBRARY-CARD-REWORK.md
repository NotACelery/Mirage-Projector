# Mirage Projector 1.0.24 — Scan Codex Library / Card Rework + Portable UI Lite

Minecraft: **1.21.1**
NeoForge: **21.1.244+**
Java: **21**
Network protocol: **36**
ProjectionSettings format: **3**

## Scan Codex library

1.0.24 turns the Scan Codex into the canonical entity-capture library instead of treating Entity Scan Cards as scanners. The held Codex and the Codex mounted in a vanilla Lectern share one scrollable browser with a global search field plus category tabs: All, Favorites, Hostile, Passive, Farm, Nether, End, Water, Players and Other. Search is an additional filter over the selected category.

Selecting an entry opens a detail view with the frozen entity preview, its captured equipment and its player/custom Name Tag when one exists. Returning from detail preserves the active category, search and scroll position. Individual entries remain favoritable/deletable. The storage cap is **25 captures per entity type**, not 25 captures for the entire Codex.

## Entity Scan Cards are containers

`mirage_projector:entity_scan_card` no longer scans entities directly. Shift + RMB capture belongs to the Scan Codex. A physical Entity Scan Card is now only a one-snapshot transport/duplication container.

A filled Entity Scan Card by itself in either the player 2×2 crafting grid or a Crafting Table produces one blank Entity Scan Card, intentionally discarding the stored snapshot.

## Lectern duplication

Mounting the Codex on a vanilla Lectern unlocks the side-page duplication extension while viewing one exact Codex entry. The extension accepts only Mirage Entity Scan Cards. `Duplicate` is enabled only for a blank card and writes the selected frozen root into that same physical card. A filled card or missing card keeps the action disabled. The old Paper cost is removed because the blank physical card itself is now the duplication input.

## Permanent import extension

The Lectern library view always exposes the import toggle, even when Easy Mob Farm is not installed. It accepts a **filled Mirage Entity Scan Card**, copies that exact frozen snapshot into the Codex and consumes one source card **only after a successful import**. Validation failure, type-limit rejection or malformed data never consumes the input.

When `easy_mob_farm` is present, the same extension additionally accepts its `mob_capture_card`. Mirage reads the optional capture component without a hard binary dependency, reconstructs the represented living entity server-side, freezes it through Mirage's own `EntityScanData`, adds it to the Codex and consumes the Easy Mob Farm card only when that import succeeds.

Reverse export from a Mirage Codex into Easy Mob Farm blank capture cards is **not implemented in 1.0.24**. If that compatibility is added later, its balance cost is specified in **experience levels**, not raw XP points, and should scale with capture rarity/complexity/equipment.

## Hand Projector portable UI lite

The portable Hand Projector screen is compacted so the controls no longer collide with the player inventory. Banner controls are persistent widgets whose visibility/message follows the synchronized ItemStack instead of being created only from the state that existed when the screen opened. Copying a Banner projector while the screen is open can therefore expose the Banner controls without reopening the GUI.

Banner presentation exposes the existing two modes correctly:

- **Forward Projection**
- **War Banner**

War Banner additionally exposes:

- **Directional** facing;
- **Always Face Viewer** billboard facing;
- bounded Size adjustment;
- bounded Height adjustment.

The screen updates ON/OFF, presentation, facing, size and height values from the live portable-device state.

## QA status

Static source/regression verification is part of this snapshot. A real NeoForge/Gradle compile is not claimed by the assistant environment; Windows `build.bat` with Java 21 remains the authoritative compile gate before marking 1.0.24 build-clean.
