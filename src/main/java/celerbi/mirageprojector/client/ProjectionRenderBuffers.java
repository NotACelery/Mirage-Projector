package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.util.Mth;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Projection-local render-buffer wrapper for 3D Item and Entity holograms.
 *
 * <p>Base LivingEntity bodies are handled by {@link ProjectionRenderContext}
 * plus a tiny LivingEntityRenderer mixin. This wrapper then handles equipment,
 * held items and compatible secondary layers. Ghost-compatible passes are
 * routed through Mirage-owned colour-only RenderTypes: they still depth-test
 * against the world, but never write depth, so a hologram cannot punch holes
 * into water or other translucent world geometry.</p>
 */
public final class ProjectionRenderBuffers {
    private static final Pattern TEXTURE_LOCATION = Pattern.compile(
            "([a-z0-9_.-]+:[a-z0-9_./-]+\\.png)",
            Pattern.CASE_INSENSITIVE
    );

    private ProjectionRenderBuffers() {
    }

    public static MultiBufferSource wrap(MultiBufferSource delegate, ProjectionSettings settings) {
        ProjectionSettings safe = settings == null ? ProjectionSettings.DEFAULT : settings.sanitized();
        if (safe.opacityPercent() >= 100 && safe.tintRgb() == 0xFFFFFF) {
            return delegate;
        }
        if (delegate instanceof ProjectionBufferSource existing && existing.settings().equals(safe)) {
            return delegate;
        }
        return new ProjectionBufferSource(delegate, safe);
    }

    /**
     * Adds a held-item-specific RenderType normalizer without applying Tint/Ghost
     * twice. Entity renderers already receive {@link #wrap}; ItemInHandRenderer
     * can nevertheless ask for the raw block chunk layers (solid/cutout/etc.)
     * instead of the entity sheets that the generic wrapper recognizes. Those
     * raw layers write depth and were the last source of the water-hole bug.
     */
    public static MultiBufferSource wrapHeldItem(MultiBufferSource delegate, ProjectionSettings settings) {
        ProjectionSettings safe = settings == null ? ProjectionSettings.DEFAULT : settings.sanitized();
        if (safe.opacityPercent() >= 100) {
            return delegate;
        }
        MultiBufferSource projection = wrap(delegate, safe);
        return new HeldItemBufferSource(projection, safe);
    }

