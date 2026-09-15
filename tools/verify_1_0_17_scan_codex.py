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

props = read('gradle.properties')
need(('mod_version=1.0.17' in props or 'mod_version=1.0.18' in props or 'mod_version=1.0.19' in props or 'mod_version=1.0.20' in props), 'version is not 1.0.17')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
need(('NETWORK_PROTOCOL = "33"' in main or 'NETWORK_PROTOCOL = "34"' in main), '1.0.17 must use network protocol 33')

items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
need('DeferredItem<ScanCodexItem> SCAN_CODEX' in items, 'Scan Codex registry entry missing')
need('ITEMS.register("scan_codex"' in items, 'scan_codex registry id missing')
need((ROOT / 'src/main/resources/assets/mirage_projector/models/item/scan_codex.json').exists(), 'Scan Codex item model missing')

codex_item = read('src/main/java/celerbi/mirageprojector/item/ScanCodexItem.java')
for token in (
    'MirageScanCodexId', 'MirageScanCodexSelected', 'ensureCodexId', 'selectedScanId',
    'scanTarget', 'ScanCodexSavedData.get', 'setSelectedScanId', 'sendSnapshot',
    'Shift + right-click' if False else 'tooltip.mirage_projector.scan_codex.scan'):
    need(token in codex_item, f'Scan Codex item contract missing: {token}')
need('properties.stacksTo(1)' in codex_item, 'Scan Codex must be non-stackable')
need('EntityScanData.create(target)' in codex_item, 'Codex does not reuse canonical EntityScanData capture')
need('target.isPassenger() || target.isVehicle()' in codex_item, 'Codex does not preserve composite scan rejection')
need('writeToCard' not in codex_item, 'Codex scan path should not materialize projector cards directly')

saved = read('src/main/java/celerbi/mirageprojector/scan/ScanCodexSavedData.java')
for token in (
    'extends SavedData', 'mirage_projector_scan_codices', 'server.overworld().getDataStorage()',
    'computeIfAbsent(FACTORY, DATA_NAME)', 'LinkedHashMap', 'addScan', 'toggleFavorite',
    'copyScanRoot', 'summaries', 'setDirty()', 'root.copy()'):
    need(token in saved, f'Scan Codex SavedData contract missing: {token}')
need('EntityScanData.readRoot(root)' in saved, 'Saved Codex entries are not validated through EntityScanData')
need('DataComponents.CUSTOM_DATA' not in saved, 'full Codex library must not be serialized inside ItemStack CustomData')

summary = read('src/main/java/celerbi/mirageprojector/scan/ScanCodexEntrySummary.java')
for token in ('UUID scanId', 'ResourceLocation entityType', 'EntityScanData.Kind kind', 'favorite', 'equipmentCount'):
    need(token in summary, f'Codex summary metadata missing: {token}')

open_payload = read('src/main/java/celerbi/mirageprojector/network/OpenScanCodexPayload.java')
need('List<ScanCodexEntrySummary> entries' in open_payload, 'Codex browser payload is not metadata-summary based')
need('CompoundTag' not in open_payload, 'Codex browser payload must not transmit full frozen scan NBT')
need('open_scan_codex' in open_payload, 'Codex open payload channel missing')

action_payload = read('src/main/java/celerbi/mirageprojector/network/ScanCodexActionPayload.java')
need('SELECT' in action_payload and 'TOGGLE_FAVORITE' in action_payload, 'Codex server actions missing')
need('ScanCodexItem.findOwnedCodex' in action_payload, 'Codex action does not validate player ownership')
need('data.contains(payload.codexId(), payload.scanId())' in action_payload, 'Codex action does not validate exact scan identity')

network = read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')
for token in ('OpenScanCodexPayload.TYPE', 'ScanCodexActionPayload.TYPE'):
    need(token in network, f'network registration missing {token}')

screen = read('src/main/java/celerbi/mirageprojector/client/ScanCodexScreen.java')
for token in (
    'EditBox', 'FAVORITES', 'PLAYERS', 'HUMANOIDS', 'HORSES', 'OTHER',
    'TOGGLE_FAVORITE', 'Action.SELECT', 'PAGE_SIZE', 'equipmentCount()', 'nameplateText()'):
    need(token in screen, f'Codex browser UI missing: {token}')

interaction = read('src/main/java/celerbi/mirageprojector/event/EntityScanInteractionEvents.java')
need('instanceof ScanCodexItem' in interaction, 'high-priority entity interaction bridge does not support Scan Codex')
need('instanceof EntityScanCardItem' in interaction, 'physical Entity Scan Card scanning regressed')

creative = read('src/main/java/celerbi/mirageprojector/registry/ModCreativeTabs.java')
need('ModItems.SCAN_CODEX.get()' in creative, 'Scan Codex not exposed in Mirage Creative tab')

langs = {}
required_keys = (
    'item.mirage_projector.scan_codex',
    'tooltip.mirage_projector.scan_codex.open',
    'tooltip.mirage_projector.scan_codex.scan',
    'gui.mirage_projector.scan_codex.search',
    'gui.mirage_projector.scan_codex.filter.favorites',
    'gui.mirage_projector.scan_codex.filter.players',
    'gui.mirage_projector.scan_codex.filter.humanoids',
    'gui.mirage_projector.scan_codex.filter.horses',
    'gui.mirage_projector.scan_codex.filter.other',
)
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in required_keys:
        need(key in langs[locale], f'{locale} missing {key}')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language parity broken')

waitlist = read('docs/WAITLIST-1.1.0.md')
need('delivered in 1.0.17 foundation' in waitlist, '1.1 waitlist does not mark Scan Codex foundation delivered')
need('D. Mirage Scan Codex' in waitlist and 'SavedData' in waitlist, 'Scan Codex authoritative waitlist details missing')
need('Duplicating Lectern' in waitlist, 'Duplicating Lectern dependency disappeared from 1.1 roadmap')
release = ROOT / 'docs/RELEASE-1.0.17-SCAN-CODEX.md'
need(release.exists(), '1.0.17 release note missing')
handoff = ROOT / 'docs/history/handoffs/NEXT-CHAT-HANDOFF-1.0.17-SCAN-CODEX.md'
need(handoff.exists(), '1.0.17 handoff missing')

if errors:
    print('Mirage Projector 1.0.17 Scan Codex verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.17 Scan Codex verification PASS')
