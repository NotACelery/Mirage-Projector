# Entity UX / item recovery / Ghost hardening — dev.23

## Scope

dev.23 is a repair wave on top of the build-clean dev.22 baseline. It intentionally does not add Banner or another source family. The goal is to make the existing Entity/Humanoid workflow safe and readable before expanding scope again.

## Physical staging is temporary

Real items placed in Entity Incoming/staging slots are never persistent projector storage.

- Accepting an Incoming physical item copies its one-count virtual appearance into Projected, consumes the Incoming virtual copy, and immediately returns the real ItemStack to the player.
- Leaving the Entity workspace returns every physical staging ItemStack, regardless of whether the menu is closed with Escape, replaced by another projector workspace, or closed server-side for another normal menu reason.
- `Inventory#add` is attempted first. Any remainder that does not fit is dropped at the player instead of being left hidden in the BlockEntity.
- Card-derived Incoming equipment remains virtual. Accepting it moves the snapshot from Incoming to Projected; there is no real ItemStack to return.
- The physical Entity Scan Card is not auto-returned because it defines the active scanned body and removing it has existing body-lifecycle semantics.

Item Snapshot Mode remains different by design: its source slot has already been virtual since dev.12, so the real source item never leaves the player's inventory and therefore needs no close-time return path.

## Entity workspace layout

The old split composition (370 px workspace plus a detached responsive preview panel) is replaced with one centred 520 px panel:

1. Entity Source
2. Incoming / Channel / Projected equipment table
3. Actions + one-line status
4. Player inventory
5. Integrated tall 3D preview on the right

Menu slot coordinates were moved together with the visual frames. No slot is left at an old invisible coordinate.

## Projection Power presentation

The Core section now distinguishes:

- Core **Capacity** (installed maximum)
- **Used** PU
- **Available** PU (remaining budget)

A selected source with no renderable content consumes 0 PU. Rotation/Floating/Lighting configuration alone no longer creates phantom power usage for an empty Image/Item/Entity source. Empty Item and Entity workspaces also stop drawing the legacy generic book placeholder, so zero-content state and world rendering agree.

## Entity Ghost Effect

The 3D projection buffer still multiplies per-vertex tint/alpha, but opaque entity layers also need a translucent RenderType for alpha to blend.

The dev.22 texture parser failed on the normal `Optional[minecraft:...]` diagnostic representation because it cut the token at the same bracket that closes `Optional`. dev.23 explicitly unwraps that resource location before choosing `entityTranslucent` / `entityTranslucentCull`.

Special render paths (glint, eyes, beams, existing translucent types, text, lines) are not structurally replaced; they keep their original RenderType and still receive the local vertex tint/alpha where supported.

## Horse projection pose

`MirageProjectionHorse#getStandAnim(float)` now returns `1.0F`. This forces the full vanilla rearing pose for the projection-only Horse without ticking horse AI/state counters. Saddle and Body Armor continue to be applied to the same horse model.

## Protocol

Protocol registrar: `10 -> 11`.

The payload wire shape did not gain a field, but dev.23 changes server-authoritative staging/acceptance semantics enough that mixed dev.22/dev.23 peers should fail compatibility instead of silently disagreeing about item ownership.

## Priority QA

1. Put real Humanoid armor/tool items in staging; accept each one; verify every real source returns immediately and Incoming clears.
2. Repeat with an already occupied Projected slot and confirm Replace/Cancel behavior.
3. Leave staging populated and close with Escape; repeat by opening Projection Settings; verify all real items return.
4. Fill inventory completely before closing; verify overflow drops exactly once and no copy remains in the projector.
5. Repeat Horse Saddle/Body Armor acceptance/close behavior.
6. Confirm the scan card remains in its source slot unless explicitly removed.
7. Test Ghost at 0/25/50/72/90% on Player, Horse, Zombie/Skeleton, armor, saddle/body armor and held items in both preview and world.
8. Confirm Horse preview and world projection are fully rearing on two hind legs.
9. Verify empty Image/Item/Entity sources show 0 PU used; verify active content shows Used/Available plus Core Capacity consistently.
10. Check GUI at the user's normal GUI scale and at one scale above/below for text/slot collisions.
