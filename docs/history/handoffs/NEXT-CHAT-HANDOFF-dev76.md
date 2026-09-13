# NEXT CHAT HANDOFF — dev.76h

Current baseline: 0.1.0-dev.76h, protocol 25.

Critical root cause found after dev.76g QA: client virtual-light invalidation crossed a vertical section boundary incorrectly for floor-based overlay consumers. Mirage now invalidates the changed section and Y-1, always invalidates snapshot-carried sections even when bytes match, and the watchdog compares client authoritative-section counts instead of the server-style aggregate store.

See `DEV76H-SECTION-BOUNDARY-INVALIDATION.md`.
