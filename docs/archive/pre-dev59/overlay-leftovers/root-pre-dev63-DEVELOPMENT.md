# Mirage Projector — development guide

Current source line: **0.1.0-dev.62**.

This file describes how the current project is organized and maintained. Chronology belongs in `CHANGELOG.md`; current feature truth belongs in `docs/CURRENT-IMPLEMENTATION.md`; pending work belongs in `docs/ROADMAP.md`.

## 1. Runtime baseline

- Minecraft 1.21.1
- NeoForge 21.1.244+
- Java 21
- Gradle 9.2.1
- Parchment 2024.11.17
- Mirage network protocol 18

`MirageProjector.NETWORK_PROTOCOL` is the single protocol authority. Do not scatter protocol literals through payload registration.

## 2. Build

Windows development entry point:

```text
build.bat
```

dev.61 reached live QA far enough to confirm the Create Backtank no longer crashes the projection renderer. dev.62 must pass the Windows build and the opacity/depth regression before the current renderer line can be called build-clean/runtime-clean.

Do not package generated/cache directories in source snapshots:

- `.gradle`
- `.gradle-dist`
- `build`
- `run`
- IDE caches
- downloaded toolchains/distributions
- generated class files

## 3. Source organization

Primary packages under `celerbi.mirageprojector`:

- `block` — physical block behavior;
- `blockentity` — persistent/synchronized world state;
- `client` — screens, renderers, caches and client-only runtime behavior;
- `crying` — Crying Obsidian growth/Beacon optical state;
- `event` — gameplay interaction/event entry points;
- `item` — dedicated item behavior;
- `compat/jade` — optional Jade integration;
- `menu` — menu/container contracts;
- `mixin` — common Mixins, with client-only Mixins below `mixin.client`;
- `network` — custom payloads and transfer sessions;
- `recipe` — state-preserving projector upgrade recipes;
- `registry` — DeferredRegister ownership.

See `docs/ARCHITECTURE.md` for the current boundaries.

## 4. Code standards

The dev.62 source baseline uses:

- 4 spaces per indentation level;
- no tabs;
- no trailing whitespace;
- no wildcard imports;
- imports grouped/sorted consistently;
- one statement per line;
- braces for control-flow bodies;
- no empty catch blocks;
- no `TODO`, `FIXME` or `HACK` markers in active source;
- no Java/Javadoc source comments under the current cleanup rule;
- public top-level type name must match its filename;
- registry/network constants centralized rather than duplicated as magic strings/numbers.

When a compatibility path needs explanation, put the rationale in current documentation rather than reintroducing inline source comments while this rule remains active.

## 5. Registry policy

Active gameplay registry entries are listed in `docs/REGISTRY-INVENTORY.md`.

The five old `improved_*_core` block IDs are compatibility-only. Java symbols are deliberately named `LEGACY_*` / `LegacyImprovedCore*`. They must not regain:

- BlockItems;
- recipes;
- Creative exposure;
- independent balance;
- dedicated user-facing render behavior.

Their sole purpose is allowing old development worlds to load and migrate into `core_booster`.

## 6. Persistence and upgrades

`MirageProjectorBlockEntity` owns projector persistent state. Normal Survival breaking packs that state into the dropped projector ItemStack.

`ProjectorStateTransfer` is the canonical chassis-upgrade bridge. Do not create a second hand-maintained list of fields for crafting transfer. Source payload is loaded through the destination BlockEntity and saved again after destination sanitization.

Core Booster material is persistent world/item state and must remain immediately synchronized visually after placement, extraction and reload.

## 7. Projection source families

Keep the rendering families distinct:

- Image/Banner are face-based 2D projection geometry;
- Item is one rendered 3D ItemStack snapshot;
- Entity is a frozen reconstructed entity/equipment snapshot.

Do not force all source families into one quad abstraction.

## 8. Asset pipeline

Current imported media limits/rules are documented in `docs/ASSET-PIPELINE.md`.

Important implementation constants include:

- 8 MiB normalized/stored Mirage asset ceiling;
- 32 KiB network chunks;
- 32 MiB importer source-file ceiling;
- 2048 maximum static normalization dimension;
- 1024 maximum GIF dimension;
- 128 GIF frame ceiling;
- 30 second upload/session timeout;
- 4 concurrent server uploads per player and 64 globally.

Do not use filenames as network identity; content hashes remain authoritative.

## 9. Power/chassis

Effective PU:

```text
floor(Base PU × chassis multiplier × Core amplification)
```

Standard Base PU is Glass 32, Quartz 48, Amethyst 64, Diamond 96 and Netherite 128. Standard cores are ×1.00; a loaded Core Booster is currently ×1.50.

Nominal chassis geometry and overdrive rules are documented in `docs/POWER-AND-CHASSIS.md`.

## 10. Crying Obsidian

The canonical visual source for buds/clusters is the user-provided vanilla Amethyst family under `docs/reference/canonical-amethyst/`. Preserve vanilla silhouette/presentation and change only the Crying Obsidian theme/palette unless a new explicit design decision supersedes it.

Current growth, harvesting, Beacon attenuation and residual-ray behavior is in `docs/CRYING-OBSIDIAN.md`.

## 11. Renderer stabilization

The current unresolved P0 is dev.58 semi-transparent Entity composition.

Regression targets include:

- clouds at projection altitude;
- water in front/behind the projection;
- opacity 100/90/50/10%;
- vanilla armor/held items/trims/glint/eyes;
- Create Netherite Backtank or another custom armor renderer;
- overlapping translucent projected entities.

Do not begin another large renderer rewrite without running `docs/QA-REGRESSION.md` against the existing candidate.

## 12. Optional integrations

Jade is compile-only/optional. Mirage must remain functional without Jade installed.

The bundled WebP decoder is a runtime implementation dependency for static WebP import support.

Future accessory/backpack integrations should be targeted adapters only when the generic render path cannot safely cover a modded renderer.

## 13. Documentation policy

Authority order:

1. source/resources;
2. `docs/CURRENT-IMPLEMENTATION.md`;
3. focused active docs;
4. `docs/ROADMAP.md`;
5. `CHANGELOG.md`;
6. `docs/archive/pre-dev59/` as history only.

Whenever a feature changes:

- update the focused active document;
- update `CURRENT-IMPLEMENTATION.md` if current behavior changed;
- remove completed work from `ROADMAP.md` rather than leaving duplicate stale entries;
- add chronology to `CHANGELOG.md`;
- update the in-game handbook when player-visible behavior changed;
- produce a recoverable source snapshot even when QA is incomplete.

## 14. Current waitlist

Do not duplicate the detailed list here. `docs/ROADMAP.md` is the single waitlist authority.

Immediate order:

1. accept/fix dev.58 ghost/cloud/modded-armor rendering;
2. validate the dev.60 loaded Core Booster Beacon relay identities and four-effective-Booster cap;
3. investigate useful-range energized-crystal lighting;
4. add render-only per-equipment-channel visibility;
5. harden entity/render compatibility and release QA;
6. defer Scan Codex to 1.1.0+.

Effigy/Colossal are not current promised chassis. Wide/Tall Banner MULTI is not implied by Image MULTI. Glowstone has no assigned role.
