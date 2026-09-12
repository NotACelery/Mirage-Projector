#!/usr/bin/env python3

# Pure-model sanity checks for the chunk dependency window used by dev.75h.

def chunk(v):
    return v // 16

def window(origin, radius):
    lo = chunk(origin - radius)
    hi = chunk(origin + radius)
    return lo, hi, hi - lo + 1

# Base Mature Cluster radius ~30: no axis may exceed five chunk columns.
for local in range(16):
    lo, hi, count = window(local, 30)
    assert count <= 5, (local, lo, hi, count)

# A source near the middle can indeed require five columns, matching the QA hypothesis.
assert window(8, 30)[2] == 5

# Boosted radius derives its own larger window rather than hardcoding +/-2 chunks.
assert max(window(local, 38)[2] for local in range(16)) >= 5
assert max(window(local, 38)[2] for local in range(16)) <= 6

print("dev.75h dependency-window numeric verification: PASS")
print("- base radius 30 => at most 5 chunk columns per axis")
print("- boosted radius 38 => window expands automatically, at most 6 columns per axis")
