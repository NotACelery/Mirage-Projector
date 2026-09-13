# NEXT CHAT HANDOFF — 0.1.0-dev.86a

Use **`0.1.0-dev.86a`** as the latest recoverable source snapshot. Network protocol is **27**.

## Latest accepted state

- Renderer/projection QA is considered closed unless a new concrete regression appears.
- Mirage Light QA is considered closed unless a new concrete regression appears.
- GIFs, Cores/Boosters and dynamically expanding Scale/Lift/Float limits are accepted.
- The six canonical projector models/textures/item presentations are closed at dev.80f.
- Compact Z-fighting is resolved; Tall front/rear glass symmetry is resolved.
- `TURN OFF`, active-source outline, `Mode currently Active`, and the dormant End Resonance shutdown guard are retained.
- dev.81 projection-source future-proofing is accepted as the current architecture baseline.
- dev.82 adds the packaged mod logo (`src/main/resources/logo.png`) and wires it through NeoForge metadata.
- dev.86 closes the EMI/JEI viewer pass: reliable Crafting tabs for projector upgrades, explicit Crying Obsidian shard recipes, compact World Interaction arrows, and age-ordered crystal Block Drops.

## dev.86 viewer delta

- EMI projector upgrades now use synthetic replacement IDs, fixing missing Crafting tabs for Display / Wide / Tall / Prism / Field.
- EMI explicitly exposes both Crying Obsidian shard recipes (Fire Charge and Magma Cream) as Crafting entries.
- Crying Obsidian World Interaction arrows are scaled down so bud/cluster sprites do not cover them.
- Crying Obsidian crystal Block Drops are replayed in Small → Medium → Large → Cluster order.
- JEI keeps the custom projector crafting extension and vanilla discovery of both Crying Obsidian shaped recipes.
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

Then run Windows `build.bat` with Java 21. The static suite verifies the dev.81 source-ID/provider/transform/energy contracts, the dev.82 mod-logo metadata wiring, the dev.86 EMI/JEI integration files, and the previously accepted projector/light regressions.


## dev.86a build hotfix

- Windows QA of dev.86 reached `compileJava` and failed only because `CryingObsidianCraftingEmiRecipe` imported `ItemLike` from `net.minecraft.world.item`.
- NeoForge 1.21.1 exposes this interface at `net.minecraft.world.level.ItemLike`, matching the rest of the project.
- dev.86a changes only that import plus current-version metadata/verifiers; EMI/JEI behavior is otherwise identical to dev.86.
