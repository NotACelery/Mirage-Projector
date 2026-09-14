# Mirage Projector 1.0.3 — Renderer Compile Hotfix

## Scope

1.0.3 is a compile-only maintenance hotfix over the 1.0.2 Prism-placement pass. No intended gameplay or rendering behavior changes.

## Fixed

- Restored the `bottom` base-height local before it is consumed by multi-source image front/back detection and placement.
- Removed the duplicate `bottom` declaration from `renderImage`; the method now computes the base height exactly once and reuses it.

## Compatibility

- Network protocol remains `28`.
- `ProjectionSettings` serialization remains version `3`.
- Existing 1.0.0/1.0.1/1.0.2-compatible saves require no migration beyond the existing v3 handling.
- Prism spacing, carousel rotation, Tilt rules and PU cost are unchanged from 1.0.2.

## Required QA

Run `build.bat` on Windows/Java 21. The original 1.0.2 compilation stopped in `MirageProjectorRenderer`; 1.0.3 specifically repairs those Java compiler errors.
