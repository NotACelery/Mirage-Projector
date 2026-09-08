# Mirage Projector — dev.20 Entity interaction priority + equipped-loadout capture

## Scope

Dev.20 closes two Entity/Humanoid gaps without changing the frozen ownership model:

1. `Shift + right-click` with an Empty/Scanned Scan Template must reach Mirage before an interactable LivingEntity can consume the gesture.
2. Humanoid Entity Mode gains the previously specified `Capture Equipped Loadout` workflow.

Minecraft / NeoForge remain fixed at **1.21.1 / 21.1.244** and Java remains **21**.

## Early scan interaction

The old implementation relied only on `Item#interactLivingEntity`. In the NeoForge 1.21.1 interaction pipeline, entity-specific/general interaction runs before that item callback. A Horse can therefore consume the action by mounting and prevent the scan-card callback from running.

Dev.20 listens to `PlayerInteractEvent.EntityInteractSpecific`, the first cancellable entity-interaction event in that pipeline. It only takes ownership when all of these are true:

- the player is sneaking;
- the target is a `LivingEntity`;
- the ItemStack in the currently processed hand is `EntityScanCardItem`.

Mirage then performs the same frozen-snapshot operation and cancels that interaction with the scan result. Normal right-click is untouched. `EntityScanCardItem#interactLivingEntity` remains as a compatibility fallback but delegates to the same `scanTarget` implementation.

Expected behavior:

| Gesture | Result |
|---|---|
| normal right-click + scan card | target's ordinary interaction |
| Shift + right-click + scan card | Mirage scan has priority |
| Shift + right-click mounted/composite target | Mirage rejection message; no mount/use action |

QA must include Horse plus other interactable LivingEntities such as Villagers and tameable mobs.

## Capture Equipped Loadout

The Entity/Humanoid workspace now exposes **Capture equipped** only in Humanoid context. The server snapshots the player's current:

- Head;
- Chest;
- Legs;
- Feet;
- Main Hand;
- Off Hand.

Each non-empty channel receives a fresh virtual snapshot UUID. Empty equipped slots clear the corresponding virtual Incoming channel, so one press represents the complete current six-channel loadout.

This operation never removes, moves, locks, decrements or otherwise edits the real player's equipment.

Captured equipment goes to **Incoming**, not Projected. Existing per-channel Apply and replacement-confirmation rules remain authoritative, so a loadout capture cannot silently overwrite an active projected set.

Physical items manually placed in left staging slots remain real staging inventory and retain their existing precedence/return path.

## Networking

The `EntityWorkspaceActionPayload.Action` enum adds `CAPTURE_EQUIPPED` at the end, preserving previous ordinals. Mirage network protocol is bumped from `7` to `8` so mixed dev.19/dev.20 peers cannot silently disagree about action semantics.

## Validation boundary

This environment can perform source/JSON/static inspection but cannot download the Gradle distribution/dependencies required for the real NeoForge build. `build.bat` on Windows remains authoritative before dev.20 can be called build-clean.
