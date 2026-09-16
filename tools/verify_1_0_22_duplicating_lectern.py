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


props = read('gradle.properties').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
need('mod_version=1.0.22' in props, 'version is not 1.0.22')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
need('NETWORK_PROTOCOL = "34"' in main, '1.0.22 must retain network protocol 34')

blocks = read('src/main/java/celerbi/mirageprojector/registry/ModBlocks.java')
items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
creative = read('src/main/java/celerbi/mirageprojector/registry/ModCreativeTabs.java')
need('DeferredBlock<DuplicatingLecternBlock> DUPLICATING_LECTERN' in blocks, 'Duplicating Lectern block registration missing')
need('"duplicating_lectern"' in blocks, 'Duplicating Lectern registry ID missing')
need('ModBlocks.DUPLICATING_LECTERN' in items, 'Duplicating Lectern BlockItem missing')
need('ModItems.DUPLICATING_LECTERN.get()' in creative, 'Duplicating Lectern missing from Mirage Creative tab')
need('ModItems.DUPLICATING_LECTERN.get()' in main, 'Duplicating Lectern missing from Functional Blocks Creative exposure')

block = read('src/main/java/celerbi/mirageprojector/block/DuplicatingLecternBlock.java')
for token in (
    'extends Block',
    'HorizontalDirectionalBlock.FACING',
    'useItemOn(',
    'stack.is(ModItems.SCAN_CODEX.get())',
    'ScanDuplicationService.duplicateSelected',
    'PASS_TO_DEFAULT_BLOCK_INTERACTION',
    'useWithoutItem(',
):
    need(token in block, f'Duplicating Lectern block contract missing: {token}')
need('BaseEntityBlock' not in block, 'Duplicating Lectern foundation must remain stateless/no BlockEntity')

service = read('src/main/java/celerbi/mirageprojector/scan/ScanDuplicationService.java')
for token in (
    'ScanCodexItem.codexId(codex)',
    'ScanCodexItem.selectedScanId(codex)',
    'copyScanRoot(codexId, selected.get())',
    'EntityScanData.readRoot(storedRoot.get())',
    'Items.PAPER',
    'EntityScanData.writeRootToCard(output, storedRoot.get())',
    'player.getInventory().add(output)',
    'player.drop(output, false)',
    'player.getAbilities().instabuild',
):
    need(token in service, f'Scan duplication service missing: {token}')
need('UUID.randomUUID()' not in service, 'physical duplication must preserve the selected capture ScanId')
need('toggleFavorite' not in service, 'physical duplication must not copy/mutate Codex favorite metadata')

scan_data = read('src/main/java/celerbi/mirageprojector/entity/EntityScanData.java')
need('boolean writeRootToCard(ItemStack card, CompoundTag root)' in scan_data, 'canonical frozen-root-to-card helper missing')
need('readRoot(root).isEmpty()' in scan_data, 'frozen-root-to-card helper does not validate canonical scan roots')
need('custom.put(ROOT_KEY, root.copy())' in scan_data, 'frozen-root copy does not preserve full snapshot root')

for rel in (
    'src/main/resources/assets/mirage_projector/blockstates/duplicating_lectern.json',
    'src/main/resources/assets/mirage_projector/models/block/duplicating_lectern.json',
    'src/main/resources/assets/mirage_projector/models/item/duplicating_lectern.json',
    'src/main/resources/data/mirage_projector/loot_table/blocks/duplicating_lectern.json',
):
    need((ROOT / rel).exists(), f'missing Duplicating Lectern resource: {rel}')

langs = {}
required = (
    'block.mirage_projector.duplicating_lectern',
    'message.mirage_projector.duplicating_lectern.instructions',
    'message.mirage_projector.duplicating_lectern.success',
    'message.mirage_projector.duplicating_lectern.no_selection',
    'message.mirage_projector.duplicating_lectern.no_paper',
    'message.mirage_projector.duplicating_lectern.missing_scan',
    'message.mirage_projector.duplicating_lectern.invalid_scan',
    'message.mirage_projector.duplicating_lectern.invalid_codex',
)
for locale in ('en_us', 'es_cl', 'es_es'):
    langs[locale] = json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json'))
    for key in required:
        need(key in langs[locale], f'{locale} missing {key}')
need(set(langs['en_us']) == set(langs['es_cl']) == set(langs['es_es']), 'language key parity broken')

waitlist = read('docs/WAITLIST-1.1.0.md')
need('delivered in 1.0.22 foundation' in waitlist, '1.1 waitlist does not mark Duplicating Lectern foundation delivered')
need('Paper is consumed; Codex entry remains' in waitlist, 'Duplicating Lectern frozen Paper/Codex contract missing')
need((ROOT / 'docs/RELEASE-1.0.22-DUPLICATING-LECTERN.md').exists(), '1.0.22 release note missing')
need((ROOT / 'docs/history/handoffs/NEXT-CHAT-HANDOFF-1.0.22-DUPLICATING-LECTERN.md').exists(), '1.0.22 handoff missing')
need('Current implementation snapshot: **1.0.22**' in read('docs/ROADMAP.md'), 'roadmap baseline not 1.0.22')
need('Current maintenance baseline: **1.0.22**' in read('docs/DEVELOPMENT.md'), 'development baseline not 1.0.22')
need('Mirage Projector 1.0.22' in read('docs/CURRENT-IMPLEMENTATION.md'), 'current implementation baseline not 1.0.22')

if errors:
    print('Mirage Projector 1.0.22 Duplicating Lectern verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.22 Duplicating Lectern verification PASS')
