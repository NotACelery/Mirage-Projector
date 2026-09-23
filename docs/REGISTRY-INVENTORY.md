# Registry Inventory — Mirage Projector 1.0.117

This file distinguishes active gameplay IDs from compatibility-only IDs.

## Active projector blocks/items

- `mirage_projector:mirage_projector`
- `mirage_projector:mirage_display`
- `mirage_projector:mirage_field_projector`
- `mirage_projector:wide_mirage_projector`
- `mirage_projector:tall_mirage_projector`
- `mirage_projector:mirage_prism`
- `mirage_projector:mirage_table_projector`
- `mirage_projector:mirage_wall_projector` — **Mirage Wall Projector**, presentation/Data-show chassis.

These are the current runtime projector chassis IDs. No temporary/comparison projector IDs are registered.

## Active ecosystem blocks/items

- `mirage_projector:core_booster`
- `mirage_projector:mirage_light_projector` — placed rechargeable Focus/Flood/Ambient/Off DYNAMIC_VISUAL emitter.
- `mirage_projector:mirage_flashlight_beacon` — itemless temporary world form used when placing a Mirage Flashlight.
- `mirage_projector:mirage_lantern` — legacy registry ID retained for compatibility; public/runtime identity: **Mirage Flashlight**, rechargeable Focus/Flood/Ambient/Off player-following DYNAMIC_VISUAL emitter.
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
- `mirage_projector:entity_scanner` — main-hand, inserted-Codex scanner for sustained living-entity capture.
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
- `mirage_projector:charging_station`
- `mirage_projector:portable_device`
- `mirage_projector:mirage_light_projector`
- `mirage_projector:scan_codex`
- `mirage_projector:entity_scanner`

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
- Mirage Flashlight;
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
- Mirage Entity Scanner;
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


## 1.0.20 registry note

1.0.20 adds no gameplay registry IDs and intentionally keeps network protocol 34. The hotfix changes only the client packed-light query path over the existing 1.0.19 identities; no registry IDs are added or renamed.

## 1.0.18 registry note

1.0.18 adds no new gameplay registry IDs. The historical IDs `mirage_projector:arm_strap` and `mirage_projector:battery_pouch_expansion_patch` are intentionally retained for save compatibility while their user-facing names are **Shoulder Strap** and **Shoulder Strap Slot Expansion**. Portable-device and Light Projector GUIs add menus/payload behavior without renaming existing device IDs.


## 1.0.21 registry note

1.0.21 adds no gameplay registry IDs and keeps network protocol 34. The runtime-QA corrections are client rendering/invalidation and portable-render path changes only.


## 1.0.22 / 1.0.23 scan-copy registry note

1.0.22 temporarily introduced block/item `mirage_projector:duplicating_lectern` as a foundation snapshot. Runtime QA rejected the separate station. 1.0.23 removes that block/item/resource identity entirely and uses vanilla `minecraft:lectern` as the physical Codex host instead. No Mirage block entity or menu registry is required; position-aware Codex payloads advance the network protocol to 35.

## 1.0.25 registry note

1.0.25 adds block/item IDs `mirage_projector:mirage_table_projector` and `mirage_projector:mirage_wall_projector`. Both reuse the canonical `mirage_projector:mirage_projector` BlockEntity type/state serialization path; no second projector BlockEntity registry identity is introduced. The two new `ProjectionChassisProfile` values are appended after the historical six and advance network protocol to 37.


## 1.0.26 registry note

1.0.26 adds the item ID `mirage_projector:presentation_remote`. No new block or BlockEntity registry identity is introduced. The remote is a stack-size-one controller whose Mirage-owned custom data stores its paired projector link UUID, dimension and position. Network protocol advances to 38 because the Image Workspace now carries automatic-presentation state and two presentation-remote payloads are registered.
