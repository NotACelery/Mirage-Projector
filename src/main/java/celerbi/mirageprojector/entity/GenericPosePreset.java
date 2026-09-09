package celerbi.mirageprojector.entity;

/** Optional pose presets for generic tameable entities such as cats and wolves. */
public enum GenericPosePreset {
    IDLE("idle"),
    SITTING("sitting");

    private final String serializedName;

    GenericPosePreset(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public GenericPosePreset next() {
        GenericPosePreset[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static GenericPosePreset fromSerializedName(String value) {
        if (value != null) {
            for (GenericPosePreset pose : values()) {
                if (pose.serializedName.equalsIgnoreCase(value)) {
                    return pose;
                }
            }
        }
        return IDLE;
    }
}
