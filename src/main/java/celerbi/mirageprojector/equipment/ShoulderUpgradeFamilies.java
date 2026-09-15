package celerbi.mirageprojector.equipment;

import celerbi.mirageprojector.MirageProjector;
import net.minecraft.resources.ResourceLocation;

/** Stable namespaced identities for shoulder-upgrade families. */
public final class ShoulderUpgradeFamilies {
    public static final ResourceLocation AUTO_BATTERY_SWAP =
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "auto_battery_swap");
    public static final ResourceLocation SHOULDER_STRAP_SLOT_EXPANSION =
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "shoulder_strap_slot_expansion");

    private ShoulderUpgradeFamilies() {
    }
}
