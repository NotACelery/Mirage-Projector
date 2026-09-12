# dev.75h — Dependency-window stable solve

## QA evidence
The remaining failure aligned exactly to chunk borders. On reload the field could remain clipped to one or more chunk columns; a manual block update could advance the field only one additional chunk at a time. dev.75f successfully removed stale/ghost sources, so this patch focuses only on convergence.

## Root cause
dev.75g captured `queryableChunksTouched()` after rebuilding. A chunk could be unavailable when the flood reached its border, become queryable while/just after the solve ran, and then be included in the post-solve readiness snapshot. Subsequent ticks saw no readiness change even though that chunk never participated in the solve.

## Fix
1. Build a deterministic dependency window from `origin ± profile.maxRadius()`.
2. Watch the whole axis-aligned chunk window, not only chunks reached by/tightly intersecting the previous solve.
3. Capture queryable dependency chunks before each solve and associate that exact snapshot with the resulting field.
4. If readiness changes during the client solve, request another stabilization pass. Even without that fast path, the stored pre-solve snapshot guarantees the next tick detects the change.
5. Server readiness audit mirrors the same pre-solve fingerprint rule.

## Performance
No chunk tickets are created and no chunk is force-loaded. For the base Mature Cluster radius (~30), the bounding window is normally at most 5×5 chunks depending on local source position. Each readiness audit is therefore only a few dozen `getChunkNow()` probes per source. Higher boosted radii derive their window automatically.

## QA
- Reload the exact chunk-border test world with F3+G.
- Do not place/break blocks while observing convergence.
- The field must expand on its own as dependency chunks become queryable.
- No one-chunk-at-a-time manual update behavior.
- Repeat with source near a chunk center and near a chunk corner.
- Repeat after walking far enough to unload/reload surrounding chunks.
- Reconfirm ghost-source removal remains fixed.

## Diagnostic command
`/miragelight chunks` reports the nearest source dependency readiness as `queryable/total`, its radius and the last solve's `unloadedEdges`. Use this if a chunk-border cut still reproduces.
