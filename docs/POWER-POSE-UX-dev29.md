> **HISTORICAL.** Entity pose notes may remain relevant, but Power/limit UI described here predates the dev.38 Power rework. Do not use this file as current Power authority. See `POWER-SYSTEM-REWORK-dev38.md` and `DOCUMENTATION-AUTHORITY-dev41.md`.

# dev.29 — Power / limits UX + contextual generic sitting pose

## Entity workspace
- Cat, wolf and parrot scans expose a persistent `Idle / Sitting` pose selector.
- Sitting is projection-only and never mutates the scanned entity or card.
- `Return gear` is only shown for Humanoid and Horse workspaces, the families that currently expose physical equipment staging channels.
- Fixed the Humanoid channel list rendering Main Hand twice.

## Power and limits
The old compact `Used / Available` line mixed budget, remaining power and geometry limits. dev.29 separates them explicitly:
- Projection cost (PU currently required)
- Core capacity (installed Core budget)
- Remaining power
- Core Scale/Lift/Float limits
- Chassis width/height, Lift/Float and source-count limits
- Current projected dimensions
- Clearance status, including blocked block count and required envelope size

Lift now charges once per 16 px; dev.28 accidentally added the same Lift term twice.

## QA
- Cat/Wolf/Parrot: Idle ↔ Sitting, preview and world render, persistence after reopen/reload.
- Cow/other Generic: no pose button.
- Cat Generic workspace: no Return gear button.
- Humanoid/Horse: Return gear still present.
- Compare Core and chassis exact limits in Projection Settings.
- Place blocks inside the highlighted clearance envelope and verify the red blocked message reports the count.
