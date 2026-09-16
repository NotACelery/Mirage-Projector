#!/usr/bin/env python3
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'src/main/java'
RES = ROOT / 'src/main/resources'
TEMPLATES = ROOT / 'src/main/templates'
DOCS = ROOT / 'docs'
MODELS = RES / 'assets/mirage_projector/models'
LANG_DIR = RES / 'assets/mirage_projector/lang'
errors: list[str] = []
notes: list[str] = []


def need(condition: bool, message: str) -> None:
    if not condition:
        errors.append(message)


def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding='utf-8')


def load_json_no_duplicates(path: Path):
    duplicates: list[str] = []

    def hook(pairs):
        result = {}
        for key, value in pairs:
            if key in result:
                duplicates.append(key)
            result[key] = value
        return result

    try:
        data = json.loads(path.read_text(encoding='utf-8'), object_pairs_hook=hook)
    except Exception as exc:  # noqa: BLE001
        errors.append(f'invalid JSON {path.relative_to(ROOT)}: {exc}')
        return None
    for key in duplicates:
        errors.append(f'duplicate JSON key {key!r} in {path.relative_to(ROOT)}')
    return data


# Release metadata / platform baseline.
props = read('gradle.properties').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
need('mod_version=1.0.9' in props, 'gradle.properties is not 1.0.9')
need('minecraft_version=1.21.1' in props, 'Minecraft baseline changed')
need('neo_version=21.1.244' in props, 'NeoForge baseline changed')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
need('NETWORK_PROTOCOL = "28"' in main, 'network protocol is not 28')
mods_toml = read('src/main/templates/META-INF/neoforge.mods.toml')
need('version="${mod_version}"' in mods_toml, 'NeoForge metadata is not wired to mod_version')
need('logoFile="logo.png"' in mods_toml, 'NeoForge metadata logo missing')
need('logoBlur=false' in mods_toml, 'NeoForge metadata logo blur setting missing')
need((RES / 'logo.png').exists(), 'release logo.png missing')
need('Want holograms in your Minecraft builds?' in mods_toml, 'NeoForge public description is not using the player-facing release overview')

# Public artifact naming keeps both Minecraft and mod versions visible.
gradle = read('build.gradle')
need('archivesName = "${mod_id}-${minecraft_version}"' in gradle, 'release jar base name does not include Minecraft version')

# Optional viewer dependencies remain optional to core runtime.
need('compileOnly "dev.emi:emi-neoforge:${emi_version}"' in gradle, 'EMI compileOnly integration missing')
need('compileOnly "mezz.jei:jei-1.21.1-neoforge-api:${jei_version}"' in gradle, 'JEI compileOnly integration missing')
need('localRuntime "dev.emi:emi-neoforge:${emi_version}"' in gradle, 'EMI localRuntime QA dependency missing')
need('localRuntime "mezz.jei:jei-1.21.1-neoforge:${jei_version}"' in gradle, 'JEI localRuntime QA dependency missing')
need('modId="emi"' not in mods_toml and 'modId="jei"' not in mods_toml, 'EMI/JEI accidentally became required NeoForge dependencies')

# JSON syntax, duplicate keys and resource references.
json_files = list(RES.rglob('*.json'))
json_data: dict[Path, object] = {}
for path in json_files:
    data = load_json_no_duplicates(path)
    if data is not None:
        json_data[path] = data

for path in (RES / 'assets/mirage_projector/blockstates').glob('*.json'):
    data = json_data.get(path)
    stack = [data]
    while stack:
        obj = stack.pop()
        if isinstance(obj, dict):
            for key, value in obj.items():
                if key == 'model' and isinstance(value, str) and value.startswith('mirage_projector:block/'):
                    target = MODELS / 'block' / (value.rsplit('/', 1)[-1] + '.json')
                    need(target.exists(), f'missing block model referenced by {path.name}: {value}')
                stack.append(value)
        elif isinstance(obj, list):
            stack.extend(obj)

