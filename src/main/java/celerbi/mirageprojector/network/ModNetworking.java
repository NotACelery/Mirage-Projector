package celerbi.mirageprojector.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("18");

        registrar.playToServer(
                UpdateProjectorPayload.TYPE,
                UpdateProjectorPayload.STREAM_CODEC,
                UpdateProjectorPayload::handle
        );
        registrar.playToServer(
                OpenEntityWorkspacePayload.TYPE,
                OpenEntityWorkspacePayload.STREAM_CODEC,
                OpenEntityWorkspacePayload::handle
        );
        registrar.playToServer(
                OpenImageWorkspacePayload.TYPE,
                OpenImageWorkspacePayload.STREAM_CODEC,
                OpenImageWorkspacePayload::handle
        );
        registrar.playToServer(
                OpenItemWorkspacePayload.TYPE,
                OpenItemWorkspacePayload.STREAM_CODEC,
                OpenItemWorkspacePayload::handle
        );
        registrar.playToServer(
                OpenBannerWorkspacePayload.TYPE,
                OpenBannerWorkspacePayload.STREAM_CODEC,
                OpenBannerWorkspacePayload::handle
        );
        registrar.playToServer(
                BannerWorkspaceActionPayload.TYPE,
                BannerWorkspaceActionPayload.STREAM_CODEC,
                BannerWorkspaceActionPayload::handle
        );
        registrar.playToServer(
                UpdateImageWorkspacePayload.TYPE,
                UpdateImageWorkspacePayload.STREAM_CODEC,
                UpdateImageWorkspacePayload::handle
        );
        registrar.playToServer(
                SetProjectionSourcePayload.TYPE,
                SetProjectionSourcePayload.STREAM_CODEC,
                SetProjectionSourcePayload::handle
        );
        registrar.playToServer(
                OpenProjectorWorkspacePayload.TYPE,
                OpenProjectorWorkspacePayload.STREAM_CODEC,
                OpenProjectorWorkspacePayload::handle
        );
        registrar.playToServer(
                EntityWorkspaceActionPayload.TYPE,
                EntityWorkspaceActionPayload.STREAM_CODEC,
                EntityWorkspaceActionPayload::handle
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
        registrar.playToClient(
                AssetUploadAckPayload.TYPE,
                AssetUploadAckPayload.STREAM_CODEC,
                AssetUploadAckPayload::handle
        );
        registrar.playToClient(
                OpenDebugHandbookPayload.TYPE,
                OpenDebugHandbookPayload.STREAM_CODEC,
                OpenDebugHandbookPayload::handle
        );
    }
}
