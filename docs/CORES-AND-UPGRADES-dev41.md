# Cores and Upgrades — authoritative design contract (dev.41)

> **Status:** authoritative design specification for the next material/crafting/core progression work. The mechanics in this document are **defined**, but most are **not implemented in dev.41** unless a section explicitly says otherwise. Existing dev.38 Power, dev.39 Image/GIF and dev.40 Entity behavior remain the active implementation until these systems land.
>
> **Design rule:** when this document conflicts with old roadmap text about glass-pane chassis recipes, independent direct recipes for every projector, purpose-built Core variants, or raw-material block visuals for installed Cores, **this document wins**.

## 1. Goals

The progression must solve five problems at once:

1. give Mirage Projector its own material identity instead of depending visually/mechanically on generic Glass Panes;
2. make chassis progression feel like upgrading one machine family rather than crafting six unrelated blocks;
3. make Improved Cores useful both inside projectors and as decorative/optical blocks in the world;
4. give Crying Obsidian renewable-ish world progression without making it trivial;
5. preserve every virtual snapshot/import/configuration when a projector is upgraded.

The resulting material language is:

- **Obsidian** = structural chassis;
- **Crying Obsidian** = resonant volcanic glass / advanced chassis material;
- **Cut Obsidian Shard** = refined optical/emitter element;
- **Glass** = transparent containment chamber, not a broad flat chassis layer;
- **Core material** = energy source;
- **Improved Core shell** = optical amplification structure around the same material source.

---

# 2. Cut Obsidian Shard

## 2.1 Item identity

**Item name:** `Cut Obsidian Shard`

Visual contract:

- slim triangular shard silhouette inspired by the readable shape language of a Prismarine Shard, but it must be an original sprite;
- palette derived from Crying Obsidian: black/purple body with restrained magenta/violet emissive-looking accents;
- must read as a sharp, deliberately cut piece of volcanic glass rather than a crystal chunk;
- intended to become one of the recognizable recurring materials of the mod.

The shard is both a crafting component and a world-usable hazard ingredient.

## 2.2 Primary production — Stonecutter

Frozen recipe:

```text
Stonecutter
1 Crying Obsidian -> 4 Cut Obsidian Shards
```

Rationale:

- a Stonecutter directly communicates precision cutting;
- 4 shards per block keeps Crying Obsidian valuable while allowing repeated projector/core crafting;
- the recipe does not require inventing a dedicated workstation for the first release.

## 2.3 Re-forming Crying Obsidian

Cut shards can be melted/fused back into a Crying Obsidian block, but deliberately at a material loss.

Frozen pattern:

```text
S S S
S H S
S S S
```

- `S` = Cut Obsidian Shard
- `H` = heat catalyst

Accepted heat catalysts are planned as **alternative recipes**:

- Fire Charge; or
- Magma Cream.

Output:

```text
8 Cut Obsidian Shards + 1 heat catalyst -> 1 Crying Obsidian
```

Because one Crying Obsidian produces four shards, rebuilding a block from eight shards consumes the equivalent of two source blocks. This is intentional: the recipe is a salvage/consolidation path, not lossless duplication.

---

# 3. Cut Obsidian Shard world loot

The shard should appear as uncommon/rare utility loot anywhere vanilla already teaches the player to associate a structure with Obsidian/Crying Obsidian, plus a few exploration structures where finding sharp mineral fragments feels natural.

## 3.1 Frozen source categories

Initial target pools:

- Ruined Portal chests;
- Abandoned Mineshaft chest minecarts;
- Village smith-related chests (weaponsmith/toolsmith/armorer-style loot rather than every village house);
- other vanilla structure loot tables that already contain Obsidian or Crying Obsidian, reviewed case-by-case before implementation.

## 3.2 Balance policy

The shard must supplement Crying Obsidian progression, not replace it.

Therefore:

- structure loot should normally award **small stacks**;
- Ruined Portals may be the most generous because the material is thematically direct;
- Mineshafts/smiths should be lower probability or lower count;
- no structure should routinely provide enough shards for a full advanced projector by itself;
- exact weights/counts are **balance TBD** and must be verified against the actual Minecraft 1.21.1 loot tables when implemented.

Do not blindly inject into every chest. The rule is thematic relation + controlled scarcity.

---

