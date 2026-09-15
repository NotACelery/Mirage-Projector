package celerbi.mirageprojector.client;

import celerbi.mirageprojector.network.OpenScanCodexPayload;
import net.minecraft.client.Minecraft;

/** Client-side metadata cache for the currently viewed physical Scan Codex. */
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
            return;
        }
        if (payload.openScreen()) {
            minecraft.setScreen(new ScanCodexScreen(payload));
        }
    }

    public static OpenScanCodexPayload latest() {
        return latest;
    }
}
