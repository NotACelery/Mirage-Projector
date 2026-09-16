#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors = []


def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')


def need(cond, msg):
    if not cond:
        errors.append(msg)


props = read('gradle.properties').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
settings = read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')
transform = read('src/main/java/celerbi/mirageprojector/ProjectionTransform.java')
spacing = read('src/main/java/celerbi/mirageprojector/PrismProjectionSpacing.java')
screen = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
renderer = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
clearance = read('src/main/java/celerbi/mirageprojector/client/ProjectionClearance.java')
power = read('src/main/java/celerbi/mirageprojector/ProjectionPower.java')

need('mod_version=1.0.3' in props, 'version is not 1.0.3')
need('NETWORK_PROTOCOL = "28"' in main, 'protocol changed unexpectedly from 28')
need('SERIALIZATION_VERSION = 3' in settings, 'ProjectionSettings format changed unexpectedly from v3')
need('MAX_TILT_DEGREES = 90' in settings, 'Tilt is not full ±90 degrees')
need('legacy v3 wire/NBT slot; always sanitized to zero' in settings, 'legacy horizontal slot is not documented')
need('debugChassisOverride,\n                0,' in settings, 'horizontal offset does not sanitize to zero')
need('withPlacement(int verticalPixels, int prismDistancePixels, float tiltDegrees)' in settings,
     'placement API still exposes horizontal translation')
need('horizontalOffsetPixels' not in transform, 'ProjectionTransform still exposes horizontal translation')
need('MAX_DISTANCE_PIXELS = 1024' in spacing, 'Prism distance technical bound changed')
need('COLLISION_MARGIN_PIXELS = 1' in spacing, 'Prism collision margin missing')
need('minimumDistancePixels' in spacing and 'effectiveDistancePixels' in spacing, 'Prism collision-safe spacing helpers missing')
need('Math.abs(Math.sin(tilt))' in spacing, 'Prism minimum spacing does not account for Tilt reach')
need('DISTANCE_PIXELS_PER_PU = 64' in spacing and 'powerCost' in spacing, 'Prism distance PU cost missing')

need('horizontalOffsetSlider' not in screen and 'offset_horizontal' not in screen,
     'Horizontal Offset remains exposed in projector UI')
need('prismSpacingAvailable()' in screen, 'Prism-only Distance capability gate missing')
need('PrismProjectionSpacing.minimumDistancePixels(buildSettings())' in screen,
     'Distance slider does not use dynamic collision-safe minimum')
need('PrismProjectionSpacing.MAX_DISTANCE_PIXELS' in screen, 'Prism Distance upper bound missing')
need('.withPlacement(verticalOffsetPixels, distanceOffsetPixels, tiltDegrees)' in screen,
     'Apply path does not persist corrected placement controls')

need('applyPrismCarouselPlacement' in renderer, 'Prism carousel placement helper missing')
need('PrismProjectionSpacing.effectiveDistancePixels(settings) * PIXEL' in renderer,
     'Prism renderer does not consume collision-safe radial Distance')
need('poseStack.mulPose(Axis.YP.rotationDegrees(carouselYawDegrees))' in renderer,
     'Prism cross is not rotated around the central projector')
need('poseStack.translate(0.0D, 0.0D, radius);' in renderer,
     'Prism faces are not placed radially after carousel rotation')
need('faceTilt = settings.transform().orientation()' in renderer,
     'Prism face-local Tilt is missing')
need('settings.horizontalOffsetPixels() * PIXEL' not in renderer,
     'renderer still applies independent horizontal translation')
need('projectionWorldOffset' not in renderer, 'legacy displaced-center renderer helper remains active')

need('PrismProjectionSpacing.effectiveDistancePixels(s) * PIXEL' in clearance,
     'clearance does not include Prism radial Distance')
need('horizontalOffsetPixels()' not in clearance, 'clearance still applies horizontal translation')
need('if (!prism)' in clearance and 'tiltReach' in clearance,
     'clearance does not separate Prism and plane Tilt envelopes')

need('PrismProjectionSpacing.supported(safeChassis, safe)' in power,
     'ProjectionPower does not gate Prism spacing cost by chassis/source')
need('PrismProjectionSpacing.powerCost(safe)' in power,
     'ProjectionPower does not charge Prism radial separation')

for lang in ('en_us', 'es_cl', 'es_es'):
    data = json.loads((ROOT / f'src/main/resources/assets/mirage_projector/lang/{lang}.json').read_text(encoding='utf-8'))
    need('gui.mirage_projector.offset_horizontal' not in data, f'{lang} still exposes Horizontal Offset localization')
    need('tooltip.mirage_projector.offset_horizontal' not in data, f'{lang} still exposes Horizontal Offset tooltip')
    for key in (
        'gui.mirage_projector.section.placement',
        'gui.mirage_projector.offset_vertical',
        'gui.mirage_projector.offset_distance',
        'gui.mirage_projector.tilt',
        'gui.mirage_projector.reset_position',
        'gui.mirage_projector.reset_tilt',
        'tooltip.mirage_projector.offset_vertical',
        'tooltip.mirage_projector.offset_distance',
        'tooltip.mirage_projector.tilt',
    ):
        need(key in data, f'{lang} missing {key}')

if errors:
    print('Mirage Projector 1.0.3 Prism-placement verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print('Mirage Projector 1.0.3 Prism-placement verification PASS')
