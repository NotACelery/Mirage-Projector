# NEXT CHAT HANDOFF — 0.1.0-dev.79a

## Recovery baseline

Use **`0.1.0-dev.79a`** as the latest recoverable source snapshot.

Platform/runtime:

- Minecraft 1.21.1;
- NeoForge 21.1.244;
- Java 21;
- Gradle 9.2.1;
- protocol 26.

`dev.76h` remains the accepted static-light architecture baseline. `dev.77` adds Core Booster identities. `dev.78–79a` are the temporary alternate-projector visual comparison/refinement line.

## What the user has already QA-approved

### Mature static light

- repeated relogs/chunk reloads now converge correctly;
- expected field appears after roughly ~1–2 seconds when logging in next to an energized Mature Cluster;
- no visible FPS loss in the tested setup;
- the historical stuck-chunk problem was resolved by the dev.76h external-cache/section-boundary invalidation fix;
- old ghost light from earlier physical-relay development builds is gone.

### Projector models through dev.79

User explicitly reported that dev.79 model/hitbox reconciliation is correct, especially Core chambers.

Accepted/locked candidates:

- Mirage Projector Alt — compact/basic identity;
- Mirage Display Alt — restored dev.78 low-corner identity;
- Wide Alt — stepped horizontal V identity, strongly approved;
- Field Alt — model/shape corrected;
- Prism Alt — strongly approved and locked.

Tall was functionally/physically correct in dev.79 but needed one final visual polish.

## dev.79a Tall-only change

Do not redesign other projectors.

Tall final contract:

- continuous rectangular obsidian lower ring;
- continuous four corner posts;
- one-pixel obsidian top ring at model Y=10..11;
- no antenna geometry;
- no fake rounded corner gaps;
- no asymmetric rear/top glass protrusion;
- purple internal Core chamber `[6,4,6] -> [10,8,10]`;
- magenta outer dome glass, four wall windows + roof;
- outer panes align to the frame pixel plane;
- exact model↔VoxelShape equality;
- all coordinates are whole model pixels.

## Audit result

Run:

```text
python tools/verify_current_line.py
```

Artifact-side result at handoff:

```text
CURRENT-LINE VERIFICATION PASS (17 gates)
```

Whole-project audit currently covers:

- 130 Java files;
- 120 JSON resources;
- 24 registered blocks;
- 21 registered items;
- blockstate/model/item-model references;
- loot coverage for registered BlockItems;
- Mixins;
- legacy runtime-file absence;
- protocol 26 / dev.76h static-light architecture;
- empty Core-slot behavior;
- pixel/quarter-pixel-aligned candidate models;
- current accepted Alt-model hash locks;
- Tall dual-glass dome contract.

`es_es` missing Alt-projector translations were repaired in this pass.

## Known non-blocking debt

- 9 `EventBusSubscriber.Bus.*` NeoForge deprecation sites remain. Do not mix that API migration into Tall QA; do it as a dedicated cleanup wave.
- Several historical verifier scripts pin old dev versions by design. Do not run every `tools/*.py` as a current gate; use `verify_current_line.py`.
- `MirageProjectorRenderer` remains a future refactor hotspot.
- Six `*_alt` comparison projector IDs are still registered/Creative-visible. Before stable 1.0.0, select/merge winning visuals into canonical blocks and explicitly remove/migrate comparison IDs.

## Newly confirmed projector-control requirements (documentation-only in this snapshot)

These requirements were confirmed after the original dev.79a handoff and are **not implemented by dev.79a yet**:

- add a non-destructive `TURN OFF` action;
- OFF hides/stops the projection but preserves its complete source/presentation configuration;
- `Use <mode> mode` explicitly activates that source and turns projection back on;
- an opened workspace is not automatically the active source;
- every workspace/tab must expose a clear current-use indicator when its source is active;
- the main Image/Item/Entity/Banner menu must give the actually active source button a white outline;
- no source button is visually active while projection is OFF;
- keep enabled state independent from source identity.

1.1.0 End Resonance extends this contract: a Dragon Egg in Field/Prism overrides normal shutdown/source controls. `TURN OFF` does nothing to the resonance state and instead emits the event message `This doesn't seem to work...` until the Dragon Egg is physically removed.

## Immediate next action

1. Windows `build.bat` for dev.79a.
2. In-game Tall visual QA from all four sides/top-ish/normal eye height with a Netherite Ingot Core and full idle animation.
3. Confirm no other candidate model changed from dev.79.
4. If approved, freeze model design and move to canonical-model selection/merge + remaining 1.0.0 release QA rather than reopening freeform chassis redesign.

## 1.1.0 preserved future scope

`WAITLIST-1.1.0.md` remains authoritative for Portable Illumination / Glow Dust / Scan Codex / portable-presentation projectors and the hidden Dragon Egg **End Resonance** Easter egg.

End Resonance teaser remains:

> **Some projections may have an End after all...**

Field = planar 2×3 End aperture; Prism = volumetric 2×3×2 anomaly. Dragon Egg acts as a non-consumed special resonance Core and restores the full prior projector state when removed.

### Build BAT hotfix (2026-09-12)
- `build.bat` now runs the real build in a child `cmd`, while the outer launcher remains alive to show the final exit code and pause even if the internal BAT/Gradle process terminates unexpectedly.
- Added the previously missing `:cleanup_missing` and `:cleanup_failed` labels.
- `CLEAN-MIRAGE-PROJECTOR.bat` now returns explicit `exit /b 0` on success instead of inheriting a stale `ERRORLEVEL`.
- Gradle is invoked directly with `call gradle.bat --no-daemon clean build --stacktrace --console=plain`.
- `build-mirage-projector.log` records stage breadcrumbs around cleanup, Java discovery and Gradle startup/exit.
- Runtime/source version remains `0.1.0-dev.79a`; this is a tooling-only hotfix.


## Late dev.79d UI cleanup

- Removed the Image workspace resolution/projection-sizing diagnostic text that overlapped the source/slot preview after import.
- Image workspace edits now preserve the active SourceMode; `Use Image mode` is the only activation boundary.