for path in (MODELS / 'item').glob('*.json'):
    data = json_data.get(path)
    if not isinstance(data, dict):
        continue
    parent = data.get('parent')
    if isinstance(parent, str) and parent.startswith('mirage_projector:block/'):
        target = MODELS / 'block' / (parent.rsplit('/', 1)[-1] + '.json')
        need(target.exists(), f'missing item parent model {parent} for {path.name}')

# Registry/resource coverage.
blocks_text = read('src/main/java/celerbi/mirageprojector/registry/ModBlocks.java')
items_text = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
block_ids = set(re.findall(r'registerProjector\("([a-z0-9_]+)"', blocks_text))
block_ids |= set(re.findall(r'registerLegacyImprovedCore\("([a-z0-9_]+)"', blocks_text))
block_ids |= set(re.findall(r'registerCrystal\("([a-z0-9_]+)"', blocks_text))
block_ids |= set(re.findall(r'BLOCKS\.register\(\s*"([a-z0-9_]+)"', blocks_text))
item_ids = set(re.findall(r'ITEMS\.registerSimpleBlockItem\("([a-z0-9_]+)"', items_text))
item_ids |= set(re.findall(r'ITEMS\.registerSimpleItem\("([a-z0-9_]+)"', items_text))
item_ids |= set(re.findall(r'ITEMS\.register\("([a-z0-9_]+)"', items_text))
need(len(block_ids) == 19, f'expected 19 registered blocks, got {len(block_ids)}')
need(len(item_ids) == 18, f'expected 18 registered items, got {len(item_ids)}')
for block_id in block_ids:
    need((RES / f'assets/mirage_projector/blockstates/{block_id}.json').exists(), f'missing blockstate: {block_id}')
    need((MODELS / f'block/{block_id}.json').exists(), f'missing block model: {block_id}')
for item_id in item_ids:
    need((MODELS / f'item/{item_id}.json').exists(), f'missing item model: {item_id}')

# Recipes reference known Mirage IDs.
known_ids = block_ids | item_ids
recipe_dir = RES / 'data/mirage_projector/recipe'
expected_recipes = {
    'mirage_projector.json',
    'mirage_display_upgrade.json',
    'wide_mirage_projector_upgrade.json',
    'tall_mirage_projector_upgrade.json',
    'mirage_prism_upgrade.json',
    'mirage_field_projector_upgrade.json',
    'crying_obsidian_from_shards_fire_charge.json',
    'crying_obsidian_from_shards_magma_cream.json',
    'crying_obsidian_shard_from_stonecutting.json',
}
need(expected_recipes <= {p.name for p in recipe_dir.glob('*.json')}, 'one or more required release recipes are missing')
for recipe in recipe_dir.glob('*.json'):
    data = json_data.get(recipe)
    stack = [data]
    while stack:
        obj = stack.pop()
        if isinstance(obj, dict):
            for key, value in obj.items():
                if key in {'id', 'item'} and isinstance(value, str) and value.startswith('mirage_projector:'):
                    need(value.split(':', 1)[1] in known_ids, f'recipe {recipe.name} references unknown ID {value}')
                stack.append(value)
        elif isinstance(obj, list):
            stack.extend(obj)

# Canonical projector surface and final visual fixes.
canonical = [
    'mirage_projector',
    'mirage_display',
    'mirage_field_projector',
    'wide_mirage_projector',
    'tall_mirage_projector',
    'mirage_prism',
]
for name in canonical:
    need((MODELS / f'block/{name}.json').exists(), f'canonical projector model missing: {name}')
    need((MODELS / f'item/{name}.json').exists(), f'canonical projector item model missing: {name}')
    need((RES / f'assets/mirage_projector/blockstates/{name}.json').exists(), f'canonical projector blockstate missing: {name}')
    need((RES / f'data/mirage_projector/loot_table/blocks/{name}.json').exists(), f'canonical projector loot table missing: {name}')

