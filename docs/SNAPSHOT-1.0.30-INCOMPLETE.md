# Mirage Projector 1.0.30-SNAPSHOT — incomplete handoff

Superseded status: this handoff records the intermediate recovery point. The completed 1.0.30 source line is described by `RELEASE-1.0.30-UX-RUNTIME-WAVE.md`.

Date: 2026-09-15

This snapshot is intentionally recoverable even though the 1.0.30 wave is not finished.
It must not be treated as build-clean or release-ready until Windows `build.bat` and runtime QA succeed.

## Current intended version state
- Mod version: 1.0.30
- Network protocol: 40
- ProjectionSettings format: 4
- Baseline: 1.0.29 Runtime QA Interaction Fixes

## Implemented in the unfinished 1.0.30 wave
- Mirage Hand Projector: in-progress real source selection for Image / Item / Entity / Banner, compact layout, source slot, battery/source positioning changes, and retained War Banner controls.
- Shoulder Strap / Mirage Equipment: mouse-release handling added for lateral slots, redundant `+3` text removed, panel/toggle positioning adjusted.
- Presentation Remote: center calculation moved to real window coordinates and neutral dead-zone enlarged.
- Wall/Data-show GUI: live Scale/X/Y preview wiring, Apply/Cancel changed toward non-closing semantics, header controls rearranged, Presentation Order overlap fixes in progress.
- Table / Entity Workspace: Use Entity Mode server-side wiring and entity-workspace layout corrections in progress.
- Entity Scan Card: dedicated Mirage texture added instead of paper placeholder.
- Scan Codex: layout/render cleanup in progress, including single custom book-canvas render path, reorganized search/tabs/results, shorter import/duplicate copy, and better lateral slot padding.
- Language parity target currently 685 keys.

## Still incomplete / pending before final 1.0.30
- Finish adapting the remaining historical gates for protocol 40 and intentional 1.0.30 UI changes.
- Add/finalize dedicated 1.0.30 verification gates.
- Re-run full cumulative regression after verifier updates.
- Verify the final packaged ZIP after clean extraction.
- Run Windows NeoForge `build.bat`.
- Runtime QA of Hand Projector modes, Shoulder Strap removal, Wall live preview, Table Entity mode, Remote center, Codex hand/lectern layouts and scan-card presentation.

## Snapshot rule
This file exists specifically so unfinished work is never withheld at the end of a development wave. Continue from this snapshot if a conversation is lost or interrupted.
