#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors=[]

def need(cond,msg):
    if not cond: errors.append(msg)

def read(rel):
    return (ROOT/rel).read_text(encoding='utf-8')

main_screen = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
table_screen = read('src/main/java/celerbi/mirageprojector/client/MirageTableProjectorScreen.java')
client_events = read('src/main/java/celerbi/mirageprojector/client/ClientEvents.java')
renderer = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
logic = read('src/main/java/celerbi/mirageprojector/client/MirageTableProjectorLogic.java')


be = read('src/main/java/celerbi/mirageprojector/blockentity/MirageProjectorBlockEntity.java')
menu = read('src/main/java/celerbi/mirageprojector/menu/MirageProjectorMenu.java')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
need('buffer.writeVarInt(chassisProfile().ordinal());' in be,
     'main menu opening data does not publish authoritative chassis identity')
need('initialChassisProfile = readChassis(buffer.readVarInt());' in menu
     and 'return initialChassisProfile;' in menu,
     'main menu screen routing still depends on client BlockEntity timing')
need('NETWORK_PROTOCOL = "43"' in main,
     'dedicated Table menu-opening transport did not advance protocol to 43')

need('class MirageTableProjectorScreen extends ResponsiveContainerScreen<MirageProjectorMenu>' in table_screen,
     'dedicated Table screen missing')
need('ClientEvents::createMirageProjectorScreen' in client_events
     and 'ResponsiveContainerScreen<MirageProjectorMenu> createMirageProjectorScreen' in client_events
     and 'menu.chassisProfile() == ProjectionChassisProfile.TABLE' in client_events
     and 'new MirageTableProjectorScreen(menu, inventory, title)' in client_events,
     'Table menu is not routed through the compile-safe dedicated-screen factory')
need('new MirageProjectorScreen(menu, inventory, title)' in client_events,
     'non-Table chassis lost the canonical fixed-projector screen')
need('event.register(ModMenus.MIRAGE_PROJECTOR.get(), (menu, inventory, title) ->' not in client_events,
     'generic-ambiguous conditional screen lambda returned to ClientEvents')

# GUI contract must remain 1:1 for layout and user-visible controls.
for token in (
    'imageWidth = 416;', 'imageHeight = 412;', 'sourceButtonWidth = 94;', 'sourceGap = 5;',
    'gui.mirage_projector.workspace.image_short', 'gui.mirage_projector.workspace.item_short',
    'gui.mirage_projector.workspace.entity_short', 'gui.mirage_projector.workspace.banner_short',
    'gui.mirage_projector.apply', 'gui.mirage_projector.cancel',
    'tableXOffsetSlider', 'tableZOffsetSlider', 'gui.mirage_projector.table.offset_y',
    'rotationButton', 'rotationPeriodSlider', 'directionButton', 'orientationButton',
    'floatingButton', 'floatAmplitudeSlider', 'lightingButton', 'ghostSlider', 'tintButton',
):
    need(token in main_screen and token in table_screen, f'Table GUI drifted from canonical control/layout token: {token}')

need('public final class MirageTableProjectorLogic' in logic, 'Table runtime logic class missing')
need('rotationY((float) Math.toRadians(finalYawDegrees))' in logic,
     'Table planar normal does not start from exact render yaw')
need('rotationX((float) Math.toRadians(-90.0F))' in logic,
     'Table planar normal/render does not share the horizontal -90 degree flatten transform')
need('world.mul(new Quaternionf(orientation.x(), orientation.y(), orientation.z(), orientation.w()))' in logic,
     'Table planar normal does not include the exact persisted Tilt quaternion')
need('world.transform(normal);' in logic,
     'Table visibility does not derive its normal from the exact render quaternion chain')
need('applyPlanarPlacement' in logic and 'applyVolumetricPlacement' in logic,
     'Table does not own separate planar/volumetric placement paths')
need('renderBoundingBox' in logic and 'horizontalOffsetPixels()' in logic and 'verticalOffsetPixels()' in logic,
     'Table does not own an offset-aware render/culling envelope')

need('renderTableProjectorRuntime(' in renderer,
     'main BER lacks dedicated Table runtime dispatch')
need('if (chassis == ProjectionChassisProfile.TABLE)' in renderer,
     'Table does not leave the canonical fixed-projector runtime early')
need('MirageTableProjectorLogic.isCameraOnFrontSide' in renderer,
     'Table front/back still uses canonical upright logic')
need('MirageTableProjectorLogic.applyPlanarPlacement' in renderer,
     'Table planar placement not delegated to dedicated logic')
need('MirageTableProjectorLogic.applyVolumetricPlacement' in renderer,
     'Table volumetric placement not delegated to dedicated logic')
need('return MirageTableProjectorLogic.renderBoundingBox(blockEntity);' in renderer,
     'Table render bounds not delegated to dedicated logic')
need('applyTableSurfaceOffsets' not in renderer,
     'legacy shared Table offset helper still exists in canonical renderer')

if errors:
    print('Mirage Projector 1.0.32 dedicated Table runtime verification FAILED')
    for e in errors: print(' -', e)
    raise SystemExit(1)
print('Mirage Projector 1.0.32 dedicated Table runtime verification PASS')
