# Mirage Projector 1.0.8 — Rechargeable Energy / Light Battery Foundation

1.0.8 is a recovery snapshot reconstructed on top of the confirmed 1.0.7 Glow Dust foundation after the preceding development chat reached its context limit before its final implementation was packaged. It keeps network protocol 28 and `ProjectionSettings` format 3.

## Runtime changes

- Added the shared `RechargeableEnergyItem` contract so item-owned charge capacity and per-pulse recharge rate belong to the energy medium rather than the Core Booster.
- Glow Dust remains fully compatible with the 1.0.7 1000-unit charge model and gains 10 units per 10-tick Core Booster pulse.
- Added `mirage_projector:light_battery` with 4000-unit capacity and +8 units per charging pulse. From empty, the current baseline is about 250 seconds versus about 50 seconds for Glow Dust.
- Core Booster charging cradles accept any supported rechargeable medium while keeping the normal Core-material socket independent.
- The legacy block-entity persistence key `ChargingGlowDust` is intentionally retained so 1.0.7 worlds do not lose the inserted charging item.
- Sneak-right-click extraction now uses normal inventory insertion first. Identical item/component states can merge naturally; overflow is dropped safely.
- Charging-item rendering is smaller and centered on the Booster axis to avoid Glow Dust/Light Battery geometry clipping through the glass shell.
- Jade now reports the current charging medium and percentage, or an explicit empty charging-cell state.
- Beacon charging still costs 0.20 absolute outgoing transmission per actively charging cell. A clear path therefore supports at most five active cells, regardless of which supported rechargeable medium is inserted.

## Light Battery presentation

The Light Battery has a generated 16×16 two-layer item model: a static casing/frame and a tintable charge layer. Full/partial/depleted state is represented by item charge data, tint, tooltip and the normal item bar. It is exposed in Creative/Ingredients in both charged and depleted QA states.

## Intentionally unresolved

The final Light Battery survival recipe is not fabricated in this snapshot. The recovered design only fixed the target at approximately five Glow Dust plus additional casing/electrical materials.

Likewise, 1.0.8 does not invent:

- Glow Dust / Light Battery projection-PU values;
- Lift-efficiency multipliers for battery-backed projection;
- battery-backed hologram light intensity/profile;
- lantern discharge rates;
- the final dedicated Charging Station recipe/power source.

The Charging Station UX target is documented in `WAITLIST-1.1.0.md`: up to four queued rechargeable cells, one active charging slot and four to five completed-output slots.

## Recovery confidence

The 1.0.7 uploaded source is the last byte-exact base available from the prior chat. The requirements above were recoverable from project context, but the un-delivered post-1.0.7 source diff was not. Therefore 1.0.8 is a source reconstruction of those confirmed requirements rather than a claim that the exact lost bytes were recovered.

Run:

```text
python tools/verify_current_line.py
```

A Windows Java 21 `build.bat` compile/runtime smoke test remains the final compiled-build gate.
