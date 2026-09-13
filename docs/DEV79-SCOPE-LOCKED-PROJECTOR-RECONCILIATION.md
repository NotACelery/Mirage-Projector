# dev.79 — Scope-Locked Projector Model / Shape Reconciliation

## Purpose

`0.1.0-dev.79` is a corrective visual pass following dev.78/dev.78a QA.

The explicit goal is to stop cross-contaminating projector redesign work. Every chassis is handled under an isolated scope, and model geometry is reconciled with its own `VoxelShape` instead of borrowing approximate shapes from another projector.

No projection mechanics, power mechanics, light-engine behavior, networking, GUI behavior or new decorative gimmicks are intentionally introduced in this snapshot.

## Global rules

- All alternate projector model coordinates remain whole model pixels only.
- No fractional static geometry or fractional rotation origins.
- Visual model, visible core chamber and block outline/collision shape are treated as separate concerns and must agree per chassis.
- No projector may be redesigned merely because another projector is being corrected.
- `Mirage Prism Alt` is frozen unless a Prism-specific QA problem is reported.

## Chassis scopes

### Mirage Prism Alt — LOCKED

Status: accepted in dev.78 QA.

- Visual JSON is hash-locked by `verify_dev79_scope_locked_projectors.py`.
- Existing accepted `VoxelShape` remains unchanged.
- No redesign is permitted in dev.79.

### Wide Mirage Projector Alt — VISUAL LOCK / SHAPE RECONCILIATION ONLY

Status: dev.78a V-shaped identity accepted.

- The elongated stepped-V visual model is unchanged from dev.78a.
- Only the `VoxelShape` was rewritten to match the actual V model geometry exactly.
- The central magenta chamber remains `[5,3,5] -> [11,7,11]`.
- The visual JSON is hash-locked to prevent accidental redesign in this pass.

### Mirage Projector Alt — VISUAL LOCK / SHAPE RECONCILIATION ONLY

Status: compact/basic identity accepted.

- Visual model is unchanged from dev.78a.
- Its `VoxelShape` now mirrors every visible model element exactly, including the chamber.
- Visual JSON is hash-locked for dev.79.

### Mirage Field Projector Alt — VISUAL LOCK / SHAPE RECONCILIATION ONLY

Status: overall design accepted; chamber outline mismatch reported.

- Visual model is unchanged from dev.78a.
- Its `VoxelShape` now mirrors its own model elements exactly.
- Chamber remains `[5,4,5] -> [11,8,11]`.
- Visual JSON is hash-locked for dev.79.

### Mirage Display Alt — RESTORE + CHAMBER CORRECTION

Status: dev.78a accidentally introduced a tall rear-frame/portal-like design not requested by QA.

- Restored the dev.78 display identity: broad base, four low corner posts and perimeter emitters.
- Removed the unplanned rear portal/frame structure entirely.
- Reduced the chamber from the oversized dev.78 volume to `[5,4,5] -> [11,8,11]`.
- `DISPLAY_ALT_SHAPE` is generated/reconciled against exactly those visible elements.

### Tall Mirage Projector Alt — ISOLATED MAJOR REDESIGN

Status: only chassis authorized for a major visual redesign in dev.79.

- Removed the off-center/antenna-like elements introduced in dev.78a.
- Replaced solid obscuring walls with a real framed core dome:
  - obsidian lower rim,
  - four narrow obsidian corner borders,
  - obsidian upper rim,
  - purple stained-glass front/back/side faces,
  - purple stained-glass top face.
- Core remains visible through the dome rather than being buried behind solid obsidian.
- New geometry has no positive-volume overlap between frame and glass faces, avoiding new z-fighting in the dome.
- Idle book remains above the dome and core anchor remains inside the visible chamber.
- `TALL_ALT_SHAPE` matches the Tall visual model exactly.

## Model/shape contract

`tools/verify_dev79_scope_locked_projectors.py` enforces:

1. integer-only projector geometry,
2. hash locks for Prism, Wide, Compact and Field visual models,
3. restored Display frame identity,
4. exact Display chamber bounds,
5. Tall dome glass-face count and no antenna-height geometry,
6. exact model-element ↔ `VoxelShape` box equality for Compact, Display, Wide, Tall and Field,
7. current renderer anchor contract,
8. `0.1.0-dev.79` version line.

Prism remains intentionally excluded from the exact box-equality rewrite because its already-approved dev.78 shape is frozen in this pass.

## QA checklist

For each non-Prism alternate projector:

- Outline must trace visible geometry instead of extending through empty space.
- Chamber outline must match visible purple glass bounds.
- Core must remain visible and clear of base geometry through its idle rotation/bobbing.
- Idle book must not intersect the chamber/dome through its idle animation.
- Test Netherite Ingot as a visually wide core item inside the chamber.

Specific visual checks:

- **Display:** no portal-like rear frame exists.
- **Field:** central chamber outline matches the visible chamber.
- **Compact:** central chamber outline matches the visible chamber.
- **Wide:** V identity is unchanged; shape follows both stepped arms and chamber.
- **Tall:** no antennas; nucleus is visible through purple-glass faces from normal player eye height.
- **Prism:** should look/behave exactly as before dev.79.
