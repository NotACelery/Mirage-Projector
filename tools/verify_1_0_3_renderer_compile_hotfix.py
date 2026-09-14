#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def need(cond, msg):
    if not cond:
        errors.append(msg)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

props = read('gradle.properties')
renderer = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
settings = read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')

need('mod_version=1.0.3' in props, 'version is not 1.0.3')
need('NETWORK_PROTOCOL = "28"' in main, 'compile hotfix unexpectedly changed protocol 28')
need('SERIALIZATION_VERSION = 3' in settings, 'compile hotfix unexpectedly changed ProjectionSettings format 3')

multi_start = renderer.find('private static void renderMultiSourceImageLayout(')
multi_end = renderer.find('private static FaceSize fitFaceToCell', multi_start)
need(multi_start >= 0 and multi_end > multi_start, 'multi-source image renderer method not found')
if multi_start >= 0 and multi_end > multi_start:
    multi = renderer[multi_start:multi_end]
    declaration = 'float bottom = physicalTop(blockEntity) + settings.liftPixels() * PIXEL + bob;'
    use = 'isCameraOnFrontSide(blockEntity, settings, angle, bottom)'
    placement = 'applyProjectionPlacement(blockEntity, settings, poseStack, bottom, angle)'
    need(multi.count(declaration) == 1, 'multi-source renderer must declare bottom exactly once')
    need(use in multi and placement in multi, 'multi-source renderer no longer reuses bottom for front/back and placement')
    need(multi.find(declaration) < multi.find(use), 'multi-source renderer uses bottom before declaration')

image_start = renderer.find('private static void renderImage(')
image_end = renderer.find('private static boolean isCameraOnFrontSide(', image_start)
need(image_start >= 0 and image_end > image_start, 'single-image renderer method not found')
if image_start >= 0 and image_end > image_start:
    image = renderer[image_start:image_end]
    declaration = 'float bottom = physicalTop(blockEntity) + settings.liftPixels() * PIXEL + bob;'
    need(image.count(declaration) == 1, 'renderImage must declare bottom exactly once')
    need(image.find(declaration) < image.find('isCameraOnFrontSide(blockEntity, settings, angle, bottom)'),
         'renderImage front/back check uses bottom before declaration')
    need(image.find(declaration) < image.find('applyProjectionPlacement(blockEntity, settings, poseStack, bottom, angle)'),
         'renderImage placement uses bottom before declaration')

if errors:
    print('Mirage Projector 1.0.3 renderer compile-hotfix verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.3 renderer compile-hotfix verification PASS')
