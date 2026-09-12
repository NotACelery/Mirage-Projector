from pathlib import Path
root=Path(__file__).resolve().parents[1]
world=(root/'src/main/java/celerbi/mirageprojector/light/engine/MirageLightWorld.java').read_text()
field=(root/'src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java').read_text()
life=(root/'src/main/java/celerbi/mirageprojector/event/MirageLightLifecycleEvents.java').read_text()
engine=(root/'src/main/java/celerbi/mirageprojector/light/engine/MirageLightEngine.java').read_text()
assert 'allDependencyChunksQueryable' in engine
assert 'PENDING_REBUILDS' in field
assert 'retryPendingSources' in field
assert 'unloadedEdges() > 0' in world
assert 'retryPendingSources(level)' in life
assert 'SOURCE_CHUNK_READINESS' not in life
print('dev.76c atomic static publication contract: PASS')
