from pathlib import Path

root = Path(__file__).resolve().parents[1]
path = root / "src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java"
text = path.read_text(encoding="utf-8")

required = [
    "level.getChunkSource().onLightUpdate(LightLayer.BLOCK, section);",
    "minecraft.levelRenderer.setSectionDirty(section.x(), section.y(), section.z());",
    "refreshKeys.add(section.offset(0, -1, 0).asLong());",
    "refresh.addAll(replacement.keySet());",
]
missing = [needle for needle in required if needle not in text]
if missing:
    raise SystemExit("FAIL: missing current virtual-light invalidation contract: " + repr(missing))

print("PASS: Mirage virtual field changes publish BLOCK light invalidation, renderer dirtiness, section-below boundary refresh and identical-snapshot refresh")
