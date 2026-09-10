# Mirage Projector — implementation audit (dev.45)

## Scope

dev.45 is a state/persistence/crafting wave. It intentionally does not add Improved Cores or Beacon-relay behavior.

## Canonical persistence path

`MirageProjectorBlockEntity.saveAdditional` remains the authoritative custom persistence serializer. dev.45 reuses vanilla `BlockEntity.saveToItem` for placed-machine packing, which writes that custom state into `minecraft:block_entity_data` and carries BlockEntity-provided item components.

`ProjectorStateTransfer` is the single upgrade transport helper. It:

1. reads existing `BLOCK_ENTITY_DATA` from the source item;
2. if absent, creates a temporary source-chassis BlockEntity and serializes its defaults;
3. changes the actual item with `ItemStack.transmuteCopy`, preserving the source component patch;
4. loads the complete source payload into a temporary target-chassis BlockEntity;
5. lets target `loadAdditional` perform the same migration/sanitization used by real placed blocks;
6. saves the target payload back to `BLOCK_ENTITY_DATA`.

No recipe copies individual fields such as `CoreItem`, `ImageSourceBank` or `EntityProjectionState`.

## Mining path

Before normal Survival destruction, `MirageProjectorBlock.playerWillDestroy` asks the BlockEntity to prepare a stateful drop. `onRemove` detects that one-shot pending drop and therefore skips historical separate physical-content ejection. `playerDestroy` emits exactly that packed projector item and still awards vanilla block-mined stats/exhaustion.

For non-player destruction, the pending packed drop does not exist and the old physical safety fallback remains active.

## Recipe system

`mirage_projector:projector_upgrade` is a custom recipe serializer with five exact 3x3 paths: display, wide, tall, prism and field. The path enum owns source/target chassis and ingredient geometry; JSON only selects the path.

The recipe's `assemble` method obtains the center projector ItemStack and delegates all state migration to `ProjectorStateTransfer`.

## Compatibility behavior

- Compact -> Display from a clean, never-placed item preserves the implicit Glass Core by materializing Compact defaults first.
- Display SINGLE -> Wide/Tall remains SINGLE.
- Display -> Prism retains the existing primary/front image as Prism North; other Prism faces remain whatever the source persistent settings already contain.
- Any illegal legacy MULTI mode reaching Prism/Field is normalized by the target BlockEntity load path to SINGLE without deleting the persisted compatibility source bank.
- Scale/Lift/Float and presentation settings are preserved; stronger target chassis do not auto-rescale the projection.
- installed physical Core, Entity Scan card and staging items remain inside the stateful result instead of being returned during crafting.

## Protocol

Network protocol remains `18`. dev.45 adds a recipe serializer and ItemStack BlockEntity data but does not change Mirage custom packet schemas.

## Known intentional limitations

- explosion/non-player destruction does not yet promise full virtual-state preservation;
- Field recipe cost remains balance-provisional;
- recipe-book presentation for the custom exact-pattern recipe requires in-game QA;
- dev.45 inherits any unresolved dev.43/dev.44 compile/render issue until Windows `build.bat` and game QA pass.
