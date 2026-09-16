# Mirage Projector 1.0.27 — Build Stability Hotfix

1.0.27 is a source/build correction over 1.0.26. It intentionally adds no gameplay feature and keeps network protocol **38** plus `ProjectionSettings` serialization format **4**.

## Windows build failures corrected

The first authoritative Windows `build.bat` of 1.0.26 exposed four source integration defects:

- `MirageProjectorRenderer` used `Direction` without importing `net.minecraft.core.Direction`;
- Presentation Remote code imported `ResourceKey` from `net.minecraft.core` instead of the Minecraft 1.21.1 location `net.minecraft.resources.ResourceKey`;
- three chassis-normalization call sites still required the canonical `ProjectionSettings.withBackFaceMode(...)` copy helper after the format-4 settings refactor;
- a removed `DuplicatingLecternBlock.java` from the superseded 1.0.22 station could survive if a newer source ZIP was copied over an existing Windows project folder.

The source imports/helper are repaired and `CLEAN-MIRAGE-PROJECTOR.bat` now deletes the obsolete Duplicating Lectern class before every build. The active source tree continues to use only the vanilla-Lectern Codex workflow introduced in 1.0.23 and reworked in 1.0.24.

## Compatibility

No registry ID, packet payload contract, Presentation Deck behavior, remote binding identity, automatic-presentation timer semantics, wall-surface validation rule or projection data format changes in this hotfix.
