package celerbi.mirageprojector.client;

import celerbi.mirageprojector.network.OpenLecternScanCodexPayload;
import celerbi.mirageprojector.network.OpenScanCodexPayload;
import net.minecraft.client.Minecraft;

/** Client-side metadata/detail cache for the currently viewed physical Scan Codex. */
public final class ClientScanCodex {
    private static OpenScanCodexPayload latest;

    private ClientScanCodex() {
    }

    public static void accept(OpenScanCodexPayload payload) {
        latest = payload;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof ScanCodexScreen screen
                && screen.codexId().equals(payload.codexId())) {
            screen.acceptSnapshot(payload);
        }
    }

    public static void acceptLectern(OpenLecternScanCodexPayload payload) {
        OpenScanCodexPayload snapshot = payload.asCodexSnapshot();
        latest = snapshot;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof ScanCodexScreen screen
                && screen.codexId().equals(payload.codexId())
                && screen.isLecternMode(payload.pos())) {
            screen.acceptSnapshot(snapshot);
        }
    }

    public static OpenScanCodexPayload latest() {
        return latest;
    }
}
