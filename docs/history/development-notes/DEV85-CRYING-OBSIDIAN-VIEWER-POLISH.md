# dev.85 — Crying Obsidian viewer UX cleanup

Version: **0.1.0-dev.85**  
Minecraft: **1.21.1**  
NeoForge: **21.1.244+**  
Network protocol: **27**

## Goal

Close the remaining recipe-viewer gap without reopening renderer/light architecture:

- expose the custom projector-upgrade recipes reliably in recipe viewers;
- add a practical, icon-only EMI tutorial page for renewable Crying Obsidian growth;
- keep the Crying Obsidian, shard and projector crafting chains discoverable in both EMI and JEI;
- avoid adding body text to EMI pages beyond tooltips.

## Implemented

### 1. Optional build integration

`build.gradle` now declares optional development/runtime viewer dependencies:

- EMI (`dev.emi:emi-neoforge`)
- JEI (`mezz.jei:jei-1.21.1-neoforge[-api]`)

These are viewer-only integrations and do not change gameplay or protocol.

### 2. EMI projector-upgrade coverage

`MirageEmiPlugin` now registers explicit EMI wrappers for the custom `ProjectorUpgradeRecipe` class so the following recipes are always visible in EMI:

- Mirage Projector → Mirage Display
- Mirage Display → Wide Mirage Projector
- Mirage Display → Tall Mirage Projector
- Mirage Display → Mirage Prism
- Mirage Display → Mirage Field Projector

The wrappers use the vanilla crafting category and present a fixed 3×3 grid plus output slot.

### 3. EMI Crying Obsidian tutorial page

A dedicated world-interaction EMI entry now exists for Crying Obsidian growth.

Behavior of the page:

- no body text on the page itself;
- four side-by-side growth examples;
- each example shows lava above Crying Obsidian;
- the lower block/item changes through small bud → medium bud → large bud → cluster;
- shard harvesting is represented visually with a pickaxe icon and shard output;
- tooltips provide the practical explanation when hovered.

The entry is linked from:

- Crying Obsidian
- Small Crying Obsidian Bud
- Medium Crying Obsidian Bud
- Large Crying Obsidian Bud
- Crying Obsidian Cluster
- Crying Obsidian Shard

### 4. JEI coverage

`MirageJeiPlugin` adds:

- a crafting-category extension for `ProjectorUpgradeRecipe`, so JEI shows the custom 3×3 upgrade recipes under the normal crafting view;
- ingredient info pages for the renewable Crying Obsidian mechanic and the projector upgrade line.

## Files added

- `src/main/java/celerbi/mirageprojector/compat/viewer/ViewerCompatData.java`
- `src/main/java/celerbi/mirageprojector/compat/emi/MirageEmiPlugin.java`
- `src/main/java/celerbi/mirageprojector/compat/emi/ProjectorUpgradeEmiRecipe.java`
- `src/main/java/celerbi/mirageprojector/compat/emi/CryingObsidianGrowthEmiRecipe.java`
- `src/main/java/celerbi/mirageprojector/compat/jei/MirageJeiPlugin.java`
- `src/main/java/celerbi/mirageprojector/compat/jei/ProjectorUpgradeCraftingExtension.java`
- `tools/verify_dev83_recipe_viewers.py`
- `docs/NEXT-CHAT-HANDOFF-dev83.md`

## QA intent

1. Launch with EMI only:
   - open recipe/usage on all projector chassis and confirm upgrade path visibility;
   - open recipe/usage on Crying Obsidian, every bud, the cluster and the shard; confirm the new icon-only tutorial appears.
2. Launch with JEI only:
   - confirm the same projector upgrade recipes appear under crafting;
   - confirm Crying Obsidian/projector ingredient info is present.
3. Confirm vanilla crafting/stonecutting recipes for Crying Obsidian and shards remain visible.
4. Confirm gameplay/light/render behavior is unchanged from dev.82.


## dev.85 UX refinement

The original dev.83 EMI page explained Crying Obsidian growth as four separate towers, which made the mechanic look like four different setups. dev.85 replaces that with one practical setup: lava above Crying Obsidian, followed by a single growth row underneath (small → medium → large → cluster).

All related items are deliberately registered as outputs on the same EMI world-interaction entry so that recipe lookup from Crying Obsidian, every bud, the cluster and the shard opens the same tutorial page. JEI text was also rewritten in plain player-facing steps with no technical jargon.


## dev.85 polish

The EMI world-interaction page was refined again so the left side focuses on one practical growth setup and the right side shows two mining outcomes: Silk Touch returns the final cluster, while a normal pickaxe gives shards. The JEI explanatory text now also states explicitly that Fortune does not increase shard drops.
