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


raw_props = read('gradle.properties')
props = raw_props.replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"40\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"40\"')
is_1028 = any(v in props for v in ('mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30'))
is_1027 = 'mod_version=1.0.27' in props or is_1028
is_1026 = 'mod_version=1.0.26' in props
is_1025 = 'mod_version=1.0.25' in props or is_1026 or is_1027
is_1024 = 'mod_version=1.0.24' in props or is_1025
need('mod_version=1.0.23' in props or is_1024 or is_1025 or is_1026 or is_1027, 'version is not a compatible 1.0.23+ line')
need(('NETWORK_PROTOCOL = "35"' in main or 'NETWORK_PROTOCOL = "36"' in main or ('NETWORK_PROTOCOL = "37"' in main or ('NETWORK_PROTOCOL = "38"' in main or 'NETWORK_PROTOCOL = "39"' in main or 'NETWORK_PROTOCOL = "40"' in main))), '1.0.23+ network protocol baseline missing')

blocks = read('src/main/java/celerbi/mirageprojector/registry/ModBlocks.java')
items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
creative = read('src/main/java/celerbi/mirageprojector/registry/ModCreativeTabs.java')
for text, where in (
    ('DUPLICATING_LECTERN', blocks),
    ('DUPLICATING_LECTERN', items),
    ('DUPLICATING_LECTERN', creative),
    ('DUPLICATING_LECTERN', main),
    ('"duplicating_lectern"', blocks),
):
    need(text not in where, f'temporary standalone Duplicating Lectern registry still present: {text}')
need(not (ROOT / 'src/main/java/celerbi/mirageprojector/block/DuplicatingLecternBlock.java').exists(),
     'temporary DuplicatingLecternBlock source still exists')
for rel in (
    'src/main/resources/assets/mirage_projector/blockstates/duplicating_lectern.json',
    'src/main/resources/assets/mirage_projector/models/block/duplicating_lectern.json',
    'src/main/resources/assets/mirage_projector/models/item/duplicating_lectern.json',
    'src/main/resources/data/mirage_projector/loot_table/blocks/duplicating_lectern.json',
):
    need(not (ROOT / rel).exists(), f'temporary Duplicating Lectern resource still exists: {rel}')

lectern_event = read('src/main/java/celerbi/mirageprojector/event/ScanCodexLecternEvents.java')
for token in (
    'PlayerInteractEvent.RightClickBlock',
    'state.getBlock() instanceof LecternBlock',
    'LecternBlockEntity lectern',
    'mounted.is(ModItems.SCAN_CODEX.get())',
    'LecternBlock.HAS_BOOK',
    'LecternBlock.tryPlaceBook',
    'ScanCodexLecternService.openMenu' if is_1024 else 'ScanCodexLecternService.sendSnapshot',
):
    need(token in lectern_event, f'vanilla Lectern integration missing: {token}')

service = read('src/main/java/celerbi/mirageprojector/scan/ScanCodexLecternService.java')
for token in (
    'MAX_USE_DISTANCE_SQR',
    'LecternBlockEntity',
    'expectedCodexId.equals(ScanCodexItem.codexId(codex))',
    'OpenLecternScanCodexPayload',
    'lectern.clearContent()',
    'LecternBlock.resetBookState',
    'player.getInventory().add(returned)',
):
    need(token in service, f'Lectern Codex server authority missing: {token}')

open_payload = read('src/main/java/celerbi/mirageprojector/network/OpenLecternScanCodexPayload.java')
action_payload = read('src/main/java/celerbi/mirageprojector/network/LecternScanCodexActionPayload.java')
network = read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')
for token in ('BlockPos pos', 'buffer.readBlockPos()', 'buffer.writeBlockPos(payload.pos())', 'ClientScanCodex.acceptLectern'):
    need(token in open_payload, f'Lectern Codex open payload missing: {token}')
for token in ('DUPLICATE', 'TAKE_CODEX', 'ScanCodexLecternService.codexAt', ('ScanDuplicationService.duplicateInto' if is_1024 else 'ScanDuplicationService.duplicate'), 'toggleFavorite'):
    need(token in action_payload, f'Lectern Codex action payload missing: {token}')
