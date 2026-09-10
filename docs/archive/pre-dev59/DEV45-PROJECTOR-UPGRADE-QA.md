# dev.45 — projector state/upgrades QA

## Build gate

- [ ] Windows `build.bat` succeeds with Java 21 / NeoForge 1.21.1.
- [ ] no recipe serializer/datapack parse errors appear during startup.
- [ ] all six projector items and all five upgrade recipes appear/load correctly.

## Base crafting

- [ ] craft Mirage Projector with 5 Crying Obsidian Shards + 1 Glass + 3 Obsidian.
- [ ] place a freshly crafted Mirage Projector and confirm starter Glass Core exists.
- [ ] craft that never-placed Mirage directly into Display and confirm the Display also contains the starter Glass Core after placement.

## Packed mining

Prepare one projector with deliberately obvious state: Diamond/Netherite Core, imported GIF/image, non-default Scale/Lift/Float/Tint/Ghost/Rotation, Entity card, virtual equipment and any physical staging items.

- [ ] break it normally in Survival.
- [ ] exactly one projector chassis item drops for the machine.
- [ ] Core/card/staging items do **not** additionally spill onto the floor.
- [ ] place the dropped projector again.
- [ ] all persistent state returns unchanged.
- [ ] imported assets still resolve/render.
- [ ] Entity/Humanoid/Horse virtual snapshots still exist.
- [ ] Item/Banner snapshots still exist.
- [ ] physical staging items remain physically retrievable from their GUI slots.

## Upgrade chain

Repeat state checks through:

- [ ] Mirage -> Display.
- [ ] Display -> Wide.
- [ ] Display -> Tall.
- [ ] Display -> Prism.
- [ ] Display -> Field.

For each result:

- [ ] source projector item is consumed once.
- [ ] target chassis is correct.
- [ ] installed Core survives.
- [ ] source mode survives.
- [ ] image/GIF references survive.
- [ ] Scale/Lift/Float remain the same numeric settings.
- [ ] Tint/Ghost/Lighting/Scanlines/Flip/rotation survive.
- [ ] Entity card + virtual equipment survive.
- [ ] Item/Banner snapshots survive.

## Image-layout normalization

- [ ] Display SINGLE -> Wide starts as SINGLE, not forced 4x1.
- [ ] Display SINGLE -> Tall starts as SINGLE, not forced 1x4.
- [ ] Display image -> Prism keeps primary source on North rather than deleting it.
- [ ] Prism does not invent 4x1/1x4 stacking.
- [ ] Field remains one continuous Plane.

## Custom item components

- [ ] rename a stateful projector in an anvil, then upgrade it; custom name should survive the item transmutation.
- [ ] stateful projector items with different BlockEntity data do not merge incorrectly.

## Destruction fallback

- [ ] Creative breaking behaves normally and does not create a Survival packed drop.
- [ ] explosion/non-player destruction still ejects real Core/card/staging contents rather than deleting them. Full virtual state is not a dev.45 guarantee in this path.

## Recipe geometry

- [ ] Wide and Tall cost exactly the same materials but require their respective horizontal/vertical patterns.
- [ ] no direct Compact -> Wide/Tall/Prism/Field recipe exists.
- [ ] Field requires 4 whole Crying Obsidian + 4 shards + Display.
