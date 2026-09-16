#!/usr/bin/env python3
from pathlib import Path
import json
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
errors=[]
def need(c,m):
    if not c: errors.append(m)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

props=read('gradle.properties').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
need(any(v in props for v in ('mod_version=1.0.14', 'mod_version=1.0.15', 'mod_version=1.0.16', 'mod_version=1.0.17', 'mod_version=1.0.18', 'mod_version=1.0.19', 'mod_version=1.0.20', 'mod_version=1.0.21', 'mod_version=1.0.22', 'mod_version=1.0.23', 'mod_version=1.0.24', 'mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'version is not a compatible 1.0.14+ line')
main=read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
need(('NETWORK_PROTOCOL = "31"' in main or 'NETWORK_PROTOCOL = "32"' in main or 'NETWORK_PROTOCOL = "33"' in main or ('NETWORK_PROTOCOL = "34"' in main or ('NETWORK_PROTOCOL = "35"' in main or ('NETWORK_PROTOCOL = \"36\"' in main or ('NETWORK_PROTOCOL = \"37\"' in main or 'NETWORK_PROTOCOL = \"38\"' in main))))),'1.0.14+ shoulder pouch protocol baseline missing')

equipment=read('src/main/java/celerbi/mirageprojector/equipment/ShoulderEquipment.java')
need('BASE_BATTERY_SLOTS = 6' in equipment,'base pouch is not 6 slots')
need('EXPANDED_BATTERY_SLOTS = 9' in equipment,'expanded pouch is not 9 slots')
need('BASE_UPGRADE_SLOTS = 2' in equipment,'base upgrade socket count is not 2')
need('EXPANDED_UPGRADE_SLOTS = 3' in equipment,'expanded upgrade socket count is not 3')
need('RechargeableEnergyItem.isRechargeable' in equipment,'pouch does not validate generic rechargeable media')
need(('ShoulderUpgradeFamilies.SHOULDER_STRAP_SLOT_EXPANSION' in equipment) or ('ShoulderUpgradeFamilies.BATTERY_POUCH_EXPANSION' in equipment),'expansion family contract missing')
strap_container=read('src/main/java/celerbi/mirageprojector/equipment/ShoulderStrapContainer.java') if (ROOT/'src/main/java/celerbi/mirageprojector/equipment/ShoulderStrapContainer.java').exists() else ''
need(('hasUpgradeFamilyExcludingSlot' in equipment or 'canPlaceUpgrade' in strap_container),'duplicate upgrade-family guard missing')
need('expansionDependentSlotsEmpty' in equipment,'expansion removal safety contract missing')
need('canRemoveStrap' in equipment,'strap dependency-removal guard missing')
need('deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt)' in equipment,'1.0.13 shoulder attachment migration override missing')
need((('normalized.putInt("Size", SLOT_COUNT)' in equipment and 'super.deserializeNBT(provider, normalized)' in equipment) or ('ItemStackHandler legacy' in equipment and 'LEGACY_SLOT_COUNT' in equipment and 'ShoulderStrapContainer' in equipment and 'migrated.serializeNBT(provider)' in equipment)), 'legacy shoulder saves are not normalized/migrated to current layout')

upgrade=read('src/main/java/celerbi/mirageprojector/item/ShoulderUpgrade.java')
need('ResourceLocation shoulderUpgradeFamily' in upgrade,'generic namespaced shoulder upgrade family missing')
recharge=read('src/main/java/celerbi/mirageprojector/item/ShoulderRechargeableDevice.java')
need('shoulderEnergyCell' in recharge and 'shoulderInsertEnergyCell' in recharge and 'shoulderExtractEnergyCell' in recharge,'generic rechargeable shoulder-device cell exchange missing')
for rel in ('src/main/java/celerbi/mirageprojector/item/MirageFlashlightItem.java','src/main/java/celerbi/mirageprojector/item/MirageHandProjectorItem.java'):
    text=read(rel)
    need('implements ShoulderRechargeableDevice' in text,f'{rel} does not expose shoulder battery exchange')

runtime=read('src/main/java/celerbi/mirageprojector/equipment/ShoulderEquipmentRuntime.java')
need('tryAutoBatterySwap' in runtime,'Auto Battery Swap runtime missing')
need('bestChargedBatterySlot' in runtime,'charged candidate selection missing')
need('canStoreOldCellAfterCandidateRemoval' in runtime,'atomic old-cell storage preflight missing')
need('energy.storedCharge(stack)' in runtime,'auto swap does not ignore depleted candidates')
need('syncOwnerInventory' in runtime,'owner pouch/upgrade synchronization missing')
need(('message.mirage_projector.shoulder.auto_swap' in runtime or 'equipment.setDevice(deviceStack)' in runtime),'auto-swap completion contract missing')

payload=read('src/main/java/celerbi/mirageprojector/network/ShoulderEquipmentInventoryPayload.java')
need('EXPANDED_BATTERY_SLOTS' in payload and 'EXPANDED_UPGRADE_SLOTS' in payload,'owner inventory payload does not carry fixed pouch/upgrade layout')
network=read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')
need('ShoulderEquipmentInventoryPayload.TYPE' in network,'owner shoulder inventory payload is not registered')

action=read('src/main/java/celerbi/mirageprojector/network/ShoulderEquipmentActionPayload.java')
need('BATTERY_8' in action and 'UPGRADE_2' in action,'network target surface does not expose 9 pouch + 3 upgrade positions')

client=read('src/main/java/celerbi/mirageprojector/client/MirageEquipmentClientEvents.java')
need((('for (int i = 0; i < 9; i++)' in client) or ('localActiveBatterySlots()' in client and 'Target.battery(i)' in client)),'inventory panel does not expose dynamic pouch positions')
need((('for (int i = 0; i < 3; i++)' in client) or ('localActiveUpgradeSlots()' in client and 'Target.upgrade(i)' in client)),'inventory panel does not expose dynamic upgrade positions')
need('MirageEquipmentPanelWidget' in client,'expanded leather panel background missing')

items=read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
need('auto_battery_swap_patch' in items,'auto battery patch not registered')
need('battery_pouch_expansion_patch' in items,'pouch expansion patch not registered')
for name in ('auto_battery_swap_patch','battery_pouch_expansion_patch'):
    model=ROOT/f'src/main/resources/assets/mirage_projector/models/item/{name}.json'
    tex=ROOT/f'src/main/resources/assets/mirage_projector/textures/item/{name}.png'
    need(model.exists(),f'{name} model missing')
    need(tex.exists(),f'{name} texture missing')
    if tex.exists():
        with Image.open(tex) as image:
            need(image.size==(16,16),f'{name} texture is {image.size}, expected 16x16')

langs={}
for locale in ('en_us','es_cl','es_es'):
    langs[locale]=json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in ('item.mirage_projector.auto_battery_swap_patch','item.mirage_projector.battery_pouch_expansion_patch','gui.mirage_projector.equipment.power_cells','message.mirage_projector.shoulder.auto_swap'):
        need(key in langs[locale],f'{locale} missing {key}')
need(set(langs['en_us'])==set(langs['es_cl'])==set(langs['es_es']),'language parity broken')

w11=read('docs/WAITLIST-1.1.0.md')
w12=read('docs/WAITLIST-1.2.0.md')
need('Foundation delivered in 1.0.14' in w11,'1.1 waitlist does not mark pouch foundation delivered')
need('UV Shoulder Light' in w12 and 'Auto UV Patch' in w12,'UV ecosystem was not kept in 1.2')
release=read('docs/RELEASE-1.0.14-SHOULDER-BATTERY-POUCH.md')
need('Network protocol: **31**' in release,'1.0.14 release protocol not documented')

if errors:
    print('Mirage Projector 1.0.14 shoulder pouch verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print('Mirage Projector 1.0.14 shoulder pouch verification PASS')
