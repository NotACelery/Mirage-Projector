# Projection Power System Rework — dev.38

> **AUTHORITATIVE CURRENT CONTRACT.** This document supersedes the dev.8/dev.19 model in which each Projection Core had independent hard Scale/Lift/Float limits and each chassis had an absolute physical Scale/Lift/Float wall. Historical notes may still describe that model, but they are not current behaviour.

## 1. Why the old model is deprecated

The previous model combined three different responsibilities:

1. the Core supplied PU;
2. the Core also hard-capped Scale/Lift/Float;
3. the chassis imposed another hard Scale/Lift/Float/envelope cap.

This created dead power. A strong Core could report a large amount of unused PU while the chassis hard cap prevented the player from spending it. It also forced the UI to expose large generic sliders and let the player hunt backwards until an invalid/orange configuration became valid.

The dev.38 model removes that contradiction. Power and efficiency are separate concepts.

## 2. New three-layer model

### 2.1. Projection Core = raw PU source

A standard Core contributes **base PU only**. It no longer owns Scale/Lift/Float hard caps.

| Standard Core material | Base PU |
|---|---:|
| Glass | 32 |
| Quartz | 48 |
| Amethyst | 64 |
| Diamond | 96 |
| Netherite | 128 |

These values are the dev.38 provisional balance curve. They are centralized in `ProjectionCoreProfile` and may be tuned later without changing the architecture.

Every current raw-material Core has:

```text
Core amplification = ×1.00
```

### 2.2. Chassis = geometry + efficiency multiplier + nominal operating range

A chassis determines:

- what geometry/source layout it supports;
- its **PU multiplier** (how effectively it uses the Core);
- its **nominal** width/height;
- its **nominal** Lift;
- its **nominal** Float;
- source/layout capabilities.

It does **not** impose an absolute Scale/Lift/Float gameplay wall in dev.38.

| Chassis | Nominal W×H | Nominal Lift | Nominal Float | PU multiplier | Image contract |
|---|---:|---:|---:|---:|---|
| Mirage Projector / Compact | 10×10 px | 32 px | 4 px | ×1.00 | one Plane |
| Mirage Display | 32×32 px | 48 px | 12 px | ×1.50 | one Plane |
| Wide | 80×32 px | 64 px | 12 px | ×2.00 | one wide Plane OR optional 4×1 |
| Tall | 32×80 px | 96 px | 16 px | ×2.00 | one tall Plane OR optional 1×4 |
| Mirage Field | 128×128 px | 144 px | 24 px | ×4.00 | one continuous large Plane; never 3×3 |
| Mirage Prism | 48×48 px per lateral face | 96 px | 12 px | ×2.00 | N/E/S/W faces |

Effigy/Colossal profiles remain future architecture and are not balance-authoritative gameplay chassis yet.

### 2.3. Core grade/amplification = future improved-Core hook

The power formula already includes a third multiplier:

```text
Core amplification
```

Current raw-material cores are Standard (`×1.00`). The first purpose-built improved Core family is reserved around:

```text
Improved Core amplification ≈ ×1.50
```

An Improved Netherite Core should therefore retain **128 base PU**, rather than becoming a second arbitrary PU tier, but would provide a stronger amplification multiplier.

The exact improved-Core items, recipes, models/textures and naming are deliberately deferred until the standard power curve has been QA'd. The dev.38 architecture is already prepared for them; adding those items must not require another power-model rewrite.

## 3. Effective capacity formula

The projector's usable budget is:

```text
Effective PU = floor(
    Core base PU
    × Chassis PU multiplier
    × Core amplification
)
```

Examples with Standard cores:

```text
Compact + Netherite
128 × 1.00 × 1.00 = 128 Effective PU

Field + Glass
32 × 4.00 × 1.00 = 128 Effective PU

Wide + Quartz
48 × 2.00 × 1.00 = 96 Effective PU

Display + Diamond
96 × 1.50 × 1.00 = 144 Effective PU
```

This intentionally makes **Compact + Netherite** and **Field + Glass** equal in raw effective capacity (128 PU), while their geometry efficiency remains very different because Compact is overdriven much sooner.

Future example:

```text
Field + Improved Netherite
128 × 4.00 × 1.50 = 768 Effective PU
```

## 4. Nominal range vs Overdrive

The old hard chassis limit is removed. Chassis dimensions/Lift/Float are now **nominal efficiency targets**.

At or below nominal:

```text
Overdrive multiplier = ×1.00
```

Above nominal, only the component that exceeds nominal receives a quadratic penalty.

For a component ratio `r`:

```text
r = current / nominal
component cost multiplier = max(1, r²)
```

Examples:

