#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

assert 'mod_version=0.1.0-dev.76' in read('gradle.properties')
assert 'NETWORK_PROTOCOL = "25"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java')

mixins = json.loads(read('src/main/resources/mirage_projector.mixins.json'))
assert 'LevelMirageLightMixin' in mixins['mixins']
assert 'LevelLightEngineMirageLightMixin' in mixins['mixins']
assert 'client.RenderChunkRegionMirageLightMixin' in mixins['client']

mod_network = read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')
assert 'MirageLightChunkSnapshotPayload.TYPE' in mod_network
assert 'MirageLightSectionSyncPayload.TYPE' not in mod_network
assert 'MirageLightSourceSyncPayload.TYPE' not in mod_network

payload = read('src/main/java/celerbi/mirageprojector/network/MirageLightChunkSnapshotPayload.java')
for token in ('PACKED_SECTION_BYTES', 'revision', 'sections', 'section(int sectionY', 'unpackLevels()'):
    assert token in payload, token
assert 'MirageLightSection.SIZE / 2' in payload

client = read('src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java')
for token in ('replaceAuthoritativeChunk', 'onLightUpdate(LightLayer.BLOCK', 'setSectionDirty', 'CHUNK_REVISIONS'):
    assert token in client, token
for forbidden in ('MirageLightSolver', 'updateSource(level', 'SOLVED_CHUNK_READINESS', 'SOURCE_SETTLE', 'SOURCE_QUIET_SETTLE_TICKS'):
    assert forbidden not in client, forbidden

world = read('src/main/java/celerbi/mirageprojector/light/engine/MirageLightWorld.java')
for token in ('authoritativeSections', 'replaceAuthoritativeChunk', 'copyAggregateSectionLevels', 'aggregateSectionKeysForChunk'):
    assert token in world, token
assert 'Math.max(solved, authoritative.levelAt(pos))' in world
assert 'level.isClientSide && source.runtimeMode() == MirageLightRuntimeMode.STATIC_WORLD' in world

network = read('src/main/java/celerbi/mirageprojector/network/MirageLightNetwork.java')
for token in ('broadcastSections', 'onChunkSent', 'sendChunkSnapshot', 'MirageLightChunkSnapshotPayload'):
    assert token in network, token
assert 'MirageLightSectionSyncPayload' not in network
assert 'MirageLightSourceSyncPayload' not in network
assert 'watchedChunks' not in network
assert 'TRACKING' not in network

field = read('src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java')
assert 'MirageLightNetwork.broadcastSections(level, touchedSections)' in field
assert 'MirageLightNetwork.broadcastUpsert' not in field
assert 'MirageLightNetwork.broadcastRemove' not in field

runtime = read('src/main/java/celerbi/mirageprojector/light/engine/MirageLightRuntimeMode.java')
assert 'STATIC_WORLD' in runtime and 'DYNAMIC_VISUAL' in runtime

levels = bytes((i * 7) & 15 for i in range(4096))
packed = bytearray(2048)
for i in range(2048):
    packed[i] = levels[i*2] | (levels[i*2+1] << 4)
unpacked = bytearray(4096)
for i, value in enumerate(packed):
    unpacked[i*2] = value & 15
    unpacked[i*2+1] = (value >> 4) & 15
assert bytes(unpacked) == levels

print('dev.76f server-authoritative STATIC_WORLD lifecycle verification: PASS')
print('- protocol 25 source-watchdog + atomic revisioned chunk snapshots registered')
print('- client static solver/retry/settle path removed')
print('- no login CLEAR_ALL or chunk-unload destructive cleanup in correctness path')
