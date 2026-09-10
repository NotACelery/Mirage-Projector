# Mirage Projector — Crying Obsidian ecosystem

## Crying Obsidian Shard

The shared crafting material is `mirage_projector:crying_obsidian_shard`.

Renewable/reversible crafting:

```text
Stonecutter: 1 Crying Obsidian -> 4 shards
```

```text
8 shards + Fire Charge -> 1 Crying Obsidian
8 shards + Magma Cream -> 1 Crying Obsidian
```

Chest loot also has uncommon shard injections for Ruined Portals, Abandoned Mineshafts and village smith-family chests.

The inventory sprite follows the Prismarine Shard silhouette with the approved Crying-Obsidian gradient.

## Renewable growth

Generator structure:

```text
lava source or flowing lava
Crying Obsidian
open air or water below
          |
          v
Small -> Medium -> Large -> Mature Cluster
```

The initial nucleation gate is the slowest part. Current source uses a 1/5 success gate on an eligible Crying Obsidian random tick. Later stage denominators are stage-specific and tuned so the full lifecycle feels broadly comparable to Amethyst while retaining a slower start.

Removing the lava stops future generator-driven progression but does not regress an existing crystal.

## Visual law

Crying Obsidian buds/clusters use the vanilla Amethyst visual family as their canonical shape/sprite reference. The user-supplied vanilla reference JSON/textures live under `docs/reference/canonical-amethyst/`.

Do not replace them with improvised grass-like, custom spike-bundle or merely "similar pixel count" models. The intended rule is vanilla Amethyst presentation with Crying-Obsidian recoloring/theme.

## Harvesting

Without Silk Touch:

- Small → 1 shard;
- Medium → 2 shards;
- Large → 3 shards;
- Mature → 4 shards.

Fortune does not increase those values. Silk Touch preserves the actual crystal stage for relocation/decoration.

## Beacon absorption

When a crystal intersects an active Beacon column, any loaded Core Boosters below that crystal are resolved first. The crystal then receives the resulting relay width/radiance/rotation state before its own attenuation and residual-ray behavior is applied.


| Stage | Vertical transmission | Vanilla block light | Residual-ray scale | Approx. ray cycle |
|---|---:|---:|---:|---:|
| Small | 75% | 0 | 25% | 480 ticks |
| Medium | 50% | 0 | 50% | 240 ticks |
| Large | 25% | 0 | 75% | 160 ticks |
| Mature | 0% | 15 | 100% | 120 ticks |

Every stage cuts the rendered vertical Beacon beam at the crystal's bottom face/pixel 0. A non-Mature stage resumes the attenuated beam above the crystal rather than drawing through the crystal model.

Buds do not emit block light solely from Beacon excitation. Mature becomes the current light-emitting crystal stage.

A Mature Cluster directly above an active Beacon suppresses the visible vertical beam and the current implementation attempts to suppress the Beacon block's own emitted light until the cluster is removed, leaving the cluster as the visible light source.

## Residual rays

Current target behavior:

- X/Z origin is the exact crystal center;
- vertical origin is approximately model pixel Y=2;
- no slow extension animation;
- ray appears instantly at full length;
- holds about 20 ticks;
- then retracts while fading for about 20 ticks;
- Mature width is roughly half the older oversized implementation;
- Mature length is about one third of the older implementation;
- lower stages reduce ray width, length and frequency by their stage scale.

The energized Mature texture uses a stronger purple/lavender treatment than the normal cluster.

## Obsidian Spike

Recipe:

```text
 S 
S S
TKT
```

- 3 Crying Obsidian Shards;
- 2 String;
- 1 Stick.

It is a low obstacle with nine visible points, hinders living-entity movement and deals 2 damage points on successful movement contact.

## Extended useful-light field
dev.67 makes the half-decay field the standard powered behavior of an energized Mature Cluster, independent of requiring a modified relay. Small/Medium/Large buds remain zero-block-light stages; when energized they attenuate/continue the Beacon beam and can emit residual rays, but they do not create a world-light field.

Mirage-owned auxiliary nodes preserve the six world-axis baseline at approximately one sampled source every two blocks. Mature level 15 therefore reaches about 29 blocks along an unobstructed primary axis before reflected Booster reinforcement. dev.69 removes generic beam-width range tiers: Quartz/Radiance is the dominant range amplifier, Diamond/Focus adds a smaller axial bonus, and Glass/Diffusion broadens coverage through bounded face-diagonal branches rather than simply making every branch brighter/farther.

Nodes remain internal-only blocks: no item, no Creative entry, no collision, no drops and replaceable. They only occupy air/other Mirage nodes in loaded chunks. Source-path tracing rejects fully opaque obstructions and applies extra loss for partial light blockers. Minecraft then performs ordinary local propagation/AO around the accepted nodes, so geometry can shadow opposite surfaces and light may still wrap naturally around corners instead of Mirage manually shading faces.

## Half-decay field baseline and teardown (dev.68)

The extended Mature Cluster field is not gated by Core Boosters. A Beacon-energized Mature Cluster uses the slow-decay field at relay tier 0; effective Boosters only add relay tiers that reinforce or extend that field. The intended baseline axial progression repeats each block-light level for approximately two blocks rather than one.

When a Mature Cluster is broken, its contribution is reconciled immediately. Each Mirage light node previously reachable from that source is removed in the same server tick if no other Mature source still needs it, or downgraded immediately to the strongest remaining overlapping contribution. The same immediate path is used when an energized Mature Cluster becomes de-energized.

## Reflected Booster light (dev.69)

Crying Obsidian now consumes dedicated reflected relay properties rather than the raw incoming Beacon width as a generic power tier. Glass broadens reflected coverage, Quartz reinforces radiance/range, Amethyst increases resonance/cadence, Diamond focuses the reflected field and Netherite preserves inversion.

Residual purple rays use the same material identity: Quartz raises alpha/length, Glass broadens the beam, Amethyst increases excitation/rotation, Diamond narrows/focuses the beam while adding modest length, and Netherite reverses rotation. The existing four-effective-Booster cap remains unchanged.
