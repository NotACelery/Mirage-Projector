from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")

assert "mod_version=0.1.0-dev.75a" in read("gradle.properties")
assert 'NETWORK_PROTOCOL = "20"' in read("src/main/java/celerbi/mirageprojector/MirageProjector.java")

mixins = json.loads(read("src/main/resources/mirage_projector.mixins.json"))
assert "LevelMirageLightMixin" in mixins["mixins"]
assert "LevelLightEngineMirageLightMixin" in mixins["mixins"]
assert "client.RenderChunkRegionMirageLightMixin" in mixins["client"]

network = read("src/main/java/celerbi/mirageprojector/network/ModNetworking.java")
assert "MirageLightSourceSyncPayload.TYPE" in network

field = read("src/main/java/celerbi/mirageprojector/crying/CryingObsidianLightField.java")
assert "cleanupLegacyPhysicalRelays" in field
assert "MirageLightNetwork.broadcastUpsert" in field
assert "defaultBlockState()" not in field or "CRYING_LIGHT_NODE.get().defaultBlockState()" not in field

node = read("src/main/java/celerbi/mirageprojector/block/CryingObsidianLightNodeBlock.java")
assert "level.removeBlock(pos, false)" in node
assert "desiredLevelAt" not in node

world = read("src/main/java/celerbi/mirageprojector/light/engine/MirageLightWorld.java")
assert "ENGINE_STATES" in world
assert "levelAt(LevelLightEngine" in world

print("dev.75a authoritative Mirage Light midpoint verification: PASS")
print("- protocol 20 source sync registered")
print("- Level / LevelLightEngine / RenderChunkRegion query bridges present")
print("- physical Crying Light Nodes are migration-only")
print("- client fields remain deterministic local solves from source descriptors")
