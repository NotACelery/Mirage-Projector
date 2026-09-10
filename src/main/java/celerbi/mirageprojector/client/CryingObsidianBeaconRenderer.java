package celerbi.mirageprojector.client;

import celerbi.mirageprojector.block.CryingObsidianCrystalBlock;
import celerbi.mirageprojector.crying.CryingObsidianCrystalStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Client-only renderer that keeps vanilla Beacon behavior while allowing Mirage crystals to
 * visually absorb the vertical beam and leak narrow Crying-Obsidian-colored residual rays.
 */
public final class CryingObsidianBeaconRenderer {
    private static final int INNER_PURPLE = 0xFFD7A6FF;
    private static final int OUTER_PURPLE = 0xFF5D197E;
    // dev.56: residual rays are half as wide as the old implementation.
    private static final float RESIDUAL_INNER_RADIUS = 0.05F;
    private static final float RESIDUAL_OUTER_RADIUS = 0.0625F;
    // Residual bursts visually originate near the crystal base rather than its geometric center.
    // 2 px = 2/16 block height; X/Z stay exactly centered.
    private static final double RESIDUAL_ORIGIN_Y = 2.0D / 16.0D;
    private static final int HOLD_TICKS = 20;
    private static final int COLLAPSE_TICKS = 20;

    private CryingObsidianBeaconRenderer() {
    }

    /**
     * @return true when Mirage rendered the Beacon and vanilla rendering should be cancelled.
     */
    public static boolean renderIfCrystalColumn(
            BeaconBlockEntity beacon,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers
    ) {
        Level level = beacon.getLevel();
        if (level == null || beacon.getBeamSections().isEmpty()) {
            return false;
        }

        List<CrystalHit> crystals = findCrystals(level, beacon.getBlockPos());
        if (crystals.isEmpty()) {
            return false;
        }

        long gameTime = level.getGameTime();
        boolean directMatureCluster = crystals.getFirst().stage().isMature()
                && crystals.getFirst().pos().equals(beacon.getBlockPos().above());
        if (!directMatureCluster) {
            renderVerticalBeam(beacon, partialTick, poseStack, buffers, gameTime, crystals);
        }
        for (CrystalHit crystal : crystals) {
            if (crystal.incomingFraction() <= 0.0001F) {
                break;
            }
            renderResidualBeam(level, beacon.getBlockPos(), crystal, partialTick, poseStack, buffers, gameTime);
            if (crystal.stage().verticalTransmission() <= 0.0001F) {
                break;
            }
        }
        return true;
    }

    private static List<CrystalHit> findCrystals(Level level, BlockPos beaconPos) {
        List<CrystalHit> result = new ArrayList<>();
        float incoming = 1.0F;

        for (int y = beaconPos.getY() + 1; y < level.getMaxBuildHeight(); y++) {
            BlockPos pos = new BlockPos(beaconPos.getX(), y, beaconPos.getZ());
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof CryingObsidianCrystalBlock crystal)) {
                continue;
            }

