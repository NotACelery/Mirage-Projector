# Cores and Projector Upgrades — authoritative design contract (dev.42)

> **Status:** authoritative **planned** contract for projector material language, Core Chamber, crafting progression, state-preserving upgrades, Improved Cores and Improved-Core Beacon relays.
>
> **Implemented systems are still governed by current source + `CURRENT-IMPLEMENTATION-AUDIT-dev42.md`.** Sections explicitly marked future remain design contracts; dev.42 material/Core-Chamber items listed as implemented are present in source.
>
> Crying-Obsidian farming/crystal/Beacon-absorption details live in `CRYING-OBSIDIAN-ECOSYSTEM-dev42.md` and override older shard/conversion text.

---

# 1. Design goals

The new progression must:

1. give Mirage a recognizable material identity instead of broad generic Glass Pane layers;
2. turn the six current projectors into one upgrade family rather than six unrelated recipes;
3. make stronger chassis worth building without invalidating powerful Cores in smaller chassis;
4. preserve imported images/GIFs/cards/snapshots/settings through crafting upgrades;
5. give standard and Improved Cores a visual identity that matches their real installed item rather than fake material blocks;
6. make Improved Cores useful as projector components **and** decorative/Beacon-optical blocks;
7. remain compatible with the dev.38 Power architecture: Base PU × chassis efficiency × Core amplification.

---

# 2. Material language

Current/future visual language:

- **Obsidian** = structural chassis / durable frame;
- **Crying Obsidian** = advanced resonant volcanic-glass structure;
- **Crying Obsidian Shard** = precision optical/emitter component;
- **Glass** = transparent containment/optical chamber, not a broad flat chassis skin;
- **standard Core material** = raw energy source;
- **Improved Core nested shells** = optical amplification/stabilization around that same source.

`Cut Obsidian Shard` is retired terminology. The shared item is now **Crying Obsidian Shard** because it can be Stonecut or naturally grown/dropped.

Full shard acquisition/growth contract: `CRYING-OBSIDIAN-ECOSYSTEM-dev42.md`.

---

# 3. Current broad Glass model layers are deprecated

dev.42 replaces the dev.40/dev.41 thin/broad Glass/Glass-Pane surfaces with composite solid structure + translucent Crying-Obsidian emitter geometry + a universal small Glass Core Chamber. The older ghost-sheet design is retired.

This is **not final art**.

Future model rules:

- remove broad flat Glass sheets from all six chassis;
- do not fix them by merely swapping the texture to Crying Obsidian while keeping coplanar geometry;
- emitter rails/plates should use custom Crying-Obsidian-derived material with enough opacity, edge definition and thickness to read from all camera angles;
- actual transparent Glass should primarily live in the Core Chamber;
- Field should visibly use whole Crying Obsidian more strongly than the smaller chassis.

---

# 4. Universal Core Chamber

## 4.1 Purpose

Replace the temporary renderer that maps raw Core items to full material-block visuals.

Bad/legacy examples:

```text
Diamond -> Diamond Block visual
Netherite Ingot -> Netherite Block visual
```

That behavior is explicitly deprecated.

## 4.2 Chamber model

Target chamber:

- approximately **4×4×4 px**;
- bounded transparent Glass enclosure;
- visually seated inside/above the chassis emitter;
- enough thickness/edge definition that it cannot disappear as a one-pixel glass sheet;
- universal dimensions where practical; each chassis mainly chooses chamber position/orientation.

## 4.3 Installed Core render

Inside the chamber:

- render the **real installed ItemStack/model**;
- use uniform scale rather than non-uniformly squeezing a material block;
- slow Y-axis rotation;
- extremely subtle vertical bob;
- elevated/fullbright-like rendering may be used if needed to communicate optical excitation;
- bounds/culling must include the entire animated item from all camera angles.

Target motion:

- one full rotation roughly every 5–6 seconds;
- bob roughly 0.3–0.5 px peak range;
- exact values are visual QA values, not gameplay mechanics.

## 4.4 Improved Core inside chamber

An Improved Core uses its own recognizable nested-shell block/item model miniaturized in the chamber.

The renderer must not require special per-material fake blocks. The installed stack itself is authoritative visual content.

---

# 5. Projector crafting progression

## 5.1 Progression graph

Only the smallest Mirage Projector is crafted directly from raw materials.

Frozen graph:

```text
Mirage Projector
      |
      v
Mirage Display
   |    |    |    |
   v    v    v    v
 Wide  Tall Prism Field
```

