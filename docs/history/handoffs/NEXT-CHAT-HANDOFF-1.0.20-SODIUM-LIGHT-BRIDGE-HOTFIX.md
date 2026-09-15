# Next Chat Handoff — Mirage Projector 1.0.20 Sodium Light Bridge Hotfix

Baseline: **1.0.20**, Minecraft 1.21.1, NeoForge 21.1.244+, Java 21, network protocol 34, ProjectionSettings format 3.

1.0.19 crashed in Sodium chunk meshing because `LevelRendererMirageLightMixin` called `BlockAndTintGetter#getLightEngine()` on Sodium `LevelSlice`, which throws `UnsupportedOperationException`. 1.0.20 changes that client-only visual bridge to query Mirage virtual light from `Minecraft.getInstance().level` / active `ClientLevel` instead, while retaining the packed vanilla sky-light channel.

Next runtime gate: launch/join with Sodium, then verify visible Focus/Flood/Ambient terrain lighting and the rest of the 1.0.19 runtime-QA list.
