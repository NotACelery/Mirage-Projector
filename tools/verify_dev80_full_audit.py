#!/usr/bin/env python3
from __future__ import annotations
import json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'src/main/java'; RES=ROOT/'src/main/resources'; MODELS=RES/'assets/mirage_projector/models'; LANG=RES/'assets/mirage_projector/lang'
errors=[]; notes=[]
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def need(c,m):
    if not c: errors.append(m)
props=read('gradle.properties')
need('mod_version=0.1.0-dev.80' in props,'gradle.properties is not dev.80')
need('minecraft_version=1.21.1' in props,'Minecraft baseline changed')
need('neo_version=21.1.244' in props,'NeoForge baseline changed')
main=read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
need('NETWORK_PROTOCOL = "26"' in main,'network protocol is not 26')
json_files=list(RES.rglob('*.json'))
for p in json_files:
    try: json.loads(p.read_text(encoding='utf-8'))
    except Exception as e: errors.append(f'invalid JSON {p.relative_to(ROOT)}: {e}')
# refs
for p in (RES/'assets/mirage_projector/blockstates').glob('*.json'):
    data=json.loads(p.read_text()); stack=[data]
    while stack:
        obj=stack.pop()
        if isinstance(obj,dict):
            for k,v in obj.items():
                if k=='model' and isinstance(v,str) and v.startswith('mirage_projector:block/'):
                    target=MODELS/'block'/(v.rsplit('/',1)[-1]+'.json'); need(target.exists(),f'missing block model referenced by {p.name}: {v}')
                stack.append(v)
        elif isinstance(obj,list): stack.extend(obj)
for p in (MODELS/'item').glob('*.json'):
    data=json.loads(p.read_text()); parent=data.get('parent')
    if isinstance(parent,str) and parent.startswith('mirage_projector:block/'):
        need((MODELS/'block'/(parent.rsplit('/',1)[-1]+'.json')).exists(),f'missing item parent model {parent} for {p.name}')
blocks=read('src/main/java/celerbi/mirageprojector/registry/ModBlocks.java'); items=read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
block_ids=set(re.findall(r'registerProjector\("([a-z0-9_]+)"',blocks)); block_ids|=set(re.findall(r'registerLegacyImprovedCore\("([a-z0-9_]+)"',blocks)); block_ids|=set(re.findall(r'registerCrystal\("([a-z0-9_]+)"',blocks)); block_ids|=set(re.findall(r'BLOCKS\.register\(\s*"([a-z0-9_]+)"',blocks))
item_ids=set(re.findall(r'ITEMS\.registerSimpleBlockItem\("([a-z0-9_]+)"',items)); item_ids|=set(re.findall(r'ITEMS\.registerSimpleItem\("([a-z0-9_]+)"',items)); item_ids|=set(re.findall(r'ITEMS\.register\("([a-z0-9_]+)"',items))
need(len(block_ids)==18,f'expected 18 registered blocks after Alt removal, got {len(block_ids)}')
need(len(item_ids)==15,f'expected 15 registered items after Alt removal, got {len(item_ids)}')
for bid in block_ids:
    need((RES/f'assets/mirage_projector/blockstates/{bid}.json').exists(),f'missing blockstate: {bid}')
    need((MODELS/f'block/{bid}.json').exists(),f'missing block model: {bid}')
for iid in item_ids: need((MODELS/f'item/{iid}.json').exists(),f'missing item model: {iid}')
for lang in ('en_us','es_es','es_cl'):
    d=json.loads((LANG/f'{lang}.json').read_text())
    for iid in item_ids: need(f'item.mirage_projector.{iid}' in d or f'block.mirage_projector.{iid}' in d,f'{lang} missing translation for {iid}')
for bid in set(re.findall(r'registerSimpleBlockItem\("([a-z0-9_]+)"',items)):
    need((RES/f'data/mirage_projector/loot_table/blocks/{bid}.json').exists(),f'missing block loot table: {bid}')
known=block_ids|item_ids
for recipe in (RES/'data/mirage_projector/recipe').glob('*.json'):
    data=json.loads(recipe.read_text()); stack=[data]
    while stack:
        obj=stack.pop()
        if isinstance(obj,dict):
            for k,v in obj.items():
                if k in ('id','item') and isinstance(v,str) and v.startswith('mirage_projector:'): need(v.split(':',1)[1] in known,f'recipe {recipe.name} references unknown id {v}')
                stack.append(v)
        elif isinstance(obj,list): stack.extend(obj)
