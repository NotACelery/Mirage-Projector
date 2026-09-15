# NEXT CHAT HANDOFF — Mirage Projector 1.0.17 Scan Codex

Date: 2026-09-14
Baseline entering wave: 1.0.16 War Banner Presentation
Current snapshot: 1.0.17 Scan Codex Foundation
Minecraft: 1.21.1
NeoForge: 21.1.244+
Java: 21
Network protocol: 33
ProjectionSettings format: 3 (unchanged)

## Implemented contract

`mirage_projector:scan_codex` is a physical non-stackable item. Shift + right-click a living entity/player stores a new canonical `EntityScanData` snapshot; right-click in air opens the Codex browser. Multiple captures of the same entity type are intentionally independent because scan UUID, not species, is the library identity.

## Storage

The item stores only `MirageScanCodexId` and optional `MirageScanCodexSelected` UUIDs in CustomData. Full scan roots live in Overworld `ScanCodexSavedData`, persisted as `mirage_projector_scan_codices.dat`. This intentionally avoids carrying potentially huge snapshot libraries inside routinely synchronized ItemStacks.

`ScanCodexSavedData` preserves insertion order, favorite state and exact root NBT. It exposes `copyScanRoot(codexId, scanId)` as the server-side foundation for the future Duplicating Lectern.

## Browser

The server sends metadata only. The client browser supports:

- text search across display name/nameplate/entity type;
- All / Favorites / Players / Humanoids / Horses / Other filters;
- paging;
- favorite/unfavorite;
- exact capture selection;
- preview metadata including type/category/nameplate/equipment count/short scan ID.

Selection and favorite mutations are server-authoritative. The selected scan UUID is persisted back on the physical Codex ItemStack.

## Network

New payloads:

- `OpenScanCodexPayload` (S2C metadata snapshot);
- `ScanCodexActionPayload` (C2S SELECT / TOGGLE_FAVORITE).

Protocol 32 -> 33. No ProjectionSettings format change.

## Scope intentionally not implemented

- Duplicating Lectern / Paper consumption;
- printing physical Entity Scan Cards from Codex entries;
- final Survival recipe / dedicated Codex art;
- delete/rename/reorder workflows;
- UV ecosystem (still entirely 1.2.0).

## Recommended next wave

1.0.18 should implement the Duplicating Lectern/copy station on top of `(Codex UUID, selected scan UUID)`, consuming Paper and producing the existing physical Entity Scan Card without modifying projector Entity workspace compatibility.

## High-value QA

- scan two different Zombies and confirm both independent rows remain;
- scan two Players / named mobs and verify frozen names and filters;
- favorite entries, close/reopen and relog/server restart to confirm SavedData persistence;
- select different entries and confirm selection persists on the Codex ItemStack;
- duplicate the physical Codex in Creative and confirm the duplicated UUID intentionally references the same server library;
- verify passenger/vehicle composite rejection remains identical to scan cards;
- build with Java 21 / NeoForge 21.1.244+.

## Verification status

- `python tools/verify_current_line.py` -> `MIRAGE PROJECTOR 1.0.17 VERIFICATION PASS (39 gates)`.
- Release audit -> 113 JSON, 20 blocks, 26 items, 605 language keys; 12 historical/non-blocking `EventBusSubscriber.Bus` deprecation sites.
- Documentation authority/current-version drift was cleaned before packaging (`README`, `CURRENT-IMPLEMENTATION`, `DEVELOPMENT`, `DOCUMENTATION-AUTHORITY`, `VERSION-SCOPE`).
- No Gradle wrapper/system Gradle is available in the current environment; Windows `build.bat` under Java 21 remains the authoritative compile check.
