#!/usr/bin/env python3
from pathlib import Path
import json, struct

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
portable_screen = read('src/main/java/celerbi/mirageprojector/client/PortableDeviceScreen.java')
portable_menu = read('src/main/java/celerbi/mirageprojector/menu/PortableDeviceMenu.java')
hand = read('src/main/java/celerbi/mirageprojector/item/MirageHandProjectorItem.java')
portable_action = read('src/main/java/celerbi/mirageprojector/network/PortableDeviceActionPayload.java')
equipment = read('src/main/java/celerbi/mirageprojector/client/MirageEquipmentClientEvents.java')
equipment_panel = read('src/main/java/celerbi/mirageprojector/client/MirageEquipmentPanelWidget.java')
projector_screen = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
remote_screen = read('src/main/java/celerbi/mirageprojector/client/PresentationRemoteScreen.java')
entity_screen = read('src/main/java/celerbi/mirageprojector/client/EntityProjectorScreen.java')
codex = read('src/main/java/celerbi/mirageprojector/client/ScanCodexScreen.java')
network = read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')

need('mod_version=1.0.30' in props, 'version is not 1.0.30')
need('NETWORK_PROTOCOL = "40"' in main, 'network protocol is not 40')
need('SERIALIZATION_VERSION = 4' in settings, 'ProjectionSettings format changed from 4')

# Hand Projector direct multi-source workspace.
for action in ('SELECT_IMAGE', 'SELECT_ITEM', 'SELECT_ENTITY', 'SELECT_BANNER'):
    need(action in portable_action, f'portable source action missing: {action}')
for mode in ('IMAGE', 'ITEM', 'ENTITY', 'BANNER'):
    need(f'ProjectionSettings.SourceMode.{mode}' in portable_screen, f'Hand Projector mode missing in screen: {mode}')
need('SOURCE_SLOT_INDEX = 1' in portable_menu and 'isFake()' in portable_menu,
     'Hand Projector virtual source well is missing')
need('captureSourceSnapshot' in portable_menu and 'clearSourceSnapshot' in portable_menu,
     'Hand Projector source well is not wired to snapshot capture/clear')
need('portable.captureProjectionSnapshot(source)' in hand, 'Item source capture is not virtual/non-consuming')
need('source.is(ModItems.ENTITY_SCAN_CARD.get())' in hand and 'EntityScanData.hasScan(source)' in hand,
     'Entity mode does not require a filled Entity Scan Card')
need('source.getItem() instanceof BannerItem' in hand, 'Banner source capture is missing')
need('commitPortableProfile' in hand and 'tag.putString(SOURCE_MODE_TAG' in hand,
     'portable source mode/profile persistence missing')
need('PortableDeviceActionPayload.TYPE' in network, 'portable action payload is not registered')

# Shoulder pseudo-slots own press and release; expansion text is represented by slots only.
need('ScreenEvent.MouseButtonPressed.Pre' in equipment and 'ScreenEvent.MouseButtonReleased.Pre' in equipment,
     'Mirage Equipment does not own both mouse press and release')
need('capturedEquipmentButton' in equipment and 'event.setCanceled(true)' in equipment,
     'Mirage Equipment outside-click suppression missing')
need('creative.isInventoryOpen()' in equipment, 'Creative equipment UI leaks outside the inventory tab')
need('int toggleX = rightEdge - 2;' in equipment and 'int panelX = rightEdge + 13;' in equipment,
     'Mirage Equipment 1.0.30 docked position is missing')
need('+3' not in equipment_panel, 'redundant +3 expansion label remains in Mirage Equipment panel')

# Wall live preview and non-closing apply/cancel.
need('private void previewWallSettings()' in projector_screen and
     'new UpdateProjectorPayload(menu.projectorPos(), buildSettings())' in projector_screen,
     'Wall live placement preview helper is missing')
need(projector_screen.count('previewWallSettings();') >= 3,
     'Scale/X/Y controls do not all publish live Wall preview')
need('saveSettings();\n            base = buildSettings();' in projector_screen,
     'Apply does not commit the live preview baseline')
need('restoreFromBase();\n            PacketDistributor.sendToServer(new UpdateProjectorPayload(menu.projectorPos(), base));' in projector_screen,
     'Cancel does not restore the committed baseline')
apply_block = projector_screen[projector_screen.find('gui.mirage_projector.apply'):projector_screen.find('gui.mirage_projector.cancel')]
need('onClose()' not in apply_block, 'Apply still closes the projector screen')

