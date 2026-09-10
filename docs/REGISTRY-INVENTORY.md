# Mirage Projector — Registry inventory

This file distinguishes active gameplay IDs from compatibility-only IDs.

## Active blocks

Projectors:

- `mirage_projector:mirage_projector`
- `mirage_projector:mirage_display`
- `mirage_projector:wide_mirage_projector`
- `mirage_projector:tall_mirage_projector`
- `mirage_projector:mirage_field_projector`
- `mirage_projector:mirage_prism`

Core system:

- `mirage_projector:core_booster`

Crying Obsidian ecosystem:

- `mirage_projector:small_crying_obsidian_bud`
- `mirage_projector:medium_crying_obsidian_bud`
- `mirage_projector:large_crying_obsidian_bud`
- `mirage_projector:crying_obsidian_cluster`
- `mirage_projector:obsidian_spike`

Internal runtime-only support block:

- `mirage_projector:crying_light_node` — **transitional legacy runtime support in dev.74**. Still supplies dev.73 visible/gameplay Mature lighting while the new virtual solver runs in shadow mode; no BlockItem, recipe or Creative exposure. Planned to leave active runtime in dev.75 after safe migration/cleanup.

## Active non-block items

- `mirage_projector:crying_obsidian_shard`
- `mirage_projector:entity_scan_card`
- `mirage_projector:debug_handbook`

Projector and ecosystem blocks also expose their normal active BlockItems, except compatibility-only blocks listed below.

## Compatibility-only blocks

These serialized IDs remain registered only to protect old development worlds:

- `mirage_projector:improved_glass_core`
- `mirage_projector:improved_quartz_core`
- `mirage_projector:improved_amethyst_core`
- `mirage_projector:improved_diamond_core`
- `mirage_projector:improved_netherite_core`

Java symbols are explicitly prefixed `LEGACY_`. They use `LegacyImprovedCoreBlock` and `LegacyImprovedCoreBlockEntity`, have no BlockItems, no recipes and no Creative exposure, and migrate placed old blocks to `core_booster` with the corresponding material.

The old block-entity registry ID `mirage_projector:improved_core` remains for that migration layer only.

## Menus

- `mirage_projector:mirage_projector`
- `mirage_projector:image_projector`
- `mirage_projector:item_projector`
- `mirage_projector:entity_projector`
- `mirage_projector:banner_projector`

## Recipe serializer

- `mirage_projector:projector_upgrade`

## Custom damage type

- `mirage_projector:obsidian_spike`

## Creative exposure

The Mirage tab exposes:

- six current projectors;
- empty Core Booster plus five loaded-state variants of the same item;
- Crying Obsidian Shard;
- four crystal stages;
- Obsidian Spike;
- Entity Scan Card;
- Debug Handbook.

Vanilla tabs additionally expose relevant active objects. Legacy Improved Core IDs must never be added back to these lists.
