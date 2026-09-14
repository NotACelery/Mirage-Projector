# Mirage Projector 1.0.4 — Fixed-tab UI and Placement Polish

## Scope

1.0.4 is a bounded fixed-projector maintenance patch. It does not introduce 1.1.0 portable-light, battery, Scan Codex or End Resonance gameplay.

## Placement corrections

- Lift is the only vertical placement axis. It is non-negative and remains part of the existing PU/overdrive model.
- The experimental `VerticalOffsetPixels` v3 field is retained only as a compatibility slot. Positive legacy values are absorbed into Lift during sanitization; the field then becomes zero. Negative legacy values do not create downward placement.
- Tilt remains quaternion-backed and supports the full -90° to +90° range.
- Mirage Prism Image/Banner faces keep chassis-exclusive radial spacing. The stored wire/NBT value remains an absolute radius for format-3 compatibility, while the UI presents **Prism Distance** as extra separation above the collision-safe no-tilt radius.
- `Prism Distance +0 px` therefore means the four faces are packed exactly to their collision boundary. The old extra one-pixel margin is removed.
- Tilt can raise the minimum extra Prism Distance automatically when the tilted face envelope would otherwise overlap another face.
- Prism Rotation remains a carousel around the projector center. No independent horizontal offset is introduced.
- Extra/Tilt-required Prism radial separation keeps the existing modest 1 PU per 64 px surcharge.

## Main projector UI

The former vertically stacked Geometry / Placement / Rotation / Floating / Appearance sections are replaced by five mutually exclusive settings tabs inside one fixed-size options area.

The following regions remain anchored regardless of the selected settings tab:

- Source Workspaces and projector ON/OFF controls;
- settings-tab row;
- Power / Capacity panel and Core slot;
- player inventory;
- Apply / Cancel controls.

The main panel height is reduced from 584 to 412 GUI pixels. A 1920×1080 window at GUI Scale 2 therefore fits without responsive overflow, while the existing scrollbar remains the fallback for smaller effective GUI heights such as GUI Scale 3.

## Compatibility

- Network protocol: 28.
- `ProjectionSettings` format: 3.
- Existing 1.0.0 saves remain compatible.
- 1.0.1–1.0.3 maintenance-test saves containing a positive Vertical Offset migrate that value into Lift without preserving a second vertical axis.
