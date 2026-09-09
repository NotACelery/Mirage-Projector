# dev.40 QA — Power UI + projection-entity stability

## Power / Capacity UI

- Core slot is fully visible and no Power text crosses it.
- Core output, chassis multiplier, Core amplification and load remain readable with Glass/Quartz/Amethyst/Diamond/Netherite.
- Capacity bar is visible directly below the load line.
- Bar fill changes when Scale/Lift/Float/features change.
- Bar reaches full/red only for a state whose calculated load exceeds capacity; ordinary valid states use the normal fill.
- Hovering blank space anywhere inside POWER / CAPACITY produces **no large tooltip**.
- Detailed PU breakdown appears only while hovering the small `?` hotspot in the Power header.
- Hovering the Core slot still shows the Core/item tooltip (or accepted-core list when empty) without the Power breakdown covering it.

## Piglin / Hoglin stability

- Piglin Entity Workspace preview is stable in Overworld while mouse is stationary.
- Piglin world projection is stable in Overworld.
- Hoglin preview/world projection is stable in Overworld.
- Nether rendering looks the same as Overworld rendering.
- No entity tick/AI/world spawn is introduced.

## Regression carry-over

- GIF/static Image dev.39 pipeline still works.
- Wide/Tall SINGLE and MULTI unchanged.
- Field remains one continuous Plane.
- Prism remains N/E/S/W with adaptive per-face aspect.
- Entity equipment lifetime cleanup from dev.38 remains intact.
- Projector facing and idle handbook book remain intact on all six chassis.
- Re-run Entity/projector/water depth-order case still pending from dev.36-dev.37 if not already cleared.
