package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;

public final class ProjectionRenderBuffers {
    private static final Pattern TEXTURE_LOCATION = Pattern.compile(
            "([a-z0-9_.-]+:[a-z0-9_./-]+\\.png)",
            Pattern.CASE_INSENSITIVE
    );
    private static final ResourceLocation CREATE_NETHERITE_BACKTANK = ResourceLocation.fromNamespaceAndPath(
            "create",
            "netherite_backtank"
    );
    private static final ResourceLocation CREATE_NETHERITE_DIVING_LAYER_1 = ResourceLocation.fromNamespaceAndPath(
            "create",
            "textures/models/armor/netherite_diving_layer_1.png"
    );
    private static final ResourceLocation CREATE_NETHERITE_DIVING_LAYER_2 = ResourceLocation.fromNamespaceAndPath(
            "create",
            "textures/models/armor/netherite_diving_layer_2.png"
    );

    private ProjectionRenderBuffers() {
    }

    public static MultiBufferSource wrap(MultiBufferSource delegate, ProjectionSettings settings) {
        return wrap(delegate, settings, false);
    }

    public static MultiBufferSource wrap(
            MultiBufferSource delegate,
            ProjectionSettings settings,
            boolean lateDepthStableGhost
    ) {
        ProjectionSettings safe = settings == null ? ProjectionSettings.DEFAULT : settings.sanitized();
        if (safe.opacityPercent() >= 100 && safe.tintRgb() == 0xFFFFFF) {
            return delegate;
        }
        if (delegate instanceof ProjectionBufferSource existing
                && existing.settings().equals(safe)
                && existing.lateDepthStableGhost() == lateDepthStableGhost) {
            return delegate;
        }
        return new ProjectionBufferSource(delegate, safe, lateDepthStableGhost);
    }

