# NEXT CHAT HANDOFF — dev.75j

Current source candidate: **0.1.0-dev.75j**. Network protocol remains **21**.

## QA finding that changed the diagnosis
The apparent chunk-border propagation failure was being observed through the private NeoForge 1.21.1 LightLevelSimple backport. Inspection of that source showed that labels are cached per `SectionPos`: they are recomputed after vanilla `ClientChunkCache.onLightUpdate(...)`, after block changes, or after chunk/cache rediscovery. Mirage field changes were only calling `LevelRenderer#setSectionDirty`, so the overlay could retain a patchwork of values sampled during different intermediate client solves.

This exactly explains several observations:
- breaking a random block repaired nearby numbers;
- unloading/reloading chunks repaired their numbers;
- removing/replacing the cluster did not necessarily refresh the same cached sections;
- the apparent failures aligned sharply to chunk/section boundaries and could change shape between joins.

## dev.75j change
`ClientMirageLightSync.markSectionsDirty(...)` now publishes every affected Mirage section through:

```java
level.getChunkSource().onLightUpdate(LightLayer.BLOCK, sectionPos);
```

It also retains the existing `levelRenderer.setSectionDirty(...)` call.

This is a generic vanilla-compatible invalidation bridge, not a direct dependency on LightLevelSimple. Any cached client light consumer listening to normal light updates can now refresh when Mirage virtual light changes. Mirage still does not write into vanilla BlockLightEngine storage.

## Next QA
1. Build/run dev.75j.
2. Join the same test world with LightLevelSimple enabled.
3. Do not break blocks or move out of range.
4. Observe the cluster for 5-10 seconds. Labels should now re-evaluate whenever Mirage replaces its virtual field instead of freezing per chunk.
5. Remove/re-place the cluster. All affected labels should immediately invalidate without requiring a manual nearby block update.
6. If the overlay now converges but actual world lighting is still geometrically wrong, the remaining issue is genuinely inside Mirage field solving/rendering and can be debugged without observer-cache ambiguity.

## Important
Do not remove the dev.75f ghost-source teardown or the later chunk-convergence changes yet. dev.75j intentionally isolates the observer invalidation bug first.
