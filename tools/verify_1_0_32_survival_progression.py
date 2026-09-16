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

def load(rel):
    return json.loads(read(rel))

props = read('gradle.properties')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
settings = read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')
need('mod_version=1.0.32' in props, 'mod_version is not 1.0.32')
need(any(v in main for v in ('NETWORK_PROTOCOL = "42"', 'NETWORK_PROTOCOL = "43"')), '1.0.32 protocol is not a compatible 42+ line')
need('SERIALIZATION_VERSION = 4' in settings, '1.0.32 must retain ProjectionSettings format 4')

# Flashlight public/runtime rename while persistent identities remain stable.
items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
flash = read('src/main/java/celerbi/mirageprojector/item/MirageFlashlightItem.java')
runtime = read('src/main/java/celerbi/mirageprojector/client/ClientRuntimeEvents.java')
need('MIRAGE_FLASHLIGHT' in items and 'ITEMS.register("mirage_lantern"' in items,
     'Flashlight Java identity or legacy mirage_lantern registry identity missing')
need('new MirageFlashlightItem(new Item.Properties().stacksTo(1))' in items,
     'Flashlight does not use MirageFlashlightItem as non-stackable device')
need(not (ROOT / 'src/main/java/celerbi/mirageprojector/item/MirageLanternItem.java').exists(),
     'obsolete MirageLanternItem source still exists')
need((ROOT / 'src/main/java/celerbi/mirageprojector/client/ClientHeldFlashlights.java').exists(),
     'ClientHeldFlashlights source missing')
need('ClientHeldFlashlights.submitVisiblePlayers' in runtime and 'ClientHeldFlashlights.resetSession' in runtime,
     'renamed held-Flashlight runtime is not wired')
need('CYCLE_FLASHLIGHT_MODE' in read('src/main/java/celerbi/mirageprojector/network/PortableDeviceActionPayload.java'),
     'portable action identity still uses Lantern terminology')
for key in ('MirageLanternMode', 'MirageLanternCell', 'MirageLanternCellPresent', 'MirageLanternCellPercent'):
    need(key in flash, f'legacy persistent Flashlight NBT compatibility key missing: {key}')
need('return "item.mirage_projector.mirage_flashlight";' in flash,
     'Flashlight public description identity missing')
need('ModBlocks.MIRAGE_FLASHLIGHT_BEACON' in flash and 'useOn(UseOnContext context)' in flash,
     'temporary Flashlight placement path missing')
need('placed.setMode(mode(flashlight));' in flash and 'placed.setEnergyCell(cell);' in flash,
     'Flashlight placement does not transfer mode/cell')

# Presentation Wall is now a Display; new true Wall Projector is illumination.
blocks = read('src/main/java/celerbi/mirageprojector/registry/ModBlocks.java')
block_items = items
be_registry = read('src/main/java/celerbi/mirageprojector/registry/ModBlockEntities.java')
need('MIRAGE_WALL_DISPLAY' in blocks and 'MirageWallDisplayBlock' in blocks
     and '"mirage_wall_projector"' in blocks,
     'legacy presentation chassis is not preserved as Mirage Wall Display')
need('MIRAGE_WALL_DISPLAY' in block_items and 'registerSimpleBlockItem("mirage_wall_projector"' in block_items,
     'Wall Display legacy BlockItem registry identity missing')
need((ROOT / 'src/main/java/celerbi/mirageprojector/block/MirageWallDisplayBlock.java').exists(),
     'MirageWallDisplayBlock source missing')
wall = read('src/main/java/celerbi/mirageprojector/block/MirageWallProjectorBlock.java')
need('BLOCKS.register(' in blocks and '"mirage_wall_illuminator"' in blocks and 'MIRAGE_WALL_PROJECTOR' in blocks,
     'true Mirage Wall Projector is not registered')
need('class MirageWallProjectorBlock extends BaseEntityBlock' in wall,
     'true wall illumination block source missing/incompatible')
need('clicked.getAxis().isHorizontal()' in wall and 'isFaceSturdy' in wall and 'getOpposite()' in wall,
     'Wall Projector does not enforce sturdy vertical-wall mounting')
need('ModBlocks.MIRAGE_WALL_PROJECTOR.get()' in be_registry,
     'Wall Projector does not reuse placed-light BlockEntity type')

# Temporary placed Flashlight is a block-only transient chassis and round-trips its device state.
beacon = read('src/main/java/celerbi/mirageprojector/block/MirageFlashlightBeaconBlock.java')
need('MIRAGE_FLASHLIGHT_BEACON' in blocks and '"mirage_flashlight_beacon"' in blocks,
     'placed Flashlight block is not registered')
need('MIRAGE_FLASHLIGHT_BEACON' not in block_items,
     'placed Flashlight must not have a normal BlockItem')
need('MirageFlashlightItem.setMode(flashlight, placed.mode());' in beacon,
     'placed Flashlight does not restore selected mode')
need('placed.extractEnergyCell()' in beacon and 'MirageFlashlightItem.replaceEnergyCell' in beacon,
     'placed Flashlight does not restore exact rechargeable cell')
need('Block.popResource(level, pos, flashlight);' in beacon,
     'placed Flashlight does not return one handheld device on break/support loss')
need('ModBlocks.MIRAGE_FLASHLIGHT_BEACON.get()' in be_registry,
     'placed Flashlight does not reuse placed-light BlockEntity type')