Consequences:

- Display is a mandatory intermediate chassis;
- advanced machines cannot bypass early Mirage progression;
- Wide/Tall/Prism/Field are specializations/upgrades of the same machine family;
- state-preserving crafting becomes a core technical requirement.

This supersedes old plans for six unrelated direct recipes.

---

# 6. Base Mirage Projector recipe

Frozen Beacon-inspired pattern:

```text
S S S
S G S
O O O
```

- `S` = Crying Obsidian Shard
- `G` = Glass Block
- `O` = Obsidian

Totals:

- 5 Crying Obsidian Shards;
- 1 Glass Block;
- 3 Obsidian.

The Glass Block represents the central optical/Core Chamber. Shards form the fine optical/emitter structure that broad Glass Panes used to represent poorly.

## 6.1 Starter Core decision — still open

Current source historically gives Compact a Glass Core automatically/migrates older worlds into one.

The new recipe does **not yet settle** whether a newly crafted Mirage Projector should:

A. start with an empty Core socket, where the central Glass Block is chamber-only; or
B. include a removable starter Glass Core, with recipe cost intentionally covering it.

Do not silently choose during recipe implementation. Resolve this explicitly before dev.45-style crafting work. Existing save migration may still need the historical fallback regardless.

## 6.2 Glowstone brainstorm — explicitly not a contract

Glowstone has been mentioned as a possible future optical/Core/crafting material.

Current decision:

- do **not** replace the standard Glass Core with Glowstone;
- do **not** add Glowstone to recipes merely to give it a use;
- no mechanic is assigned to Glowstone yet;
- reconsider only if a clear gameplay/visual role appears.

This note exists specifically so a future chat does not mistake a brainstorm for a frozen requirement.

---

# 7. Mirage Projector -> Mirage Display

Frozen recipe:

```text
Q S A
S M S
A S Q
```

- `Q` = Quartz
- `A` = Amethyst Shard
- `S` = Crying Obsidian Shard
- `M` = Mirage Projector

Output: **Mirage Display**.

Totals:

- 2 Quartz;
- 2 Amethyst Shards;
- 4 Crying Obsidian Shards;
- 1 Mirage Projector carrying its persistent state.

Design logic:

- Quartz suggests optical precision;
- Amethyst suggests resonance;
- shard frame expands/refines the emitter;
- Display is the mandatory gateway to all larger/specialized chassis.

---

# 8. Display -> Prism

Frozen pattern:

```text
S G S
G D G
S G S
```

- `S` = Crying Obsidian Shard
- `G` = Glass Block
- `D` = Mirage Display

Output: **Mirage Prism**.

The four Glass Blocks directly communicate four optical faces around the existing Display.

Prism remains a four-sided N/E/S/W projector; this recipe does not imply Wide/Tall-style stacking.

---

# 9. Display -> Wide / Tall

Hard rules:

- both consume one Mirage Display;
- both have **equivalent total material cost**;
- recipe geometry should visually indicate horizontal vs vertical expansion;
- neither is a cheaper path to the other's PU tier;
- source data/settings survive the upgrade.

Preferred symmetric candidate pair (not frozen until model/balance QA):

### Wide candidate

```text
S S S
G D G
S S S
```

### Tall candidate

```text
S G S
S D S
S G S
```

Where:

- `S` = Crying Obsidian Shard;
- `G` = Glass Block;
- `D` = Mirage Display.

Both candidates cost:

- 6 shards;
- 2 Glass Blocks;
- 1 Display.

The arrangement, not the material budget, communicates orientation.

These remain **preferred candidates**, not frozen recipes, until the refreshed models make the geometry/material relationship clear.

---

# 10. Display -> Field

Field is the large/heavy structural specialization.

Frozen requirements:

- consumes one Mirage Display;
- significantly more expensive than Wide/Tall/Prism;
- uses **whole Crying Obsidian blocks**, not only shards;
- model must expose Crying Obsidian prominently in the chassis base/structure;
- upgrade preserves projector state.

Preferred candidate:

```text
C S C
S D S
C S C
```

- `C` = Crying Obsidian
- `S` = Crying Obsidian Shard
- `D` = Mirage Display

Candidate totals:

- 4 Crying Obsidian;
- 4 shards;
- 1 Display.

The exact Field recipe remains balance-provisional until renewable Crying Obsidian farming and final models are playable together.

---

# 11. State-preserving projector upgrade recipes

This is a **hard technical contract**.

