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

props = read('gradle.properties')
need(any(f'mod_version=1.0.{minor}' in props for minor in (7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18)), 'version is not a compatible 1.0.7+ battery line')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
need(('NETWORK_PROTOCOL = "28"' in main or 'NETWORK_PROTOCOL = "29"' in main or 'NETWORK_PROTOCOL = "30"' in main or 'NETWORK_PROTOCOL = "31"' in main or 'NETWORK_PROTOCOL = "32"' in main or 'NETWORK_PROTOCOL = "33"' in main or 'NETWORK_PROTOCOL = "34"' in main), 'protocol changed unexpectedly')

item = read('src/main/java/celerbi/mirageprojector/item/GlowDustItem.java')
need('MAX_CHARGE = 1000' in item, 'Glow Dust charge capacity missing')
need('DataComponents.CUSTOM_DATA' in item, 'Glow Dust charge is not stored on the ItemStack')
need('consumeCharge' in item and 'addCharge' in item, 'Glow Dust charge mutation helpers missing')
need('isBarVisible' in item and 'getBarWidth' in item, 'Glow Dust inventory charge bar missing')
need('item.mirage_projector.glow_dust.depleted' in item, 'depleted naming state missing')

items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
need('ITEMS.register("glow_dust"' in items, 'Glow Dust item is not registered')
need((ROOT / 'src/main/resources/assets/mirage_projector/models/item/glow_dust.json').exists(), 'Glow Dust item model missing')
model = json.loads(read('src/main/resources/assets/mirage_projector/models/item/glow_dust.json'))
need(model.get('textures', {}).get('layer0') == 'minecraft:item/glowstone_dust', 'Glow Dust does not use the vanilla-inspired dust silhouette')

client = read('src/main/java/celerbi/mirageprojector/client/ClientEvents.java')
need('RegisterColorHandlersEvent.Item' in client, 'Glow Dust item tint registration missing')
need('GlowDustItem.chargeFraction' in client, 'Glow Dust tint is not charge-driven')

charger = read('src/main/java/celerbi/mirageprojector/energy/GlowDustBeaconCharging.java')
need('TRANSMISSION_COST = 0.20F' in charger, '20% beam attenuation contract missing')
need('MAX_CLEAR_PATH_CHARGERS = 5' in charger, 'five-charger clear-beam target missing')
need('CHARGE_INTERVAL_TICKS = 10' in charger and 'CHARGE_PER_INTERVAL = 10' in charger, 'charging cadence missing')
need('incomingTransmission' in charger and 'findBeaconBelow' in charger, 'Beacon-column charging validation missing')

be = read('src/main/java/celerbi/mirageprojector/blockentity/CoreBoosterBlockEntity.java')
need('ChargingGlowDust' in be, 'Core Booster charging slot is not persisted')
need('insertChargingDust' in be and 'extractChargingDust' in be, 'Core Booster charging slot operations missing')
need('GlowDustBeaconCharging.canChargeAt' in be, 'Core Booster does not charge from Beacon path')
need(('GlowDustItem.addCharge' in be) or ('RechargeableEnergyItem.addCharge' in be), 'Core Booster does not mutate rechargeable charge')

block = read('src/main/java/celerbi/mirageprojector/block/CoreBoosterBlock.java')
need('CoreBoosterBlockEntity::serverTick' in block, 'Core Booster server charging ticker missing')
need('extractChargingDust' in block, 'breaking Core Booster does not preserve charging dust')

interaction = read('src/main/java/celerbi/mirageprojector/event/CoreBoosterInteractionEvents.java')
need(('held.is(ModItems.GLOW_DUST.get())' in interaction) or ('RechargeableEnergyItem.isRechargeable(held)' in interaction), 'Glow Dust/rechargeable insertion interaction missing')
need('player.isShiftKeyDown() && held.isEmpty() && booster.hasChargingDust()' in interaction, 'Glow Dust extraction interaction missing')

beam = read('src/main/java/celerbi/mirageprojector/client/CryingObsidianBeaconRenderer.java')
need('ColumnEvent.chargingDust' in beam, 'Beacon renderer does not model charging events')
need('GlowDustBeaconCharging.attenuationAfterDust' in beam, 'Beacon renderer does not visibly attenuate charging dust')

optics = read('src/main/java/celerbi/mirageprojector/crying/CryingObsidianCrystalOptics.java')
need('GlowDustBeaconCharging.attenuationAfterDust' in optics, 'Crying Obsidian optics ignore charging attenuation')

langs = {}
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in (
        'item.mirage_projector.glow_dust',
        'item.mirage_projector.glow_dust.depleted',
        'tooltip.mirage_projector.glow_dust.discharged',
        'tooltip.mirage_projector.glow_dust.charge',
        'message.mirage_projector.core_booster.glow_dust_inserted',
    ):
        need(key in langs[locale], f'{locale} missing {key}')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')

if errors:
    print('Mirage Projector 1.0.7 Glow Dust battery verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.7 Glow Dust battery verification PASS')
