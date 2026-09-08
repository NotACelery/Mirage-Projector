package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Locale;

/**
 * Projection-local render-buffer wrapper for 3D Item and Entity holograms.
 *
 * <p>The wrapper never mutates global shader colour or shared render state. It
 * multiplies colour/alpha per emitted vertex and, when Ghost Effect is active,
 * reroutes the common opaque/cutout textured entity layers through a translucent
 * equivalent. Unknown or special RenderTypes are left structurally intact and
 * still receive vertex tint/alpha, preserving modded renderers and glint as a
 * safe fallback rather than leaking state into unrelated world rendering.</p>
 */
public final class ProjectionRenderBuffers {
    private ProjectionRenderBuffers() {
    }

    public static MultiBufferSource wrap(MultiBufferSource delegate, ProjectionSettings settings) {
        ProjectionSettings safe = settings == null ? ProjectionSettings.DEFAULT : settings.sanitized();
        if (safe.opacityPercent() >= 100 && safe.tintRgb() == 0xFFFFFF) {
            return delegate;
        }
        return new ProjectionBufferSource(delegate, safe);
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

    private static RenderType remapForGhostEffect(RenderType requested, ProjectionSettings settings) {
        if (settings.opacityPercent() >= 100) {
            return requested;
        }

        // Vanilla item/block models use these shared atlas sheets. Preserve the
        // NEW_ENTITY vertex format while switching their opaque sheets to the
        // translucent block-atlas route.
        if (requested == Sheets.solidBlockSheet() || requested == Sheets.cutoutBlockSheet()) {
            return Sheets.translucentCullBlockSheet();
        }

        String description = requested.toString();
        String lower = description.toLowerCase(Locale.ROOT);

        // These already have deliberate blending/texturing behaviour. Replacing
        // them would destroy glint animation, emissive eyes, beams or other
        // special layers. They still receive vertex-level colour/alpha below.
        if (isSpecialOrAlreadyTranslucent(lower)) {
            return requested;
        }

        if (!isCommonOpaqueEntityLayer(lower)) {
            return requested;
        }

        ResourceLocation texture = firstTexture(description);
        if (texture == null) {
            return requested;
        }

        boolean noCull = lower.contains("no_cull") || lower.contains("nocull") || lower.contains("armor_");
        return noCull
                ? RenderType.entityTranslucent(texture, false)
                : RenderType.entityTranslucentCull(texture);
    }

    private static boolean isCommonOpaqueEntityLayer(String description) {
        return description.contains("entity_solid")
                || description.contains("entity_cutout")
                || description.contains("entity_smooth_cutout")
                || description.contains("armor_cutout");
    }

    private static boolean isSpecialOrAlreadyTranslucent(String description) {
        return description.contains("translucent")
                || description.contains("glint")
                || description.contains("eyes")
                || description.contains("energy_swirl")
                || description.contains("beacon_beam")
                || description.contains("lightning")
                || description.contains("dragon_rays")
                || description.contains("entity_alpha")
                || description.contains("entity_no_outline")
                || description.contains("entity_shadow")
                || description.contains("leash")
                || description.contains("text")
                || description.contains("lines");
    }

    /**
     * RenderType has no public texture accessor in 1.21.1. Its diagnostic
     * CompositeState string does contain the canonical first TextureStateShard
     * resource location. Parse only that narrow stable fragment and fail closed
     * to the original RenderType if a modded/custom type exposes something else.
     */
    private static ResourceLocation firstTexture(String description) {
        int marker = description.indexOf("texture[");
        if (marker < 0) {
            return null;
        }
        int start = marker + "texture[".length();
        int endParen = description.indexOf('(', start);
        int endBracket = description.indexOf(']', start);
        int end;
        if (endParen >= 0 && endBracket >= 0) {
            end = Math.min(endParen, endBracket);
        } else {
            end = Math.max(endParen, endBracket);
        }
        if (end <= start) {
            return null;
        }

        String token = description.substring(start, end).trim();
        // Vanilla 1.21.1 TextureStateShard#toString() prints the resource as
        // Optional[minecraft:...]. Keep the parser deliberately narrow and
        // unwrap only that exact diagnostic representation.
        if (token.startsWith("Optional[") && token.endsWith("]")) {
            token = token.substring("Optional[".length(), token.length() - 1).trim();
        }
        return ResourceLocation.tryParse(token);
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
