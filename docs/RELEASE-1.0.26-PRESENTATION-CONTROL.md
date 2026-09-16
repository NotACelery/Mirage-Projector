# Mirage Projector 1.0.26 — Presentation Deck Automation + Data-show Remote

1.0.26 extends the 1.0.25 Table/Wall presentation foundation without changing the Wall/Data-show surface contract. Network protocol advances from **37 to 38** because the Image Workspace now synchronizes automatic-presentation state and the new handheld Presentation Remote uses dedicated open/action payloads. `ProjectionSettings` remains format **4**.

## Shared Presentation Deck

Mirage Table Projector and Mirage Wall Projector/Data-show use the same ordered nine-image deck. The workspace supports import/replace, clear, reorder, select current, Previous and Next.

Automatic Presentation is opt-in. The user can select an interval from **1 to 120 seconds**. The server owns elapsed presentation time. Playback only advances while the projector is enabled and at least two images are present. A manual slide selection from either the projector GUI or a remote does **not** disable automatic mode and does **not** reset the automatic clock. The remaining time is preserved; the next timed step advances from whichever slide is active at that moment.

With automatic mode disabled, no background process changes the current slide.

## Presentation Remote

`mirage_projector:presentation_remote` is a stack-size-one handheld controller. The Wall/Data-show model exposes a physical top pairing dock. Right-clicking the Data-show with a remote inserts/binds it. Sneak + empty-hand right-click retrieves the docked remote before the normal packed-projector pickup gesture.

Binding stores a persistent projector link UUID plus its current dimension and block position. A remote action is accepted only when:

- the player is in the bound dimension;
- the target chunk is already loaded (the remote never force-loads it);
- the target is a Mirage Wall Projector/Data-show;
- the target's persistent presentation link UUID matches the remote.

This prevents a different projector placed later at the same coordinates from inheriting the old remote.

Holding RMB with a bound remote opens a lightweight, non-pausing, no-background controller overlay. It begins at neutral `-`; moving left/right selects `<--` / `-->`; releasing RMB commits exactly one previous/next step. The overlay is intentionally an in-world quick control rather than a normal inventory screen.

The current remote crafting recipe is a **balance placeholder**, not a frozen 1.1.0 recipe. The remote dock and binding are exclusive to the Wall/Data-show chassis; Table shares the presentation deck/automatic playback but has no remote dock.

## Invalid Wall slides

Wall/Data-show projection remains stricter than hologram rendering. `WallProjectionSurface` evaluates only the real aspect-correct image rectangle after Scale and signed X/Y offsets. Terrain outside that actual image footprint is irrelevant.

If the selected slide cannot be projected onto a regular unobstructed wall at any allowed resolved scale/power combination, the deck index **still changes to that slide**. The image itself is not rendered. Instead the renderer shows Mirage's transparent red prohibition symbol (circle plus `/`, never an X). Previous/Next and automatic playback continue to traverse normally, so one invalid slide cannot trap the presentation.

## Persistence

The following survive save/reload and packed projector transfer:

- ordered nine-image deck;
- active slide index;
- automatic-presentation enabled state;
- interval and elapsed timer state;
- persistent presentation link UUID;
- docked Presentation Remote ItemStack.

Extracting a docked remote after a projector was moved refreshes the remote's dimension/position while retaining the same logical projector link UUID.
