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

props = read('gradle.properties').replace('mod_version=1.0.34', 'mod_version=1.0.30').replace('mod_version=1.0.33', 'mod_version=1.0.30').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
need('mod_version=0.1.0-dev.86a' in props, 'gradle.properties is not dev.86a')

emi_plugin = read('src/main/java/celerbi/mirageprojector/compat/emi/MirageEmiPlugin.java')
need('projectorEmiRecipeId(path)' in emi_plugin, 'projector EMI wrappers do not use synthetic IDs')
need('registry.removeRecipes(projectorDataRecipeId(path))' in emi_plugin, 'projector auto recipe suppression missing')
need('new CryingObsidianCraftingEmiRecipe' in emi_plugin, 'explicit Crying Obsidian EMI crafting wrappers missing')
need('Items.FIRE_CHARGE' in emi_plugin and 'Items.MAGMA_CREAM' in emi_plugin, 'Crying Obsidian EMI crafting variants incomplete')
need('orderCrystalBlockDrops(registry)' in emi_plugin, 'crystal block-drop ordering hook missing')
need('emi/ordered_block_drops/0' in emi_plugin, 'ordered block-drop synthetic IDs missing')

for rel in [
    'src/main/java/celerbi/mirageprojector/compat/emi/CryingObsidianCraftingEmiRecipe.java',
    'src/main/java/celerbi/mirageprojector/compat/emi/OrderedCrystalDropEmiRecipe.java',
]:
    need((ROOT / rel).exists(), f'missing dev.86 EMI support file: {rel}')

world = read('src/main/java/celerbi/mirageprojector/compat/emi/CryingObsidianGrowthEmiRecipe.java')
need('private static void addSmallArrow' in world, 'World Interaction compact-arrow helper missing')
need('12, 9' in world, 'World Interaction arrows are not rescaled to 12x9')
for pos in ['40, 55', '82, 55', '124, 55']:
    need(f'addSmallArrow(widgets, {pos})' in world, f'growth arrow missing or misplaced at {pos}')

# Both vanilla JSON recipes must still exist for JEI/vanilla recipe-manager discovery.
for rel, catalyst in [
    ('src/main/resources/data/mirage_projector/recipe/crying_obsidian_from_shards_fire_charge.json', 'minecraft:fire_charge'),
    ('src/main/resources/data/mirage_projector/recipe/crying_obsidian_from_shards_magma_cream.json', 'minecraft:magma_cream'),
]:
    data = json.loads(read(rel))
    need(data.get('type') == 'minecraft:crafting_shaped', f'{rel} is not shaped crafting')
    need(data.get('result', {}).get('id') == 'minecraft:crying_obsidian', f'{rel} does not output Crying Obsidian')
    need(any(v.get('item') == catalyst for v in data.get('key', {}).values()), f'{rel} missing expected catalyst {catalyst}')

# JEI must keep the custom crafting extension; its built-in crafting type reads RecipeManager automatically.
jei = read('src/main/java/celerbi/mirageprojector/compat/jei/MirageJeiPlugin.java')
need('addExtension(ProjectorUpgradeRecipe.class, new ProjectorUpgradeCraftingExtension())' in jei, 'JEI projector crafting extension missing')
upgrade = read('src/main/java/celerbi/mirageprojector/recipe/ProjectorUpgradeRecipe.java')
need('return RecipeType.CRAFTING;' in upgrade, 'ProjectorUpgradeRecipe is not exposed as vanilla crafting type')

# Registration order is already chronological and should remain so for viewer/data consumers.
blocks = read('src/main/java/celerbi/mirageprojector/registry/ModBlocks.java')
positions = [blocks.index(name) for name in [
    'SMALL_CRYING_OBSIDIAN_BUD',
    'MEDIUM_CRYING_OBSIDIAN_BUD',
    'LARGE_CRYING_OBSIDIAN_BUD',
    'CRYING_OBSIDIAN_CLUSTER',
]]
need(positions == sorted(positions), 'crystal block registration order is not small -> medium -> large -> cluster')

if errors:
    print('dev.86a recipe-viewer tab/order verification FAILED')
    for e in errors:
        print(' -', e)
    raise SystemExit(1)

print('dev.86a recipe-viewer tab/order verification PASS')
