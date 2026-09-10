# dev.61 — Modded equipment Ghost render safety

## Reproduced failure

A projected Humanoid/Player wearing Create's Backtank could crash the client/world render as soon as the projection became active. The crash was `IllegalStateException: Not building!` from `BufferBuilder`, reached through Create/Ponder's Backtank render path while Mirage was flushing a deferred Entity projection.

## Root cause

The late Ghost pass used one shared `ByteBufferBuilder` for every RenderType. A foil/glint equipment renderer can request two consumers at the same time: one for glint and one for the base model. Switching the shared BufferSource to the second RenderType closes the first builder even though `VertexMultiConsumer` still owns it. The next vertex written to that closed builder raises the crash.

## dev.61 correction

The deferred Ghost BufferSource now uses `MultiBufferSource.immediateWithBuffers`. Independent fixed builders are reserved for the vanilla glint variants and Mirage's known block-atlas/shield/banner/armor-trim Ghost render types. The shared fallback remains available for dynamic entity/mod textures.

This keeps the real custom Backtank surface inside Mirage's alpha/tint Ghost remapping instead of hiding the layer or disabling enchantment rendering globally.

## Required QA

1. Project a Player/Humanoid with a Create Backtank and enable Ghost opacity below 100%.
2. Repeat with foil/glint active on the Backtank.
3. Confirm no `Not building!` crash.
4. Confirm the Backtank itself receives the projection alpha/tint rather than staying opaque.
5. Re-test vanilla enchanted armor, armor trims, held enchanted items, shield and a normal modded armor item.
6. Re-test clouds and water because dev.61 deliberately preserves the late Ghost scheduling from dev.58.
