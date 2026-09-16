#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors = []


def need(condition, message):
    if not condition:
        errors.append(message)


def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')


gradle = read('build.gradle')
props = read('gradle.properties').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
need('emi_version=1.1.24+1.21.1' in props, 'gradle.properties missing emi_version')
need('jei_version=19.25.0.321' in props, 'gradle.properties missing jei_version')
need('mod_version=0.1.0-dev.86a' in props, 'gradle.properties is not dev.86a')
need('https://repo.sleeping.town/' in gradle, 'build.gradle missing EMI repository')
need('https://maven.blamejared.com' in gradle, 'build.gradle missing JEI repository')
need('dev.emi:emi-neoforge:${emi_version}' in gradle, 'build.gradle missing EMI dependency')
need('mezz.jei:jei-1.21.1-neoforge-api:${jei_version}' in gradle, 'build.gradle missing JEI api dependency')
need('mezz.jei:jei-1.21.1-neoforge:${jei_version}' in gradle, 'build.gradle missing JEI runtime dependency')

for rel in [
    'src/main/java/celerbi/mirageprojector/compat/viewer/ViewerCompatData.java',
    'src/main/java/celerbi/mirageprojector/compat/emi/MirageEmiPlugin.java',
    'src/main/java/celerbi/mirageprojector/compat/emi/ProjectorUpgradeEmiRecipe.java',
    'src/main/java/celerbi/mirageprojector/compat/emi/CryingObsidianGrowthEmiRecipe.java',
    'src/main/java/celerbi/mirageprojector/compat/jei/MirageJeiPlugin.java',
    'src/main/java/celerbi/mirageprojector/compat/jei/ProjectorUpgradeCraftingExtension.java',
    'docs/DEV86-EMI-JEI-TAB-ORDER-POLISH.md',
    'docs/NEXT-CHAT-HANDOFF-dev86.md',
]:
    need((ROOT / rel).exists(), f'missing dev.86 file: {rel}')

emi_plugin = read('src/main/java/celerbi/mirageprojector/compat/emi/MirageEmiPlugin.java')
need('@EmiEntrypoint' in emi_plugin, 'EMI plugin entrypoint annotation missing')
need('new CryingObsidianGrowthEmiRecipe()' in emi_plugin, 'EMI plugin missing Crying Obsidian tutorial registration')
need('new ProjectorUpgradeEmiRecipe' in emi_plugin, 'EMI plugin missing projector upgrade registration')

emi_recipe = read('src/main/java/celerbi/mirageprojector/compat/emi/CryingObsidianGrowthEmiRecipe.java')
need('VanillaEmiRecipeCategories.WORLD_INTERACTION' in emi_recipe, 'EMI Crying Obsidian recipe is not world-interaction')
need('widgets.addTank' in emi_recipe, 'EMI Crying Obsidian recipe missing lava tank widgets')
need('emi.mirage_projector.crying_obsidian.step.base' in emi_recipe, 'EMI Crying Obsidian base-step tooltip missing')
need('emi.mirage_projector.crying_obsidian.stage.cluster' in emi_recipe, 'EMI Crying Obsidian cluster tooltip missing')
need('ModItems.CRYING_OBSIDIAN_SHARD' in emi_recipe, 'EMI Crying Obsidian recipe missing shard linkage')
need('emi.mirage_projector.crying_obsidian.silk_touch_result' in emi_recipe, 'EMI Crying Obsidian Silk Touch result tooltip missing')
need('emi.mirage_projector.crying_obsidian.regular_pickaxe' in emi_recipe, 'EMI Crying Obsidian regular-pickaxe tooltip missing')

for rel in [
    'src/main/resources/data/mirage_projector/loot_table/blocks/small_crying_obsidian_bud.json',
    'src/main/resources/data/mirage_projector/loot_table/blocks/medium_crying_obsidian_bud.json',
    'src/main/resources/data/mirage_projector/loot_table/blocks/large_crying_obsidian_bud.json',
    'src/main/resources/data/mirage_projector/loot_table/blocks/crying_obsidian_cluster.json',
]:
    loot = read(rel)
    need('silk_touch' in loot, f'{rel} missing Silk Touch behavior')
    need('crying_obsidian_shard' in loot, f'{rel} missing shard drop behavior')
    need('apply_bonus' not in loot and 'fortune' not in loot, f'{rel} unexpectedly applies Fortune to shard drops')

jei_plugin = read('src/main/java/celerbi/mirageprojector/compat/jei/MirageJeiPlugin.java')
need('@JeiPlugin' in jei_plugin, 'JEI plugin annotation missing')
need('addExtension(ProjectorUpgradeRecipe.class, new ProjectorUpgradeCraftingExtension())' in jei_plugin, 'JEI crafting extension missing')
need('jei.mirage_projector.info.crying_obsidian_growth.1' in jei_plugin, 'JEI Crying Obsidian info missing')
need('jei.mirage_projector.info.projector_upgrades.1' in jei_plugin, 'JEI projector info missing')

langs = [
    ROOT / 'src/main/resources/assets/mirage_projector/lang/en_us.json',
    ROOT / 'src/main/resources/assets/mirage_projector/lang/es_cl.json',
]
keys = [
    'emi.mirage_projector.crying_obsidian.step.base',
    'emi.mirage_projector.crying_obsidian.step.lava',
    'emi.mirage_projector.crying_obsidian.step.start',
    'emi.mirage_projector.crying_obsidian.stage.medium',
    'emi.mirage_projector.crying_obsidian.stage.large',
    'emi.mirage_projector.crying_obsidian.stage.cluster',
    'emi.mirage_projector.crying_obsidian.silk_touch_pickaxe',
    'emi.mirage_projector.crying_obsidian.silk_touch_result',
    'emi.mirage_projector.crying_obsidian.regular_pickaxe',
    'emi.mirage_projector.crying_obsidian.shard_drop',
    'jei.mirage_projector.info.crying_obsidian_growth.1',
    'jei.mirage_projector.info.crying_obsidian_growth.6',
    'jei.mirage_projector.info.crying_obsidian_growth.7',
    'jei.mirage_projector.info.projector_upgrades.1',
]
for path in langs:
    data = json.loads(path.read_text(encoding='utf-8'))
    for key in keys:
        need(key in data, f'missing lang key {key} in {path.name}')

if errors:
    print('dev.86a recipe-viewer verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print('dev.86a recipe-viewer verification PASS')
