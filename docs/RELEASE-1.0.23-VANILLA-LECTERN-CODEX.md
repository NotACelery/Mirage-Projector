# Mirage Projector 1.0.23 — Vanilla Lectern Codex Workflow

1.0.23 replaces the temporary standalone Duplicating Lectern from 1.0.22 with the intended vanilla-Lectern workflow.

## Player flow

```text
Mirage Scan Codex
  -> place on a vanilla Minecraft Lectern
  -> right-click the occupied Lectern
  -> browse/select the Codex library in Lectern copy mode
  -> Duplicate Scan (consumes 1 Paper)
  -> physical Entity Scan Card
```

The Codex itself remains on the vanilla Lectern until the player presses **Take Codex** or breaks the Lectern. Vanilla Lectern insertion/state/drop behavior is reused rather than registering a Mirage-specific station block.

The server still copies the exact canonical frozen scan root from `ScanCodexSavedData`; duplication does not rescan or rebuild the source entity. `ScanId`, source provenance, entity/player appearance, equipment and nameplate state remain unchanged. Favorite state remains Codex-library metadata only.

## UI correction

The Scan Codex browser keeps the 1.0.18/1.0.19 no-blur behavior, but its presentation is changed from the flat dark utility panel to a two-page book-like layout. When opened from a Lectern it exposes copy controls and the current Paper count. When opened normally from the held Codex, duplication controls are absent.

## Network

Protocol advances from 34 to **35** because Lectern-context Codex snapshots/actions add explicit block-position-aware payloads. `ProjectionSettings` remains format 3.
