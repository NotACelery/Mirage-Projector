# Mirage Projector — current state and roadmap (dev.45)

> **Current source candidate:** `0.1.0-dev.45`.
> dev.45 introduces canonical stateful projector items and the first complete one-way chassis crafting progression. It inherits dev.43 crystal/Beacon and dev.44 Obsidian Spike, both still awaiting the user's full Windows/in-game QA.

## Current implemented baseline

Implemented before dev.45 and still current:

- six chassis: Mirage Projector/Compact, Mirage Display, Wide, Tall, Field, Prism;
- furnace-style horizontal placement facing;
- idle floating book on all six;
- dev.38 PU architecture and dynamic limits;
- standard Glass/Quartz/Amethyst/Diamond/Netherite Cores;
- PNG/JPEG/WebP/BMP + animated GIF pipeline and content-based format detection;
- Wide SINGLE/4x1, Tall SINGLE/1x4, Field one continuous Plane, Prism four N/E/S/W faces;
- Item/Banner/Entity virtual snapshots, Humanoid/Horse equipment workspaces and cleanup rules;
- Piglin/Hoglin dimension normalization;
- dev.42 Crying Obsidian Shard + Core Chamber/chassis material rework;
- dev.43 four-stage renewable Crying Obsidian crystals, Beacon absorption/refraction, residual beams and structure loot;
- dev.44 Obsidian Spike.

## New in dev.45

### Stateful projector items

Ordinary Survival mining packs the complete projector BlockEntity state into `minecraft:block_entity_data` on the dropped projector ItemStack. Physical Core/card/staging slots are no longer ejected separately in that path because they are already contained by the packed item.

The packed item can be placed again and vanilla `BlockItem` restoration loads the state back into the same BlockEntity type.

Middle-click clone also saves the loaded BlockEntity state when a real Level is available.

### Upgrade progression

Only the Compact/Mirage Projector is crafted from raw materials. All larger chassis are one-way upgrades:

```text
Mirage Projector
      ↓
Mirage Display
   ├─ Wide
   ├─ Tall
   ├─ Prism
   └─ Field
```

There is no direct Compact -> specialist recipe.

### Implemented recipes

Base Mirage Projector:

```text
S S S
S G S
O O O
```

- `S` Crying Obsidian Shard;
- `G` Glass Block;
- `O` Obsidian.

Mirage -> Display:

```text
Q S A
S M S
A S Q
```

Display -> Wide:

```text
S S S
G D G
S S S
```

Display -> Tall:

```text
S G S
S D S
S G S
```

Display -> Prism:

```text
S G S
G D G
S G S
```

Display -> Field:

```text
C S C
S D S
C S C
```

where `Q=Quartz`, `A=Amethyst Shard`, `M=Mirage Projector`, `D=Mirage Display`, `C=Crying Obsidian`.

Wide/Tall remain equal-cost siblings. Field's exact material cost remains balance-provisional, but the one-way Display gateway and state-preservation behavior are hard contracts.

## Canonical transfer contract

All projector persistent BlockEntity fields are transferred wholesale. Recipe code does not enumerate Core/image/entity/etc. fields.

Flow:

```text
source ItemStack
  ↓ read BLOCK_ENTITY_DATA or source defaults
source canonical payload
  ↓ load into temporary target-chassis BlockEntity
target's own migration/sanitization rules
  ↓
normalized target payload
  ↓
result ItemStack
```

A clean Compact item has no serialized payload yet. dev.45 recreates Compact defaults before upgrading, so its implicit starter Glass Core survives Compact -> Display.

Target capabilities never auto-inflate Scale/Lift/Float. Existing configured values remain exactly what the user chose.

## Destruction distinction

- ordinary Survival player mining: one packed stateful projector item;
- Creative destruction: no packed survival drop;
- non-player destruction/explosion/piston fallback: historical behavior remains, meaning real physical Core/card/staging stacks are protected by ejection, but virtual projector state is not guaranteed in dev.45.

This distinction is deliberate for dev.45; explosion-safe full state packaging can be revisited after the normal upgrade path is proven in-game.

## Next implementation order

1. **dev.45** — stateful items + upgrade crafting. **Current source candidate.**
2. **dev.46** — five Improved Cores, block/item models and projector amplification.
3. **dev.47** — Improved Core Beacon relay and crystal interaction coupling.
4. render/gameplay freeze + full regression QA.
5. technical refactor of oversized renderer/state/power classes.
6. `0.1.0` release candidate.
7. `1.1.0+` Mirage Scan Codex / scan-library workflow.

## Important inherited QA

dev.45 must not hide inherited regressions. User testing should still prioritize any dev.43/dev.44 build/render failures before diagnosing upgrade logic.
