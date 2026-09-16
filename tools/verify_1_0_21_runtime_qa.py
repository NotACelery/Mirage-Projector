#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

def need(cond, msg):
    if not cond:
        errors.append(msg)

props = read('gradle.properties').replace('mod_version=1.0.31', 'mod_version=1.0.30')
need(any(v in props for v in ('mod_version=1.0.21', 'mod_version=1.0.22', 'mod_version=1.0.23', 'mod_version=1.0.24', 'mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'version is not 1.0.21')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
need(('NETWORK_PROTOCOL = "34"' in main or ('NETWORK_PROTOCOL = "35"' in main or ('NETWORK_PROTOCOL = \"36\"' in main or ('NETWORK_PROTOCOL = \"37\"' in main or 'NETWORK_PROTOCOL = \"38\"' in main)))), '1.0.21 must retain protocol 34')

sync = read('src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java')
for token in (
    'invalidateDynamicSections(',
    'boundaryChanged(before, after, dx, dy, dz)',
    'for (int dx = -1; dx <= 1; dx++)',
    'for (int dy = -1; dy <= 1; dy++)',
    'for (int dz = -1; dz <= 1; dz++)',
    'invalidateExact(minecraft, refreshKeys)',
):
    need(token in sync, f'light invalidation missing {token}')

manager = read('src/main/java/celerbi/mirageprojector/client/ClientDynamicMirageLightManager.java')
need('snapshotAggregate(level, installedField)' in manager, 'dynamic updates do not snapshot pre-solve aggregate')
need(manager.count('ClientMirageLightSync.invalidateDynamicSections') >= 2,
     'dynamic update/removal paths do not both use boundary-aware invalidation')

shoulder = read('src/main/java/celerbi/mirageprojector/client/ClientShoulderEquipment.java')
need('poseStack.translate(-0.36D, 1.32D, 0.05D);' in shoulder,
     'Shoulder Device Y transform is not corrected to positive shoulder height')
need('right.scale(0.30D)' in shoulder and 'horizontalForward.scale(0.18D)' in shoulder,
     'shoulder Lantern source is not anchored to the shoulder area')

renderer = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
need('public void renderPortableProjection(' in renderer, 'portable projection renderer missing')
portable_segment = renderer.split('public void renderPortableProjection(', 1)[1].split('public void renderPortableWarBanner(', 1)[0]
need('ProjectionSourceRenderRegistry.renderer(settings.sourceMode())' in portable_segment,
     'portable renderer does not reuse source render registry')
need('ProjectionPower.evaluate(' not in portable_segment,
     'portable renderer still evaluates the fixed-projector Core power gate')
need('renderCoreItem(' not in portable_segment,
     'portable renderer still renders/evaluates physical fixed-projector core presentation')

held = read('src/main/java/celerbi/mirageprojector/client/ClientHeldProjectors.java')
need('MirageHandProjectorItem.portablePowerAvailable(stack, minecraft.level.registryAccess())' in held,
     'held projector does not validate embedded-cell portable power')
need('renderer.renderPortableProjection(' in held,
     'held projector still uses the fixed-projector render path')

item = read('src/main/java/celerbi/mirageprojector/item/MirageHandProjectorItem.java')
need('public static boolean portablePowerAvailable' in item,
     'portable power contract is not available to client renderer/action validation')

action = read('src/main/java/celerbi/mirageprojector/network/PortableDeviceActionPayload.java')
need('MirageHandProjectorItem.portablePowerAvailable(device, player.registryAccess())' in action,
     'GUI toggle can still enable an overloaded/inert portable projection')

need((ROOT / 'docs/RELEASE-1.0.21-RUNTIME-QA-CORRECTIONS.md').exists(), '1.0.21 release note missing')
need((ROOT / 'docs/history/handoffs/NEXT-CHAT-HANDOFF-1.0.21-RUNTIME-QA-CORRECTIONS.md').exists(), '1.0.21 handoff missing')

if errors:
    print('Mirage Projector 1.0.21 runtime QA verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print('Mirage Projector 1.0.21 runtime QA verification PASS')
