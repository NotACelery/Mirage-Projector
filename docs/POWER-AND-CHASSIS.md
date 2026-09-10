# Mirage Projector — Power and chassis

## Chassis table

| Chassis | Geometry | Nominal width × height | Nominal Lift | Nominal Float | PU multiplier |
|---|---|---:|---:|---:|---:|
| Compact | Plane | 10×10 | 32 | 4 | ×1.00 |
| Display | Plane | 32×32 | 48 | 12 | ×1.50 |
| Wide | Plane | 80×32 | 64 | 12 | ×2.00 |
| Tall | Plane | 32×80 | 96 | 16 | ×2.00 |
| Field | Plane | 128×128 | 144 | 24 | ×4.00 |
| Prism | Prism | adaptive 48×48 baseline | 96 | 12 | ×2.00 |

Nominal values are efficiency targets, not hard clipping walls.

## Core capacity

Standard Base PU:

- Glass 32;
- Quartz 48;
- Amethyst 64;
- Diamond 96;
- Netherite 128.

Standard Core amplification is ×1.00. A loaded Core Booster uses the same material Base PU at ×1.50.

```text
Effective PU = floor(Base PU × chassis multiplier × amplification)
```

## Load model

Current centralized constants include:

- base emitter cost: 2 PU;
- geometry: 256 projected pixels per PU baseline;
- Lift: 16 px per PU baseline;
- Float: 2 px per PU baseline;
- Ghost rebate divisor: 3000.

Source/effect complexity adds its own small costs. Chassis-overdrive costs are applied per component when a nominal target is exceeded.

## Dynamic limits

Scale, Lift and Float controls are solved against the current Core/chassis/source load. The UI should expose the highest feasible endpoint rather than forcing the player to search manually through a large invalid region.

The Creative debug chassis override removes chassis overdrive penalties for testing; it does not create PU and does not bypass total capacity.

## Wide/Tall image equivalence

Wide and Tall MULTI layouts use four equal square cells. Their scaling rule must stay symmetric so the same source does not become arbitrarily larger merely because the chassis is Tall.

## Prism

Prism image geometry is content-adaptive per face:

- horizontal sources target a wide-like envelope;
- vertical sources target a tall-like envelope;
- near-square sources target the square baseline.

Its four source directions are world-cardinal N/E/S/W, not relative labels that rotate with block facing.
