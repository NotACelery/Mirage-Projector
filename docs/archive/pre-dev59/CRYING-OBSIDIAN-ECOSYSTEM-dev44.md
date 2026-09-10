# Crying Obsidian Ecosystem — authoritative design contract (dev.44)

> **Status:** authoritative dev.44 contract. Crying Obsidian Shards, renewable Small/Medium/Large/Mature crystal growth, exact stage harvesting, structure loot, Beacon excitation/transmission and residual-beam rendering are implemented in dev.44 source candidate. Values marked provisional still require Windows build + in-game balance/render QA. Improved-Core coupling and extended-light >15 integration remain future.
>
> **Supersedes:** the older dev.41 proposal that converted normal Obsidian into Crying Obsidian through `lava -> Obsidian -> pointed dripstone -> cauldron` and two intermediate Obsidian states. That conversion, its intermediate blocks, and its Jade progress bar are **retired** and must not be implemented.
>
> **Core principle:** the player must first obtain genuine Crying Obsidian through vanilla/exploration. Mirage then provides a slow renewable loop by growing Crying-Obsidian crystals from that source block.

---

# 1. Material naming and identity

## 1.1 Crying Obsidian Shard

The shared crafting/drop item is named:

**Crying Obsidian Shard**

This name replaces the earlier planned `Cut Obsidian Shard` name.

Reason:

- the item can still be produced by precision-cutting a Crying Obsidian block in a Stonecutter;
- it can also drop naturally from Crying Obsidian crystal buds/clusters;
- therefore `Cut` would incorrectly imply that every shard was manufactured.

Visual contract:

- slim, sharp shard silhouette with the readable triangular language of a Prismarine Shard, but an original sprite;
- dark volcanic-glass body: black, near-black purple and deep violet;
- strong but controlled arcane-magenta/lavender veins/highlights;
- must read as a sharp piece of Crying Obsidian, not as Amethyst recolored purple-black;
- pixel-art rules must remain Minecraft-native and crisp.

Internal/tag naming should use a stable Mirage namespace; historical docs or code comments that mention `Cut Obsidian Shard` must be treated as superseded terminology in future work.

---

# 2. Primary shard acquisition

## 2.1 Stonecutter

Frozen recipe:

```text
Stonecutter
1 Crying Obsidian -> 4 Crying Obsidian Shards
```

This remains the deterministic/manual conversion path.

Design intent:

- Stonecutter communicates precision cutting;
- one block immediately provides enough material to begin interacting with Mirage progression;
- crystal farming becomes the renewable path rather than forcing the player to destroy every Crying Obsidian block they obtain.

## 2.2 Re-forming Crying Obsidian

Frozen recipe family:

```text
S S S
S H S
S S S
```

- `S` = Crying Obsidian Shard
- `H` = heat catalyst

Accepted heat catalysts:

- Fire Charge; or
- Magma Cream.

Output:

```text
8 Crying Obsidian Shards + 1 heat catalyst -> 1 Crying Obsidian
```

This is intentionally not lossless relative to Stonecutter conversion:

```text
1 Crying Obsidian -> 4 shards
8 shards + heat -> 1 Crying Obsidian
```

The player can therefore multiply an initial Crying Obsidian source only by **growing additional shards**, not by repeatedly cutting/reforming the same block.

The heat-catalyst choice gives Blaze/Magma Cube/Slime-related progression a useful connection to the decorative/optical material loop.

---

# 3. Structure loot

**dev.44 implementation:** data-driven NeoForge Global Loot Modifiers are present for Ruined Portal, Abandoned Mineshaft and village armorer/toolsmith/weaponsmith chests.


Crying Obsidian Shards should appear as uncommon exploration loot.

## 3.1 Target rule

Inject small shard stacks primarily into structures where vanilla already associates the loot/environment with:

- Obsidian;
- Crying Obsidian;
- smithing;
- mining/mineral extraction.

Initial target categories:

- Ruined Portal chests;
- Abandoned Mineshaft chest minecarts;
- Village smith-related chests (toolsmith/weaponsmith/armorer-oriented pools);
- any additional Minecraft 1.21.1 structure loot table that already directly contains Obsidian or Crying Obsidian, reviewed individually before injection.

