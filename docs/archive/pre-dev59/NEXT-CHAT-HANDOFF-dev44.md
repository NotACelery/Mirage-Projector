# Mirage Projector dev.44 — next-chat handoff

## Read order

1. `DOCUMENTATION-AUTHORITY-dev44.md`
2. `CURRENT-STATE-ROADMAP-dev44.md`
3. `CURRENT-IMPLEMENTATION-AUDIT-dev44.md`
4. `OBSIDIAN-SPIKE-dev44.md`
5. `CRYING-OBSIDIAN-ECOSYSTEM-dev44.md`
6. `CORES-AND-UPGRADES-dev44.md`
7. `DEV44-OBSIDIAN-SPIKE-QA.md`

## Current source

- `0.1.0-dev.44`
- Minecraft 1.21.1 / NeoForge 21.1.244 / Java 21
- protocol 18
- source candidate, not build-clean

## What dev.44 adds

Obsidian Spike:

- 3 Crying Obsidian Shards + 2 String + 1 Stick;
- ~half-block visual trap;
- exactly nine modeled spires;
- central spire thinner/taller;
- berry-bush-like slowdown;
- 2.0 damage per successful movement-triggered hurt event;
- vertical movement also counts;
- custom Mirage DamageType;
- non-living entities ignored.

## Critical next action

If dev.43/dev.44 have not yet been compiled on Windows, inspect `build.bat` first. Do not stack dev.45 persistence/crafting changes on top of a known compiler/Mixin failure.

If build + basic dev.43/dev.44 QA pass, begin **dev.45 state-preserving projector upgrade crafting**. The upgrade infrastructure must preserve the projector's canonical persistent state before recipes are enabled.
