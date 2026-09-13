# NEXT CHAT HANDOFF — 0.1.0-dev.80

Use **`0.1.0-dev.80`** as the latest recoverable source snapshot. Network protocol remains **26**.

## dev.80 canonical projector promotion

- The six accepted dev.79i comparison models are now the canonical runtime models.
- Removed runtime registrations/resources for `mirage_projector_alt`, `mirage_display_alt`, `mirage_field_projector_alt`, `wide_mirage_projector_alt`, `tall_mirage_projector_alt`, and `mirage_prism_alt`.
- Replaced the six canonical block-model JSONs with the accepted dev.79i models.
- Archived the six old canonical block models under `docs/history/projector-models-pre-dev80/legacy-canonical/`; they are outside `assets` and unused at runtime.
- Canonical VoxelShapes now directly match the promoted models; Prism was also reconciled to every visible cuboid during the promotion.
- `ProjectorVisualLayout` now uses the promoted model anchors directly by chassis rather than checking temporary Alt blocks.
- Custom Mirage tab and vanilla Functional Blocks order: Mirage Projector → Mirage Display → Mirage Field Projector → Wide Mirage Projector → Tall Mirage Projector → Mirage Prism.
- Projector ON/OFF, white active-mode outline, workspace `Mode currently Active`, and dormant End Resonance TURN OFF guard remain intact.

## Next visual task

The six projector inventory icons still inherit the block-model view and look like thin lines with a purple square. The next pass should design/readjust dedicated inventory presentation for all six canonical projectors without changing their placed-block models.

## Verification

Run `python tools/verify_current_line.py`, then Windows `build.bat`. The current verifier checks Alt removal, canonical model/shape promotion, creative ordering, Compact/Tall final QA fixes, resource coverage and the Mirage Light Engine regression gates.
