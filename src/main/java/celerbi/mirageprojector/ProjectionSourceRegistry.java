package celerbi.mirageprojector;

import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Common-side projection-source registry.
 *
 * <p>Sources are identified by stable namespaced IDs. The built-in Image/Item/Entity/Banner
 * sources are ordinary registrations, and future Mirage versions/addons may register another
 * source without extending a Java enum or changing persisted ordinal layouts.</p>
 *
 * <p>The common provider owns only source-presence/count semantics. Rendering is client-side and
 * registered separately through {@code ProjectionSourceRenderRegistry}. Missing registrations
 * fail closed: the source ID remains persisted in ProjectionSettings, but no content is exposed
 * until its provider is available again.</p>
 */
public final class ProjectionSourceRegistry {
    private static final Map<ResourceLocation, Definition> DEFINITIONS = new LinkedHashMap<>();

    static {
        registerBuiltin(
                ProjectionSettings.SourceMode.IMAGE,
                "gui.mirage_projector.source.image",
                new ContentProvider() {
                    @Override
                    public boolean hasContent(MirageProjectorBlockEntity projector, ProjectionSettings settings) {
                        if (projector.chassisProfile().supportsMultiSourceImageLayout()
                                && settings.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI) {
                            return projector.imageSourceBank().hasAny(projector.chassisProfile().imageLayoutSlots());
                        }
                        if (projector.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM) {
                            return settings.hasAnyImage();
                        }
                        return hasPlaneImageContent(settings);
                    }

                    @Override
                    public int contentCount(MirageProjectorBlockEntity projector, ProjectionSettings settings) {
                        if (projector.chassisProfile().supportsMultiSourceImageLayout()
                                && settings.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI) {
                            return projector.imageSourceBank().countPresent(projector.chassisProfile().imageLayoutSlots());
                        }
                        if (projector.chassisProfile().geometry() != ProjectionChassisProfile.Geometry.PRISM) {
                            return hasPlaneImageContent(settings) ? 1 : 0;
                        }
                        int count = 0;
                        if (settings.hasImage()) count++;
                        if (settings.hasEastImage()) count++;
                        if (settings.hasBackImage()) count++;
                        if (settings.hasWestImage()) count++;
                        return count;
                    }
                }
        );
        registerBuiltin(
                ProjectionSettings.SourceMode.ITEM,
                "gui.mirage_projector.source.item",
                provider(
                        (projector, settings) -> !projector.projectedStack().isEmpty(),
                        (projector, settings) -> projector.projectedStack().isEmpty() ? 0 : 1
                )
        );
        registerBuiltin(
                ProjectionSettings.SourceMode.ENTITY,
                "gui.mirage_projector.source.entity",
                provider(
                        (projector, settings) -> projector.entityProjectionState().hasProjectedEntityContent(),
                        (projector, settings) -> projector.entityProjectionState().hasProjectedEntityContent() ? 1 : 0
                )
        );
        registerBuiltin(
                ProjectionSettings.SourceMode.BANNER,
                "gui.mirage_projector.source.banner",
                new ContentProvider() {
                    @Override
                    public boolean hasContent(MirageProjectorBlockEntity projector, ProjectionSettings settings) {
                        return projector.hasAnyBannerSnapshot();
                    }

                    @Override
                    public int contentCount(MirageProjectorBlockEntity projector, ProjectionSettings settings) {
                        int limit = projector.chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM ? 4 : 1;
                        int count = 0;
                        for (int face = 0; face < limit; face++) {
                            if (!projector.bannerSnapshot(face).isEmpty()) {
                                count++;
                            }
                        }
                        return count;
                    }
                }
        );
    }

    private ProjectionSourceRegistry() {
    }

    /**
     * Register a source contract. Addons should call this during common setup.
     * Duplicate IDs are rejected rather than silently replacing another provider.
     */
    public static synchronized Definition register(
            ResourceLocation id,
            String translationKey,
            ContentProvider provider
    ) {
        return register(ProjectionSettings.SourceMode.of(id), translationKey, provider, SourceCompatibility.ALL);
    }

    public static synchronized Definition register(
            ResourceLocation id,
            String translationKey,
            ContentProvider provider,
            SourceCompatibility compatibility
    ) {
        return register(ProjectionSettings.SourceMode.of(id), translationKey, provider, compatibility);
    }

    public static synchronized Definition register(
            ProjectionSettings.SourceMode source,
            String translationKey,
            ContentProvider provider
    ) {
        return register(source, translationKey, provider, SourceCompatibility.ALL);
    }

