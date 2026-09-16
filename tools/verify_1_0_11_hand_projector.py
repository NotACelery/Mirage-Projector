#!/usr/bin/env python3
from __future__ import annotations

from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def need(condition: bool, message: str) -> None:
    if not condition:
        errors.append(message)


def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding='utf-8')

props = read('gradle.properties').replace('mod_version=1.0.32', 'mod_version=1.0.30').replace('mod_version=1.0.31', 'mod_version=1.0.30')
stabilized_1018 = any(v in props for v in ('mod_version=1.0.18', 'mod_version=1.0.19', 'mod_version=1.0.20', 'mod_version=1.0.21', 'mod_version=1.0.22', 'mod_version=1.0.23', 'mod_version=1.0.24', 'mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30'))
need(any(f'mod_version=1.0.{minor}' in props for minor in (11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30)), 'gradle.properties is not a compatible 1.0.11+ line')

items = read('src/main/java/celerbi/mirageprojector/registry/ModItems.java')
need('MIRAGE_HAND_PROJECTOR' in items, 'Mirage Hand Projector is not registered')
need('mirage_hand_projector' in items, 'mirage_hand_projector registry ID missing')

tab = read('src/main/java/celerbi/mirageprojector/registry/ModCreativeTabs.java')
need('output.accept(ModItems.MIRAGE_HAND_PROJECTOR.get())' in tab, 'Mirage Hand Projector missing from creative tab')

item = read('src/main/java/celerbi/mirageprojector/item/MirageHandProjectorItem.java')
need(('copyPortableProfile' in item) or ('captureSourceSnapshot' in item and 'setImageSource' in item),
     'portable source acquisition contract missing')
need('useOn(UseOnContext context)' in item, 'placed-projector copy interaction missing')
need('projectionEnabled' in item and 'setProjectionEnabled' in item, 'portable ON/OFF state contract missing')
need('createPortableProjector(' in item, 'portable projector reconstruction helper missing')
need('normalizePortableProjector' in item, 'portable normalization helper missing')
need('PORTABLE_MAX_OPACITY_PERCENT = 90' in item, 'portable minimum ghost/opacity cap missing')
need('ImageLayoutMode.SINGLE' in item, 'portable image single-source normalization missing')
need('portableDrainPerSecond' in item, 'portable drain contract missing')
need((('serviceCell' in item and 'insertEnergyCell' in item and 'extractEnergyCell' in item) or ('PortableDeviceMenu.open' in item and 'replaceEnergyCell' in item and 'extractEnergyCell' in item)), 'portable rechargeable-cell servicing/configuration path missing')
need(('MirageProjectorBlockEntity source' in item) or ('setImageSource' in item and 'selectSourceMode' in item),
     'portable source acquisition path missing')

client = read('src/main/java/celerbi/mirageprojector/client/ClientHeldProjectors.java')
need('renderVisiblePlayers' in client, 'held-projector world render entry missing')
need(('updateLocalHud' not in client) if stabilized_1018 else ('updateLocalHud' in client), 'held-projector HUD updater contract does not match current line')
need('MirageHandProjectorItem.createPortableProjector' in client, 'client no longer reconstructs portable projector from held item state')
need('MirageProjectorRenderer' in client, 'client held-projector path is not reusing MirageProjectorRenderer')

runtime = read('src/main/java/celerbi/mirageprojector/client/ClientRuntimeEvents.java')
need('ClientHeldProjectors.renderVisiblePlayers' in runtime, 'ClientRuntimeEvents does not render held projectors')
need(('ClientHeldProjectors.updateLocalHud' in runtime) or ('PortableDeviceMenu.open' in item and 'shouldCauseReequipAnimation' in item), 'handheld-projector feedback/configuration path missing')
need('ClientHeldProjectors.resetSession' in runtime, 'ClientRuntimeEvents does not reset handheld-projector session state')

for rel in [
    'src/main/resources/assets/mirage_projector/models/item/mirage_hand_projector.json',
    'src/main/resources/assets/mirage_projector/textures/item/mirage_hand_projector.png',
    'docs/RELEASE-1.0.11-HANDHELD-PROJECTOR.md',
    'docs/history/handoffs/NEXT-CHAT-HANDOFF-1.0.11-HANDHELD-PROJECTOR.md',
]:
    need((ROOT / rel).exists(), f'missing required 1.0.11 artifact: {rel}')

for lang in ('en_us', 'es_cl', 'es_es'):
    text = read(f'src/main/resources/assets/mirage_projector/lang/{lang}.json')
    need('item.mirage_projector.mirage_hand_projector' in text, f'hand projector item localization missing in {lang}')
    need('hud.mirage_projector.hand_projector.status' in text, f'hand projector HUD localization missing in {lang}')
    need(('tooltip.mirage_projector.hand_projector.copy' not in text and 'tooltip.mirage_projector.hand_projector.use' in text) if stabilized_1018 else ('tooltip.mirage_projector.hand_projector.copy' in text), f'hand projector tooltip localization contract mismatch in {lang}')

if errors:
    print('Mirage Projector 1.0.11 handheld projector verification FAILED')
    for err in errors:
        print(f'- {err}')
    sys.exit(1)

print('Mirage Projector 1.0.11 handheld projector verification PASS')
