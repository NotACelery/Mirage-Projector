from pathlib import Path
root = Path(__file__).resolve().parents[1]
checks=[]
def has(rel, needle):
    text=(root/rel).read_text(encoding="utf-8")
    assert needle in text, f"missing {needle!r} in {rel}"
    checks.append((rel,needle))
has("gradle.properties", "mod_version=0.1.0-dev.76h")
has("src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java", "authoritativeSectionKeysForChunk")
has("src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java", "refresh.addAll(replacement.keySet())")
has("src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java", "section.offset(0, -1, 0).asLong()")
has("src/main/java/celerbi/mirageprojector/light/engine/MirageLightWorld.java", "authoritativeSectionKeysForChunk")
has("src/main/java/celerbi/mirageprojector/light/engine/MirageLightEngine.java", "authoritativeSectionKeysForChunk")
print(f"dev.76h section-boundary contract: {len(checks)} checks passed")
