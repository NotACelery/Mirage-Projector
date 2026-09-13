# dev.71 — Per-channel Entity equipment visibility

## Goal

Implement the existing P1 contract without changing snapshot ownership: every editable Humanoid/Horse Projected equipment channel can be hidden from rendering and restored later without deleting, returning or recapturing its virtual snapshot.

## Channels

Humanoid: Head, Chest, Legs, Feet, Main Hand, Off Hand.  
Horse: Saddle, Body Armor.

## Persistence model

`EntityProjectionState` stores an `EquipmentVisibility` compound separately from `HumanoidProjected` / `HorseProjected`. Missing visibility keys load as visible, so dev.70 and older projector state migrates safely. Visibility is therefore presentation metadata, not inventory/snapshot data.

Rules:

- hide/show never changes the stored `ItemStack` or snapshot UUID;
- replacing a snapshot preserves the current channel visibility;
- clearing a projected snapshot resets that channel to visible for future content;
- incompatible family cleanup clears its snapshots and resets its visibility flags;
- canonical chassis crafting/stateful drops preserve visibility automatically through full BlockEntity state transfer.

## Render path

`EntityProjectionClientEntityFactory.applyProjectedEquipment(...)` equips only channels currently marked visible. This single reconstruction path serves world projection and Entity Workspace preview, including Horse Saddle state and bodyless Humanoid mannequins.

The equipment fingerprint now includes both snapshot UUID and visibility bit. A synced visibility toggle therefore invalidates the cached reconstructed entity immediately instead of waiting for another snapshot mutation.

If a bodyless Humanoid has stored equipment but every channel is hidden, no empty invisible mannequin is constructed/rendered. The virtual snapshots remain stored and the workspace can show them again.

## GUI / networking

Every supported row has a compact `VIS.` control next to the Projected slot. The button stays present for the current family and is disabled when no snapshot exists. ON/OFF changes only render visibility; right-clicking the Projected slot remains the destructive snapshot-clear action.

`TOGGLE_VISIBILITY` is appended to `EntityWorkspaceActionPayload.Action`. Because an old protocol-18 client would not understand ordinal 6 safely, Mirage protocol is bumped to **19**.

## Required QA

- Humanoid: toggle each of six channels independently in preview and world projection.
- Horse: toggle Saddle and Body Armor independently; hiding Saddle must remove the visible saddle state without deleting its snapshot.
- Hide then show a channel and verify the same item/components/trims/dye/glint return.
- Hide all bodyless Humanoid equipment: world projection should render nothing, while Projected rows still retain snapshots.
- Save/reload with mixed hidden/visible channels.
- Break/re-place a stateful projector and craft through a chassis upgrade; visibility and snapshots must survive together.
- Replace a hidden channel snapshot and verify it remains hidden; explicitly clear the snapshot and add new equipment, which should start visible.
- Recheck Create Netherite Backtank Ghost at ON/OFF and opacity 100/50/10 to ensure the compatibility renderer is simply absent while Chest visibility is OFF and unchanged when ON.
- Multiplayer: one client toggles visibility and another sees the reconstructed projection update after BlockEntity sync.
