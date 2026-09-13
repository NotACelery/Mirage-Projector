# NEXT CHAT HANDOFF — 0.1.0-dev.80e

Use **`0.1.0-dev.80e`** as the latest recoverable source snapshot. Network protocol remains **26**.

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

## dev.80c projector held-item size rebalance

- Follow-up to dev.80b after QA showed the projector items were now visible in hand but too large compared with a normal held block.
- GUI, ground and fixed transforms remain unchanged.
- Third-person hand transforms were reduced to scale `0.54/0.50/0.48/0.50/0.48/0.52` (compact/display/field/wide/tall/prism) with translation Y = `2.8`.
- First-person hand transforms were reduced to scale `0.60/0.56/0.54/0.56/0.54/0.58` with translation Y = `0.8`.
- Goal: keep held projectors readable while matching the feel of a normal block more closely.
- No block geometry, VoxelShape, registry, recipe, menu, power or protocol changes were introduced.

## dev.80d projector slab-like item-scale pass

- Follow-up to dev.80c after QA concluded the projector items still felt too large and too full-block-like in both inventory and hand contexts.
- The new target presentation treats projector items more like slab-height devices/props than full cubes.
- All six item-model contexts were retuned: GUI, ground, fixed, first-person and third-person.
- Shared transforms now use GUI rotation `28/225/0`, third-person rotation `72/±45/0` with translation Y `1.6`, first-person rotation `0/±45/0` with translation Y `0.15`, and fixed rotation `0/180/0`.
- Per-item scale targets are: compact `0.72/0.42/0.48`, display `0.68/0.40/0.46`, field `0.66/0.38/0.44`, wide `0.68/0.40/0.46`, tall `0.64/0.38/0.44`, prism `0.70/0.41/0.47` for GUI/third/first respectively.
- Goal: make projector items feel natural as held props, closer to slabs and other shallow blocks, while staying readable and attractive.
- No block geometry, VoxelShape, registry, recipe, menu, power or protocol changes were introduced.

## dev.80e projector first-person lift test

- Follow-up to dev.80d after QA showed the slab-like concept mostly helped the prism, but the other projectors still sat so low in first-person that they nearly disappeared under the HUD edge.
- All slab-like dev.80d scales are preserved.
- The only transform family changed is first-person: translation Y is now `1.05` for all six projector items.
- Third-person, GUI, ground and fixed transforms stay exactly as in dev.80d.
- Goal: test whether the visibility issue is mainly a placement/height problem rather than a scale problem.
- No block geometry, VoxelShape, registry, recipe, menu, power or protocol changes were introduced.

## Verification

Run `python tools/verify_current_line.py`, then Windows `build.bat`. The current verifier checks canonical promotion, projector first-person lift transforms, creative ordering, Compact/Tall final QA fixes, resource coverage and the Mirage Light Engine regression gates.
