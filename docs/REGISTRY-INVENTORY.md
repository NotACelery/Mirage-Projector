# Registry Inventory — Mirage Projector 1.0.0

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
- `mirage_projector:small_crying_obsidian_bud`
- `mirage_projector:medium_crying_obsidian_bud`
- `mirage_projector:large_crying_obsidian_bud`
- `mirage_projector:crying_obsidian_cluster`
- `mirage_projector:obsidian_spike`
- `mirage_projector:crying_obsidian_shard`
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

- six projector chassis in canonical order;
- Core Booster plus loaded material variants;
- Crying Obsidian Shard;
- Small/Medium/Large Crying Obsidian Bud and Cluster;
- Obsidian Spike;
- Entity Scan Card;
- Mirage Handbook.

Migration-only IDs must never be reintroduced into Creative tabs or normal crafting progression.
