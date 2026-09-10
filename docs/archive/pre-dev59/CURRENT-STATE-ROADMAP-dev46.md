# Mirage Projector — current state and roadmap (dev.46)

> **Current source candidate:** `0.1.0-dev.46`.
> **Last Windows build-clean baseline:** `0.1.0-dev.45`.
> Protocol remains **18**.

## Current implemented baseline

Everything from dev.45 remains, including stateful projector drops, canonical `ProjectorStateTransfer` and the one-way Mirage -> Display -> Wide/Tall/Prism/Field crafting graph. dev.45 has now been confirmed to compile successfully on the user's Windows target.

Inherited systems also include the six chassis, dev.38 PU architecture, multi-format image/GIF pipeline, Item/Banner/Entity snapshots, Humanoid/Horse workspaces, Core Chamber, renewable Crying Obsidian crystal ecosystem, Beacon absorption/refraction and Obsidian Spike.

## New in dev.46 — five Improved Cores

Implemented source candidates:

- Improved Glass Core — Base 32, amp x1.50;
- Improved Quartz Core — Base 48, amp x1.50;
- Improved Amethyst Core — Base 64, amp x1.50;
- Improved Diamond Core — Base 96, amp x1.50;
- Improved Netherite Core — Base 128, amp x1.50.

They are both placeable decorative/optical blocks and installable projector Cores. Existing Power logic remains `floor(Base PU × chassis × amplification)`.

### Model family

Each uses three separated dark-purple translucent shells with a genuinely rotated middle cuboid, plus the matching source material/item visible at the exact center.

### Recipe family under QA

```text
G S G
S C S
G S G
```

`G=Glass`, `S=Crying Obsidian Shard`, `C=matching base Core material/item`.

This recipe is playable in dev.46 but still balance-provisional.

## Metadata policy

- displayed developer/author: **Celerbi**;
- do not use `a different account identity` as the mod author identity;
- packaged mod metadata/description must remain repository-neutral and contain no repository host reference.

## Next implementation order

1. **dev.46** — Improved Core blocks/items/models + projector x1.50 amplification. **Current source candidate.**
2. **dev.47** — Improved Core Beacon relay, material identities, stacking/caps and powered-crystal coupling.
3. resolve extended-lighting integration/fallback strategy during dev.47 QA;
4. render/gameplay freeze + full regression QA;
5. technical refactor of oversized renderer/state/power classes;
6. `0.1.0` release candidate;
7. `1.1.0+` Mirage Scan Codex / scan-library workflow.

## dev.46 QA priority

1. Windows `build.bat`;
2. missing model/texture/recipe errors;
3. placement/drop of every Improved Core;
4. x1.50 Power values in multiple chassis;
5. stateful break/re-place and chassis upgrades carrying an Improved Core;
6. nested-shell transparency/z-fighting/readability in world, inventory and Core Chamber;
7. regression check of dev.45 upgrade recipes and standard Cores.
