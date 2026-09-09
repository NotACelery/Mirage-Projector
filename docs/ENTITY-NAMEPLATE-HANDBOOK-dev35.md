# dev.35 — Entity nameplates and vanilla-book Debug Handbook

## Why dev.34 was insufficient

Two separate rendering strata were involved. The handbook avoided calling `renderBackground` from its own `render`, but Minecraft calls the Screen background stratum before `render`, so the inherited generic blur still happened. Likewise, projected mob names were stored correctly but the custom Mirage label was anchored near the chassis rather than where players expect a vanilla nametag.

## Debug Handbook

`DebugHandbookScreen` now extends Minecraft 1.21.1 `BookViewScreen` and supplies a `BookAccess` built from translated Components. Each documentation paragraph is one physical vanilla page so Spanish/English/Chilean Spanish remain inside the vanilla book width. This uses vanilla book navigation/background behaviour instead of emulating it with a generic Screen.

`RenderHandEvent` suppression for the handbook remains as a narrow fail-safe while this screen is active.

## Entity CustomName

`EntityScanData.View#projectionNameplateText()` is now the single canonical resolver. For mobs it prefers the explicit v5 frozen `CustomNameText`; Player keeps the frozen display/profile name fallback. The reconstructed LivingEntity is also given that frozen CustomName again after projection normalization, while `CustomNameVisible` stays false to prevent vanilla and Mirage from drawing duplicate tags.

Mirage's dedicated projection label is now placed above the projected entity using `EntityProjectionBounds`, including Humanoid/Horse pose expansion, and uses a two-pass font render for translucent-projection readability.
