#!/usr/bin/env python3
import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / 'src/main/resources/assets/mirage_projector/models/block'
BLOCK = ROOT / 'src/main/java/celerbi/mirageprojector/block/MirageProjectorBlock.java'
LAYOUT = ROOT / 'src/main/java/celerbi/mirageprojector/ProjectorVisualLayout.java'
VERSION = ROOT / 'gradle.properties'

errors = []

ALT_NAMES = [
    'mirage_projector_alt',
    'mirage_display_alt',
    'wide_mirage_projector_alt',
    'tall_mirage_projector_alt',
    'mirage_field_projector_alt',
    'mirage_prism_alt',
]


def valid_coord(value):
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        return False
    scaled = round(float(value) * 4)
    return abs(float(value) * 4 - scaled) < 1e-9 and 0.0 <= float(value) <= 16.0


# Static geometry rule: all projector model coordinates remain pixel-grid or quarter-pixel aligned.
for name in ALT_NAMES:
    data = json.loads((MODELS / f'{name}.json').read_text())
    for child_name, child in data.get('children', {}).items():
        for idx, element in enumerate(child.get('elements', [])):
            for key in ('from', 'to'):
                vals = element.get(key, [])
                if len(vals) != 3 or any(not valid_coord(v) for v in vals):
                    errors.append(f'{name}:{child_name}[{idx}].{key} is not quarter-grid aligned: {vals}')
            rotation = element.get('rotation')
            if rotation:
                origin = rotation.get('origin', [])
                if any(not valid_coord(v) for v in origin):
                    errors.append(f'{name}:{child_name}[{idx}].rotation.origin is not quarter-grid aligned: {origin}')

# Scope locks: these models were explicitly accepted for dev.79i and must not drift silently.
LOCKED_HASHES = {
    'mirage_prism_alt.json': 'ddcde5493bdd38084222cf115cd56252c8e441a635cd8222486e6e3a7aff5f48',
    'wide_mirage_projector_alt.json': '73872e734b36a40c831b9c626d88d6abb4c3de7231ae128492bff8a3fc9b669c',
    'mirage_projector_alt.json': 'ccdc465212922638d2adfb2e3a2559f7adaee644517c3cf74266ae341fefbea7',
    'mirage_field_projector_alt.json': 'c1a943492922a8612a48cacbb407a02f4fe1528279f4b0ed44a0dba38b2ad8db',
    'mirage_display_alt.json': '07734dd8861b0e077bb776454ae8022bea54b3f901c006d3100e183b5131dbe5',
}
for filename, expected in LOCKED_HASHES.items():
    actual = hashlib.sha256((MODELS / filename).read_bytes()).hexdigest()
    if actual != expected:
        errors.append(f'scope lock violated for {filename}: {actual} != {expected}')

# Display must use the known dev.78 identity (four low corner posts/ring), not the dev.78a portal-like rear frame.
display = json.loads((MODELS / 'mirage_display_alt.json').read_text())
display_frame = {(tuple(e['from']), tuple(e['to'])) for e in display['children']['frame']['elements']}
expected_display_frame = {
    ((5, 3, 5), (11, 4, 11)),
    ((2, 3, 2), (4, 6, 4)),
    ((12, 3, 2), (14, 6, 4)),
    ((2, 3, 12), (4, 6, 14)),
    ((12, 3, 12), (14, 6, 14)),
}
if display_frame != expected_display_frame:
    errors.append('Mirage Display Alt identity was not restored to the dev.78 low-corner-frame design')
display_chamber = display['children']['chamber']['elements']
if len(display_chamber) != 1 or display_chamber[0]['from'] != [5, 4, 5] or display_chamber[0]['to'] != [11, 8, 11]:
    errors.append('Mirage Display Alt chamber must be exactly [5,4,5] -> [11,8,11]')

# Tall inner/outer dual-glass dome contract remains frozen in dev.79i.
tall = json.loads((MODELS / 'tall_mirage_projector_alt.json').read_text())
inner = tall.get('children', {}).get('chamber', {})
dome = tall.get('children', {}).get('dome_glass', {})
inner_glass = inner.get('elements', [])
dome_glass = dome.get('elements', [])
if inner.get('textures', {}).get('glass') != 'minecraft:block/purple_stained_glass':
    errors.append('Tall Alt inner core chamber must remain purple stained glass')
