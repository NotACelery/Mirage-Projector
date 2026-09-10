# dev.56 — Beacon optics rework

**Baseline:** dev.55 Core Booster cleanup.

- Cluster residual ray origin is world-space crystal center before orientation.
- Mature ray width is halved; mature ray length is reduced to ~34% of dev.55.
- Ray lifecycle: instant full-length -> 20 tick hold -> 20 tick retract/fade.
- Stage residual scaling: MATURE 1.00, LARGE 0.75, MEDIUM 0.50, SMALL 0.25; cycle periods are 120/160/240/480 ticks respectively.
- Vertical Beacon rendering never crosses a Crying Obsidian block volume: stop at bottom face/pixel 0, skip one full block, resume above only when transmission remains.
- SMALL/MEDIUM/LARGE transmit 75%/50%/25% and no longer emit block light. MATURE transmits 0% and remains level-15 when energized.
- A MATURE cluster directly above a Beacon cancels the vertical beam completely. Dynamic Beacon block-light suppression is hooked conservatively and the light engine is explicitly rechecked on cluster state/place/remove transitions.
- Protocol remains 18.

---

# dev.55 — Core Booster cleanup + authoritative material state

**Baseline:** dev.54 Core Booster redesign.

- The five old Improved Core items are no longer registered at all. Only `mirage_projector:core_booster` is user-facing.
- Legacy block IDs remain registered solely to migrate old QA-world placements; their BlockEntity schedules immediate replacement with a materialized Core Booster.
- Booster material is represented by `CoreBoosterBlock.MATERIAL` and is the authoritative renderer source.
- BlockEntity NBT remains only for loaded ItemStack persistence and dev.54 migration compatibility.
- Extraction must always result in `material=empty` and a visually empty center without needing any unrelated block update.
- Protocol remains 18.

---

# dev.54 — Core Booster redesign

- Consolidates the five user-facing Improved Core blocks into one **Core Booster** ID.
- Empty Booster recipe: `GSG / S S / GSG`; the Core material is inserted manually after placement.
- Accepted insert materials: Glass, Quartz, Amethyst Shard, Diamond, Netherite Ingot.
- Right-click inserts; Shift + right-click extracts.
- Loaded Booster ItemStacks persist their material through `BLOCK_ENTITY_DATA`, so only identical material variants stack together.
- Correct pickaxe mining returns one stateful Booster item; Silk Touch is not required.
- `anidado.json` is the canonical four-cube shell geometry.
- Center material uses one synchronized BER hologram and should be visible immediately after world/chunk load.
- Jade support is optional and compile-only.
- Legacy dev-build Improved Core IDs remain hidden only to avoid deleting blocks/items in ongoing QA worlds.

---

# dev.51 — Crying growth + Improved Core QA correction

**Baseline:** dev.50 compiled and booted successfully in the user's Windows modpack. dev.50 also proved the `BlockBehaviour.BlockStateBase` Crying Obsidian random-tick hook is live because a bud eventually spawned; dev.51 therefore adjusts eligibility/rate rather than replacing that working dispatch path.

- Crying Obsidian accepts any `FluidTags.LAVA` state directly above it (source or flowing).
- Initial nucleation gate is 1/5 per eligible random tick.
- Bud/cluster baked models are restored exactly to the pre-regression amethyst-style `minecraft:block/cross` + `minecraft:cutout` form.
- Crying Obsidian Shard uses the exact 16x16 prismarine-shard alpha silhouette with the retained Mirage purple gradient.
- Obsidian Spike GUI transform is reduced to stay inside the inventory slot.
- Improved Core shell now occupies the full 16x16x16 block; the fake three-plane center was removed.
- Placed Improved Cores now use one stateless BlockEntity and one BER-rendered material item, using the same clockwise `FIXED`/18-degree-tilt/0.32-scale presentation as the projector's installed Core.
- Protocol remains 18.

# dev.50 — Client mixin boot-crash hotfix

Real modpack QA of dev.49 reached the client mod-construction phase, which confirmed the Crying Obsidian crash from dev.48 was gone. A separate dev.47 client rendering redirect then failed because `RenderLayerMixin` could not resolve its target method in production mappings.

For dev.50 that redirect is removed completely. We do **not** downgrade it to `require = 0`: a silently dead mixin would make the collar fix look implemented while doing nothing. Secondary-layer ordering stays a tracked visual issue until it can be solved against a verified 1.21.1 render path.

The dev.49 `BlockBehaviour.BlockStateBase` random-tick hooks are carried forward unchanged.