compact = json_data.get(MODELS / 'block/mirage_projector.json')
if isinstance(compact, dict):
    emitter = compact.get('children', {}).get('emitter', {}).get('elements', [])
    need(len(emitter) == 0, 'Compact projector emitter geometry regression')
    front_lip = None
    for element in compact.get('children', {}).get('frame', {}).get('elements', []):
        if element.get('from') == [5, 2, 3] and element.get('to') == [11, 3, 5]:
            front_lip = element
            break
    need(front_lip is not None and 'up' not in front_lip.get('faces', {}), 'Compact front-lip Z-fighting fix regressed')

tall = json_data.get(MODELS / 'block/tall_mirage_projector.json')
if isinstance(tall, dict):
    emitter = tall.get('children', {}).get('emitter', {}).get('elements', [])
    boxes = {(tuple(e.get('from', [])), tuple(e.get('to', []))) for e in emitter}
    need(((6, 2, 2), (10, 4, 4)) in boxes and ((6, 2, 12), (10, 4, 14)) in boxes,
         'Tall front/rear low glass symmetry regressed')

# Runtime/source hygiene.
for path in SRC.rglob('*.java'):
    text = path.read_text(encoding='utf-8')
    rel = path.relative_to(ROOT)
    need('\t' not in text, f'tab indentation in {rel}')
    need(not re.search(r'[ \t]+$', text, re.MULTILINE), f'trailing whitespace in {rel}')
    need(re.search(r'^import\s+[^;]*\.\*;', text, re.MULTILINE) is None, f'wildcard import in {rel}')
    need(re.search(r'\b(?:TODO|FIXME|HACK|XXX)\b', text) is None, f'release marker remains in {rel}')
    need(re.search(r'\bdev\.[0-9]', text, re.IGNORECASE) is None, f'development-version comment remains in {rel}')
    need('_alt' not in text.lower(), f'removed comparison-projector reference remains in {rel}')
    need(text.endswith('\n'), f'missing final newline in {rel}')

# Runtime resources should not retain removed comparison projector IDs.
for path in RES.rglob('*'):
    if not path.is_file() or path.suffix not in {'.json', '.mcmeta'}:
        continue
    text = path.read_text(encoding='utf-8')
    need('_alt' not in text.lower(), f'removed comparison-projector resource reference remains: {path.relative_to(ROOT)}')
    need(text.endswith('\n'), f'missing final newline in {path.relative_to(ROOT)}')

# Release UI must not expose internal chassis-debug controls.
projector_screen = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
need('debugButton' not in projector_screen, 'internal Debug button is exposed in the release projector screen')
need('gui.mirage_projector.debug' not in projector_screen, 'release projector screen still references the Debug control')

# Language parity / public handbook cleanup.
langs = {}
for lang in ('en_us', 'es_cl', 'es_es'):
    path = LANG_DIR / f'{lang}.json'
    data = json_data.get(path)
    need(isinstance(data, dict), f'language file missing or invalid: {lang}')
    if isinstance(data, dict):
        langs[lang] = data
if 'en_us' in langs:
    base_keys = set(langs['en_us'])
    for lang, data in langs.items():
        need(set(data) == base_keys, f'language key parity mismatch in {lang}')
        need(not any(k.startswith('handbook.mirage_projector.page.') for k in data), f'obsolete handbook page keys remain in {lang}')
        need(data.get('item.mirage_projector.debug_handbook') in {'Mirage Handbook', 'Manual de Mirage'}, f'public handbook name not release-facing in {lang}')
        need('gui.mirage_projector.debug' not in data, f'internal Debug GUI localization remains public in {lang}')
        for key, value in data.items():
            if isinstance(value, str):
                need(re.search(r'\bdev\.[0-9]', value, re.IGNORECASE) is None, f'development-version wording remains in {lang}:{key}')

