# Mirage Projector dev.42 — QA checklist

## Build

- Run `build.bat` on Windows with Java 21.
- Do not call dev.42 build-clean until Gradle succeeds.

## Crying Obsidian Shard

- Appears in Mirage creative tab.
- Appears in the vanilla Ingredients tab (not Functional Blocks).
- Sprite is 16×16, no interpolation/artifact pixels.
- Stonecutter: 1 Crying Obsidian -> exactly 4 shards.
- 8 shards + Fire Charge -> 1 Crying Obsidian.
- 8 shards + Magma Cream -> 1 Crying Obsidian.
- Recipe does not duplicate catalyst/material unexpectedly.

## Six chassis

For Compact, Display, Wide, Tall, Field and Prism:

- broad old Glass sheet is gone;
- opaque structure stays solid/visually stable;
- purple emitter surfaces are visible but not ghostly/invisible;
- 4×4×4 Glass chamber is visible at the center;
- model does not z-fight with chamber/emitter geometry;
- item/inventory block model still reads as the same chassis family;
- horizontal placement facing remains correct.

## Core Chamber

Test Glass, Quartz, Amethyst, Diamond, Netherite in several chassis.

Expected:

- actual item appears, never material block substitution;
- Netherite is visibly a Netherite Ingot;
- Core rotates slowly and bobs subtly;
- Core stays visible from all camera angles;
- Core remains inside the Glass chamber;
- empty socket leaves an empty chamber;
- no duplicated Core model;
- no inventory mutation/item loss.

## Regression

- idle floating book still works on all six chassis;
- Image/GIF projects normally;
- Item projection normally;
- Banner projection normally;
- Entity projection normally;
- Power screen still reports correct core profile/output;
- Piglin remains stable in preview/world;
- projector vs water/entity render-order behavior is not worsened.

