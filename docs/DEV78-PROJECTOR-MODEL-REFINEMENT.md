# dev.78 — Projector Model Refinement

## Purpose

`0.1.0-dev.78` is a dedicated visual/layout pass for the six alternate projector models. It intentionally does not add new gameplay systems. The goal is to make the comparison line safe to judge in-world, in inventory and in hand before choosing which designs replace the legacy models.

## Hard modeling rule

All static projector model geometry must be grid-aligned and pixel-perfect. `from`, `to`, rotation origins/pivots and other static model coordinates must use whole model-pixel values only. Fractional/sub-pixel model coordinates are not allowed. Runtime animation such as bobbing/rotation remains continuous; its static anchor point is still defined on a whole model pixel.

## Reserved floating volumes

The model is now designed around the dynamic objects instead of placing those objects after the chassis is finished. Each alternate chassis has explicit visual anchors for:

- physical/projection top,
- floating core center,
- idle-book center,
- core render scale.

The core uses a smaller comparison-model scale (`0.28`) so its full rotated/bobbing envelope stays inside the chamber. Idle books are anchored above the physical envelope rather than using the old universal `physicalTop + 4px` rule.

## Chassis decisions

### Mirage Projector (Alt)

Full redesign. The old radial/cross silhouette read too much like Prism. The new model is a compact square starter chassis with two parallel emitter rails and a central chamber.

### Mirage Display (Alt)

Keeps its presentation-platform identity, but the chamber and book/core anchors are separated. Display remains physically taller than Field.

### Wide Mirage Projector (Alt)

Full redesign. The old two-wall arrangement was removed. The new silhouette is a low horizontal emitter rail with short end caps and a central chamber.

### Tall Mirage Projector (Alt)

Heavy refinement. The enclosing roof/canopy was removed from the projection axis. Two rear vertical pylons retain the tall silhouette while leaving the core, idle book and low-lift projection path open.

### Mirage Field Projector (Alt)

Retains the broad low platform identity. The chamber is lower than Display and its book/core anchors are now independent from the legacy chassis constants.

### Mirage Prism (Alt)

The approved silhouette is preserved. Only chamber height and renderer anchors are adjusted to stop the core/book from intersecting the model.

## Empty-core semantics

The glass visible in a projector is a chamber/container, not a built-in Glass core. New Compact/Mirage Projectors now start with an empty core slot like every other chassis. Loading data without `CoreItem` also yields an empty slot instead of silently inserting Glass.

## QA targets

Validate each original/Alt pair side by side:

1. inventory icon reads as the placed model,
2. in-hand view remains recognizable and centered,
3. floating core never touches the base/chamber through its whole bob/rotation,
4. idle book never intersects chamber/frame through its whole bob/rotation,
5. zero visible z-fighting,
6. low-lift projections begin above the physical model envelope,
7. Tall remains open above the projection axis,
8. Prism retains the silhouette already approved in QA,
9. newly placed projectors have an empty core slot.
