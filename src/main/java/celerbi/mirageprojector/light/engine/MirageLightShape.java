package celerbi.mirageprojector.light.engine;

/**
 * Geometric envelope for a Mirage light source.
 * Only OMNIDIRECTIONAL is currently solved. The remaining shapes are reserved by
 * the stable source/profile contract so future light consumers can extend geometry
 * without replacing storage or solver architecture.
 */
public enum MirageLightShape {
    OMNIDIRECTIONAL(true),
    DIRECTIONAL_CONE(false),
    RECTANGULAR_FRUSTUM(false),
    PLANE(false);

    private final boolean runtimeImplemented;

    MirageLightShape(boolean runtimeImplemented) {
        this.runtimeImplemented = runtimeImplemented;
    }

    public boolean runtimeImplemented() {
        return runtimeImplemented;
    }
}
