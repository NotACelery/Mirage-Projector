# Mirage Projector 1.0.20 — Sodium Packed-Light Bridge Hotfix

1.0.20 is a focused runtime compatibility hotfix over 1.0.19. It keeps network protocol **34** and `ProjectionSettings` format **3**.

## Crash reproduced

The 1.0.19 `LevelRendererMirageLightMixin` queried `BlockAndTintGetter#getLightEngine()` after each packed-light calculation. During Sodium chunk meshing the supplied view is `net.caffeinemc.mods.sodium.client.world.LevelSlice`; that accessor intentionally throws `UnsupportedOperationException`. The result was a client crash while building chunk meshes immediately after joining a world.

## Fix

The packed-light mixin remains client-only but no longer asks the temporary render view for its light engine. It reads Mirage virtual block light from the active Minecraft `ClientLevel`, then merges that block-light value into the packed renderer result while preserving the already-computed vanilla sky-light channel.

This keeps the 1.0.19 visual-light objective intact while avoiding Sodium's unsupported `LevelSlice#getLightEngine()` path.

## Runtime QA

- Join a world with Sodium enabled: chunk meshing must complete without `UnsupportedOperationException`.
- Confirm Focus/Flood/Ambient visibly illuminate terrain as well as light-level overlays.
- Confirm light changes still invalidate/rebuild affected sections.
- Recheck Lantern and placed Light Projector orientation/occlusion cases from the 1.0.19 QA matrix.
