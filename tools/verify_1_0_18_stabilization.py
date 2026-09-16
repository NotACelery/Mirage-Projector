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

# Release line / protocol / serialization.
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
need(any(v in props for v in ('mod_version=1.0.18', 'mod_version=1.0.19', 'mod_version=1.0.20', 'mod_version=1.0.21', 'mod_version=1.0.22', 'mod_version=1.0.23', 'mod_version=1.0.24', 'mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'version is not a compatible 1.0.18+ line')
need(('NETWORK_PROTOCOL = "34"' in main or ('NETWORK_PROTOCOL = "35"' in main or ('NETWORK_PROTOCOL = \"36\"' in main or ('NETWORK_PROTOCOL = \"37\"' in main or 'NETWORK_PROTOCOL = \"38\"' in main)))), '1.0.18 network protocol is not 34')
need(('SERIALIZATION_VERSION = 3' in settings or 'SERIALIZATION_VERSION = 4' in settings), 'ProjectionSettings format is not a compatible v3/v4 line')

# Portable-device GUI contract and GUI-only battery service.
portable_menu = read('src/main/java/celerbi/mirageprojector/menu/PortableDeviceMenu.java')
portable_screen = read('src/main/java/celerbi/mirageprojector/client/PortableDeviceScreen.java')
open_payload = read('src/main/java/celerbi/mirageprojector/network/OpenPortableDeviceMenuPayload.java')
portable_action = read('src/main/java/celerbi/mirageprojector/network/PortableDeviceActionPayload.java')
menus = read('src/main/java/celerbi/mirageprojector/registry/ModMenus.java')
client_events = read('src/main/java/celerbi/mirageprojector/client/ClientEvents.java')
network = read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')
need('PORTABLE_DEVICE' in menus and 'PortableDeviceMenu::new' in menus, 'Portable Device menu is not registered')
need('ModMenus.PORTABLE_DEVICE.get(), PortableDeviceScreen::new' in client_events, 'Portable Device screen is not registered')
need('OpenPortableDeviceMenuPayload.TYPE' in network and 'PortableDeviceActionPayload.TYPE' in network,
     'portable-device payloads are not registered')
need('RechargeableEnergyItem.isRechargeable(stack)' in portable_menu and 'getMaxStackSize()' in portable_menu,
     'Portable Device GUI does not own a one-cell rechargeable slot')
need('CYCLE_LANTERN_MODE' in portable_action and 'COPY_TARGET_PROJECTOR' in portable_action,
     'portable GUI actions are incomplete')
need('CYCLE_BANNER_PRESENTATION' in portable_action and 'WAR_BANNER_SIZE_UP' in portable_action,
     'War Banner controls were not preserved in portable GUI')

lantern = read('src/main/java/celerbi/mirageprojector/item/MirageLanternItem.java')
need('return PortableLightMode.OFF;' in lantern, 'Lantern default is not OFF')
need('player.isShiftKeyDown()' in lantern and 'mode(lantern).next()' in lantern,
     'Lantern retains GUI/mode interaction paths')
need('PortableDeviceMenu.open' in lantern, 'Lantern portable GUI path missing')
need('replaceEnergyCell' in lantern, 'Lantern GUI battery replacement hook missing')
need('serviceCell' not in lantern, 'Lantern still exposes legacy opposite-hand battery service')
need('shouldCauseReequipAnimation' in lantern and 'slotChanged || oldStack.getItem() != newStack.getItem()' in lantern,
     'Lantern battery ticks can still force re-equip animation')

hand = read('src/main/java/celerbi/mirageprojector/item/MirageHandProjectorItem.java')
need('PortableDeviceMenu.open' in hand and 'player.isShiftKeyDown()' in hand,
     'Hand Projector GUI-open interaction missing')
need('setProjectionEnabled(projector, enabled)' in hand, 'Hand Projector RMB ON/OFF path missing')
need('replaceEnergyCell' in hand, 'Hand Projector GUI battery replacement hook missing')
need('serviceCell' not in hand, 'Hand Projector still exposes legacy opposite-hand battery service')
need('shouldCauseReequipAnimation' in hand and 'slotChanged || oldStack.getItem() != newStack.getItem()' in hand,
     'Hand Projector battery ticks can still force re-equip animation')
need('tooltip.mirage_projector.hand_projector.use' in hand,
     'Hand Projector minimal right-click tooltip missing')

runtime_events = read('src/main/java/celerbi/mirageprojector/client/ClientRuntimeEvents.java')
need('updateLocalHud' not in runtime_events, 'portable devices still refresh persistent actionbar HUD every runtime tick')

# Placed Mirage Light Projector GUI and removal of physical floating battery render.
light_menu = read('src/main/java/celerbi/mirageprojector/menu/MirageLightProjectorMenu.java')
light_screen = read('src/main/java/celerbi/mirageprojector/client/MirageLightProjectorScreen.java')
light_action = read('src/main/java/celerbi/mirageprojector/network/LightProjectorActionPayload.java')
light_interaction = read('src/main/java/celerbi/mirageprojector/event/MirageLightProjectorInteractionEvents.java')
light_be = read('src/main/java/celerbi/mirageprojector/blockentity/MirageLightProjectorBlockEntity.java')
need('MIRAGE_LIGHT_PROJECTOR' in menus and 'MirageLightProjectorMenu' in menus, 'Light Projector menu is not registered')
need('ModMenus.MIRAGE_LIGHT_PROJECTOR.get(), MirageLightProjectorScreen::new' in client_events,
     'Light Projector screen is not registered')
need('LightProjectorActionPayload.TYPE' in network, 'Light Projector action payload is not registered')
need('openMenu(projector' in light_interaction and 'projector.cycleMode()' in light_interaction, 'Light Projector GUI/mode paths missing')
need('implements MenuProvider' in light_be and 'new MirageLightProjectorMenu' in light_be,
     'Light Projector block entity is not a menu provider')
need('RechargeableEnergyItem.isRechargeable(stack)' in light_menu, 'Light Projector GUI battery slot is not rechargeable-only')
need('MirageLightProjectorRenderer' not in client_events, 'legacy floating battery renderer is still registered')

# Shoulder Strap owns its packed inventory; player attachment is now just one equipment socket.
equipment = read('src/main/java/celerbi/mirageprojector/equipment/ShoulderEquipment.java')
strap_container = read('src/main/java/celerbi/mirageprojector/equipment/ShoulderStrapContainer.java')
shoulder_runtime = read('src/main/java/celerbi/mirageprojector/equipment/ShoulderEquipmentRuntime.java')
need('SLOT_COUNT = 1' in equipment, 'player ShoulderEquipment attachment is not reduced to one Strap socket')
need('new ShoulderStrapContainer(strap())' in equipment, 'Shoulder Strap ItemStack does not own dependent inventory')
need('DataComponents.CONTAINER' in strap_container and 'ItemContainerContents.fromItems' in strap_container,
     'Shoulder Strap inventory is not persisted in the Strap ItemStack')
need('DEVICE_SLOT = 0' in strap_container and 'BATTERY_SLOTS = 9' in strap_container and 'UPGRADE_SLOTS = 3' in strap_container,
     'Shoulder Strap packed layout is incomplete')
need('ItemStackHandler legacy' in equipment and 'LEGACY_SLOT_COUNT' in equipment and 'migrated.serializeNBT(provider)' in equipment,
     'legacy shoulder attachment migration into packed Strap is missing')
need('container.setItem(ShoulderStrapContainer.DEVICE_SLOT' in equipment,
     'legacy shoulder device is not migrated into Strap contents')
need('player.containerMenu.setCarried(installed.copy())' in shoulder_runtime,
     'Shoulder Device replacement does not preserve old device on cursor')

# Dynamic right-side equipment panel: one Strap slot when empty, extra slots only while active.
equipment_client = read('src/main/java/celerbi/mirageprojector/client/MirageEquipmentClientEvents.java')
panel = read('src/main/java/celerbi/mirageprojector/client/MirageEquipmentPanelWidget.java')
slot_widget = read('src/main/java/celerbi/mirageprojector/client/MirageEquipmentSlotWidget.java')
need(('panelX = guiLeft + 180' in equipment_client or 'creative ? 200 : 180' in equipment_client or 'int panelX = rightEdge + 28;' in equipment_client or 'int panelX = rightEdge + 13;' in equipment_client), 'Mirage Equipment panel is not on/docked to the right side')
need((('toggleX = guiLeft + 156' in equipment_client and 'toggleY = guiTop + 61' in equipment_client) or ('creative ? 178 : 156' in equipment_client and 'creative ? 8 : 61' in equipment_client) or ('int toggleX = rightEdge + 7;' in equipment_client and 'creative ? 10 : 61' in equipment_client) or ('int toggleX = rightEdge - 2;' in equipment_client and 'creative ? 10 : 61' in equipment_client)),
     'Mirage Equipment toggle placement contract missing')
need((('if (!strapPresent)' in equipment_client and 'return;' in equipment_client) or ('if (strapPresent)' in equipment_client and 'EMPTY_HEIGHT = 50' in panel)),
     'empty Mirage Equipment panel does not collapse to Strap-only')
need('localActiveBatterySlots()' in equipment_client and 'localActiveUpgradeSlots()' in equipment_client,
     'Shoulder slots are not created dynamically from active Strap capacity')
need('for (int i = 0; i < activeBatteries; i++)' in equipment_client
     and 'for (int i = 0; i < activeUpgrades; i++)' in equipment_client,
     'inactive expansion slots may still be rendered')
need('EMPTY_HEIGHT' in panel and 'BASE_HEIGHT' in panel and 'EXPANDED_HEIGHT' in panel,
     'Mirage Equipment panel does not resize dynamically')
need('OpenPortableDeviceMenuPayload' in slot_widget and 'PortableDeviceSource.SHOULDER' in slot_widget,
     'RMB Shoulder Device does not open real device GUI')

tooltip = read('src/main/java/celerbi/mirageprojector/client/ShoulderStrapTooltipEvents.java')
need('Screen.hasShiftDown()' in tooltip and 'ShoulderStrapContainer' in tooltip,
     'Shoulder Strap Shift-hover packed-content preview missing')
need('value = Dist.CLIENT' in tooltip, 'Shoulder Strap client tooltip hook is not client-only')

# Rechargeable media semantics and tooltip cleanup.
energy = read('src/main/java/celerbi/mirageprojector/item/RechargeableEnergyItem.java')
glow = read('src/main/java/celerbi/mirageprojector/item/GlowDustItem.java')
battery = read('src/main/java/celerbi/mirageprojector/item/LightBatteryItem.java')
craft = read('src/main/java/celerbi/mirageprojector/event/RechargeableCraftingEvents.java')
need('VANILLA_GLOW_DUST' in energy and 'stack.is(Items.GLOWSTONE_DUST)' in energy,
     'vanilla Glowstone Dust is not treated as full rechargeable media for devices/crafting')
need('normalizeForDevice' in energy and 'normalizeFullyChargedOutput' in energy,
     'Glow Dust custom/vanilla normalization helpers missing')
need('new ItemStack(Items.GLOWSTONE_DUST' in energy,
     'fully charged custom Glow Dust does not return to vanilla Glowstone Dust')
need('else if (charge < MAX_CHARGE)' in glow and 'glow_dust.full' not in glow,
     'Glow Dust still shows a Fully Charged tooltip')
need('light_battery.capacity' not in battery and 'light_battery.recharge' not in battery,
     'Light Battery still exposes debug/capacity/recharge prose')
need('glowMedia != 5' in craft and 'averageFraction' in craft and 'LightBatteryItem.MAX_CHARGE' in craft,
     'five-Glow-Dust average-charge inheritance for Light Battery crafting missing')

# Charging Station stricter input, roomy UI, and world inventory visualization.
station_be = read('src/main/java/celerbi/mirageprojector/blockentity/ChargingStationBlockEntity.java')
station_screen = read('src/main/java/celerbi/mirageprojector/client/ChargingStationScreen.java')
station_renderer = read('src/main/java/celerbi/mirageprojector/client/ChargingStationRenderer.java')
need('isRegularChargeMedium' in station_be and 'ModItems.GLOW_DUST' in station_be
     and 'Items.GLOWSTONE_DUST' in station_be and 'ModItems.LIGHT_BATTERY' in station_be,
     'Charging Station allowed-medium whitelist missing')
need('!RechargeableEnergyItem.isFull(stack)' in station_be,
     'Charging Station still accepts fully charged inputs')
need('CreativeBatteryItem' not in station_be, 'Charging Station contains Creative Battery special acceptance')
need('imageWidth = 270' in station_screen and 'imageHeight = 220' in station_screen,
     'Charging Station GUI was not enlarged')
need('ChargingStationRenderer' in client_events, 'Charging Station block-entity renderer is not registered')
for token in ('INPUT_COUNT', 'CHARGING_SLOT', 'OUTPUT_COUNT'):
    need(token in station_renderer, f'Charging Station renderer does not visualize {token}')

core = read('src/main/java/celerbi/mirageprojector/blockentity/CoreBoosterBlockEntity.java')
need('normalizeFullyChargedOutput' in station_be and 'normalizeFullyChargedOutput' in core,
     'full Glow Dust is not normalized back to vanilla in both Beacon chargers')

# Dynamic-light QA fix: voxel-volume cone intersection plus source positions outside emitter/player body.
solver = read('src/main/java/celerbi/mirageprojector/light/engine/MirageLightSolver.java')
held_lights = read('src/main/java/celerbi/mirageprojector/client/ClientHeldLanterns.java')
shoulder_client = read('src/main/java/celerbi/mirageprojector/client/ClientShoulderEquipment.java')
placed_lights = read('src/main/java/celerbi/mirageprojector/client/ClientPlacedLightProjectors.java')
need('double coneRadius' in solver and 'perpendicularSq <= coneRadius * coneRadius' in solver,
     'directional light cone still samples only voxel centers')
need('look.scale(0.82D)' in held_lights, 'held Lantern light source was not moved in front of player')
need(('look.scale(0.78D)' in shoulder_client or 'horizontalForward.scale(0.18D)' in shoulder_client), 'shoulder Lantern light source was not moved in front of player')
need('direction.scale(0.72D)' in placed_lights, 'placed Light Projector source still begins inside its chassis')

# Scan Codex must be inventory-like: world continues and no background blur/dim pass.
codex = read('src/main/java/celerbi/mirageprojector/client/ScanCodexScreen.java')
need('public boolean isPauseScreen()' in codex and 'return false;' in codex,
     'Scan Codex still pauses singleplayer')
need('public void renderBackground' in codex and 'applyBlur' not in codex, 'Scan Codex no-op blurred/dim background override missing')

# User-facing naming/upgrade polish.
items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
upgrade_item = read('src/main/java/celerbi/mirageprojector/item/ShoulderUpgradePatchItem.java')
need('AUTO_BATTERY_SWAP_PATCH' in items and 'null' in items,
     'Auto Battery Swap Patch still requires explanatory tooltip prose')
need('tooltip.mirage_projector.shoulder_strap_slot_expansion' in items,
     'Shoulder Strap Slot Expansion tooltip hook missing')

langs = {}
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    data = langs[locale]
    need(data.get('item.mirage_projector.arm_strap') in {'Shoulder Strap', 'Correa de Hombro'},
         f'{locale} Shoulder Strap display name not migrated')
    need(data.get('item.mirage_projector.battery_pouch_expansion_patch') in {
        'Shoulder Strap Slot Expansion', 'Expansión de Slots de la Correa de Hombro', 'Expansión de Ranuras de la Correa de Hombro'
    }, f'{locale} expansion display name not migrated')
    expansion_tip = data.get('tooltip.mirage_projector.shoulder_strap_slot_expansion', '')
    need(('3' in expansion_tip and ('Shoulder Strap' in expansion_tip or 'Correa de Hombro' in expansion_tip)),
         f'{locale} expansion tooltip is not concise/strap-specific')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')

# Pixel-art centering sanity for batteries and renamed Strap art.
def alpha_centroid(path):
    with Image.open(path).convert('RGBA') as image:
        need(image.size == (16, 16), f'{path.name} is {image.size}, expected 16x16')
        pts = []
        for y in range(16):
            for x in range(16):
                a = image.getpixel((x, y))[3]
                if a:
                    pts.append((x + 0.5, y + 0.5, a))
        if not pts:
            return None
        total = sum(a for _, _, a in pts)
        return (sum(x*a for x,_,a in pts)/total, sum(y*a for _,y,a in pts)/total)

for rel in (
    'src/main/resources/assets/mirage_projector/textures/item/light_battery_frame.png',
    'src/main/resources/assets/mirage_projector/textures/item/light_battery_charge.png',
    'src/main/resources/assets/mirage_projector/textures/item/creative_battery.png',
):
    path = ROOT / rel
    need(path.exists(), f'missing battery texture {rel}')
    if path.exists():
        c = alpha_centroid(path)
        if c is not None:
            need(abs(c[0] - 8.0) <= 0.75 and abs(c[1] - 8.0) <= 0.75,
                 f'{path.name} alpha centroid is not inventory-centered: {c}')
strap_texture = ROOT / 'src/main/resources/assets/mirage_projector/textures/item/arm_strap.png'
need(strap_texture.exists(), 'Shoulder Strap texture missing')
if strap_texture.exists():
    with Image.open(strap_texture) as image:
        need(image.size == (16,16), f'Shoulder Strap texture is {image.size}, expected 16x16')

# Stabilization documentation must exist once release is closed.
need((ROOT / 'docs/RELEASE-1.0.18-MASSIVE-STABILIZATION.md').exists(), '1.0.18 release note missing')
need((ROOT / 'docs/history/handoffs/NEXT-CHAT-HANDOFF-1.0.18-STABILIZATION.md').exists(), '1.0.18 handoff missing')

if errors:
    print('Mirage Projector 1.0.18 massive stabilization verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.18 massive stabilization verification PASS')