# 4. Natural Crying Obsidian conversion

## 4.1 Purpose

Crying Obsidian is intentionally scarce. Mirage adds a slow environmental process that turns ordinary Obsidian into Crying Obsidian by making it act as the solid volcanic-glass membrane in a lava/dripstone apparatus.

This is **not** instant crafting and should visually explain the word *Crying*.

## 4.2 Required structure

Frozen structure contract:

```text
[Lava source]
[Obsidian target]
[Pointed Dripstone, pointing down]
      ...
[Cauldron]
```

Requirements:

1. a **lava source block** directly above the target;
2. target starts as normal Obsidian;
3. a downward Pointed Dripstone is attached directly below the target;
4. a valid Cauldron exists below the dripstone tip in the same practical layout used for vanilla dripstone cauldrons.

If the structure becomes invalid, conversion progress stops. It does not instantly regress while left in place.

## 4.3 Speed

The complete Obsidian -> Crying Obsidian process targets an **average duration roughly twice the average time needed for a comparable vanilla dripstone lava setup to fill a Cauldron** under equivalent random-tick conditions.

Interpretation:

- “half as fast” means the full conversion should take approximately `2x` the comparable lava-cauldron process;
- implementation must centralize this rate in one constant/algorithm so it can be balanced without changing block logic;
- visual stages are progress indicators, not separate independent random processes.

Exact random-tick probability is implementation/balance work; the relative target above is frozen.

## 4.4 Visual stages

There are two hidden/intermediate world states between Obsidian and Crying Obsidian:

```text
Stage 0: Obsidian                         ~0%
Stage 1: Crying infusion intermediate A  ~33%
Stage 2: Crying infusion intermediate B  ~67%
Stage 3: Crying Obsidian                 100%
```

Two custom textures are required later.

Art direction:

- Stage 1 should still read overwhelmingly as Obsidian with the first sparse Crying Obsidian veins/tears;
- Stage 2 should visibly approach Crying Obsidian but retain enough normal Obsidian to communicate incomplete transformation;
- Stage 3 is the vanilla Crying Obsidian block.

## 4.5 Mining intermediate stages

Frozen anti-exploit rule:

- breaking Stage 1 or Stage 2 yields **normal Obsidian**;
- Silk Touch must not preserve an intermediate-stage item;
- there are no normal obtainable inventory items for the intermediate blocks;
- replacing/moving the block through ordinary break/place actions loses progress.

This prevents storing half-completed blocks and makes the conversion a world process rather than a new tradable block family.

## 4.6 Jade integration

Jade should treat both intermediate stages as **Obsidian**, not expose internal registry names.

Display concept:

```text
Obsidian
Crying infusion  [=======-----]  67%
```

Rules:

- name remains `Obsidian` for Stage 1/2;
- an optional progress line/bar communicates conversion progress;
- progress is server-authoritative;
- if Jade is absent, the mechanic works normally with textures as the only feedback;
- Jade integration must remain optional/soft dependency.

The user-facing precedent is staged compost-like blocks: the intermediate implementation detail should not become inventory clutter.

---

# 5. Obsidian Spike

## 5.1 Recipe

Frozen ingredient budget:

- 3 Cut Obsidian Shards;
- 2 String;
- 1 Stick.

The exact shaped 3x3 arrangement may be adjusted for visual readability, but the ingredient count is frozen.

## 5.2 Model

**Block name target:** `Obsidian Spike`

Visual contract:

- occupies at most approximately **half a block in height** (`8/16 px` target);
- resembles a small aggressive Amethyst Cluster rather than a fence/cactus;
- exactly **9 visible spike tips** in the model language;
- one central spike is taller, narrower and sharper than the surrounding eight;
- Crying Obsidian/Cut Shard palette with restrained violet fissures;
- collision/selection shape should correspond to the visible low cluster, not a full invisible cube.

## 5.3 Damage/slowdown

The Spike should feel materially sharper than wooden thorns.

Frozen behavior:

- each successful damage event deals **2.0 damage points = 1 heart**;
- movement through/on the Spike is slowed in the same general annoying/trapping spirit as a Sweet Berry Bush;
- damage cadence should be bush-like rather than `2 damage every game tick`;
- normal hurt/invulnerability timing must prevent instant 40-damage-per-second behavior;
- affects players and living mobs;
- does not damage dropped items, XP or ordinary projectiles.

