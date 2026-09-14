# Mirage Projector 1.0.6 — Prism Carousel Compaction

1.0.6 refines the fixed-projector placement work without changing protocol 28 or `ProjectionSettings` format 3.

## Prism Distance

`Prism Distance` remains exclusive to Mirage Prism Image/GIF and Banner projection. The visible value is extra radial expansion above the collision-safe baseline and is now bounded to **+160 px (10 blocks)**.

At `+0 px`, spacing is calculated per adjacent face pair rather than from the largest face globally. Equal square faces therefore meet at their lower corners/edges instead of leaving an oversized empty gap between them.

## Tilt interaction

Positive/outward Tilt keeps the face bottom edges at the tight baseline. Negative/inward Tilt moves the upper parts of the faces toward the carousel center, so only that direction increases the collision-safe minimum.

The UI automatically raises Prism Distance when an inward Tilt needs more room. If a requested inward angle would require more than the ten-block extra-distance budget, that angle is not accepted.

Prism Rotation remains a carousel around the central machine. Individual faces do not rotate around their own displaced centers.

## Fixed settings UI

The main screen now uses four fixed tabs:

- Geometry
- Placement
- Rotation
- Floating

Lighting, Ghost/opacity and Tint are merged into Geometry. Source Workspaces, Power / Capacity, Core slot, inventory and Apply / Cancel remain anchored regardless of the selected settings tab.

## Compatibility

- Network protocol: 28
- `ProjectionSettings` format: 3
- 1.0.5 Dynamic Mirage Light foundation retained
- legacy horizontal/vertical placement slots remain compatibility-only
