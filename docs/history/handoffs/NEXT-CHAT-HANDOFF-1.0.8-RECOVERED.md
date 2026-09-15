# NEXT CHAT HANDOFF — Mirage Projector 1.0.8 recovered battery wave

Date: 2026-09-13
Base recovered exactly: user-provided Mirage Projector 1.0.7 Glow Dust Battery Foundation source snapshot.
Current reconstructed snapshot: 1.0.8.
Minecraft 1.21.1 / NeoForge 21.1.244+ / Java 21 / protocol 28.

## Why this snapshot exists

The preceding chat ended after requirements for the next battery/charging wave had been discussed and implementation had begun, but no final ZIP was handed to the user. The actual un-delivered source diff was not recoverable byte-for-byte. This snapshot rebuilds only requirements that were recoverable with confidence on top of the confirmed 1.0.7 source.

## Implemented in reconstructed 1.0.8

- `RechargeableEnergyItem` generic item-owned charge contract.
- Existing Glow Dust migrated onto that contract without changing its 1000-unit capacity, 1.0.7 custom-data key or +10/10-tick charging baseline.
- New Light Battery:
  - 4000-unit capacity (~4× Glow Dust);
  - +8 charge per 10-tick Core Booster pulse;
  - ~250 s empty-to-full, ~5× Glow Dust's ~50 s baseline;
  - full/partial/depleted name, tooltip, bar and tint state;
  - dedicated 16×16 frame + tintable charge-layer textures;
  - Creative/Ingredients charged and depleted QA entries.
- Core Booster charging slot accepts generic rechargeable media.
- Existing persisted `ChargingGlowDust` key deliberately retained for 1.0.7 world compatibility.
- Sneak+RMB charging-cell extraction now inserts into inventory first, allowing equal item/components/charge state to merge; overflow drops safely.
- Core Booster charging-item render reduced and recentered to avoid clipping/collision with glass.
- Jade shows inserted charging medium + percentage or explicit empty charging state.
- Beacon transmission rule unchanged: -0.20 absolute transmission per actively charging cell; five active cells remain the clear-column maximum.
- Crying Obsidian optics/custom Beacon renderer use the same generalized charging state.

## Documented now, intentionally not implemented yet

### Light Battery recipe

Recovered target: approximately five Glow Dust plus additional casing/electrical materials. The rest of the recipe was not frozen, so no survival recipe was invented.

### Dedicated Charging Station

Planned GUI/flow:

- queue up to 4 rechargeable cells/batteries;
- exactly 1 actively charging slot;
- 4–5 completed-output slots;
- auto-advance input -> active -> output when space is available;
- uses the same item-owned `RechargeableEnergyItem` data contract.

Block recipe, power source, exact output count and visual model remain open.

### Projection-energy integration

Requirements recovered but numeric balance was not:

- Glow Dust may become a weak projection-energy medium but should not be consumed simply for maintaining a hologram.
- Light Battery may also become a projection-energy medium.
- Lift PU efficiency may differ by medium.
- battery-backed projections may emit Mirage light.

Do not implement exact base PU, Lift multipliers or projection-light profiles until those values are explicitly frozen with the user.

## QA gates

- Static current-line suite: `python tools/verify_current_line.py`.
- Final compile/runtime gate: Windows `build.bat` under Java 21.
- In-game QA should specifically test old 1.0.7 Glow Dust in an existing Core Booster, battery timing, stack merge on extraction, Booster item positioning and Jade charging state.
