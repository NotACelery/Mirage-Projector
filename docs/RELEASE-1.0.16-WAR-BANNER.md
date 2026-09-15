# Mirage Projector 1.0.16 — War Banner Presentation

Minecraft: **1.21.1**
NeoForge: **21.1.244+**
Java: **21**
Network protocol: **32**
ProjectionSettings format: **3**

## Scope

1.0.16 closes the first War Banner implementation pass for portable Banner profiles. It reuses the Hand Projector's existing profile, battery, device UUID and multiplayer publication rather than creating a separate team-marker entity or packet.

A Banner-profile Hand Projector stores `Forward Projection` or `War Banner`. Forward preserves the existing moving projector placement. War Banner renders only the holographic banner cloth above the owner, without a pole and without requiring the projector to stay selected or mounted. An active projector stored in normal inventory therefore continues to publish and render its War Banner.

War Banner supports `Directional` facing tied to interpolated body yaw and `Always Face Viewer`, a per-client horizontal billboard that faces the observer while remaining vertically upright. Billboard is the default. Size defaults to 65% of the same Banner's Forward Projection scale and is bounded to 45–80%; extra height defaults to +4 px and is bounded to 0–12 px. Multiple active War Banners owned by one player receive small vertical separation to avoid exact overlap.

The Shoulder Device screen exposes presentation, facing, size and height when the mounted Hand Projector currently carries a Banner source. Those settings remain on the ItemStack after removing it from the Shoulder Slot.

## Network compatibility

`PortableProjectorStatePayload` is unchanged and already carries Hand Projector custom data. Protocol advances **31 → 32** because `ShoulderDeviceControlPayload.Action` gains additional wire ordinals for War Banner controls.

## QA focus

- standing/crouching/swimming/mounted/elytra overhead positioning;
- per-viewer Billboard orientation without pitch;
- smooth Directional body-yaw tracking;
- prompt remote updates when changing presentation/facing/size/height;
- server-side 45–80% size and 0–12 px height bounds;
- continued visibility after moving an active projector from Shoulder Slot to normal inventory;
- Banner patterns/colors and Ghost opacity preserved;
- final gap/scale tuning after in-game readability testing.

No real Gradle/NeoForge compilation is claimed by this source snapshot; Windows `build.bat` under Java 21 remains the authoritative compile gate.
