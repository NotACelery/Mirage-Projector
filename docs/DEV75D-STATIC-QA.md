# Mirage Projector dev.75d — static QA report

Status: **source candidate static validation passed**. This is not a Windows/NeoForge build-clean declaration.

## Automated model/contract checks

The following project-local verifiers pass on the packaged source:

- `tools/verify_mirage_light_curve.py`
  - no-Booster conceptual 15 open field is exactly `15,15,14,14,...,1,1` across 30 cells;
  - conceptual 19 extends the saturated 15 plateau and finishes the same paired tail across 38 cells.
- `tools/verify_mirage_light_solver_model.py`
  - complete barriers disconnect downstream virtual light;
  - finite barriers permit weaker causal routes around edges;
  - overlapping source contributions aggregate by maximum.
- `tools/verify_mirage_light_occlusion_contract.py`
  - destination block opacity is handed to vanilla edge occlusion;
  - the regression fixture's finite wall lengthens the causal route from 6 to 8 steps.
- `tools/verify_mirage_light_detour_penalty.py`
  - unobstructed half-decay is unchanged;
  - the reference open probe reads 13 while the finite-wall routed probe reads 11;
  - profile, solver and source-sync payload all carry the detour contract;
  - protocol is 21.
- `tools/verify_mirage_light_authority_lifecycle.py`
  - protocol-21 source descriptors remain authoritative;
  - tracking/chunk/client rebuild paths are present;
  - legacy-node migration remains cleanup-only;
  - current terrain invalidation event families are wired;
  - active source/docs agree on dev.75d.

## Resource/source consistency

Static packaging sweep:

- 96 JSON resources parse successfully;
- `en_us`, `es_es` and `es_cl` each contain 439 identical translation keys;
- 126 Java source files pass delimiter sanity (`{}`, `()`, `[]` after comments/strings are stripped);
- public top-level Java type names match filenames in the best-effort source scan;
- no active `TODO`, `FIXME` or `HACK` markers under `src`;
- no current Java path creates `CRYING_LIGHT_NODE.get().defaultBlockState()`;
- no `.gradle`, `.gradle-dist`, `build`, `run`, `.class`, `javac.*.args`, `__pycache__` or `.pyc` artifacts are included in the source root.

## What still requires real Minecraft QA

Before calling dev.75d build-clean:

1. run `build.bat` on Windows with Java 21 / NeoForge 21.1.244+;
2. exact open floor sequence with Simple Light Level;
3. 1/2/3-high straight walls and the user's 3-high L-wall fixture;
4. slab/stair/partial-opacity plus at least one modded shape;
5. compare `/miragelight probe` `direct`/`extra` cost with the visible overlay;
6. Quartz and Diamond reinforcement through the same obstacle fixtures;
7. chunk edge/load/unload, relog, same-level respawn and dimension changes;
8. two-client tracking delivery/retraction if possible;
9. overlapping sources and several-source performance;
10. old dev.73/74 `crying_light_node` cleanup;
11. smoke dev.71 equipment visibility and dev.72 Entity envelope/Create Backtank behavior.

Any failure here is a concrete dev.75d fix candidate. Do not restore physical relay emission as a workaround.
