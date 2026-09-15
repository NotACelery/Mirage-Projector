#!/usr/bin/env python3
from pathlib import Path
import json
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'src/main/java'
RES = ROOT / 'src/main/resources'
errors = []

def need(cond, msg):
    if not cond:
        errors.append(msg)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

# Current line / format invariants.
need(any(v in read('gradle.properties') for v in ('mod_version=1.0.18', 'mod_version=1.0.19', 'mod_version=1.0.20')), 'version is not a compatible 1.0.18+ line')
need('NETWORK_PROTOCOL = "34"' in read('src/main/java/celerbi/mirageprojector/MirageProjector.java'), 'network protocol is not 34')
need('SERIALIZATION_VERSION = 3' in read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java'), 'ProjectionSettings format changed')

# Public rename cleanup while preserving world/save registry identity.
items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
need('DeferredItem<ShoulderStrapItem> SHOULDER_STRAP' in items, 'Shoulder Strap Java registry symbol not migrated')
need('DeferredItem<ShoulderUpgradePatchItem> SHOULDER_STRAP_SLOT_EXPANSION' in items, 'Shoulder Strap Slot Expansion Java symbol not migrated')
need('ITEMS.register("arm_strap"' in items, 'legacy arm_strap registry ID was changed')
need('ITEMS.register("battery_pouch_expansion_patch"' in items, 'legacy battery_pouch_expansion_patch registry ID was changed')
need('world/save compatibility' in items and 'historical registry ID' not in items, 'legacy-ID compatibility comments are missing/ambiguous')
need((SRC / 'celerbi/mirageprojector/item/ShoulderStrapItem.java').exists(), 'ShoulderStrapItem missing')
need(not (SRC / 'celerbi/mirageprojector/item/ArmStrapItem.java').exists(), 'obsolete ArmStrapItem still exists')
upgrade_families = read('src/main/java/celerbi/mirageprojector/equipment/ShoulderUpgradeFamilies.java')
need('SHOULDER_STRAP_SLOT_EXPANSION' in upgrade_families, 'upgrade family Java name was not migrated')
need('shoulder_strap_slot_expansion' in upgrade_families, 'upgrade family namespaced identity was not migrated')

# No legacy internal Java names/comments. Registry-resource strings above are intentionally exempt.
java_texts = {p: p.read_text(encoding='utf-8') for p in SRC.rglob('*.java')}
joined_java = '\n'.join(java_texts.values())
for token in ('ModItems.ARM_STRAP', 'BATTERY_POUCH_EXPANSION_PATCH', 'ShoulderUpgradeFamilies.BATTERY_POUCH_EXPANSION', 'class ArmStrapItem'):
    need(token not in joined_java, f'legacy internal Java token remains: {token}')
need('Arm Strap' not in joined_java, 'legacy public term Arm Strap remains in active Java source')

# Deprecated explicit event-bus selectors are forbidden; client mod-bus registration is explicit and dist-safe.
need('EventBusSubscriber.Bus.' not in joined_java, 'deprecated EventBusSubscriber.Bus selector remains')
client_mod = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorClient.java')
need('@Mod(value = MirageProjector.MOD_ID, dist = Dist.CLIENT)' in client_mod, 'client-only mod entrypoint is not dist-scoped')
for method in ('registerScreens', 'registerItemColors', 'registerRenderers'):
    need(f'ClientEvents::{method}' in client_mod, f'client mod bus does not register ClientEvents::{method}')

# Superseded runtime classes/payloads must stay deleted.
for rel in (
    'src/main/java/celerbi/mirageprojector/client/ShoulderDeviceScreen.java',
    'src/main/java/celerbi/mirageprojector/network/ShoulderDeviceControlPayload.java',
    'src/main/java/celerbi/mirageprojector/client/MirageLightProjectorRenderer.java',
    'src/main/java/celerbi/mirageprojector/light/LightProfileMath.java',
):
    need(not (ROOT / rel).exists(), f'obsolete runtime file returned: {rel}')
network = read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')
need('ShoulderDeviceControlPayload' not in network, 'obsolete shoulder-device payload is still registered')
need('PortableDeviceActionPayload.TYPE' in network, 'replacement PortableDeviceActionPayload is not registered')
need('MirageLightProjectorRenderer' not in read('src/main/java/celerbi/mirageprojector/client/ClientEvents.java'), 'removed floating-battery renderer is registered')

# Every concrete payload in the network package must be registered exactly through its TYPE symbol.
network_dir = SRC / 'celerbi/mirageprojector/network'
for path in sorted(network_dir.glob('*.java')):
    if path.name == 'ModNetworking.java':
        continue
    text = path.read_text(encoding='utf-8')
    if re.search(r'\b(?:record|class)\s+\w+[^\n{]*implements\s+CustomPacketPayload', text):
        need(f'{path.stem}.TYPE' in network, f'payload exists but is not registered: {path.stem}')

# Every mixin declared in config must resolve to a source class.
mixin_cfg = json.loads(read('src/main/resources/mirage_projector.mixins.json'))
mixin_root = SRC / 'celerbi/mirageprojector/mixin'
for declared in mixin_cfg.get('mixins', []) + mixin_cfg.get('client', []):
    path = mixin_root / (declared.replace('.', '/') + '.java')
    need(path.exists(), f'mixin config points at missing class: {declared}')

# Localization cleanup / parity. Stable item translation keys follow legacy registry IDs by design.
langs = {}
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language key parity broken')
for locale, data in langs.items():
    need(data.get('item.mirage_projector.arm_strap') in {'Shoulder Strap', 'Correa de Hombro'}, f'{locale} Shoulder Strap display name wrong')
    need('item.mirage_projector.battery_pouch_expansion_patch' in data, f'{locale} legacy expansion item translation key missing')
    need('tooltip.mirage_projector.shoulder_strap_slot_expansion' in data, f'{locale} current expansion tooltip missing')
    need(not any(k.startswith('gui.mirage_projector.shoulder_device.') for k in data), f'{locale} obsolete ShoulderDeviceScreen keys remain')
    for dead in (
        'tooltip.mirage_projector.auto_battery_swap_patch',
        'tooltip.mirage_projector.glow_dust.full',
        'tooltip.mirage_projector.glow_dust.recharge',
        'tooltip.mirage_projector.light_battery.capacity',
        'tooltip.mirage_projector.light_battery.full',
        'tooltip.mirage_projector.light_battery.recharge',
        'tooltip.mirage_projector.lantern.service',
        'tooltip.mirage_projector.hand_projector.copy',
        'tooltip.mirage_projector.hand_projector.persistent',
        'message.mirage_projector.lantern.cell_inserted',
        'message.mirage_projector.lantern.cell_extracted',
        'message.mirage_projector.light_projector.cell_inserted',
        'message.mirage_projector.light_projector.cell_extracted',
        'gui.mirage_projector.portable_device.presentation',
        'gui.mirage_projector.portable_device.facing',
        'gui.mirage_projector.portable_device.war_banner_size',
        'gui.mirage_projector.portable_device.war_banner_height',
    ):
        need(dead not in data, f'{locale} retained obsolete localization: {dead}')

# Stabilized portable GUI must have enough vertical separation for War Banner controls and inventory.
portable_menu = read('src/main/java/celerbi/mirageprojector/menu/PortableDeviceMenu.java')
portable_screen = read('src/main/java/celerbi/mirageprojector/client/PortableDeviceScreen.java')
need('PROJECTOR_PLAYER_INV_Y = 190' in portable_menu, 'Portable Device player inventory Y was not moved below controls')
need('HEIGHT = 274' in portable_screen, 'Portable Device screen height does not fit stabilized controls')
need('gui.mirage_projector.portable_device.banner_presentation' in portable_screen, 'War Banner presentation button does not expose current state')
need('gui.mirage_projector.portable_device.war_banner_facing' in portable_screen, 'War Banner facing button does not expose current state')

# Persistent hotbar-spam path must stay gone while event-driven status remains available.
held_projectors = read('src/main/java/celerbi/mirageprojector/client/ClientHeldProjectors.java')
need('updateLocalHud' not in held_projectors and 'lastHudTick' not in held_projectors, 'dead persistent Hand Projector HUD loop remains')
need('ClientHeldProjectors.updateLocalHud' not in read('src/main/java/celerbi/mirageprojector/client/ClientRuntimeEvents.java'), 'runtime still calls removed projector HUD loop')

# Current-state docs use current public names; historical release docs are intentionally not rewritten.
for rel in ('README.md', 'docs/CURRENT-IMPLEMENTATION.md', 'docs/WAITLIST-1.1.0.md', 'docs/DEVELOPMENT.md', 'docs/REGISTRY-INVENTORY.md'):
    text = read(rel)
    need('Shoulder Strap' in text, f'{rel} does not use current Shoulder Strap terminology')
need('Shoulder Strap Slot Expansion' in read('docs/CURRENT-IMPLEMENTATION.md'), 'current implementation still uses old expansion public name')

# Generated/cache/build residue must not enter source snapshots.
for forbidden_dir in ('build', 'run', '.gradle', '.gradle-dist', '__pycache__'):
    hits = [p for p in ROOT.rglob(forbidden_dir) if p.is_dir()]
    need(not hits, f'generated/cache directory present: {forbidden_dir}: {[str(p.relative_to(ROOT)) for p in hits[:5]]}')
for suffix in ('.class', '.jar'):
    hits = [p for p in ROOT.rglob(f'*{suffix}') if p.is_file()]
    need(not hits, f'generated binary present ({suffix}): {[str(p.relative_to(ROOT)) for p in hits[:5]]}')
for p in ROOT.glob('javac.*.args'):
    need(False, f'javac scratch file present: {p.name}')

# Audit document itself is part of the maintenance record.
need((ROOT / 'docs/history/audits/1.0.18-INTEGRITY-CLEANUP-AUDIT.md').exists(), 'integrity cleanup audit document missing')

if errors:
    print('Mirage Projector 1.0.18 integrity cleanup verification FAILED')
    for error in errors:
        print(' -', error)
    sys.exit(1)
print(f'Mirage Projector 1.0.18 integrity cleanup verification PASS ({len(langs["en_us"])} lang keys)')