# Optional EMI/JEI release contracts.
emi_plugin = read('src/main/java/celerbi/mirageprojector/compat/emi/MirageEmiPlugin.java')
need('@EmiEntrypoint' in emi_plugin, 'EMI plugin entrypoint missing')
need('new ProjectorUpgradeEmiRecipe' in emi_plugin, 'EMI projector upgrade wrappers missing')
need('new CryingObsidianGrowthEmiRecipe()' in emi_plugin, 'EMI Crying Obsidian world-interaction entry missing')
need('new CryingObsidianCraftingEmiRecipe' in emi_plugin, 'EMI Crying Obsidian crafting wrappers missing')
need('orderCrystalBlockDrops(registry)' in emi_plugin, 'EMI age-ordered crystal Block Drops hook missing')
world_interaction = read('src/main/java/celerbi/mirageprojector/compat/emi/CryingObsidianGrowthEmiRecipe.java')
need('12, 9' in world_interaction, 'EMI compact arrow dimensions regressed')
need('silk_touch_result' in world_interaction and 'regular_pickaxe' in world_interaction, 'EMI mining examples incomplete')
jei_plugin = read('src/main/java/celerbi/mirageprojector/compat/jei/MirageJeiPlugin.java')
need('@JeiPlugin' in jei_plugin, 'JEI plugin entrypoint missing')
need('addExtension(ProjectorUpgradeRecipe.class, new ProjectorUpgradeCraftingExtension())' in jei_plugin,
     'JEI projector crafting extension missing')

# Crystal loot: Silk Touch preserves; normal drops shards; Fortune does not modify amount.
for name in ('small_crying_obsidian_bud', 'medium_crying_obsidian_bud', 'large_crying_obsidian_bud', 'crying_obsidian_cluster'):
    rel = f'data/mirage_projector/loot_table/blocks/{name}.json'
    text = read(f'src/main/resources/{rel}')
    need('silk_touch' in text, f'{name} loot is missing Silk Touch behavior')
    need('crying_obsidian_shard' in text, f'{name} loot is missing shard behavior')
    need('apply_bonus' not in text and 'fortune' not in text.lower(), f'{name} loot unexpectedly applies Fortune')

# Projection source/transform/energy architecture remains in place.
settings = read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')
need('ResourceLocation id' in settings, 'projection source IDs are no longer namespaced ResourceLocations')
need('legacyOrdinal' in settings, 'legacy projection-source migration helper missing')
need((SRC / 'celerbi/mirageprojector/ProjectionSourceRegistry.java').exists(), 'ProjectionSourceRegistry missing')
need((SRC / 'celerbi/mirageprojector/client/ProjectionSourceRenderRegistry.java').exists(), 'ProjectionSourceRenderRegistry missing')
need((SRC / 'celerbi/mirageprojector/ProjectionTransform.java').exists(), 'ProjectionTransform missing')
need((SRC / 'celerbi/mirageprojector/ProjectionEnergySource.java').exists(), 'ProjectionEnergySource missing')

