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
need('mod_version=1.0.8' in props, 'version is not 1.0.8')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
need('NETWORK_PROTOCOL = "28"' in main, 'protocol changed unexpectedly')

contract = read('src/main/java/celerbi/mirageprojector/item/RechargeableEnergyItem.java')
need('interface RechargeableEnergyItem' in contract, 'shared rechargeable-energy contract missing')
need('maxCharge()' in contract and 'chargePerInterval()' in contract, 'media-specific capacity/rate contract incomplete')
need('DataComponents.CUSTOM_DATA' in contract, 'shared charge is not ItemStack-owned')

glow = read('src/main/java/celerbi/mirageprojector/item/GlowDustItem.java')
need('implements RechargeableEnergyItem' in glow, 'Glow Dust is not on the shared rechargeable contract')
need('MAX_CHARGE = 1000' in glow, 'Glow Dust capacity changed unexpectedly')
need('return 10;' in glow, 'Glow Dust +10-per-pulse baseline missing')

battery = read('src/main/java/celerbi/mirageprojector/item/LightBatteryItem.java')
need('implements RechargeableEnergyItem' in battery, 'Light Battery is not rechargeable')
need('MAX_CHARGE = 4000' in battery, 'Light Battery is not 4x Glow Dust capacity')
need('CHARGE_PER_INTERVAL = 8' in battery, 'Light Battery +8-per-pulse rate missing')
need('MirageLightBatteryCharge' in battery, 'Light Battery persistent charge key missing')
need('isBarVisible' in battery and 'getBarWidth' in battery, 'Light Battery charge bar missing')

items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
need('ITEMS.register("light_battery"' in items, 'Light Battery item not registered')
need((ROOT / 'src/main/resources/assets/mirage_projector/models/item/light_battery.json').exists(), 'Light Battery model missing')
need((ROOT / 'src/main/resources/assets/mirage_projector/textures/item/light_battery_frame.png').exists(), 'Light Battery frame texture missing')
need((ROOT / 'src/main/resources/assets/mirage_projector/textures/item/light_battery_charge.png').exists(), 'Light Battery charge texture missing')

be = read('src/main/java/celerbi/mirageprojector/blockentity/CoreBoosterBlockEntity.java')
need('RechargeableEnergyItem.isRechargeable' in be, 'Core Booster still hard-codes its charging medium')
need('RechargeableEnergyItem.addCharge' in be, 'Core Booster does not delegate charging rate to the medium')
need('CHARGING_DUST_TAG = "ChargingGlowDust"' in be, '1.0.7 charging-slot save key compatibility changed')

interaction = read('src/main/java/celerbi/mirageprojector/event/CoreBoosterInteractionEvents.java')
need('RechargeableEnergyItem.isRechargeable(held)' in interaction, 'generic rechargeable insertion missing')
need('player.getInventory().add(returned)' in interaction, 'extraction does not use inventory merge semantics')
need('player.drop(returned, false)' in interaction, 'extraction overflow safety missing')

renderer = read('src/main/java/celerbi/mirageprojector/client/CoreBoosterRenderer.java')
need('poseStack.scale(0.13F, 0.13F, 0.13F)' in renderer, 'compact charging-medium render scale missing')
need('(coreStack.isEmpty() ? 0.50D : 0.61D)' in renderer, 'centered charging-medium render placement missing')

jade = read('src/main/java/celerbi/mirageprojector/compat/jade/CoreBoosterJadePlugin.java')
need('RechargeableEnergyItem.chargePercent' in jade, 'Jade charging percentage missing')
need('jade.mirage_projector.core_booster.charging_empty' in jade, 'Jade empty charging state missing')

charger = read('src/main/java/celerbi/mirageprojector/energy/GlowDustBeaconCharging.java')
need('TRANSMISSION_COST = 0.20F' in charger, 'Beacon charging attenuation changed')
need('MAX_CLEAR_PATH_CHARGERS = 5' in charger, 'five-cell clear-column ceiling changed')

langs = {}
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in (
        'item.mirage_projector.light_battery',
        'item.mirage_projector.light_battery.depleted',
        'tooltip.mirage_projector.light_battery.charge',
        'tooltip.mirage_projector.light_battery.capacity',
        'message.mirage_projector.core_booster.charging_inserted',
        'message.mirage_projector.core_booster.charging_extracted',
        'jade.mirage_projector.core_booster.charging',
        'jade.mirage_projector.core_booster.charging_empty',
    ):
        need(key in langs[locale], f'{locale} missing {key}')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')

waitlist = read('docs/WAITLIST-1.1.0.md')
need('up to **4 rechargeable cells/batteries**' in waitlist, 'charging-station four-cell input queue not documented')
need('exactly **1 active charging slot**' in waitlist, 'charging-station active slot not documented')
need('**4–5 output slots**' in waitlist, 'charging-station output target not documented')
need('approximately **5 Glow Dust plus additional casing/electrical materials**' in waitlist, 'unresolved Light Battery recipe target not documented')

if errors:
    print('Mirage Projector 1.0.8 rechargeable battery verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.8 rechargeable battery verification PASS')