Crafting an existing projector into a stronger chassis must not create a clean/default ItemStack that silently loses player data.

## 11.1 Data that must survive

At minimum:

- installed Core ItemStack;
- SourceMode;
- image/GIF asset IDs;
- Front/Back references and source dimensions;
- Wide/Tall persisted source-bank compatibility data;
- Image layout and face modes;
- Entity Scan card/snapshot;
- frozen Player/entity appearance data;
- Humanoid virtual armor/main/off-hand snapshots;
- Horse equipment snapshots;
- Item projection snapshot;
- Banner snapshots;
- Scale;
- Lift;
- Float;
- rotation enabled/direction/speed/offset;
- Floating mode/cycle;
- Lighting;
- Ghost;
- Tint;
- Scanlines;
- flip/readable/mirrored/independent-face settings;
- future persistent projector fields unknown today.

## 11.2 Architecture rule

Do **not** hand-copy a growing list of fields in each recipe implementation.

Create one canonical transferable projector-state representation/component/data bundle.

Upgrade flow:

```text
input projector ItemStack
      ↓
extract canonical persistent projector state
      ↓
create target chassis ItemStack
      ↓
copy state wholesale
      ↓
run target-chassis compatibility normalization
      ↓
result
```

## 11.3 Compatibility normalization examples

- Display SINGLE image -> Wide remains SINGLE with same source/settings;
- Display SINGLE -> Tall remains SINGLE;
- Display -> Prism keeps the primary image and maps it through the defined Prism initial-face migration rather than deleting it;
- Entity/Card/Humanoid gear is chassis-independent and survives all upgrades;
- installed Core survives;
- imported asset references must never disappear because a machine is upgraded;
- if a new chassis has more capability than the old one, do not auto-scale/auto-overdrive the existing projection; preserve the configured values.

A custom recipe serializer/assembly path is expected.

---

# 12. Standard Core / Power baseline

Current standard Core materials and dev.38 Base PU remain:

| Standard Core | Base PU | Amplification |
|---|---:|---:|
| Glass | 32 | ×1.00 |
| Quartz | 48 | ×1.00 |
| Amethyst | 64 | ×1.00 |
| Diamond | 96 | ×1.00 |
| Netherite | 128 | ×1.00 |

Effective capacity remains:

```text
floor(Base Core PU × Chassis multiplier × Core amplification)
```

Improved Cores must extend this architecture rather than introducing a second unrelated power system.

---

# 13. Five Improved Projection Cores

Exactly five Improved Cores are planned, one per standard material:

1. Improved Glass Core
2. Improved Quartz Core
3. Improved Amethyst Core
4. Improved Diamond Core
5. Improved Netherite Core

This replaces the earlier idea of separate named families such as Amplified/Focused/Stabilized/Resonant variants for every material.

Material identity itself carries the distinctive world/Beacon behavior.

## 13.1 Projector effect

Frozen architecture:

- same Base PU as corresponding standard material;
- additional Core amplification multiplier;
- initial balance target approximately **×1.50**;
- chassis multiplier continues normally;
- PU costs/Overdrive are unchanged;
- Improved Core does not bypass Power validation.

Example target:

```text
Netherite base PU = 128
Field multiplier = 4.00
Improved amplification = 1.50

Effective PU = floor(128 × 4 × 1.50) = 768
```

`×1.50` is still a balance target, not a sacred number; it must be tested after chassis/crafting progression exists.

---

# 14. Improved Core block/item model

Every Improved Core is both:

- a Core installable in a Mirage projector;
- a placeable decorative/optical block.

Model contract:

- **outer shell:** dark-purple translucent glass-like cube aligned to world axes;
- **middle shell:** separate cube genuinely rotated in geometry;
- **inner shell:** smaller aligned containment cube;
- core material/item suspended at exact center;
- visible space between shells to prevent z-fighting/flat nested-box appearance;
- original textures/model language may evoke tesseract/End-Crystal optical containment without copying another asset.

The genuinely rotated middle volume is a hard requirement. It must not be faked only through diagonal texture lines.

---

# 15. Improved Core crafting

Preferred shared candidate pattern:

```text
G S G
S C S
G S G
```

- `G` = Glass Block
- `S` = Crying Obsidian Shard
- `C` = corresponding standard Core material/item

Output: matching Improved Core.

Candidate totals:

- 4 Glass Blocks;
- 4 Crying Obsidian Shards;
- 1 base Core material/item.

Why it fits:

