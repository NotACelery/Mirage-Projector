# Registry Inventory — Mirage Projector 1.0.18

This file distinguishes active gameplay IDs from compatibility-only IDs.

## Active projector blocks/items

- `mirage_projector:mirage_projector`
- `mirage_projector:mirage_display`
- `mirage_projector:mirage_field_projector`
- `mirage_projector:wide_mirage_projector`
- `mirage_projector:tall_mirage_projector`
- `mirage_projector:mirage_prism`

These are the only runtime projector chassis IDs. No temporary/comparison projector IDs are registered.

## Active ecosystem blocks/items

- `mirage_projector:core_booster`
- `mirage_projector:mirage_light_projector` — placed rechargeable Focus/Flood/Ambient/Off DYNAMIC_VISUAL emitter.
- `mirage_projector:mirage_lantern` — handheld rechargeable Focus/Flood/Ambient/Off player-following DYNAMIC_VISUAL emitter.
- `mirage_projector:arm_strap` — dedicated Mirage Equipment harness; unlocks the Shoulder Slot without consuming armor/offhand space.
- `mirage_projector:auto_battery_swap_patch` — Shoulder Strap utility upgrade; automatically exchanges a depleted mounted-device cell with a charged pouch cell.
- `mirage_projector:battery_pouch_expansion_patch` — Shoulder Strap capacity upgrade; expands 6→9 power-cell slots and unlocks upgrade socket 3.
- `mirage_projector:mirage_hand_projector` — handheld rechargeable portable hologram projector carrying one copied compact projection profile plus one removable cell.
- `mirage_projector:small_crying_obsidian_bud`
- `mirage_projector:medium_crying_obsidian_bud`
- `mirage_projector:large_crying_obsidian_bud`
- `mirage_projector:crying_obsidian_cluster`
- `mirage_projector:obsidian_spike`
- `mirage_projector:crying_obsidian_shard`
- `mirage_projector:glow_dust` — 1000-unit rechargeable portable-energy cell; charge is ItemStack state, not a second registry item.
- `mirage_projector:light_battery` — 4000-unit high-capacity rechargeable cell using the same item-owned charge contract.
- `mirage_projector:creative_battery` — infinite Creative/debug rechargeable medium; no Survival recipe/loot path.
- `mirage_projector:scan_codex` — physical UUID key to the persistent server-side scan library; full snapshot data is not stored on the ItemStack.
- `mirage_projector:entity_scan_card`
- `mirage_projector:debug_handbook` — public display name: **Mirage Handbook**; registry ID retained for compatibility.

Projector/ecosystem blocks expose normal BlockItems except the compatibility-only blocks below.

## Migration-only blocks

These IDs remain registered only to protect existing worlds:

- `mirage_projector:crying_light_node`
- `mirage_projector:improved_glass_core`
- `mirage_projector:improved_quartz_core`
- `mirage_projector:improved_amethyst_core`
- `mirage_projector:improved_diamond_core`
- `mirage_projector:improved_netherite_core`

The historical Improved Core blocks have no BlockItems, recipes or Creative exposure and migrate to `core_booster` with their material state. `crying_light_node` is never created by current gameplay and removes legacy physical relay data during migration.

The legacy block-entity registry ID `mirage_projector:improved_core` remains migration-only.

## Data attachments

- `mirage_projector:shoulder_equipment` — serialized player attachment that stores only the currently equipped Shoulder Strap; the Strap ItemStack owns its packed Shoulder Device, Battery Pouch and upgrades.

## Menus

- `mirage_projector:mirage_projector`
- `mirage_projector:image_projector`
- `mirage_projector:item_projector`
- `mirage_projector:entity_projector`
- `mirage_projector:banner_projector`

## Recipe serializer

- `mirage_projector:projector_upgrade`

## Damage type

- `mirage_projector:obsidian_spike`

## Projection source IDs

Built-in stable source IDs:

- `mirage_projector:image`
- `mirage_projector:item`
- `mirage_projector:entity`
- `mirage_projector:banner`

These are serialized as namespaced IDs rather than enum ordinals.

## Creative exposure

The Mirage creative tab exposes:

- six fixed hologram projector chassis in canonical order;
- Mirage Light Projector;
- Mirage Lantern;
- Mirage Hand Projector;
- Shoulder Strap;
- Auto Battery Swap Patch;
- Shoulder Strap Slot Expansion;
- Creative Battery (Creative/debug only);
- Core Booster plus loaded material variants;
- Crying Obsidian Shard;
- charged and depleted Glow Dust and Light Battery QA variants;
- Small/Medium/Large Crying Obsidian Bud and Cluster;
- Obsidian Spike;
- Mirage Scan Codex;
- Entity Scan Card;
- Mirage Handbook.

Migration-only IDs must never be reintroduced into Creative tabs or normal crafting progression.


## 1.0.15 additions

- block/item `mirage_projector:charging_station` — horizontal Beacon-powered batch charger with 4 queue + 1 active + 4 output inventory.
- block entity `mirage_projector:charging_station`.
- menu `mirage_projector:charging_station`.

## 1.0.16 registry note

War Banner adds no registry IDs. It is additional state/render behavior of the existing `mirage_projector:mirage_hand_projector` and reuses existing Banner source content.

## 1.0.17 registry note

- item `mirage_projector:scan_codex`;
- no new block/menu registry is required because the first Codex browser is a server-fed client screen rather than a container;
- world-global full scan libraries persist through Overworld SavedData file `mirage_projector_scan_codices.dat`;
- `OpenScanCodexPayload` and `ScanCodexActionPayload` are protocol-33 play payloads.


## 1.0.18 registry note

1.0.18 adds no new gameplay registry IDs. The historical IDs `mirage_projector:arm_strap` and `mirage_projector:battery_pouch_expansion_patch` are intentionally retained for save compatibility while their user-facing names are **Shoulder Strap** and **Shoulder Strap Slot Expansion**. Portable-device and Light Projector GUIs add menus/payload behavior without renaming existing device IDs.
