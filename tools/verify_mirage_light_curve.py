#!/usr/bin/env python3
"""Pure-math verification for the dev.74 fixed-point Mirage half-decay contract."""


def sequence(conceptual_light: int, substeps: int = 2):
    initial = conceptual_light * substeps + (substeps - 1)
    result = []
    for distance in range(1, conceptual_light * substeps + 1):
        energy = initial - distance
        visible = max(0, min(15, (energy + substeps - 1) // substeps))
        result.append(visible)
    return result


BASE_EXPECTED = [level for level in range(15, 0, -1) for _ in (0, 1)]
base = sequence(15)
assert base == BASE_EXPECTED, (base, BASE_EXPECTED)

boosted = sequence(19)
assert len(boosted) == 38
assert boosted[-2:] == [1, 1]
assert boosted.count(15) == 10

print("dev.74 fixed-point half-decay verification: PASS")
print("conceptual 15:", " ".join(map(str, base)))
print("conceptual 19:", " ".join(map(str, boosted)))
