# WAITLIST — General / Cross-version

The extension seams required before 1.0.0 are implemented. This file now tracks only genuine cross-version maintenance and compatibility work.

## Renderer / compatibility backlog

- Add targeted compatibility adapters only when a concrete third-party renderer cannot be handled safely by the generic Entity/Ghost path.
- Refine species/renderer-specific bounds only from reproducible clipping/culling failures.
- Keep mounted/passenger composite projections disabled until a dedicated relative-transform/equipment/bounds format exists.

## Quality / maintenance

- Keep reusable regression suites rather than one-off bug tests.
- Re-run multiplayer asset/GIF stress, save/reload, migration, render-performance and old-world compatibility at major release boundaries.
- Preserve recoverable source snapshots after meaningful implementation waves.
- Keep historical docs clearly separated from current authority.
- Maintain parity across shipped localization key sets.

## Extension architecture rule

New source/chassis/light/energy systems should use the 1.0.0 registries and capability boundaries rather than reintroducing closed enums, source-specific transform state, Core-only energy assumptions or per-frame STATIC_WORLD rebuilds.
