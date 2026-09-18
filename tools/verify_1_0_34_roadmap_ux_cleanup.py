#!/usr/bin/env python3
from pathlib import Path
import json

ROOT=Path(__file__).resolve().parents[1]
errors=[]
def need(cond,msg):
    if not cond: errors.append(msg)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

props=read('gradle.properties')
need('mod_version=1.0.34' in props,'mod version is not 1.0.34')
need('NETWORK_PROTOCOL = "43"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java'),'network protocol drifted from 43')
need('SERIALIZATION_VERSION = 4' in read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java'),'ProjectionSettings format drifted from 4')

# Wall rollback: one presentation Wall Projector, no second illumination family.
blocks=read('src/main/java/celerbi/mirageprojector/registry/ModBlocks.java')
items=read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
wall=read('src/main/java/celerbi/mirageprojector/block/MirageWallProjectorBlock.java')
need('MIRAGE_WALL_PROJECTOR' in blocks and '"mirage_wall_projector"' in blocks and 'MirageWallProjectorBlock' in blocks,'Mirage Wall Projector registry identity missing')
need('MIRAGE_WALL_DISPLAY' not in blocks and 'mirage_wall_illuminator' not in blocks,'retired wall architecture still registered')
need('MIRAGE_WALL_PROJECTOR' in items and 'registerSimpleBlockItem("mirage_wall_projector"' in items,'Wall Projector BlockItem identity missing')
need('class MirageWallProjectorBlock extends MirageProjectorBlock' in wall,'Wall Projector is not the presentation chassis')
for rel in (
 'src/main/java/celerbi/mirageprojector/block/MirageWallDisplayBlock.java',
 'src/main/java/celerbi/mirageprojector/block/MirageWallIlluminatorBlock.java',
 'src/main/resources/assets/mirage_projector/blockstates/mirage_wall_illuminator.json',
 'src/main/resources/assets/mirage_projector/models/block/mirage_wall_illuminator.json',
 'src/main/resources/assets/mirage_projector/models/item/mirage_wall_illuminator.json',
 'src/main/resources/data/mirage_projector/recipe/mirage_wall_illuminator.json',
): need(not (ROOT/rel).exists(),f'retired wall file survived: {rel}')
clean=read('CLEAN-MIRAGE-PROJECTOR.bat')
for token in ('MirageWallDisplayBlock.java','MirageWallIlluminatorBlock.java','mirage_wall_illuminator.json'):
    need(token in clean,f'overlay tombstone missing for {token}')

# Table workspace navigation must not mutate active mode; explicit Use Mode actions do.
for name in ('OpenImageWorkspacePayload','OpenItemWorkspacePayload','OpenEntityWorkspacePayload','OpenBannerWorkspacePayload'):
    text=read(f'src/main/java/celerbi/mirageprojector/network/{name}.java')
    need('SetProjectionSourcePayload' not in text and '.setSourceMode(' not in text,f'{name} still mutates active source while navigating')
for name,mode in (('ImageProjectorScreen','IMAGE'),('ItemProjectorScreen','ITEM'),('EntityProjectorScreen','ENTITY'),('BannerProjectorScreen','BANNER')):
    text=read(f'src/main/java/celerbi/mirageprojector/client/{name}.java')
    need('new SetProjectionSourcePayload' in text and f'SourceMode.{mode}' in text,f'{name} lacks explicit Use Mode source activation')
    need('workspace.mode_active' in text,f'{name} no longer reflects active-mode state')

# Portable wording follows direct-configuration architecture.
for loc in ('en_us','es_cl','es_es'):
    lang=json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{loc}.json'))
    need('profile loaded' not in lang.get('hud.mirage_projector.hand_projector.no_profile','').lower(),f'{loc} HUD still references profiles')
    need('profile loaded' not in lang.get('message.mirage_projector.hand_projector.no_profile','').lower(),f'{loc} activation still references profiles')
need(json.loads(read('src/main/resources/assets/mirage_projector/lang/en_us.json')).get('message.mirage_projector.hand_projector.no_profile') == 'No projection has been configured.','English portable empty-state wording drifted')

# Flashlight coherence: shared base shape, forward held transform, dedicated upright Ambient placed form.
flash_base=json.loads(read('src/main/resources/assets/mirage_projector/models/block/mirage_flashlight.json'))
flash_item=json.loads(read('src/main/resources/assets/mirage_projector/models/item/mirage_lantern.json'))
beacon=json.loads(read('src/main/resources/assets/mirage_projector/models/block/mirage_flashlight_beacon.json'))
beacon_amb=json.loads(read('src/main/resources/assets/mirage_projector/models/block/mirage_flashlight_beacon_ambient.json'))
beacon_bs=json.loads(read('src/main/resources/assets/mirage_projector/blockstates/mirage_flashlight_beacon.json'))
need(flash_item.get('parent')=='mirage_projector:block/mirage_flashlight','held Flashlight does not share placed geometry base')
need(beacon.get('parent')=='mirage_projector:block/mirage_flashlight','placed Flashlight does not share held geometry base')
tex=flash_base.get('textures',{})
need(tex.get('body')=='minecraft:block/crying_obsidian' and tex.get('glass')=='minecraft:block/magenta_stained_glass','Flashlight material language drifted')
need(flash_item.get('display',{}).get('firstperson_righthand',{}).get('rotation',[999])[0]==0,'held Flashlight is pitched vertically again')
need(len(beacon_amb.get('elements',[]))>=5,'Ambient Flashlight upright model missing')
for facing in ('north','east','south','west'):
    key=f'ambient=true,facing={facing}'
    need(beacon_bs.get('variants',{}).get(key,{}).get('model')=='mirage_projector:block/mirage_flashlight_beacon_ambient',f'placed Ambient Flashlight not upright for {facing}')
held=read('src/main/java/celerbi/mirageprojector/client/ClientHeldFlashlights.java')
need('mode == PortableLightMode.AMBIENT ? new Vec3(0.0D, 1.0D, 0.0D)' in held,'held Ambient Flashlight does not emit upward')

# Light Projector material/lens state.
light=json.loads(read('src/main/resources/assets/mirage_projector/models/block/mirage_light_projector.json'))
light_amb=json.loads(read('src/main/resources/assets/mirage_projector/models/block/mirage_light_projector_ambient.json'))
light_bs=json.loads(read('src/main/resources/assets/mirage_projector/blockstates/mirage_light_projector.json'))
need(light.get('textures',{}).get('outer')=='minecraft:block/crying_obsidian','Light Projector exterior is not Crying Obsidian')
need(light.get('textures',{}).get('inner')=='minecraft:block/iron_block','Light Projector interior is not Iron')
need(light.get('textures',{}).get('glass')=='minecraft:block/magenta_stained_glass','Light Projector reflector is not magenta glass')
front=[e for e in light.get('elements',[]) if e.get('from')==[4,9,2.75] and e.get('to')==[12,14,3]]
need(bool(front),'front reflector no longer keeps one-pixel housing margin')
need(len(light_amb.get('elements',[])) >= len(light.get('elements',[]))+3,'Ambient Light Projector side lenses missing')
for facing in ('north','east','south','west'):
    need(light_bs.get('variants',{}).get(f'ambient=true,facing={facing}',{}).get('model')=='mirage_projector:block/mirage_light_projector_ambient',f'Ambient Light Projector model missing for {facing}')
placed=read('src/main/java/celerbi/mirageprojector/client/ClientPlacedLightProjectors.java')
need('new Vec3(0.0D, 1.0D, 0.0D)' in placed,'placed Ambient Light Projector does not emit upward')

# Roadmap authority/recovery docs.
nextwaves=read('docs/NEXT-WAVES-1.1.0.md')
for phrase in ('End Resonance transfer','Dynamic Mirage Light completeness','Projection source architecture completion','Presentation/Codex release polish','Mirage Light Projector aiming — deliberately last'):
    need(phrase in nextwaves,f'NEXT WAVES missing: {phrase}')
need('separate wall illumination chassis' in nextwaves and 'There is no separate wall illumination chassis' in nextwaves,'wall-scope decision not frozen in roadmap')
need((ROOT/'docs/RELEASE-1.0.34-ROADMAP-UX-CLEANUP.md').exists(),'1.0.34 release note missing')

# Language parity / current names.
langs=[json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{loc}.json')) for loc in ('en_us','es_cl','es_es')]
need(set(langs[0])==set(langs[1])==set(langs[2]),'language-key parity broken')
need(langs[0].get('block.mirage_projector.mirage_wall_projector')=='Mirage Wall Projector','English Wall Projector name not restored')
need('block.mirage_projector.mirage_wall_illuminator' not in langs[0],'retired wall illuminator translation survived')

if errors:
    print('Mirage Projector 1.0.34 roadmap/UX cleanup verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print(f'Mirage Projector 1.0.34 roadmap/UX cleanup verification PASS ({len(langs[0])} lang keys)')
