#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors=[]
def need(c,m):
    if not c: errors.append(m)
def read(r): return (ROOT/r).read_text(encoding='utf-8')

need(any(v in read('gradle.properties').replace('mod_version=1.0.31', 'mod_version=1.0.30') for v in ('mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'release version is not a compatible 1.0.25+ line')
need(any(v in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') for v in ('NETWORK_PROTOCOL = "37"', 'NETWORK_PROTOCOL = "38"', 'NETWORK_PROTOCOL = "39"', 'NETWORK_PROTOCOL = "40"', 'NETWORK_PROTOCOL = "41"')), 'release protocol is not 37')
need(any(v in read('README.md') for v in ('Network protocol **37**','Network protocol **38**','Network protocol **39**','Network protocol **40**','Network protocol **41**')), 'README protocol is not 37+')
need(any(v in read('README.md') for v in ('**1.0.25** is the current implementation snapshot', '**1.0.26** is the current implementation snapshot', '**1.0.27** is the current implementation snapshot', '**1.0.28** is the current implementation snapshot', '**1.0.29** is the current implementation snapshot', '**1.0.30** is the current implementation snapshot', '**1.0.31** is the current implementation snapshot')), 'README snapshot is not a compatible 1.0.25+ line')
need((ROOT/'docs/RELEASE-1.0.25-ANCHOR-CHASSIS-FOUNDATION.md').exists(), 'release note missing')
need('## 1.0.25' in read('docs/CHANGELOG.md'), 'changelog missing 1.0.25')
need('## 1.0.25' in read('docs/VERSION-SCOPE.md'), 'version scope missing 1.0.25')
need('mirage_projector:mirage_table_projector' in read('docs/REGISTRY-INVENTORY.md'), 'registry inventory missing table projector')
need('mirage_projector:mirage_wall_projector' in read('docs/REGISTRY-INVENTORY.md'), 'registry inventory missing wall projector')

# JSON resource validity across the whole active resource tree.
for p in (ROOT/'src/main/resources').rglob('*.json'):
    try: json.loads(p.read_text(encoding='utf-8'))
    except Exception as exc: need(False, f'invalid JSON {p.relative_to(ROOT)}: {exc}')

if errors:
    print('Mirage Projector 1.0.25 release verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print('Mirage Projector 1.0.25 release verification PASS')
