# Mirage Projector dev.42 — Material / Core Chamber / Chassis Rework

> **Status:** implemented in source candidate; Windows build + in-game QA still required.
> **Baseline:** dev.41 documentation closure.
> **Network protocol:** remains 18; no packet/NBT schema change in this wave.

## 1. Goals

dev.42 is the first implementation wave after the dev.41 planning/consolidation pass. It intentionally does **not** implement crystal growth, Obsidian Spike, projector upgrade recipes, Improved Cores, or Beacon relays yet.

Its job is to make the planned material language real and remove the two physical-rendering problems that were already visible in dev.40/dev.41:

1. broad Glass/Glass-Pane surfaces reading as translucent ghost sheets;
2. installed raw Core items being replaced by fake material blocks and disappearing/reading incorrectly from some camera angles.

## 2. Crying Obsidian Shard

Registry id:

```text
mirage_projector:crying_obsidian_shard
```

The final item name is **Crying Obsidian Shard**. `Cut Obsidian Shard` is historical terminology and must not be reintroduced.

Current dev.42 sources:

### Stonecutter

```text
1 Crying Obsidian -> 4 Crying Obsidian Shards
```

### Re-form Crying Obsidian

Two equivalent shaped recipes exist:

```text
S S S
S C S
S S S
```

where `S = Crying Obsidian Shard`, and `C` may be:

- Fire Charge; or
- Magma Cream.

Result: `1 Crying Obsidian`.

This is deliberately compatible with the future renewable crystal-growth loop. dev.42 does not yet add the growth source.

## 3. Shard visual language

The item sprite is a new 16×16 pixel asset with:

- narrow shard silhouette;
- black / very dark purple volcanic-glass base;
- brighter Crying-Obsidian-purple internal vein;
- no dependency on another mod asset.

The same material family is reused by projector emitter surfaces, but the item sprite and block emitter texture are distinct assets.

## 4. Projector material language

The six chassis now share this visual contract:

```text
Obsidian
= structural chassis

Crying-Obsidian optical material
= emitter rails / focusing surfaces

Glass
= small central Core Chamber only
```

The old broad Glass surfaces are removed from the chassis designs.

### Rendering separation

Projector block models use a NeoForge composite model:

- `base`: `minecraft:solid` structural geometry;
- `emitter`: `minecraft:translucent` Crying-Obsidian optical geometry;
- `chamber`: `minecraft:translucent` 4×4×4 Glass chamber.

This is intentional. Do **not** switch the whole projector model back to one global translucent render type; that would cause the opaque Obsidian body to participate in translucent sorting and risks recreating depth/order regressions.

## 5. Universal Core Chamber

Every implemented chassis owns one 4×4×4 model-pixel Glass chamber centered on the projector.

The chamber is part of the baked block/item model and therefore remains visible even with an empty socket.

Approximate center heights:

| Chassis | Chamber center Y |
|---|---:|
| Compact | 3 px |
| Display | 4 px |
| Wide | 4 px |
| Tall | 6 px |
| Field | 5 px |
| Prism | 5 px |

The chamber size is universal. A Core must no longer become arbitrarily larger just because it is installed in a larger chassis.

## 6. Installed Core rendering

The BER now renders the **actual ItemStack installed in the Core slot**.

Removed behavior:

```text
Quartz -> Quartz Block
Amethyst Shard -> Amethyst Block
Diamond -> Diamond Block
Netherite Ingot -> Netherite Block
```

That substitution was misleading and is no longer part of the active contract.

Current item presentation:

- `ItemDisplayContext.GUI` so the installed Core uses its inventory presentation;
- uniform scale inside the 4×4×4 chamber;
- slow camera-facing roll (~one revolution per six seconds), so flat inventory sprites never turn edge-on;
- very small vertical bob;
- fullbright item lighting so the Core reads as the energized element inside the chamber;
- deterministic per-block phase offset so nearby projectors are not perfectly synchronized.

The item itself is not converted, copied into a fake block, or changed by the renderer.

## 7. Chassis-specific physical identity

### Compact

- very thin full Obsidian base;
- four short optical rails pointing toward the central chamber.

### Display

- broader Obsidian platform;
- rectangular Crying-Obsidian optical frame around the chamber.

### Wide

- wide horizontal Obsidian body;
- long front/back optical rails;
- sturdy side/end supports.

### Tall

- smaller footprint;
- taller side pylons;
- optical rails arranged to communicate vertical-purpose hardware.

### Field

- whole Crying Obsidian structural base is intentionally visible;
- reinforced corner blocks;
- optical nodes around the perimeter;
- this previews the later expensive whole-Crying-Obsidian upgrade recipe.

### Prism

- symmetric Obsidian base/corners;
- four radial optical rails pointing North/East/South/West;
- reinforces that Prism remains a four-cardinal-face projector, not a Wide/Tall stack.

## 8. Behavior intentionally unchanged

- Power System values/formulas;
- Core Base PU values;
- chassis multipliers;
- Image/GIF behavior;
- Entity/Item/Banner state;
- network protocol 18;
- Compact's historical default Glass Core behavior;
- all placement facing rules;
- all persisted projector data.

## 9. Not in dev.42

The following remain next-wave work:

- Crying Obsidian Bud/Cluster growth;
- shard structure loot injection;
- Beacon-powered crystal attenuation/refraction/light;
- Obsidian Spike;
- state-preserving projector upgrade recipes;
- Improved Cores;
- Improved-Core Beacon relay;
- extended-light provider selection.

