#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors=[]
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def need(cond,msg):
    if not cond: errors.append(msg)

props=read('gradle.properties')
main=read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
be=read('src/main/java/celerbi/mirageprojector/blockentity/MirageProjectorBlockEntity.java')
menu=read('src/main/java/celerbi/mirageprojector/menu/MirageProjectorMenu.java')
screen=read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
codex=read('src/main/java/celerbi/mirageprojector/client/ScanCodexScreen.java')
profile=read('src/main/java/celerbi/mirageprojector/ProjectionChassisProfile.java')
table_block=read('src/main/java/celerbi/mirageprojector/block/MirageTableProjectorBlock.java')
scan_data=read('src/main/java/celerbi/mirageprojector/entity/EntityScanData.java')
entity_state=read('src/main/java/celerbi/mirageprojector/entity/EntityProjectionState.java')
entity_factory=read('src/main/java/celerbi/mirageprojector/client/EntityProjectionClientEntityFactory.java')
entity_screen=read('src/main/java/celerbi/mirageprojector/client/EntityProjectorScreen.java')
entity_payload=read('src/main/java/celerbi/mirageprojector/network/EntityWorkspaceActionPayload.java')
settings=read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')
renderer=read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
portable_item=read('src/main/java/celerbi/mirageprojector/item/MirageHandProjectorItem.java')
portable_menu=read('src/main/java/celerbi/mirageprojector/menu/PortableDeviceMenu.java')
portable_screen=read('src/main/java/celerbi/mirageprojector/client/PortableDeviceScreen.java')
portable_action=read('src/main/java/celerbi/mirageprojector/network/PortableDeviceActionPayload.java')
networking=read('src/main/java/celerbi/mirageprojector/network/ModNetworking.java')

need(any(v in props for v in ('mod_version=1.0.31', 'mod_version=1.0.32')), 'version is not a compatible 1.0.31+ line')
need(any(v in main for v in ('NETWORK_PROTOCOL = "42"', 'NETWORK_PROTOCOL = "43"')), 'protocol is not a compatible 42+ line')
need('buffer.writeBoolean(projectionEnabled);' in be, 'main menu does not publish projection-enabled state')
need('initialProjectionEnabled = buffer.readBoolean();' in menu, 'main menu does not consume projection-enabled state')
need(('buffer.writeVarInt(chassisProfile().ordinal());' in be and 'initialChassisProfile = readChassis(buffer.readVarInt());' in menu)
     or ('NETWORK_PROTOCOL = "42"' in main or 'NETWORK_PROTOCOL = "43"' in main),
     'later Table-specific screen routing lacks authoritative chassis opening data')
need('selectedSourceMode = base.sourceMode();' in screen and 'projectionEnabled = menu.initialProjectionEnabled();' in screen,
     'main screen still depends on stale client BE source state')
need('return selectedSourceMode;' in screen, 'main screen source mode is not menu-snapshot driven')
need('onClose();' in screen[screen.find('gui.mirage_projector.cancel'):screen.find('gui.mirage_projector.cancel')+700],
     'Cancel does not close after reverting')
need('y + 40' in screen and 'section(graphics, x + 8, y + 26' in screen and 'section.sources"), 14, 29' in screen,
     'main header/source padding rebuild missing')

modes={
    'OpenImageWorkspacePayload.java':'IMAGE',
    'OpenItemWorkspacePayload.java':'ITEM',
    'OpenEntityWorkspacePayload.java':'ENTITY',
    'OpenBannerWorkspacePayload.java':'BANNER',
}
for fn,mode in modes.items():
    text=read('src/main/java/celerbi/mirageprojector/network/'+fn)
    need(f'activateProjectionSource(ProjectionSettings.SourceMode.{mode})' in text,
         f'{mode} workspace does not atomically activate its source server-side')
    activation=text.find(f'activateProjectionSource(ProjectionSettings.SourceMode.{mode})')
    opening=text.find('openMenu(provider')
    need(activation >= 0 and opening > activation, f'{mode} workspace opens before source activation')

for name in ('Item','Entity','Banner'):
    text=read(f'src/main/java/celerbi/mirageprojector/menu/{name}ProjectorMenu.java')
    need('ProjectionSettings initialSettings' in text and 'boolean initialProjectionEnabled' in text,
         f'{name} workspace lacks authoritative initial state snapshot')
    need('initialSettings = ProjectionSettings.read(buffer);' in text and 'initialProjectionEnabled = buffer.readBoolean();' in text,
         f'{name} workspace client does not decode authoritative state')
image_menu=read('src/main/java/celerbi/mirageprojector/menu/ImageProjectorMenu.java')
need('boolean initialProjectionEnabled' in image_menu and 'initialProjectionEnabled = buffer.readBoolean();' in image_menu,
     'Image workspace lacks projection-enabled snapshot')

for name in ('Item','Entity','Banner','Image'):
    text=read(f'src/main/java/celerbi/mirageprojector/client/{name}ProjectorScreen.java')
    need('menu.initialProjectionEnabled()' in text, f'{name} screen still reads live/stale client BE enabled state')
    need('menu.initialSettings().sourceMode()' in text, f'{name} screen still reads live/stale client BE source mode')

need('TABLE("Table"' in profile and 'PlacementCapability.LIFT' in profile and 'PlacementCapability.TILT' in profile,
     'Table chassis contract missing')
need('class MirageTableProjectorBlock extends MirageProjectorBlock' in table_block,
     'Table no longer inherits canonical projector block behavior')
