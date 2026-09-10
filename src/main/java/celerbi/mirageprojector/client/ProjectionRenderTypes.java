package celerbi.mirageprojector.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class ProjectionRenderTypes extends RenderStateShard {
    private static final Map<ResourceLocation, RenderType> ENTITY_GHOST = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> ITEM_GHOST = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> LATE_ENTITY_GHOST = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> LATE_ITEM_GHOST = new ConcurrentHashMap<>();

    private ProjectionRenderTypes() {
        super("mirage_projector_render_types", () -> {}, () -> {});
    }

    public static RenderType ghostEntity(ResourceLocation texture) {
        return ENTITY_GHOST.computeIfAbsent(texture, key -> createGhostEntity(key, false));
    }

    public static RenderType ghostItem(ResourceLocation texture) {
        return ITEM_GHOST.computeIfAbsent(texture, key -> createGhostItem(key, false));
    }

    public static RenderType lateGhostEntity(ResourceLocation texture) {
        return LATE_ENTITY_GHOST.computeIfAbsent(texture, key -> createGhostEntity(key, true));
    }

    public static RenderType lateGhostItem(ResourceLocation texture) {
        return LATE_ITEM_GHOST.computeIfAbsent(texture, key -> createGhostItem(key, true));
    }

    private static RenderType createGhostEntity(ResourceLocation texture, boolean depthWrite) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setOutputState(MAIN_TARGET)
                .setWriteMaskState(depthWrite ? COLOR_DEPTH_WRITE : COLOR_WRITE)
                .createCompositeState(false);

        return RenderType.create(
                depthWrite ? "mirage_projector_entity_ghost_late" : "mirage_projector_entity_ghost",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                RenderType.TRANSIENT_BUFFER_SIZE,
                false,
                !depthWrite,
                state
        );
    }

    private static RenderType createGhostItem(ResourceLocation texture, boolean depthWrite) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setOutputState(MAIN_TARGET)
                .setWriteMaskState(depthWrite ? COLOR_DEPTH_WRITE : COLOR_WRITE)
                .createCompositeState(false);

        return RenderType.create(
                depthWrite ? "mirage_projector_item_ghost_late" : "mirage_projector_item_ghost",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                RenderType.TRANSIENT_BUFFER_SIZE,
                false,
                !depthWrite,
                state
        );
    }
}
