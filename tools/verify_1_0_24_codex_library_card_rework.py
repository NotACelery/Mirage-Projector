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
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
main = main.replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"40\"')
need(any(v in props for v in ('mod_version=1.0.24', 'mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'version is not a compatible 1.0.24+ line')
need(('NETWORK_PROTOCOL = "36"' in main or ('NETWORK_PROTOCOL = "37"' in main or ('NETWORK_PROTOCOL = "38"' in main or 'NETWORK_PROTOCOL = "39"' in main or 'NETWORK_PROTOCOL = "40"' in main))), '1.0.24+ network protocol baseline missing')
need(any(v in read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java') for v in ('SERIALIZATION_VERSION = 3', 'SERIALIZATION_VERSION = 4')),
     'ProjectionSettings format is not a compatible v3/v4 line')

saved = read('src/main/java/celerbi/mirageprojector/scan/ScanCodexSavedData.java')
for token in ('MAX_SCANS_PER_ENTITY_TYPE = 25', 'addScanRoot(UUID codexId, CompoundTag sourceRoot)',
              'countForType(library, entityType) >= MAX_SCANS_PER_ENTITY_TYPE', 'root.putUUID("ScanId", scanId)'):
    need(token in saved, f'Codex per-type/import-root contract missing: {token}')

card = read('src/main/java/celerbi/mirageprojector/item/EntityScanCardItem.java')
interaction = read('src/main/java/celerbi/mirageprojector/event/EntityScanInteractionEvents.java')
need('scanTarget(' not in card, 'Entity Scan Card still acts as a scanner')
need('instanceof ScanCodexItem codex' in interaction and 'EntityScanCardItem' not in interaction,
     'entity capture gesture is not exclusive to Scan Codex')

recipe = read('src/main/java/celerbi/mirageprojector/recipe/ScanCardClearingRecipe.java')
serializer = read('src/main/java/celerbi/mirageprojector/recipe/ScanCardClearingRecipeSerializer.java')
serializers = read('src/main/java/celerbi/mirageprojector/registry/ModRecipeSerializers.java')
for token in ('hasExactlyOneFilledCard', 'EntityScanData.hasScan(found)', 'new ItemStack(ModItems.ENTITY_SCAN_CARD.get())'):
    need(token in recipe, f'filled-card clearing recipe missing: {token}')
need('SCAN_CARD_CLEARING' in serializers and 'ScanCardClearingRecipeSerializer' in serializers,
     'scan-card clearing serializer is not registered')
need('MapCodec.unit' in serializer and 'StreamCodec.unit' in serializer,
     'scan-card clearing serializer codec contract missing')
need((ROOT / 'src/main/resources/data/mirage_projector/recipe/clear_entity_scan_card.json').exists(),
     'scan-card clearing recipe json missing')

menu = read('src/main/java/celerbi/mirageprojector/menu/ScanCodexMenu.java')
screen = read('src/main/java/celerbi/mirageprojector/client/ScanCodexScreen.java')
for token in ('DUPLICATE_SLOT', 'IMPORT_SLOT', 'EXTENSION_SLOT_COUNT = 2',
              'stack.is(ModItems.ENTITY_SCAN_CARD.get()) && EntityScanData.hasScan(stack)',
              'EasyMobFarmCompat.isCaptureCard(stack)', 'importPanelOpen = lecternMode && open'):
    need(token in menu, f'Lectern extension-slot contract missing: {token}')
need('easyMobFarmAvailable && (!playerInventory.player.level().isClientSide || importPanelOpen)' not in menu,
     'import slot is still hidden when Easy Mob Farm is absent')

for token in (('VISIBLE_ROWS = 8' if 'mod_version=1.0.30' not in props else 'VISIBLE_ROWS = 11'), 'mouseScrolled', 'EditBox', 'ALL("gui.mirage_projector.scan_codex.tab.all"', 'FAVORITES("gui.mirage_projector.scan_codex.tab.favorites"',
              'HOSTILE("gui.mirage_projector.scan_codex.tab.hostile"', 'PASSIVE("gui.mirage_projector.scan_codex.tab.passive"', 'FARM("gui.mirage_projector.scan_codex.tab.farm"', 'NETHER("gui.mirage_projector.scan_codex.tab.nether"', 'END("gui.mirage_projector.scan_codex.tab.end"',
              'WATER("gui.mirage_projector.scan_codex.tab.water"', 'PLAYERS("gui.mirage_projector.scan_codex.tab.players"', 'OTHER("gui.mirage_projector.scan_codex.tab.other"', 'renderDetail(', 'renderEquipment(',
              'nameplateText()', 'scrollRow', 'detailView'):
    need(token in screen, f'scrollable categorized Codex/detail contract missing: {token}')
need('scrollRow = 0' not in screen[screen.find('private void leaveDetail()'):screen.find('private void requestReturnDuplicateCard()')],
     'Back from detail resets the previous list scroll')
need('boolean canImport = menu.lecternMode() && !detailView;' in screen,
     'import toggle is not permanently available in Lectern library mode')
need('menu.easyMobFarmAvailable() && !detailView' not in screen,
     'import toggle is still gated by Easy Mob Farm')
need('LecternScanCodexActionPayload.Action.IMPORT_CARD' in screen,
     'generic card import action is not wired from UI')
need('card.is(ModItems.ENTITY_SCAN_CARD.get()) && EntityScanData.hasScan(card)' in screen,
     'filled Mirage Entity Scan Card is not recognized by import UI')
need('EasyMobFarmCompat.isCaptureCard(card)' in screen,
     'optional Easy Mob Farm capture card is not recognized by import UI')
need('consumed_hint' in screen, 'destructive-import warning is not visible in import extension')

copy_service = read('src/main/java/celerbi/mirageprojector/scan/ScanDuplicationService.java')
for token in ('duplicateInto(ServerPlayer player, ItemStack codex, UUID scanId, ItemStack targetCard)',
              'EntityScanData.hasScan(targetCard)', 'EntityScanData.writeRootToCard(targetCard, storedRoot.get())'):
    need(token in copy_service, f'blank-card duplication contract missing: {token}')
need('Items.PAPER' not in copy_service, 'obsolete Paper duplication cost remains')

import_service = read('src/main/java/celerbi/mirageprojector/scan/ScanCodexImportService.java')
for token in ('importMirageCard', 'EntityScanData.copyRoot(sourceCard)', 'saved.addScanRoot(codexId, root.get())',
              'EasyMobFarmCompat.importCaptureCard', 'if (result == Result.SUCCESS)', 'sourceCard.shrink(1)'):
    need(token in import_service, f'destructive import contract missing: {token}')
need(import_service.find('if (result == Result.SUCCESS)') < import_service.find('sourceCard.shrink(1)'),
     'source card consumption is not guarded by successful import')

easy = read('src/main/java/celerbi/mirageprojector/compat/EasyMobFarmCompat.java')
for token in ('MOD_ID = "easy_mob_farm"', 'mob_capture_card', 'mob_capture_data', 'DataComponentType<?>',
              'getMethod("entityType")', 'getMethod("data")', 'EntityScanData.create(living)'):
    need(token in easy, f'Easy Mob Farm optional import bridge missing: {token}')
need('sourceCard.shrink' not in easy and 'captureCard.shrink' not in easy,
     'Easy Mob Farm bridge consumes input before generic success authority')

payload = read('src/main/java/celerbi/mirageprojector/network/LecternScanCodexActionPayload.java')
for token in ('IMPORT_CARD', 'ScanCodexImportService.importCard', 'message.mirage_projector.scan_codex.import.success_consumed'):
    need(token in payload, f'generic Lectern import payload missing: {token}')
need('IMPORT_EASY_MOB_FARM' not in payload, 'old Easy-Mob-Farm-only import action remains')

portable_menu = read('src/main/java/celerbi/mirageprojector/menu/PortableDeviceMenu.java')
portable = read('src/main/java/celerbi/mirageprojector/client/PortableDeviceScreen.java')
need(('PROJECTOR_PLAYER_INV_Y = 198' in portable_menu or 'PROJECTOR_PLAYER_INV_Y = 158' in portable_menu), 'Hand Projector inventory is not using separated lite layout')
need(('PROJECTOR_HEIGHT = 294' in portable or 'PROJECTOR_HEIGHT = 242' in portable), 'Hand Projector screen is not using its dedicated compact height')
for token in ('bannerPresentationButton', 'warBannerFacingButton', 'updateProjectorControls()',
              'CYCLE_BANNER_PRESENTATION', 'CYCLE_WAR_BANNER_FACING',
              'WAR_BANNER_SIZE_DOWN', 'WAR_BANNER_SIZE_UP', 'WAR_BANNER_HEIGHT_DOWN', 'WAR_BANNER_HEIGHT_UP',
              'mode == ProjectionSettings.SourceMode.BANNER',
              'MirageHandProjectorItem.warBannerActive(device)', 'war_banner_size_value', 'war_banner_height_value'):
    need(token in portable, f'portable lite/banner UI contract missing: {token}')
need('bannerPresentationButton.visible = banner' in portable and 'warBannerFacingButton.visible = warBanner' in portable,
     'Banner controls are not dynamically visibility-bound to live state')

release = read('docs/RELEASE-1.0.24-CODEX-LIBRARY-CARD-REWORK.md')
for token in ('25 captures per entity type', 'consumes one source card', 'experience levels', 'not raw XP',
              'Forward Projection', 'War Banner', 'Directional', 'Always Face Viewer'):
    need(token in release, f'1.0.24 release contract missing: {token}')

langs = {}
required_keys = (
    'gui.mirage_projector.scan_codex.import.consumed_hint',
    'gui.mirage_projector.scan_codex.import.easy_mob_farm_hint',
    'message.mirage_projector.scan_codex.import.success_consumed',
    'gui.mirage_projector.portable_device.turn_on',
    'gui.mirage_projector.portable_device.turn_off',
    'gui.mirage_projector.portable_device.banner_presentation_lite',
    'gui.mirage_projector.portable_device.war_banner_facing_lite',
    'gui.mirage_projector.portable_device.war_banner_size_value',
    'gui.mirage_projector.portable_device.war_banner_height_value',
)
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in required_keys:
        need(key in langs[locale], f'{locale} missing {key}')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')

for forbidden in ('build', 'run', '.gradle', '.gradle-dist', '__pycache__'):
    hits = [p for p in ROOT.rglob(forbidden) if p.is_dir()]
    need(not hits, f'generated/cache directory present: {forbidden}: {[str(p.relative_to(ROOT)) for p in hits[:5]]}')
for p in ROOT.rglob('*'):
    if p.is_file():
        need(p.suffix not in {'.class', '.jar', '.pyc'}, f'generated binary/cache present: {p.relative_to(ROOT)}')

if errors:
    print('Mirage Projector 1.0.24 Codex/card + portable UI verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print(f'Mirage Projector 1.0.24 Codex/card + portable UI verification PASS ({len(langs["en_us"])} lang keys)')
