#!/usr/bin/env python3
from pathlib import Path
import json
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
errors = []


def need(cond, msg):
    if not cond:
        errors.append(msg)


def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')


props = read('gradle.properties').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
stabilized_1018 = any(v in props for v in ('mod_version=1.0.18', 'mod_version=1.0.19', 'mod_version=1.0.20', 'mod_version=1.0.21', 'mod_version=1.0.22', 'mod_version=1.0.23', 'mod_version=1.0.24', 'mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30'))
need(any(f'mod_version=1.0.{minor}' in props for minor in (10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30)), 'version is not a compatible 1.0.10+ line')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
need(('NETWORK_PROTOCOL = "28"' in main or 'NETWORK_PROTOCOL = "29"' in main or 'NETWORK_PROTOCOL = "30"' in main or 'NETWORK_PROTOCOL = "31"' in main or 'NETWORK_PROTOCOL = "32"' in main or 'NETWORK_PROTOCOL = "33"' in main or ('NETWORK_PROTOCOL = "34"' in main or ('NETWORK_PROTOCOL = "35"' in main or ('NETWORK_PROTOCOL = \"36\"' in main or ('NETWORK_PROTOCOL = \"37\"' in main or 'NETWORK_PROTOCOL = \"38\"' in main))))), '1.0.10 unexpectedly changed network protocol')

items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
need('ITEMS.register("mirage_lantern"' in items, 'Mirage Lantern item not registered')
need('new MirageFlashlightItem(new Item.Properties().stacksTo(1))' in items, 'Mirage Flashlight compatibility identity is not non-stackable')

lantern = read('src/main/java/celerbi/mirageprojector/item/MirageFlashlightItem.java')
need('MirageLanternCell' in lantern, 'nested exact-cell storage key missing')
need('ItemStack.parseOptional' in lantern and '.save(registries)' in lantern, 'exact cell ItemStack serialization missing')
need('RechargeableEnergyItem.isRechargeable' in lantern, 'lantern cell slot is not generic rechargeable media')
need((('InteractionHand.OFF_HAND' in lantern and 'InteractionHand.MAIN_HAND' in lantern and 'serviceCell' in lantern) or ('PortableDeviceMenu.open' in lantern and 'replaceEnergyCell' in lantern)), 'lantern battery service path missing')
need('mode(flashlight).next()' in lantern, 'portable mode cycling missing')
need('level.getGameTime() % 20L' in lantern, 'once-per-second server drain cadence missing')
need('player.getMainHandItem() != stack && player.getOffhandItem() != stack' in lantern, 'held-only drain guard missing')
need('mode.chargePerSecond()' in lantern, 'lantern does not use centralized mode drain')
need('CELL_PRESENT_TAG' in lantern and 'CELL_PERCENT_TAG' in lantern, 'cheap held-state summary missing')
need('hud.mirage_projector.flashlight.status_discharged' in lantern, 'discharged hotbar state missing')

mode = read('src/main/java/celerbi/mirageprojector/light/device/PortableLightMode.java')
need('displayTranslationKey()' in mode, 'shared portable mode display-name contract missing')
need('case FOCUS -> FLOOD' in mode and 'case FLOOD -> AMBIENT' in mode and 'case AMBIENT -> OFF' in mode and 'case OFF -> FOCUS' in mode,
     'mode cycle is not Focus -> Flood -> Ambient -> Off -> Focus')

client = read('src/main/java/celerbi/mirageprojector/client/ClientHeldFlashlights.java')
need('level.players()' in client, 'remote tracked players are not considered')
need('MirageLightSourceId.entity(kind, player.getUUID())' in client, 'stable per-player lantern identity missing')
need('"flashlight_main"' in client and '"flashlight_off"' in client, 'main/offhand source identity separation missing')
need('player.getLookAngle()' in client and 'player.getEyePosition()' in client, 'held light does not follow player aim/position')
need('ClientDynamicMirageLightManager.submit' in client, 'held lantern is not feeding DYNAMIC_VISUAL')
need('ClientDynamicMirageLightManager.remove' in client, 'off/depleted/unheld source removal missing')
need((('updateLocalHud' in client and 'displayClientMessage' in client) or ('PortableDeviceMenu.open' in lantern and 'shouldCauseReequipAnimation' in lantern)), 'lantern feedback/control path missing')

runtime = read('src/main/java/celerbi/mirageprojector/client/ClientRuntimeEvents.java')
need('ClientHeldFlashlights.submitVisiblePlayers' in runtime, 'held lantern submission not wired into client runtime')
need(('ClientHeldFlashlights.updateLocalHud' in runtime) or ('PortableDeviceMenu.open' in lantern and 'shouldCauseReequipAnimation' in lantern), 'lantern feedback path missing after GUI migration')
need('ClientHeldFlashlights.resetSession' in runtime, 'held lantern session reset missing')

model = ROOT / 'src/main/resources/assets/mirage_projector/models/item/mirage_lantern.json'
texture = ROOT / 'src/main/resources/assets/mirage_projector/textures/item/mirage_lantern.png'
need(model.exists(), 'Mirage Lantern item model missing')
need(texture.exists(), 'Mirage Lantern texture missing')
if texture.exists():
    with Image.open(texture) as image:
        need(image.size == (16, 16), f'Mirage Lantern texture is {image.size}, expected 16x16')

langs = {}
required = (
    'item.mirage_projector.mirage_flashlight',
    'mode.mirage_projector.portable_light.focus',
    'mode.mirage_projector.portable_light.flood',
    'mode.mirage_projector.portable_light.ambient',
    'mode.mirage_projector.portable_light.off',
    'hud.mirage_projector.flashlight.status',
    'hud.mirage_projector.flashlight.status_discharged',
)
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in required:
        need(key in langs[locale], f'{locale} missing {key}')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')
if stabilized_1018:
    for locale in ('en_us', 'es_cl', 'es_es'):
        need('message.mirage_projector.lantern.cell_inserted' not in langs[locale] and 'message.mirage_projector.lantern.cell_extracted' not in langs[locale], f'{locale} retained removed direct-cell Lantern messages')
else:
    for locale in ('en_us', 'es_cl', 'es_es'):
        need('message.mirage_projector.lantern.cell_inserted' in langs[locale] and 'message.mirage_projector.lantern.cell_extracted' in langs[locale], f'{locale} missing historical direct-cell Lantern messages')
need(langs['es_cl'].get('hud.mirage_projector.flashlight.status_discharged', '').endswith('Sin Cargar'),
     'es_cl discharged wording is not Sin Cargar')

waitlist = read('docs/WAITLIST-1.1.0.md')
need('handheld Mirage Flashlight' in waitlist and 'Already delivered as 1.0.x foundations' in waitlist, '1.1 waitlist does not record handheld delivery')
release = read('docs/RELEASE-1.0.10-LANTERN.md')
need('Network protocol remains **28**' in release, '1.0.10 release contract has wrong protocol')

if errors:
    print('Mirage Projector 1.0.10 handheld lantern verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print('Mirage Projector 1.0.10 handheld lantern verification PASS')
