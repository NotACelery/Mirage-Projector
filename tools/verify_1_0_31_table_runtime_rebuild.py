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

need('mod_version=1.0.31' in props, 'version is not 1.0.31')
need('NETWORK_PROTOCOL = "41"' in main, 'protocol is not 41')
need('buffer.writeBoolean(projectionEnabled);' in be, 'main menu does not publish projection-enabled state')
need('initialProjectionEnabled = buffer.readBoolean();' in menu, 'main menu does not consume projection-enabled state')
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

if errors:
    print('Mirage Projector 1.0.31 Table/runtime rebuild verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print('Mirage Projector 1.0.31 Table/runtime rebuild verification PASS')
