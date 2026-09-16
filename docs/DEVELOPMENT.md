# Development Guide — Mirage Projector

Current maintenance baseline: **1.0.31** (initial stable baseline: **1.0.0**)


## 1.0.31 canonical workspace/runtime rebuild

The 1.0.31 pass removes the split client-side source-selection/workspace-open sequence. `OpenImageWorkspacePayload`, `OpenItemWorkspacePayload`, `OpenEntityWorkspacePayload` and `OpenBannerWorkspacePayload` activate their source on the server before opening the menu. Main and workspace menus serialize authoritative `ProjectionSettings` plus projection-enabled state into their opening buffers. Table stays on the canonical base-projector menu/screen path; no Table-only GUI fork exists. Main Cancel restores the opening baseline and exits. The Codex book canvas is rendered exactly once by the explicit screen render path while vanilla background blur remains disabled. Protocol 41; ProjectionSettings format 4.

## 1.0.30 UX/runtime wave

The 1.0.30 pass wires the Hand Projector's direct portable source modes, captures both halves of Mirage Equipment pseudo-slot clicks, makes Data-show placement controls live-previewed/non-closing, recenters the Presentation Remote in raw window coordinates, refreshes Entity mode menus after activation, and reflows Codex/Entity workspace UI from runtime QA. Entity Scan Card also gains a dedicated 16×16 item texture. Protocol 40; ProjectionSettings format 4.

## 1.0.29 runtime interaction QA

The 1.0.29 pass converts several 1.0.28 visual-only surfaces into authoritative interactions. Creative Mirage Equipment reads/writes the visible Creative picker cursor and intercepts external slot clicks before vanilla can interpret them as outside drops. The Codex owns an explicit live-world canvas render. Table source buttons activate the selected source before workspace navigation. Wall/Data-show pairing now mutates the held remote binding, supports explicit server-authoritative unpair/ejection, and uses a model-matched collision/outline shape. Network protocol is 39; ProjectionSettings remains format 4.

Minecraft: **1.21.1**
NeoForge: **21.1.244+**
Java: **21**
Network protocol: **41**

## Source layout

- `src/main/java/celerbi/mirageprojector/` — runtime code.
- `src/main/resources/assets/mirage_projector/` — models, textures, language and client assets.
- `src/main/resources/data/mirage_projector/` — recipes, loot tables and data-driven content.
- `src/main/templates/META-INF/neoforge.mods.toml` — generated mod metadata template.
- `docs/` — current product/architecture documentation.
- `docs/history/` and `docs/archive/` — historical development material.
- `tools/` — static regression/audit utilities.

## Maintenance rules

- Current behavior belongs in `CURRENT-IMPLEMENTATION.md`; chronology belongs in `CHANGELOG.md`; future work belongs in `ROADMAP.md`/waitlists.
- Do not reintroduce removed comparison-projector IDs or parallel projector identities.
- Keep projection activation separate from projection-source identity.
- Keep source content separate from `ProjectionTransform` presentation state.
- Preserve stable namespaced projection-source IDs and unknown-source payloads.
- Keep `STATIC_WORLD` and `DYNAMIC_VISUAL` light lifecycles separate.
- Do not make generic Mirage energy consumers depend on a fixed-projector Core socket.
- Preserve migration-only IDs until an explicit world-migration policy removes them.
- Optional EMI/JEI compatibility must remain optional: the base mod must load without either viewer.

## Code/style expectations

- Java source uses spaces, no tab indentation and no trailing whitespace.
- Source files end with a newline.
- Runtime comments explain contracts/invariants, not development-version chronology.
- `TODO`, `FIXME` and `HACK` markers are not accepted in release source.
- JSON resources must parse without duplicate keys.
- Shipped locales (`en_us`, `es_cl`, `es_es`) must expose the same translation-key set.
- Public handbook/tooltips must describe current behavior rather than historical QA state.

## Verification

Run the source-side suite before a build/release handoff:

```text
python tools/verify_current_line.py
```

The suite covers historical regression contracts that remain relevant to the 1.0.x line plus current 1.0.24 Prism/UI, dynamic-light, rechargeable-energy, GUI-configured portable/placed devices, packed Shoulder Strap/pouch/upgrades, dedicated Charging Station, War Banner, Scan Codex and release-integrity checks, including the 1.0.18 payload/mixin/deprecation/naming integrity gate, the 1.0.19 runtime-QA follow-up gate and the 1.0.20 Sodium-safe light-render bridge gate, the 1.0.21 runtime-QA render/invalidation gate, the 1.0.22 scan-copy foundation gate, the 1.0.23 vanilla-Lectern Codex workflow gate, and the 1.0.24 Codex/card/import plus portable-UI gate.

The authoritative compiled build remains Windows `build.bat` under Java 21. A static verifier is not a substitute for NeoForge compilation/runtime smoke testing.

## Versioning

`gradle.properties` is the single source of the project version used by the build metadata. Current 1.0.31 uses:

```text
mod_version=1.0.31
```

Patch development continues through monotonically increasing `1.0.x` versions. The second version component advances to `1.1.0` only when the planned feature expansion is complete; internal `dev-X` labels are reserved for exceptional recovery/build snapshots rather than normal feature numbering.
