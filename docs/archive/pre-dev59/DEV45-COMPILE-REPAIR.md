# dev.45 — first Windows compile repair

The first Windows `build.bat` run for `0.1.0-dev.45` reached `:compileJava` and reported five compiler errors, all caused by two import/symbol issues.

## Repaired causes

1. `CryingObsidianBeaconRenderer` imported `ClipContext` from `net.minecraft.world.phys`. For the Minecraft 1.21.1 / NeoForge target it belongs to `net.minecraft.world.level`.
2. `MirageProjectorBlock` invokes `Block.popResource(...)` in the packed Survival-drop path but did not import `net.minecraft.world.level.block.Block`.

No gameplay, recipe, persistence, protocol, Power or rendering design behavior was intentionally changed by this repair. Network protocol remains 18 and mod version remains `0.1.0-dev.45`.

## Status

Source/static repair complete. Run `build.bat` again on Windows. Do not mark dev.45 build-clean until that rebuild succeeds.