    public static synchronized Definition register(
            ProjectionSettings.SourceMode source,
            String translationKey,
            ContentProvider provider,
            SourceCompatibility compatibility
    ) {
        Objects.requireNonNull(source, "Projection source");
        Objects.requireNonNull(provider, "Projection source content provider");
        SourceCompatibility safeCompatibility = compatibility == null ? SourceCompatibility.ALL : compatibility;
        String safeTranslationKey = translationKey == null || translationKey.isBlank()
                ? source.serializedName()
                : translationKey;
        Definition definition = new Definition(source, safeTranslationKey, provider, safeCompatibility, false);
        Definition existing = DEFINITIONS.putIfAbsent(source.id(), definition);
        if (existing != null) {
            throw new IllegalStateException("Projection source already registered: " + source.serializedName());
        }
        return definition;
    }

    private static synchronized Definition registerBuiltin(
            ProjectionSettings.SourceMode source,
            String translationKey,
            ContentProvider provider
    ) {
        Definition definition = new Definition(source, translationKey, provider, SourceCompatibility.ALL, true);
        Definition existing = DEFINITIONS.putIfAbsent(source.id(), definition);
        if (existing != null) {
            throw new IllegalStateException("Builtin projection source already registered: " + source.serializedName());
        }
        return definition;
    }

    public static Optional<Definition> definition(ProjectionSettings.SourceMode source) {
        if (source == null) {
            return Optional.empty();
        }
        synchronized (ProjectionSourceRegistry.class) {
            return Optional.ofNullable(DEFINITIONS.get(source.id()));
        }
    }

    public static boolean isRegistered(ProjectionSettings.SourceMode source) {
        return definition(source).isPresent();
    }

    public static Collection<Definition> definitions() {
        synchronized (ProjectionSourceRegistry.class) {
            return List.copyOf(DEFINITIONS.values());
        }
    }

    public static List<Definition> definitionsFor(ProjectionChassisProfile chassis) {
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        synchronized (ProjectionSourceRegistry.class) {
            return DEFINITIONS.values().stream()
                    .filter(definition -> definition.compatibility().supports(safeChassis))
                    .toList();
        }
    }

    public static boolean isCompatible(
            ProjectionSettings.SourceMode source,
            ProjectionChassisProfile chassis
    ) {
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        return definition(source)
                .map(definition -> definition.compatibility().supports(safeChassis))
                .orElse(false);
    }

    public static boolean hasContent(
            ProjectionSettings.SourceMode source,
            MirageProjectorBlockEntity projector,
            ProjectionSettings settings
    ) {
        return definition(source)
                .map(definition -> definition.provider().hasContent(projector, settings))
                .orElse(false);
    }

    public static int contentCount(
            ProjectionSettings.SourceMode source,
            MirageProjectorBlockEntity projector,
            ProjectionSettings settings
    ) {
        return definition(source)
                .map(definition -> Math.max(0, definition.provider().contentCount(projector, settings)))
                .orElse(0);
    }

    private static ContentProvider provider(ContentPredicate predicate, ContentCounter counter) {
        return new ContentProvider() {
            @Override
            public boolean hasContent(MirageProjectorBlockEntity projector, ProjectionSettings settings) {
                return predicate.test(projector, settings);
            }

            @Override
            public int contentCount(MirageProjectorBlockEntity projector, ProjectionSettings settings) {
                return counter.count(projector, settings);
            }
        };
    }

    private static boolean hasPlaneImageContent(ProjectionSettings settings) {
        return switch (settings.backFaceMode()) {
            case FRONT, MIRRORED, READABLE -> settings.hasImage();
            case BACK -> settings.hasBackImage();
            case INDEPENDENT -> settings.hasImage() || settings.hasBackImage();
        };
    }

    public record Definition(
            ProjectionSettings.SourceMode source,
            String translationKey,
            ContentProvider provider,
            SourceCompatibility compatibility,
            boolean builtin
    ) {
    }

    @FunctionalInterface
    public interface SourceCompatibility {
        SourceCompatibility ALL = chassis -> true;

        boolean supports(ProjectionChassisProfile chassis);
    }

    public interface ContentProvider {
        boolean hasContent(MirageProjectorBlockEntity projector, ProjectionSettings settings);

        int contentCount(MirageProjectorBlockEntity projector, ProjectionSettings settings);
    }

    @FunctionalInterface
    private interface ContentPredicate {
        boolean test(MirageProjectorBlockEntity projector, ProjectionSettings settings);
    }

    @FunctionalInterface
    private interface ContentCounter {
        int count(MirageProjectorBlockEntity projector, ProjectionSettings settings);
    }
}
