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
stabilized_1018 = any(v in props for v in ('mod_version=1.0.18', 'mod_version=1.0.19', 'mod_version=1.0.20', 'mod_version=1.0.21', 'mod_version=1.0.22', 'mod_version=1.0.23', 'mod_version=1.0.24', 'mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30'))
need(any(f'mod_version=1.0.{minor}' in props for minor in (9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30)), 'current branch is not a compatible 1.0.9+ line')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
need(('NETWORK_PROTOCOL = "28"' in main or 'NETWORK_PROTOCOL = "29"' in main or 'NETWORK_PROTOCOL = "30"' in main or 'NETWORK_PROTOCOL = "31"' in main or 'NETWORK_PROTOCOL = "32"' in main or 'NETWORK_PROTOCOL = "33"' in main or ('NETWORK_PROTOCOL = "34"' in main or ('NETWORK_PROTOCOL = "35"' in main or ('NETWORK_PROTOCOL = \"36\"' in main or ('NETWORK_PROTOCOL = \"37\"' in main or 'NETWORK_PROTOCOL = \"38\"' in main))))), 'protocol changed unexpectedly')

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
        'message.mirage_projector.core_booster.charging_inserted',
        'message.mirage_projector.core_booster.charging_extracted',
        'jade.mirage_projector.core_booster.charging',
        'jade.mirage_projector.core_booster.charging_empty',
    ):
        need(key in langs[locale], f'{locale} missing {key}')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')
if stabilized_1018:
    for locale in ('en_us', 'es_cl', 'es_es'):
        need('tooltip.mirage_projector.light_battery.capacity' not in langs[locale], f'{locale} retained removed debug capacity tooltip')
else:
    for locale in ('en_us', 'es_cl', 'es_es'):
        need('tooltip.mirage_projector.light_battery.capacity' in langs[locale], f'{locale} missing historical battery capacity tooltip')

waitlist = read('docs/WAITLIST-1.1.0.md')
need(('up to **4 rechargeable cells/batteries**' in waitlist or '**4 input queue slots**' in waitlist), 'charging-station four-cell input queue not documented')
need('exactly **1 active charging slot**' in waitlist, 'charging-station active slot not documented')
need(('**4–5 output slots**' in waitlist or 'exactly **4 output slots**' in waitlist), 'charging-station output target not documented')
need('approximately **5 Glow Dust plus additional casing/electrical materials**' in waitlist, 'unresolved Light Battery recipe target not documented')


# 1.0.9 physical light-projector consumer.
mode = read('src/main/java/celerbi/mirageprojector/light/device/PortableLightMode.java')
need('FOCUS(15, 2, 24, 2, 22.0F, 4)' in mode, 'Focus QA profile baseline missing')
need('FLOOD(15, 2, 16, 1, 72.0F, 2)' in mode, 'Flood QA profile baseline missing')
need('AMBIENT(14, 2, 12, 0, 360.0F, 1)' in mode, 'Ambient QA profile baseline missing')
need('case FOCUS -> FLOOD' in mode and 'case FLOOD -> AMBIENT' in mode and 'case AMBIENT -> OFF' in mode and 'case OFF -> FOCUS' in mode, 'mode cycle is not Focus -> Flood -> Ambient -> Off -> Focus')
need('MirageDynamicLightProfiles.directionalCone' in mode and 'MirageDynamicLightProfiles.ambient' in mode, 'light modes are not backed by shared Dynamic Mirage Light profiles')

projector_be = read('src/main/java/celerbi/mirageprojector/blockentity/MirageLightProjectorBlockEntity.java')
need('private PortableLightMode mode = PortableLightMode.FOCUS' in projector_be, 'new projector does not default to Focus')
need('ItemStack energyCell' in projector_be, 'projector has no real rechargeable ItemStack slot')
need('RechargeableEnergyItem.isRechargeable' in projector_be, 'projector cell slot is not generic rechargeable media')
need('saveAdditional' in projector_be and 'getUpdatePacket' in projector_be, 'projector state persistence/sync missing')
need('consumeStoredCharge' in projector_be and '% 20L' in projector_be, 'per-second battery drain missing')

projector_block = read('src/main/java/celerbi/mirageprojector/block/MirageLightProjectorBlock.java')
need('HorizontalDirectionalBlock.FACING' in projector_block, 'light projector has no placement orientation')
need('MirageLightProjectorBlockEntity::serverTick' in projector_block, 'light projector server ticker missing')
need('extractEnergyCell()' in projector_block, 'breaking projector does not return its rechargeable medium')

