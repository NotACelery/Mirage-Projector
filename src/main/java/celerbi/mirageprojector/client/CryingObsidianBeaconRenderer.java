package celerbi.mirageprojector.client;

import celerbi.mirageprojector.CoreBoosterMaterial;
import celerbi.mirageprojector.block.CoreBoosterBlock;
import celerbi.mirageprojector.block.CryingObsidianCrystalBlock;
import celerbi.mirageprojector.crying.BeaconRelayState;
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

public final class CryingObsidianBeaconRenderer {
    private static final int INNER_PURPLE = 0xFFD7A6FF;
    private static final int OUTER_PURPLE = 0xFF5D197E;

    private static final float RESIDUAL_INNER_RADIUS = 0.05F;
    private static final float RESIDUAL_OUTER_RADIUS = 0.0625F;

    private static final double RESIDUAL_ORIGIN_Y = 2.0D / 16.0D;
    private static final float BOOSTER_EFFECT_Y = 8.5F / 16.0F;
    private static final int HOLD_TICKS = 20;
    private static final int COLLAPSE_TICKS = 20;

    private CryingObsidianBeaconRenderer() {
    }

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

        ColumnScan scan = scanColumn(level, beacon.getBlockPos());
        if (!scan.modified()) {
            return false;
        }

        long gameTime = level.getGameTime();
        boolean directMatureCluster = !scan.crystals().isEmpty()
                && scan.crystals().getFirst().stage().isMature()
                && scan.crystals().getFirst().pos().equals(beacon.getBlockPos().above());
        if (!directMatureCluster) {
            renderVerticalBeam(beacon, partialTick, poseStack, buffers, gameTime, scan.events());
        }
        for (CrystalHit crystal : scan.crystals()) {
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

    private static ColumnScan scanColumn(Level level, BlockPos beaconPos) {
        List<ColumnEvent> events = new ArrayList<>();
        List<CrystalHit> crystals = new ArrayList<>();
        BeaconRelayState relay = BeaconRelayState.BASE;
        float transmission = 1.0F;

        for (int y = beaconPos.getY() + 1; y < level.getMaxBuildHeight(); y++) {
            BlockPos pos = new BlockPos(beaconPos.getX(), y, beaconPos.getZ());
            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof CoreBoosterBlock && state.hasProperty(CoreBoosterBlock.MATERIAL)) {
                CoreBoosterMaterial material = state.getValue(CoreBoosterBlock.MATERIAL);
                if (material.present()) {
                    relay = relay.apply(material);
                    events.add(ColumnEvent.booster(pos, material));
                }
            }

            if (state.getBlock() instanceof CryingObsidianCrystalBlock crystal) {
                CrystalHit hit = new CrystalHit(pos, crystal.stage(), transmission, relay);
                crystals.add(hit);
                events.add(ColumnEvent.crystal(pos, crystal.stage()));
                transmission *= crystal.stage().verticalTransmission();
                if (transmission <= 0.0001F) {
                    break;
                }
            }
        }

        return new ColumnScan(events, crystals);
    }

