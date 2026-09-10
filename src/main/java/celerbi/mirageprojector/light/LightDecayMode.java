package celerbi.mirageprojector.light;

public enum LightDecayMode {
    VANILLA(true, false, false),
    EXTEND(true, false, false),
    CONCENTRATE(false, true, false),
    DIRECTIONAL_SPOT(false, true, false),
    ROTATING_DIRECTIONAL_SPOT(false, true, true);

    private final boolean runtimeImplemented;
    private final boolean requiresSourceLightSuppression;
    private final boolean dynamicDirection;

    LightDecayMode(
            boolean runtimeImplemented,
            boolean requiresSourceLightSuppression,
            boolean dynamicDirection
    ) {
        this.runtimeImplemented = runtimeImplemented;
        this.requiresSourceLightSuppression = requiresSourceLightSuppression;
        this.dynamicDirection = dynamicDirection;
    }

    public boolean runtimeImplemented() {
        return runtimeImplemented;
    }

    public boolean requiresSourceLightSuppression() {
        return requiresSourceLightSuppression;
    }

    public boolean dynamicDirection() {
        return dynamicDirection;
    }
}
