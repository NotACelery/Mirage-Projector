package celerbi.mirageprojector.mixin.client;

import celerbi.mirageprojector.client.ProjectionRenderContext;
import celerbi.mirageprojector.client.ProjectionRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes vanilla/NeoForge humanoid armor participate in Mirage's ghost pass.
 *
 * <p>Armor is rendered after the base body and chooses its RenderType inside
 * HumanoidArmorLayer. The two descriptors cover the vanilla HumanoidModel path
 * and NeoForge's more generic Model hook path. ProjectionRenderContext keeps
 * the redirect inert for every normal world entity.</p>
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {
    @Redirect(
            method = {
                    "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/HumanoidModel;ILnet/minecraft/resources/ResourceLocation;)V",
                    "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
            ),
            require = 1
    )
    private RenderType mirageProjector$ghostArmor(ResourceLocation texture) {
        if (ProjectionRenderContext.ghostActive()) {
            return ProjectionRenderTypes.ghostEntity(texture);
        }
        return RenderType.armorCutoutNoCull(texture);
    }
}
