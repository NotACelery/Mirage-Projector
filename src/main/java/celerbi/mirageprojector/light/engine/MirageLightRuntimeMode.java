package celerbi.mirageprojector.light.engine;

/**
 * Declares how a solved Mirage light field is intended to be consumed.
 * STATIC_WORLD is server-authoritative and section-synchronized.
 * DYNAMIC_VISUAL remains the independent backend for portable flashlights,
 * moving projectors and other rapidly changing client-visible emitters; it must not
 * write/rebuild the static section channel every frame.
 */
public enum MirageLightRuntimeMode {
    STATIC_WORLD,
    DYNAMIC_VISUAL
}
