# NEXT CHAT HANDOFF — Mirage Projector 1.0.14 Shoulder Battery Pouch

Baseline: `1.0.14`
Minecraft: `1.21.1`
NeoForge: `21.1.244+`
Java: `21`
Network protocol: `31`
ProjectionSettings format: `3`

## Work order used for this snapshot

This wave deliberately started with documentation/roadmap cleanup before gameplay changes.

1. The untouched user-supplied 1.0.13 snapshot passed `verify_current_line.py` at **30/30 gates**.
2. `docs/history/audits/PRE-1.0.14-ROADMAP-AUDIT.md` froze the release boundary:
   - 1.1.0 = portable illumination/projection/capture, shoulder equipment, War Banner, Codex/duplication, remaining chassis/End Resonance and release polish;
   - 1.2.0 = direct hologram manipulation + the complete UV ecosystem.
3. `WAITLIST-1.1.0.md` and `WAITLIST-1.2.0.md` were cleaned/restructured before implementation.
4. The next implementation wave was selected from that audit: Shoulder Strap Battery Pouch + generic upgrade framework.

## Delivered in 1.0.14

### Shoulder Battery Pouch

The Mirage player attachment now uses a fixed 14-slot maximum envelope:

```text
0       Arm Strap
1       Shoulder Device
2-10    Battery Pouch (9 physical positions; 6 active by default)
11-13   Upgrade sockets (3 physical positions; 2 active by default)
```

Battery Pouch accepts only `RechargeableEnergyItem` stacks. This includes full/partial/depleted Glow Dust, Light Battery, Creative Battery and future compatible rechargeable media. It is deliberately not a general backpack.

### Generic Shoulder Upgrades

New generic contracts:

- `ShoulderUpgrade`
- `ShoulderUpgradeFamilies`
- `ShoulderUpgradePatchItem`

Upgrade families use namespaced IDs. Duplicate families cannot be installed simultaneously.

### Auto Battery Swap Patch

Registry ID: `mirage_projector:auto_battery_swap_patch`.

It affects only the device mounted in Shoulder Slot and only devices implementing `ShoulderRechargeableDevice`.

When the installed device cell is depleted, the server:

1. searches active pouch positions for a charged candidate;
2. ignores 0%-charge candidates;
3. prefers higher stored charge, then larger capacity;
4. verifies the old cell can be safely returned after taking the candidate;
5. extracts one candidate;
6. extracts the exact depleted cell from the mounted device;
7. installs the charged candidate;
8. returns the depleted cell to the pouch.

If the preflight fails, no swap occurs. The automatic path does not intentionally drop or delete cells.

### Battery Pouch Expansion Patch

Registry ID: `mirage_projector:battery_pouch_expansion_patch`.

Without Expansion:

- 6 active battery positions;
- 2 active upgrade sockets.

With Expansion:

- 9 active battery positions;
- 3 active upgrade sockets.

Expansion cannot be removed while battery positions 7-9 or upgrade socket 3 contain items.

The third socket remains generic. This was deliberate so 1.2 can later add `Auto UV` without changing the shoulder attachment shape again.

### Arm Strap dependency safety

The Strap cannot be removed while any dependent content exists:

- Shoulder Device;
- any Battery Pouch cell;
- any Shoulder Upgrade.

Death handling removes/drops contents in dependency-safe order. `keepInventory` cloning includes all 14 physical positions.

### UI / networking

The collapsible Mirage Equipment panel now exposes:

- Shoulder Device;
- Arm Strap;
- 3 upgrade positions (expansion-only third position locks when unavailable);
- 3x3 Battery Pouch grid (bottom row locks without Expansion).

`ShoulderEquipmentInventoryPayload` synchronizes pouch/upgrades only to the owner. Remote players continue receiving only the state needed for visible mounted-device behavior.

`ShoulderEquipmentActionPayload` now addresses all 9 battery and 3 upgrade positions.

Because the network action/state surface changed, protocol advanced **30 -> 31**.

### 1.0.13 -> 1.0.14 attachment migration

This is important.

1.0.13 saved Shoulder Equipment as a two-slot `ItemStackHandler`. NeoForge item-handler NBT persists its `Size`, so simply constructing a 14-slot handler in 1.0.14 is not enough: historical NBT can shrink it back to two positions while loading.

