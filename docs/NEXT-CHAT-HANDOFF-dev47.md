# Mirage Projector — next-chat handoff after dev.47

Current source candidate: **0.1.0-dev.47**.

## What this wave did

This snapshot is a direct QA-polish follow-up after the user tested the current projector/image/entity/UI work.

Implemented in source:

1. **Core hologram rendering**
   - installed core item is no longer camera-facing;
   - it now spins clockwise as a world-like hologram inside the chamber;
   - slight tilt added so flat items do not read like a paper-thin portal slice.

2. **Projector chamber visibility**
   - all chassis chamber shells now use a custom crying-obsidian-glass texture intended to be visibly present instead of near-invisible plain glass.

3. **Image workspace text cleanup**
   - fixed the single-image label overlap caused by the missing `faceOffsetY()` on the sizing row;
   - long status/sizing lines are now width-fitted.

4. **Banner / item / entity UI cleanup**
   - redundant plane-banner hint removed;
   - item helper strings shortened;
   - the extra no-body helper line under the entity 3D preview was removed.

5. **Entity projection preview layering**
   - added a new client mixin redirect for `RenderLayer.coloredCutoutModelCopyLayerRender` so ghost rendering also covers colored cutout overlay layers such as cat collars.

6. **Crying obsidian flora visuals**
   - buds and cluster no longer use grass/cross style geometry;
   - replaced with crystal-shaped block models;
   - shard inventory sprite redone toward the previously requested prismarine-shard-like silhouette.

## Main QA points the user should verify next

1. chamber shell opacity/coverage now looks correct on all chassis;
2. core hologram spin/orientation/scale feels correct in-world;
3. cat collar now stays properly layered with the projected cat in the 3D preview and live projection;
4. image workspace text overlap is fully gone in both smaller and larger imports;
5. crying obsidian buds/clusters now read like crystal growths rather than grass.

## Notes

- This container did not have a usable Gradle wrapper executable, so this snapshot is still **source-only** from this environment and needs the usual user-side Windows compile/QA pass.
