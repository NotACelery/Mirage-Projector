# Mirage Projector — Current Implementation Audit (dev.44)

> **Status:** source candidate. dev.44 inherits all dev.43 code and is not build-clean until Windows `build.bat` plus in-game dev.43/dev.44 QA succeed.

## Platform

- Minecraft 1.21.1
- NeoForge 21.1.244
- Java 21
- Mod version `0.1.0-dev.44`
- Network protocol 18

No packet or projector-persistence schema changed in dev.44.

## Existing baseline retained

- six active projector chassis;
- Power/Overdrive/dynamic sliders;
- Image/GIF/Wide/Tall/Field/Prism contracts;
- Item/Banner/Entity snapshot systems;
- Entity family cleanup and Piglin/Hoglin stabilization;
- Core Chamber/current physical chassis rework;
- Crying Obsidian Shard;
- four renewable Crying-Obsidian crystal stages;
- structure shard loot;
- Beacon crystal excitation/transmission/residual rendering.

## New dev.44 registry/data

- block `mirage_projector:obsidian_spike`;
- matching BlockItem;
- recipe `mirage_projector:obsidian_spike`;
- block loot table returning itself;
- pickaxe mineable tag entry;
- DamageType `mirage_projector:obsidian_spike` with message id `obsidianSpike`;
- block/item models + 16×16 Crying-Obsidian spike texture;
- translations/death messages in en_us/es_es/es_cl;
- Debug Handbook General page describing the trap.

## New block implementation

`ObsidianSpikeBlock`:

- outline shape `1..15 X/Z`, `0..8 Y` model pixels;
- `noCollission()` so a victim actually enters the trap volume;
- requires a supporting center below;
- only affects LivingEntity;
- movement multiplier `(0.8, 0.75, 0.8)`;
- checks movement on X, Y and Z with threshold `0.003`;
- requests `2.0F` damage on movement-triggered server-side contact;
- normal Minecraft hurt immunity determines which requests become successful repeated hurt events;
- non-living entities are ignored.

## Model implementation

The block model contains exactly 9 spires. Each spire has a wider lower element + narrower top element, for 18 elements total. The central spire is tallest and thinnest. Surrounding spires vary in height and use modest 22.5° X/Z lean where appropriate.

## Still future

- dev.45 state-preserving projector upgrade crafting;
- dev.46 Improved Cores;
- dev.47 Improved-Core Beacon relay;
- optional >15 light integration;
- Scan Codex 1.1.0+.