The implementation should centralize damage/collision behavior rather than use per-entity special cases.

---

# 6. Chassis visual/material redesign

## 6.1 Flat Glass Pane layers are deprecated

Current dev.40 models use broad 1-pixel-high Glass/Glass Pane surfaces. At projector scale the vanilla texture often samples only the transparent middle and loses the border that visually communicates glass. The result is angle-dependent translucent “ghost layers”.

Those broad flat Glass surfaces are **deprecated design**.

New material language:

- primary base: Obsidian;
- advanced/heavy base regions: Crying Obsidian;
- emitter rails/plates: custom Cut Obsidian material derived from Crying Obsidian, moderately translucent but substantially more readable than vanilla Glass;
- actual transparent Glass is reserved for the small Core Chamber.

Do not merely swap the old thin element's texture from `glass` to `crying_obsidian`. The UV/model must be authored so edges and thickness remain readable.

## 6.2 Core Chamber

All six current chassis should converge on a small universal optical chamber around the installed Core.

Target:

- roughly `4x4x4 px` chamber;
- glass enclosure is visually explicit and fully bounded;
- installed Core item floats inside it;
- the Core uses its **real inventory item/model**, not an artificial full material block;
- slow Y rotation plus extremely small vertical bob suggests light/energy passing through it;
- chamber location may differ by chassis, but chamber dimensions should remain substantially universal.

Current dev.40 `ProjectionCoreProfile#legacyBlockVisualStack()` (raw material -> full material block) is therefore a **temporary legacy renderer**, not the final Core visual contract.

## 6.3 Field identity

Mirage Field Projector is the first chassis that should visibly use **whole Crying Obsidian** in a major part of its base. It represents a structural upgrade, not merely a wider emitter plate.

---

# 7. Projector crafting progression

## 7.1 Progression graph

Only the base Mirage Projector is crafted from raw ingredients. Other current chassis are upgrades that consume a previous projector item.

Frozen progression:

```text
Mirage Projector
      |
      v
Mirage Display
   |    |    |    |
   v    v    v    v
 Wide  Tall Prism Field
```

This replaces the old idea that all six projectors should have unrelated direct crafting recipes.

Reasons:

- chassis capability now feels like one machine family;
- advanced projectors cannot bypass the early material progression;
- custom upgrade recipes can preserve projector data;
- expensive Field progression becomes meaningful.

## 7.2 Base Mirage Projector recipe

Beacon-inspired frozen pattern:

```text
S S S
S G S
O O O
```

- `S` = Cut Obsidian Shard
- `G` = Glass Block
- `O` = Obsidian

Output: **Mirage Projector** (small/Compact chassis).

Ingredient totals:

- 5 Cut Obsidian Shards;
- 1 Glass Block;
- 3 Obsidian.

The Glass Block represents the central optical chamber; the shards replace the old broad Glass Pane idea.

## 7.3 Mirage Projector -> Mirage Display

Frozen recipe supplied by design:

```text
Q S A
S M S
A S Q
```

- `Q` = Quartz
- `A` = Amethyst Shard
- `S` = Cut Obsidian Shard
- `M` = Mirage Projector

Output: **Mirage Display**.

Totals:

- 2 Quartz;
- 2 Amethyst Shards;
- 4 Cut Obsidian Shards;
- 1 existing Mirage Projector.

This is the mandatory first chassis upgrade.

## 7.4 Mirage Display -> Prism

Frozen pattern:

```text
S G S
G D G
S G S
```

- `S` = Cut Obsidian Shard
- `G` = Glass Block
- `D` = Mirage Display

Output: **Mirage Prism**.

The four cardinal Glass Blocks visually explain the Prism's four independent lateral optical faces.

## 7.5 Mirage Display -> Wide / Tall

Frozen gameplay rule:

- both consume a Mirage Display;
- Wide and Tall must have **equivalent total material cost**;
- recipe arrangement should visually imply horizontal vs vertical emitter expansion;
- neither chassis is allowed to become a cheaper path to the other's Effective PU tier;
- exact final 3x3 patterns are **TBD during crafting balance/art implementation**.

Do not invent asymmetric costs before the final chassis models are available.

## 7.6 Mirage Display -> Field

Frozen progression rules:

