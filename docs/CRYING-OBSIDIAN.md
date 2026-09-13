# Crying Obsidian Ecosystem — 1.0.0

## Shards and reconstruction

`mirage_projector:crying_obsidian_shard` is the crafting/loot unit used by the Crying Obsidian ecosystem.

Crying Obsidian can be reconstructed from shards with either Fire Charge or Magma Cream. EMI/JEI expose both crafting variants.

## Renewable crystal growth

The practical setup is:

```text
Lava source
    ↓
Crying Obsidian
    ↓
Empty space / crystal growth
```

The crystal progresses downward through:

```text
Small Bud → Medium Bud → Large Bud → Crying Obsidian Cluster
```

Growth requires the space below the Crying Obsidian to remain available.

## Harvesting

- Silk Touch preserves the current bud/cluster stage.
- Normal harvesting yields Crying Obsidian Shards.
- Fortune does not multiply shard drops.

EMI presents the growth loop as an icon/tool-tip World Interaction page and links it from Crying Obsidian, every bud stage, Cluster and Shard.

## Beacon interaction

Crying Obsidian crystal stages interact with a Beacon column. The crystal line attenuates the incoming beam by stage and only the Mature Cluster becomes the static Mirage-light source when energized.

Loaded Core Boosters in the Beacon path contribute material-specific relay behavior. At most four effective Boosters are counted.

Material identities:

- Glass — Diffusion
- Quartz — Radiance
- Amethyst — Resonance
- Diamond — Focus
- Netherite — Inversion

These identities affect visible Beacon/reflection behavior and, where defined, the Mature Cluster's static field profile.

## Static Mirage light

An energized Mature Cluster registers one `STATIC_WORLD` Mirage source. The server solves the causal virtual field and publishes resolved light sections to clients.

Open-space level-15 half-decay is:

```text
15,15,14,14,13,13,...,2,2,1,1
```

The solver uses vanilla destination opacity/face-shape semantics. Finite obstacles can be routed around, but geometry-forced detours pay extra cost and therefore form softer/weaker shadows than an equivalent open route.

Quartz/Diamond can extend open persistence according to their identity, but no Booster grants wall penetration. Glass can soften detour cost while trading straight-line range. Amethyst/Netherite do not grant free static reach.

See `MIRAGE-LIGHT-ENGINE.md` for the authoritative solver/network contract.

## Residual optical rays

Crystal/Beacon interaction also produces purple residual optical activity. This visual system is separate from static gameplay light and can react to material-specific relay width/brightness/rotation/excitation state.

## Terrain and lifecycle updates

Relevant block/fluid/growth/piston/explosion changes invalidate affected source fields. Rebuilds use committed server geometry and never force-load chunks. Source dependency windows and chunk revision synchronization handle unload/reload/relog convergence.

## Diagnostics

The `/miragelight` diagnostic surface includes commands such as:

- `stats`
- `probe`
- `axis`
- `rebuild`

`probe` exposes Mirage, vanilla/effective values and path-cost diagnostics for regression work.

## Migration

`mirage_projector:crying_light_node` is retained only to load/clean obsolete physical-relay worlds. Current gameplay never creates physical Mirage relay nodes.

Cleanup is restricted to known Mirage migration blocks and must not remove vanilla `minecraft:light` or unrelated mod blocks.
