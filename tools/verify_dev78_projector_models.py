#!/usr/bin/env python3
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / 'src/main/resources/assets/mirage_projector/models/block'
ALT = [
    'mirage_projector_alt.json',
    'mirage_display_alt.json',
    'wide_mirage_projector_alt.json',
    'tall_mirage_projector_alt.json',
    'mirage_field_projector_alt.json',
    'mirage_prism_alt.json',
]

def valid_coord(value):
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        return False
    scaled = round(float(value) * 4)
    return abs(float(value) * 4 - scaled) < 1e-9 and 0.0 <= float(value) <= 16.0


errors=[]
for name in ALT:
    p=MODELS/name
    data=json.loads(p.read_text())
    for child_name, child in data.get('children',{}).items():
        for idx, element in enumerate(child.get('elements',[])):
            for key in ('from','to'):
                vals=element.get(key,[])
                if len(vals)!=3 or any(not valid_coord(v) for v in vals):
                    errors.append(f'{name}:{child_name}[{idx}].{key} must be quarter-grid aligned: {vals}')
            rot=element.get('rotation')
            if rot is not None:
                origin=rot.get('origin',[])
                if any(not valid_coord(v) for v in origin):
                    errors.append(f'{name}:{child_name}[{idx}].rotation.origin must be quarter-grid aligned: {origin}')

layout=(ROOT/'src/main/java/celerbi/mirageprojector/ProjectorVisualLayout.java').read_text()
for expected in [
    'new ProjectorVisualLayout(9, 5, 14, ALT_CORE_SCALE)',
    'new ProjectorVisualLayout(10, 6, 15, ALT_CORE_SCALE)',
    'new ProjectorVisualLayout(12, 6, 15, ALT_CORE_SCALE)',
    'new ProjectorVisualLayout(9, 6, 13, ALT_CORE_SCALE)',
    'new ProjectorVisualLayout(12, 7, 16, ALT_CORE_SCALE)',
]:
    if expected not in layout:
        errors.append(f'missing visual layout contract: {expected}')

be=(ROOT/'src/main/java/celerbi/mirageprojector/blockentity/MirageProjectorBlockEntity.java').read_text()
if 'coreItem.setStackInSlot(0, new ItemStack(Blocks.GLASS))' in be:
    errors.append('Compact projector still injects a default Glass core')

renderer=(ROOT/'src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java').read_text()
if 'idleBookCenter(blockEntity) + bob' not in renderer:
    errors.append('Idle book is not using model-specific visual anchor')
if 'visual.coreCenterYPixels()' not in renderer:
    errors.append('Core is not using model-specific visual anchor')
if 'visual.coreRenderScale()' not in renderer:
    errors.append('Core is not using model-specific render scale')

version=(ROOT/'gradle.properties').read_text()
if 'mod_version=0.1.0-dev.79i' not in version:
    errors.append('gradle.properties is not dev.79i')

if errors:
    print('dev.79i projector model verification FAILED')
    for e in errors:
        print(' -',e)
    raise SystemExit(1)
print('dev.79i projector model verification PASS')