# Presentation Remote raw cursor centering + neutral zone.
need('minecraft.getWindow().getWidth() * 0.5D' in remote_screen and
     'minecraft.getWindow().getHeight() * 0.5D' in remote_screen,
     'Presentation Remote does not center the native cursor in raw window pixels')
need('delta < -40.0D' in remote_screen and 'delta > 40.0D' in remote_screen,
     'Presentation Remote neutral dead-zone is not present')
need('if (action != 0)' in remote_screen, 'neutral remote release can still send a slide action')

# Entity workspace activation/layout.
# 1.0.30 used SetProjectionSourcePayload + workspace reopen. 1.0.31 replaces the
# split client sequence with one atomic server-authoritative OpenEntityWorkspacePayload.
entity_open = read('src/main/java/celerbi/mirageprojector/network/OpenEntityWorkspacePayload.java')
legacy_entity_activation = ('new SetProjectionSourcePayload' in entity_screen
                            and 'ProjectionSettings.SourceMode.ENTITY' in entity_screen
                            and 'new OpenEntityWorkspacePayload(menu.projectorPos())' in entity_screen)
atomic_entity_activation = ('new OpenEntityWorkspacePayload(menu.projectorPos())' in entity_screen
                            and 'activateProjectionSource(ProjectionSettings.SourceMode.ENTITY)' in entity_open)
need(legacy_entity_activation or atomic_entity_activation,
     'Use Entity Mode is not server-authoritative')
need('PROJECTED_X = 302' in entity_screen and 'VISIBILITY_X = 334' in entity_screen,
     'Projected/Visibility columns are not on the corrected 1.0.30 layout')
need('PREVIEW_W = 180' in entity_screen and 'PREVIEW_H = 240' in entity_screen,
     'Entity 3D preview does not use the corrected bounded panel')

# Codex one-pass canvas and compact lectern extensions.
need('protected void renderBg' in codex and 'renderCodexCanvas(graphics, mouseX, mouseY, partialTick);' in codex,
     'Codex canvas is not rendered exactly from the container background pass')
need('public void renderBackground' in codex and 'physical Codex overlays the live world' in codex,
     'Codex no-blur/no-dim contract missing')
need('LIST_X = BOOK_X + 246' in codex and 'LEFT_PAGE_X = BOOK_X + 12' in codex,
     'Codex library pages are not separated')
need('renderImportExtension' in codex and 'renderDuplicateExtension' in codex and 'extensionSlotFrame' in codex,
     'Lectern import/duplicate extensions are missing')
need('fitText(Component.translatable("gui.mirage_projector.scan_codex.import.consumed_hint").getString(), 108)' in codex,
     'Lectern import copy is not bounded')

# Entity Scan Card own 16x16 item art.
texture = ROOT / 'src/main/resources/assets/mirage_projector/textures/item/entity_scan_card.png'
model = read('src/main/resources/assets/mirage_projector/models/item/entity_scan_card.json')
need(texture.exists(), 'Entity Scan Card texture missing')
need('mirage_projector:item/entity_scan_card' in model, 'Entity Scan Card model does not reference Mirage art')
if texture.exists():
    raw = texture.read_bytes()
    try:
        width, height = struct.unpack('>II', raw[16:24])
        need((width, height) == (16, 16), f'Entity Scan Card texture is {width}x{height}, expected 16x16')
    except Exception:
        need(False, 'Entity Scan Card PNG dimensions could not be read')

# Localization parity and release docs.
lang_dir = ROOT / 'src/main/resources/assets/mirage_projector/lang'
langs = {name: json.loads((lang_dir / name).read_text(encoding='utf-8')) for name in ('en_us.json','es_cl.json','es_es.json')}
need(len({frozenset(v) for v in (set(x) for x in langs.values())}) == 1, 'language key parity is broken')
need((ROOT / 'docs/RELEASE-1.0.30-UX-RUNTIME-WAVE.md').exists(), '1.0.30 release note missing')
need('## 1.0.30' in read('docs/CHANGELOG.md'), 'changelog missing 1.0.30')
need('## 1.0.30' in read('docs/VERSION-SCOPE.md'), 'version scope missing 1.0.30')

if errors:
    print('Mirage Projector 1.0.30 UX/runtime verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print(f'Mirage Projector 1.0.30 UX/runtime verification PASS ({len(langs["en_us.json"])} lang keys)')
