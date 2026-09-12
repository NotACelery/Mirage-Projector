from pathlib import Path
root=Path(__file__).resolve().parents[1]
net=(root/'src/main/java/celerbi/mirageprojector/network/MirageLightNetwork.java').read_text()
client=(root/'src/main/java/celerbi/mirageprojector/client/ClientMirageLightLifecycleEvents.java').read_text()
reg=(root/'src/main/java/celerbi/mirageprojector/network/ModNetworking.java').read_text()
req=(root/'src/main/java/celerbi/mirageprojector/network/RequestMirageLightChunkPayload.java').read_text()
main=(root/'src/main/java/celerbi/mirageprojector/MirageProjector.java').read_text()
assert 'watchedChunks' not in net and 'TRACKING' not in net
assert 'public static void sendChunkSnapshot' in net
assert 'MirageLightChunkSnapshotPayload' in net
assert 'RequestMirageLightChunkPayload' in client and 'ChunkEvent.Load' in client
assert 'PacketDistributor.sendToServer' in client
assert 'RequestMirageLightChunkPayload.TYPE' in reg
assert 'MirageLightChunkSnapshotPayload.TYPE' in reg
assert 'MirageLightNetwork.sendChunkSnapshot' in req
assert 'MAX_CLIENT_SYNC_CHUNK_DISTANCE' in req
assert 'NETWORK_PROTOCOL = "25"' in main
assert 'for (ServerPlayer player : level.players())' in net
print('dev.76e+ chunk snapshot handshake contract PASS')