- Field consumes a Mirage Display;
- Field must be significantly more expensive than Wide/Tall/Prism;
- whole Crying Obsidian blocks must be part of the recipe, not only shards;
- its model should correspondingly expose Crying Obsidian prominently in the base;
- exact recipe is **TBD** after the model/material pass.

Candidate shape retained for balancing, **not frozen recipe**:

```text
C S C
S D S
C S C
```

- `C` = Crying Obsidian
- `S` = Cut Obsidian Shard
- `D` = Mirage Display

---

# 8. Upgrade recipes must preserve projector state

This is a hard technical contract.

When a projector item is used as the center/ingredient of an upgrade recipe, the output must preserve all compatible persistent state from the input projector.

At minimum:

- installed Core ItemStack;
- current SourceMode;
- Front/Back image identities and dimensions;
- GIF/static asset references;
- Wide/Tall image bank compatibility data where relevant;
- Entity Scan card/state;
- frozen Entity snapshot metadata;
- projected Humanoid equipment snapshots;
- Horse snapshots;
- Banner snapshots;
- Item snapshot;
- Scale/Lift/Float;
- rotation state, direction, period and offset;
- Float mode/cycle;
- Lighting;
- Ghost;
- Tint;
- Scanlines;
- Flip/back-face settings;
- future persistent fields not yet known.

Implementation rule:

> Do not write a recipe that manually copies a hand-maintained list of twenty fields.

The projector item/block data should have one canonical transferable data representation. Upgrade recipes copy that representation wholesale and then run **chassis compatibility normalization** only where necessary.

Examples:

- Mirage Display SINGLE image -> Wide remains SINGLE with same image/settings;
- Display -> Prism must preserve the primary image and use the defined Prism migration rule rather than deleting it;
- a valid Entity Scan and virtual gear survive any chassis upgrade because SourceMode Entity is chassis-independent;
- installed Core survives the upgrade;
- player must never lose imported asset references because of crafting.

This likely requires a custom recipe serializer/assembly path rather than a plain vanilla shaped result with no input-component transfer.

---

# 9. Improved Projection Cores

## 9.1 Family

There are **five Improved Cores**, exactly one per current standard Core material:

1. Improved Glass Core;
2. Improved Quartz Core;
3. Improved Amethyst Core;
4. Improved Diamond Core;
5. Improved Netherite Core.

This replaces the earlier idea of separate Amplified/Focused/Stabilized/Resonant item variants. Distinctive behavior is assigned by **material**, keeping the item family understandable.

## 9.2 Projector Power behavior

Frozen architecture:

- Improved Core inherits the **same base PU** as its standard material;
- it gains a Core amplification multiplier;
- initial balance target: approximately **x1.50 amplification**;
- chassis multiplier still applies normally;
- the Improved Core does not bypass PU or Overdrive calculations.

Formula remains:

```text
Effective PU = floor(Base Core PU x Chassis Multiplier x Core Amplification)
```

Standard materials remain `x1.00`.

Initial Improved target is `x1.50`, subject to in-game balance before release.

## 9.3 Block/item form

Every Improved Core is also a placeable decorative block.

Model contract:

- three nested dark-purple translucent glass-like cubes, visually analogous to nested/tesseract containment;
- **outer cube:** aligned to block axes;
- **middle cube:** genuinely rotated in geometry, not merely painted diagonally on a texture;
- **inner cube:** aligned/smaller containment shell;
- corresponding Core material floats at the exact center;
- enough separation between shells to read as three independent volumes rather than z-fighting coplanar glass;
- visual inspiration may evoke a tesseract/End Crystal containment effect, but model/textures must be original.

The middle rotated shell is a hard visual requirement because it provides the contrast that stops the block reading as three flat nested boxes.

## 9.4 Improved Core crafting pattern

The exact Improved Core recipe is **not frozen yet**. The current preferred candidate is:

```text
G S G
S C S
G S G
```

- `G` = Glass Block
- `S` = Cut Obsidian Shard
- `C` = corresponding standard Core material/item

Output: matching Improved Core.

This candidate intentionally combines transparent containment (Glass) with the refined Crying-Obsidian optical frame (shards). Before implementation, verify cost against the projector upgrade tree and the scarcity of Crying Obsidian.

