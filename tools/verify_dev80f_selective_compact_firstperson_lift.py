#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources/assets/mirage_projector/models/item'
errors = []

def need(cond, msg):
    if not cond:
        errors.append(msg)

def eq3(a, b):
    return len(a) == 3 and all(abs(float(x) - float(y)) < 1e-9 for x, y in zip(a, b))

expected = {
    'mirage_projector': {'gui': 0.72, 'third': 0.42, 'first': 0.48, 'ground': 0.30, 'fixed': 0.44, 'gui_y': 0.0, 'third_y': 1.6, 'first_y': 1.85},
    'mirage_display': {'gui': 0.68, 'third': 0.40, 'first': 0.46, 'ground': 0.28, 'fixed': 0.42, 'gui_y': -0.1, 'third_y': 1.6, 'first_y': 1.05},
    'mirage_field_projector': {'gui': 0.66, 'third': 0.38, 'first': 0.44, 'ground': 0.27, 'fixed': 0.40, 'gui_y': -0.1, 'third_y': 1.6, 'first_y': 1.05},
    'wide_mirage_projector': {'gui': 0.68, 'third': 0.40, 'first': 0.46, 'ground': 0.28, 'fixed': 0.42, 'gui_y': -0.1, 'third_y': 1.6, 'first_y': 1.05},
    'tall_mirage_projector': {'gui': 0.64, 'third': 0.38, 'first': 0.44, 'ground': 0.27, 'fixed': 0.40, 'gui_y': -0.15, 'third_y': 1.6, 'first_y': 1.05},
    'mirage_prism': {'gui': 0.70, 'third': 0.41, 'first': 0.47, 'ground': 0.29, 'fixed': 0.43, 'gui_y': 0.0, 'third_y': 1.6, 'first_y': 1.05},
}
for name, cfg in expected.items():
    p = RES / f'{name}.json'
    need(p.exists(), f'missing item model: {name}')
    if not p.exists():
        continue
    model = json.loads(p.read_text())
    need(model.get('parent') == f'mirage_projector:block/{name}', f'wrong parent for {name}')
    d = model.get('display', {})
    need(eq3(d['gui'].get('rotation', []), [28,225,0]), f'{name} gui rotation incorrect')
    need(eq3(d['gui'].get('translation', []), [0,cfg['gui_y'],0]), f'{name} gui translation incorrect')
    need(eq3(d['gui'].get('scale', []), [cfg['gui']]*3), f'{name} gui scale incorrect')
    need(eq3(d['ground'].get('translation', []), [0,2,0]), f'{name} ground translation incorrect')
    need(eq3(d['ground'].get('scale', []), [cfg['ground']]*3), f'{name} ground scale incorrect')
    need(eq3(d['fixed'].get('rotation', []), [0,180,0]), f'{name} fixed rotation incorrect')
    need(eq3(d['fixed'].get('scale', []), [cfg['fixed']]*3), f'{name} fixed scale incorrect')
    need(eq3(d['thirdperson_righthand'].get('rotation', []), [72,45,0]), f'{name} thirdperson_righthand rotation incorrect')
    need(eq3(d['thirdperson_righthand'].get('translation', []), [0,cfg['third_y'],0]), f'{name} thirdperson_righthand translation incorrect')
    need(eq3(d['thirdperson_righthand'].get('scale', []), [cfg['third']]*3), f'{name} thirdperson_righthand scale incorrect')
    need(eq3(d['thirdperson_lefthand'].get('rotation', []), [72,225,0]), f'{name} thirdperson_lefthand rotation incorrect')
    need(eq3(d['thirdperson_lefthand'].get('translation', []), [0,cfg['third_y'],0]), f'{name} thirdperson_lefthand translation incorrect')
    need(eq3(d['thirdperson_lefthand'].get('scale', []), [cfg['third']]*3), f'{name} thirdperson_lefthand scale incorrect')
    need(eq3(d['firstperson_righthand'].get('rotation', []), [0,45,0]), f'{name} firstperson_righthand rotation incorrect')
    need(eq3(d['firstperson_righthand'].get('translation', []), [0,cfg['first_y'],0]), f'{name} firstperson_righthand translation incorrect')
    need(eq3(d['firstperson_righthand'].get('scale', []), [cfg['first']]*3), f'{name} firstperson_righthand scale incorrect')
    need(eq3(d['firstperson_lefthand'].get('rotation', []), [0,225,0]), f'{name} firstperson_lefthand rotation incorrect')
    need(eq3(d['firstperson_lefthand'].get('translation', []), [0,cfg['first_y'],0]), f'{name} firstperson_lefthand translation incorrect')
    need(eq3(d['firstperson_lefthand'].get('scale', []), [cfg['first']]*3), f'{name} firstperson_lefthand scale incorrect')

if errors:
    print('dev.80f selective compact-projector first-person lift verification FAILED')
    for e in errors:
        print(' -', e)
    raise SystemExit(1)
print('dev.80f selective compact-projector first-person lift verification PASS')
