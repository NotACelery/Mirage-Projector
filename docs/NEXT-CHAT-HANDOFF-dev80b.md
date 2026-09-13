# NEXT CHAT HANDOFF — 0.1.0-dev.80b

Use **`0.1.0-dev.80b`** as the latest recoverable source snapshot. Network protocol remains **26**.

## dev.80 canonical projector promotion

- The six accepted dev.79i comparison models are now the canonical runtime models.
- Removed runtime registrations/resources for `mirage_projector_alt`, `mirage_display_alt`, `mirage_field_projector_alt`, `wide_mirage_projector_alt`, `tall_mirage_projector_alt`, and `mirage_prism_alt`.
- Replaced the six canonical block-model JSONs with the accepted dev.79i models.
- Archived the six old canonical block models under `docs/history/projector-models-pre-dev80/legacy-canonical/`; they are outside `assets` and unused at runtime.
- Canonical VoxelShapes now directly match the promoted models; Prism was also reconciled to every visible cuboid during the promotion.
- `ProjectorVisualLayout` now uses the promoted model anchors directly by chassis rather than checking temporary Alt blocks.
- Custom Mirage tab and vanilla Functional Blocks order: Mirage Projector → Mirage Display → Mirage Field Projector → Wide Mirage Projector → Tall Mirage Projector → Mirage Prism.
- Projector ON/OFF, white active-mode outline, workspace `Mode currently Active`, and dormant End Resonance TURN OFF guard remain intact.

## dev.80a projector item presentation

- The dev.80 canonical projector promotion remains the runtime base.
- The six canonical projector item models now define explicit display transforms for GUI, ground, fixed, third-person and first-person rendering.
- Goal: make projector items read like real placed blocks in inventory/hand contexts instead of thin debug-like silhouettes.
- No block geometry, VoxelShape, registry, recipe, menu, power or protocol changes were introduced.

## dev.80b projector held-item visibility tuning

- Follow-up to dev.80a after QA showed the projector items looked acceptable in inventory and on the ground but were too small in hand.
- GUI, ground and fixed transforms remain unchanged from dev.80a.
- Third-person hand transforms were enlarged and lifted to scale `0.68/0.62/0.60/0.62/0.60/0.64` (compact/display/field/wide/tall/prism) with translation Y = `3.5`.
- First-person hand transforms were enlarged and lifted to scale `0.78/0.72/0.70/0.72/0.70/0.74` with translation Y = `1.5`.
- Goal: make held projectors clearly visible without disturbing the improved inventory and dropped-item presentation.
- No block geometry, VoxelShape, registry, recipe, menu, power or protocol changes were introduced.

## Verification

Run `python tools/verify_current_line.py`, then Windows `build.bat`. The current verifier checks canonical promotion, projector held-item visibility transforms, creative ordering, Compact/Tall final QA fixes, resource coverage and the Mirage Light Engine regression gates.
