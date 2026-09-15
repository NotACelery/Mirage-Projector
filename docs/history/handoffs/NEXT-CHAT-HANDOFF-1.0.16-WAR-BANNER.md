# NEXT CHAT HANDOFF — Mirage Projector 1.0.16 War Banner Presentation

Date: 2026-09-14
Baseline entering wave: 1.0.15 Dedicated Charging Station
Current snapshot: 1.0.16 War Banner Presentation
Minecraft: 1.21.1
NeoForge: 21.1.244+
Java: 21
Network protocol: 32
ProjectionSettings format: 3 (unchanged)

## User contract implemented

The Mirage Hand Projector now gives Banner sources two portable presentation modes:

- `Forward Projection`: historical handheld behavior in front of the player;
- `War Banner`: a compact, pole-less holographic banner floating above the owning player.

War Banner is a presentation choice stored on the Hand Projector ItemStack, not a new projection source. Banner colors/patterns still use the existing Banner source/snapshot pipeline and the same ghost/tint/fullbright rendering infrastructure.

War Banner remains valid while the active Hand Projector is moved off the Shoulder Slot and into the normal player inventory. The 1.0.12 persistent portable-projector state/sync remains authoritative for that behavior.

## Persistent Hand Projector fields

New CustomData keys:

- `MirageHandProjectorBannerPresentation`
- `MirageHandProjectorWarBannerFacing`
- `MirageHandProjectorWarBannerSize`
- `MirageHandProjectorWarBannerHeight`

Presentation values:

- `forward`
- `war_banner`

Facing values:

- `directional`
- `billboard`

Defaults and bounds:

- default presentation: Forward Projection;
- default War Banner facing: Always Face Viewer / Billboard;
- default size: 65% of the same banner's Forward Projection scale;
- size range: 45%..80%, stepped by 5%;
- default extra height: +4 px;
- height range: 0..12 px, stepped by 2 px.

War Banner only activates when the portable source is Banner. Non-Banner sources continue using Forward Projection regardless of the stored Banner presentation preference.

## Rendering architecture

`ClientHeldProjectors` routes active portable Banner projectors into the War Banner path when `MirageHandProjectorItem.warBannerActive(stack)` is true. All other portable projectors keep the historical forward-placement renderer.

War Banner reconstructs the normalized portable projector only to recover the existing Banner snapshot/settings, then calls the public `MirageProjectorRenderer.renderPortableWarBanner(...)` helper.

That helper renders only `BannerRenderer.FLAG`; it intentionally does not render the vanilla banner pole/staff. It reuses the existing projection render buffers and `renderBannerFace(...)`, so Banner pattern/color, ghost opacity, tint and fullbright semantics stay shared with placed/forward projections.

The final War Banner scale is relative to the same banner's Forward Projection scale:

`forwardModelScale * sizePercent / 100`

This guarantees that the configured 45%..80% range remains visually smaller than Forward Projection instead of behaving like an unrelated absolute size.

## Position / orientation

The anchor is player-relative and interpolated from the tracked vanilla player transform. It uses the current player bounding-box height so standing/crouching/pose changes influence the overhead placement.

Base visual intent:

- centered over the player;
- no physical Hand Projector required to be visible;
- no banner pole near the back/shoulder;
- compact gap above the current pose;
- configurable additional height;
- multiple active War Banners from the same player receive a small vertical separation to reduce exact z-fighting/overlap.

Facing modes:

### Always Face Viewer / Billboard

Each observing client computes the horizontal direction from the banner anchor toward its own camera. Only yaw is used; pitch is ignored so the hologram remains upright. Different observers can therefore see the same War Banner front-facing at the same time without server-side rotation traffic.

### Directional

Uses interpolated player body yaw. It is intended to behave like a physical directional team standard: side/rear viewing remains meaningful rather than always presenting the front.

The exact sign/180-degree visual convention must be confirmed in-game because BannerRenderer/model front conventions are difficult to prove from static verification alone.

## Shoulder configuration UI

`ShoulderDeviceScreen` now exposes Banner-specific controls when a Mirage Hand Projector with Banner source is mounted:

