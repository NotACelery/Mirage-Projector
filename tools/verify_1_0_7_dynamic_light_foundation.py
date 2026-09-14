#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def need(cond, msg):
    if not cond:
        errors.append(msg)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

props = read('gradle.properties')
need('mod_version=1.0.7' in props, 'version is not 1.0.7')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
need('NETWORK_PROTOCOL = "28"' in main, 'protocol changed unexpectedly')

shape = read('src/main/java/celerbi/mirageprojector/light/engine/MirageLightShape.java')
need('DIRECTIONAL_CONE(true)' in shape, 'directional cone is not runtime-enabled')

solver = read('src/main/java/celerbi/mirageprojector/light/engine/MirageLightSolver.java')
need('insideShapeEnvelope(profile, origin, nextPos)' in solver, 'solver does not enforce shape envelope')
need('profile.shape() == MirageLightShape.OMNIDIRECTIONAL' in solver, 'omnidirectional fast path missing')
need('profile.shape() != MirageLightShape.DIRECTIONAL_CONE' in solver, 'directional cone path missing')
need('forward.dot(offset.normalize()) >= threshold' in solver, 'cone angular test missing')
need(solver.index('insideShapeEnvelope(profile, origin, nextPos)') < solver.index('MirageLightEngine.isChunkQueryable(level, nextPos)'),
     'shape filtering must happen before chunk readiness accounting')

snapshot = read('src/main/java/celerbi/mirageprojector/light/engine/MirageDynamicLightSnapshot.java')
for token in ('refreshIntervalTicks', 'cullDistanceBlocks', 'staleAfterTicks', 'MirageLightRuntimeMode.DYNAMIC_VISUAL'):
    need(token in snapshot, f'dynamic snapshot missing {token}')

profiles = read('src/main/java/celerbi/mirageprojector/light/engine/MirageDynamicLightProfiles.java')
need('MirageLightShape.OMNIDIRECTIONAL' in profiles, 'dynamic ambient profile missing')
need('MirageLightShape.DIRECTIONAL_CONE' in profiles, 'dynamic directional profile missing')

manager = read('src/main/java/celerbi/mirageprojector/client/ClientDynamicMirageLightManager.java')
for token in (
    'camera.distanceToSqr(snapshot.position())',
    'snapshot.staleAfterTicks()',
    'snapshot.refreshIntervalTicks()',
    'MirageLightEngine.updateSource',
    'MirageLightEngine.removeSource',
    'ClientMirageLightSync.invalidateSections',
    'installedField.stats().unloadedEdges() > 0',
):
    need(token in manager, f'dynamic manager missing contract: {token}')
need('replaceAuthoritativeChunk' not in manager and 'installAuthoritativeSection' not in manager,
     'dynamic manager must not touch authoritative STATIC_WORLD publication')

runtime = read('src/main/java/celerbi/mirageprojector/client/ClientRuntimeEvents.java')
need('ClientDynamicMirageLightManager.tick' in runtime, 'dynamic manager is not ticked client-side')
need('ClientDynamicMirageLightManager.resetSession' in runtime, 'dynamic manager is not reset on logout')

source_id = read('src/main/java/celerbi/mirageprojector/light/engine/MirageLightSourceId.java')
need('entity(String kind, UUID uuid)' in source_id, 'entity-attached stable source factory missing')
need('keyed(String kind, long key)' in source_id, 'device-key source factory missing')

waitlist = read('docs/WAITLIST-1.1.0.md')
need('Foundation delivered in **1.0.5**' in waitlist, '1.1 waitlist does not record delivered dynamic foundation')

if errors:
    print('Mirage Projector 1.0.7 dynamic-light foundation verification FAILED')
    for error in errors:
        print(f' - {error}')
    raise SystemExit(1)

print('Mirage Projector 1.0.7 dynamic-light foundation verification PASS')
