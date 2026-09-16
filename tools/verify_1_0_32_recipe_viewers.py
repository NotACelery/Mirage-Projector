#!/usr/bin/env python3
"""Verify 1.0.32 Survival recipes remain natively discoverable in JEI and EMI."""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors = []


def need(cond, msg):
    if not cond:
        errors.append(msg)


def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')


# These recipes intentionally stay vanilla-shaped. JEI and EMI both consume the vanilla
# RecipeManager crafting surface automatically; explicit wrappers are reserved for Mirage's
# custom ProjectorUpgradeRecipe serializer and the two EMI Crying Obsidian overrides.
recipes = {
    'light_battery': 'mirage_projector:light_battery',
    'mirage_flashlight': 'mirage_projector:mirage_lantern',  # stable legacy registry ID
    'mirage_light_projector': 'mirage_projector:mirage_light_projector',
    'mirage_wall_projector': 'mirage_projector:mirage_wall_illuminator',
    'shoulder_strap': 'mirage_projector:arm_strap',  # stable legacy registry ID
    'auto_battery_swap_patch': 'mirage_projector:auto_battery_swap_patch',
    'shoulder_strap_slot_expansion': 'mirage_projector:battery_pouch_expansion_patch',
    'charging_station': 'mirage_projector:charging_station',
    'mirage_hand_projector': 'mirage_projector:mirage_hand_projector',
    'scan_codex': 'mirage_projector:scan_codex',
    'mirage_table_projector': 'mirage_projector:mirage_table_projector',
    'mirage_wall_display': 'mirage_projector:mirage_wall_projector',  # stable legacy registry ID
}

for recipe_id, expected_output in recipes.items():
    rel = f'src/main/resources/data/mirage_projector/recipe/{recipe_id}.json'
    path = ROOT / rel
    need(path.exists(), f'missing viewer-visible crafting recipe: {recipe_id}')
    if not path.exists():
        continue
    try:
        data = json.loads(path.read_text(encoding='utf-8'))
    except Exception as exc:
        need(False, f'invalid recipe JSON {recipe_id}: {exc}')
        continue
    need(data.get('type') == 'minecraft:crafting_shaped', f'{recipe_id} must remain vanilla crafting_shaped for native JEI/EMI discovery')
    need(data.get('result', {}).get('id') == expected_output, f'{recipe_id} output drifted: expected {expected_output}')
    pattern = data.get('pattern', [])
    need(len(pattern) == 3 and all(isinstance(row, str) and len(row) == 3 for row in pattern), f'{recipe_id} is not a complete 3x3 viewer grid')

battery = json.loads(read('src/main/resources/data/mirage_projector/recipe/light_battery.json'))
need(battery.get('pattern') == ['GCG', 'IGI', 'GRG'], 'Light Battery viewer pattern must remain GCG / IGI / GRG')
need(battery.get('key', {}).get('G', {}).get('tag') == 'mirage_projector:glow_dust_media', 'Light Battery G slots must use glow_dust_media tag')
media = json.loads(read('src/main/resources/data/mirage_projector/tags/item/glow_dust_media.json'))
need('minecraft:glowstone_dust' in media.get('values', []), 'viewer Glow Dust tag missing vanilla Glowstone Dust')
need('mirage_projector:glow_dust' in media.get('values', []), 'viewer Glow Dust tag missing rechargeable Mirage Glow Dust')
need(not (ROOT / 'src/main/resources/data/mirage_projector/recipe/glow_dust.json').exists(), 'Glow Dust must not gain a separate Survival/viewer recipe')

# Optional viewer integrations must still be present, while the normal Survival recipes must not
# be suppressed/replaced by EMI custom wrappers. Their vanilla RecipeManager rows are canonical.
gradle = read('build.gradle')
need('dev.emi:emi-neoforge:${emi_version}' in gradle, 'optional EMI integration dependency missing')
need('mezz.jei:jei-1.21.1-neoforge-api:${jei_version}' in gradle, 'optional JEI API dependency missing')
need('mezz.jei:jei-1.21.1-neoforge:${jei_version}' in gradle, 'optional JEI runtime dependency missing')

jei = read('src/main/java/celerbi/mirageprojector/compat/jei/MirageJeiPlugin.java')
emi = read('src/main/java/celerbi/mirageprojector/compat/emi/MirageEmiPlugin.java')
need('@JeiPlugin' in jei, 'JEI plugin entrypoint missing')
need('@EmiEntrypoint' in emi, 'EMI plugin entrypoint missing')
need('addExtension(ProjectorUpgradeRecipe.class, new ProjectorUpgradeCraftingExtension())' in jei, 'JEI custom projector extension missing')
need('registerProjectorCrafting(registry)' in emi, 'EMI custom projector registration missing')

for recipe_id in recipes:
    need(f'"{recipe_id}"' not in emi and f'"mirage_projector:{recipe_id}"' not in emi,
         f'EMI plugin unexpectedly intercepts vanilla Survival recipe {recipe_id}; this would risk duplicate/missing Crafting rows')

# Legacy output IDs must render the current public names in viewers.
expected_public_names = {
    'item.mirage_projector.mirage_flashlight': 'Mirage Flashlight',
    'block.mirage_projector.mirage_wall_projector': 'Mirage Wall Display',
    'block.mirage_projector.mirage_wall_illuminator': 'Mirage Wall Projector',
}
for locale in ('en_us', 'es_cl', 'es_es'):
    lang = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in expected_public_names:
        need(key in lang, f'{locale} missing viewer-facing translation {key}')

for rel in ('README.md', 'docs/CURRENT-IMPLEMENTATION.md', 'docs/QA-REGRESSION.md', 'docs/RELEASE-1.0.32-SURVIVAL-PROGRESSION.md'):
    text = read(rel)
    need('JEI' in text and 'EMI' in text, f'{rel} does not document 1.0.32 recipe-viewer coverage')

if errors:
    print('Mirage Projector 1.0.32 JEI/EMI Survival recipe verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print(f'Mirage Projector 1.0.32 JEI/EMI Survival recipe verification PASS ({len(recipes)} native Crafting entries)')
