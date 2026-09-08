package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * In-world editing preview for the conservative projection clearance envelope.
 * It exists only while a Mirage Projector screen is open and never changes blocks.
 */
@EventBusSubscriber(modid = MirageProjector.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ProjectionClearancePreviewRenderer {
    private static BlockPos projectorPos;
    private static ProjectionClearance.Result result = ProjectionClearance.Result.UNKNOWN;

    private ProjectionClearancePreviewRenderer() {
    }

    public static void show(BlockPos pos, ProjectionClearance.Result newResult) {
        projectorPos = pos == null ? null : pos.immutable();
        result = newResult == null ? ProjectionClearance.Result.UNKNOWN : newResult;
    }

    public static void clear() {
        projectorPos = null;
        result = ProjectionClearance.Result.UNKNOWN;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES
                || projectorPos == null
                || result == null
                || !result.known()
                || !result.hasEnvelope()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            clear();
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        VertexConsumer consumer = minecraft.renderBuffers().bufferSource().getBuffer(RenderType.lines());

        AABB envelope = result.envelope().move(-camera.x, -camera.y, -camera.z);
        if (result.clear()) {
            LevelRenderer.renderLineBox(poseStack, consumer, envelope, 0.35F, 1.0F, 0.55F, 0.9F);
        } else {
            LevelRenderer.renderLineBox(poseStack, consumer, envelope, 1.0F, 0.65F, 0.2F, 0.95F);
        }

        for (BlockPos blocked : result.blockedPreview()) {
            AABB box = new AABB(blocked).inflate(0.002D).move(-camera.x, -camera.y, -camera.z);
            LevelRenderer.renderLineBox(poseStack, consumer, box, 1.0F, 0.15F, 0.15F, 1.0F);
        }
    }
}
