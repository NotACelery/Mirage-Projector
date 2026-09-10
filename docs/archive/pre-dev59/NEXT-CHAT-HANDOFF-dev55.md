# Mirage Projector — next-chat handoff after dev.55

Current source candidate: **0.1.0-dev.55**.

## Main changes

- Removed registration of the five legacy Improved Core BlockItems so they no longer appear in Creative/Search or normal inventory workflows.
- Retained only their block IDs as hidden migration shims for old QA worlds; placed legacy blocks auto-convert to the single Core Booster on chunk load.
- Core Booster material is now an enum BlockState property and is the renderer's authoritative source.
- Shift-right-click extraction changes the blockstate to `empty`, eliminating the stale spinning ghost-item regression.
- dev.54 BlockEntity-only material data migrates into the new blockstate automatically on load.

## QA targets

- Search/creative visibility
- legacy block auto-migration
- instant visual clearing on extraction
- save/reload of loaded and empty Boosters