# Java hygiene
for p in SRC.rglob('*.java'):
    text=p.read_text(encoding='utf-8'); rel=p.relative_to(ROOT)
    for i,line in enumerate(text.splitlines(),1):
        need('\t' not in line,f'tab indentation {rel}:{i}'); need(line.rstrip()==line,f'trailing whitespace {rel}:{i}')
    need(re.search(r'^import\s+[^;]*\.\*;',text,re.M) is None,f'wildcard import in {rel}')
    typ=re.search(r'public\s+(?:final\s+|abstract\s+)?(?:class|interface|enum|record)\s+(\w+)',text)
    if typ: need(typ.group(1)==p.stem,f'public type/file mismatch in {rel}')
    need(text.count('{')==text.count('}'),f'brace-count mismatch in {rel}')
# light contracts
field=read('src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java'); client=read('src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java'); network=read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java'); world=read('src/main/java/celerbi/mirageprojector/light/engine/MirageLightWorld.java')
need('WATCHDOG_HEARTBEAT_TICKS = 20' in field,'source watchdog heartbeat changed'); need('verifyActiveSources' in field,'source watchdog missing'); need('getChunkNow' in field,'watchdog forcing chunk query regressed'); need('MirageLightChunkRevisionManifestPayload.TYPE' in network,'manifest payload missing'); need('MirageLightChunkSnapshotPayload.TYPE' in network,'snapshot payload missing'); need('RequestMirageLightChunkPayload.TYPE' in network,'chunk request payload missing'); need('authoritativeSectionKeysForChunk' in client,'client authoritative mirror missing'); need('REQUEST_RETRY_TICKS = 40L' in client,'snapshot retry changed'); need('STATIC_WORLD' in world,'STATIC_WORLD backend missing')
be=read('src/main/java/celerbi/mirageprojector/blockentity/MirageProjectorBlockEntity.java'); need('coreItem.setStackInSlot(0, new ItemStack(Blocks.GLASS))' not in be,'default Glass core injection regressed'); need('coreItem.setStackInSlot(0, ItemStack.EMPTY)' in be,'empty Core slot migration contract missing')
# alt runtime absent
for p in list(SRC.rglob('*.java'))+list(RES.rglob('*.json')):
    text=p.read_text(encoding='utf-8')
    need('_alt' not in text.lower(),f'alt runtime/resource reference remains: {p.relative_to(ROOT)}')
# docs current
for rel in ('README.md','docs/DOCUMENTATION-AUTHORITY.md','docs/CURRENT-IMPLEMENTATION.md','docs/ROADMAP.md','docs/DEVELOPMENT.md','docs/MIRAGE-LIGHT-ENGINE.md'):
    need('0.1.0-dev.80' in read(rel),f'current authority doc does not identify dev.80: {rel}')
need('Network protocol: **26**' in read('docs/CURRENT-IMPLEMENTATION.md'),'CURRENT-IMPLEMENTATION protocol drift'); need('Network protocol: **26**' in read('docs/MIRAGE-LIGHT-ENGINE.md'),'MIRAGE-LIGHT-ENGINE protocol drift')
cleanup=read('CLEAN-MIRAGE-PROJECTOR.bat'); need('--from-build' in cleanup,'cleanup build chaining missing')
for forbidden in ('.gradle-dist','build/classes','run/saves'): need(not (ROOT/forbidden).exists(),f'heavyweight path present: {forbidden}')
for p in SRC.rglob('*.java'):
    if re.search(r'\b(?:TODO|FIXME|HACK|XXX)\b',p.read_text()): notes.append(f'source marker present: {p.relative_to(ROOT)}')
dep=[str(p.relative_to(ROOT)) for p in SRC.rglob('*.java') if 'EventBusSubscriber.Bus.' in p.read_text()]
if dep: notes.append(f'known non-blocking EventBusSubscriber.Bus deprecation sites: {len(dep)}')
if errors:
    print('dev.80 full current-source audit FAILED'); [print(' -',e) for e in errors]; raise SystemExit(1)
print(f'dev.80 full current-source audit PASS ({len(json_files)} JSON, {len(block_ids)} blocks, {len(item_ids)} items)')
for n in notes: print('NOTE',n)
