# Mirage Projector 1.0.13 — Shoulder Equipment Foundation

1.0.13 adds Mirage-owned player equipment for portable devices without consuming vanilla armor or offhand space. The first foundation introduces the Arm Strap, Shoulder Slot, synchronized mounted-device state and a compact inventory extension.

## Mirage Equipment panel

The normal player inventory gains a small collapsible Mirage Equipment extension beside the vanilla layout. It contains two dedicated slots:

1. **Arm Strap** — stores the enabling harness.
2. **Shoulder Slot** — unlocked only while an Arm Strap is equipped.

These are Mirage-owned equipment slots. They do not replace helmet/chest/legs/boots, do not occupy the vanilla offhand and do not participate in the normal `F` swap-hands behavior.

The visual treatment uses a compact leather/brown frame so the harness reads as attached equipment while the vanilla offhand remains visually and mechanically intact.

## Arm Strap

Registry ID: `mirage_projector:arm_strap`.

The Arm Strap is a real ItemStack stored in the player attachment, not a boolean unlock flag. Removing it is rejected while a Shoulder Device remains mounted; the player must empty the Shoulder Slot first.

## Shoulder Slot

The slot accepts only items implementing `ShoulderMountableDevice`. Initial compatible devices are:

- Mirage Lantern;
- Mirage Hand Projector.

Mounted devices continue their device lifecycle outside normal inventory/hand slots. Lanterns continue mode-based drain and dynamic illumination. Active Mirage Hand Projectors continue their persistent projection drain and state publication.

Left-clicking the Mirage equipment widgets performs server-authoritative insert/extract/swap operations with the vanilla carried stack. Right-clicking an occupied Shoulder Slot opens the first compact Shoulder Device control screen: Lantern can cycle its portable light mode and Mirage Hand Projector can toggle its projection without first removing the device.

## Multiplayer and rendering

The server publishes a compact Shoulder Equipment state keyed by player identity. Clients therefore know which mount is active without inspecting another player's arbitrary normal inventory.

Mounted devices receive an initial physical right-shoulder/upper-arm item render. This transform is a QA baseline and may be visually tuned before 1.1.0. A shoulder-mounted Lantern also submits its existing `DYNAMIC_VISUAL` light profile from the player attachment point.

The existing persistent Mirage Hand Projector state publication remains responsible for the visible portable hologram itself, so moving a Hand Projector into the Shoulder Slot does not create a parallel projection renderer/network system.

## Vanilla shoulder reservation

The first Mirage mount reserves the player's **right shoulder** while a Shoulder Device is installed. Vanilla shoulder entities remain free to occupy the left shoulder. Once the Mirage device is removed, the reserved side becomes available again.

Insertion is refused if the right vanilla shoulder is already occupied, preventing the new equipment state from silently displacing an existing shoulder rider.

## Persistence and death behavior

Shoulder Equipment is persisted as player attachment data. The Arm Strap and mounted device remain real ItemStacks with their full components/configuration.

- save/relog preserves the equipment;
- `keepInventory` copies the Mirage equipment to the replacement player;
- normal death drops the mounted device first and then the Arm Strap so the strap extraction rule cannot swallow either item.

## Protocol

1.0.13 adds three new play payloads for shoulder-equipment interaction/control/state publication. Network protocol advances from **29 to 30**. `ProjectionSettings` remains format **3**.

## Deferred polish

Still intentionally open for later 1.0.x/1.1.0 work:

- final physical shoulder transform and first-person presentation;
- richer per-device mounted configuration screens;
- War Banner overhead/billboard renderer;
- dyeable bird/Parrot-like device skins;
- optional compatibility bridges to external equipment-slot mods;
- survival recipe/balance for the Arm Strap.
