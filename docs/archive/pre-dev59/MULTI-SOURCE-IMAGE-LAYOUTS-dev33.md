> **SUPERSEDED by dev.38.** This document records the historical dev.33 implementation, not the current design contract. The mandatory Field 3×3/9-source layout was an incorrect interpretation and has been rolled back. Current rules: Field = one continuous 128×128 Plane; Wide/Tall = optional SINGLE or four-source 4×1 / 1×4 modes.

# Multi-source Image layouts — dev.33

## Physical layouts

- Wide: 4×1, four independent virtual Image sources.
- Tall: 1×4, four independent virtual Image sources.
- Field: 3×3, nine independent virtual Image sources.

These are Plane layouts, not Prism faces. The source bank therefore lives separately from the existing Front/Back/North/East/South/West ProjectionSettings fields.

## Scale and cell fitting

Scale controls the longest axis of the complete physical layout. At the chassis maximum this resolves to Wide 80×32 px, Tall 32×80 px and Field 80×80 px. Each source keeps its original aspect ratio and is centered inside its cell with a small visual gutter.

## Rear side

The complete layout shares one rear-side rule: Mirrored or Readable. Independent rear source banks are not emulated with the old single `BackImage` field because that would be ambiguous for 4/9 source cells.

## Persistence / migration

`ImageSourceBank` persists nine virtual asset descriptors in BlockEntity NBT and travels with the Image Workspace menu/update payload. When a pre-dev.33 Wide/Tall/Field projector has no bank but still has the legacy Front image, that image migrates to slot 1.

## Power / clearance

Power cost depends on populated source cells. The physical envelope and clearance use the complete active layout footprint, because empty cells do not make the emitter frame itself narrower or shorter.

## UI

The Image Workspace shows a physical layout map. Clicking a cell selects it, while a separate large preview and controls edit only that source. This avoids fitting nine full editor cards into one screen.
