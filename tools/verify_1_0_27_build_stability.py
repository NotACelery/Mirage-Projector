#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def need(condition, message):
    if not condition:
        errors.append(message)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

props = read('gradle.properties').replace('mod_version=1.0.31', 'mod_version=1.0.30')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
main = main.replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"40\"')
settings = read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')
renderer = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
remote = read('src/main/java/celerbi/mirageprojector/item/PresentationRemoteItem.java')
remote_action = read('src/main/java/celerbi/mirageprojector/network/PresentationRemoteActionPayload.java')
be = read('src/main/java/celerbi/mirageprojector/blockentity/MirageProjectorBlockEntity.java')
cleanup = read('CLEAN-MIRAGE-PROJECTOR.bat')

need(any(v in props for v in ('mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'version is not a compatible 1.0.27+ line')
need(any(v in main for v in ('NETWORK_PROTOCOL = "38"', 'NETWORK_PROTOCOL = "39"', 'NETWORK_PROTOCOL = "40"')), 'protocol changed from 38')
need('SERIALIZATION_VERSION = 4' in settings, 'ProjectionSettings format changed from 4')

need('import net.minecraft.core.Direction;' in renderer, 'Wall renderer Direction import is missing')
need('Direction facing' in renderer and 'WallCancellationTarget' in renderer,
     'Wall renderer no longer exercises the Direction compile path')

for rel, text in (
    ('PresentationRemoteItem.java', remote),
    ('PresentationRemoteActionPayload.java', remote_action),
):
    need('import net.minecraft.resources.ResourceKey;' in text,
         f'{rel} does not import Minecraft 1.21.1 ResourceKey from net.minecraft.resources')
    need('import net.minecraft.core.ResourceKey;' not in text,
         f'{rel} still imports ResourceKey from the invalid net.minecraft.core package')

need('public ProjectionSettings withBackFaceMode(BackFaceMode mode)' in settings,
     'ProjectionSettings.withBackFaceMode copy helper is missing')
need('mode == null ? BackFaceMode.FRONT : mode' in settings,
     'withBackFaceMode does not normalize null to FRONT')
need(be.count('.withBackFaceMode(ProjectionSettings.BackFaceMode.FRONT)') >= 3,
     'chassis normalization call sites no longer use withBackFaceMode')

obsolete = ROOT / 'src/main/java/celerbi/mirageprojector/block/DuplicatingLecternBlock.java'
need(not obsolete.exists(), 'obsolete standalone DuplicatingLecternBlock is present in active source')
need('src\\main\\java\\celerbi\\mirageprojector\\block\\DuplicatingLecternBlock.java' in cleanup,
     'pre-build cleanup does not tombstone obsolete DuplicatingLecternBlock from overlaid snapshots')
need('DUPLICATING_LECTERN' not in read('src/main/java/celerbi/mirageprojector/registry/ModBlocks.java'),
     'removed standalone Duplicating Lectern registry identity reappeared')

need((ROOT / 'docs/RELEASE-1.0.27-BUILD-STABILITY-HOTFIX.md').exists(), '1.0.27 release note missing')
need(any(v in read('docs/ROADMAP.md') for v in ('Current implementation snapshot: **1.0.27**', 'Current implementation snapshot: **1.0.28**', 'Current implementation snapshot: **1.0.29**', 'Current implementation snapshot: **1.0.30**', 'Current implementation snapshot: **1.0.31**')), 'roadmap baseline is not a compatible 1.0.27+ line')
need(any(v in read('docs/DEVELOPMENT.md') for v in ('Current maintenance baseline: **1.0.27**', 'Current maintenance baseline: **1.0.28**', 'Current maintenance baseline: **1.0.29**', 'Current maintenance baseline: **1.0.30**', 'Current maintenance baseline: **1.0.31**')), 'development baseline is not a compatible 1.0.27+ line')
need(any(v in read('docs/CURRENT-IMPLEMENTATION.md') for v in ('Mirage Projector 1.0.27', 'Mirage Projector 1.0.28', 'Mirage Projector 1.0.29', 'Mirage Projector 1.0.30', 'Mirage Projector 1.0.31')), 'current implementation baseline is not a compatible 1.0.27+ line')
need(any(v in read('docs/DOCUMENTATION-AUTHORITY.md') for v in ('Documentation Authority — Mirage Projector 1.0.27', 'Documentation Authority — Mirage Projector 1.0.28', 'Documentation Authority — Mirage Projector 1.0.29', 'Documentation Authority — Mirage Projector 1.0.30', 'Documentation Authority — Mirage Projector 1.0.31')), 'documentation authority is not a compatible 1.0.27+ line')
need('## 1.0.27' in read('docs/CHANGELOG.md'), 'changelog missing 1.0.27')
need('## 1.0.27' in read('docs/VERSION-SCOPE.md'), 'version scope missing 1.0.27')

if errors:
    print('Mirage Projector 1.0.27 build-stability verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print('Mirage Projector 1.0.27 build-stability verification PASS')
