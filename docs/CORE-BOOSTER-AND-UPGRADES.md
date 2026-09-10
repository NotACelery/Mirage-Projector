# Mirage Projector — Core Booster and projector upgrades

## Standard Projection Cores

The projector Core slot directly accepts the existing standard materials recognized by `ProjectionCoreProfile`:

- Glass / Glass Block → 32 PU;
- Quartz / Quartz Block → 48 PU;
- Amethyst Shard / Amethyst Block → 64 PU;
- Diamond / Diamond Block → 96 PU;
- Netherite Ingot / Netherite Block → 128 PU.

Standard amplification is ×1.00.

## Core Booster

There is one active Booster block/item, not five Improved Core products.

Recipe:

```text
G S G
S   S
G S G
```

- `G` = Glass;
- `S` = Crying Obsidian Shard.

The crafted Booster is empty.

### Loading

Right-click an empty placed Booster with exactly one supported material:

- Glass;
- Quartz;
- Amethyst Shard;
- Diamond;
- Netherite Ingot.

The material becomes the Booster's central stored Core and is rendered as the rotating holographic item inside the nested shell.

### Unloading

Shift + right-click removes the stored material and returns it. The center must become visually empty immediately.

### Breaking and stacking

A correctly mined Booster preserves its stored material. Silk Touch is not required. ItemStacks only combine when their complete stored Booster state is equal, so different materials do not merge.

### Projector behavior

An empty Booster is not a valid projector Core. A loaded Booster maps to the corresponding improved profile and uses ×1.50 amplification without changing that material's Base PU identity.

## Canonical visual geometry

`docs/reference/core-booster/anidado.json` is the current geometry reference. The visible design uses nested translucent Crying-Obsidian-palette shells with the stored material in the center.

## Legacy Improved Core migration

Old dev builds registered five independent Improved Core blocks. They are no longer gameplay content. Their block IDs remain only so old worlds can load and automatically convert them to the equivalent loaded Core Booster.

Do not create new recipes, BlockItems or separate balancing for those legacy IDs.

## Projector crafting progression

Base Mirage Projector:

```text
S S S
S G S
O O O
```

- 5 Crying Obsidian Shards;
- 1 Glass;
- 3 Obsidian.

Compact → Display:

```text
Q S A
S M S
A S Q
```

- 2 Quartz;
- 2 Amethyst Shards;
- 4 Crying Obsidian Shards;
- the stateful Mirage Projector.

Display → Wide:

```text
S S S
G D G
S S S
```

Display → Tall:

```text
S G S
S D S
S G S
```

Display → Prism:

```text
S G S
G D G
S G S
```

Display → Field:

```text
C S C
S D S
C S C
```

`D` is the stateful Display, `G` Glass, `S` Crying Obsidian Shard and `C` whole Crying Obsidian.

## State preservation

`ProjectorStateTransfer` is the only canonical crafting bridge. Upgrade output is created from the source stack, the saved BlockEntity payload is loaded through the destination chassis BlockEntity so its own sanitization/migration rules apply, and the normalized result is stored back on the output item.

This preserves current persistent projector systems and automatically covers new persistent fields that use the same BlockEntity serialization path.

## Beacon relay

A loaded Core Booster placed in an active Beacon column relays and modifies the outgoing beam while preserving the current stained-glass hue. Relay state is cumulative from the Beacon upward and only the first four loaded Boosters are effective.

| Material | Relay identity | Initial dev.60 behavior |
|---|---|---|
| Glass | Diffusion | +35 percentage points of vanilla width and a softer/broader outer beam |
| Quartz | Radiance | +25 percentage points width and increased beam luminance |
| Amethyst | Resonance | +25 percentage points width and ×1.25 rotation speed per effective Amethyst, capped near ×2 |
| Diamond | Focus | +25 percentage points width with a tighter inner beam relative to the outer beam |
| Netherite | Inversion | +25 percentage points width and reverses outgoing beam rotation; additional Netherite Boosters do not toggle it back |

Width additions are additive and capped near 200% vanilla. Empty Boosters do nothing. Loaded Boosters beyond the four-effective limit remain physical but add no further optical modifier. Each Booster applies its relay transition at the physical internal-core plane `8.5/16` inside that Booster block. The lower beam segment preserves the incoming state and only the outgoing segment above the core inherits the material effect. Crying Obsidian attenuation is applied after upstream Booster relay state is resolved.

dev.69–70 define the downstream reflected-light branch specifically for an energized Mature Crying Obsidian Cluster. Small/Medium/Large buds remain zero-block-light optical stages. Mature powered light decays at approximately half vanilla speed through Mirage-owned nodes. Reflected material identity is no longer inferred from generic relay width: Quartz/Radiance is the dominant static range amplifier, Diamond adds smaller focused axial reach, Amethyst drives residual Resonance, Netherite preserves Inversion, and Glass broadens residual-ray geometry. dev.70 intentionally removes dev.69's long diagonal Glass world-light relays because scalar omnidirectional Light Nodes leaked around wall occlusion. Every real source/node remains capped to vanilla level 15. dev.66's reusable `LightProfile` foundation remains the architecture for future profiles.


## Extraction guard (dev.68)

A loaded Core Booster does not extract its core merely because the player is sneaking and right-clicking it. Extraction requires the interacting hand to be empty or to hold the same item type as the core stored in the Booster. If the hand contains a different item, Mirage does not consume/cancel that interaction, allowing the held tool or item to perform its own block interaction instead. This prevents accidental ejection while using compatible interaction tools around the relay stack.
