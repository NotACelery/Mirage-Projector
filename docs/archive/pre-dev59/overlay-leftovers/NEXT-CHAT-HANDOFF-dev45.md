# Mirage Projector — next-chat handoff after dev.45

## Baseline

Current source candidate: **0.1.0-dev.45**. Protocol **18**.

dev.45 inherits dev.43 Crying Obsidian crystal/Beacon work and dev.44 Obsidian Spike, then adds canonical stateful projector ItemStacks and the complete one-way chassis upgrade crafting progression.

Do not mark dev.45 build-clean until the user's Windows `build.bat` succeeds.

## Critical new architecture

`ProjectorStateTransfer` is now the only intended bridge for projector upgrades. Never hand-copy individual persistent fields in future recipes.

Ordinary Survival mining packs the BlockEntity into the dropped projector item using vanilla `BlockEntity.saveToItem`. Normal placement restores it through `minecraft:block_entity_data`.

Crafting changes chassis via `ItemStack.transmuteCopy`, loads the complete source payload into a temporary target BlockEntity to run target migration/sanitization, then writes the normalized payload to the result.

This means future persistent fields automatically travel as long as they remain part of BlockEntity persistence.

## Implemented progression

```text
Mirage Projector
      ↓
Mirage Display
   ├─ Wide
   ├─ Tall
   ├─ Prism
   └─ Field
```

Base Mirage recipe = 5 Crying Obsidian Shards + 1 Glass + 3 Obsidian.

Display recipe = 2 Quartz + 2 Amethyst Shards + 4 Crying Obsidian Shards + Mirage.

Wide/Tall = equal cost: 6 shards + 2 Glass + Display, patterns horizontal/vertical.

Prism = 4 shards + 4 Glass + Display.

Field = 4 whole Crying Obsidian + 4 shards + Display; exact cost remains balance-provisional.

## Hard preservation rules

Upgrades must preserve:

- Core;
- SourceMode;
- PNG/JPEG/WebP/BMP/GIF asset IDs and dimensions;
- image layout/Prism face state/source bank;
- Entity Scan card/snapshot;
- frozen Player/entity appearance;
- Humanoid/Horse virtual equipment;
- Item/Banner snapshots;
- Scale/Lift/Float;
- rotation/floating/lighting/Ghost/Tint/Scanlines/Flip/BackFace;
- future persistent BlockEntity fields.

Do not automatically enlarge existing projections after upgrade.

## Next wave

**dev.46 = five Improved Cores.**

Planned materials: Glass, Quartz, Amethyst, Diamond, Netherite. Same Base PU as standard material, initial amplification target around x1.50. Physical Improved Core is also a decorative block: three nested dark-purple translucent shells, with the middle shell actually rotated geometrically, and source material visible in the center.

Beacon-specific Improved Core behavior belongs primarily to dev.47, not dev.46, unless a minimal hook is necessary for architecture.

## Still future

- Improved Core Beacon relay / Diffusion-Radiance-Resonance-Focus-Inversion;
- extended-lighting library decision and crystal coupling;
- optional Unrefined Crying Crystal Core;
- Scan Codex in 1.1.0+;
- full post-feature renderer/state refactor.
