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

need(any(v in read('gradle.properties').replace('mod_version=1.0.34', 'mod_version=1.0.30').replace('mod_version=1.0.33', 'mod_version=1.0.30').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30') for v in ('mod_version=1.0.20', 'mod_version=1.0.21', 'mod_version=1.0.22', 'mod_version=1.0.23', 'mod_version=1.0.24', 'mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'gradle.properties is not a compatible 1.0.20+ line')
need(('NETWORK_PROTOCOL = "34"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') or ('NETWORK_PROTOCOL = "35"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') or ('NETWORK_PROTOCOL = \"36\"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') or ('NETWORK_PROTOCOL = \"37\"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') or ('NETWORK_PROTOCOL = \"38\"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') or 'NETWORK_PROTOCOL = \"39\"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') or ('NETWORK_PROTOCOL = \"40\"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') or 'NETWORK_PROTOCOL = \"41\"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') or 'NETWORK_PROTOCOL = \"42\"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') or 'NETWORK_PROTOCOL = \"43\"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java'))))))), 'network protocol is not 34')
need(any(v in read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java') for v in ('SERIALIZATION_VERSION = 3', 'SERIALIZATION_VERSION = 4')), 'ProjectionSettings format is not a compatible v3/v4 line')
need(any(v in read('docs/ROADMAP.md') for v in ('Current implementation snapshot: **1.0.20**', 'Current implementation snapshot: **1.0.21**', 'Current implementation snapshot: **1.0.22**', 'Current implementation snapshot: **1.0.23**', 'Current implementation snapshot: **1.0.24**', 'Current implementation snapshot: **1.0.25**', 'Current implementation snapshot: **1.0.26**', 'Current implementation snapshot: **1.0.27**', 'Current implementation snapshot: **1.0.28**', 'Current implementation snapshot: **1.0.29**', 'Current implementation snapshot: **1.0.30**', 'Current implementation snapshot: **1.0.31**', 'Current implementation snapshot: **1.0.32**', 'Current implementation snapshot: **1.0.33**', 'Current implementation snapshot: **1.0.34**')), 'roadmap baseline is not a compatible 1.0.20+ line')
need(any(v in read('docs/DEVELOPMENT.md') for v in ('Current maintenance baseline: **1.0.20**', 'Current maintenance baseline: **1.0.21**', 'Current maintenance baseline: **1.0.22**', 'Current maintenance baseline: **1.0.23**', 'Current maintenance baseline: **1.0.24**', 'Current maintenance baseline: **1.0.25**', 'Current maintenance baseline: **1.0.26**', 'Current maintenance baseline: **1.0.27**', 'Current maintenance baseline: **1.0.28**', 'Current maintenance baseline: **1.0.29**', 'Current maintenance baseline: **1.0.30**', 'Current maintenance baseline: **1.0.31**', 'Current maintenance baseline: **1.0.32**', 'Current maintenance baseline: **1.0.33**', 'Current maintenance baseline: **1.0.34**')), 'development baseline is not a compatible 1.0.20+ line')
need(any(v in read('docs/DOCUMENTATION-AUTHORITY.md') for v in ('Documentation Authority — Mirage Projector 1.0.20', 'Documentation Authority — Mirage Projector 1.0.21', 'Documentation Authority — Mirage Projector 1.0.22', 'Documentation Authority — Mirage Projector 1.0.23', 'Documentation Authority — Mirage Projector 1.0.24', 'Documentation Authority — Mirage Projector 1.0.25', 'Documentation Authority — Mirage Projector 1.0.26', 'Documentation Authority — Mirage Projector 1.0.27', 'Documentation Authority — Mirage Projector 1.0.28', 'Documentation Authority — Mirage Projector 1.0.29', 'Documentation Authority — Mirage Projector 1.0.30', 'Documentation Authority — Mirage Projector 1.0.31', 'Documentation Authority — Mirage Projector 1.0.32', 'Documentation Authority — Mirage Projector 1.0.33', 'Documentation Authority — Mirage Projector 1.0.34')), 'documentation authority is not a compatible 1.0.20+ line')
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
