# dev.59 — Full audit / rake operation

## Scope

dev.59 is a maintenance wave only. It intentionally adds no new gameplay mechanic.

Goals:

- rake the entire source/resource/documentation tree;
- remove source comments per current user request;
- enforce the established formatting baseline;
- distinguish active gameplay from migration-only compatibility code;
- eliminate stale documentation authority;
- rebuild the current implementation record and waitlist from actual source + surviving design decisions.

## Code cleanup performed

- removed Java/Javadoc comments across active Java source;
- removed explanatory comments from Gradle source;
- normalized indentation to spaces;
- normalized trailing whitespace;
- removed wildcard imports;
- sorted imports;
- removed statically unused imports;
- expanded compact multi-statement formatting where it violated the one-statement-per-line standard;
- replaced the remaining empty cleanup catch with an explicit debug log;
- removed the obsolete legacy Improved Core BER;
- renamed old five-core compatibility classes/symbols to `LegacyImprovedCore*` / `LEGACY_*` while preserving serialized registry IDs;
- moved the Crying Obsidian common random-tick/light mixin out of the misleading `.mixin.client` Java package and changed the mixin root accordingly.

## Source hygiene target

Current style after dev.59:

- 4-space indentation;
- no tabs;
- no wildcard imports;
- no trailing whitespace;
- no source comments/Javadocs;
- no empty catch blocks;
- no TODO/FIXME/HACK markers in active code;
- network protocol remains centralized as `MirageProjector.NETWORK_PROTOCOL`.

## Documentation rake

Pre-dev.59 focused documents were moved to:

```text
docs/archive/pre-dev59/
```

They remain available for migration archaeology but no longer compete with current authority.

The active documentation set was rebuilt from current source and current decisions. Stale entries such as five independent Improved Cores, future Effigy/Colossal chassis and already-implemented Image/Entity/Crying-Obsidian features were removed from the active waitlist.

Canonical reference assets supplied by the user were preserved under `docs/reference/`.

## Backlog reconciliation

Important genuine pending work restored/retained:

- dev.58 cloud/translucent-Entity + modded-armor render acceptance;
- loaded Core Booster Beacon relay identities: Diffusion/Radiance/Resonance/Focus/Inversion;
- capped Booster stacking and downstream Crying Obsidian excitation;
- safe extended/useful-range lighting strategy;
- per-channel Humanoid/Horse equipment visibility without deleting snapshots;
- targeted accessory/backpack compatibility where generic rendering is insufficient;
- composite/mounted/jockey Entity design;
- performance/LOD and multiplayer asset stress hardening;
- 1.1.0+ Mirage Scan Codex/copy station;
- optional Unrefined Crying Crystal Core remains brainstorming only.

## Validation status

Static checks are run before packaging and recorded in the final dev.59 handoff. Windows `build.bat` remains required because this environment does not provide the project's complete local NeoForge/Gradle build runtime.

## Final static validation before packaging

Final dev.59 source snapshot checks:

- 99 Java source files scanned;
- 94 runtime JSON resources parsed successfully;
- 423 localization keys in each of `en_us`, `es_cl` and `es_es`, with exact key parity;
- 0 Java comment tokens;
- 0 Java tabs;
- 0 Java trailing-whitespace findings;
- 0 Java indentation findings against the 4-space baseline;
- 0 wildcard imports;
- 0 duplicate imports;
- 0 naive unused-import candidates;
- 0 multi-statement-line candidates outside normal `for` syntax;
- 0 active TODO/FIXME/HACK markers;
- 0 public top-level type/filename mismatches;
- 0 brace-balance findings in the static source scan;
- every configured Mixin has a corresponding Java source file;
- local blockstate → model and model → texture references checked without missing Mirage resources;
- all 15 registered active item IDs have an item model and localization entry;
- all 17 registered block IDs, including the five migration-only IDs, have blockstate/localization coverage;
- legacy `improved_*_core` IDs have no BlockItems, recipes or item models;
- 0 obsolete-author strings and 0 forbidden repository-hosting-name strings anywhere in the packaged project tree;
- metadata author remains `Celerbi`;
- active documentation reduced to 13 current files;
- 112 pre-dev.59 documents retained only under `docs/archive/pre-dev59/`;
- 11 canonical/reference files retained under `docs/reference/`.

No automated unit-test source exists in this project line. The next authoritative validation is therefore Windows `build.bat`, followed by the in-game matrix in `QA-REGRESSION.md`.
