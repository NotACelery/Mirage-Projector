package celerbi.mirageprojector.scan;

import celerbi.mirageprojector.entity.EntityScanData;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

/** Lightweight metadata sent to the Codex browser; full entity NBT remains server-side. */
public record ScanCodexEntrySummary(
        UUID scanId,
        ResourceLocation entityType,
        EntityScanData.Kind kind,
        String displayName,
        String nameplateText,
        boolean playerSource,
        boolean customNamed,
        boolean favorite,
        int equipmentCount
) {
    public ScanCodexEntrySummary {
        displayName = displayName == null ? "" : displayName;
        nameplateText = nameplateText == null ? "" : nameplateText;
        equipmentCount = Math.max(0, equipmentCount);
    }
}
