# dev.25 build fix

dev.24 introduced a compile-only regression in `EntityProjectionPreviewRenderer`: the call to `EntityRenderDispatcher#render` contained four position/rotation doubles before the float arguments, while Minecraft 1.21.1 expects three position doubles followed by yaw and partial tick floats.

dev.25 removes the extra `0.0D` and leaves the rest of the dev.24 Entity/Ghost/layout implementation unchanged.
