# NEXT CHAT HANDOFF — Mirage Projector 1.0.13 Shoulder Equipment Foundation

Baseline: `1.0.13`
Minecraft: `1.21.1`
NeoForge: `21.1.244+`
Java: `21`
Network protocol: `30`
ProjectionSettings format: `3`

## Delivered

- Mirage-owned serializable player attachment with two ItemStack slots: Arm Strap + Shoulder Device.
- New `mirage_projector:arm_strap` item.
- Collapsible Mirage Equipment extension on the vanilla inventory screen.
- Shoulder Slot only unlocks with Arm Strap and only accepts `ShoulderMountableDevice` items.
- Initial mountable devices: Mirage Lantern and Mirage Hand Projector.
- Server-authoritative slot manipulation using the normal carried/cursor ItemStack.
- Arm Strap cannot be removed while a device is mounted.
- Mounted devices receive server ticks outside normal player inventory/hand slots.
- Mounted Lantern keeps portable-light drain and `DYNAMIC_VISUAL` output.
- Mounted Hand Projector keeps persistent projection drain/state sync.
- Compact Shoulder Device screen: cycle Lantern mode / toggle Hand Projector projection.
- Physical right-shoulder/upper-arm QA render for mounted device.
- Right vanilla shoulder reservation while Mirage device is mounted; left vanilla shoulder remains available.
- save/relog plus death/keepInventory handling for the real attachment ItemStacks.
- Shoulder equipment state sync for remote clients.

## Main new source files

- `item/ArmStrapItem.java`
- `item/ShoulderMountableDevice.java`
- `equipment/ShoulderEquipment.java`
- `equipment/ShoulderEquipmentRuntime.java`
- `registry/ModAttachments.java`
- `event/ShoulderEquipmentEvents.java`
- `client/ClientShoulderEquipment.java`
- `client/MirageEquipmentClientEvents.java`
- `client/MirageEquipmentSlotWidget.java`
- `client/ShoulderDeviceScreen.java`
- `network/ShoulderEquipmentActionPayload.java`
- `network/ShoulderEquipmentStatePayload.java`
- `network/ShoulderDeviceControlPayload.java`
- `mixin/PlayerShoulderReservationMixin.java`

## Design contracts frozen in WAITLIST-1.1.0

- Mirage equipment never competes with chest armor/offhand.
- Dedicated Arm Strap slot unlocks dedicated Shoulder Slot.
- Offhand and F swap remain vanilla.
- Shoulder Slot uses an extensible mountable-device contract rather than hard-coded slot rules.
- device must be removed before strap.
- future bird/Parrot disguise is a cosmetic device skin, not a replacement projector registry item.
- initial Mirage mount reserves the right shoulder.
- future Curios/Accessories-style integration may be optional, never required by the base feature.

## QA priorities

1. Windows Java 21 `build.bat` compile.
2. Inventory screen at GUI scales 1–4: panel/toggle must not overlap Recipe Book or vanilla offhand badly.
3. Equip Arm Strap with cursor, then mount/unmount Lantern and Hand Projector.
4. Verify strap cannot be removed with occupied Shoulder Slot.
5. RMB Shoulder Slot opens compact device control screen.
6. Lantern mounted: Focus/Flood/Ambient/Off drain and light should continue with both hands free.
7. Hand Projector mounted: active projection should remain synchronized/visible and battery should drain.
8. Remote-player physical shoulder item positioning.
9. Vanilla Parrot: one may occupy left shoulder while Mirage reserves right; insertion must reject preoccupied right shoulder.
10. death with and without keepInventory; no duplicated/lost strap/device.
11. relog, dimension change and server restart persistence.

## Known polish target

The physical shoulder item transform is intentionally a first QA pass. Do not treat exact X/Y/Z/rotation/scale as final art direction until tested against Steve/Alex armor and movement animations.