- Glass = three-shell containment language;
- shards = Crying-Obsidian optical frame/amplifier;
- center = same energy source, preserving Base PU identity.

This is **preferred but not frozen** until:

1. Crying Obsidian renewable loop is playable;
2. projector upgrade recipes have actual costs;
3. Improved Core ×1.50 balance is tested.

Special note for Improved Glass Core: using Glass as both shell material and center is acceptable thematically but final material count/recipe readability must be checked in-game.

---

# 16. Improved Cores in Beacon beams

Improved Cores are optical **relays/amplifiers**, unlike Crying Obsidian Clusters which absorb/terminate the beam.

## 16.1 Universal relay geometry

When a placed Improved Core intersects an active Beacon beam:

- incoming beam reaches the Core;
- incoming segment visually terminates at the block's middle horizontal plane `Y + 0.5` (between pixels 8 and 9);
- outgoing segment begins at that same plane;
- outgoing beam is wider/more visually powerful;
- current Beacon/stained-glass color is preserved;
- the Core never replaces stained glass as the hue mechanism.

Conceptually:

```text
        normal incoming beam
               |
         [Improved Core]
------------- y+0.5 -------------
             |||
             ||| wider outgoing beam
             |||
```

This must read as one continuous optical relay, not disconnected beams.

---

# 17. Material-specific Beacon effects

Every Improved Core widens the outgoing beam. Its material adds one secondary identity.

## 17.1 Improved Glass Core — Diffusion

Purpose: strongest expansion / broad decorative beam.

Target:

- strongest width contribution;
- softer/broader outer beam;
- does not receive the strongest brightness/motion effect.

Initial width contribution target: **+35 percentage points** of vanilla width before global cap.

## 17.2 Improved Quartz Core — Radiance

Purpose: visibly more luminous beam.

Target:

- standard width contribution;
- inner beam appears brighter/hotter;
- outer beam more readable;
- hue unchanged.

Initial width contribution: **+25 percentage points**.

Exact luminance/alpha multiplier is renderer-balance TBD.

## 17.3 Improved Amethyst Core — Resonance

Purpose: motion/resonance.

Target:

- standard width contribution;
- faster beam rotation;
- optional very subtle width pulse only if visually clean.

Initial targets:

- width +25 percentage points;
- rotation speed +25% per effective Amethyst Core;
- rotation-speed cap about **2× vanilla**.

## 17.4 Improved Diamond Core — Focus

Purpose: concentrated bright center inside a larger beam.

Target:

- standard overall width increase;
- stronger/more defined inner beam relative to outer layer;
- reads as concentrated high-energy light while still obeying universal wider-beam rule.

Initial width contribution: **+25 percentage points**.

Exact inner/outer width/alpha ratios are renderer QA values.

## 17.5 Improved Netherite Core — Inversion

Purpose: unmistakable reversal behavior.

Target:

- standard width increase;
- reverses outgoing beam rotation direction;
- may feel slightly heavier/slower if visually useful, but reversal is primary identity;
- multiple Netherite Cores do **not** toggle/cancel one another: if one or more effective Netherite effects are present, the affected outgoing beam remains reversed.

Initial width contribution: **+25 percentage points**.

---

# 18. Improved-Core stacking

Frozen anti-runaway rules:

- at most **4 Improved Cores** in one Beacon column contribute effects;
- additional Improved Cores can remain physical/decorative but add no further amplifier effect;
- secondary effects from different materials combine;
- beam width is additive/capped, not multiplicative runaway;
- global final width target cap ≈ **200% vanilla**.

Initial width model:

```text
non-Glass Improved Core: +25 percentage points
Glass Improved Core:     +35 percentage points
final width cap:          200% vanilla
```

Examples:

- Quartz + Amethyst = wider + brighter + faster rotation;
- Quartz + Amethyst + Netherite = wider + brighter + faster + reversed;
- four Glass cores still clamp at global width cap.

Centralize stack aggregation so multiple render paths cannot calculate different beam state.

---

# 19. Improved Core vs Crying Obsidian Cluster

Do not merge these mechanics.

| Block | Beacon relationship |
|---|---|
| Stained Glass | changes beam hue, passes beam |
| Improved Core | relays/amplifies beam, preserves hue, continues upward |
| Crying Obsidian Bud/Cluster | absorbs/attenuates beam, glows purple, mature stage terminates visual vertical beam and leaks residual side light |

Detailed crystal behavior lives in `CRYING-OBSIDIAN-ECOSYSTEM-dev42.md`.

