# NEXT CHAT HANDOFF — dev.75i

## Target
Client chunk-settle convergence after dev.75h QA showed that queryable chunk membership alone was insufficient.

## Proven QA facts
- Stale/ghost Mature Cluster sources from older builds are fixed.
- Initial/rejoin light can still be clipped on chunk boundaries.
- Manual block updates repair later geometry.
- Unloading until LightLevelSimple no longer displays values, then returning, repairs the field.
- LightLevelSimple in this setup is the project's own NeoForge 1.21.1 backport, so it is treated as a trusted diagnostic consumer of effective light rather than a suspected incompatible third-party version.

## dev.75i changes
1. Server chunk-readiness-only rebuilds no longer spam unchanged source descriptors to clients.
2. New source creation still always broadcasts.
3. Real terrain/source changes still notify clients.
4. Identical client UPSERTs re-solve without tearing down source/readiness identity.
5. Each client source gets a 12-tick quiet-settle re-solve; dependency chunk activity resets this quiet timer.
6. Each client source also gets one bounded late verify at 80 ticks.
7. Existing 5x5-ish dependency window / pre-solve readiness fingerprint remains intact.

## QA
- Join/rejoin and do not touch blocks for at least 5 seconds.
- Check whether the field becomes complete by itself after the quiet/late passes.
- Repeat with F3+G, especially the previous SE/SW chunk borders.
- Confirm moving away/back is no longer needed.
- Confirm ordinary block placement/breaking inside the field still causes immediate recomputation.
- Confirm stale-source teardown remains fixed.
