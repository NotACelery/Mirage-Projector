#!/usr/bin/env python3
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def need(c,m):
    if not c: errors.append(m)
def read(r): return (ROOT/r).read_text(encoding='utf-8')
need('mod_version=1.0.34' in read('gradle.properties'),'release version is not 1.0.34')
need('NETWORK_PROTOCOL = "43"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java'),'network protocol is not 43')
need('SERIALIZATION_VERSION = 4' in read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java'),'ProjectionSettings format is not 4')
need('**1.0.34** is the current implementation snapshot' in read('README.md'),'README current snapshot missing')
need('Current implementation snapshot: **1.0.34**' in read('docs/ROADMAP.md'),'ROADMAP baseline missing')
need('Current maintenance baseline: **1.0.34**' in read('docs/DEVELOPMENT.md'),'DEVELOPMENT baseline missing')
need('Documentation Authority — Mirage Projector 1.0.34' in read('docs/DOCUMENTATION-AUTHORITY.md'),'documentation authority baseline missing')
need('Version: **1.0.34**' in read('docs/CURRENT-IMPLEMENTATION.md'),'CURRENT-IMPLEMENTATION baseline missing')
need((ROOT/'docs/NEXT-WAVES-1.1.0.md').exists(),'NEXT WAVES recovery roadmap missing')
need((ROOT/'docs/RELEASE-1.0.34-ROADMAP-UX-CLEANUP.md').exists(),'1.0.34 release note missing')
need((ROOT/'tools/verify_1_0_34_roadmap_ux_cleanup.py').exists(),'1.0.34 feature gate missing')
langs=[]
for loc in ('en_us','es_cl','es_es'):
    try: langs.append(json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{loc}.json')))
    except Exception as exc: need(False,f'{loc} invalid JSON: {exc}')
if len(langs)==3: need(set(langs[0])==set(langs[1])==set(langs[2]),'language-key parity broken')
for p in (ROOT/'src/main/resources').rglob('*.json'):
    try: json.loads(p.read_text(encoding='utf-8'))
    except Exception as exc: need(False,f'invalid JSON {p.relative_to(ROOT)}: {exc}')
for forbidden in ('build','run','.gradle','.gradle-dist','.idea','node_modules','__pycache__'):
    need(not [p for p in ROOT.rglob(forbidden) if p.is_dir()],f'generated/cache directory present: {forbidden}')
for p in ROOT.rglob('*'):
    if p.is_file(): need(p.suffix not in {'.class','.jar','.pyc'},f'generated binary/cache present: {p.relative_to(ROOT)}')
if errors:
    print('Mirage Projector 1.0.34 release verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print(f'Mirage Projector 1.0.34 release verification PASS ({len(langs[0]) if langs else 0} lang keys)')
