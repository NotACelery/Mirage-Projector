package celerbi.mirageprojector.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("3");

        registrar.playToServer(
                UpdateProjectorPayload.TYPE,
                UpdateProjectorPayload.STREAM_CODEC,
                UpdateProjectorPayload::handle
        );
        registrar.playToServer(
                UploadAssetChunkPayload.TYPE,
                UploadAssetChunkPayload.STREAM_CODEC,
                UploadAssetChunkPayload::handle
        );
        registrar.playToServer(
                RequestAssetPayload.TYPE,
                RequestAssetPayload.STREAM_CODEC,
                RequestAssetPayload::handle
        );
        registrar.playToClient(
                DownloadAssetChunkPayload.TYPE,
                DownloadAssetChunkPayload.STREAM_CODEC,
                DownloadAssetChunkPayload::handle
        );
    }
}
