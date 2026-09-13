# NEXT CHAT HANDOFF — 0.1.0-dev.83

Use **`0.1.0-dev.83`** as the latest recoverable source snapshot. Network protocol is **27**.

## Latest accepted state

- Renderer/projection QA is considered closed unless a new concrete regression appears.
- Mirage Light QA is considered closed unless a new concrete regression appears.
- GIFs, Cores/Boosters and dynamically expanding Scale/Lift/Float limits are accepted.
- The six canonical projector models/textures/item presentations are closed at dev.80f.
- Compact Z-fighting is resolved; Tall front/rear glass symmetry is resolved.
- `TURN OFF`, active-source outline, `Mode currently Active`, and the dormant End Resonance shutdown guard are retained.
- dev.81 projection-source future-proofing is accepted as the current architecture baseline.
- dev.82 adds the packaged mod logo (`src/main/resources/logo.png`) and wires it through NeoForge metadata.
- dev.83 adds optional EMI/JEI integrations for projector recipes and Crying Obsidian documentation.

## dev.83 viewer delta

- EMI now registers explicit wrappers for all custom projector chassis-upgrade recipes.
- EMI now adds an icon-only world-interaction tutorial page for renewable Crying Obsidian growth.
- The Crying Obsidian tutorial is linked from Crying Obsidian, all three bud stages, the cluster and the shard.
- JEI now extends the vanilla crafting category for `ProjectorUpgradeRecipe` and adds ingredient info pages for Crying Obsidian growth plus the projector upgrade line.
- Protocol/gameplay/render/light behavior is intentionally unchanged.

## dev.81 architectural baseline

- `ProjectionSettings.SourceMode` is no longer a closed enum. It is an interned namespaced-ID value object.
- Built-in IDs are `mirage_projector:image`, `:item`, `:entity`, `:banner`.
- Save/network source serialization uses IDs instead of enum ordinals.
- Legacy integer `SourceMode` NBT still migrates.
- `ProjectionSettings` network/NBT format is explicitly versioned; current format version is 2.
- Protocol bumped 26 → 27.
- `ProjectionSourceRegistry` supplies common content/count providers and chassis compatibility.
- `ProjectionSourceRenderRegistry` supplies client renderer dispatch; BER top-level source dispatch is registry-driven.
- Unknown source IDs and opaque `ProjectionSourcePayloads` survive missing providers.
- `ProjectionTransform` introduces persisted normalized quaternion orientation while preserving all current transform controls.
- `ProjectionEnergySource` decouples ProjectionPower from the assumption that every future device has a Core socket.
- `ProjectionChassisProfile.supportsProjectionSource(...)` provides the chassis/source compatibility seam.

## Important compatibility behavior

A missing addon/provider must fail closed without deleting its saved source ID or opaque payload. Viewer integrations are optional: when EMI or JEI is absent, the mod must still load and behave normally.

## Next 1.0.0 work

Do not reopen renderer/light architecture without a concrete bug. Remaining work is release hardening:

- recipe/progression/balance sanity review in-game;
- EMI/JEI smoke QA with real runtime jars;
- handbook/public-text cleanup;
- save/reload + old-world migration smoke test on the source-ID codec;
- final documentation/archive cleanup;
- Windows Java 21 build and release-candidate smoke pass.

## Verification

Run:

```text
python tools/verify_current_line.py
```

Then run Windows `build.bat` with Java 21. The static suite verifies the dev.81 source-ID/provider/transform/energy contracts, the dev.82 mod-logo metadata wiring, the dev.83 EMI/JEI integration files, and the previously accepted projector/light regressions.