Exact handling for Glass-as-core center, raw-item vs material-block acceptance, and final Glass/shard counts remain balance decisions.

---

# 10. Improved Cores in Beacon beams

## 10.1 Optical relay concept

When a placed Improved Core intersects an active Beacon beam, it acts as an optical relay/amplifier.

Frozen geometric rule:

- incoming vanilla beam terminates at the **middle horizontal plane of the Core block**, exactly between pixel rows 8 and 9 (`Y + 0.5 block`);
- a new outgoing beam segment begins at that same plane;
- outgoing segment is wider/more visually powerful;
- Core does **not** recolor the beam;
- stained glass remains the vanilla color-control mechanism.

Conceptually:

```text
       narrow incoming beam
              |
              |
        [ Improved Core ]
------------- y+0.5 -------------
             |||
             ||| wider outgoing beam
             |||
```

This should be rendered as a continuous optical relay, not as two visibly disconnected beams.

## 10.2 Universal beam effect

Every Improved Core:

- increases outgoing beam width;
- preserves the current incoming color;
- keeps the Beacon's normal vertical role;
- can combine with other Improved Cores up the same beam column;
- cannot replace/remove the role of stained glass.

## 10.3 Material-specific effects

### Improved Glass Core — Diffusion

Role: maximum visual expansion.

- strongest width contribution of the five;
- outer beam appears broader/softer;
- does not receive the strongest brightness or rotation bonus.

Target width contribution before global cap: approximately `+35%` of vanilla beam width.

### Improved Quartz Core — Radiance

Role: luminous beam.

- standard width increase;
- increases perceived beam brightness/inner luminance;
- does not change hue.

Target width contribution: approximately `+25%`.

Brightness stack values are balance TBD and must clamp safely.

### Improved Amethyst Core — Resonance

Role: motion/resonance.

- standard width increase;
- increases beam rotation speed;
- optional very subtle width pulse may be considered only if it remains visually clean and inexpensive.

Target width contribution: approximately `+25%`.

Target rotation contribution: approximately `+25% speed` per effective Amethyst Core, capped around `2x` vanilla speed.

### Improved Diamond Core — Focus

Role: defined high-energy center.

- standard overall width increase;
- strengthens/defines the bright inner beam relative to the widened outer beam;
- should visually read as focused energy without making the total beam narrower than the universal Improved-Core width rule.

Target width contribution: approximately `+25%`.

Exact inner/outer alpha-width ratios are renderer balance TBD.

### Improved Netherite Core — Inversion

Role: unmistakable rotational behavior.

- standard width increase;
- reverses outgoing beam rotation direction;
- may use a slightly heavier/slower rotational feel, but reversal is the primary identity;
- multiple Netherite Cores **do not cancel each other out**. Presence of one or more active Netherite relay effects means reversed rotation for the affected outgoing segment.

Target width contribution: approximately `+25%`.

## 10.4 Stacking limits

Frozen anti-explosion rule:

- at most **4 Improved Cores** in one Beacon column contribute mechanical/visual amplification;
- additional placed Improved Cores may remain decorative/transparent to the already-modified beam but add no further amplifier power;
- mixed materials combine their secondary effects;
- global width caps at approximately **200% of vanilla width**.

Width should be additive/capped, not multiplicative runaway.

Target interpretation:

```text
normal non-Glass Core contribution: +25 percentage points
Glass contribution:                 +35 percentage points
maximum final beam width:           200% vanilla
```

Examples:

- Quartz + Amethyst -> wider + brighter + faster rotation;
- Quartz + Amethyst + Netherite -> wider + brighter + faster + reversed rotation;
- 4 Glass cores still clamp at the global width cap rather than growing indefinitely.

Stacking calculations must be centralized so changing one balance constant cannot desynchronize segments/render paths.

---

# 11. Current standard Core visual deprecation

Current dev.40 behavior maps raw Core items to material-block visuals for the tiny BER Core (`Diamond -> Diamond Block`, `Netherite Ingot -> Netherite Block`, etc.).

This is explicitly **deprecated by this design**.

Final desired behavior:

- standard Core: render actual installed inventory item inside Core Chamber;
- Improved Core: render its own recognizable Improved-Core block/item model inside the chamber;
- both use uniform small scale, slow rotation and small bob;
- chamber/culling bounds include the entire animated item at all camera angles.

