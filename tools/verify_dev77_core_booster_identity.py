from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
checks=[]

def need(path, text, label):
    data=(root/path).read_text(encoding='utf-8')
    ok=text in data
    checks.append((label,ok))

def forbid(path,text,label):
    data=(root/path).read_text(encoding='utf-8')
    checks.append((label,text not in data))

need(Path('src/main/java/celerbi/mirageprojector/item/CoreBoosterItem.java'), 'item.mirage_projector.core_booster.loaded', 'dynamic loaded Core Booster name')
need(Path('src/main/java/celerbi/mirageprojector/crying/BeaconRelayState.java'), 'reflectedStaticRangeDeltaTier', 'material-specific static range helper')
need(Path('src/main/java/celerbi/mirageprojector/crying/BeaconRelayState.java'), 'reflectedDetourExtraCostUnits', 'diffusion/focus detour helper')
need(Path('src/main/java/celerbi/mirageprojector/crying/BeaconRelayState.java'), 'case DIAMOND -> 0.08F', 'Diamond beam no longer generic width')
need(Path('src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java'), 'spec.detourExtraCostUnits()', 'source profile consumes Booster detour identity')
need(Path('src/main/java/celerbi/mirageprojector/light/engine/MirageLightEngine.java'), 'int minChunkX = Math.floorDiv(origin.getX() - radius, 16);', 'allocation-free dependency readiness loop')
for lang in ('en_us.json','es_cl.json','es_es.json'):
    need(Path('src/main/resources/assets/mirage_projector/lang')/lang, 'core_booster.field_effect.glass', f'{lang} field-effect localization')

failed=[label for label,ok in checks if not ok]
for label,ok in checks:
    print(('PASS' if ok else 'FAIL'), label)
if failed:
    raise SystemExit('dev.77 verification failed: '+', '.join(failed))
print('dev.77 Core Booster identity verification PASS')
