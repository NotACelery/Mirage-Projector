# Mirage Projector 1.0.31-SNAPSHOT — Table Projector Runtime Rebuild

Recoverable source snapshot. This is **not** a release/build-clean claim.

## Baseline authority

Built directly from the user-confirmed 1.0.30 ZIP whose SHA-256 is:
`939fd16fe3de032c0a07a0cbb162ff5437e46a49b24fbeeac4113a37b5fcb6ae`

## Implemented so far

- Version advanced to 1.0.31; network protocol 41.
- Table Projector remains on the canonical MirageProjectorMenu/MirageProjectorScreen path used by Display and the original chassis. No parallel Table-only GUI is introduced.
- Opening Image/Item/Entity/Banner workspaces now atomically activates the corresponding source server-side before opening the menu.
- Workspace menus receive authoritative ProjectionSettings/projection-enabled snapshots instead of depending on a possibly stale client BlockEntity.
- Main projector menu receives projection-enabled state in its opening data and tracks source/projection state locally from the authoritative menu snapshot.
- Main GUI Cancel restores the base settings and closes the interface.
- Main header/source-workspace spacing increased so top controls no longer collide with SOURCE WORKSPACES / Active source.
- Scan Codex canvas rendering restored to one explicit pass in render(), with renderBg() intentionally empty; this preserves the no-blur contract without making the book transparent or drawing it twice.

## Still pending in this snapshot

- Complete static regression suite and dedicated 1.0.31 verifier.
- Documentation/version-line cleanup for release candidate.
- Windows Gradle build and in-game QA.
- If QA exposes remaining Table renderer/placement differences, continue from this exact snapshot rather than the abandoned intermediate 1.0.31 folders from the stalled execution.
