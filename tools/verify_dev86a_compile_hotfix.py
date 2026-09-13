#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
path = ROOT / 'src/main/java/celerbi/mirageprojector/compat/emi/CryingObsidianCraftingEmiRecipe.java'
text = path.read_text(encoding='utf-8')
errors = []

if 'import net.minecraft.world.level.ItemLike;' not in text:
    errors.append('CryingObsidianCraftingEmiRecipe must import net.minecraft.world.level.ItemLike')
if 'import net.minecraft.world.item.ItemLike;' in text:
    errors.append('legacy/wrong net.minecraft.world.item.ItemLike import still present')

if errors:
    print('dev.86a compile-hotfix verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print('dev.86a compile-hotfix verification PASS')
