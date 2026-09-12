# NEXT CHAT HANDOFF — dev.76f

Baseline: `0.1.0-dev.76f`, protocol 24.

Primary QA gate: repeated login to the iron light-grid world. The same complete Mirage field must appear every login. dev.76f removes late CLEAR_ALL/unwatch/unload deletion races and replaces per-section delivery with revisioned atomic chunk snapshots.

If a chunk is still missing, inspect whether the server authoritative aggregate itself lacks it; transport should no longer be able to erase a newer chunk snapshot with an older one.
