#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors = []

def need(cond, msg):
    if not cond:
        errors.append(msg)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

need('mod_version=1.0.20' in read('gradle.properties'), 'gradle.properties is not 1.0.20')
need('NETWORK_PROTOCOL = "34"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java'), 'network protocol is not 34')
need('SERIALIZATION_VERSION = 3' in read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java'), 'ProjectionSettings format is not 3')
need('Current implementation snapshot: **1.0.20**' in read('docs/ROADMAP.md'), 'roadmap baseline is not 1.0.20')
need('Current maintenance baseline: **1.0.20**' in read('docs/DEVELOPMENT.md'), 'development baseline is not 1.0.20')
need('Documentation Authority — Mirage Projector 1.0.20' in read('docs/DOCUMENTATION-AUTHORITY.md'), 'documentation authority is not 1.0.20')
need((ROOT/'docs/RELEASE-1.0.20-SODIUM-LIGHT-BRIDGE-HOTFIX.md').exists(), '1.0.20 release note missing')
need((ROOT/'docs/history/handoffs/NEXT-CHAT-HANDOFF-1.0.20-SODIUM-LIGHT-BRIDGE-HOTFIX.md').exists(), '1.0.20 handoff missing')

bridge = read('src/main/java/celerbi/mirageprojector/mixin/client/LevelRendererMirageLightMixin.java')
need('Minecraft.getInstance().level' in bridge and 'level.getLightEngine()' not in bridge,
     'Sodium-safe LevelRenderer light bridge contract missing')

mixins = json.loads(read('src/main/resources/mirage_projector.mixins.json'))
need('client.LevelRendererMirageLightMixin' in mixins.get('client', []), 'LevelRenderer light bridge mixin is not registered')

for rel in ('build', 'run', '.gradle', '.gradle-dist', '__pycache__'):
    need(not (ROOT / rel).exists(), f'forbidden generated path present: {rel}')
for path in ROOT.rglob('*'):
    if path.is_file():
        need(path.suffix not in {'.class', '.jar', '.pyc'}, f'generated binary/cache present: {path.relative_to(ROOT)}')
        need(not path.name.startswith('javac.'), f'javac temporary file present: {path.relative_to(ROOT)}')

if errors:
    print('Mirage Projector 1.0.20 release audit FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.20 release audit PASS')
