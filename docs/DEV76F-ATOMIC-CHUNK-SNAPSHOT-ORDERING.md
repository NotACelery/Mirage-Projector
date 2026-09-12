# dev.76f — Atomic chunk snapshots and login ordering

## Bug isolated
`PlayerLoggedInEvent`/dimension/respawn could emit `CLEAR_ALL` after some chunk snapshots were already installed. Chunk unload/unwatch cleanup could also erase a newer snapshot during chunk object replacement. This made missing chunks vary between identical logins.

## New contract
- No server `CLEAR_ALL` during login/respawn/dimension change. Client level/session teardown owns global cleanup.
- No destructive Mirage clear on normal chunk unload/unwatch. An unloaded chunk is invisible; its next load explicitly requests and atomically replaces its snapshot.
- A chunk snapshot is one payload containing every non-zero Mirage section for that chunk.
- Each server chunk has a monotonic Mirage revision. Client ignores older snapshots.
- Aggregate changes send a full atomic snapshot for each changed chunk rather than per-section clear/set deltas.
- Protocol 24.

This preserves server-authoritative STATIC_WORLD lighting while removing packet-order races from correctness.
