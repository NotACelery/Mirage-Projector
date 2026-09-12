# NEXT CHAT HANDOFF — 0.1.0-dev.75h

## Baseline
`dev.75h` is the current source candidate. It accumulates dev.75d Mirage Light Engine, dev.75e alternate projector comparison models, dev.75f ghost-source teardown and dev.75g source-centric chunk convergence.

## Latest QA finding
Chunk-border clipping still reproduced on dev.75g. The critical race was post-solve readiness capture: a chunk could become queryable after the flood had already stopped at its boundary, then be incorrectly recorded as available for that solve.

## dev.75h fix
- Explicit axis-aligned dependency window from `origin ± maxRadius` (base field normally <= 5×5 chunks).
- No force-loading/chunk tickets.
- Readiness fingerprint captured BEFORE solving.
- Client requests stabilization if readiness changes during a solve.
- Server audit stores the same pre-solve snapshot.
- Chunk fast paths use dependency-window membership.

## QA gate
Build on Windows Java 21, then reload the established iron-platform/chunk-border test without manually updating blocks. The field must converge across all available dependency chunks by itself. Also reconfirm old ghost lights remain gone.

## Diagnostic command
`/miragelight chunks` reports the nearest source dependency readiness as `queryable/total`, its radius and the last solve's `unloadedEdges`. Use this if a chunk-border cut still reproduces.
