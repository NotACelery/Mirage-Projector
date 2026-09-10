package celerbi.mirageprojector.mixin.client;

import celerbi.mirageprojector.client.CryingObsidianBeaconRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconRenderer.class)
public abstract class BeaconRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void mirageProjector$renderCryingObsidianOptics(
            BeaconBlockEntity beacon,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay,
            CallbackInfo ci
    ) {
        if (CryingObsidianBeaconRenderer.renderIfCrystalColumn(beacon, partialTick, poseStack, buffers)) {
            ci.cancel();
        }
    }
}
