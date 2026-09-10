package celerbi.mirageprojector.light.engine;

/**
 * Declares how a solved Mirage light field is intended to be consumed.
 * dev.74 only builds STATIC_WORLD fields; DYNAMIC_VISUAL is reserved for
 * portable projectors, moving holograms and other client-driven emitters.
 */
public enum MirageLightRuntimeMode {
    STATIC_WORLD,
    DYNAMIC_VISUAL
}