**Baseline:** dev.48 rejected at runtime because Mixin could not resolve `Block#randomTick`.

The nucleation path now lives entirely in `BlockBehaviour.BlockStateBase`: eligibility is forced through `isRandomlyTicking`, and actual nucleation is injected at `randomTick(ServerLevel, BlockPos, RandomSource)`. This avoids relying on a `Block` declaration that does not exist and hooks the server's real state dispatch path.

# dev.46 — Improved Projection Cores

**Baseline:** dev.45 Windows `build.bat` confirmed successful by the user. dev.46 remains source-only until its own Windows build.

## Implemented source candidate

- Five physical Core blocks/items: `improved_glass_core`, `improved_quartz_core`, `improved_amethyst_core`, `improved_diamond_core`, `improved_netherite_core`.
- Same Base PU as the standard material; initial amplification multiplier `x1.50`.
- Existing `ProjectionPower` formula remains authoritative: `floor(Base PU × chassis multiplier × Core amplification)`.
- All five Improved Core BlockItems are accepted by the existing physical Core slot and persist through projector state packing/upgrades exactly like standard Cores.
- Shared nested-shell model family: aligned outer shell, genuinely Y-rotated 45-degree middle volume, aligned inner shell, centered material/item cards; all three shells have physical spacing.
- Preferred recipe candidate is now playable for QA: `GSG / SCS / GSG`, where `G=Glass`, `S=Crying Obsidian Shard`, `C=matching base Core material`.
- Beacon relay/effect identities are not activated in dev.46; that remains the next dependency after Core block/power QA.
- Protocol stays `18`.

## Metadata invariant

- Public mod author: **Celerbi**.
- `neoforge.mods.toml` description stays repository-neutral and contains no repository URL/reference.
- Do not reintroduce `a different account identity` as the displayed developer/author identity.

## QA required before build-clean

1. Windows `build.bat`.
2. Confirm all five blocks/items/models load without missing-model or texture warnings.
3. Place/break every Improved Core and confirm self-drop.
4. Confirm middle shell is visibly geometric/rotated rather than only a texture illusion.
5. Install each Improved Core in Compact/Display/Field and verify Base PU is unchanged while amplification reads x1.50.
6. Verify expected effective capacities, especially Netherite + Field = `floor(128 × 4 × 1.50) = 768 PU`.
7. Pack/break/re-place a projector carrying an Improved Core and upgrade its chassis; Core must survive.
8. Confirm standard Cores remain x1.00 and all prior dev.45 state-preserving recipes still work.
9. Visual QA for nested transparency, z-fighting, GUI icon/readability and Core Chamber miniaturization.

# Mirage Projector — Development Guide (dev.45)

## 1. Baseline

- Minecraft **1.21.1**
- NeoForge **21.1.244**
- Java **21**
- Gradle **9.2.1**
- Parchment **2024.11.17**
- Mod version **0.1.0-dev.45**
- Network protocol **18** (`MirageProjector.NETWORK_PROTOCOL`)

`dev.45` adds stateful projector ItemStacks and canonical chassis-upgrade crafting on top of dev.43 crystals and dev.44 Obsidian Spike. Protocol remains 18 because this uses vanilla ItemStack BlockEntity data and a recipe serializer rather than changing Mirage custom packet schemas.

Current QA facts:

- dev.40 ran in-game;
- Piglin shaking fix is confirmed;
- dev.42 replaced the legacy Core block visual and broad Glass sheets in source;
- dev.43 adds four crystal stages, renewable growth, exact stage drops, structure loot and custom Beacon optics;
- dev.44 adds the Obsidian Spike trap;
- dev.45 packs projector BlockEntity state into dropped items and implements Mirage -> Display -> specialist upgrade crafting;
- first Windows dev.45 compile attempt reached `:compileJava` and failed on two source/import root causes: `ClipContext` was imported from `net.minecraft.world.phys` instead of `net.minecraft.world.level`, and `MirageProjectorBlock` used `Block.popResource` without importing `net.minecraft.world.level.block.Block`;
- this dev.45 compile-repair snapshot fixes both reported causes; Windows rebuild + in-game confirmation are still pending.

Start any recovery/new chat with:

1. `docs/DOCUMENTATION-AUTHORITY-dev44.md`
2. `docs/CURRENT-STATE-ROADMAP-dev44.md`
3. `docs/CURRENT-IMPLEMENTATION-AUDIT-dev44.md`

---

# 2. Implemented chassis/Power invariants

