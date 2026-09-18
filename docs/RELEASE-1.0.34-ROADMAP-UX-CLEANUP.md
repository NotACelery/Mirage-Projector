# Mirage Projector 1.0.34 — Roadmap & UX/visual cleanup

## Scope

This checkpoint intentionally cleans already-playable systems and freezes the remaining 1.1 implementation order before further End Resonance work. Network protocol remains **43** and ProjectionSettings format remains **4**.

## Delivered

- Restored the original presentation/Data-show **Mirage Wall Projector** (`mirage_projector:mirage_wall_projector`) as the sole wall-projector identity.
- Removed the temporary `mirage_wall_illuminator` registry/resources/recipe and added cumulative overlay tombstones.
- Fixed Table mode navigation: opening Image/Item/Entity/Banner does not mutate SourceMode; only explicit Use Mode actions do.
- Replaced copied-profile-era Hand Projector empty-state text with `No projection has been configured.`
- Rebuilt Mirage Flashlight held/placed models around a coherent Crying-Obsidian + magenta-glass work-flashlight form; held form is horizontal/forward and Ambient points upward.
- Reworked Mirage Light Projector exterior/interior material balance, enlarged the front reflector to a one-pixel housing margin, and added four upper lateral Ambient lenses.
- Added `NEXT-WAVES-1.1.0.md` as the recovery-oriented roadmap.

## Deliberately not in this wave

- End Resonance dimensional transfer.
- Dynamic-light terrain invalidation/stress pass.
- Dynamic source-registry workspace enumeration.
- Final Codex/presentation art polish and ceiling decision.
- Mirage Light Projector visible yaw/pitch aiming; this remains deliberately last.

## Recipe viewers

The current line exposes eleven surviving Survival crafting recipes natively through the vanilla RecipeManager to JEI and EMI. The removed `mirage_wall_illuminator` recipe must not appear.