# Frozen Survival recipes.
expected_recipes = (
    'light_battery.json', 'mirage_flashlight.json', 'mirage_light_projector.json',
    'mirage_wall_projector.json', 'shoulder_strap.json', 'auto_battery_swap_patch.json',
    'shoulder_strap_slot_expansion.json', 'charging_station.json',
    'mirage_hand_projector.json', 'scan_codex.json', 'mirage_table_projector.json',
    'mirage_wall_display.json',
)
for name in expected_recipes:
    path = ROOT / 'src/main/resources/data/mirage_projector/recipe' / name
    need(path.exists(), f'1.0.32 Survival recipe missing: {name}')
    if path.exists():
        try:
            json.loads(path.read_text(encoding='utf-8'))
        except Exception as exc:
            need(False, f'invalid recipe JSON {name}: {exc}')
need(not (ROOT / 'src/main/resources/data/mirage_projector/recipe/glow_dust.json').exists(),
     'Glow Dust must intentionally have no crafting recipe')

battery = load('src/main/resources/data/mirage_projector/recipe/light_battery.json')
need(battery.get('pattern') == ['GCG', 'IGI', 'GRG'],
     'Light Battery pattern is not the frozen GCG/IGI/GRG layout')
need(battery.get('key', {}).get('G', {}).get('tag') == 'mirage_projector:glow_dust_media',
     'Light Battery X does not use shared Glow Dust media tag')
need(battery.get('key', {}).get('C', {}).get('item') == 'minecraft:copper_ingot'
     and battery.get('key', {}).get('I', {}).get('item') == 'minecraft:iron_ingot'
     and battery.get('key', {}).get('R', {}).get('item') == 'minecraft:redstone',
     'Light Battery casing/electrical ingredients drifted')
media = load('src/main/resources/data/mirage_projector/tags/item/glow_dust_media.json')
need(set(media.get('values', [])) == {'minecraft:glowstone_dust', 'mirage_projector:glow_dust'},
     'Glow Dust media tag must contain vanilla full dust + Mirage rechargeable dust')
craft_hook = read('src/main/java/celerbi/mirageprojector/event/RechargeableCraftingEvents.java')
need('5 * GlowDustItem.MAX_CHARGE' in craft_hook and 'averageFraction' in craft_hook,
     'five-media Light Battery average-charge inheritance missing')

# Visual identity gates.
flash_model = read('src/main/resources/assets/mirage_projector/models/item/mirage_lantern.json')
need('"elements"' in flash_model and 'minecraft:block/crying_obsidian' in flash_model
     and 'minecraft:block/magenta_stained_glass' in flash_model,
     'Flashlight model is not the Crying-Obsidian/magenta-glass 3D design')
light_model = read('src/main/resources/assets/mirage_projector/models/block/mirage_light_projector.json')
need('minecraft:block/iron_block' in light_model and 'minecraft:block/crying_obsidian' in light_model
     and 'minecraft:block/magenta_stained_glass' in light_model,
     'floor Light Projector model lacks iron + Crying Obsidian + magenta visual language')
for rel in (
    'src/main/resources/assets/mirage_projector/blockstates/mirage_wall_illuminator.json',
    'src/main/resources/assets/mirage_projector/models/block/mirage_wall_illuminator.json',
    'src/main/resources/assets/mirage_projector/models/item/mirage_wall_illuminator.json',
    'src/main/resources/data/mirage_projector/loot_table/blocks/mirage_wall_illuminator.json',
    'src/main/resources/assets/mirage_projector/blockstates/mirage_flashlight_beacon.json',
    'src/main/resources/assets/mirage_projector/models/block/mirage_flashlight_beacon.json',
    'src/main/resources/data/mirage_projector/loot_table/blocks/mirage_flashlight_beacon.json',
):
    need((ROOT / rel).exists(), f'1.0.32 resource missing: {rel}')

# Locale/public naming and parity.
langs = {}
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = load(f'src/main/resources/assets/mirage_projector/lang/{locale}.json')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language-key parity broken')
need(langs['en_us'].get('item.mirage_projector.mirage_flashlight') == 'Mirage Flashlight',
     'English public Flashlight name missing')
need(langs['en_us'].get('block.mirage_projector.mirage_wall_projector') == 'Mirage Wall Display',
     'legacy presentation chassis is not publicly Mirage Wall Display')
need(langs['en_us'].get('block.mirage_projector.mirage_wall_illuminator') == 'Mirage Wall Projector',
     'new illumination chassis is not publicly Mirage Wall Projector')
for locale in langs:
    need('item.mirage_projector.mirage_lantern' not in langs[locale],
         f'{locale} retained obsolete public Mirage Lantern translation key')

# Current authority/waitlist must show exactly what was delivered vs deliberately deferred.
waitlist = read('docs/WAITLIST-1.1.0.md')
need('delivered in 1.0.32' in waitlist.lower() and 'Mirage Wall Projector' in waitlist
     and 'placed temporarily' in waitlist.lower(),
     '1.1.0 waitlist does not record 1.0.32 physical/progression delivery')
need('yaw' in waitlist.lower() and 'pitch' in waitlist.lower(),
     'floor Light Projector yaw/pitch follow-up was accidentally removed from waitlist')
current = read('docs/CURRENT-IMPLEMENTATION.md')
need('Version: **1.0.32**' in current and '## 1.0.32 Survival progression' in current,
     'current implementation authority is not on 1.0.32')
need((ROOT / 'docs/RELEASE-1.0.32-SURVIVAL-PROGRESSION.md').exists(),
     '1.0.32 release note missing')

if errors:
    print('Mirage Projector 1.0.32 Survival progression verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print(f'Mirage Projector 1.0.32 Survival progression verification PASS ({len(langs["en_us"])} lang keys)')