need('OpenLecternScanCodexPayload.TYPE' in network, 'Lectern open payload not registered')
need('LecternScanCodexActionPayload.TYPE' in network, 'Lectern action payload not registered')

service_copy = read('src/main/java/celerbi/mirageprojector/scan/ScanDuplicationService.java')
copy_tokens = (
    ('duplicateInto(ServerPlayer player, ItemStack codex, UUID scanId, ItemStack targetCard)',
     'copyScanRoot(codexId, scanId)',
     'EntityScanData.writeRootToCard(targetCard, storedRoot.get())')
    if is_1024 else
    ('duplicate(ServerPlayer player, ItemStack codex, UUID scanId)',
     'copyScanRoot(codexId, scanId)',
     'Items.PAPER',
     'EntityScanData.writeRootToCard(output, storedRoot.get())')
)
for token in copy_tokens:
    need(token in service_copy, f'exact-root duplication contract missing: {token}')

screen = read('src/main/java/celerbi/mirageprojector/client/ScanCodexScreen.java')
for token in (
    ('menu.lecternPos()' if is_1024 else 'private final BlockPos lecternPos'),
    ('menu.lecternMode()' if is_1024 else 'gui.mirage_projector.scan_codex.lectern_mode'),
    'gui.mirage_projector.scan_codex.duplicate',
    'gui.mirage_projector.scan_codex.take_codex',
    'LecternScanCodexActionPayload.Action.DUPLICATE',
    'LecternScanCodexActionPayload.Action.TAKE_CODEX',
    'public void renderBackground',
    ('Intentional: physical Codex overlays the live world' if is_1024 else 'Intentionally no blur/dim'),
    'renderBook(',
):
    need(token in screen, f'Codex book/Lectern UI contract missing: {token}')
need('0xF01A1517' not in screen, 'old flat dark Codex panel palette is still present')

langs = {}
required = (
    'tooltip.mirage_projector.scan_codex.lectern',
    'gui.mirage_projector.scan_codex.duplicate',
    'gui.mirage_projector.scan_codex.take_codex',
    'message.mirage_projector.scan_codex.duplicate.success',
) if is_1024 else (
    'tooltip.mirage_projector.scan_codex.lectern',
    'gui.mirage_projector.scan_codex.lectern_mode',
    'gui.mirage_projector.scan_codex.duplicate',
    'gui.mirage_projector.scan_codex.take_codex',
    'gui.mirage_projector.scan_codex.paper_available',
    'message.mirage_projector.scan_codex.duplicate.success',
    'message.mirage_projector.scan_codex.duplicate.no_paper',
)
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in required:
        need(key in langs[locale], f'{locale} missing {key}')
    need('block.mirage_projector.duplicating_lectern' not in langs[locale],
         f'{locale} still exposes removed standalone Duplicating Lectern block')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language key parity broken')

need((ROOT / 'docs/RELEASE-1.0.23-VANILLA-LECTERN-CODEX.md').exists(), '1.0.23 release note missing')
need((ROOT / 'docs/history/handoffs/NEXT-CHAT-HANDOFF-1.0.23-VANILLA-LECTERN-CODEX.md').exists(),
     '1.0.23 handoff missing')
current_minor = 32 if 'mod_version=1.0.32' in raw_props else (31 if 'mod_version=1.0.31' in raw_props else (30 if 'mod_version=1.0.30' in props else (29 if 'mod_version=1.0.29' in props else (28 if is_1028 else (27 if is_1027 else (26 if is_1026 else (25 if is_1025 else (24 if is_1024 else 23))))))))
need(f'Current implementation snapshot: **1.0.{current_minor}**' in read('docs/ROADMAP.md'), 'roadmap baseline is not current')
need(f'Current maintenance baseline: **1.0.{current_minor}**' in read('docs/DEVELOPMENT.md'), 'development baseline is not current')
need(f'Mirage Projector 1.0.{current_minor}' in read('docs/CURRENT-IMPLEMENTATION.md'), 'current implementation baseline is not current')
need('vanilla `minecraft:lectern`' in read('docs/CURRENT-IMPLEMENTATION.md'), 'current implementation does not freeze vanilla Lectern workflow')

if errors:
    print('Mirage Projector 1.0.23 vanilla Lectern Codex verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.23 vanilla Lectern Codex verification PASS')
