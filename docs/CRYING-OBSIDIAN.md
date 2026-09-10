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

Only an energized Mature Cluster is intended to own the useful world-light field. Small/Medium/Large remain zero-block-light optical stages.

Live dev.70–73 QA showed that the physical `crying_light_node` strategy cannot be the final implementation. Even a node placed on a valid source path becomes an independent omnidirectional vanilla emitter and can radiate sideways or refill a region hidden from the original Cluster. The measured result therefore diverged from both wall causality and the exact half-decay target.

dev.74 begins the replacement with a parallel server-side Mirage Light Engine shadow source. It uses fixed-point energy and six-neighbour voxel propagation, so every solved cell is connected to the Cluster through an actual traversable path. Per-edge opacity/face blocking delegates to vanilla light-occlusion semantics. A full separating barrier disconnects the shadow field; a finite wall can still be routed around at the cost of a longer/weaker path.

The no-Booster virtual target at outward distances 1–30 is exactly:

```text
15 15 14 14 13 13 12 12 11 11 10 10 9 9 8 8 7 7 6 6 5 5 4 4 3 3 2 2 1 1
```

Quartz/Radiance and Diamond/Focus still feed conceptual scalar reinforcement into the Mature shadow profile, with Quartz stronger. Final virtual visible values remain capped to 15; extra conceptual power extends the saturated plateau/tail. Glass, Amethyst and Netherite keep their reflected visual identities without seeding independent side emitters.

For transition safety, dev.74 still leaves the dev.73 physical nodes active as visible/gameplay light. `/miragelight` commands inspect the new virtual field separately. dev.75 is responsible for the authoritative backend handoff and legacy-node cleanup.

## Half-decay source lifecycle

The Mature field is not gated by Core Boosters. An energized Mature source registers even at reflected tier zero. Terrain place/break edits remain coalesced until the end of the same server tick, then affected Mature sources are refreshed once against the committed geometry. Core Booster material changes explicitly trigger the same nearby source refresh.

Breaking or de-energizing a Mature source removes its dev.74 virtual contribution immediately from the per-Level aggregate. Other source contributions remain and become visible through max aggregation. The legacy physical-node reconciliation remains active only for the dev.74 transition.

The solver never force-loads chunks. During dev.74 shadow QA, `/miragelight rebuild` can refresh the nearest source after a relevant chunk loads. Automatic chunk-load invalidation becomes mandatory before dev.75 virtual light is authoritative.

## Reflected Booster light

Crying Obsidian consumes dedicated reflected relay properties rather than raw incoming Beacon width as a generic power tier. Quartz reinforces static Radiance, Diamond adds smaller Focus reinforcement, Amethyst increases residual Resonance/cadence and Netherite preserves Inversion. Glass broadens reflected residual-ray geometry without creating static secondary world-light branches.

Residual purple rays keep the same material identity: Quartz raises alpha/length, Glass broadens the beam, Amethyst increases excitation/rotation, Diamond narrows/focuses the beam while adding modest length, and Netherite reverses rotation. The existing four-effective-Booster cap remains unchanged.
