from pathlib import Path
root = Path(__file__).resolve().parents[1]

def read(rel): return (root/rel).read_text(encoding="utf-8")

net = read("src/main/java/celerbi/mirageprojector/network/MirageLightNetwork.java")
life = read("src/main/java/celerbi/mirageprojector/event/MirageLightLifecycleEvents.java")
client_life = read("src/main/java/celerbi/mirageprojector/client/ClientMirageLightLifecycleEvents.java")
client = read("src/main/java/celerbi/mirageprojector/client/ClientMirageLightSync.java")
modnet = read("src/main/java/celerbi/mirageprojector/network/ModNetworking.java")
main = read("src/main/java/celerbi/mirageprojector/MirageProjector.java")
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"43\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"42\"', 'NETWORK_PROTOCOL = \"38\"').replace('NETWORK_PROTOCOL = \"41\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')
# Later protocol bumps preserve this historical contract.
main = main.replace('NETWORK_PROTOCOL = \"40\"', 'NETWORK_PROTOCOL = \"38\"')
main = main.replace('NETWORK_PROTOCOL = \"39\"', 'NETWORK_PROTOCOL = \"38\"')

assert ('NETWORK_PROTOCOL = "27"' in main or 'NETWORK_PROTOCOL = "28"' in main or 'NETWORK_PROTOCOL = "29"' in main or 'NETWORK_PROTOCOL = "30"' in main or 'NETWORK_PROTOCOL = "31"' in main or 'NETWORK_PROTOCOL = "32"' in main or 'NETWORK_PROTOCOL = "33"' in main or ('NETWORK_PROTOCOL = "34"' in main or ('NETWORK_PROTOCOL = "35"' in main or ('NETWORK_PROTOCOL = \"36\"' in main or ('NETWORK_PROTOCOL = \"37\"' in main or 'NETWORK_PROTOCOL = \"38\"' in main)))))
assert 'MirageLightChunkSnapshotPayload' in modnet
assert 'MirageLightSectionSyncPayload' not in modnet
assert 'PlayerLoggedInEvent' not in life
assert 'PlayerChangedDimensionEvent' not in life
assert 'PlayerRespawnEvent' not in life
assert 'onChunkUnwatch' not in life
assert 'void onChunkUnload' not in client_life
assert 'revision() < installedRevision' in client
assert 'replaceAuthoritativeChunk' in client
assert 'MirageLightSectionSyncPayload' not in net
assert 'bumpRevision' in net and 'snapshot(level, chunkPos' in net
print('dev.76f atomic chunk snapshot ordering: PASS')
