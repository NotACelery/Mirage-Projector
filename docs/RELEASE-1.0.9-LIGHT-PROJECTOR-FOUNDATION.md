# Mirage Projector 1.0.9 — Physical Light Projector / DYNAMIC_VISUAL Consumer

1.0.9 turns the previously infrastructure-only moving-light runtime into real gameplay through the first physical Mirage illumination device. Network protocol remains 28; fixed projector `ProjectionSettings` remains format 3, because this wave adds no packet or settings-wire shape changes.

## Physical Mirage Light Projector

New registry ID: `mirage_projector:mirage_light_projector`.

The block is a horizontally oriented reflector-style emitter and owns one real rechargeable-energy slot. It accepts any item implementing `RechargeableEnergyItem`, so Glow Dust and Light Battery work without device-specific battery code.

Interaction contract:

- normal right-click with Glow Dust or Light Battery inserts one rechargeable medium when the slot is empty;
- Shift + right-click removes the exact inserted stack and first attempts normal inventory insertion, so matching charge/component state can merge naturally; overflow drops safely;
- normal right-click with a loaded projector advances exactly one mode in the cycle `Focus → Flood → Ambient → Off → Focus`;
- normal right-click with no rechargeable medium installed is consumed by the block but intentionally produces no visible mode change;
- the default mode is Focus;
- mode and inserted ItemStack persist across save/reload and synchronize through normal block-entity update packets.

The inserted cell is also rendered physically in the rear cradle.

## Dynamic light consumer

The client now discovers synchronized placed Mirage Light Projectors in nearby loaded chunks and submits them into `ClientDynamicMirageLightManager` using a stable block-position source identity. The existing DYNAMIC_VISUAL runtime remains client-local; server state owns mode and battery while each client solves the visual light field from the synchronized device state.

Mode shapes:

- Focus — narrow long directional cone;
- Flood — wider, shorter directional cone;
- Ambient — local omnidirectional field;
- Off — no field and no drain.

The numerical cone/radius/drain values are centralized in `PortableLightMode` as QA balance values rather than spread across the light engine or interaction code.

## Rechargeable-energy consumption

The server consumes charge once per second while the projector is actively emitting. The current QA baseline is:

| Mode | Charge / second | Glow Dust nominal runtime | Light Battery nominal runtime |
|---|---:|---:|---:|
| Focus | 4 | ~4m 10s | ~16m 40s |
| Flood | 2 | ~8m 20s | ~33m 20s |
| Ambient | 1 | ~16m 40s | ~66m 40s |
| Off | 0 | no drain | no drain |

These are tuning values, not an architectural freeze. Light Battery remains exactly four times Glow Dust capacity, so it lasts four times as long under the same device mode.

When a cell reaches zero charge, the selected mode remains persisted but the projector stops emitting until the cell is recharged or replaced.

## Feedback / Jade

Mode-change actionbar feedback uses the exact strings:

```text
Projector: Focus
Projector: Flood
Projector: Ambient
Projector: Off
```

Insertion/extraction feedback includes battery percentage. Jade shows the current mode plus inserted rechargeable medium and percentage, or an explicit empty-battery state.

A richer timed HUD overlay for the broader 1.1 portable family can build on the same synchronized device state; 1.0.9 does not introduce a second networking model only for HUD presentation.

## Deliberately still open

1.0.9 does not invent:

- a survival recipe for the physical light projector;
- the final 1.1 lantern item interaction model;
- final Focus/Flood/Ambient balance;
- stationary DYNAMIC_VISUAL re-solve hooks for arbitrary nearby block edits;
- projection-energy PU/Lift values for Glow Dust or Light Battery;
- battery-backed hologram illumination values;
- the dedicated Charging Station implementation.

Run `python tools/verify_current_line.py` before packaging. Windows Java 21 `build.bat` remains the final compile/runtime gate.
