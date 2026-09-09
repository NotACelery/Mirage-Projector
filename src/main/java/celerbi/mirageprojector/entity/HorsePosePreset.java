package celerbi.mirageprojector.entity;

import java.util.Locale;

/** Projection-only Horse pose presets. These never tick horse AI or mutate the scan card. */
public enum HorsePosePreset {
    IDLE("idle"),
    REARING("rearing");

    private final String serializedName;

    HorsePosePreset(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public HorsePosePreset next() {
        HorsePosePreset[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static HorsePosePreset fromSerializedName(String value) {
        if (value == null || value.isBlank()) {
            return IDLE;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (HorsePosePreset pose : values()) {
            if (pose.serializedName.equals(normalized)) {
                return pose;
            }
        }
        return IDLE;
    }
}
