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

need(any(v in read('gradle.properties').replace('mod_version=1.0.34', 'mod_version=1.0.30').replace('mod_version=1.0.33', 'mod_version=1.0.30').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30') for v in ('mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'release version is not a compatible 1.0.27+ line')
need(any(v in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') for v in ('NETWORK_PROTOCOL = "38"', 'NETWORK_PROTOCOL = "39"', 'NETWORK_PROTOCOL = "40"', 'NETWORK_PROTOCOL = "41"', 'NETWORK_PROTOCOL = "42"', 'NETWORK_PROTOCOL = "43"')), 'release protocol is not 38')
need(any(v in read('README.md') for v in ('Network protocol **38**','Network protocol **39**','Network protocol **40**','Network protocol **41**', 'Network protocol is **42**', 'Network protocol **42**','Network protocol **43**')), 'README protocol is not 38')
need(any(v in read('README.md') for v in ('**1.0.27** is the current implementation snapshot', '**1.0.28** is the current implementation snapshot', '**1.0.29** is the current implementation snapshot', '**1.0.30** is the current implementation snapshot', '**1.0.31** is the current implementation snapshot', '**1.0.32** is the current implementation snapshot', '**1.0.33** is the current implementation snapshot', '**1.0.34** is the current implementation snapshot')), 'README snapshot is not a compatible 1.0.27+ line')
need((ROOT / 'docs/RELEASE-1.0.27-BUILD-STABILITY-HOTFIX.md').exists(), 'release note missing')
need('## 1.0.27' in read('docs/CHANGELOG.md'), 'changelog missing 1.0.27')
need('## 1.0.27' in read('docs/VERSION-SCOPE.md'), 'version scope missing 1.0.27')

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
    print('Mirage Projector 1.0.27 release verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.27 release verification PASS')
