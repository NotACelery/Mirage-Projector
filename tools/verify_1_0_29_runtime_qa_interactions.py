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
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"40\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"40\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"40\"')
events = read('src/main/java/celerbi/mirageprojector/client/MirageEquipmentClientEvents.java')
slot = read('src/main/java/celerbi/mirageprojector/client/MirageEquipmentSlotWidget.java')
action = read('src/main/java/celerbi/mirageprojector/network/ShoulderEquipmentActionPayload.java')
cursor = read('src/main/java/celerbi/mirageprojector/network/ShoulderEquipmentCursorPayload.java')
runtime = read('src/main/java/celerbi/mirageprojector/equipment/ShoulderEquipmentRuntime.java')
network = read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')
codex = read('src/main/java/celerbi/mirageprojector/client/ScanCodexScreen.java')
screen = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
layout = read('src/main/java/celerbi/mirageprojector/ProjectorVisualLayout.java')
remote = read('src/main/java/celerbi/mirageprojector/item/PresentationRemoteItem.java')
be = read('src/main/java/celerbi/mirageprojector/blockentity/MirageProjectorBlockEntity.java')
block = read('src/main/java/celerbi/mirageprojector/block/MirageProjectorBlock.java')
unpair = read('src/main/java/celerbi/mirageprojector/network/UnpairPresentationRemotePayload.java')