This distinction is a core part of the worldbuilding/gameplay readability.

---

# 20. Future Scan Codex — 1.1.0+

This remains deliberately outside current 0.1.x scope.

Problem today:

```text
20 identical projections
-> potentially 20 Empty Scan Templates / repeated scans
```

Future flow:

```text
Entity
  -> scan once into Mirage Scan Codex
  -> persistent searchable/filterable library

Codex entry + Paper
  -> dedicated copy/printing station
  -> physical Entity Scan Card
  -> existing projector card slot
```

Desired Codex capabilities:

- search;
- filters;
- Player vs mob/category organization;
- favorites;
- stored name/type preview;
- duplicate cards without needing the original entity again.

The current physical Entity Scan Card remains the projector-facing format, minimizing projector rewrites.

Do not begin this before the current chassis/Core/progression/rendering work stabilizes. Target **1.1.0 or later**.

---

# 21. Implementation sequence

This document is one part of the broader roadmap in `CURRENT-STATE-ROADMAP-dev42.md`.

Within the chassis/Core branch:

1. Crying Obsidian Shard exists first;
2. refresh six chassis visual language + Core Chamber;
3. implement canonical projector-state transfer infrastructure;
4. base Mirage recipe;
5. Mirage -> Display recipe;
6. Display -> Wide/Tall/Prism/Field recipes;
7. balance costs with renewable Crying-Obsidian ecosystem;
8. implement five Improved Core blocks/items/models;
9. connect Improved amplification to existing Power formula;
10. implement Beacon relay/effects/stacking;
11. only then consider optional Unrefined Crying Crystal Core.

Each step should produce a recoverable source snapshot and dedicated QA before the next large dependency.

---

# 22. Frozen vs provisional

## Frozen

- `Crying Obsidian Shard` supersedes `Cut Obsidian Shard` terminology;
- broad Glass-Pane chassis layers are deprecated;
- Glass mainly represents Core Chamber containment;
- Core Chamber uses real installed ItemStack at small uniform scale with slow rotation/bob;
- progression graph is Mirage -> Display -> Wide/Tall/Prism/Field;
- base Mirage recipe `SSS / SGS / OOO`;
- Display recipe `QSA / SMS / ASQ`;
- Prism recipe `SGS / GDG / SGS`;
- Wide/Tall must cost equally;
- Field must use whole Crying Obsidian and be significantly more expensive;
- upgrades preserve canonical projector state wholesale;
- exactly five Improved Cores, one per current standard material;
- Improved Core preserves Base PU and adds amplification; target around ×1.50;
- Improved Core model uses three nested shells with genuinely rotated middle shell;
- Beacon relay starts new beam at Y+0.5, widens it and preserves incoming color;
- Improved Glass/Quartz/Amethyst/Diamond/Netherite identities are Diffusion/Radiance/Resonance/Focus/Inversion;
- max four effective Improved Cores; global width target cap ~2×;
- Scan Codex remains 1.1.0+.

## Provisional / balance/art validation

- starter Glass Core vs empty socket in newly crafted base projector;
- exact Wide/Tall recipe pair (preferred candidates documented above);
- exact Field recipe (preferred candidate documented above);
- exact Improved Core recipe/material counts;
- exact Improved amplification value after ×1.50 testing;
- exact Core Chamber dimensions/position if 4×4×4 conflicts with final models;
- precise new emitter texture/translucency;
- exact Improved Beacon brightness/focus/pulse rendering constants;
- any future Glowstone role (currently none).


---

# Powered crystal coupling (future Beacon integration)

Improved Core Beacon effects must feed into the powered Crying Obsidian Bud/Cluster system instead of ending at the visual beam renderer.

- Width-oriented amplification: a crystal intersecting the widened beam becomes visibly more energetic and produces stronger residual refraction.
- Radiance/intensity-oriented amplification: the crystal's useful emitted light increases when technically possible.
- Preferred high-range implementation: optional maintained extended-light integration on NeoForge 1.21.1.
- Dependency-free fallback: vanilla light level remains capped; approximate the intended benefit with greater effective illumination range only if it can be done without invasive global-light-engine changes.
- Resolve stacked Improved-Core modifiers into one effective beam first, then derive the crystal response from that result.
- Stained glass remains responsible for Beacon hue. Core/crystal effects must not overwrite that contract.

See `CRYING-OBSIDIAN-ECOSYSTEM-dev42.md` for the authoritative crystal-side rules.
