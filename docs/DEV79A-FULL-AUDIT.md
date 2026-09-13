# DEV79A — Full current-source audit and Tall final-polish pass

## Purpose

`0.1.0-dev.79a` is deliberately narrow in runtime/model scope but broad in verification scope.

Visual/model changes are **Tall Mirage Projector Alt only**. The audit then checks the complete current source/resource line so the next chat can resume from a trustworthy snapshot instead of reconstructing project state from dozens of development handoffs.

No projection, PU/power, networking, asset-transfer, entity-snapshot or Mirage Light Engine gameplay behavior is intentionally changed by this pass.

---

## 1. Tall-only visual correction

The dev.79 Tall dome was functionally correct and its shape matched the model, but in-game QA found visual irregularity in the frame joins, a heavy two-pixel top cap and an asymmetric glass protrusion.

### Final dev.79a Tall contract

- static model geometry remains whole-pixel only;
- bottom obsidian ring is continuous, with no corner gaps used to fake rounding;
- four vertical corner posts are continuous;
- top obsidian ring is exactly **one model pixel thick** at `Y=10..11`;
- top/bottom/frame joins are rectangular and symmetric;
- no antenna geometry is allowed;
- the old protruding/irregular glass roof is removed;
- the **inner core chamber** is one centered Purple Stained Glass box: `[6,4,6] -> [10,8,10]`;
- the **outer dome** uses Magenta Stained Glass on four walls plus the roof;
- outer dome panes sit in the same model-pixel plane as the obsidian frame so the window reads larger without fractional offsets;
- the floating core remains centered at model Y=6;
- idle book/projection anchors remain unchanged from the accepted dev.79 layout;
- `TALL_ALT_SHAPE` is regenerated to equal the visible Tall cuboid set exactly.

The dual-glass treatment is intentional: purple identifies the actual Core chamber while magenta identifies the larger specialist Tall dome.

---

## 2. Scope locks

The following candidate models are explicitly frozen in dev.79a and are SHA-256 checked by the current verification suite:

- Mirage Projector Alt;
- Mirage Display Alt;
- Wide Mirage Projector Alt;
- Mirage Field Projector Alt;
- Mirage Prism Alt.

Only `tall_mirage_projector_alt.json` is allowed to change visually in this snapshot.

This prevents another model pass from accidentally redesigning unrelated chassis while correcting one projector.

---

## 3. Current runtime baseline recovered by the audit

- Minecraft: **1.21.1**
- NeoForge: **21.1.244**
- Java: **21**
- Gradle: **9.2.1**
- Parchment: **2024.11.17**
- Current source version: **0.1.0-dev.79a**
- Current Mirage network protocol: **25**

### Static Mirage light

The accepted current architecture is the accumulated dev.76h line:

- server-authoritative `STATIC_WORLD` solve;
- complete dependency-window publication rather than partial solves;
- revisioned atomic chunk snapshots;
- explicit client chunk snapshot requests;
- lightweight revision manifests and retry path;
- source-centric Mature Cluster watchdog without forced chunk tickets;
- client reads `max(vanilla, Mirage)` rather than feeding Mirage voxels back into vanilla propagation;
- every authoritative snapshot refreshes downstream light caches even if bytes are unchanged;
- section Y invalidation also refreshes section Y-1 for floor/top-face consumers such as the private LightLevelSimple backport.

The user accepted repeated relog/world-load QA after dev.76h: the field converges correctly after roughly a second or two near an energized source and no visible FPS regression was observed.

### Core Booster identities

The dev.77 split remains current:

- Glass / Diffusion: shorter static reach, softer detour penalty;
- Quartz / Radiance: strongest pure static reach;
- Amethyst / Resonance: optical/residual activity, no free static reach;
- Diamond / Focus: stronger detour shadow identity, reach only in pairs;
- Netherite / Inversion: optical rotation inversion, no free static reach.

Loaded Core Booster item names remain material-aware; empty stays simply `Core Booster`.

---

## 4. Registry/resource audit

Current source contains:

- **129 Java source files**;
- **120 JSON resources**;
- **24 registered blocks**;
- **21 registered items**.

The audit verifies:

- every JSON resource parses;
- every Mirage blockstate reference resolves to an existing model;
- every Mirage item model that inherits a Mirage block model resolves;
- every registered block has blockstate/model resources;
- every registered item has an item model;
- every registered BlockItem has a block loot table;
- Mirage recipe references resolve against registered Mirage IDs;
- all 12 canonical/comparison projector blocks are accepted by the projector BlockEntity type;
- all configured Mixins resolve to source classes;
- removed legacy runtime classes/payloads are absent;
- current projector BlockEntity registration includes the six canonical and six temporary Alt chassis;
- no default Glass Core is injected into a projector with no saved Core item;
- Java source passes current static hygiene checks: no tab indentation, trailing whitespace, wildcard imports, filename/public-type mismatch, TODO/FIXME/HACK markers, or basic brace-count mismatch.

### Audit repair: Spanish translations

`es_es.json` was missing names for the six temporary `*_alt` comparison projectors. dev.79a fills those keys from the already-current `es_cl` equivalents.

