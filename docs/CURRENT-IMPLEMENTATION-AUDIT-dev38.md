# Current implementation audit — Mirage Projector 0.1.0-dev.38

> **Purpose:** one recovery document describing what the source actually implements after the dev.38 power/chassis rework, what is authoritative, what is historical, and what remains QA/future work. If a historical dev.N document conflicts with this file, the dev.38 authoritative documents win.

Closure checklist: `DEV38-CLOSURE-QA.md`.

## 1. Platform / protocol / persistence

- Minecraft: 1.21.1.
- NeoForge target used by the project: 21.1.244.
- Java: 21.
- Source version: `0.1.0-dev.38`.
- Network payload registrar protocol: **17**.
- Entity Scan data version: **5**.
- ProjectionSettings owns canonical NBT/network serialization.
- `ImageLayoutMode` is persisted/networked in dev.38.
- `ImageSourceBank` keeps 9 legacy slots only for migration/recovery; current Wide/Tall consume 4 and Field consumes none.

## 2. Registered playable projector chassis

Current registered blocks/items:

1. Mirage Projector / Compact;
2. Mirage Display;
3. Wide Mirage Projector;
4. Tall Mirage Projector;
5. Mirage Field Projector;
6. Mirage Prism.

Effigy and Colossal remain architecture profiles only; they are not current registered playable chassis.

All six registered chassis share the same `MirageProjectorBlockEntity` family and Core socket architecture.

All six also share the same idle feedback contract: when no renderable source exists, the BER draws a floating vanilla book above the chassis even if the Core socket is empty.

## 3. Placement orientation

Current blocks expose horizontal `FACING` and new placement faces the player furnace-style.

- Plane-like projection base orientation follows block `FACING`.
- Prism Image/Banner N/E/S/W remain absolute world-cardinal faces.
- User Rotation/Orientation is applied on top of the Plane placement base angle.

QA pending: all four placement directions for every chassis and persistence after world reload.

## 4. Source modes

`ProjectionSettings.SourceMode` order remains:

```text
IMAGE  = 0
ITEM   = 1
ENTITY = 2
BANNER = 3
```

This order must not be reordered casually because historical persistent/network ordinals depend on it.

### IMAGE

Current capabilities:

- local PNG/JPG/JPEG/WebP import and normalized PNG asset identity;
- multiplayer asset transport/cache by asset ID;
- Plane Front/Back modes;
- Tint, Ghost, Lighting, Rotation, Lift, Float, Vertical Flip and Scanlines;
- Prism N/E/S/W independent assets;
- Wide/Tall optional `SINGLE` / `MULTI` layout;
- Field single continuous Plane.

### ITEM

Current capabilities:

- virtual one-item snapshot; real item remains with player;
- ordinary item/block uses 3D vanilla ItemRenderer path;
- standalone armor uses equipped geometry on an invisible rig where supported;
- dedicated Item Workspace and preview.

### ENTITY

Current capabilities:

- Empty Scan Template -> Shift + right-click living target/player;
- client-only render reconstruction; projection entity is not spawned into the ClientLevel and does not run AI/ticks;
- frozen scan identity independent from source entity lifetime;
- Player packed textures + slim/wide/model parts/dominant arm fidelity metadata;
- mob CustomName scan data v5 and base-anchored projected name label;
- Humanoid six channels: Head/Chest/Legs/Feet/Main Hand/Off Hand;
- Horse Saddle/Body Armor channels;
- Generic Cat/Wolf/Parrot Idle/Sitting;
- Humanoid pose presets and Horse Idle/Rearing;
- staging ownership/return safety from dev.23+;
- dev.38 card-kind invalidation: a different active GUI family destroys virtual snapshots belonging to equipment rows that disappear. Humanoid -> Horse/Generic clears Humanoid Incoming/Projected; Horse -> Humanoid/Generic clears Horse Incoming/Projected; Generic retains no editable equipment workspace. Removing a Humanoid card alone still supports the bodyless mannequin.

Known exclusions/pending:

- jockey/passenger/composite entity stacks;
- special renderer visual bounds/adapters beyond EntityType dimensions;
- custom/modded skin/equipment systems that bypass supported vanilla hooks;
- further Ghost/Tint hardening for unusual custom RenderTypes/glint paths.

### BANNER

Current capabilities:

- virtual banner appearance snapshots;
- cloth/pattern render without pole/crossbar;
- Plane source;
- Prism N/E/S/W sources and same-source copy behaviour;
- global presentation settings.

Pending:

- richer cloth preview in Banner Workspace;
- optional future Wide/Tall Banner multi-source design only if explicitly specified later. Do not infer it from Image MULTI automatically.

## 5. Current Image chassis contract

