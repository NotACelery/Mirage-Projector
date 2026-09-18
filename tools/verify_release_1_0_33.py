#!/usr/bin/env python3
from pathlib import Path
import json

ROOT=Path(__file__).resolve().parents[1]
errors=[]
def need(cond,msg):
    if not cond: errors.append(msg)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

need(any(v in read('gradle.properties') for v in ('mod_version=1.0.33','mod_version=1.0.34')), 'current line no longer carries 1.0.33 release compatibility')
need('NETWORK_PROTOCOL = "43"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java'), 'network protocol changed unexpectedly from 43')
need('SERIALIZATION_VERSION = 4' in read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java'), 'ProjectionSettings format is not 4')
need(any(v in read('README.md') for v in ('**1.0.33** is the current implementation snapshot', '**1.0.34** is the current implementation snapshot','**1.0.34** is the current implementation snapshot')), 'README current snapshot is incompatible')
need(any(v in read('docs/ROADMAP.md') for v in ('Current implementation snapshot: **1.0.33**', 'Current implementation snapshot: **1.0.34**','Current implementation snapshot: **1.0.34**')), 'ROADMAP baseline is incompatible')
need(any(v in read('docs/DEVELOPMENT.md') for v in ('Current maintenance baseline: **1.0.33**', 'Current maintenance baseline: **1.0.34**','Current maintenance baseline: **1.0.34**')), 'DEVELOPMENT baseline is incompatible')
need(any(v in read('docs/DOCUMENTATION-AUTHORITY.md') for v in ('Documentation Authority — Mirage Projector 1.0.33', 'Documentation Authority — Mirage Projector 1.0.34','Documentation Authority — Mirage Projector 1.0.34')), 'documentation authority baseline is incompatible')
need(any(v in read('docs/CURRENT-IMPLEMENTATION.md') for v in ('Version: **1.0.33**', 'Version: **1.0.34**','Version: **1.0.34**')), 'CURRENT-IMPLEMENTATION baseline is incompatible')
need((ROOT/'docs/RELEASE-1.0.33-END-RESONANCE-FOUNDATION.md').exists(), '1.0.33 internal release note missing')
need((ROOT/'tools/verify_1_0_33_end_resonance_foundation.py').exists(), '1.0.33 feature gate missing')
need('Implementation status (1.0.33 foundation)' in read('docs/WAITLIST-1.1.0.md'), 'waitlist status not updated')

langs=[]
for locale in ('en_us','es_cl','es_es'):
    try: langs.append(json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json')))
    except Exception as exc: need(False, f'{locale} language JSON invalid: {exc}')
if len(langs)==3:
    need(set(langs[0])==set(langs[1])==set(langs[2]), 'language-key parity broken')

for p in (ROOT/'src/main/resources').rglob('*.json'):
    try: json.loads(p.read_text(encoding='utf-8'))
    except Exception as exc: need(False, f'invalid JSON {p.relative_to(ROOT)}: {exc}')

for forbidden in ('build','run','.gradle','.gradle-dist','.idea','node_modules','__pycache__'):
    need(not [p for p in ROOT.rglob(forbidden) if p.is_dir()], f'generated/cache directory present: {forbidden}')
for p in ROOT.rglob('*'):
    if p.is_file(): need(p.suffix not in {'.class','.jar','.pyc'}, f'generated binary/cache present: {p.relative_to(ROOT)}')

if errors:
    print('Mirage Projector 1.0.33 release verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print(f'Mirage Projector 1.0.33 release verification PASS ({len(langs[0]) if langs else 0} lang keys)')
