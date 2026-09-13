# NEXT CHAT HANDOFF — 0.1.0-dev.79

## Baseline

`dev.79` follows `dev.78a` and is strictly a scope-locked model/shape reconciliation pass.

The static Mirage light engine remains based on the QA-confirmed `dev.76h` architecture. Core Booster identity/polish from dev.77 remains accumulated. No lighting/power/network mechanics were intentionally changed in dev.79.

## dev.79 visual decisions

- Mirage Prism Alt is LOCKED and intentionally untouched.
- Wide Alt keeps the accepted dev.78a stepped-V design; only its shape is reconciled.
- Compact/Mirage Projector Alt keeps the accepted simple design; only its shape is reconciled.
- Field Alt keeps its accepted visual design; only its shape is reconciled.
- Display Alt restores the dev.78 low-corner-post identity and removes the accidental dev.78a portal-like rear frame; its chamber is reduced.
- Tall Alt is the only major redesign: obsidian-framed purple-glass dome, no antennas, visible core.

## Hard modeling rule

All static model coordinates/pivots remain whole pixels. No decimals/sub-pixel offsets.

## Validation tool

Run:

```text
python tools/verify_dev79_scope_locked_projectors.py
```

This protects accepted visual models via hashes and checks exact model ↔ VoxelShape equality for all non-Prism alt chassis.

## Required in-game QA

- Compare model outline/hitbox directly against visible glass/frame geometry.
- Test core with Netherite Ingot in every chamber.
- Watch full idle core rotation/bob and idle book bob.
- Confirm Display has no portal-like structure.
- Confirm Tall core is visible from normal standing eye height and dome reads as obsidian frame + purple glass.
- Confirm Wide V design did not regress.
- Confirm Prism is unchanged.
