# Mirage Projector 1.0.19 — Runtime QA Follow-up

1.0.19 is the direct correction wave for the first in-game QA of 1.0.18. It intentionally adds no new gameplay family and keeps network protocol **34** plus `ProjectionSettings` format **3**.

## Corrected interactions

- Mirage Lantern: normal RMB cycles `Off -> Focus -> Flood -> Ambient -> Off`; Shift+RMB opens its real configuration/battery GUI.
- Placed Mirage Light Projector: normal RMB cycles mode; Shift+RMB opens its real configuration/battery GUI.
- Mirage Hand Projector remains normal RMB ON/OFF and Shift+RMB GUI.
- Battery insertion/extraction remains GUI-only for all three device families.

## Dynamic visual light

1.0.18 runtime QA proved the virtual field itself was present: Simple Light Level/debug queries showed correct Focus/Flood/Ambient values even while terrain remained visually dark. 1.0.19 adds `LevelRendererMirageLightMixin`, merging `MirageLightEngine.virtualBlockLight(...)` into both 1.21.1 packed-light `LevelRenderer.getLightColor(...)` paths while preserving vanilla sky light. Existing section invalidation forces affected terrain to rebuild.

This is the source-side fix for the ghost-light symptom and still requires authoritative in-game confirmation after a Java 21 NeoForge build.

## GUI / inventory QA

- Portable Device, Mirage Light Projector and Charging Station screens explicitly render item hover tooltips.
- Lantern uses a compact screen/player-inventory layout instead of inheriting the tall Hand Projector screen.
- Mirage Equipment now initializes in Creative inventory as well as Survival inventory.
- Scan Codex remains non-pausing and explicitly overrides vanilla `renderBackground(...)` as a no-op so `Screen.render()` cannot reintroduce blur/dimming.

## Charging Station QA

- Queue/input items render on the rear/input side of the glass chamber; completed output stacks render nearest the front/output face.
- Jade now displays the active charging item name and live charge percentage.

## Scope retained

The existing floor-standing Mirage Light Projector is retained. Later 1.1.0 work may make its physical head visibly yaw/pitch aimable. A separate wall-mounted light projector is still pending, and the handheld Mirage Lantern still has a later ground-placement goal similar to a vanilla lantern/portable beacon.

## Verification

The static release line contains **44 current-line gates**, including dedicated `verify_1_0_19_qa_followup.py` and `verify_release_1_0_19.py` checks on top of the historical regression chain. The release audit sees **113 JSON resources, 20 blocks, 26 items and 578 translation keys per locale**. Static verification cannot replace the Windows/Java 21 NeoForge compile and runtime QA, especially for the final visual-light bridge.