# 1.0.7 fixed-tab placement foundation.
need('SERIALIZATION_VERSION = 3' in settings, 'ProjectionSettings serialization is not v3')
need('horizontalOffsetPixels' in settings, 'legacy v3 horizontal compatibility slot missing')
need('legacy v3 wire/NBT slot; always sanitized to zero' in settings, 'legacy horizontal compatibility semantics undocumented')
need('withPlacement(int prismDistancePixels, float tiltDegrees)' in settings, 'placement helper still exposes a second vertical axis')
need('MAX_TILT_DEGREES = 90' in settings, 'Tilt is not full ±90 degrees')
transform = read('src/main/java/celerbi/mirageprojector/ProjectionTransform.java')
need('horizontalOffsetPixels' not in transform, 'ProjectionTransform still exposes horizontal translation')
need('verticalOffsetPixels' not in transform, 'ProjectionTransform still exposes a second vertical translation axis')
need('positive values migrate into Lift, then zero' in settings, 'legacy Vertical Offset migration contract undocumented')
need('Mth.clamp(liftPixels + Math.max(0, verticalOffsetPixels)' in settings, 'positive legacy Vertical Offset is not absorbed into Lift')
need('orientationFromTiltDegrees' in transform and 'tiltDegrees(' in transform, 'quaternion Tilt helpers missing')
spacing = read('src/main/java/celerbi/mirageprojector/PrismProjectionSpacing.java')
need('minimumDistancePixels' in spacing and 'effectiveDistancePixels' in spacing, 'Prism collision-safe spacing helper missing')
need('minimumExtraDistancePixels' in spacing and 'effectiveExtraDistancePixels' in spacing and 'absoluteDistancePixelsFromExtra' in spacing, 'Prism user-facing extra-distance mapping missing')
need('MAX_EXTRA_DISTANCE_PIXELS = 160' in spacing and 'Math.min(a.widthPixels(), b.widthPixels()) * 0.5D' in spacing, 'Prism +0 baseline/cap is not edge-tight')
need('DISTANCE_PIXELS_PER_PU = 64' in spacing and 'powerCost' in spacing, 'Prism spacing PU rule missing')
screen = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
need('horizontalOffsetSlider' not in screen and 'offset_horizontal' not in screen, 'Horizontal Offset remains exposed in release UI')
need('prismSpacingAvailable()' in screen and 'PrismProjectionSpacing.minimumExtraDistancePixels' in screen, 'Prism-only extra Distance UI missing')
need('imageHeight = 412;' in screen, 'main projector panel is not compact enough for 1080p GUI Scale 2')
need('enum SettingsTab' in screen and 'SettingsTab.GEOMETRY' in screen and 'SettingsTab.PLACEMENT' in screen and 'SettingsTab.ROTATION' in screen and 'SettingsTab.FLOATING' in screen and 'SettingsTab.APPEARANCE' not in screen, 'four fixed settings tabs missing')
need('verticalOffsetSlider' not in screen and 'offset_vertical' not in screen, 'deprecated Vertical Offset remains exposed in UI')
need('refreshSettingsTabVisibility' in screen, 'tab visibility controller missing')
renderer = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
need('applyPrismCarouselPlacement' in renderer, 'Prism carousel placement helper missing')
need('PrismProjectionSpacing.effectiveDistancePixels(settings) * PIXEL' in renderer, 'Prism renderer does not use collision-safe radial Distance')
need('projectionWorldOffset' not in renderer, 'legacy displaced-center renderer helper remains')
clearance = read('src/main/java/celerbi/mirageprojector/client/ProjectionClearance.java')
need('PrismProjectionSpacing.effectiveDistancePixels(s) * PIXEL' in clearance, 'clearance does not include Prism radial Distance')
need('horizontalOffsetPixels()' not in clearance and 'verticalOffsetPixels()' not in clearance, 'clearance still applies deprecated independent translation')
power = read('src/main/java/celerbi/mirageprojector/ProjectionPower.java')
need('PrismProjectionSpacing.powerCost(safe)' in power, 'Prism radial Distance has no PU cost')

# Mirage Light release invariants.
field = read('src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java')
client = read('src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java')
network = read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')
world = read('src/main/java/celerbi/mirageprojector/light/engine/MirageLightWorld.java')
need('WATCHDOG_HEARTBEAT_TICKS = 20' in field, 'Mirage light watchdog heartbeat changed')
need('verifyActiveSources' in field, 'Mirage light source watchdog missing')
need('MirageLightChunkRevisionManifestPayload.TYPE' in network, 'Mirage light revision manifest payload missing')
need('MirageLightChunkSnapshotPayload.TYPE' in network, 'Mirage light chunk snapshot payload missing')
need('RequestMirageLightChunkPayload.TYPE' in network, 'Mirage light chunk request payload missing')
need('authoritativeSectionKeysForChunk' in client, 'client authoritative Mirage mirror missing')
need('REQUEST_RETRY_TICKS = 40L' in client, 'Mirage snapshot retry timing changed')
need('STATIC_WORLD' in world, 'STATIC_WORLD backend missing')