Until this visual pass is implemented, the old block visual remains compatibility code, not a stable art contract.

---

# 12. Future 1.1.0 — Mirage Scan Codex

This feature is deliberately **not part of the current 0.1.x implementation wave**, but the progression contract is recorded now so current Scan Cards remain compatible with it.

## 12.1 Problem

Current workflow requires one physical Empty Scan Template per copied scan. Twenty identical projectors can require twenty manual scans of the same entity.

## 12.2 Future design

A persistent **Mirage Scan Codex** stores scans as a searchable/filterable library.

Desired workflow:

```text
Entity
  -> scan once into Codex
  -> permanent library entry

Codex entry + Paper
  -> copy/printing station
  -> physical Entity Scan Card
  -> existing projector slot
```

Benefits:

- player scans an entity once;
- duplicate cards cost Paper rather than repeated access to the original entity;
- current projector architecture continues consuming physical cards, avoiding a full projector rewrite;
- Codex can support search, filters, favorites, name/type preview and player/mob organization.

A dedicated copying workstation is preferred over stuffing this behavior into the projector GUI.

Target version: **1.1.0 or later**.

Do not start this system until the current chassis/core/render progression is stable; the Debug Handbook work proved that book-heavy UI can consume significant implementation/QA time.

---

# 13. Implementation order

Recommended order after dev.41 audit:

1. Cut Obsidian Shard item + sprite + Stonecutter recipe + re-form recipe;
2. Crying Obsidian natural conversion blocks/textures + optional Jade provider;
3. Obsidian Spike block/model/recipe/damage behavior;
4. six chassis visual refresh: remove broad Glass layers, add Cut-Obsidian emitter language + universal Core Chamber;
5. implement base Mirage recipe and state-preserving upgrade recipe infrastructure;
6. Display recipe, then Wide/Tall/Prism/Field upgrade recipes;
7. five Improved Core block/items + nested/rotated model + projector amplification;
8. Beacon relay renderer/mechanics + stacking caps;
9. loot-table injection balance pass;
10. full progression QA and recipe-book/JEI/EMI compatibility review if desired;
11. much later: 1.1.0 Scan Codex/copy station.

Do not implement everything in one giant patch. The contract is unified, but each implementation stage should remain independently recoverable/buildable.

---

# 14. Frozen vs provisional values

## Frozen design

- 1 Crying Obsidian -> 4 Cut Obsidian Shards in Stonecutter;
- 8 shards + Fire Charge or Magma Cream -> 1 Crying Obsidian;
- Obsidian Spike uses 3 shards + 2 String + 1 Stick;
- Spike target <= half-block height, 9 tips, 2.0 damage per successful hurt event;
- natural Crying conversion structure uses lava / Obsidian / pointed dripstone / Cauldron;
- two intermediate stages, both mine back to normal Obsidian;
- full conversion target ~2x comparable lava-cauldron time;
- Jade identifies intermediates as Obsidian + optional progress;
- base Mirage recipe `SSS / SGS / OOO`;
- Mirage -> Display recipe `QSA / SMS / ASQ`;
- Display is mandatory before Wide/Tall/Prism/Field;
- Display -> Prism recipe `SGS / GDG / SGS`;
- upgrade crafting preserves projector state;
- five Improved Cores, one per material;
- Improved Core same base PU + amplification, initial target x1.50;
- three nested shells, middle shell genuinely rotated;
- Beacon relay begins outgoing segment at Y+0.5, preserves color and widens beam;
- Glass/Quartz/Amethyst/Diamond/Netherite identities = Diffusion/Radiance/Resonance/Focus/Inversion;
- max 4 effective Improved Cores per beam, global width cap ~2x;
- Scan Codex deferred to 1.1.0+.

## Provisional / needs balance or art validation

- exact shard loot weights/counts;
- exact random-tick probability for Crying conversion, while preserving the 2x relative target;
- exact Wide/Tall recipes;
- exact Field recipe beyond Display + whole Crying Obsidian requirement;
- exact Improved Core recipe/pattern and material counts;
- exact Improved Core x1.50 balance before release;
- exact Beacon brightness/focus alpha values;
- optional Amethyst pulse;
- final Core Chamber size/location if 4x4x4 causes model conflicts;
- exact new Cut-Obsidian chassis textures/models.
