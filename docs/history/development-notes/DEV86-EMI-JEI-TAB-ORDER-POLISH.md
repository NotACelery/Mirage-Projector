# dev.86 — EMI / JEI tab and ordering polish

Version: **0.1.0-dev.86**  
Network protocol: **27**

## Goal

Finish recipe-viewer discoverability before 1.0.0 without touching gameplay systems.

## EMI

- Projector upgrade recipes now use separate synthetic EMI IDs after suppressing any automatic representation of the custom serializer. This prevents the old removal predicate from deleting the replacement recipe too, so Mirage Display / Wide / Tall / Prism / Field all get a real **Crafting** tab.
- Crying Obsidian gets two explicit **Crafting** entries:
  - 8 Crying Obsidian Shards + Fire Charge;
  - 8 Crying Obsidian Shards + Magma Cream.
- The Crying Obsidian **World Interaction** page keeps the practical one-setup layout, but all progression/mining arrows are rendered at half-size (`12×9`) so buds and cluster sprites no longer overlap them.
- Crying Obsidian crystal **Block Drops** rows are captured and replayed with deterministic age-prefixed IDs so the intended order is:
  1. Small Bud
  2. Medium Bud
  3. Large Bud
  4. Cluster

## JEI

- `ProjectorUpgradeRecipe` remains a normal crafting recipe type and keeps its JEI crafting-category extension, so the custom projector upgrades render in JEI's Crafting category.
- The two Crying Obsidian shard recipes remain ordinary shaped crafting JSON recipes, so JEI discovers them through the vanilla crafting recipe manager.

## Scope

No protocol, projector behavior, light behavior, renderer, GIF, Core, loot amount, or Fortune behavior changes were introduced.
