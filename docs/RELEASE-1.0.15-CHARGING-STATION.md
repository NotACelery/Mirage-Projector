# Mirage Projector 1.0.15 — Dedicated Charging Station

## Scope

This snapshot implements the batch Beacon charger frozen in the 1.1 roadmap. It does not add UV behavior, War Banner logic or new rechargeable media.

## Inventory flow

- input queue: 4 rechargeable-only slots;
- active charger: 1 rechargeable-only slot with a hard stack limit of 1;
- output: 4 ordered slots;
- queue advances one physical item at a time;
- a 100% cell moves to the leftmost compatible output;
- if all outputs are blocked, the full active cell remains in place and no next cell begins charging.

## Logistics

The station has a horizontal output/front that faces the placing player. NeoForge item capabilities expose insert-only input access on the other five faces and extract-only output access on the front. The station additionally pushes one item every eight ticks into a compatible adjacent inventory on that front face. Output candidates are scanned left-to-right, and a filtered target rejecting one completed medium does not prevent a later compatible output from being attempted. No vanilla/mod transport block is hard-coded.

## Beacon charging

The station shares the existing `RechargeableEnergyItem` and Beacon attenuation contract. An incomplete active cell consumes 0.20 outgoing transmission, exactly like an active Core Booster charging cradle. The separate Core Booster material relay remains unchanged.

## QA focus

1. Place from all four horizontal directions and confirm the output texture always faces the placer.
2. Insert rechargeable media through top, bottom, rear, left and right automation; verify front insertion is rejected.
3. Confirm the active slot never contains more than one item even when input receives a stack.
4. Fill all four outputs and confirm a newly completed cell stays in the active slot at 100%.
5. Free one output and confirm the active full cell moves into the leftmost compatible output and the queue resumes.
6. Attach chest/hopper/dropper/dispenser/mod inventory to the front and confirm one-item-per-8-tick automatic ejection.
7. Confirm front-side external extraction works and non-front extraction is rejected.
8. Confirm Glow Dust, Light Battery and Creative Battery all follow the generic rechargeable contract.
9. Confirm active charging attenuates the Beacon path and completed/blocked full cells do not.
