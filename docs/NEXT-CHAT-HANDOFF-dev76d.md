# NEXT CHAT HANDOFF — 0.1.0-dev.76d

## Purpose
Build/cleanup stabilization on top of dev.76c. No Mirage lighting behavior was intentionally changed.

## Fixes
- Added the missing `java.util.Map` import required by `MirageLightLifecycleEvents`.
- Replaced the legacy cleanup BAT with a focused cumulative pre-build cleanup.
- `build.bat` still calls `CLEAN-MIRAGE-PROJECTOR.bat --from-build` automatically before Gradle.
- The cleanup BAT has no pause on success and returns naturally to the caller.
- The automatic build cleanup no longer performs historical documentation archiving.

## Architecture retained
- Protocol 22.
- Server-authoritative STATIC_WORLD solved sections.
- Atomic publication gate from dev.76c.
- Client mirrors server sections and reads `max(vanilla, Mirage)`.
- DYNAMIC_VISUAL remains reserved for future mobile/lantern lighting.

## Immediate gate
Run `build.bat` on Windows Java 21. If build succeeds, resume in-game QA of the dev.76c atomic section publication behavior.
