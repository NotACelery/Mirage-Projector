# NEXT CHAT HANDOFF — Mirage Projector 1.0.9 physical light projector

Current source snapshot: **1.0.9**.
Minecraft 1.21.1 / NeoForge 21.1.244+ / Java 21 / network protocol **28** / `ProjectionSettings` format **3**.

This handoff exists specifically so the implementation can be resumed even if the development chat reaches its context limit before the next package is delivered.

## What 1.0.9 adds

### Physical Mirage Light Projector

Registry ID: `mirage_projector:mirage_light_projector`.

The new block is a persistent horizontally-oriented reflector device with one real rechargeable `ItemStack` slot. It accepts any `RechargeableEnergyItem`; currently that means Glow Dust and Light Battery.

Interaction contract:

- normal RMB with rechargeable medium inserts one cell when empty;
- Shift + RMB extracts the exact inserted stack, first using normal inventory merge semantics and dropping only overflow;
- a loaded projector cycles exactly `Focus → Flood → Ambient → Off → Focus` with one normal RMB per step;
- no cell means normal RMB has no visible mode effect;
- default mode is Focus;
- breaking the block returns the inserted cell;
- mode + exact cell components/charge persist through save/load and synchronize through the block entity update packet.

Exact mode actionbar strings are intentionally frozen as:

- `Projector: Focus`
- `Projector: Flood`
- `Projector: Ambient`
- `Projector: Off`

The rear cradle physically renders the inserted rechargeable item.

### First real DYNAMIC_VISUAL consumer

`ClientPlacedLightProjectors` scans nearby loaded client chunks once per game tick, resolves synchronized projector block entities and submits a stable block-position source to `ClientDynamicMirageLightManager`.

- Focus/Flood use `DIRECTIONAL_CONE` profiles pointed along block facing.
- Ambient uses the shared omnidirectional profile.
- Off, empty slot and depleted cells do not emit.
- turning Off / becoming non-emitting explicitly removes the installed dynamic source rather than waiting only for stale timeout.
- the client scan radius is 64 blocks; current light-profile radii are smaller than that.

Server state owns battery/mode. The dynamic light solve remains client-local visual state and does not publish into authoritative `STATIC_WORLD` chunk snapshots.

### Centralized QA balance

`PortableLightMode` is the single balance authority for this device family. Current values are deliberately QA values, not a permanent balance freeze:

| Mode | Conceptual light | Radius | Shape | Cone | Drain/s |
|---|---:|---:|---|---:|---:|
| Focus | 15 | 24 | directional | 22° | 4 |
| Flood | 15 | 16 | directional | 72° | 2 |
| Ambient | 14 | 12 | omnidirectional | — | 1 |
| Off | 0 | 0 | none | — | 0 |

Nominal runtime:

- Glow Dust 1000: Focus ~4m10s, Flood ~8m20s, Ambient ~16m40s.
- Light Battery 4000: Focus ~16m40s, Flood ~33m20s, Ambient ~66m40s.

Drain occurs server-side once per second. A depleted cell leaves the selected mode intact but stops emission until recharged/replaced.

## Files added in 1.0.9

Java:

- `light/device/PortableLightMode.java`
- `block/MirageLightProjectorBlock.java`
- `blockentity/MirageLightProjectorBlockEntity.java`
- `event/MirageLightProjectorInteractionEvents.java`
- `client/ClientPlacedLightProjectors.java`
- `client/MirageLightProjectorRenderer.java`

Resources:

- `blockstates/mirage_light_projector.json`
- `models/block/mirage_light_projector.json`
- `models/item/mirage_light_projector.json`
- `loot_table/blocks/mirage_light_projector.json`

QA/docs:

- `tools/verify_1_0_9_light_projector.py`
- `tools/verify_release_1_0_9.py`
- `docs/RELEASE-1.0.9-LIGHT-PROJECTOR-FOUNDATION.md`
- this handoff.

Registry, creative-tab, Jade, language, client-render and runtime-event files were updated accordingly.

## Protocol decision

Keep network protocol **28**. 1.0.9 adds no payload and changes no existing packet/settings wire schema. Do not bump merely because the mod version changed.

## Verification at packaging time

`python tools/verify_current_line.py` passes **23/23 gates**.

Release auditor result:

- 102 JSON files
- 19 registered blocks
- 18 registered items
- 478 keys per locale (`en_us`, `es_cl`, `es_es`), with parity

A Windows Java 21 `build.bat` run is still required as the compiled/in-game gate because this source-only environment has Java 21 but no Gradle executable/wrapper.

## First in-game QA to perform

1. Place the new light projector facing each horizontal direction and confirm beam direction follows the physical front.
2. RMB with full/partial Glow Dust and Light Battery; verify exact charge survives insertion/extraction.
3. Cycle all four modes and confirm actionbar literals and immediate visual change.
4. Let each mode drain for at least 10 seconds and compare percentage change to expected rate.
5. Let a cell reach zero: selected mode should persist while light stops.
6. Recharge/exchange the cell: selected mode should resume without being reset.
7. Save/reload and relog with a partial cell and non-default mode.
8. Two-player test: both clients should see the same selected mode/light state after block-entity sync.
9. Break the device with a partial cell inserted and confirm the cell drops exactly once with charge intact.
10. Test Focus/Flood against walls/corners and confirm the existing Mirage occlusion/detour behavior still applies.

## Intentionally not invented in 1.0.9

- survival crafting recipe for the light projector;
- final Focus/Flood/Ambient balance;
- handheld lantern/device item and its first-person interaction model;
- dedicated Charging Station;
- rechargeable-media PU values for hologram projectors;
- battery-backed hologram illumination values;
- arbitrary nearby block-edit invalidation specifically optimized for stationary DYNAMIC_VISUAL devices.

## Best next implementation chunk

After the Windows/in-game QA above, the clean continuation is the **handheld/mobile illumination layer** on top of the same `PortableLightMode` + `RechargeableEnergyItem` + `DYNAMIC_VISUAL` seam. Do not duplicate a second light solver or battery system. If placement/model direction needs correction from QA, fix that first because the handheld layer should share the same profile semantics.
