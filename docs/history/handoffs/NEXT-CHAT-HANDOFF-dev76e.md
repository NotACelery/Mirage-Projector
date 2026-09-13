# NEXT CHAT HANDOFF — 0.1.0-dev.76e

## Purpose
Fix the final apparently missing Mirage-light chunk after dev.76c/76d atomic server publication.

## Diagnosis
Atomic publication changed the symptom from a gradually-growing partial solve to an almost-complete field appearing after ~1.5 seconds, with occasionally exactly one whole chunk missing. That strongly points to section delivery rather than voxel solving.

The dev.76 transport still maintained its own `watchedChunks` set. During login/respawn, duplicating vanilla chunk tracking created an ordering hole: a chunk could be visible client-side but absent from Mirage's private tracking set, so a later authoritative section broadcast would skip it.

## dev.76e transport contract
Static light remains server-authoritative and atomically solved.

Delivery is now intentionally redundant:
1. authoritative source commits broadcast changed sections directly to nearby players;
2. `ChunkWatchEvent.Sent` sends a full Mirage snapshot for that chunk;
3. every client `ChunkEvent.Load` sends `RequestMirageLightChunkPayload`, and the server replies with the full current chunk snapshot.

`MirageLightNetwork` no longer keeps a private watched-chunk registry.

## Snapshot semantics
`sendChunkSnapshot()` first sends `CLEAR_CHUNK`, then all currently non-zero Mirage sections for the chunk. This makes a request an idempotent authoritative replacement and removes stale vertical sections from older generations.

## Protocol
Protocol bumped from 22 to 23 due the new client->server chunk snapshot request payload.

## QA target
- enter the existing iron-platform test world;
- do not touch blocks;
- vanilla light may appear first while the server waits for the complete static dependency window;
- Mirage should then appear atomically;
- no single chunk should remain at vanilla-only values;
- repeat several logins;
- walk far enough to unload/reload chunks and return;
- remove/replace the Mature Cluster.
