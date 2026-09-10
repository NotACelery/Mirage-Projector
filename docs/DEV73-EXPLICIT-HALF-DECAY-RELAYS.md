# dev.73 — Explicit full-range Mature half-decay relays

## Why this pass exists

Live QA with a block-light level overlay showed that the accumulated dev.72 implementation did not actually hold every light level for two blocks. The visible curve doubled only roughly through levels 14–11; level 15 and the 10–1 tail decayed too quickly.

The previous implementation placed Mirage relay blocks only at one position per two-block decay bucket and relied on vanilla propagation from that sparse source to fill the unsampled position. That mathematical shortcut was not a reliable runtime contract.

## dev.73 contract

World-light remains limited to the six source-causal axial branches introduced/stabilized by dev.70. The difference is density: every accepted air cell on a branch is now an explicit relay candidate.

For an energized Mature Cluster without a Core Booster, the intended axial sequence is:

```text
distance  1  2  3  4  5  6 ... 27 28 29 30
level    15 15 14 14 13 13 ...  2  2  1  1
```

The Cluster's own block remains the physical source at level 15. The 30-block sequence above describes positions outward from it.

Core Booster power may raise conceptual light above 15, but no real Minecraft block-light node may exceed 15. Extra conceptual power is represented as a longer saturated level-15 plateau followed by the same half-speed tail. With the existing four-effective-Booster cap, the maximum conceptual level is 19 and the maximum axial candidate distance becomes 38 blocks.

## Occlusion is unchanged

Dense relays do not weaken the dev.70 wall contract. A full opaque block terminates its entire downstream branch. Partial light blockers add path penalty. Terrain edits are still coalesced and rebuilt at the end of the same server tick. Glass still does not seed long diagonal static-light branches.

## Performance note

The no-Booster field grows from roughly 15 sampled relay positions per direction to 30 explicit positions per direction; maximum boosted branches grow to 38 positions per direction. This is intentional for deterministic measured levels, but multi-source/overlap QA must watch for server hitching before build-clean acceptance.

## Acceptance

Use the Simple Light Level backport or another block-light numeric overlay and verify every no-Booster level from 15 through 1 occupies exactly two consecutive positions on an unobstructed primary axis. Then repeat opaque-wall, unblock, overlap, Quartz and Diamond tests from `QA-REGRESSION.md`.
