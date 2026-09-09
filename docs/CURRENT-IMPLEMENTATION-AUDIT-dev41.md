# Current implementation audit — Mirage Projector 0.1.0-dev.41

## Status

`dev.41` is a **consolidation / documentation / code-hygiene source candidate** built from dev.40.

Confirmed from user QA before this audit:

- dev.40 was executed in-game;
- the Piglin `isShaking`/zombification visual jitter fix works;
- the current physical Core visual can disappear from certain camera angles;
- the broad Glass/Glass-Pane surfaces in current chassis models read as translucent ghost layers and are not acceptable final art.

Network protocol remains **18**. dev.41 intentionally does not change packet/NBT semantics.

## dev.41 changes actually present in source

- version advanced to `0.1.0-dev.41`;
- network protocol token centralized as `MirageProjector.NETWORK_PROTOCOL` instead of a magic literal in `ModNetworking`;
- unused future placeholder chassis `EFFIGY` and `COLOSSAL` removed from the active `ProjectionChassisProfile` enum; only the six registered gameplay chassis remain;
- `ImageSourceBank` now names the distinction explicitly:
  - `ACTIVE_MULTI_SLOTS = 4` for real Wide/Tall gameplay;
  - `PERSISTED_COMPAT_SLOTS = 9` only for dev.33-dev.37 save/wire migration;
- the temporary raw-Core-to-material-block renderer method is renamed `legacyBlockVisualStack()` so new code does not mistake it for final design;
- documentation authority and future progression are consolidated in:
  - `DOCUMENTATION-AUTHORITY-dev41.md`;
  - `CORES-AND-UPGRADES-dev41.md`;
  - `CODE-QUALITY-AUDIT-dev41.md`.

No PU formula, Image/GIF behavior, Entity snapshot behavior, SourceMode, asset transport, render-order stage or network payload is intentionally changed by dev.41.

---

# Implemented authoritative systems

## Platform / network

- Minecraft 1.21.1;
- NeoForge 21.1.244;
- Java 21;
- network protocol 18;
- current image assets use `<sha256>.asset`; legacy `<sha256>.png` remains readable.

## Six current chassis

Only these are active gameplay chassis:

1. Mirage Projector / Compact;
2. Mirage Display;
3. Wide Mirage Projector;
4. Tall Mirage Projector;
5. Mirage Field Projector;
6. Mirage Prism.

All six use horizontal `FACING` on placement. Plane-style projection orientation follows the placed block. Prism Image/Banner faces retain world-cardinal North/East/South/West semantics.

Current nominal efficiency targets:

| Chassis | Nominal W×H | Lift | Float | Power multiplier |
|---|---:|---:|---:|---:|
| Compact | 10×10 | 32 | 4 | ×1.00 |
| Display | 32×32 | 48 | 12 | ×1.50 |
| Wide | 80×32 | 64 | 12 | ×2.00 |
| Tall | 32×80 | 96 | 16 | ×2.00 |
| Field | 128×128 | 144 | 24 | ×4.00 |
| Prism | adaptive face envelope; baseline 48×48 | 96 | 12 | ×2.00 |

These are nominal efficiency ranges, not hard gameplay caps.

## Power system

Current standard Core base PU:

| Core | Base PU | Amplification |
|---|---:|---:|
| Glass | 32 | ×1.00 |
| Quartz | 48 | ×1.00 |
| Amethyst | 64 | ×1.00 |
| Diamond | 96 | ×1.00 |
| Netherite | 128 | ×1.00 |

Effective capacity:

```text
floor(Base Core PU × Chassis multiplier × Core amplification)
```

Current costs remain those defined in `POWER-SYSTEM-REWORK-dev38.md`, including:

- emitter/stability base cost;
- geometry cost;
- quadratic overdrive above chassis nominal geometry/lift/float;
- source complexity;
- presentation features;
- tiny Ghost rebate capped by the formula rather than functioning as a power exploit.

Scale/Lift/Float controls compute PU-payable maxima dynamically.

## Image / GIF

Implemented formats:

- PNG static;
- JPEG/JPG static;
- WebP static;
- BMP static;
- GIF animated.

Content sniffing, not extension, selects the format. Therefore a GIF renamed `.png` is still GIF. Animated WebP and APNG are identified and explicitly rejected until playback support exists.

GIF limits/timing remain dev.39 authority.

### Wide / Tall

- SINGLE: one continuous aspect-preserving source;
- MULTI: exactly four active cells;
  - Wide = 4×1;
  - Tall = 1×4;
- active cells are equivalent squares at the same global Scale;
- Field never uses this bank as an active grid.

### Field

- one continuous Plane;
- the old dev.33 3×3 interpretation is not active behavior.

### Prism

