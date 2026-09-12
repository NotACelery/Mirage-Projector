#!/usr/bin/env python3
"""Regression model for dev.75d obstacle-detour decay.

Open travel keeps the fixed-point half-decay contract. Only path length above the
source-to-voxel Manhattan minimum receives extra cost. For the current Mature
profile (2 substeps per visible level), direct travel costs 1 internal unit per
block and obstacle-only detour travel costs 2 internal units per extra block.
"""
from __future__ import annotations

import heapq
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIRS = ((1, 0), (-1, 0), (0, 1), (0, -1))


def visible(energy: int, substeps: int = 2) -> int:
    return max(0, min(15, (energy + substeps - 1) // substeps))


def solve(blocked: set[tuple[int, int]], conceptual: int = 15):
    substeps = 2
    air_cost = 1
    detour_extra = 1
    radius = conceptual * substeps
    initial = conceptual * substeps + (substeps - 1)
    origin = (0, 0)
    best = {origin: initial}
    queue = [(-initial, origin)]
    while queue:
        negative_energy, current = heapq.heappop(queue)
        energy = -negative_energy
        if best.get(current) != energy:
            continue
        x, y = current
        direct_here = abs(x) + abs(y)
        for dx, dy in DIRS:
            nxt = (x + dx, y + dy)
            if nxt in blocked:
                continue
            direct_next = abs(nxt[0]) + abs(nxt[1])
            if direct_next > radius:
                continue
            edge_cost = air_cost
            if direct_next < direct_here:
                # One inward edge increases (pathLength - directDistance) by 2.
                edge_cost += 2 * detour_extra
            next_energy = energy - edge_cost
            if next_energy <= 0 or next_energy <= best.get(nxt, 0):
                continue
            best[nxt] = next_energy
            heapq.heappush(queue, (-next_energy, nxt))
    return best


# 1) Open-space contract must be untouched.
open_field = solve(set())
open_curve = [visible(open_field.get((distance, 0), 0)) for distance in range(1, 31)]
expected = [level for level in range(15, 0, -1) for _ in (0, 1)]
assert open_curve == expected, (open_curve, expected)

# 2) A finite wall forces a real detour. Probe is 6 direct blocks from the source.
# Shortest routed path is 8 blocks: 2 extra path steps. Base half-decay would cost 8
# internal units; dev.75d adds 2 extra units, producing weighted cost 10.
wall = {(3, y) for y in range(0, 3)}
wall_field = solve(wall)
probe = (6, 0)
initial = 31
assert open_field[probe] == initial - 6
assert wall_field[probe] == initial - 10, wall_field[probe]
assert visible(open_field[probe]) == 13
assert visible(wall_field[probe]) == 11

# 3) A location reached monotonically without detouring keeps the open-space result.
side_probe = (3, -1)
assert wall_field[side_probe] == open_field[side_probe]

# 4) Source code/profile/network must expose the explicit detour contract.
profile = (ROOT / 'src/main/java/celerbi/mirageprojector/light/engine/MirageLightProfile.java').read_text(encoding='utf-8')
solver = (ROOT / 'src/main/java/celerbi/mirageprojector/light/engine/MirageLightSolver.java').read_text(encoding='utf-8')
payload = (ROOT / 'src/main/java/celerbi/mirageprojector/network/MirageLightChunkSnapshotPayload.java').read_text(encoding='utf-8')
main = (ROOT / 'src/main/java/celerbi/mirageprojector/MirageProjector.java').read_text(encoding='utf-8')
assert 'detourExtraCostUnits' in profile
assert 'detourBacktrackPenaltyUnits' in profile
assert 'nextDirectDistance < currentDirectDistance' in solver
assert 'PACKED_SECTION_BYTES' in payload and 'revision' in payload
assert 'NETWORK_PROTOCOL = "25"' in main

print('dev.75d Mirage Light detour-penalty verification: PASS')
print('open probe level:', visible(open_field[probe]), 'wall-routed probe level:', visible(wall_field[probe]))
print('open curve:', ' '.join(map(str, open_curve)))
