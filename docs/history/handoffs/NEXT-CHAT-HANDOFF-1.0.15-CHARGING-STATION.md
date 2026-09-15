# NEXT CHAT HANDOFF — Mirage Projector 1.0.15 Dedicated Charging Station

Date: 2026-09-14
Baseline entering wave: 1.0.14 Shoulder Battery Pouch / Auto Swap
Current snapshot: 1.0.15 Dedicated Charging Station
Minecraft: 1.21.1
NeoForge: 21.1.244+
Java: 21
Network protocol: 31 (unchanged)
ProjectionSettings format: 3 (unchanged)

## User contract implemented

The new `mirage_projector:charging_station` is the dedicated batch charger for the rechargeable-energy ecosystem. It is intended to sit directly in an active Beacon column without requiring hopper/chute infrastructure above or below the beam.

Inventory flow is fixed at:

- slots 0..3: four rechargeable-only input queue slots;
- slot 4: one player-accessible active charging slot, hard limited to one physical item;
- slots 5..8: four ordered output slots.

The input queue may accept stacks. Only one unit at a time advances into the active slot. Once a cell reaches 100%, it moves into the leftmost local output able to accept it. If all local outputs are blocked, the full active cell remains in slot 4 and the queue stops.

## Direction / logistics contract

`ChargingStationBlock.FACING` is the output/front. Placement uses `context.getHorizontalDirection().getOpposite()`, so the marked output faces the placing player like a furnace instead of being hard-coded north.

NeoForge `Capabilities.ItemHandler.BLOCK` is registered for the block entity:

- output/front face: output-only, extraction allowed, insertion rejected;
- top/bottom/rear/left/right: input-only, insertion allowed, extraction rejected;
- unsided access: queue insertion + output extraction only; active slot remains private to the player GUI/internal machine flow.

The station also performs hopper-like active ejection every 8 ticks. It queries the adjacent front inventory through the standard block item-handler capability and pushes one completed item. Outputs are scanned left-to-right; if a filtered destination rejects one completed medium, later outputs are still attempted during the same push pass.

No Create/filtered-hopper/vanilla-container class is hard-coded; compatibility is through the item-handler capability surface.

## Beacon charging architecture

New shared interface:

- `energy/BeaconRechargeableCharger.java`

Both `CoreBoosterBlockEntity` and `ChargingStationBlockEntity` implement it. `GlowDustBeaconCharging` now reasons about the shared interface while retaining the historical Core Booster overload for compatibility/verifiers.

An incomplete active cell consumes the existing 0.20 absolute Beacon transmission. Full/blocked cells do not consume charging transmission. Crying Obsidian optics and the custom Beacon renderer use the same generalized active-charger predicate.

The Core Booster material-relay identity remains separate; the station does not inherit or replace Glass/Quartz/etc. relay behavior.

## Registered content

New block/item:

- `mirage_projector:charging_station`

New block entity/menu:

- `mirage_projector:charging_station`

Resources include horizontal blockstate, block/item models, three 16x16 first-pass textures, block loot and pickaxe mineable tag entry.

The first art pass prioritizes a glass/dark-purple body and an unmistakable output face. Final art and Survival recipe remain 1.1 progression/polish work.

## GUI

`ChargingStationMenu` + `ChargingStationScreen` provide a compact cutter-style flow:

`4 queued inputs -> 1 active charger -> 4 outputs`

Player interaction can directly service the active slot; it is not exposed to automation. Output slots reject manual insertion. Shift-click of rechargeable media prefers the input queue and falls back to the active slot.

## Version / documentation

- `gradle.properties`: 1.0.15
- protocol remains 31: no new custom payload was required.
- `docs/WAITLIST-1.1.0.md` marks Dedicated Charging Station as delivered foundation; final recipe/art remain open.
- current implementation, roadmap, version scope, registry inventory, architecture, changelog, release note and README were updated.
- UV remains entirely 1.2.0 scope.

## Verification

Final working-tree result:

`MIRAGE PROJECTOR 1.0.15 VERIFICATION PASS (35 gates)`

Release audit result:

`Mirage Projector 1.0.15 release audit PASS (112 JSON, 20 blocks, 25 items, 567 lang keys)`

Known non-blocking historical deprecation count: 12 `EventBusSubscriber.Bus` sites.

No real Gradle/NeoForge compilation was run in this environment. Authoritative next check remains Windows `build.bat` under Java 21 followed by in-game QA.

## High-value in-game QA

1. Place the station facing north/east/south/west and verify marked output faces the placer.
2. Place it in a live Beacon beam and confirm the beam continues through the glass-like station.
3. Insert stacked Glow Dust / Light Batteries from each of five non-front faces through hoppers/chutes/compatible logistics.
4. Confirm active slot physically remains count 1.
5. Confirm completed cells fill local outputs from left to right.
6. Fill all outputs and verify the full active cell remains in place and charging/queue stops.
7. Attach chest/hopper/dispenser/dropper/filtered/mod inventory to the front and verify automatic push without top/bottom logistics.
8. Use a filtered target that rejects output slot 1 but accepts a later output medium; verify later output can still eject.
9. Verify front insertion is rejected and non-front extraction is rejected.
10. Stack multiple active chargers/Core Booster cradles in one Beacon column and verify shared 0.20 transmission attenuation/visuals.
11. Confirm Creative Battery passes through immediately as already-full media without consuming beam transmission.

## Recommended next work after QA

Do not start UV in 1.1. Remaining major 1.1 candidates include War Banner, Scan Codex / Duplicating Lectern, final shoulder/device GUI polish, table/wall projector families, End Resonance and progression/recipe balancing. Charging Station recipe/art can be finalized once its physical/logistics QA is accepted.
