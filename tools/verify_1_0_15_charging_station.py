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

props = read('gradle.properties')
need(('mod_version=1.0.15' in props or 'mod_version=1.0.16' in props or 'mod_version=1.0.17' in props or 'mod_version=1.0.18' in props or 'mod_version=1.0.19' in props or 'mod_version=1.0.20' in props), 'version is not a compatible 1.0.15+ line')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
need(('NETWORK_PROTOCOL = "31"' in main or 'NETWORK_PROTOCOL = "32"' in main or 'NETWORK_PROTOCOL = "33"' in main or 'NETWORK_PROTOCOL = "34"' in main), '1.0.15+ network protocol baseline missing')

block = read('src/main/java/celerbi/mirageprojector/block/ChargingStationBlock.java')
need('HorizontalDirectionalBlock.FACING' in block, 'charging station is not horizontal-directional')
need('context.getHorizontalDirection().getOpposite()' in block, 'station front/output does not face placer')
need('useWithoutItem' in block and 'openMenu' in block, 'station menu interaction missing')
need('ChargingStationBlockEntity::serverTick' in block, 'station server ticker missing')

be = read('src/main/java/celerbi/mirageprojector/blockentity/ChargingStationBlockEntity.java')
for token in (
        'INPUT_COUNT = 4', 'CHARGING_SLOT = 4', 'OUTPUT_COUNT = 4', 'SLOT_COUNT = 9',
        'OUTPUT_PUSH_INTERVAL_TICKS = 8', 'moveCompletedCellToOutput', 'advanceInputQueue',
        'pushOneOutputItem', 'RechargeableEnergyItem.isRechargeable', 'copyWithCount(1)'):
    need(token in be, f'charging station invariant missing: {token}')
need('slot == CHARGING_SLOT ? 1' in be, 'active charging slot is not hard-limited to one item')
need('for (int slot = OUTPUT_START; slot < OUTPUT_START + OUTPUT_COUNT; slot++)' in be,
     'output search is not left-to-right')
need('side == outputDirection() ? outputAutomation : inputAutomation' in be,
     'directional automation split missing')
need('ItemHandlerHelper.insertItemStacked' in be, 'automatic output insertion does not use generic item-handler compatibility')
need('Capabilities.ItemHandler.BLOCK' in be, 'automatic output does not target standard NeoForge item handlers')
need('implements MenuProvider, BeaconRechargeableCharger' in be, 'station does not share Beacon charger contract')

cap = read('src/main/java/celerbi/mirageprojector/registry/ModCapabilities.java')
need('Capabilities.ItemHandler.BLOCK' in cap and 'ModBlockEntities.CHARGING_STATION.get()' in cap,
     'charging station item capability registration missing')
need('station.itemHandlerFor(side)' in cap, 'capability provider is not side-aware')
need('ModCapabilities::register' in main, 'capability registration is not wired to mod bus')

energy = read('src/main/java/celerbi/mirageprojector/energy/GlowDustBeaconCharging.java')
core = read('src/main/java/celerbi/mirageprojector/blockentity/CoreBoosterBlockEntity.java')
need('BeaconRechargeableCharger charger' in energy, 'Beacon charging path is still concrete-Core-Booster-only')
need('isActiveChargingBlockEntity' in energy, 'shared active charger optics helper missing')
need('implements BeaconRechargeableCharger' in core, 'Core Booster was not bridged to shared charger contract')
optics = read('src/main/java/celerbi/mirageprojector/crying/CryingObsidianCrystalOptics.java')
renderer = read('src/main/java/celerbi/mirageprojector/client/CryingObsidianBeaconRenderer.java')
need('isActiveChargingBlockEntity' in optics, 'crystal optics ignore Charging Station attenuation')
need('isActiveChargingBlockEntity' in renderer, 'Beacon renderer ignores Charging Station attenuation')

menu = read('src/main/java/celerbi/mirageprojector/menu/ChargingStationMenu.java')
need('ChargingStationBlockEntity.INPUT_COUNT' in menu, 'menu input queue missing')
need('ChargingStationBlockEntity.CHARGING_SLOT' in menu, 'menu active slot missing')
need('ChargingStationBlockEntity.OUTPUT_COUNT' in menu, 'menu output slots missing')
need((('return !outputOnly && RechargeableEnergyItem.isRechargeable(stack)' in menu) or ('return !outputOnly && ChargingStationBlockEntity.isChargeableInput(stack)' in menu)),
     'menu slot validation does not enforce chargeable-input / output-only behavior')

screen = read('src/main/java/celerbi/mirageprojector/client/ChargingStationScreen.java')
need('Input Queue' not in screen, 'screen contains hard-coded English label')
need('charging_station.queue' in screen and 'charging_station.output' in screen,
     'charging station screen localization hooks missing')

blocks = read('src/main/java/celerbi/mirageprojector/registry/ModBlocks.java')
items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
bes = read('src/main/java/celerbi/mirageprojector/registry/ModBlockEntities.java')
menus = read('src/main/java/celerbi/mirageprojector/registry/ModMenus.java')
need('"charging_station"' in blocks, 'charging station block registry missing')
need('"charging_station"' in items, 'charging station item registry missing')
need('"charging_station"' in bes, 'charging station block entity registry missing')
need('"charging_station"' in menus, 'charging station menu registry missing')

for rel in (
        'src/main/resources/assets/mirage_projector/blockstates/charging_station.json',
        'src/main/resources/assets/mirage_projector/models/block/charging_station.json',
        'src/main/resources/assets/mirage_projector/models/item/charging_station.json',
        'src/main/resources/data/mirage_projector/loot_table/blocks/charging_station.json'):
    need((ROOT / rel).exists(), f'missing charging station resource: {rel}')

blockstate = json.loads(read('src/main/resources/assets/mirage_projector/blockstates/charging_station.json'))
need(set(blockstate.get('variants', {})) == {'facing=north','facing=east','facing=south','facing=west'},
     'charging station blockstate does not cover four horizontal facings')
for tex in ('charging_station_side.png','charging_station_top.png','charging_station_output.png'):
    p = ROOT / 'src/main/resources/assets/mirage_projector/textures/block' / tex
    need(p.exists(), f'missing charging station texture {tex}')
    if p.exists():
        with Image.open(p) as image:
            need(image.size == (16,16), f'{tex} is {image.size}, expected 16x16')

langs = {}
for locale in ('en_us','es_cl','es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in (
            'block.mirage_projector.charging_station',
            'container.mirage_projector.charging_station',
            'gui.mirage_projector.charging_station.queue',
            'gui.mirage_projector.charging_station.active',
            'gui.mirage_projector.charging_station.output',
            'gui.mirage_projector.charging_station.progress'):
        need(key in langs[locale], f'{locale} missing {key}')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')

waitlist = read('docs/WAITLIST-1.1.0.md')
need('Dedicated Charging Station — implemented in 1.0.15' in waitlist, '1.1 waitlist does not mark station implemented')
need('**4 input queue slots**' in waitlist and '**4 output slots**' in waitlist,
     'station queue/output contract not frozen in waitlist')
need('one physical Glow Dust / battery' in waitlist, 'single active-cell contract not documented')
need('every face except the machine output face' in waitlist, 'side input policy not documented')
need('one completed item every eight ticks' in waitlist, 'automatic output cadence not documented')
need((ROOT / 'docs/RELEASE-1.0.15-CHARGING-STATION.md').exists(), '1.0.15 release note missing')

if errors:
    print('Mirage Projector 1.0.15 charging station verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.15 charging station verification PASS')
