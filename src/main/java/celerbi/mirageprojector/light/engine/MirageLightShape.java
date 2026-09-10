package celerbi.mirageprojector.light.engine;

/**
 * Geometric envelope for a Mirage light source.
 * Only OMNIDIRECTIONAL is solved in dev.74; the remaining shapes are part of
 * the stable source/profile contract so later projector lighting does not need
 * another storage/solver rewrite.
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
