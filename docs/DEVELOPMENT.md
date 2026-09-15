# Development Guide — Mirage Projector

Current maintenance baseline: **1.0.20** (initial stable baseline: **1.0.0**)
Minecraft: **1.21.1**
NeoForge: **21.1.244+**
Java: **21**
Network protocol: **34**

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

The suite covers historical regression contracts that remain relevant to the 1.0.x line plus current 1.0.20 Prism/UI, dynamic-light, rechargeable-energy, GUI-configured portable/placed devices, packed Shoulder Strap/pouch/upgrades, dedicated Charging Station, War Banner, Scan Codex and release-integrity checks, including the 1.0.18 payload/mixin/deprecation/naming integrity gate, the 1.0.19 runtime-QA follow-up gate and the 1.0.20 Sodium-safe light-render bridge gate.

The authoritative compiled build remains Windows `build.bat` under Java 21. A static verifier is not a substitute for NeoForge compilation/runtime smoke testing.

## Versioning

`gradle.properties` is the single source of the project version used by the build metadata. Current 1.0.20 uses:

```text
mod_version=1.0.20
```

Patch development continues through monotonically increasing `1.0.x` versions. The second version component advances to `1.1.0` only when the planned feature expansion is complete; internal `dev-X` labels are reserved for exceptional recovery/build snapshots rather than normal feature numbering.