    private static void renderVerticalBeam(
            BeaconBlockEntity beacon,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            long gameTime,
            List<ColumnEvent> events
    ) {
        List<BeaconBlockEntity.BeaconBeamSection> sections = beacon.getBeamSections();
        BlockPos beaconPos = beacon.getBlockPos();
        int sectionStart = 0;
        int eventIndex = 0;
        float transmission = 1.0F;
        BeaconRelayState relay = BeaconRelayState.BASE;

        for (int sectionIndex = 0; sectionIndex < sections.size() && transmission > 0.0001F; sectionIndex++) {
            BeaconBlockEntity.BeaconBeamSection section = sections.get(sectionIndex);
            float sectionEnd = sectionIndex == sections.size() - 1
                    ? BeaconRenderer.MAX_RENDER_Y
                    : sectionStart + section.getHeight();
            float cursor = sectionStart;

            while (eventIndex < events.size()) {
                ColumnEvent event = events.get(eventIndex);
                float eventBottom = event.pos().getY() - beaconPos.getY();
                if (eventBottom < cursor) {
                    eventIndex++;
                    continue;
                }
                if (eventBottom > sectionEnd) {
                    break;
                }

                if (eventBottom > cursor) {
                    renderVerticalSegment(
                            poseStack,
                            buffers,
                            partialTick,
                            gameTime,
                            cursor,
                            eventBottom,
                            section.getColor(),
                            transmission,
                            relay
                    );
                    cursor = eventBottom;
                }

                if (event.material() != CoreBoosterMaterial.EMPTY) {
                    float effectY = eventBottom + BOOSTER_EFFECT_Y;
                    if (effectY > cursor) {
                        renderVerticalSegment(
                                poseStack,
                                buffers,
                                partialTick,
                                gameTime,
                                cursor,
                                Math.min(effectY, sectionEnd),
                                section.getColor(),
                                transmission,
                                relay
                        );
                    }
                    relay = relay.apply(event.material());
                    cursor = effectY;
                } else if (event.stage() != null) {
                    transmission *= event.stage().verticalTransmission();
                    cursor = eventBottom + event.stage().verticalBeamResumeOffset();
                }
                eventIndex++;
                if (transmission <= 0.0001F) {
                    break;
                }
            }

            if (transmission > 0.0001F && sectionEnd > cursor) {
                renderVerticalSegment(
                        poseStack,
                        buffers,
                        partialTick,
                        gameTime,
                        cursor,
                        sectionEnd,
                        section.getColor(),
                        transmission,
                        relay
                );
            }
            sectionStart += section.getHeight();
        }
    }

    private static void renderVerticalSegment(
            PoseStack poseStack,
            MultiBufferSource buffers,
            float partialTick,
            long gameTime,
            float startY,
            float endY,
            int sectionColor,
            float transmission,
            BeaconRelayState relay
    ) {
        int attenuated = attenuateColor(sectionColor, transmission);
        int boosted = boostColor(attenuated, relay.brightnessScale());
        renderBeamSegment(
                poseStack,
                buffers,
                partialTick,
                gameTime,
                startY,
                endY,
                boosted,
                boosted,
                transmission,
                relay.innerRadius(0.20F),
                relay.outerRadius(0.25F),
                relay.signedRotationSpeed()
        );
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
        float excitation = crystal.relay().reflectedExcitationScale();
        int period = Math.max(45, Math.round(stage.residualCycleTicks() / excitation));
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
        float maxLength = residualLength(stage, seed, crystal.relay());

        Vec3 worldStart = new Vec3(
                crystal.pos().getX() + 0.5D,
                crystal.pos().getY() + RESIDUAL_ORIGIN_Y,
                crystal.pos().getZ() + 0.5D
        );
        Vec3 unclippedEnd = worldStart.add(direction.scale(maxLength));
        Vec3 clippedEnd = clipResidual(level, crystal.pos(), worldStart, unclippedEnd);
        float collisionLength = (float) worldStart.distanceTo(clippedEnd);
        if (collisionLength <= 0.05F) {
            return;
        }

        float lengthFactor;
        float alpha;
        if (phaseTick < HOLD_TICKS) {

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
        alpha *= energyScale * Math.min(1.35F, crystal.relay().reflectedBrightnessScale());
        alpha = Mth.clamp(alpha, 0.0F, 1.0F);

        poseStack.pushPose();

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
                RESIDUAL_INNER_RADIUS * stage.residualScale() * crystal.relay().reflectedInnerRadiusScale(),
                RESIDUAL_OUTER_RADIUS * stage.residualScale() * crystal.relay().reflectedOuterRadiusScale(),
                false,
                crystal.relay().signedRotationSpeed()
        );
        poseStack.popPose();
    }

