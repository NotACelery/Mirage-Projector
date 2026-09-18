#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def need(condition, message):
    if not condition:
        errors.append(message)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

props = read('gradle.properties').replace('mod_version=1.0.34', 'mod_version=1.0.30').replace('mod_version=1.0.33', 'mod_version=1.0.30').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"40\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"40\"')
recipe = read('src/main/java/celerbi/mirageprojector/recipe/ScanCardClearingRecipe.java')
serializer = read('src/main/java/celerbi/mirageprojector/recipe/ScanCardClearingRecipeSerializer.java')
recipe_json = read('src/main/resources/data/mirage_projector/recipe/clear_entity_scan_card.json')

need(any(v in props for v in ('mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'version is not a compatible 1.0.28+ line')
need(any(v in main for v in ('NETWORK_PROTOCOL = "38"', 'NETWORK_PROTOCOL = "39"', 'NETWORK_PROTOCOL = "40"')), 'custom payload protocol is not a compatible 38+ line')
need('public static final ScanCardClearingRecipe INSTANCE = new ScanCardClearingRecipe();' in recipe,
     'ScanCardClearingRecipe canonical singleton is missing')
need('private ScanCardClearingRecipe()' in recipe,
     'ScanCardClearingRecipe constructor is not restricted to the canonical instance')
need('MapCodec.unit(() -> ScanCardClearingRecipe.INSTANCE)' in serializer,
     'JSON codec does not decode to the canonical ScanCardClearingRecipe instance')
need('StreamCodec.unit(ScanCardClearingRecipe.INSTANCE)' in serializer,
     'network codec does not encode the canonical ScanCardClearingRecipe instance')
need('MapCodec.unit(ScanCardClearingRecipe::new)' not in serializer,
     'old fresh-instance JSON codec remains present')
need('StreamCodec.unit(new ScanCardClearingRecipe())' not in serializer,
     'old mismatched unit stream instance remains present')
need('"type": "mirage_projector:scan_card_clearing"' in recipe_json,
     'filled-card clearing recipe no longer uses the custom serializer')

# StreamCodec.unit in Minecraft 1.21.1 performs an equals check during encode.
# A stateless recipe must therefore be decoded and streamed as the same/equal value.
for p in (ROOT / 'src/main/java/celerbi/mirageprojector/recipe').glob('*Serializer.java'):
    text = p.read_text(encoding='utf-8')
    need('StreamCodec.unit(new ' not in text,
         f'{p.name} constructs a separate StreamCodec.unit value and may fail recipe sync equality')

need((ROOT / 'docs/RELEASE-1.0.28-RECIPE-SYNC-HOTFIX.md').exists(), '1.0.28 release note missing')
need('clientbound/minecraft:update_recipes' in read('docs/RELEASE-1.0.28-RECIPE-SYNC-HOTFIX.md'),
     'release note does not document the runtime packet failure')

if errors:
    print('Mirage Projector 1.0.28 recipe-sync verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print('Mirage Projector 1.0.28 recipe-sync verification PASS')
