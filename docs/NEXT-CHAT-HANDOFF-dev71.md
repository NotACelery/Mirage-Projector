# Mirage Projector — next-chat handoff dev.71

Current source candidate: **0.1.0-dev.71**. Network protocol: **19**.

dev.71 implements the previous P1 per-component Equipment visibility contract while dev.70 light-field QA continues in parallel.

Implemented channels:

- Humanoid Head / Chest / Legs / Feet / Main Hand / Off Hand;
- Horse Saddle / Body Armor.

Visibility is saved separately from virtual snapshots. OFF means render suppression only: no snapshot deletion, return, recapture or UUID change. ON reconstructs the projection from the already stored snapshot. Clearing a Projected snapshot resets that channel to visible. Incompatible family cleanup resets that family's visibility state.

Entity Workspace now shows a `VIS.` ON/OFF control beside every supported Projected row. World and preview reconstruction both use `EntityProjectionClientEntityFactory.applyProjectedEquipment(...)`; its cache fingerprint includes visibility. Bodyless Humanoid rendering requires at least one visible projected equipment channel, while hidden snapshots remain stored/source-valid.

Protocol 19 is intentional because `TOGGLE_VISIBILITY` was appended to `EntityWorkspaceActionPayload.Action`; do not revert to 18 unless the new action is moved to a separately version-safe payload.

Required QA before build-clean:

- Windows Java 21 compile;
- all six Humanoid channels ON/OFF independently;
- Horse Saddle / Body Armor independently;
- all-hidden bodyless mannequin;
- save/reload, stateful break/re-place and chassis-upgrade persistence;
- replace hidden snapshot vs clear-and-recapture semantics;
- multiplayer sync;
- Create Netherite Backtank Ghost regression;
- dev.70 wall/block/unblock/overlap light-field QA remains open.

After visibility is accepted, return to P2 renderer/entity hardening and release QA rather than re-opening removed P1 work.
