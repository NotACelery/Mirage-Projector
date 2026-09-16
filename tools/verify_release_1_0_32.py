#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors=[]
def need(cond,msg):
    if not cond: errors.append(msg)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

need('mod_version=1.0.32' in read('gradle.properties'), 'release version is not 1.0.32')
need(any(v in read('src/main/java/celerbi/mirageprojector/MirageProjector.java') for v in ('NETWORK_PROTOCOL = "42"', 'NETWORK_PROTOCOL = "43"')), 'release protocol is not a compatible 42+ line')
need('SERIALIZATION_VERSION = 4' in read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java'), 'ProjectionSettings format is not 4')
need('**1.0.32** is the current implementation snapshot' in read('README.md'), 'README current snapshot is not 1.0.32')
need('Current implementation snapshot: **1.0.32**' in read('docs/ROADMAP.md'), 'roadmap baseline is not 1.0.32')
need('Current maintenance baseline: **1.0.32**' in read('docs/DEVELOPMENT.md'), 'development baseline is not 1.0.32')
need('Documentation Authority — Mirage Projector 1.0.32' in read('docs/DOCUMENTATION-AUTHORITY.md'), 'documentation authority is not 1.0.32')
need('Version: **1.0.32**' in read('docs/CURRENT-IMPLEMENTATION.md'), 'current implementation version is not 1.0.32')
need('## 1.0.32' in read('docs/CHANGELOG.md'), 'changelog missing 1.0.32')
need('## 1.0.32' in read('docs/VERSION-SCOPE.md'), 'version scope missing 1.0.32')
need((ROOT/'docs/RELEASE-1.0.32-SURVIVAL-PROGRESSION.md').exists(), '1.0.32 release note missing')
need((ROOT/'tools/verify_1_0_32_recipe_viewers.py').exists(), '1.0.32 JEI/EMI recipe-viewer gate missing')
need((ROOT/'tools/verify_1_0_32_overlay_cleanup.py').exists(), '1.0.32 overlay-cleanup gate missing')
need((ROOT/'tools/verify_1_0_32_table_rotation_visibility.py').exists(), '1.0.32 table-rotation visibility gate missing')
need((ROOT/'tools/verify_1_0_32_table_dedicated_runtime.py').exists(), '1.0.32 dedicated Table runtime gate missing')
need((ROOT/'docs/RELEASE-1.0.32-TABLE-DEDICATED-RUNTIME-HOTFIX.md').exists(), '1.0.32 dedicated Table runtime release note missing')

# Every shipped JSON must parse from a clean source tree.
for p in (ROOT/'src/main/resources').rglob('*.json'):
    try:
        json.loads(p.read_text(encoding='utf-8'))
    except Exception as exc:
        need(False, f'invalid JSON {p.relative_to(ROOT)}: {exc}')

# Localization parity is a release gate.
langs=[]
for locale in ('en_us','es_cl','es_es'):
    try:
        langs.append(json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json')))
    except Exception as exc:
        need(False, f'{locale} language JSON invalid: {exc}')
if len(langs)==3:
    need(set(langs[0]) == set(langs[1]) == set(langs[2]), 'language-key parity broken')

# Required progression resources and compatibility resources must ship.
for rel in (
    'src/main/resources/data/mirage_projector/recipe/light_battery.json',
    'src/main/resources/data/mirage_projector/recipe/mirage_flashlight.json',
    'src/main/resources/data/mirage_projector/recipe/mirage_light_projector.json',
    'src/main/resources/data/mirage_projector/recipe/mirage_wall_projector.json',
    'src/main/resources/data/mirage_projector/recipe/shoulder_strap.json',
    'src/main/resources/data/mirage_projector/recipe/auto_battery_swap_patch.json',
    'src/main/resources/data/mirage_projector/recipe/shoulder_strap_slot_expansion.json',
    'src/main/resources/data/mirage_projector/recipe/charging_station.json',
    'src/main/resources/data/mirage_projector/recipe/mirage_hand_projector.json',
    'src/main/resources/data/mirage_projector/recipe/scan_codex.json',
    'src/main/resources/data/mirage_projector/recipe/mirage_table_projector.json',
    'src/main/resources/data/mirage_projector/recipe/mirage_wall_display.json',
    'src/main/resources/assets/mirage_projector/models/item/mirage_lantern.json',
    'src/main/resources/assets/mirage_projector/blockstates/mirage_wall_projector.json',
    'src/main/resources/assets/mirage_projector/blockstates/mirage_wall_illuminator.json',
    'src/main/resources/assets/mirage_projector/blockstates/mirage_flashlight_beacon.json',
):
    need((ROOT/rel).exists(), f'release resource missing: {rel}')
need(not (ROOT/'src/main/resources/data/mirage_projector/recipe/glow_dust.json').exists(), 'Glow Dust unexpectedly gained a direct recipe')
need(not (ROOT/'src/main/resources/data/mirage_projector/recipe/creative_battery.json').exists(), 'Creative Battery unexpectedly gained a Survival recipe')

# Source-only hygiene.
for forbidden in ('build','run','.gradle','.gradle-dist','.idea','node_modules','__pycache__'):
    hits=[p for p in ROOT.rglob(forbidden) if p.is_dir()]
    need(not hits, f'generated/cache directory present: {forbidden}')
for p in ROOT.rglob('*'):
    if p.is_file():
        need(p.suffix not in {'.class','.jar','.pyc'}, f'generated binary/cache present: {p.relative_to(ROOT)}')

if errors:
    print('Mirage Projector 1.0.32 release verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
count=len(langs[0]) if langs else 0
print(f'Mirage Projector 1.0.32 release verification PASS ({count} lang keys)')
