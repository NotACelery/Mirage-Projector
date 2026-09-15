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

props = read('gradle.properties')
stabilized_1018 = any(v in props for v in ('mod_version=1.0.18', 'mod_version=1.0.19', 'mod_version=1.0.20'))
need(any(f'mod_version=1.0.{minor}' in props for minor in (12, 13, 14, 15, 16, 17, 18, 19, 20)), 'version is not a compatible 1.0.12+ line')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
need(('NETWORK_PROTOCOL = "29"' in main or 'NETWORK_PROTOCOL = "30"' in main or 'NETWORK_PROTOCOL = "31"' in main or 'NETWORK_PROTOCOL = "32"' in main or 'NETWORK_PROTOCOL = "33"' in main or 'NETWORK_PROTOCOL = "34"' in main), '1.0.12+ protocol baseline missing')

item = read('src/main/java/celerbi/mirageprojector/item/MirageHandProjectorItem.java')
need('DEVICE_ID_TAG = "MirageHandProjectorId"' in item, 'stable portable device UUID key missing')
need('ensureDeviceId' in item and 'UUID.randomUUID()' in item, 'portable device UUID creation missing')
need('PortableProjectorStatePayload' in item and 'PacketDistributor.sendToPlayer' in item, 'server portable-state publication missing')
need('if (projectionEnabled(stack))' in item and 'syncState(player, stack)' in item, 'active inventory projector heartbeat missing')
need('player.getMainHandItem() != stack && player.getOffhandItem() != stack' not in item, 'handheld projector still drains only while held')
need('energy.consumeStoredCharge(cell, drain)' in item, 'portable projector battery drain missing')

payload = read('src/main/java/celerbi/mirageprojector/network/PortableProjectorStatePayload.java')
need('ownerId' in payload and 'deviceId' in payload, 'portable sync identity fields missing')
need('CompoundTag customData' in payload and 'writeNbt' in payload and 'readNbt' in payload, 'portable sync NBT state body missing')
need('ClientHeldProjectors.acceptServerState' in payload, 'portable payload does not reach client cache')

network = read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')
need('PortableProjectorStatePayload.TYPE' in network, 'portable state payload not registered')
need('registrar.playToClient' in network, 'play-to-client networking surface missing')

client = read('src/main/java/celerbi/mirageprojector/client/ClientHeldProjectors.java')
need('SERVER_STATES' in client, 'client portable-state cache missing')
need('STALE_STATE_TICKS' in client and 'pruneStale' in client, 'portable stale-state cleanup missing')
need('payload.customData()' in client and 'CustomData.update' in client, 'portable synchronized custom-data reconstruction missing')
need('MirageHandProjectorItem.createPortableProjector' in client, 'synced portable projector does not reuse renderer reconstruction')
need(('updateLocalHud' not in client) if stabilized_1018 else ('player.getInventory().getContainerSize()' in client), 'portable inventory-state/UI contract mismatch')

creative = read('src/main/java/celerbi/mirageprojector/item/CreativeBatteryItem.java')
need('implements RechargeableEnergyItem' in creative, 'Creative Battery does not use generic rechargeable contract')
need('storedCharge(ItemStack stack)' in creative and 'return MAX_CHARGE;' in creative, 'Creative Battery is not permanently full')
need('consumeStoredCharge(ItemStack stack, int amount)' in creative, 'Creative Battery infinite discharge override missing')
need('return Math.max(0, amount);' in creative, 'Creative Battery does not acknowledge consumption while staying full')
need('depleted(ItemStack stack)' in creative and 'return false;' in creative, 'Creative Battery can become depleted')

items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
need('ITEMS.register("creative_battery"' in items, 'Creative Battery item not registered')
creative_tab = read('src/main/java/celerbi/mirageprojector/registry/ModCreativeTabs.java')
need('output.accept(ModItems.CREATIVE_BATTERY.get())' in creative_tab, 'Creative Battery missing from Mirage creative tab')

model = ROOT / 'src/main/resources/assets/mirage_projector/models/item/creative_battery.json'
texture = ROOT / 'src/main/resources/assets/mirage_projector/textures/item/creative_battery.png'
need(model.exists(), 'Creative Battery item model missing')
need(texture.exists(), 'Creative Battery texture missing')
if texture.exists():
    with Image.open(texture) as image:
        need(image.size == (16, 16), f'Creative Battery texture is {image.size}, expected 16x16')

langs = {}
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in (
        'item.mirage_projector.creative_battery',
        'tooltip.mirage_projector.creative_battery.infinite',
        'tooltip.mirage_projector.creative_battery.debug_only',
    ):
        need(key in langs[locale], f'{locale} missing {key}')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')
if stabilized_1018:
    for locale in ('en_us', 'es_cl', 'es_es'):
        need('tooltip.mirage_projector.hand_projector.persistent' not in langs[locale] and 'tooltip.mirage_projector.hand_projector.use' in langs[locale], f'{locale} portable-projector tooltip cleanup mismatch')
else:
    for locale in ('en_us', 'es_cl', 'es_es'):
        need('tooltip.mirage_projector.hand_projector.persistent' in langs[locale], f'{locale} missing historical persistent tooltip')

waitlist = read('docs/WAITLIST-1.1.0.md')
need('#### War Banner presentation mode' in waitlist, 'War Banner waitlist contract missing')
need('Always Face Viewer / Omnidirectional Billboard' in waitlist, 'War Banner billboard option missing')
need('no physical banner pole/staff' in waitlist, 'War Banner pole-less requirement missing')
need('smaller scale than the normal forward-projected Banner' in waitlist, 'War Banner smaller-scale requirement missing')
need('Creative / debug battery' in waitlist, 'Creative/debug battery waitlist contract missing')
need('delivered in 1.0.12' in waitlist.lower(), '1.0.12 portable persistence delivery not recorded in waitlist')

release = read('docs/RELEASE-1.0.12-PERSISTENT-PORTABLE-STATE.md')
need('network protocol advances to **29**' in release, '1.0.12 release note does not document protocol 29')
need('Creative Battery' in release and 'Persistent inventory projector' in release, '1.0.12 release note incomplete')
need((ROOT / 'docs/history/handoffs/NEXT-CHAT-HANDOFF-1.0.12-PERSISTENT-PORTABLE.md').exists(), '1.0.12 handoff missing')

if errors:
    print('Mirage Projector 1.0.12 persistent portable-state verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print('Mirage Projector 1.0.12 persistent portable-state verification PASS')
