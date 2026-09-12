from pathlib import Path

root = Path(__file__).resolve().parents[1]
path = root / "src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java"
text = path.read_text(encoding="utf-8")

required = [
    "level.getChunkSource().onLightUpdate(LightLayer.BLOCK, sectionPos);",
    "minecraft.levelRenderer.setSectionDirty(sectionX, sectionY, sectionZ);",
]
missing = [needle for needle in required if needle not in text]
if missing:
    raise SystemExit("FAIL: missing virtual-light invalidation contract: " + repr(missing))

print("PASS: Mirage virtual field changes publish vanilla-style BLOCK light invalidation and renderer dirtiness")
