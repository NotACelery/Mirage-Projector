#!/usr/bin/env python3
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
mods_toml = ROOT / 'src/main/templates/META-INF/neoforge.mods.toml'
logo = ROOT / 'src/main/resources/logo.png'
errors = []

def need(cond, msg):
    if not cond:
        errors.append(msg)

need(mods_toml.exists(), 'missing neoforge.mods.toml template')
if mods_toml.exists():
    text = mods_toml.read_text()
    need('logoFile="logo.png"' in text, 'mods.toml missing logoFile="logo.png"')
    need('logoBlur=false' in text, 'mods.toml missing logoBlur=false')

need(logo.exists(), 'missing src/main/resources/logo.png')
if logo.exists():
    img = Image.open(logo)
    need(img.format == 'PNG', f'logo.png is not PNG: {img.format}')
    w, h = img.size
    need((w, h) == (880, 776), f'logo.png unexpected dimensions: {(w, h)}')

if errors:
    print('dev.82 mod logo integration verification FAILED')
    for e in errors:
        print(' -', e)
    raise SystemExit(1)
print('dev.82 mod logo integration verification PASS')
