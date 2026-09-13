# DEV78A — projector model corrections

## Scope

This follow-up snapshot refines the alternate projector chassis models introduced in dev.78, based on in-game QA feedback.

The goal is strictly visual/model-side polish:

- keep all model coordinates pixel-perfect (whole integers only),
- preserve the approved Mirage Prism identity,
- reduce oversized core chambers,
- avoid visual collision between floating decorative items and projector geometry,
- restore stronger identity to Wide and Tall chassis.

No projection logic, gameplay rules or light-engine behavior were intentionally changed here.

## Implemented adjustments

### Mirage Projector (Alt)
- Reworked into a simpler compact/basic silhouette.
- Reduced the visual dominance of the glass chamber.
- Kept the chassis intentionally modest so it reads as the entry-level projector.

### Mirage Display (Alt)
- Reduced the chamber size substantially.
- Added a taller rear display frame so it stays visually distinct from Mirage Field.
- Kept the chamber low enough to reduce book/chamber visual conflict.

### Mirage Field Projector (Alt)
- Lowered/refined the core chamber.
- Preserved the broad flat field-platform identity.
- Kept the chamber clear of the base to avoid obvious core/base collision.

### Wide Mirage Projector (Alt)
- Rebuilt from scratch.
- New silhouette uses a horizontal elongated structure with stepped diagonal arms, forming a visual V profile.
- The center of that V now acts as the magenta core chamber.
- The shape is meant to communicate horizontal spread instead of looking like a generic flat base.

### Tall Mirage Projector (Alt)
- Reworked again to restore a stronger vertical identity.
- Added back a hooded/domed upper structure rather than leaving only two plain towers.
- Widened the chamber footprint while reducing the previous awkward narrow/tall feeling.

### Mirage Prism (Alt)
- Left intentionally aligned with the dev.78 user-approved structure.
- No identity reset was attempted because the current Prism direction is already accepted.

## Renderer anchor adjustments

`ProjectorVisualLayout` was updated so the floating core/book anchors better match the corrected chamber heights:

- Mirage Projector Alt: `(9, 5, 14)`
- Mirage Display Alt: `(10, 5, 15)`
- Wide Mirage Projector Alt: `(9, 5, 14)`
- Tall Mirage Projector Alt: `(12, 6, 15)`
- Mirage Field Projector Alt: `(9, 6, 13)`
- Mirage Prism Alt: unchanged `(12, 7, 16)`

## Validation

Static validation completed:

- all alternate projector model `from`/`to` coordinates remain whole integers,
- no fractional rotation origins were introduced,
- renderer still uses model-specific core/book anchors,
- source line bumped to `0.1.0-dev.78a`.

Recommended in-game QA focus:

1. confirm core item clearance for all projector alt chassis,
2. specifically test Netherite Ingot visual fit inside every chamber,
3. confirm idle book no longer clips ugly into chamber/dome geometry,
4. confirm Wide reads clearly as the horizontal-spread chassis,
5. confirm Tall feels like a visual upgrade rather than a downgrade.
