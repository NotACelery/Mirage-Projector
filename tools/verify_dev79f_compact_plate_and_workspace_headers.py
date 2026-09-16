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
need(model["children"]["emitter"].get("render_type") == "minecraft:cutout", "Compact emitter child changed render type unexpectedly")
solids=[]
for child_name, child in model["children"].items():
    if child.get("render_type") == "minecraft:solid":
        for element in child.get("elements", []):
            solids.append((child_name, element["from"], element["to"]))

# dev.79g: repeated QA showed the decorative Compact Alt emitters were still the likely
# cause of the visible striped artifact, so their visible geometry is removed entirely.
emitters = model["children"].get("emitter", {}).get("elements", [])
need(len(emitters) == 0, "Compact Alt emitter visuals must be removed entirely in dev.79g")

# Workspace headers: title owns row 1; Use <mode> mode + Back own row 2.
item_screen = (ROOT / "src/main/java/celerbi/mirageprojector/client/ItemProjectorScreen.java").read_text()
banner_screen = (ROOT / "src/main/java/celerbi/mirageprojector/client/BannerProjectorScreen.java").read_text()
entity_screen = (ROOT / "src/main/java/celerbi/mirageprojector/client/EntityProjectorScreen.java").read_text()
image_screen = (ROOT / "src/main/java/celerbi/mirageprojector/client/ImageProjectorScreen.java").read_text()
item_menu = (ROOT / "src/main/java/celerbi/mirageprojector/menu/ItemProjectorMenu.java").read_text()
banner_menu = (ROOT / "src/main/java/celerbi/mirageprojector/menu/BannerProjectorMenu.java").read_text()
entity_menu = (ROOT / "src/main/java/celerbi/mirageprojector/menu/EntityProjectorMenu.java").read_text()
for name, text in [("Item", item_screen), ("Banner", banner_screen), ("Entity", entity_screen), ("Image", image_screen)]:
    need("topPos + 34" in text, f"{name} workspace mode/back row is not below the title")
need("fit(title.getString(), 150)" in item_screen, "Item workspace title is not clamped away from the mode/back row")
need("fit(title.getString(), 190)" in banner_screen, "Banner workspace title is not clamped away from the mode/back row")
need("fit(title.getString(), 220)" in entity_screen, "Entity workspace title is not clamped away from the mode/back row")
need("fit(title.getString(), 220)" in image_screen, "Image workspace title is not clamped away from the mode/back row")
need("SNAPSHOT_Y = 96" in item_menu and "PLAYER_INV_Y = 202" in item_menu, "Item workspace slots unexpectedly drifted")
need("FACE_Y = 102" in banner_menu and "PLAYER_INV_Y = 222" in banner_menu, "Banner workspace slots unexpectedly drifted")
need("CARD_Y = 87" in entity_menu and "HUMANOID_FIRST_Y = 154" in entity_menu and "PLAYER_INV_Y = 378" in entity_menu, "Entity workspace slots unexpectedly drifted")
need("244, 62, 0xFF9FBED1" in image_screen, "Image multi-source summary still overlaps the header controls")

props=(ROOT/'gradle.properties').read_text()
main=(ROOT/'src/main/java/celerbi/mirageprojector/MirageProjector.java').read_text()
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
need('mod_version=0.1.0-dev.79g' in props, 'version is not dev.79g')
need('NETWORK_PROTOCOL = "26"' in main, 'network protocol is not 26')

if errors:
    print('dev.79g projection-state / Compact model verification FAILED')
    for e in errors:
        print(' -', e)
    raise SystemExit(1)
print('dev.79g projection-state / Compact model verification PASS')