- Toggle Projection;
- Presentation: Forward Projection / War Banner;
- Facing: Directional / Always Face Viewer;
- War Banner Size - / +;
- War Banner Height - / +;
- Back.

Facing/size/height controls are disabled while presentation is Forward. The settings remain attached to the Hand Projector ItemStack after removing it from the Shoulder Slot.

`ShoulderDeviceControlPayload.Action` gained:

- `CYCLE_BANNER_PRESENTATION`
- `CYCLE_WAR_BANNER_FACING`
- `WAR_BANNER_SIZE_DOWN`
- `WAR_BANNER_SIZE_UP`
- `WAR_BANNER_HEIGHT_DOWN`
- `WAR_BANNER_HEIGHT_UP`

Server-side item helpers enforce source checks and numeric bounds. After a successful action, the server immediately republishes the portable-projector state and shoulder state so remote clients do not have to wait for the periodic portable heartbeat.

## Networking / protocol

No new payload type was added. War Banner reuses the existing `PortableProjectorStatePayload`, including its persistent device UUID identity and normal player movement tracking.

Protocol changed from 31 to 32 because the serialized ordinal surface of `ShoulderDeviceControlPayload.Action` changed. Clients/servers from the previous protocol must therefore not be considered wire-compatible.

`ProjectionSettings` remains format 3.

## Documentation / roadmap

Updated:

- `README.md`
- `docs/CHANGELOG.md`
- `docs/ROADMAP.md`
- `docs/WAITLIST-1.1.0.md`
- `docs/VERSION-SCOPE.md`
- `docs/CURRENT-IMPLEMENTATION.md`
- `docs/ARCHITECTURE.md`
- `docs/DEVELOPMENT.md`
- `docs/DOCUMENTATION-AUTHORITY.md`
- `docs/REGISTRY-INVENTORY.md`

New release note:

- `docs/RELEASE-1.0.16-WAR-BANNER.md`

The 1.1 waitlist marks the War Banner foundation as delivered while retaining final placement/readability QA and polish. The UV family remains wholly reserved for 1.2.0.

## Verification

Final working-tree result after the relative-size correction:

`MIRAGE PROJECTOR 1.0.16 VERIFICATION PASS (37 gates)`

Release audit:

`Mirage Projector 1.0.16 release audit PASS (112 JSON, 20 blocks, 25 items, 577 lang keys)`

Known non-blocking historical deprecation count: 12 `EventBusSubscriber.Bus` sites.

No real Gradle/NeoForge compilation was run in this environment. The authoritative next gate remains Windows `build.bat` under Java 21 followed by in-game QA.

## High-value in-game QA

1. Compile with Java 21 / NeoForge 21.1.244+.
2. Mount a configured Banner Hand Projector, select War Banner and verify the pole/staff is absent.
3. Verify Billboard from front/side/rear using at least two separate multiplayer observers; each observer should independently see the face toward their own camera.
4. Verify Billboard stays vertical when the observer is substantially above/below the target player.
5. Verify Directional follows body direction and check specifically for a possible reversed/180-degree convention.
6. Test standing, crouching, swimming, elytra and mounted poses for vertical anchor clipping/floating.
7. Test 45%, 65% and 80% size against the same Banner in Forward Projection and confirm War Banner is always smaller.
8. Test 0, +4 and +12 px height for readability without excessive obstruction.
9. Configure War Banner while mounted, move the active Hand Projector into a normal inventory slot and verify the hologram remains active/visible remotely.
10. Deplete/remove/replace the battery while stored and confirm existing persistent-ON semantics remain correct.
11. Test multiple active Hand Projectors on one player and confirm their small vertical separation is readable and stable.
12. Confirm Image/Item/Entity portable projections are unchanged and still use the historical forward path.

## Recommended next work after QA

Do not start UV during 1.1. The strongest remaining major 1.1 candidates are:

- Scan Codex + Duplicating Lectern/data-family foundation;
- Table/Wall projector chassis families;
- final Shoulder/device UX, recipes, progression and energy balancing;
- End Resonance / Dragon Egg Core behavior.

War Banner itself should receive only QA-driven positioning/readability polish unless testing exposes a structural issue.
