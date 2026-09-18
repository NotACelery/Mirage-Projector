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

need(any(v in read('gradle.properties') for v in ('mod_version=1.0.31', 'mod_version=1.0.32', 'mod_version=1.0.33', 'mod_version=1.0.34')), 'release line is not compatible with 1.0.31')
need(any(v in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') for v in ('NETWORK_PROTOCOL = "42"', 'NETWORK_PROTOCOL = "43"')), 'release protocol is not a compatible 42+ line')
need('SERIALIZATION_VERSION = 4' in read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java'), 'ProjectionSettings format is not 4')
need(any(v in read('README.md') for v in ('Network protocol is **42**','Network protocol **42**','Network protocol is **43**','Network protocol **43**')), 'README protocol is not a compatible 42+ line')
need(any(v in read('README.md') for v in ('**1.0.31** is the current implementation snapshot', '**1.0.32** is the current implementation snapshot', '**1.0.33** is the current implementation snapshot', '**1.0.34** is the current implementation snapshot')), 'README snapshot is not a compatible 1.0.31+ line')
need(any(v in read('docs/ROADMAP.md') for v in ('Current implementation snapshot: **1.0.31**', 'Current implementation snapshot: **1.0.32**', 'Current implementation snapshot: **1.0.33**', 'Current implementation snapshot: **1.0.34**')), 'roadmap is not on a compatible 1.0.31+ line')
need(any(v in read('docs/ROADMAP.md') for v in ('Network protocol: **42**','Network protocol: **43**')), 'roadmap protocol is stale')
need(any(v in read('docs/DEVELOPMENT.md') for v in ('Current maintenance baseline: **1.0.31**', 'Current maintenance baseline: **1.0.32**', 'Current maintenance baseline: **1.0.33**', 'Current maintenance baseline: **1.0.34**')), 'development baseline is not a compatible 1.0.31+ line')
need(any(v in read('docs/DEVELOPMENT.md') for v in ('Network protocol: **42**','Network protocol: **43**')), 'development protocol is stale')
need(any(v in read('docs/DOCUMENTATION-AUTHORITY.md') for v in ('Documentation Authority — Mirage Projector 1.0.31', 'Documentation Authority — Mirage Projector 1.0.32', 'Documentation Authority — Mirage Projector 1.0.33', 'Documentation Authority — Mirage Projector 1.0.34')), 'documentation authority is not a compatible 1.0.31+ line')
need(any(v in read('docs/CURRENT-IMPLEMENTATION.md') for v in ('Version: **1.0.31**', 'Version: **1.0.32**', 'Version: **1.0.33**', 'Version: **1.0.34**')), 'current implementation version is not a compatible 1.0.31+ line')
need(any(v in read('docs/CURRENT-IMPLEMENTATION.md') for v in ('Network protocol: **42**','Network protocol: **43**')), 'current implementation protocol is stale')
need('## 1.0.31' in read('docs/CHANGELOG.md'), 'changelog missing 1.0.31')
need('## 1.0.31' in read('docs/VERSION-SCOPE.md'), 'version scope missing 1.0.31')
need((ROOT / 'docs/RELEASE-1.0.31-TABLE-RUNTIME-REBUILD.md').exists(), '1.0.31 release note missing')

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
    print('Mirage Projector 1.0.31 release verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.31 release verification PASS')