Active `ProjectionChassisProfile` values:

- COMPACT
- DISPLAY
- WIDE
- TALL
- FIELD
- PRISM

Do not add placeholder enum values without real registered gameplay blocks and compatibility review.

Nominal values:

| Chassis | W×H | Lift | Float | Power multiplier |
|---|---:|---:|---:|---:|
| Compact | 10×10 | 32 | 4 | ×1.00 |
| Display | 32×32 | 48 | 12 | ×1.50 |
| Wide | 80×32 | 64 | 12 | ×2.00 |
| Tall | 32×80 | 96 | 16 | ×2.00 |
| Field | 128×128 | 144 | 24 | ×4.00 |
| Prism | adaptive 48×48 baseline | 96 | 12 | ×2.00 |

Nominals are efficiency targets, not hard caps.

Standard Core Base PU:

- Glass 32
- Quartz 48
- Amethyst 64
- Diamond 96
- Netherite 128

```text
capacity = floor(basePU × chassisMultiplier × coreAmplification)
```

Standard amplification = ×1.00. dev.38 Overdrive/PU/Ghost behavior remains authoritative.

---

# 3. Facing

`MirageProjectorBlock.FACING` is horizontal Furnace-like placement.

- Plane projections follow FACING;
- Wide/Tall shapes rotate where appropriate;
- Prism N/E/S/W Image/Banner face semantics remain world-cardinal.

Do not regress blocks to static orientation.

---

# 4. Image/GIF invariants

`ImageSourceBank`:

```text
ACTIVE_MULTI_SLOTS = 4
PERSISTED_COMPAT_SLOTS = 9
```

- slots 0–3 = active Wide/Tall MULTI;
- 4–8 = dev.33–37 compatibility only;
- Field never uses the bank as an active grid.

Supported import:

- PNG/JPEG/WebP/BMP static;
- GIF animated;
- Animated WebP/APNG detected/rejected explicitly.

Decoder selection is based on content, never extension.

`ProjectionImageSizing` remains shared authority for Wide/Tall SINGLE renderer + PU dimensions.

Prism remains four independent cardinal faces, adaptive aspect, no stacking.

---

# 5. Entity invariants

Projection entities:

- are client render-only;
- are never spawned into world entity lists;
- do not run AI/tick;
- use frozen scan state;
- keep virtual equipment separate from physical staging;
- purge incompatible virtual slot groups when entity family changes;
- normalize temporary Piglin/Hoglin zombification state in `EntityProjectionClientEntityFactory` only.

Do not reintroduce dimension hacks in individual renderers.

---

# 6. Render-order/depth invariants

- Entity projections use the dedicated deferred Mirage pass/buffer;
- never call `endBatch()` on Minecraft's global `bufferSource()` from Mirage Entity flushing;
- Ghost requires depth test but must not reintroduce depth write that cuts water/translucent geometry;
- Image/Banner/Item must remain isolated from Entity flush side effects.

Any render-pipeline edit requires a source snapshot and explicit Entity/projector/water regression QA.

---

# 7. GUI invariants

- main projector screen owns global presentation + Core/Power;
- Workspaces own SourceMode-specific content;
- Preview renderers do not draw Screen labels/layout;
- Power detailed tooltip opens only from its dedicated `?` hotspot;
- reserve a physical Core-slot column;
- capacity/load bar remains visible;
- never add labels without reserving their rectangle — GUI collision has regressed repeatedly.

---

# 8. Compatibility/migration invariants

Do not delete old-looking code before identifying the migration it protects.

Deliberate compatibility includes:

- 9-slot persisted Image bank for dev.33–37;
- legacy `.png` asset files pre-dev.39;
- older Entity scan versions;
- old item/equipment recovery paths;
- historical pre-Core migration as applicable.

NBT/wire cleanup must state exactly which old worlds/cards/assets it stops supporting.

---

# 9. Crying Obsidian ecosystem

Authority: `docs/CRYING-OBSIDIAN-ECOSYSTEM-dev44.md`.

Implemented/current contract:

