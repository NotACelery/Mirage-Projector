# dev.72 — Entity envelope, nameplate and frustum hardening

## Goal

Make large Entity projections cull from their real visual envelope instead of from the chassis, restore the intended projected-nameplate placement, and add conservative generic bounds for visible equipment without introducing an LOD/quality policy.

## Shared Entity bounds

`EntityProjectionBounds` remains the shared source for Entity preview/clearance/world envelope sizing. It starts from the scanned EntityType dimensions and pose expansion, then reserves conservative extra extent when visible projected equipment can exceed the vanilla body box:

- one visible Humanoid hand: width at least 1.30× target scale;
- both visible hands: width at least 1.42× target scale;
- visible hand equipment: height at least 1.18× target scale;
- visible Chest: width at least 1.20× target scale;
- visible Head: height at least 1.10× target scale;
- visible Horse Body Armor: width at least 1.08× target scale.

These are culling/clearance safety envelopes, not gameplay size changes and not claims that every modded renderer fits perfectly. Concrete renderers that exceed them still belong in targeted P2 adapters/refinements.

## Nameplate

The dedicated Mirage label is anchored at:

```text
physical projector top
+ Lift
+ current Float bob
+ projected Entity envelope height
+ 4 px gap
```

It remains camera-billboarded and two-pass, but its RGB/alpha now derive from the same Tint/Ghost settings as the hologram. Vanilla internal name rendering stays suppressed to avoid duplicates.

## BER culling

`MirageProjectorRenderer#getRenderBoundingBox` now unions the physical block with the full active projection:

- projection width/rotation radius;
- Entity pose/equipment envelope;
- Lift;
- downward Float amplitude;
- projected height;
- nameplate width/height where present.

Because this AABB is authoritative, `shouldRenderOffScreen` returns false. This lets vanilla frustum culling avoid submitting off-camera giant projections while preserving high-Lift projections whose chassis itself is no longer visible. `getViewDistance()` remains 256; dev.72 does not introduce distance LOD or quality reduction.

## Protocol/state

No saved or network fields change. Protocol remains 19.

## Acceptance

See `QA-REGRESSION.md` dev.72 section. Windows Java 21 compilation and in-game screen-edge/high-Lift/Create regression QA are still required before build-clean acceptance.
