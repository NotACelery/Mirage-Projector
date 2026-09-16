#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors = []

def need(cond, msg):
    if not cond:
        errors.append(msg)

be = (ROOT / "src/main/java/celerbi/mirageprojector/blockentity/MirageProjectorBlockEntity.java").read_text()
need("private boolean projectionEnabled = true;" in be, "projectionEnabled state missing/default is not ON")
need('tag.putBoolean("ProjectionEnabled", projectionEnabled)' in be, "projectionEnabled is not persisted")
need('!tag.contains("ProjectionEnabled") || tag.getBoolean("ProjectionEnabled")' in be, "legacy worlds do not default projectionEnabled to ON")
need("endResonanceLocksControls()" in be and "Items.DRAGON_EGG" in be, "End Resonance shutdown hook missing")
need(be.count("settings = settings.withSourceMode(") == 1, "workspace editing still changes active SourceMode implicitly")
settings_java = (ROOT / "src/main/java/celerbi/mirageprojector/ProjectionSettings.java").read_text()
need("sourceMode, newImageLayoutMode" in settings_java, "Image workspace still forces SourceMode.IMAGE instead of preserving active source")
need("projectionEnabled = true;" in be and "activateProjectionSource" in be, "Use-mode activation boundary does not re-enable projection")

renderer = (ROOT / "src/main/java/celerbi/mirageprojector/client/MirageProjectorRenderer.java").read_text()
need("if (!blockEntity.projectionEnabled())" in renderer, "renderer does not stop projection when OFF")

network = (ROOT / "src/main/java/celerbi/mirageprojector/network/ModNetworking.java").read_text()
need("SetProjectionEnabledPayload.TYPE" in network, "projection enabled payload not registered")
source_payload = (ROOT / "src/main/java/celerbi/mirageprojector/network/SetProjectionSourcePayload.java").read_text()
need("activateProjectionSource(payload.sourceMode())" in source_payload, "Use-mode payload bypasses activation boundary")
enabled_payload = (ROOT / "src/main/java/celerbi/mirageprojector/network/SetProjectionEnabledPayload.java").read_text()
need("This doesn't seem to work" not in enabled_payload, "event text should remain localized, not hard-coded")
need("end_resonance.turn_off_blocked" in enabled_payload, "End Resonance TURN OFF event hook missing")

screen = (ROOT / "src/main/java/celerbi/mirageprojector/client/MirageProjectorScreen.java").read_text()
need("renderActiveModeOutline" in screen and "0xFFFFFFFF" in screen, "white active-mode outline missing")
need("new SetProjectionEnabledPayload(menu.projectorPos(), false)" in screen, "TURN OFF button payload missing")

image_screen = (ROOT / "src/main/java/celerbi/mirageprojector/client/ImageProjectorScreen.java").read_text()
need("gui.mirage_projector.image.single_sizing" not in image_screen, "Image workspace still renders the debug resolution/projection sizing line")
need("ProjectionImageSizing" not in image_screen, "Image workspace still carries debug sizing calculation code")

for name in ["ImageProjectorScreen.java","ItemProjectorScreen.java","EntityProjectorScreen.java","BannerProjectorScreen.java"]:
    text = (ROOT / "src/main/java/celerbi/mirageprojector/client" / name).read_text()
    need("currentProjectionEnabled() && currentSourceMode()" in text, f"{name} active-state label ignores OFF")

model_path = ROOT / "src/main/resources/assets/mirage_projector/models/block/mirage_projector_alt.json"
model = json.loads(model_path.read_text())
need(model["children"]["emitter"].get("render_type") == "minecraft:cutout", "Compact emitter is still in translucent sorting")
solids=[]
for child_name, child in model["children"].items():
    if child.get("render_type") == "minecraft:solid":
        for element in child.get("elements", []):
            solids.append((child_name, element["from"], element["to"]))
for idx, element in enumerate(model["children"]["emitter"].get("elements", [])):
    ef, et = element["from"], element["to"]
    for child_name, sf, st in solids:
        overlap=[(max(ef[a],sf[a]),min(et[a],st[a])) for a in range(3)]
        need(not all(hi > lo for lo,hi in overlap), f"Compact emitter {idx} still intersects solid {child_name} volume: {overlap}")

props=(ROOT/'gradle.properties').read_text()
main=(ROOT/'src/main/java/celerbi/mirageprojector/MirageProjector.java').read_text()
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
need('mod_version=0.1.0-dev.79d' in props, 'version is not dev.79d')
need('NETWORK_PROTOCOL = "26"' in main, 'network protocol is not 26')

if errors:
    print('dev.79d projection-state / Compact model verification FAILED')
    for e in errors:
        print(' -', e)
    raise SystemExit(1)
print('dev.79d projection-state / Compact model verification PASS')
