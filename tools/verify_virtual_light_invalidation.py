from pathlib import Path

root = Path(__file__).resolve().parents[1]
path = root / "src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java"
text = path.read_text(encoding="utf-8")

required = [
    "level.getChunkSource().onLightUpdate(LightLayer.BLOCK, section);",
    "minecraft.levelRenderer.setSectionDirty(section.x(), section.y(), section.z());",
    "refresh.addAll(replacement.keySet());",
]
missing = [needle for needle in required if needle not in text]
if missing:
    raise SystemExit("FAIL: missing current virtual-light invalidation contract: " + repr(missing))

legacy_vertical = "refreshKeys.add(section.offset(0, -1, 0).asLong());" in text
halo_contract = all(token in text for token in (
    "for (int dx = -1; dx <= 1; dx++)",
    "for (int dy = -1; dy <= 1; dy++)",
    "for (int dz = -1; dz <= 1; dz++)",
    "boundaryChanged(before, after, dx, dy, dz)",
))
if not (legacy_vertical or halo_contract):
    raise SystemExit("FAIL: missing section-boundary/neighbor render invalidation contract")

print("PASS: Mirage virtual field changes publish BLOCK light invalidation, renderer dirtiness, render-neighbor boundary refresh and identical-snapshot refresh")
