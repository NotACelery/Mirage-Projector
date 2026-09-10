# Mirage Projector — next-chat handoff after dev.57

Current source candidate: **0.1.0-dev.57**.

This wave is a focused transparency-composition fix on top of the dev.56 compile fix + pixel-2 ray origin snapshot.

## Change

Mirage ghost entity/item RenderTypes now output through Minecraft's `ITEM_ENTITY_TARGET` instead of `MAIN_TARGET`, while retaining colour-only writes and normal depth testing. This is intended to stop clouds from being composited visually in front of semi-transparent projected bodies/armor without regressing the earlier water-hole fix.

## QA

- projected body vs clouds;
- projected armor vs clouds;
- projected held item vs clouds;
- water behind transparent projection;
- normal and Fabulous graphics if practical.
