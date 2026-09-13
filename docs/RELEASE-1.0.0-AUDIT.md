# Mirage Projector 1.0.0 — Release Audit

Release version: **1.0.0**
Minecraft: **1.21.1**
NeoForge baseline: **21.1.244**
Network protocol: **27**

## Release status

The 1.0.0 source tree has completed the final documentation, source-cleanup and static release-audit pass. Systems already accepted through in-game QA were not reopened unless the audit found a concrete issue.

## Cleanup completed

- Active documentation was rewritten around the stable 1.0.0 product instead of the development chronology.
- Development notes, historical handoffs and completed 1.0 waitlists were preserved under `docs/history/`.
- Runtime comments that only described old `dev.xx` transitions, Alt-model comparisons or temporary QA state were removed or rewritten as current behavior.
- Obsolete handbook page keys were removed; the live handbook surface now uses the current section-based content only.
- The public handbook name is **Mirage Handbook** / **Manual de Mirage** while the existing registry ID remains unchanged for compatibility.
- English, Chilean Spanish and Spanish language files have exact key parity.
- Release-facing source/resources/docs were normalized for trailing whitespace, tabs and final newlines.
- The builder version is now `1.0.0`.

## Static audit results

The release verifier checks the complete active runtime/resource surface, including:

- JSON validity and duplicate-key detection;
- block/item/model/blockstate/resource references;
- six canonical projector assets and their accepted geometry fixes;
- recipe and loot-table coverage;
- optional EMI/JEI integration contracts;
- Crying Obsidian Silk Touch/shard semantics and absence of Fortune bonuses;
- projection-source IDs, registries, transform persistence and generic energy-source boundary;
- Mirage Light sync/watchdog contracts;
- mod-logo metadata;
- active-documentation/archive separation;
- public language-key parity;
- absence of temporary `dev.xx`, `_alt`, TODO/FIXME/HACK markers from active runtime source;
- release-source formatting expectations.

Current result:

```text
Mirage Projector 1.0.0 release audit PASS
96 JSON
18 blocks
15 items
434 language keys per locale
```

The accumulated stable regression suite also passes:

```text
MIRAGE PROJECTOR 1.0.0 VERIFICATION PASS (19 gates)
```

## Known non-blocking compiler cleanup

There are **9** remaining uses of NeoForge's deprecated `EventBusSubscriber.Bus` annotation form. They are currently compile warnings rather than runtime or release blockers. They are deliberately recorded here instead of forcing a late lifecycle-registration rewrite immediately before the stable release.

## Final build authority

The static audit does not replace the NeoForge compiler/package build. The authoritative release build remains Windows `build.bat` with Java 21. The final source cleanup after the last accepted in-game build is limited to documentation, comments, language/public text, release metadata/versioning and audit tooling; gameplay architecture is intentionally frozen.

## 1.1.0 boundary

New features should branch from this exact 1.0.0 source baseline. The 1.0.0 renderer, projector models, held-item presentation, Mirage Light foundation, GIF/Core behavior, projection-source architecture and EMI/JEI integration should be treated as stable unless a concrete regression is discovered.

## Release UI cleanup

- Removed the internal creative-only projector Debug button from the public 1.0.0 screen.
- Expanded the Tint control across the freed Appearance row.
- Kept internal compatibility/state plumbing intact so the release fix does not alter saves or protocol.

## Responsive GUI hotfix

- Added a shared responsive container-screen base for the main projector screen and all source workspaces.
- Tall GUIs now expose a vertical scrollbar only when the effective scaled window is shorter than the panel content.
- Mouse wheel and drag scrolling move the complete container origin, vanilla slots and custom widgets together so hitboxes stay aligned.
- The Image workspace also shifts its source-bank hit map with the scroll offset.
- Normal-height windows retain the original centered layout and show no scrollbar.