## 3.2 Balance rules

- Ruined Portals may be the most generous source because the association is direct;
- Mineshaft/smith loot should usually be smaller or rarer;
- loot supplements the progression but must not replace Crying Obsidian acquisition/farming;
- do not inject into every chest generically;
- exact weights/count ranges are implementation-balance values and must be checked against the actual 1.21.1 vanilla loot tables.

---

# 4. Renewable crystal growth

**dev.44 implementation:** present in source. Vanilla Crying Obsidian is made random-tick eligible by a narrow Mixin hook; nucleation requires a Lava source above and grows only into the block below.


## 4.1 Generator structure

A vanilla **Crying Obsidian** block becomes the permanent generator/source block when the following condition exists:

```text
[Lava source]
[Crying Obsidian]
[air / valid crystal growth space]
```

Rules:

1. a **lava source** must be directly above the Crying Obsidian;
2. the crystal grows from the **bottom face** of the Crying Obsidian;
3. the space directly below must be valid for the next crystal stage;
4. Pointed Dripstone and Cauldron are **not** used by this new system;
5. the Crying Obsidian source block is not consumed by growth;
6. removing the lava stops future growth but does not regress an existing bud/cluster;
7. restoring the valid structure allows growth to resume.

Narrative/visual logic:

> Lava continuously heats the volcanic glass from above. The Crying Obsidian slowly forms resonant crystalline growth on its cooler lower face, as if the block were literally "crying" solid shards downward.

## 4.2 Growth stages

Mirror vanilla Amethyst's readable four-stage vocabulary:

1. **Small Crying Obsidian Bud**
2. **Medium Crying Obsidian Bud**
3. **Large Crying Obsidian Bud**
4. **Crying Obsidian Cluster**

The geometry may deliberately follow the familiar vanilla Amethyst Bud/Cluster silhouette/attachment language, while using original Crying-Obsidian textures and emissive variants.

The mature `Cluster` name is valid: vanilla uses `Amethyst Cluster` for one mature block made of many visible crystal tips.

## 4.3 Facing / placement

- natural growth underneath Crying Obsidian is `FACING=DOWN` / outward from the source block;
- Silk-Touched buds/clusters should use Amethyst-like directional placement so they remain useful as decoration on valid faces;
- only a crystal positioned in the valid generator relationship under Lava + Crying Obsidian continues natural growth;
- decorative crystals placed elsewhere remain at their current age indefinitely.

---

# 5. Growth timing

**Current source balance (QA provisional):** nucleation succeeds on 1/32 eligible Crying-Obsidian random-tick opportunities; Small -> Medium 1/24, Medium -> Large 1/16, Large -> Mature 1/12 on eligible crystal random-tick opportunities. These are random-tick probabilities, not fixed real-time timers.


## 5.1 Design rule

The hardest step is **nucleation**: getting the first Small Bud to appear.

Once crystalline structure exists, later stages form increasingly quickly.

Frozen relative growth targets:

| Transition | Relative growth opportunity |
|---|---:|
| Empty -> Small | x1.00 — slowest |
| Small -> Medium | x1.50 |
| Medium -> Large | x2.00 |
| Large -> Mature Cluster | x3.00 — fastest |

The exact random-tick probability/time is intentionally not frozen until in-game balance.

Implementation requirements:

- centralize the probabilities/weights instead of scattering random constants through block code;
- do not use real-time timers that force ticking block entities if random ticks/block states can model the mechanic cleanly;
- preserve the ordering: `Empty -> Small` must remain clearly the bottleneck.

## 5.2 Automation philosophy

Automation is allowed; Mirage should not detect Create/Observers or punish specific mods.

The economic tradeoff is enough:

- harvesting immediately at Small always yields only 1 shard;
- waiting to Mature yields 4 shards;
- the Small stage is also the slowest stage to obtain.

Therefore a naive observer-driven harvester that breaks on the first growth event is renewable but inefficient by design.

A smarter/manual farm receives substantially more material per successful nucleation.

---

