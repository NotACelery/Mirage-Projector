# Mirage Projector 1.0.31 — Table / Canonical Workspace Runtime Rebuild

1.0.31 is a runtime architecture correction over 1.0.30. It advances the custom payload protocol to **42** because fixed-projector menu opening data and workspace routing changed. `ProjectionSettings` remains format **4**.

The completion pass also finalizes the recovered Player/Table/portable changes: Player scans are skin/profile-only with Base Skin / All Layers; Table gains X/Y/Z offsets, Tilt, Rotation-OFF defaults and center-pivot image transforms; Hand Projector stores a physical Projection Core separately from its battery, requires Core + energy + source, caps Compact scale at 10 px, removes Copy Target Projector and supports direct image import through dedicated Scale/Image payloads.

## Why the Table Projector looked inert

Table was already registered on the same BlockEntity, menu and main screen as Mirage Projector and Mirage Display. The failure was not a missing Table-only GUI. Source selection and workspace navigation were split into separate client→server actions while the opened screen also consulted client BlockEntity state. In runtime QA this allowed the workspace to open before the source update reached the client, so `Use Entity mode` and equivalent controls could appear to do nothing.

1.0.31 keeps Table on the canonical fixed-projector path and removes that timing dependency instead of introducing a second implementation.

## Atomic workspace routing

Opening Image, Item, Entity or Banner now performs one server-authoritative operation: validate the projector, activate the requested source, then open the matching workspace. The main screen no longer sends a separate `SetProjectionSourcePayload` immediately before navigation.

The main projector menu and all four source-workspace menus serialize authoritative `ProjectionSettings` and projection-enabled state into their opening buffers. Their screens use that snapshot for initial source/ON-state presentation rather than requiring a synchronized client BlockEntity to be current at construction time.

## Main projector navigation

Table, Mirage Display and the original fixed chassis continue to use `MirageProjectorMenu` and `MirageProjectorScreen`. Table-specific behavior remains its horizontal physical anchor, source rendering orientation and capability profile.

Main **Cancel** now restores the configuration captured when the screen opened, sends that baseline back to the server and exits the interface. Source-workspace/header spacing is increased so Wall/Data-show header controls do not collide with `SOURCE WORKSPACES` or `Active source`.

## Scan Codex regression correction

The Codex continues to suppress vanilla `renderBackground(...)`, preserving the live-world/no-blur behavior. Its parchment/book canvas is now drawn explicitly exactly once from `render(...)` before `super.render(...)` draws widgets. `renderBg(...)` no longer draws the canvas. This avoids both the runtime transparent-book regression and the older double-render path.

## Compatibility / validation

- Minecraft 1.21.1 / NeoForge 21.1.244+ / Java 21.
- Network protocol **42**.
- `ProjectionSettings` format **4**.
- No registry IDs are renamed.
- Table retains the canonical fixed-projector BlockEntity/menu/screen architecture.
- Windows `build.bat` and in-game QA remain the authoritative compile/runtime gates.
