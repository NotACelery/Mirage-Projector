#!/usr/bin/env python3
from pathlib import Path
import json, re

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
MODELS=RES/'assets/mirage_projector/models/block'
errors=[]
def need(c,m):
    if not c: errors.append(m)

canon=[
    'mirage_projector','mirage_display','mirage_field_projector',
    'wide_mirage_projector','tall_mirage_projector','mirage_prism'
]
alts=[
    'mirage_projector_alt','mirage_display_alt','mirage_field_projector_alt',
    'wide_mirage_projector_alt','tall_mirage_projector_alt','mirage_prism_alt'
]

props=(ROOT/'gradle.properties').read_text()
need('mod_version=0.1.0-dev.82' in props,'version is not dev.82')
main=(ROOT/'src/main/java/celerbi/mirageprojector/MirageProjector.java').read_text()
need('NETWORK_PROTOCOL = "27"' in main,'network protocol is not current protocol 27')

# Temporary alt registry/runtime surface must be gone.
for rel in [
    'src/main/java/celerbi/mirageprojector/registry/ModBlocks.java',
    'src/main/java/celerbi/mirageprojector/registry/ModItems.java',
    'src/main/java/celerbi/mirageprojector/registry/ModBlockEntities.java',
    'src/main/java/celerbi/mirageprojector/block/MirageProjectorBlock.java',
    'src/main/java/celerbi/mirageprojector/ProjectorVisualLayout.java',
    'src/main/java/celerbi/mirageprojector/MirageProjector.java',
    'src/main/java/celerbi/mirageprojector/registry/ModCreativeTabs.java',
]:
    text=(ROOT/rel).read_text()
    need('_ALT' not in text and '_alt' not in text, f'alt runtime identifier remains in {rel}')

for alt in alts:
    for rel in [
        f'assets/mirage_projector/models/block/{alt}.json',
        f'assets/mirage_projector/models/item/{alt}.json',
        f'assets/mirage_projector/blockstates/{alt}.json',
        f'data/mirage_projector/loot_table/blocks/{alt}.json',
    ]:
        need(not (RES/rel).exists(), f'temporary alt resource still active: {rel}')

# Old canonical models must be archived outside runtime assets.
hist=ROOT/'docs/history/projector-models-pre-dev80/legacy-canonical'
for name in canon:
    need((hist/f'{name}.json').exists(),f'legacy canonical model not archived: {name}')
    need((MODELS/f'{name}.json').exists(),f'canonical promoted model missing: {name}')

# Creative order exact in both mod tab and vanilla Functional Blocks.
expected=[
'MIRAGE_PROJECTOR','MIRAGE_DISPLAY','MIRAGE_FIELD_PROJECTOR',
'WIDE_MIRAGE_PROJECTOR','TALL_MIRAGE_PROJECTOR','MIRAGE_PRISM'
]
for rel in ['src/main/java/celerbi/mirageprojector/registry/ModCreativeTabs.java','src/main/java/celerbi/mirageprojector/MirageProjector.java']:
    text=(ROOT/rel).read_text()
    positions=[text.find(f'ModItems.{field}.get()') for field in expected]
    need(all(p>=0 for p in positions),f'projector missing from creative ordering in {rel}')
    need(positions==sorted(positions),f'wrong projector creative order in {rel}: {positions}')

# Canonical BlockEntity accepts exactly canonical chassis fields.
be=(ROOT/'src/main/java/celerbi/mirageprojector/registry/ModBlockEntities.java').read_text()
for field in expected:
    need(f'ModBlocks.{field}.get()' in be,f'canonical BlockEntity missing {field}')
need('_ALT' not in be,'alt BlockEntity registration remains')

# Model coordinates remain quarter-grid aligned and canonical item/blockstate refs exist.
def valid(v):
    if isinstance(v,bool) or not isinstance(v,(int,float)): return False
    return 0 <= float(v) <= 16 and abs(float(v)*4-round(float(v)*4))<1e-9
for name in canon:
    model=json.loads((MODELS/f'{name}.json').read_text())
    for child_name,child in model.get('children',{}).items():
        for i,e in enumerate(child.get('elements',[])):
            for key in ('from','to'):
                vals=e.get(key,[])
                need(len(vals)==3 and all(valid(v) for v in vals),f'invalid coord {name}:{child_name}[{i}].{key}={vals}')
    item=RES/f'assets/mirage_projector/models/item/{name}.json'
    state=RES/f'assets/mirage_projector/blockstates/{name}.json'
    loot=RES/f'data/mirage_projector/loot_table/blocks/{name}.json'
    need(item.exists(),f'canonical item model missing {name}')
    need(state.exists(),f'canonical blockstate missing {name}')
    need(loot.exists(),f'canonical loot table missing {name}')
    if item.exists():
        need(json.loads(item.read_text()).get('parent')==f'mirage_projector:block/{name}',f'item model parent not canonical: {name}')

# Canonical shape constants must match visible model cuboids.
block=(ROOT/'src/main/java/celerbi/mirageprojector/block/MirageProjectorBlock.java').read_text()
shape_map={
'COMPACT_SHAPE':'mirage_projector','DISPLAY_SHAPE':'mirage_display','FIELD_SHAPE':'mirage_field_projector',
'WIDE_SHAPE':'wide_mirage_projector','TALL_SHAPE':'tall_mirage_projector','PRISM_SHAPE':'mirage_prism'
}
for const,name in shape_map.items():
    m=re.search(rf'private static final VoxelShape {const} = Shapes\.or\((.*?)\n    \);',block,re.S)
    need(m is not None,f'missing shape constant {const}')
    if not m: continue
    boxes={tuple(float(x.strip()) for x in raw.split(',')) for raw in re.findall(r'box\(([^)]*)\)',m.group(1))}
    model=json.loads((MODELS/f'{name}.json').read_text())
    model_boxes={tuple(float(x) for x in e['from']+e['to']) for child in model.get('children',{}).values() for e in child.get('elements',[])}
    need(boxes==model_boxes,f'{const} != canonical {name} model; missing={model_boxes-boxes}, extra={boxes-model_boxes}')

# Preserve final Compact/Tall QA fixes after promotion.
compact=json.loads((MODELS/'mirage_projector.json').read_text())
need(len(compact.get('children',{}).get('emitter',{}).get('elements',[]))==0,'Compact canonical emitter geometry regressed')
front=None
for e in compact.get('children',{}).get('frame',{}).get('elements',[]):
    if e.get('from')==[5,2,3] and e.get('to')==[11,3,5]: front=e
need(front is not None and 'up' not in front.get('faces',{}),'Compact canonical front-lip z-fighting fix regressed')
need(any(e.get('from')==[5,2,5] and e.get('to')==[11,3,11] for e in compact.get('children',{}).get('base',{}).get('elements',[])),'Compact canonical upper-base overlap fix regressed')

tall=json.loads((MODELS/'tall_mirage_projector.json').read_text())
em=tall.get('children',{}).get('emitter',{}).get('elements',[])
boxes={(tuple(e.get('from',[])),tuple(e.get('to',[]))) for e in em}
need(((6,2,2),(10,4,4)) in boxes and ((6,2,12),(10,4,14)) in boxes,'Tall canonical front/rear low glass symmetry regressed')

# Current source should not expose Alt translations.
for lang in ['en_us','es_cl','es_es']:
    data=json.loads((RES/f'assets/mirage_projector/lang/{lang}.json').read_text())
    need(not any(k.endswith('_alt') for k in data),f'{lang} still exposes alt translation keys')

if errors:
    print('dev.80f projector promotion verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print('dev.82 projector promotion verification PASS')
