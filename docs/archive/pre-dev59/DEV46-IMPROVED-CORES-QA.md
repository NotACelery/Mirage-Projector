# dev.46 — Improved Projection Cores QA

Status: **source candidate; Windows build + in-game QA pending**.

## Frozen implementation in this wave

- Exactly five Improved Cores, matching Glass / Quartz / Amethyst / Diamond / Netherite.
- Standard material Base PU remains 32 / 48 / 64 / 96 / 128.
- Improved grade uses initial x1.50 amplification.
- Existing chassis multiplier and PU/Overdrive costs are unchanged.
- Blocks are placeable decorative/optical blocks and installable Core items.
- Model has three spatially separated shells; the middle shell is a genuinely rotated model element.
- Beacon relay behavior is **not** part of dev.46 and must not be inferred from the block existing.

## Recipe candidate under live QA

```text
G S G
S C S
G S G
```

`G=Glass`, `S=Crying Obsidian Shard`, `C=matching standard material/item`.

## Capacity expectations

| Core | Base | Amp | Compact x1 | Field x4 |
|---|---:|---:|---:|---:|
| Improved Glass | 32 | 1.50 | 48 | 192 |
| Improved Quartz | 48 | 1.50 | 72 | 288 |
| Improved Amethyst | 64 | 1.50 | 96 | 384 |
| Improved Diamond | 96 | 1.50 | 144 | 576 |
| Improved Netherite | 128 | 1.50 | 192 | 768 |

## Metadata invariant

The mod must display **Celerbi** as its author/developer identity. The packaged metadata and description must contain no repository URL/reference.
