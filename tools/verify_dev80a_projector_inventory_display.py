#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources/assets/mirage_projector/models/item'
errors = []

def need(cond, msg):
    if not cond:
        errors.append(msg)

expected = {
    'mirage_projector': {'gui': 0.8, 'third': 0.39, 'first': 0.42, 'ground': 0.34, 'fixed': 0.5, 'gui_y': 0.5},
    'mirage_display': {'gui': 0.74, 'third': 0.36, 'first': 0.39, 'ground': 0.30, 'fixed': 0.46, 'gui_y': 0.0},
    'mirage_field_projector': {'gui': 0.70, 'third': 0.34, 'first': 0.37, 'ground': 0.28, 'fixed': 0.44, 'gui_y': 0.0},
    'wide_mirage_projector': {'gui': 0.74, 'third': 0.36, 'first': 0.39, 'ground': 0.30, 'fixed': 0.46, 'gui_y': 0.0},
    'tall_mirage_projector': {'gui': 0.68, 'third': 0.34, 'first': 0.37, 'ground': 0.28, 'fixed': 0.44, 'gui_y': 0.0},
    'mirage_prism': {'gui': 0.76, 'third': 0.37, 'first': 0.40, 'ground': 0.31, 'fixed': 0.48, 'gui_y': 0.25},
}

def eq3(a, b):
    return len(a) == 3 and all(abs(float(x) - float(y)) < 1e-9 for x, y in zip(a, b))

for name, cfg in expected.items():
    p = RES / f'{name}.json'
    need(p.exists(), f'missing item model: {name}')
    if not p.exists():
        continue
    model = json.loads(p.read_text())
    need(model.get('parent') == f'mirage_projector:block/{name}', f'wrong parent for {name}')
    display = model.get('display', {})
    for key in ['gui', 'ground', 'fixed', 'thirdperson_righthand', 'thirdperson_lefthand', 'firstperson_righthand', 'firstperson_lefthand']:
        need(key in display, f'{name} missing display transform: {key}')
    if 'gui' in display:
        need(eq3(display['gui'].get('rotation', []), [30,225,0]), f'{name} gui rotation incorrect')
        need(eq3(display['gui'].get('translation', []), [0,cfg['gui_y'],0]), f'{name} gui translation incorrect')
        need(eq3(display['gui'].get('scale', []), [cfg['gui']]*3), f'{name} gui scale incorrect')
    if 'ground' in display:
        need(eq3(display['ground'].get('translation', []), [0,2,0]), f'{name} ground translation incorrect')
        need(eq3(display['ground'].get('scale', []), [cfg['ground']]*3), f'{name} ground scale incorrect')
    if 'fixed' in display:
        need(eq3(display['fixed'].get('scale', []), [cfg['fixed']]*3), f'{name} fixed scale incorrect')
    if 'thirdperson_righthand' in display:
        need(eq3(display['thirdperson_righthand'].get('rotation', []), [75,45,0]), f'{name} thirdperson_righthand rotation incorrect')
        need(eq3(display['thirdperson_righthand'].get('translation', []), [0,2.5,0]), f'{name} thirdperson_righthand translation incorrect')
        need(eq3(display['thirdperson_righthand'].get('scale', []), [cfg['third']]*3), f'{name} thirdperson_righthand scale incorrect')
    if 'thirdperson_lefthand' in display:
        need(eq3(display['thirdperson_lefthand'].get('rotation', []), [75,225,0]), f'{name} thirdperson_lefthand rotation incorrect')
        need(eq3(display['thirdperson_lefthand'].get('translation', []), [0,2.5,0]), f'{name} thirdperson_lefthand translation incorrect')
        need(eq3(display['thirdperson_lefthand'].get('scale', []), [cfg['third']]*3), f'{name} thirdperson_lefthand scale incorrect')
    if 'firstperson_righthand' in display:
        need(eq3(display['firstperson_righthand'].get('rotation', []), [0,45,0]), f'{name} firstperson_righthand rotation incorrect')
        need(eq3(display['firstperson_righthand'].get('translation', []), [0,0,0]), f'{name} firstperson_righthand translation incorrect')
        need(eq3(display['firstperson_righthand'].get('scale', []), [cfg['first']]*3), f'{name} firstperson_righthand scale incorrect')
    if 'firstperson_lefthand' in display:
        need(eq3(display['firstperson_lefthand'].get('rotation', []), [0,225,0]), f'{name} firstperson_lefthand rotation incorrect')
        need(eq3(display['firstperson_lefthand'].get('translation', []), [0,0,0]), f'{name} firstperson_lefthand translation incorrect')
        need(eq3(display['firstperson_lefthand'].get('scale', []), [cfg['first']]*3), f'{name} firstperson_lefthand scale incorrect')

if errors:
    print('dev.80a projector inventory-display verification FAILED')
    for e in errors:
        print(' -', e)
    raise SystemExit(1)
print('dev.80a projector inventory-display verification PASS')
