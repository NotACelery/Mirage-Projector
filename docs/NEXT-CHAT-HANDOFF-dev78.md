# NEXT CHAT HANDOFF — 0.1.0-dev.78

## Baseline

- Source baseline supplied by user: `0.1.0-dev.77`.
- `dev.76h` static Mirage Light loading remains the accepted light-engine baseline.
- `dev.77` Core Booster identity/polish is retained.

## dev.78 scope

Dedicated projector model/layout QA. No new gameplay features.

### Complete redesign
- Mirage Projector Alt
- Wide Mirage Projector Alt

### Heavy refinement
- Tall Mirage Projector Alt

### Layout/clearance refinement
- Mirage Display Alt
- Mirage Field Projector Alt
- Mirage Prism Alt (preserve approved silhouette)

## Hard rule

Projector model geometry must be pixel-perfect: no decimal model coordinates, pivots or element bounds. Static renderer anchors are whole model pixels. Animated bob/rotation may interpolate normally around those anchors.

## Renderer/layout changes

New `ProjectorVisualLayout` provides alternate-model-specific whole-pixel anchors for:
- projection top,
- core center,
- idle-book center,
- core render scale.

This prevents the previous universal chassis anchors from placing books/cores inside the new geometry. Alternate models also receive selection/collision shapes aligned to their new forms.

## Core semantic fix

Compact projectors no longer receive a free/default Glass core. The visible glass is only the chamber. New placements and old data without a `CoreItem` remain empty.

## QA before promotion

Compare original and Alt blocks side by side in world, inventory and hand. Check full core/book animation envelopes and low-lift projections. Do not replace/remove originals until the user approves the new comparison models.
