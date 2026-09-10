# dev.55 — Core Booster cleanup + ghost-core sync fix

## User QA regressions fixed

### Legacy Improved Core variants still visible

The five dev.46 Improved Core BlockItems are no longer registered. This removes them from Creative/Search and prevents them from being obtained/placed as normal independent items.

The old **block IDs** remain registered only as compatibility shims for existing QA worlds. Their BlockEntity schedules a one-tick migration on load, replacing the legacy block with the single `core_booster` block while preserving the corresponding Glass / Quartz / Amethyst / Diamond / Netherite material.

### Extracted core remained rendered as a ghost item

The Core Booster material is now mirrored into an authoritative `material` BlockState property (`empty`, `glass`, `quartz`, `amethyst`, `diamond`, `netherite`). The BlockEntity renderer reads that state every frame.

Extraction therefore changes the actual blockstate to `material=empty`, which Minecraft synchronizes through its normal block update path. There is no stale client-only material field left for the renderer to keep drawing.

For dev.54 world compatibility, old BlockEntity NBT is migrated into the new BlockState on load.

## QA

1. Creative Search should show only `Core Booster`, not the five old Improved Core items.
2. Entering a world containing old Improved Core blocks should convert them into Core Boosters with the correct material.
3. Shift-right-click extraction should immediately remove the center hologram and leave the Booster visibly empty.
4. Reinsert a different material and confirm the new item appears immediately.
5. Save/reload loaded and empty Boosters and confirm their state is correct without neighboring block updates.
