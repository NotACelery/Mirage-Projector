# Mirage Projector 1.0.29 — Runtime QA Interaction Fixes

1.0.29 is the first broad runtime correction pass after 1.0.28 successfully reached an in-game world. It bumps the custom payload protocol to **39** because Mirage Equipment now sends a Creative cursor snapshot/correction and the Wall/Data-show exposes an explicit remote-unpair action. `ProjectionSettings` remains format **4**.

## Mirage Equipment

- In Creative, the Mirage Equipment toggle exists only while the vanilla player-inventory tab is open; Building Blocks/Search/etc. hide it immediately.
- The toggle/panel are offset away from the vanilla frame instead of touching the right border.
- Equipment slots outside the vanilla container bounds intercept their clicks before `AbstractContainerScreen` can treat them as an outside drop.
- The Creative picker menu's visible carried stack is sent with the action and is trusted only for Creative players; Survival continues using the authoritative server container cursor.
- The server sends an explicit carried-stack correction back to the currently visible container menu. Shoulder Strap insertion therefore behaves as a real slot interaction instead of throwing the item into the world.

## Scan Codex

- Handheld and lectern modes explicitly draw the parchment/book canvas in `render()` before container widgets.
- Vanilla background blur/dimming remains disabled.
- The search field is integrated into the parchment instead of appearing as an unanchored black bar.
- Lectern home/Import/Duplicate controls have visible page-extension backing.

## Table Projector

- Image, Item, Entity and Banner buttons now activate the selected source server-side before opening its workspace.
- The Table installed-Core anchor uses a smaller 0.13 scale and centered 4.5 px height so rotating/bobbing Core models remain inside the chamber rather than clipping through the glass/base.

## Wall/Data-show Presentation Remote

- Docking binds the actual held remote before creating the physical dock copy. This fixes Creative mode, where the held source stack is not consumed and previously remained unbound.
- The projector GUI exposes **Unpair Remote** while a pairing exists. Unpairing invalidates the link UUID, unbinds matching player-held/inventory copies, ejects the physical docked remote, and leaves the dock ready for a new controller.
- Old controller copies with the invalidated UUID can no longer operate the projector.

## Wall/Data-show hit geometry

- The block outline/collision shape now follows the low-profile data-show body, front lens assembly, top accent and remote dock, rotating with N/E/S/W facing.
- Collision explicitly reuses the same shape as selection to prevent a one-block-full invisible collision surface.
