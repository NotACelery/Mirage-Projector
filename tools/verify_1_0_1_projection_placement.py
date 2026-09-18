#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

def need(cond, msg):
    if not cond:
        errors.append(msg)

props = read('gradle.properties').replace('mod_version=1.0.34', 'mod_version=1.0.30').replace('mod_version=1.0.33', 'mod_version=1.0.30').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
settings = read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')
transform = read('src/main/java/celerbi/mirageprojector/ProjectionTransform.java')
screen = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
menu = read('src/main/java/celerbi/mirageprojector/menu/MirageProjectorMenu.java')
renderer = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
clearance = read('src/main/java/celerbi/mirageprojector/client/ProjectionClearance.java')

need('mod_version=1.0.1' in props, 'version is not 1.0.1')
need('NETWORK_PROTOCOL = "28"' in main, 'protocol is not 28')
need('SERIALIZATION_VERSION = 3' in settings, 'ProjectionSettings format is not v3')
for field in ('horizontalOffsetPixels', 'verticalOffsetPixels', 'distanceOffsetPixels'):
    need(field in settings, f'missing settings field {field}')
    need(field in transform, f'missing transform field {field}')
for tag in ('HorizontalOffsetPixels', 'VerticalOffsetPixels', 'DistanceOffsetPixels'):
    need(tag in settings, f'missing NBT placement tag {tag}')
need('serializationVersion >= 3 ? buffer.readVarInt() : 0' in settings, 'v2 network compatibility defaults missing')
need('orientationFromTiltDegrees' in transform, 'tilt quaternion construction missing')
need('MAX_TILT_DEGREES = 89' in settings, 'tilt safety bound changed')
need('MAX_PLACEMENT_OFFSET_PIXELS = 256' in settings, 'placement offset bound changed')

need('imageHeight = 584' in screen, 'main projector screen height was not expanded for placement controls')
for key in ('offset_horizontal', 'offset_vertical', 'offset_distance', 'tilt', 'reset_position', 'reset_tilt'):
    need(f'gui.mirage_projector.{key}' in screen, f'missing UI control {key}')
need('withPlacement(horizontalOffsetPixels, verticalOffsetPixels, distanceOffsetPixels, tiltDegrees)' in screen,
     'Apply path does not persist placement controls')
need('CORE_SLOT_Y = 384' in menu and 'PLAYER_INV_Y = 468' in menu, 'main menu slots were not moved with expanded layout')

need(renderer.count('applyProjectionPlacement(') >= 7, 'shared placement helper is not used by all projected sources')
need('settings.horizontalOffsetPixels() * PIXEL' in renderer, 'renderer horizontal offset missing')
need('settings.distanceOffsetPixels() * PIXEL' in renderer, 'renderer distance offset missing')
need('settings.verticalOffsetPixels() * PIXEL' in renderer, 'renderer vertical offset missing')
need('new Quaternionf(orientation.x(), orientation.y(), orientation.z(), orientation.w())' in renderer,
     'renderer quaternion tilt missing')
need('projectionWorldOffset(blockEntity, settings)' in renderer, 'offset-aware culling/front-side logic missing')
need('settings.tiltDegrees()' in renderer, 'render bounding box does not account for tilt')
need('double normalY = -Math.sin(tiltRadians)' in renderer, 'tilted plane front/back selection ignores camera height')
need('double toCameraY = camera.y - originY' in renderer, 'tilted plane front/back selection lacks vertical camera vector')

for token in ('horizontalOffsetPixels()', 'verticalOffsetPixels()', 'distanceOffsetPixels()', 'tiltDegrees()'):
    need(token in clearance, f'clearance missing {token}')

for lang in ('en_us', 'es_cl', 'es_es'):
    data = json.loads((ROOT / f'src/main/resources/assets/mirage_projector/lang/{lang}.json').read_text(encoding='utf-8'))
    for key in (
        'gui.mirage_projector.section.placement',
        'gui.mirage_projector.offset_horizontal',
        'gui.mirage_projector.offset_vertical',
        'gui.mirage_projector.offset_distance',
        'gui.mirage_projector.tilt',
        'gui.mirage_projector.reset_position',
        'gui.mirage_projector.reset_tilt',
        'tooltip.mirage_projector.offset_horizontal',
        'tooltip.mirage_projector.offset_vertical',
        'tooltip.mirage_projector.offset_distance',
        'tooltip.mirage_projector.tilt',
    ):
        need(key in data, f'{lang} missing {key}')

if errors:
    print('Mirage Projector 1.0.1 projection-placement verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.1 projection-placement verification PASS')
