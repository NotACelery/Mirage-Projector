#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors=[]

def read(rel): return (ROOT/rel).read_text(encoding='utf-8')
def need(cond,msg):
    if not cond: errors.append(msg)

settings=read('src/main/java/celerbi/mirageprojector/ProjectionSettings.java')
registry=read('src/main/java/celerbi/mirageprojector/ProjectionSourceRegistry.java')
render_registry=read('src/main/java/celerbi/mirageprojector/client/ProjectionSourceRenderRegistry.java')
renderer=read('src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java')
be=read('src/main/java/celerbi/mirageprojector/blockentity/MirageProjectorBlockEntity.java')
payload=read('src/main/java/celerbi/mirageprojector/network/SetProjectionSourcePayload.java')
transform=read('src/main/java/celerbi/mirageprojector/ProjectionTransform.java')
energy=read('src/main/java/celerbi/mirageprojector/ProjectionEnergySource.java')
power=read('src/main/java/celerbi/mirageprojector/ProjectionPower.java')
main=read('src/main/java/celerbi/mirageprojector/MirageProjector.java')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')

need('public static final class SourceMode' in settings,'SourceMode is still a closed enum')
need('public enum SourceMode' not in settings,'closed SourceMode enum still present')
for source in ('image','item','entity','banner'):
    need(f'"{source}"' in settings and 'fromNamespaceAndPath' in settings,f'missing stable namespaced source id for {source}')
need('buffer.writeUtf(s.sourceMode().serializedName(), 128)' in settings,'ProjectionSettings network still uses source ordinal')
need('SourceMode.parse(buffer.readUtf(128))' in settings,'ProjectionSettings source ID network decode missing')
need('tag.putString("SourceId"' in settings,'SourceId NBT persistence missing')
need('fromLegacyOrdinal' in settings and 'tag.contains("SourceMode")' in settings,'legacy SourceMode ordinal migration missing')
need('SERIALIZATION_VERSION = 2' in settings,'ProjectionSettings serialization version missing')
need('ProjectionSettingsVersion' in settings,'ProjectionSettings NBT format version missing')
need('sourceMode().ordinal()' not in settings,'source ordinal serialization remains in ProjectionSettings')
need('sourceMode().ordinal()' not in payload and 'writeUtf(payload.sourceMode().serializedName(), 128)' in payload,'SetProjectionSourcePayload still uses ordinal')

need('class ProjectionSourceRegistry' in registry,'common projection source registry missing')
for source in ('IMAGE','ITEM','ENTITY','BANNER'):
    need(f'ProjectionSettings.SourceMode.{source}' in registry,f'builtin {source} is not registered')
need('definitionsFor' in registry and 'SourceCompatibility' in registry,'chassis-compatible source enumeration seam missing')
need('ContentProvider' in registry,'projection content provider contract missing')
need('ProjectionSourceRegistry.contentCount' in be and 'ProjectionSourceRegistry.hasContent' in be,'block entity still hardcodes source content dispatch')
need('ProjectionSourcePayloads' in be and 'projectionSourcePayload' in be,'opaque unknown source payload preservation missing')
need('isCompatible(sourceMode, chassisProfile())' in be,'source activation does not enforce registry/chassis compatibility')

need('class ProjectionSourceRenderRegistry' in render_registry,'client render registry missing')
need('ProjectionSourceRenderRegistry.renderer(settings.sourceMode())' in renderer,'renderer still hardcodes top-level source dispatch')
for source in ('IMAGE','ITEM','ENTITY','BANNER'):
    need(f'ProjectionSettings.SourceMode.{source}' in renderer,f'builtin renderer registration missing for {source}')
need('switch (settings.sourceMode())' not in renderer,'renderer still contains closed source switch')

need('record ProjectionTransform' in transform,'ProjectionTransform abstraction missing')
for q in ('orientationX','orientationY','orientationZ','orientationW'):
    need(q in settings and q in transform,f'quaternion-ready transform field missing: {q}')
need('normalizeOrientation' in transform,'quaternion normalization missing')
need('public ProjectionTransform transform()' in settings and 'withTransform' in settings,'ProjectionSettings transform boundary missing')

need('interface ProjectionEnergySource' in energy,'device-agnostic energy boundary missing')
need('ProjectionEnergySource.fromCore(core)' in power,'ProjectionPower does not adapt fixed Core through energy boundary')
need('ProjectionEnergySource energySource' in power,'generic ProjectionPower energy overload missing')

need('NETWORK_PROTOCOL = "27"' in main,'network protocol was not bumped to 27 for source/transform codec change')

# Closed source switches/ordinals should be gone from current runtime source.
for p in (ROOT/'src/main/java').rglob('*.java'):
    text=p.read_text(encoding='utf-8')
    rel=p.relative_to(ROOT)
    need('SourceMode.fromOrdinal' not in text,f'legacy SourceMode.fromOrdinal remains in {rel}')
    need('sourceMode().ordinal()' not in text,f'sourceMode ordinal remains in {rel}')
    need('switch (settings.sourceMode())' not in text,f'closed settings.sourceMode switch remains in {rel}')
    need('switch (safe.sourceMode())' not in text,f'closed safe.sourceMode switch remains in {rel}')

if errors:
    print('dev.82 projection-source future-proofing verification FAILED')
    for e in errors: print(' -',e)
    raise SystemExit(1)
print('dev.82 projection-source future-proofing verification PASS')
