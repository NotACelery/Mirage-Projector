#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors=[]
def need(cond,msg):
    if not cond: errors.append(msg)
def read(rel): return (ROOT/rel).read_text(encoding='utf-8')

props=read('gradle.properties')
chassis=read('src/main/java/celerbi/mirageprojector/ProjectionChassisProfile.java')
res=read('src/main/java/celerbi/mirageprojector/SpecialResonanceProfile.java')
be=read('src/main/java/celerbi/mirageprojector/blockentity/MirageProjectorBlockEntity.java')
menu=read('src/main/java/celerbi/mirageprojector/menu/MirageProjectorMenu.java')
renderer=read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
screen=read('src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java')
geom=read('src/main/java/celerbi/mirageprojector/EndResonanceGeometry.java')
transfer=read('src/main/java/celerbi/mirageprojector/ProjectorStateTransfer.java')
upgrade=read('src/main/java/celerbi/mirageprojector/recipe/ProjectorUpgradeRecipe.java')
block=read('src/main/java/celerbi/mirageprojector/block/MirageProjectorBlock.java')

need(any(v in props for v in ('mod_version=1.0.33','mod_version=1.0.34')), 'mod version is not compatible with 1.0.33 foundation')
need('END_RESONANCE' in res and 'Items.DRAGON_EGG' in res, 'Dragon Egg special resonance provider missing')
need('ProjectionCoreProfile.fromStack' not in res and 'basePower' not in res and 'materialOutput' not in res, 'special resonance must not be normal PU arithmetic')
need('FIELD("Field"' in chassis and 'Anchor.FLOOR_UPRIGHT, true,' in chassis, 'Field does not expose End Resonance capability')
need('PRISM("Prism"' in chassis and chassis.count('Anchor.FLOOR_UPRIGHT, true,') == 2, 'Prism/Field capability set is not exclusive')
need('supportsEndResonance()' in chassis, 'chassis End Resonance capability accessor missing')

need('acceptsCoreStack' in be and 'SpecialResonanceProfile.fromStack(stack).supportedBy(chassisProfile())' in be,
     'BlockEntity core slot does not accept supported special resonance catalysts')
need('endResonanceRestoreSnapshot' in be and 'captureProjectionFacingState' in be and 'restoreProjectionFacingState' in be,
     'persistent restore snapshot contract missing')
need('tag.put("EndResonanceRestoreSnapshot"' in be and 'tag.contains("EndResonanceRestoreSnapshot")' in be,
     'restore snapshot is not persisted across saves/reloads')
need('snapshot.remove("CoreItem")' in be, 'physical catalyst leaked into restore snapshot')
need('merged.put("CoreItem", coreItem.serializeNBT(registries))' in be, 'restore does not preserve current physical core-slot state')
need('endResonanceLocksControls()' in be and 'if (endResonanceLocksControls())' in be,
     'normal projector mutation lock missing')
need('if (!enabled && endResonanceLocksControls())' in be,
     'TURN OFF is not blocked by resonance ownership')
need('sourceMode == null || endResonanceLocksControls()' in be,
     'normal source activation is not suspended')
need('loadingCoreState' in be, 'core load/restore recursion guard missing')

need('SpecialResonanceProfile.fromStack(stack).supportedBy(initialChassisProfile)' in menu,
     'Field/Prism menu cannot insert Dragon Egg through the physical Core slot')
need('SpecialResonanceProfile.fromStack(current).supportedBy(initialChassisProfile)' in menu,
     'quick-move does not support the special catalyst')

need('renderEndResonance(blockEntity' in renderer and 'blockEntity.endResonanceActive()' in renderer,
     'BER does not short-circuit normal projection for End Resonance')
need('END_RESONANCE_TEXTURE' in renderer and 'textures/environment/end_portal.png' in renderer,
     'End visual language texture missing')
need('renderEndResonanceField' in renderer and 'renderEndResonancePrism' in renderer,
     'Field/Prism do not own distinct resonance visuals')
need('EndResonanceGeometry.renderBounds(blockEntity)' in renderer,
     'resonance render bounds are not independent from normal projection settings')
need('FIELD_WIDTH = 2.0D' in geom and 'FIELD_HEIGHT = 3.0D' in geom,
     'Field fixed 2x3 geometry missing')
need('PRISM_WIDTH = 2.0D' in geom and 'PRISM_HEIGHT = 3.0D' in geom and 'PRISM_DEPTH = 2.0D' in geom,
     'Prism fixed 2x3x2 geometry missing')
need('MirageProjectorBlock.FACING' in geom, 'Field orientation is not chassis-facing authoritative')

need('gui.mirage_projector.end_resonance.title' in screen and 'refreshEndResonanceLock' in screen,
     'resonance-specific GUI/read-only state missing')
need('if (!endResonanceActive())' in screen and 'new SetProjectionEnabledPayload' in screen,
     'TURN OFF client feedback path no longer remains attemptable')

need('hasSpecialResonanceCatalyst' in transfer and 'return ItemStack.EMPTY;' in transfer,
     'chassis upgrade/state-copy anti-dup guard missing')
need('ProjectorStateTransfer.hasSpecialResonanceCatalyst' in upgrade,
     'upgrade recipe does not reject installed unique catalysts')
need('projector.stateForCreativeClone' in block,
     'Creative clone/pick-block can still duplicate the physical Dragon Egg')
need('state.remove("CoreItem")' in be and 'state.remove("EndResonanceRestoreSnapshot")' in be,
     'Creative clone state is not sanitized')

# No direct recipe should teach/consume the Dragon Egg.
for p in (ROOT/'src/main/resources/data/mirage_projector/recipe').glob('*.json'):
    text=p.read_text(encoding='utf-8')
    need('minecraft:dragon_egg' not in text, f'Dragon Egg exposed as normal recipe ingredient: {p.name}')

langs=[]
for locale in ('en_us','es_cl','es_es'):
    langs.append(json.loads(read(f'src/main/resources/assets/mirage_projector/lang/{locale}.json')))
need(set(langs[0]) == set(langs[1]) == set(langs[2]), 'language-key parity broken')
for key in (
    'gui.mirage_projector.end_resonance.title',
    'gui.mirage_projector.end_resonance.suspended',
    'gui.mirage_projector.end_resonance.detected',
    'gui.mirage_projector.end_resonance.aperture',
):
    need(all(key in lang for lang in langs), f'missing resonance localization key: {key}')

if errors:
    print('Mirage Projector 1.0.33 End Resonance foundation verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print(f'Mirage Projector 1.0.33 End Resonance foundation verification PASS ({len(langs[0])} lang keys)')
