package celerbi.mirageprojector.light.engine;

/**
 * Geometric envelope for a Mirage light source.
 * OMNIDIRECTIONAL and DIRECTIONAL_CONE are solved by the shared voxel engine.
 * The remaining shapes stay reserved by the stable source/profile contract so future
 * consumers can extend geometry without replacing storage or solver architecture.
 */
public enum MirageLightShape {
    OMNIDIRECTIONAL(true),
    DIRECTIONAL_CONE(true),
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
