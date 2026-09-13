# dev.76h — section-boundary invalidation fix

## Root cause found
The long-running apparent "missing chunk" failure was strongly amplified by the QA overlay cache rather than only by Mirage chunk transport.

LightLevelSimple stores labels by the section containing the **floor block**, but samples block light at the **block above** that floor. On a floor at Y=79, labels live in section Y=4 while the sampled light lives in section Y=5. Mirage notified `ClientChunkCache.onLightUpdate()` only for Y=5. Therefore cached labels in Y=4 could stay vanilla-only until a block update or chunk reload rebuilt them. Since the overlay scans chunks incrementally, this appeared as random whole horizontal chunks failing at chunk borders.

## Runtime changes
- Every authoritative chunk snapshot now invalidates all carried Mirage sections even if its data is byte-identical to the existing client mirror.
- Every Mirage light-section invalidation also invalidates the section immediately below it. This is a generic section-boundary rule for surfaces whose visible/top-face light sample lies one section above their owning block.
- Revision manifest client shape validation now counts `authoritativeSections`, not `aggregateSections`. The previous dev.76g code checked the wrong store on clients, making the watchdog believe every lit chunk had zero local sections and repeatedly request identical snapshots.
- Protocol remains 25; the wire format is unchanged.

## QA
Use the same Y=79/80 iron-floor test. Multiple logins should no longer leave random cached whole-chunk islands. If a hole remains, compare actual Mirage queries rather than only overlay labels before changing solver/network architecture again.
