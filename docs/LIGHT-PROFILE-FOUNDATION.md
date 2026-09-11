# Mirage Projector — light profile foundation / compatibility contract

Current line: **dev.75d**. The complete current engine authority is `MIRAGE-LIGHT-ENGINE.md`.

This file preserves the profile-design boundary introduced in dev.66/dev.74 and records what is runtime-enabled versus reserved.

## Runtime profile data

`MirageLightProfile` currently carries:

```text
conceptualLight
substepsPerLightLevel
airStepCostUnits
detourExtraCostUnits
maxRadius
decayMode
shape
direction
coneAngleDegrees
rgb
```

`MirageLightSource` additionally carries its stable source ID/origin and runtime mode.

## Current Mature EXTEND profile

```text
conceptual = 15 + reflected static boost (0..4)
substeps   = 2
airCost    = 1
detourExtra= 1
radius     = conceptual * 2 (30..38)
shape      = OMNIDIRECTIONAL
runtime    = STATIC_WORLD
```

Open travel therefore loses half a visible level per block. Obstacle-only extra path loses a full visible level per extra block overall. Visible result is capped to vanilla 15.

The detour term is deliberately profile data, not hard-coded Cluster logic. Future profiles can choose zero, equal or stronger shadow-routing penalty while sharing one solver.

## Runtime-enabled decay/shapes

Current authoritative scalar solver supports:

- `VANILLA` + `OMNIDIRECTIONAL`;
- `EXTEND` + `OMNIDIRECTIONAL`.

Reserved, not yet runtime-enabled:

- `CONCENTRATE`;
- `DIRECTIONAL_SPOT`;
- `ROTATING_DIRECTIONAL_SPOT`;
- directional cone;
- rectangular frustum;
- plane/projected-surface emission.

Do not claim these as player features until dev.76+ implements their solver/backend semantics.

## Runtime modes

- `STATIC_WORLD`: implemented; deterministic server/client source field suitable for Mature world/gameplay light.
- `DYNAMIC_VISUAL`: reserved for moving/portable/projected emitters. It must use a moving-light backend rather than forcing server static-field solves every frame.

## RGB reservation

`rgb` already travels with the profile and the Mature uses `0xA84CFF` metadata. Current authoritative gameplay/render bridge is scalar 0–15. RGB is reserved so future visual light can preserve color without changing source/profile identity; scalar gameplay semantics may collapse color to luminance/intensity.

## Compatibility boundary

Legacy `celerbi.mirageprojector.light.LightProfile` / `LightDecayMode` helpers remain where older code/design inputs still reference them. The forward source/solver/storage authority is `celerbi.mirageprojector.light.engine`.