---

## 5. Projector model audit

All six Alt model families are checked for whole-integer `from`/`to` coordinates and integer rotation origins.

Current model state:

- **Compact Alt** — accepted/locked;
- **Display Alt** — dev.79 restored low-corner identity, accepted/locked;
- **Wide Alt** — accepted V-shaped horizontal identity, locked;
- **Field Alt** — accepted/locked;
- **Prism Alt** — accepted/locked;
- **Tall Alt** — only model changed in dev.79a; final dome QA pending.

Non-Prism candidate `VoxelShape` definitions are checked against their actual visible cuboid set so model/outline/collision drift becomes a verifier failure instead of an in-game surprise.

Temporary `*_alt` IDs remain Creative-visible comparison artifacts. They are **not** intended to become twelve shipping projector products. Before 1.0.0, the winning models must be merged into the canonical six chassis and the temporary comparison IDs explicitly removed/migrated.

---

## 6. Current verification gates

Use this command from the project root:

```text
python tools/verify_current_line.py
```

It runs the current-source suite instead of blindly running every historical snapshot verifier.

Current suite covers 16 gates:

- cumulative build cleanup contract;
- atomic static publication;
- build/cleanup stabilization;
- chunk snapshot request handshake;
- atomic/revisioned chunk snapshot ordering;
- Core Booster math;
- Core Booster identity;
- projector integer-grid/model anchor rules;
- dev.79a projector scope lock/model↔shape reconciliation;
- exact Mirage half-decay curve;
- dependency-window math;
- detour penalty;
- occlusion contract;
- causal solver model;
- current virtual-light invalidation/section-boundary bridge;
- whole-project current-source audit.

Current result in the artifact environment:

```text
CURRENT-LINE VERIFICATION PASS (16 gates)
```

The artifact environment does **not** have Gradle installed/downloadable, so this is not a substitute for `build.bat` on Windows.

---

## 7. Documentation audit/repair

Several active documents still described dev.75d/dev.76 as the current line and protocol 21/22 even though runtime had moved to protocol 25. dev.79a synchronizes the active authority documents to the real current state:

- root `README.md`;
- `DOCUMENTATION-AUTHORITY.md`;
- `CURRENT-IMPLEMENTATION.md`;
- `ROADMAP.md`;
- `VERSION-SCOPE.md` current applicability note;
- `DEVELOPMENT.md`;
- `ARCHITECTURE.md`;
- `MIRAGE-LIGHT-ENGINE.md`;
- `QA-REGRESSION.md` current-protocol note;
- `REGISTRY-INVENTORY.md` temporary Alt-ID status;
- `WAITLIST-1.0.0.md` acceptance status.

Historical dev-specific notes intentionally keep their historical protocol/version statements where they describe that old snapshot.

---

## 8. Known debt found but intentionally not mixed into dev.79a runtime

### NeoForge event-bus deprecation warnings

Nine current Java files still use `EventBusSubscriber.Bus.*`, which NeoForge reports as deprecated in the current baseline:

- `client/ClientEvents.java`;
- `client/ClientMirageLightLifecycleEvents.java`;
- `client/ClientRuntimeEvents.java`;
- `client/ProjectionClearancePreviewRenderer.java`;
- `event/CoreBoosterInteractionEvents.java`;
- `event/CryingObsidianLightInvalidationEvents.java`;
- `event/EntityScanInteractionEvents.java`;
- `event/MirageLightDebugCommands.java`;
- `event/MirageLightLifecycleEvents.java`.

These are currently warnings, not the cause of the earlier compile failures. They are deliberately left for a dedicated API-cleanup pass rather than changing event wiring during model QA.

### Historical verifier scripts

Several old `tools/verify_dev75*` / `verify_dev76*` scripts intentionally pin the version/file topology of their historical snapshot. They are retained as archaeology and are **not** all expected to pass on current source. Use `verify_current_line.py` for the current gate.

### Renderer architecture

`MirageProjectorRenderer` remains a large coordinator and a future refactor hotspot. No renderer refactor is mixed into dev.79a.

### Temporary comparison registries

The six `*_alt` projector IDs are still registered and Creative-visible. Final model merge/removal is a real 1.0.0 closure task.

---

## 9. Build/QA boundary

The artifact-side static audit is clean. `dev.79a` is **not** declared build-clean until Windows `build.bat` succeeds.

Required immediate QA after build:

1. Tall outline/collision still matches its final visible geometry;
2. top rim reads as a clean one-pixel obsidian ring;
3. no corner gaps/rounded-looking irregularity;
4. no rear glass protrusion from any viewing side;
5. magenta outer dome is symmetric and contrasts cleanly with the purple inner chamber;
6. Netherite Ingot and other Core items remain visibly contained through the full core bob/rotation;
7. idle book does not clip the raised dome;
8. Wide/Compact/Display/Field/Prism visually match dev.79 exactly, confirming the Tall-only scope lock in-game.

If this passes, do **not** reopen all model designs. The next model step should be explicit canonical-model selection/merge, not another unconstrained candidate redesign wave.
