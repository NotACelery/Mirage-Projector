package celerbi.mirageprojector.mixin.client;

import celerbi.mirageprojector.client.ProjectionRenderContext;
import celerbi.mirageprojector.client.ProjectionRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
            return ProjectionRenderContext.lateDepthStableGhost()
                    ? ProjectionRenderTypes.lateGhostEntity(texture)
                    : ProjectionRenderTypes.ghostEntity(texture);
        }
        return RenderType.armorCutoutNoCull(texture);
    }
}
