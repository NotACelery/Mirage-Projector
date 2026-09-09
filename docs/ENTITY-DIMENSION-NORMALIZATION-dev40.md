# Entity dimension normalization — dev.40

## Problem

Mirage reconstructs scanned LivingEntity instances as **client-only render clones** in the current `ClientLevel`. They are never spawned, never tick AI and never convert. Even so, some vanilla renderers query environment-sensitive entity state every render call.

Piglin is the confirmed case. In a dimension that is not piglin-safe (Overworld/End), `AbstractPiglin#isConverting()` reports the zombification state used by the vanilla Piglin renderer's shaking path. A Mirage Piglin therefore visibly trembles in both the Entity Workspace preview and the world projection even though the Mirage clone can never actually finish that conversion.

Hoglin follows the same family of dimension-driven zombification semantics and is normalized in the same pass to prevent the equivalent Zoglin conversion shake.

## dev.40 rule

Projection clones are **dimension-neutral visual snapshots**. The dimension containing the projector must not force a visual conversion state that the original scan did not intentionally encode as a Mirage feature.

Immediately after loading frozen entity NBT and before rendering:

- `AbstractPiglin` clone -> `setImmuneToZombification(true)`;
- `Hoglin` clone -> `setImmuneToZombification(true)`.

This only mutates the temporary client-side clone. Mirage does **not** rewrite the Entity Scan Card, source UUID, frozen NBT or real entity.

## Why this is preferable to ticking or NoAI hacks

- Mirage still never calls `tick()` / `aiStep()` on projection-only entities.
- No world mutation, sounds, particles, conversion or AI can happen.
- We do not globally set `NoAI` as a renderer workaround because other renderers may interpret AI state visually.
- The fix is scoped to the environmental conversion flag that causes the shake.

## Shared path

Both preview and world projection use `EntityProjectionClientEntityFactory`, so the normalization applies to both render contexts and cannot drift into two independent fixes.

## QA

1. Scan a normal Piglin in the Nether.
2. Preview it from a projector in the Overworld with the cursor completely still: no shaking.
3. Project it in-world in the Overworld: no shaking.
4. Rotate/hover the preview manually: head/body follow the intended Mirage preview controls smoothly.
5. Repeat with a Hoglin.
6. Re-enter the Nether and confirm the same scans render identically.
7. Confirm the real Piglin/Hoglin and the scan-card data are untouched.

## Future audit

If another entity reacts visually to the projector dimension or local biome/block state without being ticked, add a **specific projection-only normalization adapter** here rather than enabling AI or mutating the frozen scan generically.
