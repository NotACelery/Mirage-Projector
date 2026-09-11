# Optional Create bridge — Mirage Schematics / Projected Plans / Projected Schematics

Status: future separate addon concept. The final public name is not frozen.

## Dependency boundary

The bridge depends on **Mirage Projector + Create**. Mirage Projector itself must not gain a hard Create dependency.

The bridge registers a new namespaced projection source/tab: conceptually **Blueprint**. This is why the 1.0.0 projection-source/workspace architecture must be extensible.

## Core experience

A Create schematic becomes a renderable holographic plan rather than only a construction-placement preview.

- large 3D schematic projection;
- arbitrary display Scale;
- Lift/position through normal Mirage presentation controls;
- free 3D rotation through the 1.2.0 manipulation system;
- no artificial zoom required for the normal tabletop/room-scale 3D view because the player can physically approach the hologram.

Display transform and construction/grid transform must remain separate. A freely tilted 37° presentation should never imply that Schematicannon can build a structure at that transform.

## Chassis capability matrix

### Handheld projector

**Blueprint unavailable.** The handheld/lightweight projector deliberately does not support this source.

### Horizontal/table projector — preferred Blueprint host

- default/primary way to inspect full 3D schematics;
- miniature factory/structure over a table or floor;
- scale from model-sized to approximately real-world size as performance allows;
- grab/RMB rotation from 1.2.0 is central to inspection.

### Wall / data-show projector

Blueprint can act as a special presentation slide:

- project one selected/orthographic face in 2D-like form;
- rotate the blueprint/view while presenting;
- **zoom is useful here** because the viewer is constrained by the wall projection frame;
- hide/remove layers/slices to explain internal construction;
- mix Blueprint slides between ordinary image slides in one ordered presentation.

## Layer/slice controls

Future Blueprint UI may expose:

- Y-layer min/max;
- hide above/below selected layer;
- incremental layer peel;
- optional category/material visibility later.

The first implementation should prioritize reliable geometric layers over a huge analysis UI.

## Rendering/performance

Do not render every schematic block from scratch every frame.

- partition/cook schematic geometry into section/batch meshes;
- rebuild meshes when schematic/resource-relevant state changes;
- Scale/rotation/Lift normally transform cached geometry;
- frustum/culling and size limits must be defined for huge schematics;
- addon failure must not corrupt the base projector or unknown-source payload.

## Optional future Create actions

Only after viewing is stable:

- `Align for Construction`: snap display orientation/scale back to a valid Create grid transform;
- material/layer inspection;
- hand-off of a valid aligned transform to Create placement tooling where API support makes this safe.

These are not prerequisites for the first bridge release.
