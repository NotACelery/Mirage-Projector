#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors = []

def need(condition, message):
    if not condition:
        errors.append(message)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

props = read('gradle.properties')
stabilized_1018 = any(v in props for v in ('mod_version=1.0.18', 'mod_version=1.0.19', 'mod_version=1.0.20'))
need(('mod_version=1.0.16' in props or 'mod_version=1.0.17' in props or 'mod_version=1.0.18' in props or 'mod_version=1.0.19' in props or 'mod_version=1.0.20' in props), 'version is not a compatible 1.0.16+ line')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
need(('NETWORK_PROTOCOL = "32"' in main or 'NETWORK_PROTOCOL = "33"' in main or 'NETWORK_PROTOCOL = "34"' in main), '1.0.16+ War Banner protocol baseline missing')

item = read('src/main/java/celerbi/mirageprojector/item/MirageHandProjectorItem.java')
for token in (
    'MirageHandProjectorBannerPresentation',
    'MirageHandProjectorWarBannerFacing',
    'MirageHandProjectorWarBannerSize',
    'MirageHandProjectorWarBannerHeight',
    'WAR_BANNER_DEFAULT_SIZE_PERCENT = 65',
    'WAR_BANNER_MIN_SIZE_PERCENT = 45',
    'WAR_BANNER_MAX_SIZE_PERCENT = 80',
    'WAR_BANNER_DEFAULT_HEIGHT_PIXELS = 4',
    'WAR_BANNER_MIN_HEIGHT_PIXELS = 0',
    'WAR_BANNER_MAX_HEIGHT_PIXELS = 12',
    'BannerPresentation.WAR_BANNER',
    'WarBannerFacing.BILLBOARD',
    'cycleBannerPresentation',
    'cycleWarBannerFacing',
    'adjustWarBannerSize',
    'adjustWarBannerHeight',
    'publishState'):
    need(token in item, f'hand projector War Banner contract missing: {token}')
need('sourceMode(projector) == ProjectionSettings.SourceMode.BANNER' in item,
     'War Banner is not restricted to Banner source profiles')
need('return BILLBOARD;' in item, 'Always Face Viewer is not the default War Banner facing')

client = read('src/main/java/celerbi/mirageprojector/client/ClientHeldProjectors.java')
need('MirageHandProjectorItem.warBannerActive(stack)' in client,
     'portable renderer does not route War Banner separately')
need('player.getBbHeight()' in client, 'War Banner anchor does not follow current player pose height')
need('Math.atan2(dx, dz)' in client, 'Billboard yaw is not derived from viewer direction')
need('player.yBodyRotO' in client and 'player.yBodyRot' in client,
     'Directional War Banner does not use interpolated body yaw')
need('warBannerSizePercent(stack)' in client and 'warBannerHeightPixels(stack)' in client,
     'War Banner size/height controls are not consumed by renderer')
need('LightTexture.FULL_BRIGHT' in client, 'War Banner does not preserve projection fullbright behavior')

renderer = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
need('renderPortableWarBanner' in renderer, 'shared Banner renderer helper missing')
need('ProjectionRenderBuffers.wrap(bufferSource, settings)' in renderer,
     'War Banner does not reuse projection ghost/tint rendering wrapper')
need('renderBannerFace(' in renderer, 'War Banner does not reuse Banner pattern/model renderer')

control = read('src/main/java/celerbi/mirageprojector/network/PortableDeviceActionPayload.java') if stabilized_1018 else read('src/main/java/celerbi/mirageprojector/network/ShoulderDeviceControlPayload.java')
for token in (
    'CYCLE_BANNER_PRESENTATION', 'CYCLE_WAR_BANNER_FACING',
    'WAR_BANNER_SIZE_DOWN', 'WAR_BANNER_SIZE_UP',
    'WAR_BANNER_HEIGHT_DOWN', 'WAR_BANNER_HEIGHT_UP'):
    need(token in control, f'Shoulder control missing {token}')
need('MirageHandProjectorItem.publishState(player, device)' in control,
     'War Banner control changes are not immediately published to portable sync')

screen = read('src/main/java/celerbi/mirageprojector/client/PortableDeviceScreen.java') if stabilized_1018 else read('src/main/java/celerbi/mirageprojector/client/ShoulderDeviceScreen.java')
if stabilized_1018:
    for token in ('CYCLE_BANNER_PRESENTATION', 'CYCLE_WAR_BANNER_FACING', 'WAR_BANNER_SIZE_DOWN', 'WAR_BANNER_SIZE_UP', 'WAR_BANNER_HEIGHT_DOWN', 'WAR_BANNER_HEIGHT_UP'):
        need(token in screen, f'Portable Device War Banner UI missing {token}')
else:
    for token in (
        'banner_presentation', 'war_banner_facing', 'war_banner_size', 'war_banner_height',
        'WAR_BANNER_MIN_SIZE_PERCENT', 'WAR_BANNER_MAX_SIZE_PERCENT',
        'WAR_BANNER_MIN_HEIGHT_PIXELS', 'WAR_BANNER_MAX_HEIGHT_PIXELS'):
        need(token in screen, f'Shoulder Device War Banner UI missing {token}')

payload = read('src/main/java/celerbi/mirageprojector/network/PortableProjectorStatePayload.java')
need('CompoundTag customData' in payload, 'portable projector payload no longer carries custom data')
need('portable_projector_state' in payload, 'portable projector state channel missing')

langs = {}
keys = (
    'gui.mirage_projector.portable_device.banner_presentation.forward',
    'gui.mirage_projector.portable_device.banner_presentation.war_banner',
    'gui.mirage_projector.portable_device.war_banner_facing.directional',
    'gui.mirage_projector.portable_device.war_banner_facing.billboard',
) if stabilized_1018 else (
    'gui.mirage_projector.shoulder_device.banner_presentation',
    'gui.mirage_projector.shoulder_device.banner_presentation.forward',
    'gui.mirage_projector.shoulder_device.banner_presentation.war_banner',
    'gui.mirage_projector.shoulder_device.war_banner_facing',
    'gui.mirage_projector.shoulder_device.war_banner_facing.directional',
    'gui.mirage_projector.shoulder_device.war_banner_facing.billboard',
    'gui.mirage_projector.shoulder_device.war_banner_size',
    'gui.mirage_projector.shoulder_device.war_banner_height',
    'tooltip.mirage_projector.hand_projector.banner_presentation',
    'tooltip.mirage_projector.hand_projector.war_banner_facing',
)
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in keys:
        need(key in langs[locale], f'{locale} missing {key}')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')

waitlist = read('docs/WAITLIST-1.1.0.md')
need('delivered in 1.0.16 foundation' in waitlist, '1.1 waitlist does not mark War Banner foundation delivered')
need('Always Face Viewer / Omnidirectional Billboard' in waitlist, 'War Banner billboard contract lost from waitlist')
release = ROOT / 'docs/RELEASE-1.0.16-WAR-BANNER.md'
need(release.exists(), '1.0.16 release note missing')
if release.exists():
    release_text = release.read_text(encoding='utf-8')
    need('Network protocol: **32**' in release_text, 'release note protocol mismatch')
    need('45–80%' in release_text and '0–12 px' in release_text, 'release note War Banner bounds missing')

if errors:
    print('Mirage Projector 1.0.16 War Banner verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.16 War Banner verification PASS')