need('createMenu(int containerId, Inventory inventory, Player player)' in be and 'new MirageProjectorMenu(containerId, inventory, this)' in be,
     'Table/base chassis no longer share canonical main menu path')
need('chassis != ProjectionChassisProfile.WALL' in read('src/main/java/celerbi/mirageprojector/ProjectionSourceRegistry.java'),
     'built-in non-Wall source compatibility contract missing')

need('renderCodexCanvas(graphics, mouseX, mouseY, partialTick);\n        super.render' in codex,
     'Codex does not explicitly draw its canvas before widgets')
renderbg=codex[codex.find('protected void renderBg'):codex.find('private void renderCodexCanvas')]
need('renderCodexCanvas(' not in renderbg, 'Codex canvas is still drawn a second time from renderBg')
need('public void renderBackground' in codex and 'physical Codex overlays the live world' in codex,
     'Codex no-blur/no-dim contract missing')

# Recovered 1.0.31 completion wave: Player scans are skin/profile-only.
need('CompoundTag equipment = playerSource' in scan_data and 'new CompoundTag()' in scan_data,
     'legacy Player scans still reload equipment')
need('if (!(target instanceof Player))' in scan_data and 'stripHumanoidEquipment(entityData);' in scan_data,
     'new Player scans still capture humanoid equipment')
need('private boolean playerAllLayers = true;' in entity_state
     and 'root.putBoolean("PlayerAllLayers", playerAllLayers);' in entity_state,
     'Player Base Skin / All Layers state is not persistent')
need('state.playerAllLayers()' in entity_factory and 'DATA_PLAYER_MODE_CUSTOMISATION' in entity_factory,
     'Player layer mask is not applied by the client entity factory')
need('TOGGLE_PLAYER_LAYERS' in entity_payload and 'playerLayersButton' in entity_screen,
     'Entity Workspace Player layer toggle missing')
need('!scan.playerSource()' in entity_state and 'clearHumanoidEquipmentOnly()' in entity_state,
     'Player projection state does not reject inherited equipment')

# Table placement/pivot completion.
need('PlacementCapability.TABLE_XZ_OFFSET' in profile and 'supportsTableXzOffset()' in profile,
     'Table X/Z placement capability missing')
need('withSurfaceOffsets' in settings and 'withRotationEnabled' in settings,
     'Table offset/default-rotation helpers missing')
need('settings = settings.withRotationEnabled(false);' in be,
     'new Table projectors do not default Rotation OFF')
need('tableXOffsetSlider' in screen and 'tableZOffsetSlider' in screen
     and 'gui.mirage_projector.table.offset_y' in screen,
     'Table X/Y/Z Placement controls missing')
table_logic=read('src/main/java/celerbi/mirageprojector/client/MirageTableProjectorLogic.java')
need('applyAnchorAndOffsets' in table_logic and 'horizontalOffsetPixels()' in table_logic
     and 'verticalOffsetPixels()' in table_logic and '-size.height() * 0.5D' in renderer,
     'Table renderer lacks dedicated X/Z anchor offsets or image-center pivot')

# Hand Projector: physical Core + independent rechargeable energy + direct Image/Scale controls.
need('PORTABLE_MAX_SCALE_PIXELS = 10' in portable_item, 'Hand Projector Compact scale cap is not 10 px')
need('MirageHandProjectorCore' in portable_item and 'ProjectionCoreProfile coreProfile' in portable_item,
     'Hand Projector physical Projection Core persistence missing')
need('portableState.remove("CoreItem")' in portable_item,
     'physical Hand Projector Core can be duplicated into the portable profile')
need('coreProfile(projector, registries)' in portable_item and 'handheldEnergySource' not in portable_item,
     'portable PU evaluation still treats battery as a Core')
need('hasCore(projector)' in portable_item and 'hasEnergyCell(projector)' in portable_item,
     'portable runtime does not require both Core and energy')
need('setPortableScale' in portable_item and 'setImageSource' in portable_item,
     'portable Scale/Image server mutations missing')
need('CORE_SLOT_INDEX = 1' in portable_menu and 'MACHINE_SLOT_COUNT = 3' in portable_menu
     and 'DeviceCoreContainer' in portable_menu,
     'Portable Device menu lacks a physical Core slot')
need('SOURCE WORKSPACES' not in portable_screen or 'source_workspaces' in portable_screen,
     'Portable Device Source Workspaces presentation missing')
need('ScaleSlider' in portable_screen and 'openImagePicker' in portable_screen
     and 'ClientAssetTransport.uploadIfPresent' in portable_screen,
     'Portable Device Scale/direct Image UI missing')
need('COPY_TARGET_PROJECTOR' not in portable_action and 'copyPortableProfile' not in portable_item
     and 'copy_target' not in portable_screen,
     'obsolete Copy Target Projector flow still active')
need('PortableDeviceScalePayload.TYPE' in networking and 'PortableDeviceImagePayload.TYPE' in networking,
     'new portable Scale/Image payloads are not registered')
need((ROOT/'src/main/java/celerbi/mirageprojector/network/PortableDeviceScalePayload.java').exists(),
     'PortableDeviceScalePayload source missing')
need((ROOT/'src/main/java/celerbi/mirageprojector/network/PortableDeviceImagePayload.java').exists(),
     'PortableDeviceImagePayload source missing')

if errors:
    print('Mirage Projector 1.0.31 Table/runtime rebuild verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print('Mirage Projector 1.0.31 Table/runtime rebuild verification PASS')