| Chassis | Current Image behaviour |
|---|---|
| Compact | one continuous Plane |
| Display | one continuous Plane |
| Wide | toggle: continuous SINGLE or 4×1 MULTI |
| Tall | toggle: continuous SINGLE or 1×4 MULTI |
| Field | **one continuous Plane only** |
| Prism | independent N/E/S/W lateral faces |

### Mandatory regression guard

The dev.33 Field 3×3/9-image behaviour is **not current design**. It must never be recovered as a present requirement merely because old docs/changelog entries record that historical implementation.

Wide/Tall MULTI geometry uses equal square cells. At Scale 80:

```text
Wide = 80×20 = four 20×20 cells
Tall = 20×80 = four 20×20 cells
```

## 6. Projection Power contract

See `POWER-SYSTEM-REWORK-dev38.md` for exact mathematics.

Current standard Core base output:

| Core | Base PU |
|---|---:|
| Glass | 32 |
| Quartz | 48 |
| Amethyst | 64 |
| Diamond | 96 |
| Netherite | 128 |

Current chassis multipliers:

| Chassis | Multiplier |
|---|---:|
| Compact | ×1.00 |
| Display | ×1.50 |
| Wide | ×2.00 |
| Tall | ×2.00 |
| Field | ×4.00 |
| Prism | ×2.00 |

Formula:

```text
Effective PU = floor(Core base PU × chassis multiplier × Core amplification)
```

Current raw cores use amplification ×1.00.

The architecture reserves future improved Core amplification, currently targeted around ×1.50 while preserving material Base PU. No improved-Core item/recipe/texture is registered in dev.38.

### Deprecated power behaviour

Not current:

- Core-specific Scale/Lift/Float max values;
- chassis nominal W×H/Lift/Float as hard gameplay walls;
- double-limit failure states from dev.8/dev.19;
- slider ranges intentionally extending through invalid orange space.

### Nominal + Overdrive

Current nominal targets:

| Chassis | Nominal W×H | Lift | Float |
|---|---:|---:|---:|
| Compact | 10×10 | 32 | 4 |
| Display | 32×32 | 48 | 12 |
| Wide | 80×32 | 64 | 12 |
| Tall | 32×80 | 96 | 16 |
| Field | 128×128 | 144 | 24 |
| Prism | 48×48 | 96 | 12 |

Above nominal is legal if PU allows it. Component penalty is quadratic (`ratio²`).

### PU components

- emitter/stability: 2 PU;
- base geometry: ceil(area/256);
- Lift: ceil(px/16);
- Float: ceil(px/2) when enabled;
- source complexity fixed surcharge;
- feature fixed surcharge;
- component-specific Overdrive penalty;
- Ghost rebate capped mathematically at 3% of non-base gross load at Ghost 90%.

Physical Float invariant remains `Float <= Lift`.

## 7. Projection Settings GUI contract

The primary screen owns only global presentation + Core/Power/clearance/inventory.

Current global controls:

- Scale;
- Lift;
- Rotation enable/period/direction/orientation;
- Floating enable/mode/amplitude/timing;
- Fullbright/World Light;
- Ghost;
- Tint;
- Creative Debug chassis;
- Core socket;
- Power status;
- Clearance.

Scale/Lift/Float have dynamic effective endpoints. Power panel reports base output, multipliers, effective capacity/load, dimensions vs nominal and current/effective maxima. Hovering Power reports exact component breakdown.

### GUI ownership invariant

A preview renderer renders content; the Screen owns labels/layout. The dev.38 Item Workspace removes the duplicate renderer-owned heading that had begun overlapping UI text.

## 8. Projection Core socket / current items

Accepted legacy Core materials are resolved from raw/material stacks:

- Glass;
- Quartz / Quartz Block;
- Amethyst Shard / Amethyst Block;
- Diamond / Diamond Block;
- Netherite Ingot / Netherite Block.

Compact retains the historical default/migration Glass Core behaviour; newer/larger chassis start with an empty socket to avoid free Core generation.

Future purpose-built Improved Core items are intentionally not registered yet.

## 9. Render pipeline invariants

### Ghost/Tint

3D projection rendering uses Mirage-local render buffers/RenderTypes rather than global shader colour mutation.

Ghost contract:

- depth test: yes;
- depth write: no for normalized Ghost translucent layers;
- colour write: yes;
- world opaque geometry must still occlude correctly;
- translucent water must not be punched out by projection depth writes.

### Deferred Entity pass

The dev.37 recovery currently queues Entity holograms and flushes them at NeoForge `AFTER_TRIPWIRE_BLOCKS` using a Mirage-owned `BufferSource`.

Regression guard:

> Never call `endBatch()` on Minecraft's shared global block/entity buffer as part of the Mirage Entity deferred pass. dev.36 did so and disturbed Image rendering.

QA still required in-game for:

- projector physically in front of vs behind giant Entity hologram;
- water in front of vs behind Ghost Entity;
- overlapping Ghost Entities from multiple viewpoints.