- four lateral faces only: N/E/S/W;
- one image/GIF per face;
- no 4×1/1×4 stacking;
- horizontal, vertical and near-square sources receive adaptive nominal face envelopes;
- geometric overdrive is evaluated per face then summed.

## Item / Banner / Entity

- Item snapshots are virtual copies; real input item is not stored as projected inventory;
- Banner snapshots are virtual and render cloth/pattern content;
- Entity scans reconstruct client-side render entities and never spawn/tick them into the level;
- Player appearance metadata is frozen sufficiently for current vanilla skin/model-part behavior;
- Humanoid gear snapshots may form a bodyless equipment rig after card removal;
- changing to an incompatible entity family purges virtual slot groups that disappear from the workspace;
- Piglin/Hoglin projection clones are normalized against dimension zombification state; user QA confirms Piglin shaking is fixed in dev.40.

## Idle marker

All six current chassis render the idle floating vanilla book when they have no renderable source, independent of installed Core presence.

---

# Current known problems / not yet stable design

## 1. Core physical renderer

Current implementation still substitutes raw Core items with full material-block ItemStacks and non-uniformly scales them in the BER.

Observed user bug:

- active Core visual disappears from some camera angles.

This path is **not final design**. `CORES-AND-UPGRADES-dev41.md` replaces it conceptually with a universal ~4×4×4 Glass Core Chamber containing the real installed inventory item, floating/rotating at uniform scale.

Do not spend large effort polishing `legacyBlockVisualStack()` unless a minimal interim culling fix is required before the chamber implementation.

## 2. Current chassis Glass layers

Current six block models still contain broad thin Glass/Glass-Pane geometry. At their UV scale the visible border frequently disappears, producing angle-dependent translucent sheets/ghost layers.

This is not a shader bug to preserve. Final art contract replaces broad Glass with Cut-Obsidian emitter material and reserves actual Glass for the small Core Chamber.

## 3. Craft progression not implemented

At dev.41 there are still no production recipes for the six projector chassis in resources. Only the Empty Scan Template recipe is present.

The new crafting progression in `CORES-AND-UPGRADES-dev41.md` is therefore a planned contract and does not conflict with an existing released recipe graph.

## 4. Improved Cores not implemented

`ProjectionCoreProfile` contains amplification architecture, but there are no Improved Core items/blocks/recipes/models yet.

## 5. Crying Obsidian / shard systems not implemented

Cut Obsidian Shard, natural Crying conversion intermediates, Jade provider, Obsidian Spike and shard loot injection are design-only in dev.41.

## 6. Remaining render QA inherited from older builds

Still worth explicitly regression-testing before a stable 0.1.x release:

- Entity hologram vs physical projectors at different depths;
- Entity Ghost vs water in front/behind;
- overlapping translucent Entity projections;
- special/modded RenderTypes, eyes/glint/beams;
- GIF stress/performance and multiplayer asset transfer;
- Prism adaptive face PU/rendering with mixed aspects.

---

# Removed/retired source concepts in dev.41

## EFFIGY / COLOSSAL enum placeholders

They were not registered blocks, menus or real current chassis and existed only as provisional architecture values. Keeping them in the active enum made ordinal transport describe values players could never own.

Removed from active source. If either concept returns, introduce it with a real design and explicit network/schema plan.

## Ambiguous nine-slot active Image bank

Nine persisted slots remain for migration compatibility, but source now distinguishes them from the four active Wide/Tall slots by constant name. Slots 4-8 are not gameplay capacity.

## Raw material block Core visual as “final” API

The method remains temporarily because the renderer uses it, but its name/comment now explicitly identifies it as legacy visual behavior pending the Core Chamber pass.

---

# Important open design decision discovered by audit

Compact currently receives a default Glass Core in its BlockEntity constructor and in a legacy migration path.

The new base recipe uses a central Glass Block as the optical chamber ingredient, but the design has **not yet explicitly decided** whether a newly crafted/placed Mirage Projector should also include a free removable Glass Core.

Do not silently infer this from the recipe art. Before implementing crafting progression, choose one:

1. base projector starts with an empty Core socket; Glass Block is chamber only; or
2. base projector includes a Glass Core and recipe cost intentionally covers that starter Core.

Legacy worlds may still need the pre-Core migration fallback regardless of the new-craft decision.

---

# Next implementation order

Use `CORES-AND-UPGRADES-dev41.md` as the next progression contract. Suggested sequence:

1. Cut Obsidian Shard + crafting/loot foundation;
2. natural Crying conversion + two textures + optional Jade;
3. Obsidian Spike;
4. chassis art/Core Chamber replacement;
5. state-preserving upgrade recipe infrastructure and recipe graph;
6. five Improved Core block/items + projector amplification;
7. Beacon relay/stacking;
8. final balance/QA.

Future Scan Codex remains 1.1.0+.
