# Mirage Projector 1.0.2 — Prism Placement Correction

Minecraft: **1.21.1**
NeoForge: **21.1.244+**
Network protocol: **28**
ProjectionSettings format: **3**

## Scope

1.0.2 corrects the first advanced-placement pass so the controls match the intended projector identities instead of exposing generic free translation.

## Shared placement

All fixed projectors retain signed **Vertical Offset** and quaternion-backed **Tilt**. Tilt supports **-90° through +90°**. Horizontal Offset is not a supported projector control.

## Mirage Prism radial Distance

Distance is exclusive to Mirage Prism while projecting Images/GIFs or Banners. It is the radial distance from the Prism center to each of the four lateral projection faces, so increasing it enlarges the projection cross without changing image/banner scale.

The minimum is automatic and collision-safe:

- use the largest active face footprint as the shared side envelope;
- keep at least a one-pixel separation margin between adjacent face spaces;
- account for the radial reach introduced by Tilt;
- when Tilt or Scale needs more room, raise the effective minimum Distance automatically.

The renderer still rotates the complete Prism cross around the central projector. Individual faces do not rotate around their own displaced centers.

## Power

Base collision-safe Prism spacing is part of the chassis and is free. Radial separation beyond the no-Tilt collision baseline costs a small amount of Projection Power at a deliberately gentle rate. Tilt-driven extra spacing participates in the same cost.

## Compatibility

The network format does not change from 1.0.1: format 3 / protocol 28 remain valid. The horizontal-offset integer already present in that format is retained as a compatibility slot but sanitizes to zero and has no renderer/UI effect. 1.0.0 worlds remain readable through the existing format migration path.