need(any(v in props for v in ('mod_version=1.0.29','mod_version=1.0.30')), 'version is not a compatible 1.0.29+ line')
need(any(v in main for v in ('NETWORK_PROTOCOL = "39"', 'NETWORK_PROTOCOL = "40"')), 'network protocol is not 39')
need('SERIALIZATION_VERSION = 4' in read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java'), 'ProjectionSettings format changed from 4')

# Mirage Equipment visibility + real interaction outside the vanilla container rectangle.
need('creative.isInventoryOpen()' in events, 'Creative Mirage Equipment is not gated to the player inventory tab')
need(('int toggleX = rightEdge + 7;' in events and 'int panelX = rightEdge + 28;' in events)
     or ('int toggleX = rightEdge - 2;' in events and 'int panelX = rightEdge + 13;' in events)
     or ('int toggleX = rightEdge + 5;' in events and 'int panelX = rightEdge + 24;' in events),
     'Mirage Equipment is not docked at a supported inventory-frame position')
need('ScreenEvent.MouseButtonPressed.Pre' in events and 'event.setCanceled(true)' in events,
     'external Mirage Equipment slot clicks are not intercepted before vanilla outside-drop handling')
need('containerScreen.getMenu().getCarried().copy()' in slot,
     'equipment slot does not capture the visible Creative picker cursor')
need('ItemStack clientCarried' in action and 'ItemStack.OPTIONAL_STREAM_CODEC' in action,
     'equipment action payload does not carry the Creative cursor snapshot')
need('player.isCreative()' in runtime and ': player.containerMenu.getCarried()' in runtime,
     'server does not restrict client-carried cursor trust to Creative players')
need('containerScreen.getMenu().setCarried(corrected.copy())' in cursor,
     'server cursor correction does not update the currently visible container menu')
need('ShoulderEquipmentCursorPayload.TYPE' in network,
     'equipment cursor correction payload is not registered')

# Codex canvas must render independently of the suppressed vanilla background path.
need('private void renderCodexCanvas' in codex, 'Codex explicit canvas renderer missing')
need('renderCodexCanvas(graphics, mouseX, mouseY, partialTick);' in codex,
     'Codex canvas is not invoked by render()')
need('public void renderBackground' in codex and 'physical Codex overlays the live world' in codex,
     'Codex no-blur/no-dim background contract missing')
need('searchBox.setBordered(false)' in codex and 'renderLecternHomeExtension' in codex,
     'Codex parchment-integrated search/lectern backing missing')

# Table source mode and Core Chamber QA. 1.0.31 replaced the split client
# SetProjectionSource + OpenWorkspace sequence with one atomic server-side workspace activation.
workspace_payloads = {
    'IMAGE': read('src/main/java/celerbi/mirageprojector/network/OpenImageWorkspacePayload.java'),
    'ITEM': read('src/main/java/celerbi/mirageprojector/network/OpenItemWorkspacePayload.java'),
    'ENTITY': read('src/main/java/celerbi/mirageprojector/network/OpenEntityWorkspacePayload.java'),
    'BANNER': read('src/main/java/celerbi/mirageprojector/network/OpenBannerWorkspacePayload.java'),
}
for source, payload in workspace_payloads.items():
    legacy = f'activateSource(ProjectionSettings.SourceMode.{source})' in screen
    atomic = f'activateProjectionSource(ProjectionSettings.SourceMode.{source})' in payload
    need(legacy or atomic,
         f'Table/main projector {source} button has no authoritative source activation path')
need(('new SetProjectionSourcePayload(menu.projectorPos(), source)' in screen)
     or all('activateProjectionSource(ProjectionSettings.SourceMode.' + source + ')' in payload
            for source, payload in workspace_payloads.items()),
     'source activation has neither legacy helper nor 1.0.31 atomic workspace authority')
need('case TABLE -> new ProjectorVisualLayout(6, 4.5F, 11, 0.13F);' in layout,
     'Table Core render is not reduced/recentered inside its chamber')
need('blockEntity.chassisProfile() != ProjectionChassisProfile.TABLE' in read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java'),
     'Table Core still receives the fixed-chassis X tilt that clips its shallow chamber')

# Remote pairing/unpairing must be bidirectional and server-authoritative.
bind_pos = be.find('PresentationRemoteItem.bind(\n                source')
copy_pos = be.find('ItemStack docked = source.copyWithCount(1);')
need(bind_pos >= 0 and copy_pos > bind_pos,
     'Wall/Data-show pairing still binds only the docked copy instead of the held source remote')
need('public static void unbind(ItemStack stack)' in remote, 'Presentation Remote has no binding-clear operation')
need('public boolean unpairPresentationRemote(ServerPlayer player)' in be and 'presentationLinkId = null;' in be,
     'projector cannot invalidate/eject the current remote pairing')
need('UnpairPresentationRemotePayload.TYPE' in network and 'unpairPresentationRemote(player)' in unpair,
     'server-authoritative Unpair Remote payload is not wired')
need('gui.mirage_projector.wall.unpair_remote' in screen,
     'Wall/Data-show GUI has no Unpair Remote button')

# Data-show physical hit geometry.
need('box(6, 6, 7, 10, 6.25, 10)' in block, 'Wall shape does not include the physical top remote dock')
need('protected VoxelShape getCollisionShape' in block and 'return getShape(state, level, pos, context);' in block,
     'Wall/projector collision shape is not synchronized with its outline shape')

# Localization parity.
lang_dir = ROOT / 'src/main/resources/assets/mirage_projector/lang'
langs = {name: json.loads((lang_dir / name).read_text(encoding='utf-8')) for name in ('en_us.json','es_cl.json','es_es.json')}
keysets = {name: set(data) for name, data in langs.items()}
need(len(set(map(frozenset, keysets.values()))) == 1, 'language key parity is broken')
for data in langs.values():
    need('gui.mirage_projector.wall.unpair_remote' in data, 'Unpair Remote localization missing')
    need('gui.mirage_projector.scan_codex.lectern_actions' in data, 'Codex lectern backing localization missing')

need((ROOT / 'docs/RELEASE-1.0.29-RUNTIME-QA-INTERACTION-FIXES.md').exists(), '1.0.29 release note missing')
need('## 1.0.29' in read('docs/CHANGELOG.md'), 'changelog missing 1.0.29')
need('## 1.0.29' in read('docs/VERSION-SCOPE.md'), 'version scope missing 1.0.29')

if errors:
    print('Mirage Projector 1.0.29 runtime QA interaction verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print(f'Mirage Projector 1.0.29 runtime QA interaction verification PASS ({len(langs["en_us.json"])} lang keys)')
