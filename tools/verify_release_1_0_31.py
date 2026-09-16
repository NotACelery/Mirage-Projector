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

need('mod_version=1.0.31' in read('gradle.properties'), 'release version is not 1.0.31')
need('NETWORK_PROTOCOL = "41"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java'), 'release protocol is not 41')
need('SERIALIZATION_VERSION = 4' in read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java'), 'ProjectionSettings format is not 4')
need('Network protocol **41**' in read('README.md'), 'README protocol is not 41')
need('**1.0.31** is the current implementation snapshot' in read('README.md'), 'README snapshot is not 1.0.31')
need('Current implementation snapshot: **1.0.31**' in read('docs/ROADMAP.md'), 'roadmap is not on 1.0.31')
need('Network protocol: **41**' in read('docs/ROADMAP.md'), 'roadmap protocol is stale')
need('Current maintenance baseline: **1.0.31**' in read('docs/DEVELOPMENT.md'), 'development baseline is not 1.0.31')
need('Network protocol: **41**' in read('docs/DEVELOPMENT.md'), 'development protocol is stale')
need('Documentation Authority — Mirage Projector 1.0.31' in read('docs/DOCUMENTATION-AUTHORITY.md'), 'documentation authority is not 1.0.31')
need('Version: **1.0.31**' in read('docs/CURRENT-IMPLEMENTATION.md'), 'current implementation version is stale')
need('Network protocol: **41**' in read('docs/CURRENT-IMPLEMENTATION.md'), 'current implementation protocol is stale')
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
