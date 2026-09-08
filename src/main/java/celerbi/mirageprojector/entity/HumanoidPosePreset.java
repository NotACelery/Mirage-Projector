package celerbi.mirageprojector.entity;

import java.util.Locale;

/**
 * Persistent pose selection for the virtual Humanoid rig.
 *
 * <p>Pose is intentionally independent from entity scans and equipment snapshot
 * UUIDs: cycling a pose never requires recapturing a body or an ItemStack.</p>
 */
public enum HumanoidPosePreset {
    STANDING("standing"),
    GUARD("guard"),
    HERO("hero"),
    COMBAT("combat"),
    RAISED_MAIN_HAND("raised_main_hand"),
    RAISED_OFF_HAND("raised_off_hand"),
    DUAL_WIELD("dual_wield"),
    DISPLAY("display");

    private final String serializedName;

    HumanoidPosePreset(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public HumanoidPosePreset next() {
        HumanoidPosePreset[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static HumanoidPosePreset fromSerializedName(String value) {
        if (value == null || value.isBlank()) {
            return STANDING;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (HumanoidPosePreset pose : values()) {
            if (pose.serializedName.equals(normalized) || pose.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return pose;
            }
        }
        return STANDING;
    }
}