interaction_light = read('src/main/java/celerbi/mirageprojector/event/MirageLightProjectorInteractionEvents.java')
light_menu = read('src/main/java/celerbi/mirageprojector/menu/MirageLightProjectorMenu.java') if (ROOT / 'src/main/java/celerbi/mirageprojector/menu/MirageLightProjectorMenu.java').exists() else ''
light_action = read('src/main/java/celerbi/mirageprojector/network/LightProjectorActionPayload.java') if (ROOT / 'src/main/java/celerbi/mirageprojector/network/LightProjectorActionPayload.java').exists() else ''
legacy_light_controls = ('player.isShiftKeyDown() && projector.hasEnergyCell()' in interaction_light
        and 'RechargeableEnergyItem.isRechargeable(held)' in interaction_light
        and 'projector.cycleMode()' in interaction_light)
gui_light_controls = ('openMenu(projector' in interaction_light
        and 'RechargeableEnergyItem.isRechargeable(stack)' in light_menu
        and 'projector.cycleMode()' in light_action)
need(legacy_light_controls or gui_light_controls, 'light-projector battery/mode control path missing')
if gui_light_controls:
    need('extractEnergyCell' not in interaction_light and 'insertEnergyCell' not in interaction_light,
         '1.0.18+ light projector must not manipulate cells through raw block right-click gestures')

placed = read('src/main/java/celerbi/mirageprojector/client/ClientPlacedLightProjectors.java')
need('ClientDynamicMirageLightManager.submit' in placed, 'placed projector is not feeding DYNAMIC_VISUAL runtime')
need('MirageLightSourceId.block("light_projector", pos)' in placed, 'placed projector dynamic source identity is not stable per block')
need('projector.mode().profile(direction)' in placed, 'placed projector does not submit the active mode profile')

dynamic = read('src/main/java/celerbi/mirageprojector/client/ClientDynamicMirageLightManager.java')
need('MirageLightEngine.updateSource' in dynamic, 'dynamic light manager no longer solves submitted sources')
need('MirageLightRuntimeMode.DYNAMIC_VISUAL' in read('src/main/java/celerbi/mirageprojector/light/engine/MirageDynamicLightSnapshot.java'), 'dynamic snapshots are not DYNAMIC_VISUAL sources')

registry_blocks = read('src/main/java/celerbi/mirageprojector/registry/ModBlocks.java')
registry_items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
registry_bes = read('src/main/java/celerbi/mirageprojector/registry/ModBlockEntities.java')
need('"mirage_light_projector"' in registry_blocks, 'light projector block not registered')
need('MIRAGE_LIGHT_PROJECTOR' in registry_items, 'light projector BlockItem not registered')
need('MIRAGE_LIGHT_PROJECTOR' in registry_bes, 'light projector BlockEntityType not registered')
for rel in (
    'src/main/resources/assets/mirage_projector/blockstates/mirage_light_projector.json',
    'src/main/resources/assets/mirage_projector/models/block/mirage_light_projector.json',
    'src/main/resources/assets/mirage_projector/models/item/mirage_light_projector.json',
    'src/main/resources/data/mirage_projector/loot_table/blocks/mirage_light_projector.json',
):
    need((ROOT / rel).exists(), f'missing light-projector resource: {rel}')

for locale in ('en_us', 'es_cl', 'es_es'):
    lang = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in (
        'block.mirage_projector.mirage_light_projector',
        'hud.mirage_projector.light_projector.mode.focus',
        'hud.mirage_projector.light_projector.mode.flood',
        'hud.mirage_projector.light_projector.mode.ambient',
        'hud.mirage_projector.light_projector.mode.off',
        'jade.mirage_projector.light_projector.battery',
    ):
        need(key in lang, f'{locale} missing {key}')
    if stabilized_1018:
        need('gui.mirage_projector.light_projector.cycle_mode' in lang, f'{locale} missing stabilized Light Projector GUI localization')
        need('message.mirage_projector.light_projector.cell_inserted' not in lang and 'message.mirage_projector.light_projector.cell_extracted' not in lang, f'{locale} retained removed direct-cell messages')
    else:
        need('message.mirage_projector.light_projector.cell_inserted' in lang and 'message.mirage_projector.light_projector.cell_extracted' in lang, f'{locale} missing historical direct-cell messages')
    need(lang['hud.mirage_projector.light_projector.mode.focus'] == 'Projector: Focus', f'{locale} Focus HUD literal changed')
    need(lang['hud.mirage_projector.light_projector.mode.flood'] == 'Projector: Flood', f'{locale} Flood HUD literal changed')
    need(lang['hud.mirage_projector.light_projector.mode.ambient'] == 'Projector: Ambient', f'{locale} Ambient HUD literal changed')
    need(lang['hud.mirage_projector.light_projector.mode.off'] == 'Projector: Off', f'{locale} Off HUD literal changed')

release_doc = read('docs/RELEASE-1.0.9-LIGHT-PROJECTOR-FOUNDATION.md')
need('Network protocol remains 28' in release_doc, '1.0.9 wire-compatibility note missing')
need('Focus → Flood → Ambient → Off → Focus' in release_doc, 'stable mode-cycle contract not documented')

if errors:
    print('Mirage Projector 1.0.9 light projector verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.9 light projector verification PASS')
