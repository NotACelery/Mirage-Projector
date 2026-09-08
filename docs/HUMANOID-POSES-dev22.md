# Humanoid poses — dev.22

## Scope

dev.22 implements the first real pose layer for Humanoid Entity projections without turning the Mirage rig into a world entity or storing pose inside captured equipment.

## State contract

- Pose is persistent projector state in `EntityProjectionState`.
- Pose is independent from the scanned body, `ScanId`, and every virtual equipment snapshot UUID.
- Cycling a pose never consumes, moves, replaces or recaptures a real `ItemStack`.
- Removing/replacing a body scan does not silently erase the selected Humanoid pose.
- Non-Humanoid Entity modes do not consume the Humanoid pose setting.

## Presets

1. Standing
2. Guard
3. Hero
4. Combat
5. Raised Main Hand
6. Raised Off Hand
7. Dual Wield
8. Display

Raised Main/Off Hand follows the render entity's main-arm handedness instead of assuming right-handed players.

## Render architecture

`HumanoidPoseController` binds only temporary Mirage client render entities to a preset. `HumanoidModelMixin` applies the preset after vanilla `HumanoidModel#setupAnim` has prepared the normal frame. The binding uses weak entity keys so discarded preview/cache entities can disappear naturally and ordinary world entities remain untouched.

Because the transform is applied to the Humanoid model itself, normal armor layers and hand-held item transforms inherit the same arm/body pose. The head keeps vanilla look/camera motion and receives only the preset's additive head offset.

Standalone equipped armor rendered by **Item Mode** deliberately does not inherit Entity/Humanoid pose state.

## Preview and clearance

The GUI preview uses conservative pose extents when calculating auto-fit so raised/open arms are less likely to clip. The world clearance envelope and BlockEntity render bounding box use the same preset extents.

This is intentionally a first conservative implementation. It is **not** exact mesh/model-part bounds and it does not solve species-aware bounds for Horse, Generic entities or custom renderers. Those remain a separate pass.

## Not in this pass

- arbitrary/custom per-limb pose editor;
- animation timelines or pose interpolation;
- exact model-part bounding boxes;
- Horse/Generic pose presets;
- special modded renderer adapters;
- access-control/ownership changes.

## Required QA

- Run the project through Windows `build.bat` on NeoForge 21.1.244 / Java 21.
- Player scan: all eight presets and save/reload.
- Zombie and Skeleton Humanoid scans.
- Bodyless Humanoid with armor only, hands only and full six-slot loadout.
- Right-handed and left-handed main-arm behavior for Raised Main/Off Hand.
- Helmet/chest/legs/boots with dye, trims and enchantment glint.
- Sword/tool/block/shield in Main/Off Hand.
- Ghost/Tint enabled while cycling every preset.
- GUI preview at different GUI scales; verify no obvious clipping.
- Clearance preview with Display/Dual Wield/Raised poses near walls/ceilings.
- Multiplayer: one player cycles pose and another sees the synced result.
- Confirm generic Item-mode equipped armor does not inherit Entity pose state.
