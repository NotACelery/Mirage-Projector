# dev.30 — Species-aware Entity bounds

Entity clearance/render bounds no longer treat every Entity projection as a square `Scale × Scale` object.

- Reads the scanned `EntityType` native width/height.
- Preserves the renderer's existing rule that Scale targets the entity's largest native dimension.
- Humanoid pose expansion still applies on top of native dimensions.
- Horse Rearing reserves a taller conservative envelope than Idle.
- Generic entities such as cats/cows now use their own aspect ratio for clearance and BlockEntity render culling.
- The change affects clearance/culling only; it does not alter the rendered model scale itself.

This is species-aware and substantially tighter than the previous square bound. Special renderers with visual geometry beyond their EntityType dimensions (for example unusual modded effects) remain part of later renderer-specific QA.