    public static int tintedArgb(ProjectionSettings settings, int sourceRgb) {
        ProjectionSettings safe = settings == null ? ProjectionSettings.DEFAULT : settings.sanitized();
        int red = multiplyChannel((sourceRgb >> 16) & 0xFF, (safe.tintRgb() >> 16) & 0xFF);
        int green = multiplyChannel((sourceRgb >> 8) & 0xFF, (safe.tintRgb() >> 8) & 0xFF);
        int blue = multiplyChannel(sourceRgb & 0xFF, safe.tintRgb() & 0xFF);
        int alpha = Mth.clamp(Math.round(safe.opacity() * 255.0F), 0, 255);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int multiplyChannel(int source, int tint) {
        return Mth.clamp(Math.round(source * (tint / 255.0F)), 0, 255);
    }

    private record ProjectionBufferSource(
            MultiBufferSource delegate,
            ProjectionSettings settings
    ) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType requested) {
            RenderType effective = remapForGhostEffect(requested, settings);
            return new ProjectionVertexConsumer(delegate.getBuffer(effective), settings);
        }
    }

    private record HeldItemBufferSource(
            MultiBufferSource delegate,
            ProjectionSettings settings
    ) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType requested) {
            return delegate.getBuffer(remapHeldItemForGhostEffect(requested, settings));
        }
    }

    private static RenderType remapHeldItemForGhostEffect(RenderType requested, ProjectionSettings settings) {
        if (settings.opacityPercent() >= 100) {
            return requested;
        }

        // ItemRenderer may ask for chunk-style layers even when it is rendering
        // a held ItemStack. In a projection these layers must never keep their
        // normal depth-writing state: every block/item model is sampled from
        // the block atlas and belongs in Mirage's colour-only item pass.
        if (requested == RenderType.solid()
                || requested == RenderType.cutoutMipped()
                || requested == RenderType.cutout()
                || requested == RenderType.translucent()
                || requested == RenderType.translucentMovingBlock()
                || requested == Sheets.solidBlockSheet()
                || requested == Sheets.cutoutBlockSheet()
                || requested == Sheets.translucentItemSheet()) {
            return ProjectionRenderTypes.ghostItem(InventoryMenu.BLOCK_ATLAS);
        }

        // Preserve the explicit atlas-backed special cases already understood
        // by the normal projection wrapper (shield/banner/trims) and recover
        // texture-backed entity/item layers when possible.
        return remapForGhostEffect(requested, settings);
    }

    private static RenderType remapForGhostEffect(RenderType requested, ProjectionSettings settings) {
        if (settings.opacityPercent() >= 100) {
            return requested;
        }

        // Held blocks and normal items all ultimately sample the block atlas.
        // Route them through Mirage's colour-only ghost pass so they cannot
        // stamp depth over water or other translucent world geometry.
        if (requested == Sheets.solidBlockSheet()
                || requested == Sheets.cutoutBlockSheet()
                || requested == Sheets.translucentItemSheet()) {
            return ProjectionRenderTypes.ghostItem(InventoryMenu.BLOCK_ATLAS);
        }

        if (requested == Sheets.shieldSheet()) {
            return ProjectionRenderTypes.ghostEntity(Sheets.SHIELD_SHEET);
        }
        if (requested == Sheets.bannerSheet()) {
            return ProjectionRenderTypes.ghostEntity(Sheets.BANNER_SHEET);
        }
        if (requested == Sheets.armorTrimsSheet(false) || requested == Sheets.armorTrimsSheet(true)) {
            return ProjectionRenderTypes.ghostEntity(Sheets.ARMOR_TRIMS_SHEET);
        }

        String description = requested.toString();
        String lower = description.toLowerCase(Locale.ROOT);

        // Keep effect-only passes native. Rewiring any of these can break
        // outlines, emissive eyes, beams, text, shadows or world masks.
        if (isSpecialLayer(lower)) {
            return requested;
        }

        // Normal opaque *and already-translucent* entity/item layers are safe
        // to convert when we can recover their texture. Already-translucent
        // vanilla layers still need Mirage's COLOR_WRITE mask; otherwise they
        // can reproduce the water-hole bug despite already having alpha.
        if (!isCompatibleEntityOrItemLayer(lower)) {
            return requested;
        }

        ResourceLocation texture = firstTexture(description);
        if (texture == null) {
            // Unknown/modded RenderTypes fail closed instead of risking the
            // depth buffer. Their vertices still receive Mirage tint/alpha.
            return requested;
        }

        if (lower.contains("item_entity_translucent")) {
            return ProjectionRenderTypes.ghostItem(texture);
        }
        return ProjectionRenderTypes.ghostEntity(texture);
    }

    private static boolean isCompatibleEntityOrItemLayer(String description) {
        return description.contains("entity_solid")
                || description.contains("entity_cutout")
                || description.contains("entity_smooth_cutout")
                || description.contains("entity_translucent")
                || description.contains("armor_cutout")
                || description.contains("armor_trims")
                || description.contains("item_entity_translucent");
    }

    private static boolean isSpecialLayer(String description) {
        return description.contains("glint")
                || description.contains("eyes")
                || description.contains("energy_swirl")
                || description.contains("breeze_wind")
                || description.contains("beacon_beam")
                || description.contains("lightning")
                || description.contains("dragon_rays")
                || description.contains("entity_alpha")
                || description.contains("entity_no_outline")
                || description.contains("entity_shadow")
                || description.contains("leash")
                || description.contains("water_mask")
                || description.contains("outline")
                || description.contains("text")
                || description.contains("lines");
    }

    /**
     * RenderType does not expose its texture in 1.21.1. We use its diagnostic
     * description only as a compatibility fallback for secondary layers. Unlike
     * dev.23, this parser searches for a complete namespaced PNG and cannot be
     * confused by nested Optional brackets.
     */
    private static ResourceLocation firstTexture(String description) {
        Matcher matcher = TEXTURE_LOCATION.matcher(description);
        while (matcher.find()) {
            ResourceLocation resource = ResourceLocation.tryParse(matcher.group(1));
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }

    private static final class ProjectionVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float tintRed;
        private final float tintGreen;
        private final float tintBlue;
        private final float opacity;

        private ProjectionVertexConsumer(VertexConsumer delegate, ProjectionSettings settings) {
            this.delegate = delegate;
            this.tintRed = settings.tintRed();
            this.tintGreen = settings.tintGreen();
            this.tintBlue = settings.tintBlue();
            this.opacity = settings.opacity();
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(
                    Mth.clamp(Math.round(red * tintRed), 0, 255),
                    Mth.clamp(Math.round(green * tintGreen), 0, 255),
                    Mth.clamp(Math.round(blue * tintBlue), 0, 255),
                    Mth.clamp(Math.round(alpha * opacity), 0, 255)
            );
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }
    }
}