# 6. Harvesting and drops

**dev.44 implementation:** block loot tables use Silk-Touch alternatives and exact 1/2/3/4 shard counts with no Fortune bonus.


## 6.1 Without Silk Touch

Frozen drop rule: **one shard per age level**.

| Crystal stage | Shards dropped |
|---|---:|
| Small Bud | 1 |
| Medium Bud | 2 |
| Large Bud | 3 |
| Mature Cluster | 4 |

`Fortune` must not increase these counts in the initial design. This preserves the intended wait-vs-harvest decision and keeps the renewable loop predictable.

## 6.2 With Silk Touch

Silk Touch drops the actual current crystal block/stage.

Consequences:

- players can collect every stage for decoration;
- a Silk-Touched immature stage can be replaced beneath a valid Lava + Crying Obsidian generator and continue growing from its saved stage;
- a mature Cluster can be preserved for Beacon-refraction decoration instead of being converted to shards.

No intermediate "age item" beyond the actual Small/Medium/Large/Mature blocks is required.

---

# 7. Visual art direction

**dev.44 implementation:** each age has an original unenergized and energized 16×16 cutout texture/model. Art remains subject to visual QA; the contract below governs future polish.


## 7.1 Unenergized crystals

Base appearance:

- structure/readability comparable to vanilla Amethyst Bud/Cluster stages;
- material predominantly black / black-purple volcanic glass;
- arcane Crying-Obsidian purple should be substantially more pronounced than on the vanilla Crying Obsidian block;
- mature Cluster should have a strong central/focal tip and a dense multi-tip silhouette;
- do not simply hue-shift Amethyst textures; author Crying-Obsidian-specific fracture/vein patterns.

## 7.2 Energized variants

When intersecting an active Beacon beam, every age receives an energized visual state:

- much stronger internal lavender highlights;
- deep saturated Crying-Obsidian purple in outer crystal regions;
- near-white/lavender hottest points only where useful;
- emissive texture/material pass so the crystal visibly looks charged even if the world's block-light system is capped;
- the older/larger the stage, the more of the crystal appears energized.

Implementation may use blockstate/model-layer selection, BER/custom render, emissive model data, or another clean 1.21.1 NeoForge path; the visible contract matters more than one implementation technique.

---

# 8. Beacon interception model

**dev.44 implementation:** client Beacon rendering is replaced only for active columns containing Mirage crystals. Vanilla Beacon gameplay remains active; stained-glass color sections are retained while Mirage applies age-dependent visual transmission.


## 8.1 Beacon activation vs visual beam

Critical invariant:

> A Crying Obsidian crystal must **not deactivate the Beacon** merely because its visual beam is absorbed.

If the crystal were treated as an ordinary opaque Beacon-blocking cube by vanilla logic, the Beacon would disable, the crystal would stop receiving energy, then potentially reactivate in a loop.

Therefore separate:

- **Beacon gameplay validity/effects:** remains active through the crystal;
- **beam rendering:** Mirage attenuates or terminates the visual beam at the crystal according to crystal age.

The Beacon's status-effect mechanics/range are not reduced by the crystal in the initial design.

## 8.2 Vertical transmission by age

As the crystal grows, it traps more of the incoming light.

Frozen visual transmission targets:

| Stage | Approx. beam continuing upward | Approx. beam absorbed |
|---|---:|---:|
| Small | 75% | 25% |
| Medium | 50% | 50% |
| Large | 25% | 75% |
| Mature Cluster | **0%** | **100%** |

Rules:

- the incoming/continuing vertical beam retains its existing Beacon/stained-glass color;
- the crystal's own glow/refraction is Crying-Obsidian purple;
- mature Cluster visually behaves like a solid stopper: the vertical beam ends at the crystal and does not continue above it;
- if several crystals are stacked in one beam, only the light remaining from lower stages can visually reach upper stages; a Mature Cluster prevents any higher crystal from becoming Beacon-energized by that column.

The exact clipping plane should be aligned to the visible crystal intersection/AABB so the beam appears to enter the crystal rather than stopping conspicuously below it.

---

# 9. Beacon-powered block light

