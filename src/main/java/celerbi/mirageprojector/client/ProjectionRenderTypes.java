package celerbi.mirageprojector.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RenderTypes owned by Mirage for ghost projections.
 *
 * <p>The important differences from a normal opaque entity pass are:
 * these passes still depth-test against the world, but they write colour only,
 * and they participate in Minecraft's ITEM_ENTITY_TARGET transparency target.
 * The colour-only mask keeps the old water-hole fix, while the entity target
 * keeps Fabulous/shader-transparency composition ordered correctly against
 * clouds and the other dedicated transparency framebuffers.</p>
 */
public final class ProjectionRenderTypes extends RenderStateShard {
    private static final Map<ResourceLocation, RenderType> ENTITY_GHOST = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> ITEM_GHOST = new ConcurrentHashMap<>();

    private ProjectionRenderTypes() {
        super("mirage_projector_render_types", () -> {}, () -> {});
    }

    public static RenderType ghostEntity(ResourceLocation texture) {
        return ENTITY_GHOST.computeIfAbsent(texture, ProjectionRenderTypes::createGhostEntity);
    }

    public static RenderType ghostItem(ResourceLocation texture) {
        return ITEM_GHOST.computeIfAbsent(texture, ProjectionRenderTypes::createGhostItem);
    }

    private static RenderType createGhostEntity(ResourceLocation texture) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);

        return RenderType.create(
                "mirage_projector_entity_ghost",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                RenderType.TRANSIENT_BUFFER_SIZE,
                false,
                true,
                state
        );
    }

    private static RenderType createGhostItem(ResourceLocation texture) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);

        return RenderType.create(
                "mirage_projector_item_ghost",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                RenderType.TRANSIENT_BUFFER_SIZE,
                false,
                true,
                state
        );
    }
}
