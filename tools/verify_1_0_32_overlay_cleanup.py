#!/usr/bin/env python3
"""Protect the 1.0.32 overlay-upgrade cleanup contract for renamed Flashlight sources."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def need(cond, msg):
    if not cond:
        errors.append(msg)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

cleanup = read('CLEAN-MIRAGE-PROJECTOR.bat')

# The final tree itself must contain only the current runtime identities.
need((ROOT / 'src/main/java/celerbi/mirageprojector/client/ClientHeldFlashlights.java').exists(),
     'current ClientHeldFlashlights source missing')
need((ROOT / 'src/main/java/celerbi/mirageprojector/item/MirageFlashlightItem.java').exists(),
     'current MirageFlashlightItem source missing')
need(not (ROOT / 'src/main/java/celerbi/mirageprojector/client/ClientHeldLanterns.java').exists(),
     'obsolete ClientHeldLanterns source shipped in current tree')
need(not (ROOT / 'src/main/java/celerbi/mirageprojector/item/MirageLanternItem.java').exists(),
     'obsolete MirageLanternItem source shipped in current tree')

# Copy-over upgrades on Windows leave deleted files behind, so pre-build cleanup
# must remove the two 1.0.31 source names before javac sees both generations.
for tombstone in (
    r'src\main\java\celerbi\mirageprojector\client\ClientHeldLanterns.java',
    r'src\main\java\celerbi\mirageprojector\item\MirageLanternItem.java',
):
    need(tombstone in cleanup, f'cleanup missing overlay tombstone: {tombstone}')

# The previous CALL :delete_file helper produced Windows "label not found"
# diagnostics during build; 1.0.32 intentionally uses an inline FOR cleanup.
need('call :delete_file' not in cleanup.lower(), 'cleanup still calls fragile :delete_file helper')
need('\n:delete_file' not in cleanup.lower().replace('\r', ''), 'obsolete :delete_file helper label still present')
need('for %%F in (' in cleanup, 'cumulative inline cleanup loop missing')
need('cumulative pre-build cleanup - 1.0.32' in cleanup, 'cleanup diagnostic version is not 1.0.32')

if errors:
    print('Mirage Projector 1.0.32 overlay cleanup verification FAILED')
    for e in errors:
        print(' -', e)
    raise SystemExit(1)

print('Mirage Projector 1.0.32 overlay cleanup verification PASS')
