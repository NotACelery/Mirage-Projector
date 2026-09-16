# Mirage Projector 1.0.28 — Recipe Synchronization Hotfix

1.0.28 is a runtime connection hotfix over 1.0.27. It adds no gameplay feature and keeps Mirage network protocol **38** plus `ProjectionSettings` format **4**.

## Failure

A compiled 1.0.27 client/server reached world join and then disconnected with:

`Failed to encode packet clientbound/minecraft:update_recipes`

The failing custom recipe was the stateless filled Entity Scan Card clearing recipe. Its JSON codec used `MapCodec.unit(ScanCardClearingRecipe::new)`, producing a fresh recipe object, while its network codec used `StreamCodec.unit(new ScanCardClearingRecipe())`, capturing a different object. Minecraft 1.21.1 `StreamCodec.unit` explicitly requires the encoded value to be `.equals(...)` to its captured unit value. Since the recipe class used identity equality, synchronization rejected the decoded recipe instance.

## Correction

`ScanCardClearingRecipe` now owns one canonical `INSTANCE`. Both the JSON codec and stream codec resolve exactly that instance:

- `MapCodec.unit(() -> ScanCardClearingRecipe.INSTANCE)`
- `StreamCodec.unit(ScanCardClearingRecipe.INSTANCE)`

This preserves the zero-byte stateless stream representation while satisfying vanilla's equality requirement.

## QA

Required runtime smoke test: compile, launch, join a singleplayer world, then join a multiplayer server with the same Mirage build and confirm recipe synchronization completes without disconnect. After joining, verify one filled Entity Scan Card by itself crafts into one blank Entity Scan Card.
