# Mirage Projector dev.43 — next-chat handoff

Use this file first when continuing in a new chat.

## Read order

1. `DOCUMENTATION-AUTHORITY-dev43.md`
2. `CURRENT-STATE-ROADMAP-dev43.md`
3. `CURRENT-IMPLEMENTATION-AUDIT-dev43.md`
4. `CRYING-OBSIDIAN-ECOSYSTEM-dev43.md`
5. `CORES-AND-UPGRADES-dev43.md`
6. `DEV43-CRYSTAL-BEACON-QA.md`

## Current source

- Version: `0.1.0-dev.43`
- Minecraft 1.21.1 / NeoForge 21.1.244 / Java 21
- Network protocol 18
- dev.43 is a **source candidate**, not yet build-clean.

## What dev.43 just added

- four Crying-Obsidian crystal ages;
- Crying Obsidian + Lava-source downward renewable growth;
- slow nucleation / faster later ages;
- exact 1/2/3/4 shard drops and Silk stage recovery;
- shard loot in Ruined Portal, Mineshaft and village smith chests;
- energized age BlockState + vanilla fallback light;
- 75/50/25/0% visual Beacon transmission;
- Mature visual beam termination while Beacon gameplay remains active;
- deterministic narrow purple residual beams with extend/hold/retract lifecycle and solid collision clipping.

## Critical first action in next chat

If the user has not yet tested dev.43, request/inspect the Windows `build.bat` result first. If it builds, execute `DEV43-CRYSTAL-BEACON-QA.md` before adding dev.44 content.

Any Mixin error or Beacon rendering regression takes priority over new features.

## Next planned wave after dev.43 QA

**dev.44 — Obsidian Spike**:

- 3 Crying Obsidian Shards;
- 2 String;
- 1 Stick;
- ~half-block, 9 tips;
- 2.0 damage per hurt event;
- berry-bush-like movement annoyance.

Then dev.45 state-preserving projector upgrade recipes, dev.46 Improved Cores, dev.47 Improved-Core Beacon relay.

## Important future lighting rule

Improved-Core Beacon modifiers must resolve first. Powered crystals respond to the final effective beam:

- wider beam -> stronger visual crystal excitation;
- Radiance/intensity -> stronger useful crystal lighting;
- stained glass remains hue authority.

Optional >15 lighting provider should be evaluated before final dev.47 balance. Base Mirage must work without it; do not rewrite Minecraft's whole light engine.