**dev.44 implementation:** crystals poll Beacon excitation every 20 ticks and toggle an `energized` blockstate. Vanilla fallback light is 14/15/15/15 by age. Extended >15 output is not bundled yet.


## 9.1 Intended light curve

The design intent is for the youngest powered crystal to be around Torch-level brightness and the mature Cluster to reach roughly **twice that effective intensity/range** if an extended-light engine is available.

Target extended-light curve:

| Stage | Intended powered light level |
|---|---:|
| Small | 14 |
| Medium | 18 |
| Large | 23 |
| Mature Cluster | 28 |

These values are gameplay/art targets, not vanilla-compatible literals above 15.

## 9.2 Vanilla fallback

Minecraft's vanilla block-light storage caps normal emission at 15.

Without an optional compatible extended-light provider:

| Stage | Vanilla-compatible fallback |
|---|---:|
| Small | 14 |
| Medium | 15 |
| Large | 15 |
| Mature Cluster | 15 |

Age progression beyond that cap must still remain obvious through:

- emissive strength;
- beam transmission;
- residual-beam frequency/length;
- charged texture intensity.

## 9.3 Optional extended-light integration

Mirage may later support an optional extended-light provider/library rather than making such a library mandatory.

Contract:

- Mirage must remain functional with no optional lighting mod;
- if a compatible provider is present, use the 14/18/23/28 target curve or later balance-equivalent values;
- isolate integration behind a small compatibility layer; do not leak third-party API types through core block logic;
- do not fork save/network semantics merely because the lighting provider is absent.

## 9.4 Purple lighting limitation

Vanilla Minecraft block light has intensity, not RGB color.

Therefore the initial implementation can guarantee:

- purple/lavender emissive crystal visuals;
- purple residual beams;
- purple visual glow/bloom where the rendering pipeline permits;
- normal world block-light intensity for spawn prevention.

Actual purple illumination cast onto neighboring block surfaces requires a colored-light/shader integration and is **not** a base requirement.

---

# 10. Residual escape/refraction beam

**dev.44 implementation:** deterministic client rendering with solid-block ray clipping is present. Current active phases are 20 ticks extend + 20 ticks full hold + 20 ticks retract/fade; stage cycles are currently 180/160/140/120 ticks Small->Mature.


The residual beam represents a thin path where trapped Beacon light temporarily finds a fracture in the Crying Obsidian and escapes sideways.

It is intentionally **not** a full Beacon beam.

## 10.1 Width

Target beam width:

- approximately **half the width of the vanilla Beacon's inner/core beam**;
- the outer glow may be even thinner/softer;
- it must read as a narrow residual leak, not as a second complete Beacon.

## 10.2 Color

The residual beam does **not** preserve stained-glass hue.

It uses Crying Obsidian's optical identity:

- deep dark violet/purple outer energy;
- brighter lavender/magenta inner light;
- restrained near-white highlight only near peak intensity if required.

This distinguishes:

- stained glass = colors the normal vertical Beacon beam;
- Improved Core = modifies/relays the vertical beam while preserving its color;
- Crying Obsidian crystal = absorbs the beam and leaks purple residual energy.

## 10.3 Number of active leaks

Initial design:

- maximum **one residual escape beam per crystal at a time**;
- no multi-beam burst in the first implementation;
- one active leak is visually readable and limits rendering cost.

## 10.4 Direction

Each leak chooses a new pseudo-random three-dimensional escape direction.

Requirements:

- avoid directions that are effectively identical to the main vertical Beacon axis;
- permit horizontal, diagonal and mildly upward/downward escape paths;
- direction must be deterministic/synchronized enough that multiplayer clients do not see wildly different beams for the same event;
- seed/event state can be server-synchronized or deterministically derived from BlockPos + event sequence/world time;
- perform a world collision/raycast so the visual beam stops at a solid obstacle instead of drawing through walls.

## 10.5 Maximum length by age

Mature target from design: **3–4 blocks**.

Suggested age scaling:

| Stage | Max residual leak target |
|---|---:|
| Small | ~1.0–1.5 blocks |
| Medium | ~1.5–2.5 blocks |
| Large | ~2.5–3.5 blocks |
| Mature Cluster | ~3.0–4.0 blocks |

