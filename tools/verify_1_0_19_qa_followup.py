#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors = []

def need(cond, msg):
    if not cond:
        errors.append(msg)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

props = read('gradle.properties').replace('mod_version=1.0.31', 'mod_version=1.0.30')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
settings = read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')
need(any(v in props for v in ('mod_version=1.0.19', 'mod_version=1.0.20', 'mod_version=1.0.21', 'mod_version=1.0.22', 'mod_version=1.0.23', 'mod_version=1.0.24', 'mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'version is not a compatible 1.0.19+ line')
need(('NETWORK_PROTOCOL = "34"' in main or ('NETWORK_PROTOCOL = "35"' in main or ('NETWORK_PROTOCOL = \"36\"' in main or ('NETWORK_PROTOCOL = \"37\"' in main or 'NETWORK_PROTOCOL = \"38\"' in main)))), '1.0.19 must retain protocol 34 (no wire-format change)')
need(('SERIALIZATION_VERSION = 3' in settings or 'SERIALIZATION_VERSION = 4' in settings), 'ProjectionSettings format is not a compatible v3/v4 line')

# Correct portable/placed interaction semantics from QA.
lantern = read('src/main/java/celerbi/mirageprojector/item/MirageLanternItem.java')
shift = lantern.find('if (player.isShiftKeyDown())')
open_menu = lantern.find('PortableDeviceMenu.open', shift)
mode_cycle = lantern.find('PortableLightMode next = mode(lantern).next()')
need(shift >= 0 and open_menu > shift and mode_cycle > open_menu,
     'Lantern must open GUI on Shift+RMB and cycle mode on normal RMB')
need('serviceCell' not in lantern, 'Lantern reintroduced direct hand battery service')

light_interaction = read('src/main/java/celerbi/mirageprojector/event/MirageLightProjectorInteractionEvents.java')
shift = light_interaction.find('if (player.isShiftKeyDown())')
open_menu = light_interaction.find('openMenu(projector', shift)
mode_cycle = light_interaction.find('projector.cycleMode()', shift)
need(shift >= 0 and open_menu > shift and mode_cycle > open_menu,
     'placed Light Projector must open GUI on Shift+RMB and cycle mode on normal RMB')

hand = read('src/main/java/celerbi/mirageprojector/item/MirageHandProjectorItem.java')
need('player.isShiftKeyDown()' in hand and 'PortableDeviceMenu.open' in hand,
     'Hand Projector Shift+RMB GUI path missing')
need('setProjectionEnabled(projector, enabled)' in hand,
     'Hand Projector normal-RMB projection toggle missing')

# GUI sizing and real hover tooltips.
portable_menu = read('src/main/java/celerbi/mirageprojector/menu/PortableDeviceMenu.java')
portable_screen = read('src/main/java/celerbi/mirageprojector/client/PortableDeviceScreen.java')
light_screen = read('src/main/java/celerbi/mirageprojector/client/MirageLightProjectorScreen.java')
station_screen = read('src/main/java/celerbi/mirageprojector/client/ChargingStationScreen.java')
need('COMPACT_PLAYER_INV_Y = 100' in portable_menu and ('PROJECTOR_PLAYER_INV_Y = 198' in portable_menu or 'PROJECTOR_PLAYER_INV_Y = 190' in portable_menu or 'PROJECTOR_PLAYER_INV_Y = 158' in portable_menu),
     'portable menu does not separate compact Lantern and Hand Projector layouts')
need(('imageHeight = menu.projectorLayout() ? HEIGHT : 184' in portable_screen or 'imageHeight = menu.projectorLayout() ? PROJECTOR_HEIGHT : LANTERN_HEIGHT' in portable_screen),
     'Lantern/Hand Projector layouts are not independently sized')
need('10, 20' in portable_screen or ', 20,' in portable_screen,
     'portable device status text was not moved below title')
for name, text in (
    ('Portable Device', portable_screen),
    ('Light Projector', light_screen),
    ('Charging Station', station_screen),
):
    need('renderTooltip(graphics, mouseX, mouseY);' in text,
         f'{name} GUI does not render item hover tooltips')

# Mirage Equipment must initialize in Survival and Creative inventory screens.
equipment_client = read('src/main/java/celerbi/mirageprojector/client/MirageEquipmentClientEvents.java')
need('CreativeModeInventoryScreen' in equipment_client and 'instanceof CreativeModeInventoryScreen' in equipment_client,
     'Mirage Equipment is not exposed in Creative inventory')
need((('creative ? 200 : 180' in equipment_client and 'creative ? 178 : 156' in equipment_client) or ('int panelX = rightEdge + 28;' in equipment_client and 'int toggleX = rightEdge + 7;' in equipment_client) or ('int panelX = rightEdge + 13;' in equipment_client and 'int toggleX = rightEdge - 2;' in equipment_client)),
     'Creative Mirage Equipment panel/toggle placement contract missing')

# Codex behaves like an inventory overlay: live world, no vanilla blur/dim pass.
codex = read('src/main/java/celerbi/mirageprojector/client/ScanCodexScreen.java')
need('public boolean isPauseScreen()' in codex and 'return false;' in codex,
     'Scan Codex still pauses singleplayer')
need('public void renderBackground' in codex and 'applyBlur' not in codex,
     'Scan Codex does not override vanilla blur/dim background as a no-op')

# Charging Station in-world lanes: input behind, output toward the front/output face.
station_renderer = read('src/main/java/celerbi/mirageprojector/client/ChargingStationRenderer.java')
need('ChargingStationBlockEntity.INPUT_COUNT' in station_renderer and '0.25D' in station_renderer,
     'Charging Station input stacks are not rendered on the input/back lane')
need('ChargingStationBlockEntity.OUTPUT_COUNT' in station_renderer and '-0.25D' in station_renderer,
     'Charging Station output stacks are not rendered near the output/front lane')

# Jade must expose active charge progress rather than only slot contents.
jade = read('src/main/java/celerbi/mirageprojector/compat/jade/CoreBoosterJadePlugin.java')
need('ChargingStationComponent.INSTANCE' in jade and 'ChargingStationBlock.class' in jade,
     'Charging Station Jade component is not registered')
need('activeChargingStack()' in jade and 'RechargeableEnergyItem.chargePercent(active)' in jade,
     'Charging Station Jade component does not report active charging percentage')

# Final renderer bridge: virtual Mirage block-light must be merged into packed vertex light.
light_mixin_path = ROOT / 'src/main/java/celerbi/mirageprojector/mixin/client/LevelRendererMirageLightMixin.java'
need(light_mixin_path.exists(), 'LevelRenderer visual Mirage-light bridge mixin missing')
if light_mixin_path.exists():
    mixin = light_mixin_path.read_text(encoding='utf-8')
    need('getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I' in mixin,
         'LevelRenderer simple packed-light overload is not bridged')
    need('getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I' in mixin,
         'LevelRenderer BlockState packed-light overload is not bridged')
    need('MirageLightEngine.virtualBlockLight' in mixin and 'LightTexture.block' in mixin
         and 'LightTexture.sky' in mixin and 'LightTexture.pack' in mixin,
         'visual bridge does not merge Mirage block light while preserving sky light')
mixins_json = json.loads(read('src/main/resources/mirage_projector.mixins.json'))
need('client.LevelRendererMirageLightMixin' in mixins_json.get('client', []),
     'LevelRenderer visual Mirage-light bridge is not registered')

# The accepted floor Light Projector remains, while later aiming/wall/lantern placement are documented.
waitlist = read('docs/WAITLIST-1.1.0.md')
need('floor-standing `Mirage Light Projector`' in waitlist,
     'accepted floor Mirage Light Projector is not documented')
need('yaw' in waitlist.lower() and 'pitch' in waitlist.lower(),
     'future floor Light Projector yaw/pitch aiming is not documented')
need('wall-mounted light projector' in waitlist.lower(),
     'separate wall-mounted light projector remains undocumented')
need('vanilla-lantern-like portable beacon' in waitlist.lower(),
     'future placeable Lantern behavior is not documented')

# Lang parity includes new Jade charge-progress text.
langs = {}
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    need('jade.mirage_projector.charging_station.progress' in langs[locale],
         f'{locale} Charging Station Jade progress localization missing')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')

# No generated build/cache residue in release tree.
for rel in ('build', 'run', '.gradle', '.gradle-dist', '__pycache__'):
    need(not (ROOT / rel).exists(), f'forbidden generated path present: {rel}')
for path in ROOT.rglob('*'):
    if path.is_file():
        need(path.suffix not in {'.class', '.jar', '.pyc'}, f'generated binary/cache present: {path.relative_to(ROOT)}')
        need(not path.name.startswith('javac.'), f'javac temporary file present: {path.relative_to(ROOT)}')

if errors:
    print('Mirage Projector 1.0.19 QA follow-up verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print(f'Mirage Projector 1.0.19 QA follow-up verification PASS ({len(langs["en_us"])} lang keys)')
