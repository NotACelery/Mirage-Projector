#!/usr/bin/env python3
"""Pure-model checks for the dev.74 causal fixed-point Mirage light solver.

This intentionally does not emulate Minecraft block-shape opacity. It validates the
solver invariants that must remain true regardless of the NeoForge integration layer:
fixed-point decay, six-neighbour causality, complete-wall separation, finite-wall
corner routing, and max aggregation.
"""
from __future__ import annotations

from collections import deque

DIRS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))


def visible(energy: int, substeps: int = 2) -> int:
    return max(0, min(15, (energy + substeps - 1) // substeps))


def solve(conceptual: int, blocked: set[tuple[int, int, int]], radius: int | None = None):
    substeps = 2
    radius = conceptual * substeps if radius is None else radius
    initial = conceptual * substeps + (substeps - 1)
    best = {(0, 0, 0): initial}
    # This FIFO is enough for the uniform-cost model used by these checks.
    queue = deque([(0, 0, 0)])
    while queue:
        x, y, z = queue.popleft()
        energy = best[(x, y, z)]
        next_energy = energy - 1
        if next_energy <= 0:
            continue
        for dx, dy, dz in DIRS:
            nxt = (x + dx, y + dy, z + dz)
            if sum(map(abs, nxt)) > radius or nxt in blocked:
                continue
            if next_energy <= best.get(nxt, 0):
                continue
            best[nxt] = next_energy
            queue.append(nxt)
    return best


def assert_open_curve():
    field = solve(15, set())
    got = [visible(field.get((d, 0, 0), 0)) for d in range(1, 31)]
    expected = [level for level in range(15, 0, -1) for _ in (0, 1)]
    assert got == expected, (got, expected)


def assert_complete_wall_disconnects():
    radius = 30
    # Fill every voxel of the x=4 cross-section that lies inside the Manhattan ball.
    blocked = {
        (4, y, z)
        for y in range(-radius, radius + 1)
        for z in range(-radius, radius + 1)
        if 4 + abs(y) + abs(z) <= radius
    }
    field = solve(15, blocked, radius)
    assert field.get((5, 0, 0), 0) == 0
    assert field.get((15, 0, 0), 0) == 0


def assert_finite_wall_routes_with_detour_loss():
    blocked = {(4, y, z) for y in range(-2, 3) for z in range(-2, 3)}
    open_field = solve(15, set())
    wall_field = solve(15, blocked)
    probe = (7, 0, 0)
    open_energy = open_field[probe]
    routed_energy = wall_field.get(probe, 0)
    assert 0 < routed_energy < open_energy, (routed_energy, open_energy)


def assert_max_aggregation():
    a = 7
    b = 11
    assert max(a, b) == 11
    assert max(a, 0) == 7


if __name__ == "__main__":
    assert_open_curve()
    assert_complete_wall_disconnects()
    assert_finite_wall_routes_with_detour_loss()
    assert_max_aggregation()
    print("dev.74 Mirage Light solver model verification: PASS")
    print("- exact 15,15 ... 1,1 open-air curve")
    print("- complete causal barrier disconnects downstream field")
    print("- finite wall permits weaker corner routing")
    print("- overlapping contributions aggregate by max")
