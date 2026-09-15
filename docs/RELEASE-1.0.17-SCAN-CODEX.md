# Mirage Projector 1.0.17 — Scan Codex Foundation

1.0.17 introduces the persistent Mirage Scan Codex library without changing the projector-facing Entity Scan Card format.

## Player flow

- Hold a Mirage Scan Codex and Shift + right-click a living entity or Player to create a new frozen capture.
- Right-click the Codex in air to open its library browser.
- Search by frozen display/nameplate/entity-type text.
- Cycle filters across All, Favorites, Players, Humanoids, Horses and Other.
- Favorite captures independently.
- Select one exact capture; that scan UUID remains stored on the physical Codex item for future copy-station use.

Scanning the same species repeatedly creates independent entries. A baby Zombie in one equipment set and another baby Zombie in different equipment are separate captures rather than one species unlock.

## Storage architecture

The physical ItemStack stores only:

- `MirageScanCodexId` — stable physical library identity;
- `MirageScanCodexSelected` — optional selected scan UUID.

Full `EntityScanData` roots live in Overworld `ScanCodexSavedData` (`mirage_projector_scan_codices.dat`). This avoids placing potentially large libraries inside ItemStack CustomData, which would otherwise be resent during ordinary inventory synchronization.

The browser receives metadata summaries only: scan UUID, entity type/kind, frozen display/nameplate text, Player flag, custom-name flag, favorite state and frozen equipment count. Full entity NBT stays server-side.

## Future Duplicating Lectern seam

`ScanCodexSavedData.copyScanRoot(codexId, scanId)` returns the exact canonical frozen root for server-side consumers. 1.0.17 does not yet consume Paper or print physical Entity Scan Cards; that is intentionally left to the dedicated Duplicating Lectern wave.

## Networking

Adds:

- `OpenScanCodexPayload` — metadata-only server-to-client browser snapshot;
- `ScanCodexActionPayload` — server-authoritative selection/favorite actions.

Network protocol advances from 32 to 33. `ProjectionSettings` remains serialization format 3.

## Verification

The source-side regression suite passes all 39 gates. The 1.0.17 release audit sees 113 JSON resources, 20 blocks, 26 items and 605 translation keys in each shipped locale. API-risk review confirms the 1.21.1 `SavedData.Factory`/`computeIfAbsent`, `EditBox#setHint`, `ResourceLocation.parse` and `CompoundTag#getAllKeys` call shapes used by this wave. A real NeoForge compile/runtime smoke test remains required on the external Windows Java 21 build path.
