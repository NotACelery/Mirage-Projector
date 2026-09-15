# Next-chat handoff — Mirage Projector 1.0.19 QA Follow-up

## Baseline

- Mod version: **1.0.19**
- Minecraft: **1.21.1**
- NeoForge: **21.1.244+**
- Java: **21**
- Network protocol: **34** (unchanged from 1.0.18)
- `ProjectionSettings` serialization: **3**

## Runtime QA corrections in this wave

- Lantern normal RMB cycles mode; Shift+RMB opens GUI.
- Placed Light Projector normal RMB cycles mode; Shift+RMB opens GUI.
- Hand Projector remains normal RMB ON/OFF; Shift+RMB opens GUI.
- Portable/Light Projector/Charging Station screens explicitly render hover tooltips.
- Lantern uses compact 184px screen with player inventory at Y=100; Hand Projector remains expanded with inventory at Y=190.
- Mirage Equipment supports `CreativeModeInventoryScreen`.
- Charging Station world renderer uses +Z rear/input lane and -Z front/output lane after facing transform.
- Jade reports active Charging Station cell + live percent.
- Scan Codex `renderBackground(...)` is a no-op and `isPauseScreen()` remains false.
- `LevelRendererMirageLightMixin` merges virtual Mirage block light into packed terrain light for both 1.21.1 `getLightColor` overloads.

## QA that remains authoritative

The visual-light bridge is source/API-verified but has not been confirmed in-game in this environment. Build with Java 21 on Windows and verify that Focus/Flood/Ambient visibly illuminate terrain rather than only appearing in Simple Light Level overlays. Also verify Creative Mirage Equipment interaction, custom GUI tooltips, Codex sharp background, Charging Station lane direction/Jade progress and Lantern/Light Projector gestures.

## Later 1.1.0 scope clarified

- Keep the existing floor Mirage Light Projector.
- Later add visible yaw/pitch aiming to its lamp head.
- Wall-mounted light projector remains a separate pending device.
- Handheld Mirage Lantern still has a future temporary ground-placement goal.

## Static verification

- Current-line suite: **44/44 PASS** before packaging.
- Release audit: **113 JSON / 20 blocks / 26 items / 578 lang keys**.
- Explicit deprecated `EventBusSubscriber.Bus` selectors: **0**.
- No generated build/cache/class/jar residue is permitted in the source snapshot.
