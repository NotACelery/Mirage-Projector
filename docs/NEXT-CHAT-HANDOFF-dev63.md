# Mirage Projector — next-chat handoff dev.63

**Superseded by dev.64.** Retained only as development history.

## Current source

`0.1.0-dev.63`

Protocol remains 18.

## What changed

dev.63 audited Create 6.0.10's Netherite Backtank implementation. The item is `BacktankItem.Layered`, so one equipped chest item produces three relevant visual contributions: a separate Backtank block-model layer plus two synthetic Humanoid armor passes using `netherite_diving_layer_2` and `netherite_diving_layer_1`.

Mirage now treats only those two synthetic armor textures specially during semi-transparent Entity rendering. They use colour-only late Ghost passes and each receives `1 - sqrt(1 - requestedOpacity)` alpha, making the two-pass composite approximate one ordinary armor layer. The separate tank keeps dev.62's late depth-stable Ghost path. dev.61's independent-buffer fix remains intact.

## Documentation layout rule

`README.md` is the project-root documentation entry point. `CHANGELOG.md`, `DEVELOPMENT.md`, `THIRD_PARTY_NOTICES.md` and all other project documentation belong under `/docs`.

Because dev.63 moves those three root documents, this snapshot legitimately updates `CLEAN-MIRAGE-PROJECTOR.bat`. It retires stale root copies safely when a snapshot is extracted over an older folder. Do not keep changing or redistributing the cleaner in ordinary feature waves unless a future implementation actually removes or relocates files.

## Immediate QA

1. Compile dev.63 with `build.bat`.
2. Scan/project a Humanoid or Player wearing Create's Netherite Backtank.
3. Test 0%, 1%, 10%, 50% and 90% transparency.
4. Compare physical tank opacity with the synthetic diving chestpiece.
5. Rotate camera through high/low/front/back angles and look for z-fighting.
6. Repeat with foil/glint if available.
7. Recheck clouds and water behind the projection.

## P0 if still failing

If only the Create synthetic chestpiece remains wrong, keep the fix texture/layer-specific. If z-fighting persists on unrelated vanilla armor/body surfaces too, diagnose the exact remaining RenderType before changing the global late Ghost depth contract again.

## Next P1 after renderer acceptance

- powered Crying Obsidian useful lighting range;
- per-component Equipment visibility;
- renderer/entity hardening;
- mounted/composite design;
- release hardening;
- Scan Codex remains 1.1.0+.
