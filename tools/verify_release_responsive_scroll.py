#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []


def need(condition, message):
    if not condition:
        errors.append(message)


def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

base_rel = 'src/main/java/celerbi/mirageprojector/client/ResponsiveContainerScreen.java'
need((ROOT / base_rel).exists(), 'missing responsive container-screen base')
if (ROOT / base_rel).exists():
    base = read(base_rel)
    for token, message in [
        ('mouseScrolled(', 'responsive base missing mouse-wheel scrolling'),
        ('mouseDragged(', 'responsive base missing scrollbar dragging'),
        ('mouseReleased(', 'responsive base missing scrollbar release handling'),
        ('maxContentScroll()', 'responsive base missing overflow calculation'),
        ('widget.setY(widget.getY() + delta)', 'responsive base does not move widgets with the container'),
        ('topPos = newTop', 'responsive base does not move vanilla container origin/slot hitboxes'),
        ('renderResponsiveScrollbar', 'responsive base missing scrollbar renderer'),
    ]:
        need(token in base, message)

screens = [
    ('MirageProjectorScreen.java', 'MirageProjectorMenu'),
    ('ImageProjectorScreen.java', 'ImageProjectorMenu'),
    ('EntityProjectorScreen.java', 'EntityProjectorMenu'),
    ('ItemProjectorScreen.java', 'ItemProjectorMenu'),
    ('BannerProjectorScreen.java', 'BannerProjectorMenu'),
]
for filename, menu in screens:
    rel = f'src/main/java/celerbi/mirageprojector/client/{filename}'
    text = read(rel)
    need(f'extends ResponsiveContainerScreen<{menu}>' in text,
         f'{filename} is not using the responsive container-screen base')
    need('extends AbstractContainerScreen<' not in text,
         f'{filename} still directly extends AbstractContainerScreen')

image = read('src/main/java/celerbi/mirageprojector/client/ImageProjectorScreen.java')
need('protected void onContentScrolled(int deltaY)' in image and 'mapY += deltaY;' in image,
     'ImageProjectorScreen does not keep its source-bank hit map aligned while scrolling')

main = read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
need('imageHeight = 500;' in main, 'main projector screen content height changed unexpectedly')
need('debugButton' not in main, 'release Debug button returned while adding responsive scrolling')

if errors:
    print('1.0.0 responsive-scroll verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)

print('1.0.0 responsive-scroll verification PASS')
