#!/usr/bin/env python3
from pathlib import Path
import json
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
errors = []

def need(condition, message):
    if not condition:
        errors.append(message)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

props = read('gradle.properties').replace('mod_version=1.0.31', 'mod_version=1.0.30')
stabilized_1018 = any(v in props for v in ('mod_version=1.0.18', 'mod_version=1.0.19', 'mod_version=1.0.20', 'mod_version=1.0.21', 'mod_version=1.0.22', 'mod_version=1.0.23', 'mod_version=1.0.24', 'mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30'))
need(any(v in props for v in ('mod_version=1.0.13', 'mod_version=1.0.14', 'mod_version=1.0.15', 'mod_version=1.0.16', 'mod_version=1.0.17', 'mod_version=1.0.18', 'mod_version=1.0.19', 'mod_version=1.0.20', 'mod_version=1.0.21', 'mod_version=1.0.22', 'mod_version=1.0.23', 'mod_version=1.0.24', 'mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'version is not a compatible 1.0.13+ line')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
need(('NETWORK_PROTOCOL = "30"' in main or 'NETWORK_PROTOCOL = "31"' in main or 'NETWORK_PROTOCOL = "32"' in main or 'NETWORK_PROTOCOL = "33"' in main or ('NETWORK_PROTOCOL = "34"' in main or ('NETWORK_PROTOCOL = "35"' in main or ('NETWORK_PROTOCOL = \"36\"' in main or ('NETWORK_PROTOCOL = \"37\"' in main or 'NETWORK_PROTOCOL = \"38\"' in main))))), '1.0.13+ shoulder protocol baseline missing')
need('ModAttachments.register(modEventBus)' in main, 'attachment registry is not initialized')

attachment = read('src/main/java/celerbi/mirageprojector/registry/ModAttachments.java')
need('shoulder_equipment' in attachment, 'shoulder equipment attachment registration missing')
need('AttachmentType.serializable' in attachment, 'shoulder equipment is not serializable player attachment data')

equipment = read('src/main/java/celerbi/mirageprojector/equipment/ShoulderEquipment.java')
strap_container = read('src/main/java/celerbi/mirageprojector/equipment/ShoulderStrapContainer.java') if (ROOT / 'src/main/java/celerbi/mirageprojector/equipment/ShoulderStrapContainer.java').exists() else ''
need('STRAP_SLOT = 0' in equipment and 'DEVICE_SLOT = 1' in equipment, 'shoulder equipment logical slot contract missing')
need(('ShoulderMountableDevice' in equipment or 'ShoulderMountableDevice' in strap_container), 'Shoulder Slot does not use mountable-device contract')
need(('slot == STRAP_SLOT && !canRemoveStrap()' in equipment or 'canRemoveStrap()' in equipment), 'strap removal guard missing')

runtime = read('src/main/java/celerbi/mirageprojector/equipment/ShoulderEquipmentRuntime.java')
need('player.containerMenu.getCarried()' in runtime, 'server-authoritative cursor interaction missing')
need('getShoulderEntityRight()' in runtime, 'right vanilla shoulder occupancy guard missing')
need('mountable.serverTickShoulder' in runtime, 'mounted device runtime ticking missing')
need('ShoulderEquipmentStatePayload' in runtime, 'shoulder state publication missing')

events = read('src/main/java/celerbi/mirageprojector/event/ShoulderEquipmentEvents.java')
need('PlayerTickEvent.Post' in events, 'shoulder equipment tick hook missing')
need('PlayerEvent.Clone' in events and 'RULE_KEEPINVENTORY' in events, 'keepInventory clone handling missing')
need('LivingDropsEvent' in events, 'normal death shoulder drops missing')
legacy_device_drop = events.find('dropSlot(player, event, equipment, ShoulderEquipment.DEVICE_SLOT)')
legacy_strap_drop = events.find('dropSlot(player, event, equipment, ShoulderEquipment.STRAP_SLOT)')
packed_device_drop = events.find('equipment.extractDevice()')
packed_strap_drop = events.find('equipment.extractItem(ShoulderEquipment.STRAP_SLOT')
need(
    (legacy_device_drop >= 0 and legacy_strap_drop >= 0 and legacy_device_drop < legacy_strap_drop)
    or (packed_device_drop >= 0 and packed_strap_drop >= 0 and packed_device_drop < packed_strap_drop),
    'death drop order must remove device before strap'
)

mountable = read('src/main/java/celerbi/mirageprojector/item/ShoulderMountableDevice.java')
lantern = read('src/main/java/celerbi/mirageprojector/item/MirageLanternItem.java')
projector = read('src/main/java/celerbi/mirageprojector/item/MirageHandProjectorItem.java')
need('interface ShoulderMountableDevice' in mountable, 'shoulder mountable device contract missing')
need(('implements ShoulderMountableDevice' in lantern or 'implements ShoulderRechargeableDevice' in lantern), 'Mirage Lantern is not shoulder mountable')
need('serverTickShoulder' in lantern, 'shoulder-mounted Lantern ticking missing')
need(('ShoulderMountableDevice' in projector or 'ShoulderRechargeableDevice' in projector) and 'serverTickShoulder' in projector, 'Mirage Hand Projector shoulder ticking missing')

items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
need('ITEMS.register("arm_strap"' in items, 'Arm Strap item is not registered')
tab = read('src/main/java/celerbi/mirageprojector/registry/ModCreativeTabs.java')
need(('output.accept(ModItems.SHOULDER_STRAP.get())' in tab) if stabilized_1018 else ('output.accept(ModItems.ARM_STRAP.get())' in tab), 'Shoulder Strap missing from Mirage creative tab')

network = read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')
payloads = ('ShoulderEquipmentActionPayload', 'PortableDeviceActionPayload', 'ShoulderEquipmentStatePayload') if stabilized_1018 else ('ShoulderEquipmentActionPayload', 'ShoulderDeviceControlPayload', 'ShoulderEquipmentStatePayload')
for payload in payloads:
    need(f'{payload}.TYPE' in network, f'{payload} is not registered')

client = read('src/main/java/celerbi/mirageprojector/client/ClientShoulderEquipment.java')
need('submitShoulderLanterns' in client and 'ClientDynamicMirageLightManager.submit' in client, 'mounted Lantern dynamic-light bridge missing')
need('renderMountedDevice' in client, 'physical shoulder-device render helper missing')
client_events = read('src/main/java/celerbi/mirageprojector/client/MirageEquipmentClientEvents.java')
need('InventoryScreen' in client_events and 'ScreenEvent.Init.Post' in client_events, 'inventory equipment panel hook missing')
need('RenderPlayerEvent.Post' in client_events, 'shoulder physical render event hook missing')
slot = read('src/main/java/celerbi/mirageprojector/client/MirageEquipmentSlotWidget.java')
need((('ShoulderDeviceScreen' in slot) or ('OpenPortableDeviceMenuPayload' in slot and 'PortableDeviceSource.SHOULDER' in slot)) and 'button == 1' in slot, 'RMB Shoulder Slot device GUI contract missing')
need('LEATHER_BORDER' in slot, 'brown/leather equipment visual treatment missing')
legacy_screen_path = ROOT / 'src/main/java/celerbi/mirageprojector/client/ShoulderDeviceScreen.java'
portable_screen_path = ROOT / 'src/main/java/celerbi/mirageprojector/client/PortableDeviceScreen.java'
legacy_screen = legacy_screen_path.read_text(encoding='utf-8') if legacy_screen_path.exists() else ''
portable_screen = portable_screen_path.read_text(encoding='utf-8') if portable_screen_path.exists() else ''
need(
    ('CYCLE_LANTERN_MODE' in legacy_screen and 'TOGGLE_PROJECTOR' in legacy_screen)
    or ('CYCLE_LANTERN_MODE' in portable_screen and 'TOGGLE_PROJECTOR' in portable_screen),
    'initial shoulder device controls incomplete'
)

runtime_events = read('src/main/java/celerbi/mirageprojector/client/ClientRuntimeEvents.java')
need('ClientShoulderEquipment.submitShoulderLanterns' in runtime_events, 'shoulder Lantern submission not wired')
need('ClientShoulderEquipment.resetSession' in runtime_events, 'shoulder client state reset missing')

mixin = read('src/main/java/celerbi/mirageprojector/mixin/PlayerShoulderReservationMixin.java')
need('setEntityOnShoulder' in mixin and 'getShoulderEntityLeft' in mixin and 'getShoulderEntityRight' in mixin, 'vanilla right-shoulder reservation mixin incomplete')
mixins = read('src/main/resources/mirage_projector.mixins.json')
need('PlayerShoulderReservationMixin' in mixins, 'shoulder reservation mixin not enabled')

model = ROOT / 'src/main/resources/assets/mirage_projector/models/item/arm_strap.json'
texture = ROOT / 'src/main/resources/assets/mirage_projector/textures/item/arm_strap.png'
need(model.exists(), 'Arm Strap item model missing')
need(texture.exists(), 'Arm Strap texture missing')
if texture.exists():
    with Image.open(texture) as image:
        need(image.size == (16, 16), f'Arm Strap texture is {image.size}, expected 16x16')

langs = {}
required = (
    'item.mirage_projector.arm_strap',
    'gui.mirage_projector.equipment.toggle',
    'gui.mirage_projector.equipment.strap_slot',
    'gui.mirage_projector.equipment.shoulder_slot',
    'message.mirage_projector.shoulder.strap_blocked',
    'message.mirage_projector.shoulder.vanilla_occupied',
)
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in required:
        need(key in langs[locale], f'{locale} missing {key}')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')
if stabilized_1018:
    for locale in ('en_us', 'es_cl', 'es_es'):
        need(not any(k.startswith('gui.mirage_projector.shoulder_device.') for k in langs[locale]), f'{locale} retained obsolete ShoulderDeviceScreen localization')

waitlist = read('docs/WAITLIST-1.1.0.md')
need(('#### Mirage Equipment / Arm Strap / Shoulder Slot' in waitlist) or ('#### Mirage Equipment / Shoulder Strap / Shoulder Slot' in waitlist), 'Shoulder Equipment waitlist contract missing')
need('must not compete with vanilla chest armor or the normal offhand' in waitlist, 'armor/offhand independence not frozen')
need('right shoulder' in waitlist.lower(), 'right-shoulder reservation not documented')
need('bird/Parrot-like disguise' in waitlist, 'future bird-skin direction not documented')
release = read('docs/RELEASE-1.0.13-SHOULDER-EQUIPMENT.md')
need('29 to 30' in release, '1.0.13 release protocol bump not documented')
need((ROOT / 'docs/history/handoffs/NEXT-CHAT-HANDOFF-1.0.13-SHOULDER-EQUIPMENT.md').exists(), '1.0.13 handoff missing')

if errors:
    print('Mirage Projector 1.0.13 shoulder equipment verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.13 shoulder equipment verification PASS')