            result.add(new CrystalHit(pos, crystal.stage(), incoming));
            incoming *= crystal.stage().verticalTransmission();
            if (incoming <= 0.0001F) {
                break;
            }
        }
        return result;
    }

    private static void renderVerticalBeam(
            BeaconBlockEntity beacon,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            long gameTime,
            List<CrystalHit> crystals
    ) {
        List<BeaconBlockEntity.BeaconBeamSection> sections = beacon.getBeamSections();
        BlockPos beaconPos = beacon.getBlockPos();
        int sectionStart = 0;
        float transmission = 1.0F;
        int crystalIndex = 0;

        for (int sectionIndex = 0; sectionIndex < sections.size() && transmission > 0.0001F; sectionIndex++) {
            BeaconBlockEntity.BeaconBeamSection section = sections.get(sectionIndex);
            float sectionEnd = sectionIndex == sections.size() - 1
                    ? BeaconRenderer.MAX_RENDER_Y
                    : sectionStart + section.getHeight();
            float cursor = sectionStart;

            while (crystalIndex < crystals.size()) {
                CrystalHit crystal = crystals.get(crystalIndex);
                float crystalBottom = crystal.pos().getY() - beaconPos.getY();
                float crystalTop = crystalBottom + 1.0F;
                if (crystalTop <= cursor) {
                    crystalIndex++;
                    continue;
                }
                if (crystalBottom > sectionEnd) {
                    break;
                }

                // Stop at pixel 0 of the crystal block: never draw the vanilla beam
                // through any part of a bud/cluster. Buds resume above at lower energy.
                if (crystalBottom > cursor) {
                    int attenuatedColor = attenuateColor(section.getColor(), transmission);
                    renderBeamSegment(
                            poseStack,
                            buffers,
                            partialTick,
                            gameTime,
                            cursor,
                            crystalBottom,
                            attenuatedColor,
                            attenuatedColor,
                            transmission,
                            0.20F,
                            0.25F
                    );
                }

                transmission *= crystal.stage().verticalTransmission();
                cursor = Math.max(cursor, crystalTop);
                crystalIndex++;
                if (transmission <= 0.0001F) {
                    break;
                }
            }

            if (transmission > 0.0001F && sectionEnd > cursor) {
                int attenuatedColor = attenuateColor(section.getColor(), transmission);
                renderBeamSegment(
                        poseStack,
                        buffers,
                        partialTick,
                        gameTime,
                        cursor,
                        sectionEnd,
                        attenuatedColor,
                        attenuatedColor,
                        transmission,
                        0.20F,
                        0.25F
                );
            }
            sectionStart += section.getHeight();
        }
    }

    private static void renderResidualBeam(
            Level level,
            BlockPos beaconPos,
            CrystalHit crystal,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            long gameTime
    ) {
        CryingObsidianCrystalStage stage = crystal.stage();
        int period = stage.residualCycleTicks();
        long offset = Math.floorMod(mix64(crystal.pos().asLong()), period);
        long shiftedTime = gameTime + offset;
        int phaseTick = (int) Math.floorMod(shiftedTime, period);
        int activeTicks = HOLD_TICKS + COLLAPSE_TICKS;
        if (phaseTick >= activeTicks) {
            return;
        }

        long eventIndex = Math.floorDiv(shiftedTime, period);
        long seed = mix64(crystal.pos().asLong() ^ (eventIndex * 0x9E3779B97F4A7C15L));
        Vec3 direction = residualDirection(seed);
        float maxLength = residualLength(stage, seed);

        Vec3 worldStart = new Vec3(
                crystal.pos().getX() + 0.5D,
                crystal.pos().getY() + RESIDUAL_ORIGIN_Y,
                crystal.pos().getZ() + 0.5D
        );
        Vec3 unclippedEnd = worldStart.add(direction.scale(maxLength));
        Vec3 clippedEnd = clipResidual(level, worldStart, unclippedEnd);
        float collisionLength = (float) worldStart.distanceTo(clippedEnd);
        if (collisionLength <= 0.05F) {
            return;
        }

        float lengthFactor;
        float alpha;
        if (phaseTick < HOLD_TICKS) {
            // The burst appears instantly at full length and remains stable for one second.
            lengthFactor = 1.0F;
            alpha = 1.0F;
        } else {
            float t = (phaseTick - HOLD_TICKS + partialTick) / COLLAPSE_TICKS;
            float remaining = 1.0F - Mth.clamp(t, 0.0F, 1.0F);
            lengthFactor = smoothStep(remaining);
            alpha = remaining;
        }

        float visibleLength = collisionLength * lengthFactor;
        if (visibleLength <= 0.01F || alpha <= 0.01F) {
            return;
        }

        float energyScale = Mth.clamp(crystal.incomingFraction(), 0.15F, 1.0F);
        alpha *= energyScale;

        poseStack.pushPose();
        // Position the residual ray at the exact X/Z center before rotating it.
        // Vertically the visual origin is intentionally near the crystal base (pixel 2),
        // which reads better than the geometric midpoint (pixel 8).
        poseStack.translate(
                crystal.pos().getX() - beaconPos.getX() + 0.5,
                crystal.pos().getY() - beaconPos.getY() + RESIDUAL_ORIGIN_Y,
                crystal.pos().getZ() - beaconPos.getZ() + 0.5
        );
        Quaternionf rotation = new Quaternionf().rotationTo(
                new Vector3f(0.0F, 1.0F, 0.0F),
                new Vector3f((float) direction.x, (float) direction.y, (float) direction.z)
        );
        poseStack.mulPose(rotation);
        renderBeamSegmentLocal(
                poseStack,
                buffers,
                partialTick,
                gameTime,
                0.0F,
                visibleLength,
                INNER_PURPLE,
                OUTER_PURPLE,
                alpha,
                RESIDUAL_INNER_RADIUS * stage.residualScale(),
                RESIDUAL_OUTER_RADIUS * stage.residualScale(),
                false
        );
        poseStack.popPose();
    }

    private static Vec3 clipResidual(Level level, Vec3 start, Vec3 end) {
        Entity contextEntity = Minecraft.getInstance().player;
        if (contextEntity == null) {
            return end;
        }
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, contextEntity));
        return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
    }

    private static Vec3 residualDirection(long seed) {
        double u = unit(seed);
        double v = unit(mix64(seed + 0x632BE59BD9B4E019L));
        double yaw = u * Math.PI * 2.0;
        double pitch = Math.toRadians(-25.0 + v * 50.0);
        double horizontal = Math.cos(pitch);
        return new Vec3(Math.cos(yaw) * horizontal, Math.sin(pitch), Math.sin(yaw) * horizontal).normalize();
    }

    private static float residualLength(CryingObsidianCrystalStage stage, long seed) {
        float max = stage.residualMaxLength();
        float min = max * 0.75F;
        return Mth.lerp((float) unit(mix64(seed ^ 0xD1B54A32D192ED03L)), min, max);
    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private static double unit(long seed) {
        return (double) (seed >>> 11) * 0x1.0p-53;
    }

    private static long mix64(long value) {
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private static void renderBeamSegment(
            PoseStack poseStack,
            MultiBufferSource buffers,
            float partialTick,
            long gameTime,
            float startY,
            float endY,
            int innerColor,
            int outerColor,
            float alpha,
            float innerRadius,
            float outerRadius
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 0.0);
        renderBeamSegmentLocal(
                poseStack,
                buffers,
                partialTick,
                gameTime,
                startY,
                endY,
                innerColor,
                outerColor,
                alpha,
                innerRadius,
                outerRadius,
                true
        );
        poseStack.popPose();
    }

    private static void renderBeamSegmentLocal(
            PoseStack poseStack,
            MultiBufferSource buffers,
            float partialTick,
            long gameTime,
            float startY,
            float endY,
            int innerColor,
            int outerColor,
            float alpha,
            float innerRadius,
            float outerRadius,
            boolean centerOnBlock
    ) {
        if (endY <= startY || alpha <= 0.0F) {
            return;
        }

        float height = endY - startY;
        poseStack.pushPose();
        if (centerOnBlock) {
            poseStack.translate(0.5, 0.0, 0.5);
        }
        float animation = (float) Math.floorMod(gameTime, 40L) + partialTick;
        float signedAnimation = -animation;
        float textureOffset = Mth.frac(signedAnimation * 0.2F - (float) Mth.floor(signedAnimation * 0.1F));

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(animation * 2.25F - 45.0F));
        float innerV0 = -1.0F + textureOffset;
        float innerV1 = height * (0.5F / innerRadius) + innerV0;
        VertexConsumer inner = buffers.getBuffer(RenderType.beaconBeam(BeaconRenderer.BEAM_LOCATION, false));
        renderPart(
                poseStack.last(),
                inner,
                withAlpha(innerColor, Math.round(255.0F * alpha)),
                startY,
                endY,
                0.0F,
                innerRadius,
                innerRadius,
                0.0F,
                -innerRadius,
                0.0F,
                0.0F,
                -innerRadius,
                0.0F,
                1.0F,
                innerV1,
                innerV0
        );
        poseStack.popPose();

        float outerV0 = -1.0F + textureOffset;
        float outerV1 = height + outerV0;
        VertexConsumer outer = buffers.getBuffer(RenderType.beaconBeam(BeaconRenderer.BEAM_LOCATION, true));
        renderPart(
                poseStack.last(),
                outer,
                withAlpha(outerColor, Math.round(32.0F * alpha)),
                startY,
                endY,
                -outerRadius,
                -outerRadius,
                outerRadius,
                -outerRadius,
                -outerRadius,
                outerRadius,
                outerRadius,
                outerRadius,
                0.0F,
                1.0F,
                outerV1,
                outerV0
        );
        poseStack.popPose();
    }

    private static int attenuateColor(int color, float transmission) {
        float t = Mth.clamp(transmission, 0.0F, 1.0F);
        int red = Math.round(((color >>> 16) & 0xFF) * t);
        int green = Math.round(((color >>> 8) & 0xFF) * t);
        int blue = Math.round((color & 0xFF) * t);
        return FastColor.ARGB32.color(255, red, green, blue);
    }

    private static int withAlpha(int color, int alpha) {
        return FastColor.ARGB32.color(Mth.clamp(alpha, 0, 255), color);
    }

    private static void renderPart(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int color,
            float minY,
            float maxY,
            float x0,
            float z0,
            float x1,
            float z1,
            float x2,
            float z2,
            float x3,
            float z3,
            float minU,
            float maxU,
            float minV,
            float maxV
    ) {
        renderQuad(pose, consumer, color, minY, maxY, x0, z0, x1, z1, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, color, minY, maxY, x2, z2, x3, z3, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, color, minY, maxY, x1, z1, x2, z2, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, color, minY, maxY, x3, z3, x0, z0, minU, maxU, minV, maxV);
    }

    private static void renderQuad(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int color,
            float minY,
            float maxY,
            float x0,
            float z0,
            float x1,
            float z1,
            float minU,
            float maxU,
            float minV,
            float maxV
    ) {
        addVertex(pose, consumer, color, maxY, x0, z0, maxU, minV);
        addVertex(pose, consumer, color, minY, x0, z0, maxU, maxV);
        addVertex(pose, consumer, color, minY, x1, z1, minU, maxV);
        addVertex(pose, consumer, color, maxY, x1, z1, minU, minV);
    }

    private static void addVertex(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int color,
            float y,
            float x,
            float z,
            float u,
            float v
    ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private record CrystalHit(BlockPos pos, CryingObsidianCrystalStage stage, float incomingFraction) {
    }
}
