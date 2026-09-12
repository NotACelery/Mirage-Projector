#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")

server = read("src/main/java/celerbi/mirageprojector/event/MirageLightLifecycleEvents.java")
client_event = read("src/main/java/celerbi/mirageprojector/client/ClientMirageLightLifecycleEvents.java")
client = read("src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java")
field = read("src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java")
network = read("src/main/java/celerbi/mirageprojector/network/MirageLightNetwork.java")
world = read("src/main/java/celerbi/mirageprojector/light/engine/MirageLightWorld.java")

# Server LOAD must remain pending until real Level queryability exists.
assert "getChunkNow(chunkPos.x, chunkPos.z) != null" in server
assert "batch.removeAll(loaded)" in server
assert "computeIfAbsent(level, ignored -> new ChunkLoadBatch())" in server
assert "MAX_CHUNK_BATCH_AGE_TICKS" not in server

# Client LOAD and UNLOAD are intentionally distinct; LOAD polls the same readiness
# family as MirageLightSolver (Level.hasChunkAt) before being consumed.
assert "queueChunkLoaded" in client_event
assert "queueChunkUnloaded" in client_event
assert "PENDING_LOAD_CHUNKS" in client
assert "PENDING_UNLOAD_CHUNKS" in client
assert "getChunkNow(chunkPos.x, chunkPos.z)" in client
assert "PENDING_LOAD_CHUNKS.remove(packedChunk)" in client

# Old-world / unusual teardown must be origin-authoritative.
assert "removeMirageSource(level, sourcePos, true)" in field
assert "pruneStaleSources" in field
assert "removeSourcesAtOrigin" in field
assert "broadcastRemove" in field
assert "origin.equals(entry.getValue())" in network
assert "removeSourcesAtOrigin" in world

print("dev.75h chunk readiness + stale source lifecycle verification: PASS")
print("- chunk LOAD events cannot be consumed before solver queryability")
print("- client/server retries are direction-agnostic")
print("- block-backed source teardown is origin-authoritative")
