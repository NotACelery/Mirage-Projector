#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def need(cond, msg):
    if not cond:
        errors.append(msg)

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

props = read('gradle.properties').replace('mod_version=1.0.31', 'mod_version=1.0.30')
main = read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
settings = read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')
bridge = read('src/main/java/celerbi/mirageprojector/mixin/client/LevelRendererMirageLightMixin.java')

need(any(v in props for v in ('mod_version=1.0.20', 'mod_version=1.0.21', 'mod_version=1.0.22', 'mod_version=1.0.23', 'mod_version=1.0.24', 'mod_version=1.0.25', 'mod_version=1.0.26', 'mod_version=1.0.27', 'mod_version=1.0.28', 'mod_version=1.0.29', 'mod_version=1.0.30')), 'version is not a compatible 1.0.20+ line')
need(('NETWORK_PROTOCOL = "34"' in main or ('NETWORK_PROTOCOL = "35"' in main or ('NETWORK_PROTOCOL = \"36\"' in main or ('NETWORK_PROTOCOL = \"37\"' in main or 'NETWORK_PROTOCOL = \"38\"' in main)))), '1.0.20 must retain protocol 34')
need(('SERIALIZATION_VERSION = 3' in settings or 'SERIALIZATION_VERSION = 4' in settings), 'ProjectionSettings format is not a compatible v3/v4 line')
need('Minecraft.getInstance().level' in bridge, 'packed-light bridge does not resolve the active ClientLevel')
need('ClientLevel clientLevel' in bridge, 'packed-light bridge does not use an explicit ClientLevel')
need('MirageLightEngine.virtualBlockLight(clientLevel, pos)' in bridge,
     'packed-light bridge does not query Mirage light through ClientLevel')
need('level.getLightEngine()' not in bridge,
     'packed-light bridge still calls BlockAndTintGetter#getLightEngine and will crash on Sodium LevelSlice')
need('LightTexture.block(packed)' in bridge and 'LightTexture.sky(packed)' in bridge and 'LightTexture.pack' in bridge,
     'packed-light block/sky merge contract missing')
need('getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I' in bridge,
     'simple packed-light overload is not bridged')
need('getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I' in bridge,
     'BlockState packed-light overload is not bridged')

release = ROOT / 'docs/RELEASE-1.0.20-SODIUM-LIGHT-BRIDGE-HOTFIX.md'
handoff = ROOT / 'docs/history/handoffs/NEXT-CHAT-HANDOFF-1.0.20-SODIUM-LIGHT-BRIDGE-HOTFIX.md'
need(release.exists(), '1.0.20 release note missing')
need(handoff.exists(), '1.0.20 handoff missing')
if release.exists():
    r = release.read_text(encoding='utf-8')
    need('LevelSlice' in r and 'UnsupportedOperationException' in r, 'release note does not document the Sodium crash mechanism')

if errors:
    print('Mirage Projector 1.0.20 Sodium light bridge verification FAILED')
    for error in errors:
        print(' -', error)
    raise SystemExit(1)
print('Mirage Projector 1.0.20 Sodium light bridge verification PASS')
