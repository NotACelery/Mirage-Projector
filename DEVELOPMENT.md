# Mirage Projector — Development Guide (dev.41)

## 1. Baseline

- Minecraft **1.21.1**
- NeoForge **21.1.244**
- Java **21**
- Gradle **9.2.1**
- Parchment **2024.11.17**
- Mod version **0.1.0-dev.41**
- Network protocol **18** (`MirageProjector.NETWORK_PROTOCOL`)

`dev.41` remains the consolidation/documentation baseline. Do not bump to dev.42 until actual next-wave code/resources are implemented.

Current QA facts:

- dev.40 ran in-game;
- Piglin shaking fix is confirmed;
- current Core visual has camera-angle disappearance;
- broad chassis Glass layers are visually unacceptable ghost sheets.

Start any recovery/new chat with:

1. `docs/DOCUMENTATION-AUTHORITY-dev41.md`
2. `docs/CURRENT-STATE-ROADMAP-dev41.md`
3. `docs/CURRENT-IMPLEMENTATION-AUDIT-dev41.md`

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

# 9. Planned Crying Obsidian ecosystem

Authority: `docs/CRYING-OBSIDIAN-ECOSYSTEM-dev41.md`.

Important current contract:

- final shared item name is `Crying Obsidian Shard`, not `Cut Obsidian Shard`;
- Stonecutter: 1 Crying Obsidian -> 4 shards;
- 8 shards + Fire Charge or Magma Cream -> Crying Obsidian;
- renewable growth comes from Lava directly above Crying Obsidian, growing downward through Small/Medium/Large/Mature crystal stages;
- **do not implement** the retired normal-Obsidian + Pointed-Dripstone + Cauldron conversion/intermediate blocks;
- no-Silk drops 1/2/3/4 shards by age; Silk obtains stage; no Fortune bonus initially;
- larger powered crystals absorb more Beacon beam and emit more light;
- Mature visually blocks 100% of the vertical beam while Beacon gameplay remains active;
- mature target residual beam ~3–4 blocks, ~half Beacon inner-beam width, Crying-Obsidian-purple, extend -> hold -> retract/fade;
- Obsidian Spike remains 3 shards + 2 String + 1 Stick, 9 tips, 2.0 damage/hurt event.

Optional >15 lighting must be isolated behind a compatibility provider. Base mod cannot assume it exists.

---

# 10. Planned chassis/Core progression

Authority: `docs/CORES-AND-UPGRADES-dev41.md`.

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

Projector crafting upgrades must transfer canonical persistent projector state wholesale, then normalize only chassis-specific incompatibilities.

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

# 12. Planned implementation order

Full authority: `docs/CURRENT-STATE-ROADMAP-dev41.md`.

1. dev.42 — Shard + chassis visual/Core Chamber/culling fix.
2. dev.43 — renewable crystals + loot + Beacon refraction/light/residual beams.
3. dev.44 — Obsidian Spike.
4. dev.45 — canonical state-preserving projector upgrade crafting.
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
