package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side renderer registry keyed by stable projection-source IDs.
 *
 * <p>Common source presence/count semantics live in ProjectionSourceRegistry. This registry is
 * intentionally client-only and lets future/addon sources attach a renderer without extending
 * MirageProjectorRenderer's built-in source chain.</p>
 */
public final class ProjectionSourceRenderRegistry {
    private static final Map<ProjectionSettings.SourceMode, Renderer> RENDERERS = new ConcurrentHashMap<>();

    private ProjectionSourceRenderRegistry() {
    }

    public static void register(ResourceLocation id, Renderer renderer) {
        register(ProjectionSettings.SourceMode.of(id), renderer);
    }

    public static void register(ProjectionSettings.SourceMode source, Renderer renderer) {
        if (source == null || renderer == null) {
            throw new IllegalArgumentException("Projection source renderer requires non-null source and renderer");
        }
        Renderer existing = RENDERERS.putIfAbsent(source, renderer);
        if (existing != null) {
            throw new IllegalStateException("Projection renderer already registered: " + source.serializedName());
        }
    }

    static void registerBuiltin(ProjectionSettings.SourceMode source, Renderer renderer) {
        register(source, renderer);
    }

    public static Optional<Renderer> renderer(ProjectionSettings.SourceMode source) {
        return Optional.ofNullable(source == null ? null : RENDERERS.get(source));
    }

    @FunctionalInterface
    public interface Renderer {
        void render(RenderContext context);
    }

    public record RenderContext(
            MirageProjectorRenderer renderer,
            MirageProjectorBlockEntity blockEntity,
            ProjectionSettings settings,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            float angle,
            float bob,
            int projectionLight,
            double gameTime
    ) {
    }
}
