# NEXT CHAT HANDOFF — Mirage Projector 1.0.12 Persistent Portable State

Baseline: `1.0.12`
Minecraft: `1.21.1`
NeoForge: `21.1.244+`
Java: `21`
Network protocol: `29`
ProjectionSettings format: `3`

## Delivered

- Mirage Hand Projector receives a stable ItemStack-owned device UUID.
- ON handheld projectors remain active and continue draining while stored in normal player inventory.
- Added `PortableProjectorStatePayload` (server -> client).
- Server publishes active portable state to same-dimension clients; vanilla tracking still supplies player movement/orientation.
- `ClientHeldProjectors` now caches synchronized active devices and renders them even when the owner is not visibly holding the projector.
- Synced portable state expires client-side after missed heartbeats so dropped/deleted devices do not leave permanent ghosts.
- Added `mirage_projector:creative_battery`, an infinite Creative/debug rechargeable medium using the generic `RechargeableEnergyItem` contract.
- War Banner design is frozen in `docs/WAITLIST-1.1.0.md`, but the final overhead/billboard renderer is intentionally not part of 1.0.12.

## Important files

- `src/main/java/celerbi/mirageprojector/item/MirageHandProjectorItem.java`
- `src/main/java/celerbi/mirageprojector/client/ClientHeldProjectors.java`
- `src/main/java/celerbi/mirageprojector/network/PortableProjectorStatePayload.java`
- `src/main/java/celerbi/mirageprojector/item/CreativeBatteryItem.java`
- `src/main/java/celerbi/mirageprojector/network/ModNetworking.java`
- `src/main/java/celerbi/mirageprojector/registry/ModItems.java`
- `src/main/resources/assets/mirage_projector/models/item/creative_battery.json`
- `src/main/resources/assets/mirage_projector/textures/item/creative_battery.png`
- `docs/WAITLIST-1.1.0.md`
- `docs/RELEASE-1.0.12-PERSISTENT-PORTABLE-STATE.md`

## QA priorities

1. Turn a Hand Projector ON, move it to a non-hotbar inventory slot and verify the hologram remains visible and charge continues dropping.
2. Confirm another client still sees that hologram while the owner is not holding the item.
3. Move the item repeatedly between hotbar/inventory slots; the projection should remain the same logical device.
4. Turn it OFF after pulling it back into hand; remote projection should disappear immediately.
5. Remove/drop an active projector without clean OFF and verify remote stale cleanup removes it shortly afterward.
6. Insert Creative Battery into Hand Projector, Mirage Lantern and placed Mirage Light Projector; charge must remain at 100% indefinitely.
7. Multiplayer relog/dimension-change smoke test for stale portable cache.

## Known follow-up

- War Banner overhead renderer and Billboard/Directional controls.
- Optimize portable state publication if large Entity profiles create unacceptable multiplayer bandwidth under many active projectors.
- Consider event-scoped tracking publication instead of same-dimension fanout if future stress QA shows need.
- Portable-projector self-configuration UI remains separate future work.

## Static verification result

```text
Mirage Projector 1.0.12 persistent portable-state verification PASS
Mirage Projector 1.0.12 release audit PASS (105 JSON, 19 blocks, 21 items, 524 lang keys)
MIRAGE PROJECTOR 1.0.12 VERIFICATION PASS (28 gates)
```
