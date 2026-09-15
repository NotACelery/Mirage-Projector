# Mirage Projector 1.0.14 — Shoulder Battery Pouch / Upgrade Foundation

1.0.14 extends the 1.0.13 Arm Strap from a two-slot mount into the battery-management foundation required by long-running shoulder devices.

## Battery Pouch

The serialized player attachment reserves nine pouch positions but exposes only six by default. Every active pouch slot accepts only `RechargeableEnergyItem` stacks, including Glow Dust, Light Battery, Creative Battery and future compatible media. Partial/depleted components are preserved.

The pouch is purpose-built energy storage, not a general backpack.

## Upgrade sockets

The base Arm Strap exposes two generic upgrade sockets. Upgrade identity is namespaced by family so duplicate upgrades from the same family are rejected independently of registry item identity.

### Auto Battery Swap Patch

When installed, this patch only affects the device mounted in Shoulder Slot. At the normal server device cadence, if that device exposes `ShoulderRechargeableDevice` and its installed cell is depleted:

1. choose a charged pouch candidate;
2. verify that removing one candidate creates/retains enough pouch capacity to safely store the depleted cell;
3. extract the candidate and old cell;
4. install the charged candidate;
5. return the depleted cell to the pouch.

A failed safety check performs no swap. Zero-charge candidates are ignored. The runtime never intentionally deletes/drops a battery during automatic replacement.

### Battery Pouch Expansion Patch

Expansion changes active capacity from **6 to 9** pouch slots and from **2 to 3** upgrade sockets. Removing Expansion is blocked while battery slots 7–9 or upgrade socket 3 contain items.

The third generic socket is intentionally reserved as architecture for later upgrade families. No UV-specific behavior is implemented in 1.0.14; UV remains 1.2.0 scope.

## Arm Strap removal

The strap can only be removed when Shoulder Device, all pouch slots and all upgrade sockets are empty. Normal death/keepInventory handling covers the enlarged attachment inventory.

## 1.0.13 save migration

1.0.13 persisted the Shoulder Equipment attachment as a two-slot `ItemStackHandler` (Arm Strap + Shoulder Device). NeoForge's item-handler serialization persists its slot count, so loading that data unchanged after the 1.0.14 expansion could otherwise recreate a two-slot handler.

`ShoulderEquipment.deserializeNBT` now normalizes the persisted `Size` to the fixed 14-slot 1.0.14 envelope before delegating item decoding. Existing slot 0/1 contents are preserved, while the new pouch/upgrade positions initialize empty. This makes direct 1.0.13 -> 1.0.14 player-save migration explicit instead of depending on constructor size.

## UI / sync

The collapsible Mirage Equipment panel now displays Shoulder Device, Arm Strap, upgrade sockets and a 3×3 pouch grid. Expansion-only positions remain visible but locked until Expansion is installed.

Remote players still receive only the shoulder state needed for visible mounted-device behavior. The pouch/upgrade inventory is synchronized through a separate owner-only payload.

## Versioning

- Mod version: `1.0.14`
- Network protocol: **31**
- `ProjectionSettings`: format **3**

No Survival recipes for the two patches are frozen in this snapshot.
