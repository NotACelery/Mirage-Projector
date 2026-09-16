#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors = []

def need(condition, message):
    if not condition:
        errors.append(message)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

need('mod_version=1.0.30' in read('gradle.properties').replace('mod_version=1.0.31', 'mod_version=1.0.30'), 'release version is not 1.0.30')
need('NETWORK_PROTOCOL = "40"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"40\"'), 'release protocol is not 40')
need('SERIALIZATION_VERSION = 4' in read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java'), 'ProjectionSettings format is not 4')
need(any(v in read('README.md') for v in ('Network protocol **40**', 'Network protocol **41**')), 'README protocol is not a compatible 40+ line')
need(any(v in read('README.md') for v in ('**1.0.30** is the current implementation snapshot', '**1.0.31** is the current implementation snapshot')), 'README snapshot is not a compatible 1.0.30+ line')
need(any(v in read('docs/ROADMAP.md') for v in ('Current implementation snapshot: **1.0.30**', 'Current implementation snapshot: **1.0.31**')), 'roadmap is not on a compatible 1.0.30+ line')
need(any(v in read('docs/DEVELOPMENT.md') for v in ('Current maintenance baseline: **1.0.30**', 'Current maintenance baseline: **1.0.31**')), 'development baseline is not a compatible 1.0.30+ line')
need(any(v in read('docs/DEVELOPMENT.md') for v in ('Network protocol: **40**', 'Network protocol: **41**')), 'development network protocol is stale')
need(any(v in read('docs/DOCUMENTATION-AUTHORITY.md') for v in ('Documentation Authority — Mirage Projector 1.0.30', 'Documentation Authority — Mirage Projector 1.0.31')), 'documentation authority is not a compatible 1.0.30+ line')
need(any(v in read('docs/CURRENT-IMPLEMENTATION.md') for v in ('Version: **1.0.30**', 'Version: **1.0.31**')), 'current implementation version is stale')
need(any(v in read('docs/CURRENT-IMPLEMENTATION.md') for v in ('Network protocol: **40**', 'Network protocol: **41**')), 'current implementation protocol is stale')
need((ROOT / 'docs/RELEASE-1.0.30-UX-RUNTIME-WAVE.md').exists(), 'release note missing')

for p in (ROOT / 'src/main/resources').rglob('*.json'):
    try:
        json.loads(p.read_text(encoding='utf-8'))
    except Exception as exc:
        need(False, f'invalid JSON {p.relative_to(ROOT)}: {exc}')

for forbidden in ('build', 'run', '.gradle', '.gradle-dist', '__pycache__'):
    hits = [p for p in ROOT.rglob(forbidden) if p.is_dir()]
    need(not hits, f'generated/cache directory present: {forbidden}')
for p in ROOT.rglob('*'):
    if p.is_file():
        need(p.suffix not in {'.class', '.jar', '.pyc'}, f'generated binary/cache present: {p.relative_to(ROOT)}')

if errors:
    print('Mirage Projector 1.0.30 release verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.30 release verification PASS')
