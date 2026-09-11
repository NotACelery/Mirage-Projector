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

Only an energized Mature Cluster owns the useful static world-light field. Small/Medium/Large remain zero-block-light optical stages.

The authoritative field is virtual: no physical relay blocks are created. Open no-Booster propagation is exact half-decay over 30 blocks (`15,15,14,14,...,1,1`). `MirageLightOcclusion` uses real vanilla destination opacity/face shape, so solid walls cannot be crossed. A finite wall may be routed around only through adjacent traversable voxels.

dev.75d makes that wrap more natural: direct/open distance keeps half-decay, while route distance that exists only because the obstacle forced a detour receives additional decay. There is no abrupt "everything behind the wall becomes vanilla" switch; deeper shadow pockets simply require a more expensive best path.

Effective block light is `max(vanilla, Mirage)` at read time. Virtual cells are final contributions, never new vanilla emission sources. The old `crying_light_node` block is migration-only. Full details: `MIRAGE-LIGHT-ENGINE.md`.

## Half-decay source lifecycle

The Mature field is baseline behavior and does not require a Booster. Mature + energized registers the source; de-energizing/removing it tears down only that source contribution and reveals any overlapping survivor.

Server terrain edits are coalesced and rebuild impacted fields against the committed geometry. Current event coverage includes place/multi-place/break, fluid placement, crop/feature growth, pistons, explosions and Core Booster-driven refresh. The solver never force-loads chunks. Source/destination chunk lifecycle is handled by the dev.75b authoritative backend and clients solve synchronized source descriptors against their own loaded geometry.

`/miragelight stats`, `probe`, `axis` and `rebuild` are the diagnostic surface. dev.75d `probe` reports weighted direct/extra cost in addition to Mirage/vanilla/effective levels.

## Reflected Booster light

Crying Obsidian consumes dedicated reflected relay identities instead of treating generic Beacon width as world-light power. Up to four effective Boosters are resolved before the Mature source profile is built.

- Quartz/Radiance contributes +1 conceptual static-light tier each.
- Diamond/Focus contributes a smaller static bonus: +1 tier per two effective Diamond, rounded up.
- Total reflected static boost is capped at +4, so Mature conceptual power is 15–19 and nominal open radius 30–38.
- Glass/Diffusion broadens residual reflected-ray geometry but creates no independent static side field.
- Amethyst/Resonance increases residual excitation/rotation activity.
- Netherite/Inversion reverses reflected rotation.

All static reinforcement uses the same causal occlusion + dev.75d detour solver. Quartz/Diamond therefore improve open persistence but do not grant wall penetration. Residual rays keep their dedicated brightness/radius/length/excitation identities.