```text
1.00× nominal -> 1.00× component cost
1.25× nominal -> 1.5625× component cost
1.50× nominal -> 2.25× component cost
2.00× nominal -> 4.00× component cost
3.00× nominal -> 9.00× component cost
```

This is the key design property:

- a very strong Core **can** force a small projector beyond its nominal design;
- doing so becomes rapidly inefficient;
- a larger chassis can reach the same physical result for much less PU;
- no installed Core becomes useless merely because a hard chassis cap was reached.

### 4.1. Geometry Overdrive

For Plane/Item/Entity geometry, Mirage compares the actual projected dimensions against the chassis nominal W×H:

```text
geometryRatio = max(
    actualWidth / nominalWidth,
    actualHeight / nominalHeight,
    1
)
```

The geometry PU component is then multiplied by `geometryRatio²`.

This also means aspect ratio matters correctly. A very tall image placed in Wide can overdrive Wide's nominal 32 px height even if its longest axis is still reasonable.

### 4.2. Lift Overdrive

```text
liftRatio = max(1, Lift / nominalLift)
```

Only the Lift cost receives `liftRatio²`.

### 4.3. Float Overdrive

```text
floatRatio = max(1, Float / nominalFloat)
```

Only the Float cost receives `floatRatio²`.

Float still has one physical safety invariant independent from PU:

```text
Float amplitude <= Lift
```

This prevents the bobbing projection from oscillating down through the projector/floor.

## 5. Exact dev.38 PU consumption formula

All PU values are integers. Individual components round upward where noted so that a non-zero effect never becomes free by fractional rounding.

### 5.1. Fixed emitter/stability cost

Every non-empty active projection pays:

```text
Base emitter cost = 2 PU
```

This base cost is never discounted by Ghost.

### 5.2. Geometry base cost

Before Overdrive:

```text
Geometry base PU = ceil(projected pixel area / 256)
```

`256 px²` is one 16×16 Minecraft-pixel surface.

Examples before Overdrive:

```text
10×10  = 100 px²   -> 1 PU
16×16  = 256 px²   -> 1 PU
32×32  = 1024 px²  -> 4 PU
80×32  = 2560 px²  -> 10 PU
128×128 = 16384 px² -> 64 PU
```

After the base area is known:

```text
Geometry PU = ceil(Geometry base PU × geometryRatio²)
```

#### Item / Entity

The generic PU proxy uses `Scale × Scale`. Species-aware Entity clearance/render bounds remain separate; this PU proxy intentionally keeps 3D complexity predictable.

#### Banner

Banner geometry uses its 1:2 cloth aspect (`width = Scale/2`, `height = Scale`) and multiplies geometry by the number of populated projected faces.

#### Prism Image

Prism sums the actual aspect-preserved area of each populated N/E/S/W face.

#### Wide/Tall MULTI

MULTI has four equal square cells. PU geometry is charged per populated cell, plus a small source-complexity surcharge. At Scale 80:

```text
Wide MULTI = 80×20 total layout = four 20×20 cells
Tall MULTI = 20×80 total layout = four 20×20 cells
```

This is deliberately symmetric so Tall does not magnify the same source more than Wide.

#### Plane Independent Back

Front + Independent Back are two texture sources on **one physical Plane**. Mirage therefore does not double the physical geometry area; the rear asset adds source complexity instead.

### 5.3. Source-complexity cost

Current dev.38 surcharge:

| Source mode | Extra PU |
|---|---:|
| Plane Image, Front/Back/Mirrored/Readable | 0 |
| Plane Image with real Independent Front+Back | +1 |
| Wide/Tall MULTI with >1 populated source | +1 |
| Prism Image | +2 |
| Item | +2 |
| Entity | +4 |
| Banner Plane | +1 |
| Banner Prism | +2 |

These are complexity/stability charges, not geometry area charges.

### 5.4. Lift cost

Before Overdrive:

```text
Lift base PU = ceil(Lift px / 16)
```

Then:

```text
Lift PU = ceil(Lift base PU × liftRatio²)
```

### 5.5. Float cost

Only when Floating is enabled:

```text
Float base PU = ceil(Float px / 2)
Float PU = ceil(Float base PU × floatRatio²)
```

Float is intentionally more expensive per pixel than Lift because it reserves oscillating motion around the chosen position.

### 5.6. Presentation feature cost

Current fixed feature charges:

| Feature | PU |
|---|---:|
| Rotation enabled | +1 |
| Rotation-synced Floating active | +1 |
| Fullbright | +1 |
| Image scanlines | +1 |
| Tint | +0 |
| Flip | +0 |
| Back readability/mirroring without independent asset | +0 |

These numbers remain balance-tunable, but the categories are explicit in `ProjectionPower.Breakdown` and visible in the Power tooltip.

## 6. Ghost PU rebate

Ghost should have a tiny efficiency meaning without becoming a min-max exploit.