Exact random ranges can be tuned in QA, but Mature must remain within the 3–4 block visual target unless later explicitly changed.

## 10.6 Animation lifecycle

The animation must communicate:

> A fracture opens, a strong burst escapes and reaches farther; the route remains open briefly; then the trapped light loses the path, becomes weaker and retreats back into the crystal.

Use three explicit phases.

### Phase A — breakthrough / extension

Target duration: about **0.75–1.0 s**.

- visible length grows from the crystal toward the randomly chosen endpoint;
- the beam segment that already exists is bright/strong rather than beginning as an invisible line;
- the leading tip may feather/fade slightly so extension is smooth;
- opacity/intensity approaches its peak as the beam reaches full length.

Conceptually:

```text
Cluster ==
Cluster ======
Cluster ============
Cluster ====================  target length
```

### Phase B — full-length hold

Target duration: **~1.0 s**.

- beam remains at its full chosen/collision-limited length;
- remains at or very near peak opacity;
- this short hold makes the completed escape path readable before collapse begins.

### Phase C — collapse / fade

Target duration: about **1.0 s**.

- beam gets progressively **shorter from the far endpoint toward the crystal**;
- global opacity simultaneously fades from strong/opaque toward transparent;
- intensity weakens continuously;
- at the end, length and alpha both reach zero.

Conceptually:

```text
100%  Cluster ====================
 70%  Cluster ===============
 40%  Cluster =========
 10%  Cluster ===
  0%  Cluster
```

This is a deliberate combination of geometric retraction and alpha decay; do not implement it as alpha-only fading of a permanently full-length beam.

## 10.7 Cooldown

After the beam collapses:

- wait a pseudo-random cooldown before choosing another escape direction;
- initial target: roughly **2–5 seconds** for a Mature Cluster;
- younger stages should leak less frequently than mature ones;
- exact ranges are balance/visual QA values.

## 10.8 Source lost mid-animation

If the Beacon stops powering the crystal during an active leak:

- no new leaks may start;
- current leak should enter a short collapse/fade rather than pop off in one frame;
- target emergency collapse around 0.2–0.4 s unless normal Phase C is already underway.

## 10.9 Gameplay role of residual beam

Initial release contract:

- visual only;
- does not damage entities;
- does not activate Beacon effects;
- does not carry redstone;
- does not itself place/emit block light along the entire ray.

The energized crystal block is the gameplay light source. This keeps the first implementation bounded.

---

# 11. Age-dependent Beacon personality

A larger crystal traps more energy, so several visual/mechanical parameters should move in the same direction:

| Property | Small -> Mature trend |
|---|---|
| Vertical beam transmission | decreases |
| Absorbed fraction | increases |
| Powered block light | increases |
| Emissive texture intensity | increases |
| Residual-beam maximum length | increases |
| Residual-beam frequency | increases |
| Residual-beam peak strength | increases modestly |

Do not make Small look like a fully mature optical reactor with only a different model size.

---

# 12. Obsidian Spike relationship

The previously defined **Obsidian Spike** remains a separate crafted trap that uses Crying Obsidian Shards.

Frozen ingredient budget:

- 3 Crying Obsidian Shards;
- 2 String;
- 1 Stick.

Frozen behavior/art direction remains:

- approximately half-block height;
- nine spike tips;
- thin central tip taller/sharper than the other eight;
- Crying-Obsidian palette;
- bush-like movement hindrance;
- 2.0 damage points (1 heart) per successful hurt event;
- normal hurt cadence/invulnerability prevents 2 damage every game tick.

The Spike is not a crystal-growth stage and does not interact with Beacon beams in the base design.

---

# 13. Possible future unrefined crystal Core

This is a **backlog concept**, not part of the first Shard/Crystal implementation and not one of the five Improved Cores.

Working concept:

**Unrefined Crying Crystal Core** / **Cluster Core** (final name TBD)

Fantasy:

- use a mature Crying Obsidian Cluster as an optical Core without refining/stabilizing it;
- it can power a projector, but the raw crystal refracts the projection imperfectly.

