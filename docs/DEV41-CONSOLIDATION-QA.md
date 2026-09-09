# dev.41 consolidation QA

This version is intended to be behavior-neutral outside small defensive/cache cleanup. QA should therefore focus on proving that the deprecation/code-hygiene changes did not alter current gameplay contracts.

## Build

1. Run Windows `build.bat` with Java 21.
2. Confirm mod version is `0.1.0-dev.41`.
3. Confirm network protocol remains `18`.
4. Verify WebP Jar-in-Jar packaging still succeeds.

## Chassis / menu ordinal regression

`ProjectionChassisProfile` removed only unused trailing EFFIGY/COLOSSAL placeholders. Verify all six real projectors still open the correct menus:

- Compact/Mirage Projector;
- Display;
- Wide;
- Tall;
- Field;
- Prism.

Open Image Workspace on all six and confirm no chassis is decoded as another profile.

## Image-bank compatibility

- Wide MULTI exposes exactly 4 slots.
- Tall MULTI exposes exactly 4 slots.
- Field exposes no MULTI grid.
- loading an old dev.33-dev.37 world with legacy bank data must not crash or corrupt the bank merely from opening/applying a workspace.

## Image/GIF smoke test

- PNG static;
- GIF animated;
- WebP static;
- renamed GIF-as-PNG;
- Prism four faces;
- Wide/Tall SINGLE and MULTI.

No behavior is expected to differ from dev.40/dev.39.

## Entity smoke test

- Piglin preview in Overworld remains stable (no shaking);
- Piglin world projection remains stable;
- Humanoid -> incompatible entity-family snapshot cleanup remains intact.

## Power smoke test

- standard Core values remain 32/48/64/96/128 base PU;
- dynamic Scale/Lift/Float maxima unchanged;
- Power breakdown unchanged;
- protocol-centralization must not change payload behavior.

## Known visual issues intentionally not fixed in this audit

Do not fail the dev.41 consolidation build for these already-known issues; they are targets for the next visual/material implementation:

- legacy physical Core can disappear at some camera angles;
- current broad Glass/Glass-Pane chassis layers can appear as translucent ghost sheets;
- installed Core still renders via legacy material-block substitution.

These are superseded by the planned Core Chamber / Cut-Obsidian chassis redesign in `CORES-AND-UPGRADES-dev41.md`.
