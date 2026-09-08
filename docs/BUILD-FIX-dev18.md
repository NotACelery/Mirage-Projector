# Mirage Projector 0.1.0-dev.18 — Windows compile repair

## Input

The first Windows build of dev.17 reached NeoForge `:compileJava` and reported **13 errors / 6 deprecation warnings**. The errors have three roots.

## 1. `Player` import

`MirageProjectorBlockEntity` uses `Player` in the menu/staging API but had no player import. dev.18 restores the import; no behavior change.

## 2. Equipment-slot resolution

In Minecraft/NeoForge 1.21.1, `LivingEntity#getEquipmentSlotForItem(ItemStack)` is an **instance** method. It first honors NeoForge's stack-level slot override, then vanilla `Equipable`, and otherwise falls back to Main Hand.

Mirage does not have a live target entity when validating a virtual/staging row, so dev.18 introduces `EquipmentSnapshotRules` and mirrors the non-entity-specific part of that resolution statically:

1. `ItemStack#getEquipmentSlot()`;
2. `Equipable.get(stack)`;
3. Main Hand fallback.

Armor rows require an exact armor slot; Main/Off Hand staging intentionally accepts any non-empty item because Mirage lets users choose either hand visually.

## 3. Saddle is not an `EquipmentSlot`

Minecraft 1.21.1 defines `MAINHAND`, `OFFHAND`, `FEET`, `LEGS`, `CHEST`, `HEAD`, and `BODY`. There is no `SADDLE`. A horse saddle lives in the horse's own equipment inventory (`AbstractHorse.INV_SLOT_SADDLE` / equipment slot-access offset), while horse armor uses `EquipmentSlot.BODY`.

Therefore Mirage keeps **its own virtual channels**:

- `SADDLE` -> no vanilla `EquipmentSlot`; validated as `Items.SADDLE`;
- `BODY` -> `EquipmentSlot.BODY`.

Horse scan capture copies Saddle from vanilla `SaddleItem` NBT and Body Armor from `getItemBySlot(BODY)`, then strips those fields from the base frozen body so Incoming/Projected conflict handling stays authoritative.

## Client-only horse saddle visibility

Vanilla `HorseModel` decides whether saddle geometry is visible via `AbstractHorse#isSaddled()`, normally driven by a server-synced horse flag. Mirage's reconstructed horse is intentionally never spawned or server-owned, so dev.18 adds `MirageProjectionHorse`: a client-only subclass used only for scanned vanilla horses. Its `isSaddled()` derives directly from the reconstructed saddle inventory slot. This avoids reflection, fake synced-data writes and any nonexistent `EquipmentSlot.SADDLE`.

Windows/in-game QA must still verify the final saddle mesh and horse armor together.

## Status

- Target: Minecraft 1.21.1 / NeoForge 21.1.244 / Java 21.
- Protocol: 6 unchanged.
- Entity Scan data version: 3 unchanged.
- dev.18 is **not build-clean** until the real Windows `build.bat` succeeds.
