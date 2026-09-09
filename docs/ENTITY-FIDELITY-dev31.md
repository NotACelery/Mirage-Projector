# dev.31 — Entity reconstruction fidelity

## Problem observed in live QA

A scan could contain the correct Player texture while the reconstructed projection still differed visually from the source. The reason is that not every Player visual setting is part of normal entity NBT. In addition, Generic sitting-pose changes were not invalidating the cached temporary entity.

## Player scan v4

`EntityScanData` version 4 keeps the existing frozen GameProfile `textures` property and adds:

- `SkinModel`: `slim` or `wide`, decoded from the packed vanilla texture metadata when available;
- `ModelParts`: the bit mask produced from the source Player's visible `PlayerModelPart` values;
- `MainArm`: the source Player's dominant arm.

The temporary `MirageRemotePlayer` restores model-part visibility and dominant arm after its NBT load. Its resolved `PlayerSkin` is wrapped with the frozen skin model so the dispatcher selects the same slim/wide PlayerRenderer geometry as the captured source.

Old cards remain readable. Missing v4 fields fall back to vanilla defaults, so exact fidelity for a previously scanned Player requires rescanning once with dev.31+.

## Generic pose cache

Generic projections have no editable equipment snapshot set, so the old equipment fingerprint returned early as the constant `generic`. That made `Idle/Sitting` changes reuse the old client entity. dev.31 includes the Generic pose in the fingerprint before the equipment-null early return.

## QA

1. Scan a slim-skin Player with visible jacket/sleeves/hat; compare source and projection.
2. Toggle one or more skin customization layers before scanning and verify the projection preserves exactly those layers.
3. Scan a left-handed Player and verify held-item side/pose semantics.
4. Disconnect the scanned Player and reload the projection to verify the packed frozen skin still resolves.
5. Scan Cat/Wolf/Parrot and toggle Idle/Sitting repeatedly without reopening the GUI.
6. Confirm old v3 Player cards still open without crashes; then rescan to obtain v4 fidelity.