    public static MultiBufferSource wrapHeldItem(
            MultiBufferSource delegate,
            ProjectionSettings settings,
            boolean lateDepthStableGhost
    ) {
        ProjectionSettings safe = settings == null ? ProjectionSettings.DEFAULT : settings.sanitized();
        if (safe.opacityPercent() >= 100) {
            return delegate;
        }
        MultiBufferSource projection = wrap(delegate, safe, lateDepthStableGhost);
        return new HeldItemBufferSource(projection, safe, lateDepthStableGhost);
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

    private static final class ProjectionBufferSource implements MultiBufferSource {
        private final MultiBufferSource delegate;
        private final ProjectionSettings settings;
        private final boolean lateDepthStableGhost;
        private int createBacktankArmorPass;
        private boolean suppressNextBacktankGlint;

        private ProjectionBufferSource(
                MultiBufferSource delegate,
                ProjectionSettings settings,
                boolean lateDepthStableGhost
        ) {
            this.delegate = delegate;
            this.settings = settings;
            this.lateDepthStableGhost = lateDepthStableGhost;
        }

        private ProjectionSettings settings() {
            return settings;
        }

        private boolean lateDepthStableGhost() {
            return lateDepthStableGhost;
        }

        @Override
        public VertexConsumer getBuffer(RenderType requested) {
            VertexConsumer createBacktank = createBacktankBuffer(requested);
            if (createBacktank != null) {
                return createBacktank;
            }
            RenderType effective = remapForGhostEffect(requested, settings, lateDepthStableGhost);
            return new ProjectionVertexConsumer(delegate.getBuffer(effective), settings, settings.opacity());
        }

        private VertexConsumer createBacktankBuffer(RenderType requested) {
            if (settings.opacityPercent() >= 100 || !activeCreateNetheriteBacktank()) {
                return null;
            }

            if (requested == RenderType.armorEntityGlint() && suppressNextBacktankGlint) {
                suppressNextBacktankGlint = false;
                return DiscardingVertexConsumer.INSTANCE;
            }

            String description = requested.toString();
            String lower = description.toLowerCase(Locale.ROOT);
            ResourceLocation texture = firstTexture(description);

            if (CREATE_NETHERITE_DIVING_LAYER_2.equals(texture)) {
                createBacktankArmorPass = Math.max(createBacktankArmorPass, 1);
                suppressNextBacktankGlint = true;
                return DiscardingVertexConsumer.INSTANCE;
            }
            if (CREATE_NETHERITE_DIVING_LAYER_1.equals(texture)) {
                createBacktankArmorPass = Math.max(createBacktankArmorPass, 2);
                suppressNextBacktankGlint = false;
                RenderType effective = ghostEntity(CREATE_NETHERITE_DIVING_LAYER_1, lateDepthStableGhost);
                return new ProjectionVertexConsumer(delegate.getBuffer(effective), settings, settings.opacity());
            }

            if (texture == null && lower.contains("armor_cutout_no_cull") && createBacktankArmorPass < 2) {
                createBacktankArmorPass++;
                if (createBacktankArmorPass == 1) {
                    suppressNextBacktankGlint = true;
                    return DiscardingVertexConsumer.INSTANCE;
                }
                suppressNextBacktankGlint = false;
                RenderType effective = ghostEntity(CREATE_NETHERITE_DIVING_LAYER_1, lateDepthStableGhost);
                return new ProjectionVertexConsumer(delegate.getBuffer(effective), settings, settings.opacity());
            }

            return null;
        }
    }

    private record HeldItemBufferSource(
            MultiBufferSource delegate,
            ProjectionSettings settings,
            boolean lateDepthStableGhost
    ) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType requested) {
            return delegate.getBuffer(remapHeldItemForGhostEffect(requested, settings, lateDepthStableGhost));
        }
    }

    private static RenderType remapHeldItemForGhostEffect(
            RenderType requested,
            ProjectionSettings settings,
            boolean lateDepthStableGhost
    ) {
        if (settings.opacityPercent() >= 100) {
            return requested;
        }

        if (requested == RenderType.solid()
                || requested == RenderType.cutoutMipped()
                || requested == RenderType.cutout()
                || requested == RenderType.translucent()
                || requested == RenderType.translucentMovingBlock()
                || requested == Sheets.solidBlockSheet()
                || requested == Sheets.cutoutBlockSheet()
                || requested == Sheets.translucentItemSheet()) {
            return ghostItem(InventoryMenu.BLOCK_ATLAS, lateDepthStableGhost);
        }

        return remapForGhostEffect(requested, settings, lateDepthStableGhost);
    }

    private static RenderType remapForGhostEffect(
            RenderType requested,
            ProjectionSettings settings,
            boolean lateDepthStableGhost
    ) {
        if (settings.opacityPercent() >= 100) {
            return requested;
        }

        if (requested == RenderType.solid()
                || requested == RenderType.cutoutMipped()
                || requested == RenderType.cutout()
                || requested == RenderType.translucent()
                || requested == RenderType.translucentMovingBlock()
                || requested == Sheets.solidBlockSheet()
                || requested == Sheets.cutoutBlockSheet()
                || requested == Sheets.translucentItemSheet()) {
            return ghostItem(InventoryMenu.BLOCK_ATLAS, lateDepthStableGhost);
        }

        if (requested == Sheets.shieldSheet()) {
            return ghostEntity(Sheets.SHIELD_SHEET, lateDepthStableGhost);
        }
        if (requested == Sheets.bannerSheet()) {
            return ghostEntity(Sheets.BANNER_SHEET, lateDepthStableGhost);
        }
        if (requested == Sheets.armorTrimsSheet(false) || requested == Sheets.armorTrimsSheet(true)) {
            return ghostEntity(Sheets.ARMOR_TRIMS_SHEET, lateDepthStableGhost);
        }

        String description = requested.toString();
        String lower = description.toLowerCase(Locale.ROOT);

        if (isSpecialLayer(lower)) {
            return requested;
        }

        ResourceLocation texture = firstTexture(description);
        if (texture != null) {
            if (lower.contains("item") || lower.contains("block")) {
                return ghostItem(texture, lateDepthStableGhost);
            }
            return ghostEntity(texture, lateDepthStableGhost);
        }

        return requested;
    }

    private static boolean activeCreateNetheriteBacktank() {
        LivingEntity entity = ProjectionRenderContext.activeEntity();
        if (entity == null) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(entity.getItemBySlot(EquipmentSlot.CHEST).getItem());
        return CREATE_NETHERITE_BACKTANK.equals(itemId);
    }

    private static RenderType ghostEntity(ResourceLocation texture, boolean lateDepthStableGhost) {
        return lateDepthStableGhost
                ? ProjectionRenderTypes.lateGhostEntity(texture)
                : ProjectionRenderTypes.ghostEntity(texture);
    }

    private static RenderType ghostItem(ResourceLocation texture, boolean lateDepthStableGhost) {
        return lateDepthStableGhost
                ? ProjectionRenderTypes.lateGhostItem(texture)
                : ProjectionRenderTypes.ghostItem(texture);
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

    private static final class DiscardingVertexConsumer implements VertexConsumer {
        private static final DiscardingVertexConsumer INSTANCE = new DiscardingVertexConsumer();

        private DiscardingVertexConsumer() {
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }
    }

    private static final class ProjectionVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float tintRed;
        private final float tintGreen;
        private final float tintBlue;
        private final float opacity;

        private ProjectionVertexConsumer(VertexConsumer delegate, ProjectionSettings settings, float opacity) {
            this.delegate = delegate;
            this.tintRed = settings.tintRed();
            this.tintGreen = settings.tintGreen();
            this.tintBlue = settings.tintBlue();
            this.opacity = Mth.clamp(opacity, 0.0F, 1.0F);
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