    private static Vec3 clipResidual(Level level, BlockPos sourcePos, Vec3 start, Vec3 end) {
        Entity contextEntity = Minecraft.getInstance().player;
        if (contextEntity == null) {
            return end;
        }
        Vec3 ray = end.subtract(start);
        double exit = 1.0D;
        if (Math.abs(ray.x) > 1.0E-7D) {
            double boundary = ray.x > 0.0D ? sourcePos.getX() + 1.0D : sourcePos.getX();
            double t = (boundary - start.x) / ray.x;
            if (t >= 0.0D) {
                exit = Math.min(exit, t);
            }
        }
        if (Math.abs(ray.y) > 1.0E-7D) {
            double boundary = ray.y > 0.0D ? sourcePos.getY() + 1.0D : sourcePos.getY();
            double t = (boundary - start.y) / ray.y;
            if (t >= 0.0D) {
                exit = Math.min(exit, t);
            }
        }
        if (Math.abs(ray.z) > 1.0E-7D) {
            double boundary = ray.z > 0.0D ? sourcePos.getZ() + 1.0D : sourcePos.getZ();
            double t = (boundary - start.z) / ray.z;
            if (t >= 0.0D) {
                exit = Math.min(exit, t);
            }
        }
        double rayLength = ray.length();
        double epsilon = rayLength <= 1.0E-7D ? 0.0D : 0.002D / rayLength;
        double startT = Mth.clamp(exit + epsilon, 0.0D, 1.0D);
        Vec3 collisionStart = start.lerp(end, startT);
        if (collisionStart.distanceToSqr(end) <= 1.0E-6D) {
            return end;
        }
        BlockHitResult hit = level.clip(new ClipContext(
                collisionStart,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                contextEntity
        ));
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

    private static float residualLength(
            CryingObsidianCrystalStage stage,
            long seed,
            BeaconRelayState relay
    ) {
        float max = stage.residualMaxLength() * relay.reflectedLengthScale();
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
            float outerRadius,
            float rotationSpeed
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
                true,
                rotationSpeed
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
            boolean centerOnBlock,
            float rotationSpeed
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
        float signedAnimation = -animation * rotationSpeed;
        float textureOffset = Mth.frac(signedAnimation * 0.2F - (float) Mth.floor(signedAnimation * 0.1F));

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(animation * 2.25F * rotationSpeed - 45.0F));
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

    private static int boostColor(int color, float brightness) {
        float factor = Math.max(1.0F, brightness);
        int red = Math.min(255, Math.round(((color >>> 16) & 0xFF) * factor));
        int green = Math.min(255, Math.round(((color >>> 8) & 0xFF) * factor));
        int blue = Math.min(255, Math.round((color & 0xFF) * factor));
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
        renderQuad(pose, consumer, color, minY, maxY, x3, z3, x2, z2, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, color, minY, maxY, x1, z1, x3, z3, minU, maxU, minV, maxV);
        renderQuad(pose, consumer, color, minY, maxY, x2, z2, x0, z0, minU, maxU, minV, maxV);
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

    private record CrystalHit(
            BlockPos pos,
            CryingObsidianCrystalStage stage,
            float incomingFraction,
            BeaconRelayState relay
    ) {
    }

    private record ColumnEvent(
            BlockPos pos,
            CoreBoosterMaterial material,
            CryingObsidianCrystalStage stage
    ) {
        private static ColumnEvent booster(BlockPos pos, CoreBoosterMaterial material) {
            return new ColumnEvent(pos, material, null);
        }

        private static ColumnEvent crystal(BlockPos pos, CryingObsidianCrystalStage stage) {
            return new ColumnEvent(pos, CoreBoosterMaterial.EMPTY, stage);
        }
    }

    private record ColumnScan(List<ColumnEvent> events, List<CrystalHit> crystals) {
        private boolean modified() {
            return !events.isEmpty();
        }
    }
}
