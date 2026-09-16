#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors=[]

def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def need(c,m):
    if not c: errors.append(m)

props=read('gradle.properties').replace('mod_version=1.0.31', 'mod_version=1.0.30')
spacing=read('src/main/java/celerbi/mirageprojector/PrismProjectionSpacing.java')
screen=read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
renderer=read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
clearance=read('src/main/java/celerbi/mirageprojector/client/ProjectionClearance.java')
power=read('src/main/java/celerbi/mirageprojector/ProjectionPower.java')
settings=read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')

need('mod_version=1.0.6' in props,'version is not 1.0.6')
need('MAX_EXTRA_DISTANCE_PIXELS = 160' in spacing,'Prism user distance cap is not 160 px / 10 blocks')
need('Math.min(a.widthPixels(), b.widthPixels()) * 0.5D' in spacing,'adjacent-face tight collision floor is missing')
need('tiltDegrees < 0.0F' in spacing,'Prism spacing does not distinguish inward from outward tilt')
need('sharedInwardReach' in spacing,'inward Tilt collision reach missing')
need('clampTiltDegreesToSafeRange' in spacing,'unsafe inward Tilt rejection helper missing')
need('minimumAllowedTiltDegrees' in spacing,'dynamic Prism Tilt floor missing')
need('PrismProjectionSpacing.powerCost(safe)' in power,'Prism Distance PU surcharge missing')
need('applyPrismCarouselPlacement' in renderer,'Prism carousel placement missing')
need('MAX_TILT_DEGREES = 90' in settings,'Tilt is not full ±90°')

for tab in ('GEOMETRY','PLACEMENT','ROTATION','FLOATING'):
    need(f'SettingsTab.{tab}' in screen,f'missing {tab} tab')
need('SettingsTab.APPEARANCE' not in screen and 'appearanceTabButton' not in screen,'Appearance still exists as a separate tab')
need('setWidgetState(lightingButton, geometry, geometry)' in screen,'Lighting was not merged into Geometry')
need('setWidgetState(ghostSlider, geometry, geometry)' in screen,'Ghost was not merged into Geometry')
need('setWidgetState(tintButton, geometry, geometry)' in screen,'Tint was not merged into Geometry')
need('tabWidth = 94' in screen and 'tabGap = 5' in screen,'four-tab fixed-width layout missing')
need('PrismProjectionSpacing.MAX_EXTRA_DISTANCE_PIXELS' in screen,'screen does not cap Prism Distance at +160 px')
need('PrismProjectionSpacing.minimumAllowedTiltDegrees' in screen,'screen does not refuse collision-unsafe Tilt')
need('PrismProjectionSpacing.maximumDistancePixels' in screen,'screen does not normalize old excessive Prism distances')
need('imageHeight = 412;' in screen,'fixed main screen height changed')

need('settings.tiltDegrees() > 0.0F' in renderer and 'prismOutwardTiltReach' in renderer,'Prism BER bounds still treat inward/outward Tilt identically')
need('s.tiltDegrees() > 0.0F' in clearance and 'outwardTiltReach' in clearance,'Prism clearance bounds still treat inward/outward Tilt identically')

for lang in ('en_us','es_es','es_cl'):
    p=ROOT/f'src/main/resources/assets/mirage_projector/lang/{lang}.json'
    d=json.loads(p.read_text(encoding='utf-8'))
    need('gui.mirage_projector.tab.appearance' not in d,f'{lang} still exposes Appearance tab label')
    need('10 blocks' in d.get('tooltip.mirage_projector.offset_distance','') or '10 bloques' in d.get('tooltip.mirage_projector.offset_distance',''),f'{lang} Prism Distance tooltip lacks 10-block cap')

if errors:
    print('Mirage Projector 1.0.6 Prism compaction/tab verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print('Mirage Projector 1.0.6 Prism compaction/tab verification PASS')