Desired projection signature:

- subtle deterministic refractive wobble/distortion;
- slight purple chromatic split/edge ghosting;
- small spatial shimmer rather than random frame-to-frame noise;
- visually obvious that the player is using an unrefined crystal;
- must not corrupt/modify stored image/entity/item snapshots themselves.

Power stats, PU, recipe, Core tier and whether it is a novelty or viable alternative remain **TBD**.

Do not implement this before:

1. normal Core Chamber renderer;
2. Crying Obsidian crystal growth;
3. five Improved Cores;
4. stable projection-render pipeline suitable for controlled distortion.

---

# 14. Retired ideas

The following are explicitly superseded and should be removed from current/authoritative roadmap text:

- normal Obsidian gradually turning into Crying Obsidian through Pointed Dripstone + Cauldron;
- two intermediate `Crying infusion` Obsidian block states;
- Jade progress bar for that retired conversion;
- treating shard renewability as dependent on converting unlimited normal Obsidian.

Historical documents may preserve these ideas for archaeology, but must not be used as implementation specifications.

---

# 15. Implementation order for this ecosystem

The dev.43 crystal/Beacon wave and dev.44 Obsidian Spike are implemented as source candidates. Remaining ecosystem work moves to projector upgrades and Improved Cores after QA.


Recommended sequence inside the broader Mirage roadmap:

1. **Crying Obsidian Shard** item, sprite, Stonecutter recipe and 8-shard re-form recipes;
2. four directional crystal growth blocks/models/textures and growth/drop logic;
3. structure loot injection for shards;
4. Silk Touch / no-Fortune harvest behavior and automation QA;
5. energized crystal visuals + Beacon detection/transmission attenuation;
6. vanilla light fallback and optional extended-light compatibility abstraction;
7. residual escape-beam renderer/animation/collision;
8. Obsidian Spike block/model/recipe/gameplay;
9. later, optional unrefined Cluster Core concept.

This ecosystem should land in recoverable snapshots rather than one enormous patch.

---

# 16. Frozen vs provisional values

## Frozen

- item name direction: `Crying Obsidian Shard` supersedes `Cut Obsidian Shard`;
- Stonecutter: 1 Crying Obsidian -> 4 shards;
- re-form: 8 shards + Fire Charge **or** Magma Cream -> 1 Crying Obsidian;
- renewable source requires Lava directly above Crying Obsidian and grows downward;
- four Amethyst-like ages: Small, Medium, Large, Mature Cluster;
- first nucleation is slowest; later stages progressively faster;
- no-Silk drops exactly 1/2/3/4 shards by age;
- Silk Touch drops the current crystal block/stage;
- Fortune does not increase shard count initially;
- Mature Cluster blocks 100% of the **visual** vertical Beacon beam while Beacon gameplay remains active;
- younger ages transmit progressively more visual beam: target 75/50/25/0%;
- intended extended powered light curve: 14/18/23/28;
- vanilla fallback is capped at 15;
- residual escape beam is purple Crying-Obsidian colored, approximately half Beacon-inner-beam width;
- one residual leak at a time;
- Mature residual length target 3–4 blocks;
- residual beam extends, holds full length ~1 s, then retracts while fading;
- Obsidian Spike is implemented with 3 shards + 2 String + 1 Stick, nine tips and 2.0 damage per successful hurt event;
- retired Obsidian->Crying dripstone/cauldron conversion must not be implemented.

## Provisional / QA balance

- exact random-tick probabilities for all growth transitions;
- exact per-age residual cooldown ranges;
- exact per-age residual lengths below Mature;
- exact extended-light provider/library, if any;
- exact normal/unpowered mechanical light emission of the crystal blocks;
- exact emissive texture intensity;
- exact collision ray behavior around transparent/non-full blocks;
- exact loot-table weights/counts;
- final name/stats/recipe of the future unrefined Cluster Core.


---

# Improved-Core -> Beacon -> Crystal coupling (future, frozen in dev.44)