`ShoulderEquipment.deserializeNBT(HolderLookup.Provider, CompoundTag)` now copies the stored tag, forces `Size = SLOT_COUNT`, then delegates to `ItemStackHandler`. Existing historical slots 0/1 are preserved; new positions initialize empty.

Do not remove this migration normalization while 1.0.13 player saves remain supported.

## New files / major implementation surfaces

### Runtime/contracts

- `src/main/java/celerbi/mirageprojector/item/ShoulderRechargeableDevice.java`
- `src/main/java/celerbi/mirageprojector/item/ShoulderUpgrade.java`
- `src/main/java/celerbi/mirageprojector/item/ShoulderUpgradePatchItem.java`
- `src/main/java/celerbi/mirageprojector/equipment/ShoulderUpgradeFamilies.java`
- `src/main/java/celerbi/mirageprojector/network/ShoulderEquipmentInventoryPayload.java`
- `src/main/java/celerbi/mirageprojector/client/MirageEquipmentPanelWidget.java`

### Heavily changed

- `ShoulderEquipment.java`
- `ShoulderEquipmentRuntime.java`
- `ShoulderEquipmentEvents.java`
- `ShoulderEquipmentActionPayload.java`
- `ClientShoulderEquipment.java`
- `MirageEquipmentClientEvents.java`
- `MirageEquipmentSlotWidget.java`
- `MirageLanternItem.java`
- `MirageHandProjectorItem.java`
- `ModNetworking.java`
- `ModItems.java`
- `ModCreativeTabs.java`

### Resources

- `models/item/auto_battery_swap_patch.json`
- `textures/item/auto_battery_swap_patch.png`
- `models/item/battery_pouch_expansion_patch.json`
- `textures/item/battery_pouch_expansion_patch.png`
- `en_us`, `es_cl`, `es_es` language files.

### Docs/audit

- `docs/history/audits/PRE-1.0.14-ROADMAP-AUDIT.md`
- `docs/RELEASE-1.0.14-SHOULDER-BATTERY-POUCH.md`
- cleaned `WAITLIST-1.1.0.md`
- expanded/reframed `WAITLIST-1.2.0.md`
- current implementation/roadmap/version/changelog/architecture/QA documents updated to 1.0.14/protocol 31.

## Explicitly NOT implemented

- no UV runtime, UV Shoulder Light, Auto UV, UV damage or UV Marks — all are 1.2.0;
- no final recipes for Auto Battery Swap / Pouch Expansion yet;
- no final 1.1 recipes for Glow Dust/Light Battery yet;
- no War Banner renderer yet;
- no bird/cosmetic shoulder skin yet;
- no Scan Codex/Duplicating Lectern yet;
- no horizontal/table or wall/data-show projector yet;
- no End Resonance runtime yet.

## Verification

The final source tree passes:

```text
MIRAGE PROJECTOR 1.0.14 VERIFICATION PASS (33 gates)
```

Current release audit:

```text
Mirage Projector 1.0.14 release audit PASS
108 JSON
19 blocks
24 items
559 lang keys
```

The 1.0.14 pouch verifier explicitly checks the legacy 1.0.13 slot-size migration normalization.

## Windows QA priority

A real NeoForge compilation/runtime pass remains authoritative. On Windows / Java 21:

1. run `build.bat`;
2. upgrade a player/world that already has a 1.0.13 Arm Strap + mounted device and confirm both survive;
3. verify six base battery slots and two base upgrades;
4. install Expansion and verify 9 + 3;
5. attempt to remove Expansion while expansion-only positions are occupied — it must refuse;
6. insert full/partial/depleted Glow Dust, Light Battery and Creative Battery;
7. deplete a mounted Lantern and Hand Projector with Auto Swap installed and confirm exact old cell returns to pouch;
8. verify no auto swap for a device merely in normal inventory/hand;
9. test death with and without `keepInventory`;
10. test multiplayer owner-only pouch sync and remote mounted-device behavior.

## Best next implementation candidates after QA

According to the cleaned 1.1 roadmap, strong next waves are:

- War Banner overhead presentation + Directional/Billboard facing;
- Mirage Equipment final visual/layout polish after real in-game QA;
- Scan Codex foundation;
- final rechargeable-item progression/recipes;
- remaining table/wall projector chassis architecture.

Do not pull the UV family forward; it is frozen under 1.2.0.