## 10. Debug Handbook

Current intended screen:

- based on vanilla `BookViewScreen` behaviour to avoid the old generic-Screen blur issue;
- enlarged/centred dev.37 layout;
- seven tabs: General, Compact, Display, Wide, Tall, Field, Prism;
- General pages document dev.38 Power formula, nominal/Overdrive, Ghost rebate, dynamic sliders, snapshots and Plane modes;
- chassis tabs explicitly document purpose, nominal targets and multiplier.

Opening the handbook suppresses only that handbook's own first-person hand/item render, not all hands globally.

QA pending at several GUI scales.

## 11. Data ownership / anti-duplication invariants

- Image assets are referenced by asset IDs/cache/server store.
- Item/Banner/equipment projected objects are virtual snapshots.
- Capturing a virtual snapshot does not consume the real object.
- Physical Entity staging is returned when accepted or when leaving; overflow drops exactly once near the player.
- Projected snapshots are not obtainable inventory storage.
- Virtual equipment state is context-owned: if a card-kind change removes a family of GUI rows, those hidden virtual snapshots are cleared immediately instead of remaining serialized.
- Empty-card Humanoid remains a supported explicit bodyless-mannequin state; incompatible-card cleanup must not be confused with simple card removal.
- Entity scan has its own Mirage scan identity; source entity UUID is provenance only.

## 12. Current migration rules

- ProjectionSettings missing new fields load safe defaults.
- `ImageLayoutMode` defaults to SINGLE.
- unsupported MULTI on Field/other non-Wide/Tall chassis is coerced to SINGLE.
- dev.33-dev.37 legacy slot 0 can seed/recover continuous Front.
- all 9 legacy ImageSourceBank slots remain serializable so opening a migrated Field does not silently destroy old source references.
- historical Item physical-storage migration return path remains preserved for worlds from before virtual Item snapshots.
- EntityProjectionState load migration prunes incompatible hidden virtual equipment only when an active body identifies a concrete Humanoid/Horse/Generic family; no-body state is left intact so legitimate bodyless Humanoid mannequins survive reload.

## 13. Current pending queue after dev.38 implementation

### Must QA/fix before calling dev.38 stable

1. Windows `build.bat` compile.
2. Field one-image continuous behaviour and save/reload.
3. Wide/Tall SINGLE/MULTI switching, persistence and equal-cell scale.
4. Dynamic Power sliders with every Core/chassis combination.
5. Nominal→Overdrive transition and quadratic cost display.
6. Ghost rebate values and endpoint recalculation.
7. Placement FACING for all chassis.
8. Floating idle book on Compact/Display/Wide/Tall/Field/Prism, including chassis with no Core installed.
9. Entity lifetime matrix: Humanoid bodyless preservation; Humanoid -> Generic/Horse cleanup with no reappearance after later card removal; Horse -> Humanoid/Generic cleanup.
10. Item Workspace collision fix at GUI scales 2/3/4 and reduced window sizes.
11. Debug Handbook centring/tabs at several GUI scales.
12. Entity/projector/water render-order regression suite from dev.36/dev.37.

### Current technical hardening backlog

- special/modded RenderType Ghost/Tint and glint QA;
- renderer-specific Entity visual bounds/adapters;
- rich Banner cloth preview;
- final chassis models/crafts after dimensions/power balance stabilize;
- server/admin limits if needed after performance QA;
- frustum/culling/LOD optimization for very large/overdriven installations;
- multiplayer latency/large-asset transport stress QA.

### Explicitly later / not implied by current features

- physical Improved Core items/recipes/textures;
- Effigy/Colossal playable blocks;
- accessory/backpack/Curios/Artifacts/Trinkets adapters;
- Entity Catalog / Scan Binder;
- composite/jockey Entity projections;
- Wide/Tall Banner MULTI unless separately designed.

## 14. Next feature priority after dev.38

After Windows build + in-game QA close dev.38, the first new feature priority is **GIF / Animated Image import**. This should be treated as an Image-pipeline extension, not as generic video playback. The next design pass must freeze decoder/frame-duration semantics, safety limits (resolution/frame count/FPS/memory), normalized asset identity, multiplayer transport/cache representation, world playback timing and behaviour across Plane/Wide/Tall/Field/Prism before implementation.

Purpose-built Improved Core items/recipes/textures and optional chassis model/texture refresh remain planned, but they are deliberately separated from dev.38 stabilization and from the first GIF architecture pass.

## 15. Recovery priority

When continuing in another chat/build:

1. use dev.38 source + this audit as current contract;
2. treat dev.33 Field grid and dev.8/dev.19 hard Core/chassis limits as historical only;
3. do not mark dev.38 build-clean until Windows compile + in-game QA confirms it;
4. preserve a source snapshot after every substantial wave even if QA is incomplete.