This interaction is **defined but not implemented in dev.44**. Powered crystals exist now; Improved-Core relay modifiers do not. When the relay wave arrives, its final effective beam must be resolved before crystal response is calculated.

## Shared principle

A powered Crying Obsidian Bud/Cluster does not receive an abstract fixed amount of energy. Its visual response and mechanical light output are allowed to scale with the modified Beacon beam that actually reaches it.

Therefore, an Improved Core placed below the crystal may strengthen the crystal response if that Core changes the outgoing Beacon relay.

### Beam-width amplifier -> stronger crystal excitation

If an Improved Core makes the Beacon beam wider, a Bud/Cluster intersecting that enlarged beam must appear substantially more excited than the same age exposed to an unmodified vanilla-width beam.

The exact formula remains a balance task for the Beacon wave, but the following effects are required:

- stronger emissive texture/model response;
- stronger residual purple escape-beam presence;
- higher perceived energy stored in the crystal;
- no change to the crystal's growth age merely because the beam is wider.

A wider beam must not simply clip through the model while the crystal continues using the same visual brightness.

### Radiance/intensity amplifier -> stronger mechanical lighting

If an Improved Core increases Beacon radiance/intensity, the powered Bud/Cluster must also increase the amount of useful world light it provides.

Preferred implementation order:

1. investigate a maintained NeoForge 1.21.1-compatible extended-light provider/library;
2. if a safe optional provider exists, allow powered crystal values above vanilla block-light 15;
3. preserve a dependency-free fallback for ordinary Mirage installations.

The desired gameplay is **greater illuminated radius**, not merely a numerically brighter source pixel.

If the vanilla fallback cannot exceed light level 15, the preferred approximation is a slower spatial light-decay / auxiliary-light strategy so the mature crystal illuminates roughly twice the useful distance of a torch, rather than pretending that 15 is mechanically "twice as bright" as 14.

No custom light-engine rewrite is authorized merely to achieve this feature. If extended-range lighting cannot be implemented safely, keep the vanilla fallback and revisit the integration later.

### Color rule

The crystal's emitted/refracted visual effect remains Crying-Obsidian purple. Improved Cores do **not** steal the stained-glass role of controlling vanilla Beacon hue.

Mechanical block light may remain the engine's ordinary non-RGB light unless a future compatible colored-light provider is explicitly adopted.

### Stacking

When several Improved Cores relay the same beam, their capped Beacon modifiers are resolved first. The Bud/Cluster then reacts to the resulting effective beam. Do not multiply crystal effects independently once per Core; this prevents exponential brightness/width abuse.


---

# 11. Obsidian Spike — implemented dev.44

The Obsidian Spike is the first non-projector crafted utility for Crying Obsidian Shards.

Frozen recipe:

```text
 S 
S S
TKT
```

- `S` = Crying Obsidian Shard
- `T` = String
- `K` = Stick
- output = 1 Obsidian Spike

Physical/visual contract:

- approximately 8 model pixels / half-block visual height;
- nine independently modeled shard tips;
- center tip is taller and visibly thinner than the eight surrounding tips;
- Crying-Obsidian black/deep-violet material with strong arcane purple veins;
- no full collision cube: living entities can move into the trap instead of standing safely on a solid half slab;
- outline/selection envelope is approximately 14×8×14 model pixels.

Gameplay contract:

- living entities receive the same class of movement hindrance as Sweet Berry Bush movement drag;
- damage is movement-triggered rather than a free continuous 20-Hz damage loop;
- each successful `hurt(...)` event requests exactly **2.0 damage points = 1 heart**;
- Minecraft's normal hurt/invulnerability timing naturally prevents the 2.0 value from becoming 40 damage/second;
- horizontal or vertical movement through the trap can trigger damage, so dropping directly onto the points still counts;
- stationary living entities remain slowed but do not receive artificial repeated damage merely for standing still;
- items, XP and projectiles are ignored by trap movement/damage logic;
- the spike uses a Mirage-specific `obsidianSpike` DamageType/death message rather than pretending the damage came from a vanilla cactus or berry bush.

The block requires a supporting surface beneath it and breaks if that support disappears. This keeps it a floor trap rather than a floating decorative shard cloud.
