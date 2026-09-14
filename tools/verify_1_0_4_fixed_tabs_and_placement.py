#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors=[]
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def need(c,m):
    if not c: errors.append(m)

props=read('gradle.properties')
main=read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
settings=read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')
transform=read('src/main/java/celerbi/mirageprojector/ProjectionTransform.java')
spacing=read('src/main/java/celerbi/mirageprojector/PrismProjectionSpacing.java')
screen=read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
menu=read('src/main/java/celerbi/mirageprojector/menu/MirageProjectorMenu.java')
renderer=read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
clearance=read('src/main/java/celerbi/mirageprojector/client/ProjectionClearance.java')
responsive=read('src/main/java/celerbi/mirageprojector/client/ResponsiveContainerScreen.java')
power=read('src/main/java/celerbi/mirageprojector/ProjectionPower.java')

need('mod_version=1.0.4' in props,'version is not 1.0.4')
need('NETWORK_PROTOCOL = "28"' in main,'protocol unexpectedly changed')
need('SERIALIZATION_VERSION = 3' in settings,'settings format unexpectedly changed')
need('withPlacement(int prismDistancePixels, float tiltDegrees)' in settings,'placement API still contains Vertical Offset')
need('Mth.clamp(liftPixels + Math.max(0, verticalOffsetPixels)' in settings,'positive legacy Vertical Offset is not migrated into Lift')
need('positive values migrate into Lift, then zero' in settings,'legacy vertical slot is not documented')
need('verticalOffsetPixels' not in transform,'ProjectionTransform still contains Vertical Offset')
need('MAX_TILT_DEGREES = 90' in settings,'Tilt is not ±90°')

need('COLLISION_MARGIN_PIXELS = 0' in spacing,'Prism tight baseline still contains artificial margin')
need('minimumExtraDistancePixels' in spacing,'Prism minimum extra-distance helper missing')
need('effectiveExtraDistancePixels' in spacing,'Prism effective extra-distance helper missing')
need('absoluteDistancePixelsFromExtra' in spacing,'Prism UI-extra to absolute-radius mapping missing')
need('DISTANCE_PIXELS_PER_PU = 64' in spacing,'Prism small PU distance rule changed')
need('PrismProjectionSpacing.powerCost(safe)' in power,'Prism distance is not charged')
need('applyPrismCarouselPlacement' in renderer and 'Axis.YP.rotationDegrees(carouselYawDegrees)' in renderer,'Prism carousel rotation contract missing')
need('verticalOffsetPixels()' not in renderer,'renderer still applies Vertical Offset separately from Lift')
need('verticalOffsetPixels()' not in clearance,'clearance still applies Vertical Offset separately from Lift')

need('imageHeight = 412;' in screen,'main screen height is not 412')
need('CORE_SLOT_Y = 200' in menu and 'PLAYER_INV_Y = 294' in menu,'main fixed-region slot anchors do not match compact layout')
for tab in ('GEOMETRY','PLACEMENT','ROTATION','FLOATING','APPEARANCE'):
    need(f'SettingsTab.{tab}' in screen,f'missing {tab} settings tab')
need('refreshSettingsTabVisibility' in screen,'mutually-exclusive settings-tab visibility controller missing')
need('verticalOffsetSlider' not in screen and 'offset_vertical' not in screen,'Vertical Offset still exposed in screen')
need('PrismProjectionSpacing.minimumExtraDistancePixels' in screen,'Prism Distance slider is not expressed as extra spacing')
need('PrismProjectionSpacing.absoluteDistancePixelsFromExtra' in screen,'Prism Distance slider does not map back to safe absolute radius')
need('.withPlacement(distanceOffsetPixels, tiltDegrees)' in screen,'Apply path does not persist Lift/Prism Distance/Tilt model')
need('maxContentScroll()' in responsive and 'renderResponsiveScrollbar' in responsive,'responsive fallback scrollbar missing')
# 1080p GUI scale 2 gives 540 GUI px; viewport keeps 8px margin on each edge.
need(412 <= 540 - 16,'main screen would still overflow at 1080p GUI Scale 2')

for lang in ('en_us','es_es','es_cl'):
    d=json.loads((ROOT/f'src/main/resources/assets/mirage_projector/lang/{lang}.json').read_text(encoding='utf-8'))
    need('gui.mirage_projector.offset_vertical' not in d,f'{lang} still contains Vertical Offset UI label')
    need('tooltip.mirage_projector.offset_vertical' not in d,f'{lang} still contains Vertical Offset tooltip')
    for key in ('gui.mirage_projector.tab.geometry','gui.mirage_projector.tab.placement','gui.mirage_projector.tab.rotation','gui.mirage_projector.tab.floating','gui.mirage_projector.tab.appearance','gui.mirage_projector.offset_distance'):
        need(key in d,f'{lang} missing {key}')
    need('+%s px' in d['gui.mirage_projector.offset_distance'],f'{lang} Prism Distance does not communicate extra spacing')

if errors:
    print('Mirage Projector 1.0.4 fixed-tab/placement verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print('Mirage Projector 1.0.4 fixed-tab/placement verification PASS')
