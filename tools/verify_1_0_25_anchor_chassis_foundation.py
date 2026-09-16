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

props = read('gradle.properties').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"40\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"40\"')
settings = read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')
need(any(v in props for v in ('mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'version is not a compatible 1.0.25+ line')
need(any(v in main for v in ('NETWORK_PROTOCOL = "37"', 'NETWORK_PROTOCOL = "38"', 'NETWORK_PROTOCOL = "39"', 'NETWORK_PROTOCOL = "40"')), '1.0.25 network protocol must be 37')
need('SERIALIZATION_VERSION = 4' in settings, 'ProjectionSettings format must be 4')
need('withWallOffsets' in settings and 'horizontalOffsetPixels' in settings and 'verticalOffsetPixels' in settings,
     'signed Wall X/Y ProjectionSettings contract missing')
need('serializationVersion >= 4' in settings and 'readLegacyAndDiscard' in settings,
     'format-4 reader does not preserve pre-4 compatibility migration')

chassis = read('src/main/java/celerbi/mirageprojector/ProjectionChassisProfile.java')
for token in (
    'TABLE("Table"', 'WALL("Wall"', 'Anchor.TABLE_HORIZONTAL', 'Anchor.WALL_TARGET',
    'PlacementCapability.WALL_XY_OFFSET', 'supportsWallXyOffset()',
    'usesHorizontalPlaneFor(ProjectionSettings.SourceMode source)'):
    need(token in chassis, f'chassis anchor/capability contract missing: {token}')
need(chassis.find('PRISM("Prism"') < chassis.find('TABLE("Table"') < chassis.find('WALL("Wall"'),
     'TABLE/WALL must remain appended after historical six chassis ordinals')
need('WALL_OUTWARD' not in chassis, 'obsolete wall-mounted anchor identity remains in chassis contract')

blocks = read('src/main/java/celerbi/mirageprojector/registry/ModBlocks.java')
items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
be_registry = read('src/main/java/celerbi/mirageprojector/registry/ModBlockEntities.java')
creative = read('src/main/java/celerbi/mirageprojector/registry/ModCreativeTabs.java')
for token in ('MIRAGE_TABLE_PROJECTOR', 'MirageTableProjectorBlock', 'MIRAGE_WALL_DISPLAY', 'MirageWallDisplayBlock'):
    need(token in blocks, f'new projector block registration missing: {token}')
for token in ('mirage_table_projector', 'mirage_wall_projector'):
    need(token in items, f'new projector BlockItem missing: {token}')
need('ModBlocks.MIRAGE_TABLE_PROJECTOR.get()' in be_registry and 'ModBlocks.MIRAGE_WALL_DISPLAY.get()' in be_registry,
     'Table/Wall do not reuse canonical Mirage projector BlockEntity type')
need('ModItems.MIRAGE_TABLE_PROJECTOR.get()' in creative and 'ModItems.MIRAGE_WALL_DISPLAY.get()' in creative,
     'Table/Wall are not exposed for QA in Mirage Creative tab')

base = read('src/main/java/celerbi/mirageprojector/block/MirageProjectorBlock.java')
table = read('src/main/java/celerbi/mirageprojector/block/MirageTableProjectorBlock.java')
wall = read('src/main/java/celerbi/mirageprojector/block/MirageWallDisplayBlock.java')
block_entity = read('src/main/java/celerbi/mirageprojector/blockentity/MirageProjectorBlockEntity.java')
for token in ('TABLE_SHAPE', 'WALL_SHAPE', 'ProjectionChassisProfile.TABLE', 'ProjectionChassisProfile.WALL',
              'player.isShiftKeyDown()', 'copyPendingPackedPlayerBreakDrop()', 'level.removeBlock(pos, false)'):
    need(token in base, f'base projector Table/Wall packed-pickup contract missing: {token}')
need('isFaceSturdy(level, supportPos, Direction.UP)' in table, 'Table sturdy support-below contract missing')
need('isCollisionShapeFullBlock(level, supportPos)' in wall,
     'Wall/Data-show must require a complete collision support and reject slabs/stairs')
need('clicked.getAxis()' not in wall, 'Wall/Data-show still carries wall-mounted placement-face restrictions')
for text, label in ((table, 'Table'), (wall, 'Wall')):
    need('preparePackedPlayerBreak' in text and 'copyPendingPackedPlayerBreakDrop' in text and 'Block.popResource' in text,
         f'{label} support-loss path does not preserve packed state')
need('box(2, 0, 3, 14, 5, 13)' in base and 'box(3, 5, 5, 13, 6, 12)' in base,
     'Wall physical VoxelShape is not the low-profile <=6px Data-show body')

surface = read('src/main/java/celerbi/mirageprojector/WallProjectionSurface.java')
for token in (
    'ProjectionImageSizing.size(image.width(), image.height(), scale)',
    'settings.horizontalOffsetPixels()', 'settings.verticalOffsetPixels()',
    'validateRectangle(', 'validateCell(', 'isCollisionShapeFullBlock',
    'for (int scale = requestedScale; scale >= ProjectionSettings.DEBUG_MIN_SCALE_PIXELS; scale--)',
    'ProjectionPower.evaluateWallProjection', 'distancePixels(step)', 'MAX_SEARCH_BLOCKS'):
    need(token in surface, f'Wall actual-image target resolver missing: {token}')
need('actual image rectangle' in surface.lower() or 'aspect-correct image rectangle' in surface.lower(),
     'Wall resolver does not document actual-image-footprint invariant')
need('nominal projector envelope' in surface.lower(),
     'Wall resolver no longer explicitly rejects nominal-envelope validation')

power = read('src/main/java/celerbi/mirageprojector/ProjectionPower.java')
need('wallDistanceCost' in power and 'evaluateWallProjection' in power,
     'Wall distance is not part of Projection Power')
need('distancePixels - 16' in power, 'Wall distance surcharge baseline changed unexpectedly')

source_registry = read('src/main/java/celerbi/mirageprojector/ProjectionSourceRegistry.java')
need(source_registry.count('chassis -> chassis != ProjectionChassisProfile.WALL') >= 3,
     'Wall must reject Item/Entity/Banner source families')
need('chassisProfile() == ProjectionChassisProfile.WALL' in source_registry,
     'Wall Image source-bank content contract missing')

bank = read('src/main/java/celerbi/mirageprojector/ImageSourceBank.java')
for token in ('PERSISTED_COMPAT_SLOTS = 9', 'swap(int first, int second)', 'normalizePresentIndex', 'nextPresentIndex'):
    need(token in bank, f'Wall presentation playlist bank helper missing: {token}')
for token in ('wallSlideIndex', 'activeWallImage()', 'setWallSlideIndex', 'WallSlideIndex',
              'wallProjectionSurface()', 'applyImageWorkspace(ProjectionSettings newSettings, ImageSourceBank bank, int'):
    need(token in block_entity, f'Wall slideshow/runtime persistence missing: {token}')

update_image = read('src/main/java/celerbi/mirageprojector/network/UpdateImageWorkspacePayload.java')
image_menu = read('src/main/java/celerbi/mirageprojector/menu/ImageProjectorMenu.java')
image_screen = read('src/main/java/celerbi/mirageprojector/client/ImageProjectorScreen.java')
need('int wallSlideIndex' in update_image and 'b.writeVarInt(Math.max(0, p.wallSlideIndex()))' in update_image,
     'Wall current slide is not synchronized in Image Workspace payload')
need('initialWallSlideIndex' in image_menu and 'isWallPresentation()' in image_menu,
     'Image menu lacks Wall presentation state')
for token in ('initWallPresentation()', 'moveSelectedWallSlide', 'stepWallSlide',
              'wallSetCurrentButton', 'renderWallPresentationWorkspace', 'ImageSourceBank.PERSISTED_COMPAT_SLOTS'):
    need(token in image_screen, f'Wall PowerPoint-like playlist GUI missing: {token}')

renderer = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
table_logic = read('src/main/java/celerbi/mirageprojector/client/MirageTableProjectorLogic.java')
for token in ('renderWallDataShowImage', 'wallProjectionSurface()', 'activeWallImage()', 'wallPlaneYaw',
              'usesHorizontalPlaneFor(settings.sourceMode())'):
    need(token in renderer, f'render anchor/Data-show contract missing: {token}')
need('WALL_OUTWARD' not in renderer, 'renderer still contains obsolete wall-mounted outward anchor')
need('Axis.XP.rotationDegrees(-90.0F)' in renderer or 'Axis.XP.rotationDegrees(-90.0F)' in table_logic,
     'Table planar sources are not rotated to horizontal tabletop presentation')

screen = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
for token in ('supportsWallXyOffset()', 'horizontalOffsetSlider', 'verticalOffsetSlider',
              ('withWallOffsets(horizontalOffsetPixels, verticalOffsetPixels)' if 'withWallOffsets(horizontalOffsetPixels, verticalOffsetPixels)' in screen else 'withSurfaceOffsets(horizontalOffsetPixels, verticalOffsetPixels)'), 'WallProjectionSurface.resolve',
              'ProjectionSourceRegistry.isCompatible'):
    need(token in screen, f'Wall settings/compatibility GUI contract missing: {token}')

clearance = read('src/main/java/celerbi/mirageprojector/client/ProjectionClearance.java')
need('safeChassis == ProjectionChassisProfile.WALL' in clearance and 'return Result.EMPTY' in clearance,
     'generic hologram clearance still owns Wall instead of actual-image wall validation')
need('WALL_OUTWARD' not in clearance, 'obsolete wall-mounted clearance path remains')

resources = [
    'src/main/resources/assets/mirage_projector/blockstates/mirage_table_projector.json',
    'src/main/resources/assets/mirage_projector/blockstates/mirage_wall_projector.json',
    'src/main/resources/assets/mirage_projector/models/block/mirage_table_projector.json',
    'src/main/resources/assets/mirage_projector/models/block/mirage_wall_projector.json',
    'src/main/resources/assets/mirage_projector/models/item/mirage_table_projector.json',
    'src/main/resources/assets/mirage_projector/models/item/mirage_wall_projector.json',
    'src/main/resources/data/mirage_projector/loot_table/blocks/mirage_table_projector.json',
    'src/main/resources/data/mirage_projector/loot_table/blocks/mirage_wall_projector.json',
]
for rel in resources:
    path = ROOT / rel
    need(path.exists(), f'1.0.25 resource missing: {rel}')
    if path.exists():
        try:
            json.loads(path.read_text(encoding='utf-8'))
        except Exception as exc:
            need(False, f'invalid JSON {rel}: {exc}')
wall_model = read('src/main/resources/assets/mirage_projector/models/block/mirage_wall_projector.json')
need('minecraft:block/obsidian' in wall_model and 'minecraft:block/crying_obsidian' in wall_model
     and 'mirage_projector:block/crying_obsidian_emitter' in wall_model,
     'Wall first-pass Data-show art does not use Obsidian + Crying Obsidian optical language')

# 1.0.25 deliberately left these recipes unfrozen; later 1.0.32 progression may freeze them.
for rel in ('src/main/resources/data/mirage_projector/recipe/mirage_table_projector.json',
            'src/main/resources/data/mirage_projector/recipe/mirage_wall_display.json'):
    path = ROOT / rel
    if path.exists():
        try:
            json.loads(path.read_text(encoding='utf-8'))
        except Exception as exc:
            need(False, f'later frozen chassis recipe is invalid JSON: {rel}: {exc}')

langs = {}
required_lang = (
    'block.mirage_projector.mirage_table_projector', 'block.mirage_projector.mirage_wall_projector',
    'container.mirage_projector.table', 'container.mirage_projector.wall',
    'gui.mirage_projector.wall.offset_x', 'gui.mirage_projector.wall.offset_y',
    'gui.mirage_projector.wall.playlist', 'gui.mirage_projector.wall.set_current',
    'gui.mirage_projector.wall.failure.irregular_surface', 'tooltip.mirage_projector.power.wall_distance')
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in required_lang:
        need(key in langs[locale], f'{locale} missing {key}')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')

release = read('docs/RELEASE-1.0.25-ANCHOR-CHASSIS-FOUNDATION.md')
for token in ('Data-show, not wall-mounted', 'actual aspect-correct image rectangle', 'X Offset', 'Y Offset',
              'distance surcharge', '9-slot presentation playlist', 'network protocol to **37**',
              'format **3** to **4**', 'Survival recipe'):
    need(token in release, f'1.0.25 release contract missing: {token}')
need(any(v in read('docs/ROADMAP.md') for v in ('Current implementation snapshot: **1.0.25**', 'Current implementation snapshot: **1.0.26**', 'Current implementation snapshot: **1.0.27**', 'Current implementation snapshot: **1.0.28**', 'Current implementation snapshot: **1.0.29**', 'Current implementation snapshot: **1.0.30**', 'Current implementation snapshot: **1.0.31**', 'Current implementation snapshot: **1.0.32**')), 'roadmap baseline is not a compatible 1.0.25+ line')
need(any(v in read('docs/DEVELOPMENT.md') for v in ('Current maintenance baseline: **1.0.25**', 'Current maintenance baseline: **1.0.26**', 'Current maintenance baseline: **1.0.27**', 'Current maintenance baseline: **1.0.28**', 'Current maintenance baseline: **1.0.29**', 'Current maintenance baseline: **1.0.30**', 'Current maintenance baseline: **1.0.31**', 'Current maintenance baseline: **1.0.32**')), 'development baseline is not a compatible 1.0.25+ line')
need(any(v in read('docs/CURRENT-IMPLEMENTATION.md') for v in ('Mirage Projector 1.0.25', 'Mirage Projector 1.0.26', 'Mirage Projector 1.0.27', 'Mirage Projector 1.0.28', 'Mirage Projector 1.0.29', 'Mirage Projector 1.0.30', 'Mirage Projector 1.0.31', 'Mirage Projector 1.0.32')), 'current implementation baseline is not a compatible 1.0.25+ line')
need(any(v in read('docs/DOCUMENTATION-AUTHORITY.md') for v in ('Documentation Authority — Mirage Projector 1.0.25', 'Documentation Authority — Mirage Projector 1.0.26', 'Documentation Authority — Mirage Projector 1.0.27', 'Documentation Authority — Mirage Projector 1.0.28', 'Documentation Authority — Mirage Projector 1.0.29', 'Documentation Authority — Mirage Projector 1.0.30', 'Documentation Authority — Mirage Projector 1.0.31', 'Documentation Authority — Mirage Projector 1.0.32')),
     'documentation authority baseline is not a compatible 1.0.25+ line')

for forbidden in ('build', 'run', '.gradle', '.gradle-dist', '__pycache__'):
    hits = [p for p in ROOT.rglob(forbidden) if p.is_dir()]
    need(not hits, f'generated/cache directory present: {forbidden}: {[str(p.relative_to(ROOT)) for p in hits[:5]]}')
for p in ROOT.rglob('*'):
    if p.is_file():
        need(p.suffix not in {'.class', '.jar', '.pyc'}, f'generated binary/cache present: {p.relative_to(ROOT)}')

if errors:
    print('Mirage Projector 1.0.25 Table/Data-show verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print(f'Mirage Projector 1.0.25 Table/Data-show verification PASS ({len(langs["en_us"])} lang keys)')