if len(inner_glass) != 1 or inner_glass[0].get('from') != [6, 4, 6] or inner_glass[0].get('to') != [10, 8, 10]:
    errors.append('Tall Alt inner core chamber must be exactly [6,4,6] -> [10,8,10]')
if dome.get('textures', {}).get('glass') != 'minecraft:block/magenta_stained_glass':
    errors.append('Tall Alt outer dome must use magenta stained glass')
if len(dome_glass) != 5:
    errors.append(f'Tall Alt outer dome must use four wall faces plus one roof; got {len(dome_glass)}')
expected_frame = {
    ((4, 3, 4), (12, 4, 5)),
    ((4, 3, 11), (12, 4, 12)),
    ((4, 3, 5), (5, 4, 11)),
    ((11, 3, 5), (12, 4, 11)),
    ((4, 4, 4), (5, 10, 5)),
    ((11, 4, 4), (12, 10, 5)),
    ((4, 4, 11), (5, 10, 12)),
    ((11, 4, 11), (12, 10, 12)),
    ((4, 10, 4), (12, 11, 5)),
    ((4, 10, 11), (12, 11, 12)),
    ((4, 10, 5), (5, 11, 11)),
    ((11, 10, 5), (12, 11, 11)),
}
actual_frame = {(tuple(e['from']), tuple(e['to'])) for e in tall['children']['frame']['elements']}
if actual_frame != expected_frame:
    errors.append('Tall Alt obsidian dome frame is not the continuous one-pixel top/bottom ring contract')
all_tall_elements = [e for child in tall['children'].values() for e in child.get('elements', [])]
if any(max(e['from'][1], e['to'][1]) > 11 for e in all_tall_elements):
    errors.append('Tall Alt contains geometry above Y=11; antenna-like additions are forbidden')

# Reconcile each non-Prism alternate visual model with its own VoxelShape exactly.
java = BLOCK.read_text()
SHAPE_MAP = {
    'COMPACT_ALT_SHAPE': 'mirage_projector_alt',
    'DISPLAY_ALT_SHAPE': 'mirage_display_alt',
    'WIDE_ALT_SHAPE': 'wide_mirage_projector_alt',
    'TALL_ALT_SHAPE': 'tall_mirage_projector_alt',
    'FIELD_ALT_SHAPE': 'mirage_field_projector_alt',
}
for constant, model_name in SHAPE_MAP.items():
    match = re.search(
        rf'private static final VoxelShape {constant} = Shapes\.or\((.*?)\n    \);',
        java,
        re.S,
    )
    if not match:
        errors.append(f'missing Java shape constant {constant}')
        continue
    shape_boxes = {
        tuple(float(v.strip()) if '.' in v else int(v.strip()) for v in m.split(','))
        for m in re.findall(r'box\(([^)]*)\)', match.group(1))
    }
    model = json.loads((MODELS / f'{model_name}.json').read_text())
    model_boxes = {
        tuple(e['from'] + e['to'])
        for child in model['children'].values()
        for e in child.get('elements', [])
    }
    if shape_boxes != model_boxes:
        missing = sorted(model_boxes - shape_boxes)
        extra = sorted(shape_boxes - model_boxes)
        errors.append(f'{constant} does not exactly match {model_name}: missing={missing}, extra={extra}')

layout = LAYOUT.read_text()
for expected in [
    'new ProjectorVisualLayout(9, 5, 14, ALT_CORE_SCALE)',
    'new ProjectorVisualLayout(10, 6, 15, ALT_CORE_SCALE)',
    'new ProjectorVisualLayout(9, 5, 14, ALT_CORE_SCALE)',
    'new ProjectorVisualLayout(12, 6, 15, ALT_CORE_SCALE)',
    'new ProjectorVisualLayout(9, 6, 13, ALT_CORE_SCALE)',
    'new ProjectorVisualLayout(12, 7, 16, ALT_CORE_SCALE)',
]:
    if expected not in layout:
        errors.append(f'missing visual layout anchor contract: {expected}')

if 'mod_version=0.1.0-dev.79i' not in VERSION.read_text():
    errors.append('gradle.properties is not 0.1.0-dev.79i')

if errors:
    print('dev.79i scope-locked projector verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print('dev.79i scope-locked projector verification PASS')