# Active documentation is stable-release-facing; historical dev material is archived.
active_docs = list(DOCS.glob('*.md'))
required_docs = {
    'CURRENT-IMPLEMENTATION.md', 'ARCHITECTURE.md', 'DOCUMENTATION-AUTHORITY.md', 'ROADMAP.md',
    'QA-REGRESSION.md', 'REGISTRY-INVENTORY.md', 'MIRAGE-LIGHT-ENGINE.md', 'DEVELOPMENT.md', 'CHANGELOG.md'
}
need(required_docs <= {p.name for p in active_docs}, 'one or more active release documents are missing')
for path in active_docs:
    text = path.read_text(encoding='utf-8')
    need(text.endswith('\n'), f'missing final newline in active doc {path.name}')
    need(not re.search(r'[ \t]+$', text, re.MULTILINE), f'trailing whitespace in active doc {path.name}')
    if path.name != 'CHANGELOG.md':
        need(re.search(r'\b0\.1\.0-dev\.', text) is None, f'development version remains in active doc {path.name}')
        need(re.search(r'\bdev\.[0-9]', text, re.IGNORECASE) is None, f'development chronology remains in active doc {path.name}')
need((DOCS / 'history/development-notes').is_dir(), 'development-note history archive missing')
need((DOCS / 'history/handoffs').is_dir(), 'handoff history archive missing')
need(not any(re.match(r'DEV\d', p.name) for p in DOCS.glob('*.md')), 'DEVxx files remain in active docs root')
need(not any(DOCS.glob('NEXT-CHAT-HANDOFF*.md')), 'handoff files remain in active docs root')
need((DOCS / 'history/release-1.0.0/WAITLIST-1.0.0-COMPLETED.md').exists(), 'completed 1.0.0 waitlist archive missing')

# Release source tree must not include generated heavyweight runtime/build data.
for forbidden in ('.gradle-dist', 'build', 'run', 'out', 'bin'):
    need(not (ROOT / forbidden).exists(), f'generated heavyweight path present in release source: {forbidden}')
cleanup = read('CLEAN-MIRAGE-PROJECTOR.bat')
need('--from-build' in cleanup, 'cleanup/build chaining contract missing')

# Formatting expectations for release-facing text.
release_text_roots = [SRC, RES, TEMPLATES, ROOT / 'tools']
for base in release_text_roots:
    if not base.exists():
        continue
    for path in base.rglob('*'):
        if not path.is_file() or path.suffix not in {'.java', '.json', '.py', '.toml', '.mcmeta'}:
            continue
        try:
            text = path.read_text(encoding='utf-8')
        except UnicodeDecodeError:
            continue
        need(text.endswith('\n'), f'missing final newline: {path.relative_to(ROOT)}')
        need(not re.search(r'[ \t]+$', text, re.MULTILINE), f'trailing whitespace: {path.relative_to(ROOT)}')
        if path.suffix in {'.java', '.json', '.py', '.toml'}:
            need('\t' not in text, f'tab character in release-facing text: {path.relative_to(ROOT)}')

# Non-blocking deprecation note for existing NeoForge annotations.
dep_sites = [str(p.relative_to(ROOT)) for p in SRC.rglob('*.java') if 'EventBusSubscriber.Bus.' in p.read_text(encoding='utf-8')]
if dep_sites:
    notes.append(f'known non-blocking EventBusSubscriber.Bus deprecation sites: {len(dep_sites)}')

if errors:
    print('Mirage Projector 1.0.9 release audit FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print(f'Mirage Projector 1.0.9 release audit PASS ({len(json_files)} JSON, {len(block_ids)} blocks, {len(item_ids)} items, {len(langs.get("en_us", {}))} lang keys)')
for note in notes:
    print('NOTE', note)
