from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

assert 'mod_version=0.1.0-dev.75d' in read('gradle.properties')
assert 'NETWORK_PROTOCOL = "21"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java')

mixins = json.loads(read('src/main/resources/mirage_projector.mixins.json'))
assert 'LevelMirageLightMixin' in mixins['mixins']
assert 'LevelLightEngineMirageLightMixin' in mixins['mixins']
assert 'client.RenderChunkRegionMirageLightMixin' in mixins['client']

network = read('src/main/java/celerbi/mirageprojector/network/MirageLightNetwork.java')
for token in ('ChunkPos', 'watchedChunks', 'deliveredSources', 'onChunkSent', 'onChunkUnwatch', 'reconcilePlayer', 'sourceTouchesChunk'):
    assert token in network, token
assert 'sendToAllPlayersInDimension' not in network

lifecycle = read('src/main/java/celerbi/mirageprojector/event/MirageLightLifecycleEvents.java')
for token in ('ChunkEvent.Load', 'ChunkEvent.Unload', 'ChunkWatchEvent.Sent', 'ChunkWatchEvent.UnWatch', 'getChunkNow', 'cleanupLegacyNodesInChunk', 'discoverSourcesInChunk', 'refreshSourcesForLoadedChunks'):
    assert token in lifecycle, token

client = read('src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java')
for token in ('PENDING_GEOMETRY_CHUNKS', 'flushChunkGeometryChanges', 'setSectionDirty', 'sourceTouchesChunk'):
    assert token in client, token

invalid = read('src/main/java/celerbi/mirageprojector/event/CryingObsidianLightInvalidationEvents.java')
for token in ('FluidPlaceBlockEvent', 'BlockGrowFeatureEvent', 'CropGrowEvent.Post', 'PistonEvent.Post', 'ExplosionEvent.Detonate'):
    assert token in invalid, token

field = read('src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java')
for token in ('cleanupLegacyNodesInChunk', 'discoverSourcesInChunk', 'removeSourcesInChunk', 'refreshSourcesForLoadedChunks', 'MirageLightNetwork.broadcastUpsert'):
    assert token in field, token
assert 'CRYING_LIGHT_NODE.get().defaultBlockState()' not in field

world = read('src/main/java/celerbi/mirageprojector/light/engine/MirageLightWorld.java')
assert 'replaceFieldContribution' in world
assert 'ENGINE_STATES' in world

# There must be no runtime creation of the legacy node anywhere in Java sources.
for path in (ROOT / 'src/main/java').rglob('*.java'):
    text = path.read_text(encoding='utf-8')
    if 'CRYING_LIGHT_NODE.get().defaultBlockState()' in text:
        raise AssertionError(f'legacy node creation remains in {path.relative_to(ROOT)}')

# Current docs must advertise the same line/protocol.
assert '0.1.0-dev.75d' in read('README.md')
assert 'Network protocol 21' in read('README.md')
assert 'Version line: **0.1.0-dev.75d**' in read('docs/CURRENT-IMPLEMENTATION.md')
assert 'Network protocol: **21**' in read('docs/CURRENT-IMPLEMENTATION.md')
assert (ROOT / 'docs/DEV75B-AUTHORITATIVE-VIRTUAL-LIGHT-LIFECYCLE.md').exists()
assert (ROOT / 'docs/NEXT-CHAT-HANDOFF-dev75d.md').exists()

print('dev.75d authoritative Mirage Light lifecycle verification: PASS')
print('- protocol 21 source descriptors remain authoritative')
print('- tracking-scoped delivery / chunk lifecycle / client rebuild paths present')
print('- orphan legacy-node migration scan present; no runtime legacy-node creation')
print('- terrain invalidation covers player/fluid/growth/piston/explosion paths')
print('- active docs and source line agree on dev.75d')