The dev.38 formula is:

```text
eligible = max(0, gross PU - 2 base emitter PU)
Ghost saving = floor(eligible × GhostPercent / 3000)
Final PU = max(1, gross PU - Ghost saving)
```

Ghost is clamped to 0–90%.

Therefore at maximum Ghost:

```text
90 / 3000 = 0.03
```

so the theoretical maximum rebate is **3% of the non-base load**, rounded down. Small projections often save 0 PU; sufficiently large projections may save a handful. The effect exists, but cannot be used to erase meaningful costs.

The emitter's base 2 PU is never rebate-eligible.

## 7. Complete total formula

Conceptually:

```text
Gross PU =
    2 emitter
  + Geometry PU after geometry Overdrive
  + Source complexity PU
  + Lift PU after Lift Overdrive
  + Float PU after Float Overdrive
  + Presentation feature PU

Ghost saving = floor((Gross PU - 2) × Ghost% / 3000)

Final load = max(1, Gross PU - Ghost saving)

Projection active iff:
    Core exists
    AND Float <= Lift when Floating is enabled
    AND Final load <= Effective PU
```

There are no current Core-specific Scale/Lift/Float hard failures and no normal chassis hard Scale/Lift/Float failure.

## 8. Dynamic slider contract

Scale, Lift and Float are not generic fixed-range controls anymore.

Each slider endpoint is generated from the **largest value that keeps the complete current configuration valid** with the installed Core/chassis.

For Scale, Mirage searches the technical safety range and holds current Lift/Float/features constant. For Lift it holds current Scale/Float/features constant. For Float it holds current Scale/Lift/features constant.

The UI therefore behaves as:

```text
[ minimum ---------------- current ---------------- effective maximum ]
```

not:

```text
[ minimum ---------------- valid ------- invalid/orange ------- arbitrary max ]
```

If changing the Core or enabling a costly feature makes an already-loaded legacy configuration invalid, the recovery order is:

1. reduce optional Float if necessary;
2. reduce Lift if still necessary;
3. reduce Scale only as the final fallback.

The objective is to preserve the user's main visual size whenever possible.

Technical search ceilings exist only as implementation safety rails (`Scale 512`, `Lift 512`, `Float 128`) and **must never be documented to players as chassis limits**.

## 9. Power GUI contract

Primary information:

```text
<Core> · base output N PU
Chassis <name> ×M · Core amp ×A
Effective capacity C PU · Load L / C PU
Size W×H · nominal NW×NH
Scale current / effective max
Lift current / effective max (nominal N)
Float current / effective max (nominal N)
```

If Overdrive is active, the status explicitly reports the largest active Overdrive ratio.

Hovering the Power section exposes an exact component breakdown so balancing can be debugged without making `Remaining PU` the main interaction model.

`Remaining PU` may still be derived as diagnostic information (`capacity - load`), but it is not the player's primary limit indicator.

## 10. Debug chassis override

Creative `Debug chassis` remains a development tool.

In dev.38 it means:

- remove chassis Overdrive penalties for stress tests;
- **do not** bypass Core presence;
- **do not** bypass Effective PU;
- **do not** bypass the physical `Float <= Lift` safety invariant.

This preserves the historical principle that Debug is not infinite free projection power.

## 11. Persistence / networking

The power rework itself adds no new packet or NBT field. Base PU, chassis multiplier and current Standard amplification are code-derived from existing Core/chassis state.

Protocol remains **17**, already required by dev.38 `ImageLayoutMode`.

Old saved Scale/Lift/Float values remain readable. When the settings GUI opens, dynamic feasibility recalculates against the new power model.

## 12. Explicitly deprecated behaviours

The following must not be reintroduced from historical docs/code:

- Glass/Quartz/Amethyst/Diamond/Netherite each owning hard Scale caps;
- Core-specific hard Lift caps;
- Core-specific hard Float caps;
- chassis nominal W×H being treated as an absolute gameplay Scale wall;
- chassis nominal Lift/Float being absolute gameplay walls;
- exposing a huge slider whose invalid region is discovered only by orange text;
- treating `Remaining PU` as the main explanation of what the player can change;
- interpreting Field as a 3×3/nine-image projector.

## 13. Balance status and future work

The architecture is frozen for dev.38 QA; the numeric curve is provisional until real in-game tests.

Future improved-Core work may add dedicated crafted/textured items using the existing amplification hook. Before doing so, define:

1. recipes and material cost;
2. visual identity/model/texture;
3. whether all five material families receive an Improved version;
4. final amplification (current design target ≈ ×1.50);
5. whether any later specialist Core grade trades general amplification for a particular geometry/effect.

Do not increase standard Core PU merely to simulate an Improved grade; that would collapse the distinction this rework intentionally creates.