- final shared item name is `Crying Obsidian Shard`, not `Cut Obsidian Shard`;
- Stonecutter: 1 Crying Obsidian -> 4 shards;
- 8 shards + Fire Charge or Magma Cream -> Crying Obsidian;
- renewable growth accepts any Lava fluid state (source or flowing) directly above Crying Obsidian, growing downward through Small/Medium/Large/Mature crystal stages; dev.51 uses a 1/5 initial nucleation gate per eligible random tick;
- **do not implement** the retired normal-Obsidian + Pointed-Dripstone + Cauldron conversion/intermediate blocks;
- no-Silk loot tables drop exactly 1/2/3/4 shards by age; Silk Touch obtains the current stage; Fortune is intentionally absent;
- dev.43 polls Beacon excitation every 20 ticks; larger powered crystals absorb more visual beam and emit more vanilla block light;
- a client Beacon renderer hook preserves Beacon gameplay/stained-glass colors while rendering stage transmission 75/50/25/0%; Mature visually blocks 100% of the vertical beam;
- residual beam renderer is implemented: stage-scaled range up to Mature 3–4 blocks, ~half Beacon inner width, Crying-Obsidian purple, extend 1s -> hold 1s -> retract/fade 1s, collision-limited;
- Obsidian Spike is implemented in dev.44: 3 shards + 2 String + 1 Stick, approximately half-block visual height, nine modeled tips, berry-bush-like movement hindrance and 2.0 damage per successful movement-triggered hurt event.

Optional >15 lighting must be isolated behind a compatibility provider. Base mod cannot assume it exists.

---

# 10. Planned chassis/Core progression

Authority: `docs/CORES-AND-UPGRADES-dev44.md`.

Material language:

- Obsidian = frame;
- Crying Obsidian/Shards = emitter/advanced optical material;
- Glass = primarily Core Chamber;
- Core material = energy source.

Broad Glass/Pane chassis layers are deprecated.

Core Chamber target:

- ~4×4×4 px;
- real installed ItemStack;
- uniform small scale;
- slow Y rotation + tiny bob;
- culling/bounds valid from every camera angle.

Progression:

```text
Mirage -> Display -> Wide/Tall/Prism/Field
```

Projector crafting upgrades transfer canonical persistent projector state wholesale, then normalize only chassis-specific incompatibilities. dev.45 implements this in `ProjectorStateTransfer`; do not add hand-copied per-field upgrade logic.

Do not create plain recipes that lose imported assets/snapshots/settings.

---

# 11. Planned Improved Cores

Exactly five material-based Improved Cores.

Projector behavior:

- same Base PU;
- target amplification ~×1.50;
- normal chassis multiplier/PU/Overdrive still apply.

Model:

- three nested dark-purple shells;
- middle shell truly rotated geometry;
- corresponding material centered.

Beacon:

- Glass = Diffusion;
- Quartz = Radiance;
- Amethyst = Resonance;
- Diamond = Focus;
- Netherite = Inversion;
- relay starts at Y+0.5;
- keeps stained-glass hue;
- width increases;
- max four effective cores;
- final width target cap ~2×.

Glowstone currently has **no defined role**. Do not improvise one during implementation.

---

# 12. Implementation order

Full authority: `docs/CURRENT-STATE-ROADMAP-dev44.md`.

1. dev.42 — Shard + chassis visual/Core Chamber/culling fix.
2. dev.43 — renewable crystals + loot + Beacon refraction/light/residual beams.
3. dev.44 — Obsidian Spike. Implemented source candidate.
4. dev.45 — canonical state-preserving projector upgrade crafting. **Current source candidate.**
5. dev.46 — five Improved Cores + amplification.
6. dev.47 — Improved-Core Beacon relay/effects.
7. feature/render freeze QA.
8. controlled technical refactor.
9. 0.1.0 release preparation.
10. 1.1.0+ Scan Codex/copy station.

Every wave must produce a recoverable source snapshot even before final QA.

---

# 13. Code-quality/refactor policy

Audit: `docs/CODE-QUALITY-AUDIT-dev41.md`.

Large hotspots should be decomposed only after feature behavior stabilizes:

- `MirageProjectorRenderer`;
- `MirageProjectorBlockEntity`;
- `ImageProjectorScreen`;
- `ProjectionPower`;
- `MirageProjectorScreen`;
- `EntityProjectorScreen`.

Do not combine a huge structural refactor with new block/Core/render mechanics.

Style:

- 4 spaces;
- no tabs;
- no wildcard imports;
- no trailing whitespace;
- compatibility comments explain **why** a path still exists;
- magic protocol values/constants must be centralized.

---

# 14. Snapshot policy

Every implementation/documentation wave must remain recoverable.

Do not include:

- `.gradle`;
- `.gradle-dist`;
- `build`;
- `run`;
- IDE caches;
- downloaded distributions;
- generated class files.

Keep root build metadata, `src`, `docs`, `gradle/wrapper` metadata and required tooling/scripts.
