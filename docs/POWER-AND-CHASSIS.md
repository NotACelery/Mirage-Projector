# Projection Power and Chassis — 1.0.4

## Chassis profiles

| Chassis | Geometry | Nominal size | Nominal Lift | Nominal Float | PU multiplier |
|---|---|---:|---:|---:|---:|
| Mirage Projector | Plane | 10×10 | 32 | 4 | ×1.00 |
| Mirage Display | Plane | 32×32 | 48 | 12 | ×1.50 |
| Wide Mirage Projector | Plane | 80×32 | 64 | 12 | ×2.00 |
| Tall Mirage Projector | Plane | 32×80 | 96 | 16 | ×2.00 |
| Mirage Field Projector | Plane | 128×128 | 144 | 24 | ×4.00 |
| Mirage Prism | Prism | 48×48 baseline | 96 | 12 | ×2.00 |

Nominal dimensions are efficiency targets, not hard render caps. The feasible UI range expands when sufficient Projection Power is available.

## Core profiles

| Core material | Base PU | Standard amplification | Loaded Core Booster amplification |
|---|---:|---:|---:|
| Glass | 32 | ×1.00 | ×1.50 |
| Quartz | 48 | ×1.00 | ×1.50 |
| Amethyst | 64 | ×1.00 | ×1.50 |
| Diamond | 96 | ×1.00 | ×1.50 |
| Netherite | 128 | ×1.00 | ×1.50 |

Effective capacity:

```text
floor(Base PU × chassis multiplier × Core amplification)
```

`ProjectionPower` is the only authority for effective capacity, component costs, Overdrive state and dynamic feasible Scale/Lift/Float limits.

## Energy abstraction

Fixed projectors use `ProjectionEnergySource` backed by the installed `ProjectionCoreProfile`. The generic power layer does not require every future Mirage device to expose a fixed-projector Core socket; portable devices can supply another energy backend later.

## Multi-source layouts

- Wide: optional 4-column image layout.
- Tall: optional 4-row image layout.
- Field: one continuous plane.
- Prism: independent cardinal faces where supported by the source family.

## Mirage Prism radial spacing

Mirage Prism Image/Banner faces use a collision-safe radial Distance from the projector center. The base distance required to keep adjacent face envelopes separated is treated as part of the chassis. Additional separation, including extra distance required by Tilt, adds a deliberately small PU surcharge at **1 PU per 64 px** of extra radial reach (rounded up).

Distance does not scale the projected content and is not available as a generic translation axis on the other projector chassis.

## Overdrive

When a requested presentation exceeds the nominal chassis envelope, power demand rises. If available PU is sufficient, the projector can remain active in Overdrive rather than imposing an arbitrary render cap. UI slider limits are recalculated from the current Core/chassis/source/presentation state.
