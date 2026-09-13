# Mirage Projector — next-chat handoff dev.64

## Current source

`0.1.0-dev.64`

Protocol remains 18.

## Why dev.64 exists

Live dev.63 QA compiled and ran, but the Netherite Backtank synthetic diving chest remained much more opaque than the separate tank and still looked unstable. Create renders one `create:netherite_backtank` chest item as two Humanoid armor surfaces (`netherite_diving_layer_2` inner, `netherite_diving_layer_1` outer) plus the independent Backtank block-model layer. Pair-normalizing the two armor alphas did not account for the projected Humanoid body underneath.

## dev.64 change

During Ghost transparency only, Mirage discards the synthetic inner diving layer and its paired glint when present, then renders the outer diving layer once at the requested opacity using late Entity depth stabilization. The separate tank renderer is unchanged. At 100% opacity Create remains completely native. A texture-aware path is backed by a deterministic two-pass fallback scoped to an active projected `create:netherite_backtank`, so there is no hard Create class dependency.

## QA next

Compile with the existing dev.63-fixed `build.bat`. Test the same captured Player/Humanoid with Netherite Backtank at 100%, 99%, 90%, 50% and 10% opacity; orbit around it; compare chest fade to tank fade; test foil/glint if available; recheck clouds and water.

No file was removed or relocated in dev.64. Do not issue or modify the cleaner for this wave.
