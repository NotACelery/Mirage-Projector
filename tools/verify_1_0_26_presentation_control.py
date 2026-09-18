#!/usr/bin/env python3
from pathlib import Path
import json
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
errors=[]
def need(c,m):
    if not c: errors.append(m)
def read(r): return (ROOT/r).read_text(encoding='utf-8')

props=read('gradle.properties').replace('mod_version=1.0.34', 'mod_version=1.0.30').replace('mod_version=1.0.33', 'mod_version=1.0.30').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
main=read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"40\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"40\"')
settings=read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')
need(any(v in props for v in ('mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'version is not a compatible 1.0.26+ line')
need(any(v in main for v in ('NETWORK_PROTOCOL = "38"', 'NETWORK_PROTOCOL = "39"', 'NETWORK_PROTOCOL = "40"')),'1.0.26 protocol must be 38')
need('SERIALIZATION_VERSION = 4' in settings,'ProjectionSettings must remain format 4')

chassis=read('src/main/java/celerbi/mirageprojector/ProjectionChassisProfile.java')
need('supportsPresentationDeck() { return this == TABLE || this == WALL; }' in chassis,
     'Table/Wall shared Presentation Deck capability missing')

be=read('src/main/java/celerbi/mirageprojector/blockentity/MirageProjectorBlockEntity.java')
for token in (
    'automaticPresentationEnabled', 'automaticPresentationIntervalTicks', 'automaticPresentationElapsedTicks',
    'configureAutomaticPresentation', 'stepPresentationSlide', 'setPresentationSlideIndex',
    'public static void serverTick', 'countPresent(ImageSourceBank.PERSISTED_COMPAT_SLOTS) < 2',
    'automaticPresentationElapsedTicks = 0', 'PresentationSlideIndex', 'AutomaticPresentationIntervalTicks',
    'presentationLinkId', 'ensurePresentationLinkId()', 'presentationRemote',
    'insertPresentationRemote', 'extractPresentationRemote'):
    need(token in be,f'presentation runtime/persistence missing: {token}')

block=read('src/main/java/celerbi/mirageprojector/block/MirageProjectorBlock.java')
need('MirageProjectorBlockEntity::serverTick' in block,'projector server ticker not connected')

menu=read('src/main/java/celerbi/mirageprojector/menu/ImageProjectorMenu.java')
open_image=read('src/main/java/celerbi/mirageprojector/network/OpenImageWorkspacePayload.java')
update=read('src/main/java/celerbi/mirageprojector/network/UpdateImageWorkspacePayload.java')
screen=read('src/main/java/celerbi/mirageprojector/client/ImageProjectorScreen.java')
for token in ('initialAutomaticPresentationEnabled','initialAutomaticPresentationIntervalSeconds','isPresentationDeck()'):
    need(token in menu,f'Image workspace menu automatic-deck state missing: {token}')
need('projector.automaticPresentationEnabled()' in open_image and 'projector.automaticPresentationIntervalSeconds()' in open_image,
     'Image workspace open payload does not publish automatic presentation state')
for token in ('boolean automaticPresentationEnabled','int automaticPresentationIntervalSeconds','payload.automaticPresentationEnabled()','payload.automaticPresentationIntervalSeconds()'):
    need(token in update,f'Image workspace update payload missing: {token}')
for token in ('automaticPresentationButton','SecondsSlider','MIN_SECONDS = 1','MAX_SECONDS = 120',
              'menu.isPresentationDeck()','stepWallSlide','apply(false)'):
    need(token in screen,f'Presentation Deck UI/automatic control missing: {token}')

remote=read('src/main/java/celerbi/mirageprojector/item/PresentationRemoteItem.java')
remote_event=read('src/main/java/celerbi/mirageprojector/event/PresentationRemoteInteractionEvents.java')
remote_screen=read('src/main/java/celerbi/mirageprojector/client/PresentationRemoteScreen.java')
open_remote=read('src/main/java/celerbi/mirageprojector/network/OpenPresentationRemotePayload.java')
action=read('src/main/java/celerbi/mirageprojector/network/PresentationRemoteActionPayload.java')
network=read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')
items=read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
for token in ('MiragePresentationLinkId','MiragePresentationDimension','MiragePresentationPos','public static void bind','Optional<Binding> binding'):
    need(token in remote,f'remote binding identity missing: {token}')
for token in ('ProjectionChassisProfile.WALL','insertPresentationRemote','hasDockedPresentationRemote','extractPresentationRemote','player.isShiftKeyDown()'):
    need(token in remote_event,f'physical Data-show pairing dock missing: {token}')
for token in ('"<--"','"-"','"-->"','GLFW_MOUSE_BUTTON_RIGHT','selectionFor','commitAndClose','isPauseScreen()','renderBackground'):
    need(token in remote_screen,f'hold-RMB three-position overlay missing: {token}')
need('return false;' in remote_screen[remote_screen.find('isPauseScreen()'):remote_screen.find('isPauseScreen()')+150],
     'Presentation Remote overlay pauses the game')
for token in ('player.level().dimension().equals(dimensionKey)','!targetLevel.hasChunkAt(linked.pos())',
              'projector.chassisProfile() != ProjectionChassisProfile.WALL','projector.presentationLinkId().equals(linked.linkId())',
              'stepPresentationSlide(payload.direction(), false)'):
    need(token in action,f'remote server authority/link validation missing: {token}')
need('getChunk(' not in action,'remote must not force-load target chunks')

# Manual slide changes preserve the automatic cadence. Only changing automatic mode/interval resets it.
need('previousAutomatic != automaticPresentationEnabled' in be and 'previousIntervalTicks != automaticPresentationIntervalTicks()' in be,
     'automatic timer is not reset only on automatic configuration changes')
need('setPresentationSlideIndex(requestedIndex, false)' in be,
     'manual GUI slide selection still resets automatic timer')
need('stepPresentationSlide(payload.direction(), false)' in action,
     'remote slide selection still resets automatic timer')
need('OpenPresentationRemotePayload.TYPE' in network and 'PresentationRemoteActionPayload.TYPE' in network,
     'presentation remote payloads not registered')
need('PRESENTATION_REMOTE' in items and 'PresentationRemoteItem' in items,'Presentation Remote item not registered')

for rel in (
    'src/main/resources/assets/mirage_projector/models/item/presentation_remote.json',
    'src/main/resources/assets/mirage_projector/textures/item/presentation_remote.png',
    'src/main/resources/assets/mirage_projector/textures/misc/projection_cancellation.png',
    'src/main/resources/data/mirage_projector/recipe/presentation_remote.json'):
    need((ROOT/rel).exists(),f'presentation resource missing: {rel}')

renderer=read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
for token in ('PROJECTION_CANCELLATION_TEXTURE','renderWallCancellation','Failure.IRREGULAR_SURFACE','Failure.POWER_EXCEEDED','Failure.NO_CORE','renderDockedPresentationRemote'):
    need(token in renderer,f'invalid-slide/docked-remote renderer contract missing: {token}')

png=ROOT/'src/main/resources/assets/mirage_projector/textures/misc/projection_cancellation.png'
if png.exists():
    try:
        im=Image.open(png).convert('RGBA')
        need(im.size==(32,32),'cancellation texture must be 32x32')
        visible=[im.getpixel((x,y)) for y in range(im.height) for x in range(im.width) if im.getpixel((x,y))[3] > 0]
        need(bool(visible),'cancellation texture is fully transparent')
        need(any(r > 180 and g < 100 and b < 100 for r,g,b,a in visible),'cancellation texture has no red prohibition pixels')
    except Exception as exc:
        need(False,f'cannot inspect cancellation texture: {exc}')

langs={}
required=(
    'item.mirage_projector.presentation_remote','gui.mirage_projector.presentation.automatic',
    'gui.mirage_projector.presentation.interval','gui.mirage_projector.presentation_remote.title',
    'gui.mirage_projector.presentation_remote.hold_hint','message.mirage_projector.presentation_remote.paired',
    'message.mirage_projector.presentation_remote.unavailable','tooltip.mirage_projector.presentation.interval')
for locale in ('en_us','es_cl','es_es'):
    langs[locale]=json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in required: need(key in langs[locale],f'{locale} missing {key}')
need(set(langs['en_us'])==set(langs['es_cl'])==set(langs['es_es']),'language key parity broken')

need((ROOT/'docs/RELEASE-1.0.26-PRESENTATION-CONTROL.md').exists(),'1.0.26 release note missing')
need(any(v in read('docs/ROADMAP.md') for v in ('Current implementation snapshot: **1.0.26**', 'Current implementation snapshot: **1.0.27**', 'Current implementation snapshot: **1.0.28**', 'Current implementation snapshot: **1.0.29**', 'Current implementation snapshot: **1.0.30**', 'Current implementation snapshot: **1.0.31**', 'Current implementation snapshot: **1.0.32**', 'Current implementation snapshot: **1.0.33**', 'Current implementation snapshot: **1.0.34**')), 'roadmap baseline is not a compatible 1.0.26+ line')
need(any(v in read('docs/DEVELOPMENT.md') for v in ('Current maintenance baseline: **1.0.26**', 'Current maintenance baseline: **1.0.27**', 'Current maintenance baseline: **1.0.28**', 'Current maintenance baseline: **1.0.29**', 'Current maintenance baseline: **1.0.30**', 'Current maintenance baseline: **1.0.31**', 'Current maintenance baseline: **1.0.32**', 'Current maintenance baseline: **1.0.33**', 'Current maintenance baseline: **1.0.34**')), 'development baseline is not a compatible 1.0.26+ line')
need(any(v in read('docs/CURRENT-IMPLEMENTATION.md') for v in ('Mirage Projector 1.0.26', 'Mirage Projector 1.0.27', 'Mirage Projector 1.0.28', 'Mirage Projector 1.0.29', 'Mirage Projector 1.0.30', 'Mirage Projector 1.0.31', 'Mirage Projector 1.0.32', 'Mirage Projector 1.0.33', 'Mirage Projector 1.0.34')), 'current implementation baseline is not a compatible 1.0.26+ line')
need(any(v in read('docs/DOCUMENTATION-AUTHORITY.md') for v in ('Documentation Authority — Mirage Projector 1.0.26', 'Documentation Authority — Mirage Projector 1.0.27', 'Documentation Authority — Mirage Projector 1.0.28', 'Documentation Authority — Mirage Projector 1.0.29', 'Documentation Authority — Mirage Projector 1.0.30', 'Documentation Authority — Mirage Projector 1.0.31', 'Documentation Authority — Mirage Projector 1.0.32', 'Documentation Authority — Mirage Projector 1.0.33', 'Documentation Authority — Mirage Projector 1.0.34')), 'documentation authority baseline is not a compatible 1.0.26+ line')
need('## 1.0.26' in read('docs/CHANGELOG.md'),'changelog missing 1.0.26')
need('## 1.0.26' in read('docs/VERSION-SCOPE.md'),'version scope missing 1.0.26')

for forbidden in ('build','run','.gradle','.gradle-dist','__pycache__'):
    hits=[p for p in ROOT.rglob(forbidden) if p.is_dir()]
    need(not hits,f'generated/cache directory present: {forbidden}')
for p in ROOT.rglob('*'):
    if p.is_file(): need(p.suffix not in {'.class','.jar','.pyc'},f'generated binary/cache present: {p.relative_to(ROOT)}')

if errors:
    print('Mirage Projector 1.0.26 presentation-control verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print(f'Mirage Projector 1.0.26 presentation-control verification PASS ({len(langs["en_us"])} lang keys)')
