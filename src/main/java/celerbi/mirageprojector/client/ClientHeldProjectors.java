package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.ProjectionImageSizing;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.network.PortableProjectorStatePayload;
import celerbi.mirageprojector.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Client renderer and synchronized-state cache for Hand Projectors. */
public final class ClientHeldProjectors {
    private static final double MAX_RENDER_DISTANCE_SQUARED = 96.0D * 96.0D;
    private static final long STALE_STATE_TICKS = 60L;
    private static final Map<UUID, Map<UUID, SyncedState>> SERVER_STATES = new HashMap<>();

    private ClientHeldProjectors() {
    }

    public static void acceptServerState(PortableProjectorStatePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (payload == null) {
            return;
        }
        if (!payload.active()) {
            removeState(payload.ownerId(), payload.deviceId());
            return;
        }

        CompoundTag customData = payload.customData();
        ItemStack stack = new ItemStack(ModItems.MIRAGE_HAND_PROJECTOR.get());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.merge(customData.copy()));
        long now = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        SERVER_STATES.computeIfAbsent(payload.ownerId(), ignored -> new HashMap<>())
                .put(payload.deviceId(), new SyncedState(stack, now));
    }

    public static void renderVisiblePlayers(
            Minecraft minecraft,
            PoseStack poseStack,
            Vec3 cameraPosition,
            float partialTick
    ) {
        if (minecraft == null || minecraft.level == null) {
            return;
        }

        pruneStale(minecraft.level.getGameTime());
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        boolean renderedAny = false;
        for (Player player : minecraft.level.players()) {
            if (player == null || player.isRemoved()) {
                continue;
            }
            if (player.position().distanceToSqr(cameraPosition) > MAX_RENDER_DISTANCE_SQUARED) {
                continue;
            }

            Map<UUID, SyncedState> synced = SERVER_STATES.get(player.getUUID());
            int renderedForPlayer = 0;
            Set<UUID> renderedDeviceIds = new HashSet<>();
            if (synced != null && !synced.isEmpty()) {
                for (Map.Entry<UUID, SyncedState> entry : synced.entrySet()) {
                    ItemStack equipped = equippedProjector(player, entry.getKey());
                    if (equipped.isEmpty()) {
                        continue;
                    }
                    SyncedState state = entry.getValue();
                    if (!MirageHandProjectorItem.emittingProjection(state.stack())) {
                        continue;
                    }
                    renderedDeviceIds.add(entry.getKey());
                    renderedAny |= renderProjector(
                            minecraft,
                            poseStack,
                            bufferSource,
                            cameraPosition,
                            partialTick,
                            player,
                            state.stack(),
                            renderedForPlayer++
                    );
                }
            }

            ItemStack main = player.getMainHandItem();
            if (main.is(ModItems.MIRAGE_HAND_PROJECTOR.get()) && MirageHandProjectorItem.emittingProjection(main)
                    && !renderedDeviceIds.contains(MirageHandProjectorItem.deviceId(main))) {
                renderedAny |= renderProjector(
                        minecraft, poseStack, bufferSource, cameraPosition, partialTick, player, main, renderedForPlayer++
                );
            }
            ItemStack off = player.getOffhandItem();
            if (off.is(ModItems.MIRAGE_HAND_PROJECTOR.get()) && MirageHandProjectorItem.emittingProjection(off)
                    && !renderedDeviceIds.contains(MirageHandProjectorItem.deviceId(off))) {
                renderedAny |= renderProjector(
                        minecraft, poseStack, bufferSource, cameraPosition, partialTick, player, off, renderedForPlayer
                );
            }
            ItemStack shoulder = ClientShoulderEquipment.device(player.getUUID());
            if (shoulder.is(ModItems.MIRAGE_HAND_PROJECTOR.get()) && MirageHandProjectorItem.emittingProjection(shoulder)
                    && !renderedDeviceIds.contains(MirageHandProjectorItem.deviceId(shoulder))) {
                renderedAny |= renderProjector(
                        minecraft, poseStack, bufferSource, cameraPosition, partialTick, player, shoulder, renderedForPlayer
                );
            }
        }
        if (renderedAny) {
            bufferSource.endBatch();
        }
    }

    public static void resetSession() {
        SERVER_STATES.clear();
    }

    private static boolean renderProjector(
            Minecraft minecraft,
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            Vec3 cameraPosition,
            float partialTick,
            Player player,
            ItemStack stack,
            int ordinal
    ) {
        if (MirageHandProjectorItem.warBannerActive(stack)) {
            return renderWarBanner(
                    minecraft, poseStack, bufferSource, cameraPosition, partialTick, player, stack, ordinal
            );
        }
        if (!MirageHandProjectorItem.portablePowerAvailable(stack, minecraft.level.registryAccess())) {
            return false;
        }

        Placement placement = placement(minecraft, player, partialTick, ordinal,
                MirageHandProjectorItem.projectionDistancePixels(stack));
        MirageProjectorBlockEntity portable = MirageHandProjectorItem.createPortableProjector(
                stack,
                minecraft.level,
                placement.blockPos(),
                placement.facing()
        );
        if (portable == null) {
            return false;
        }

        BlockEntityRenderer<?> rawRenderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(portable);
        if (!(rawRenderer instanceof MirageProjectorRenderer renderer)) {
            return false;
        }

        if (portable.settings().sourceMode() == ProjectionSettings.SourceMode.IMAGE
                && MirageHandProjectorItem.imagePresentation(stack)
                == MirageHandProjectorItem.ImagePresentation.SURFACE) {
            BlockHitResult target = surfaceTarget(minecraft, player, partialTick);
            if (target == null || !surfaceFullySupportsImage(minecraft, target, portable.settings(), cameraPosition)) return false;
            renderer.renderPortableSurfaceImage(portable, target.getLocation(), target.getDirection(), cameraPosition,
                    poseStack, bufferSource, LevelRenderer.getLightColor(minecraft.level, target.getBlockPos()));
            return true;
        }

        int packedLight = LevelRenderer.getLightColor(minecraft.level, placement.blockPos());
        poseStack.pushPose();
        // The temporary block entity supplies source state only. Its block position must never
        // become the visual anchor: that quantizes a held projection to whole blocks.
        poseStack.translate(
                placement.anchor().x - cameraPosition.x,
                placement.anchor().y - cameraPosition.y,
                placement.anchor().z - cameraPosition.z
        );
        float renderYaw = placement.yawDegrees() - 180.0F;
        if (portable.settings().sourceMode() == ProjectionSettings.SourceMode.IMAGE
                && MirageHandProjectorItem.imagePresentation(stack)
                == MirageHandProjectorItem.ImagePresentation.BILLBOARD) {
            renderYaw = (float) Math.toDegrees(Math.atan2(
                    cameraPosition.x - placement.anchor().x,
                    cameraPosition.z - placement.anchor().z
            ));
        }
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(renderYaw));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        renderer.renderPortableProjection(portable, partialTick, poseStack, bufferSource, packedLight, 0,
                cameraPosition, placement.anchor(), renderYaw);
        poseStack.popPose();
        return true;
    }

    private static boolean renderWarBanner(
            Minecraft minecraft,
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            Vec3 cameraPosition,
            float partialTick,
            Player player,
            ItemStack stack,
            int ordinal
    ) {
        if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            return false;
        }
        Placement placement = placement(minecraft, player, partialTick, ordinal,
                MirageHandProjectorItem.projectionDistancePixels(stack));
        BlockPos projectorPos = placement.blockPos();
        MirageProjectorBlockEntity portable = MirageHandProjectorItem.createPortableProjector(
                stack,
                minecraft.level,
                projectorPos,
                horizontalFacing(player)
        );
        if (portable == null || portable.bannerSnapshot(0).isEmpty()) {
            return false;
        }

        BlockEntityRenderer<?> rawRenderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(portable);
        if (!(rawRenderer instanceof MirageProjectorRenderer renderer)) {
            return false;
        }

        ProjectionSettings settings = portable.settings();
        // The banner model is 2.5 blocks tall at unit scale.  Half scale therefore matches
        // Minecraft's placed banner height; the percentage control is relative to that size.
        float modelScale = MirageHandProjectorItem.warBannerSizePercent(stack) / 200.0F;
        double bannerHeight = modelScale * 2.5D;
        double heightOffset = MirageHandProjectorItem.warBannerHeightPixels(stack) / 16.0D;
        Vec3 bodyForward = horizontalForward(player, partialTick);
        Vec3 bodyRight = new Vec3(-bodyForward.z, 0.0D, bodyForward.x);
        double stackSeparation = Math.max(0, ordinal) * 0.18D;
        boolean billboard = MirageHandProjectorItem.warBannerFacing(stack)
                == MirageHandProjectorItem.WarBannerFacing.BILLBOARD;
        Vec3 anchor;
        float yawDegrees;
        anchor = placement.playerPosition()
                .add(bodyForward.scale(-0.48D))
                .add(bodyRight.scale(stackSeparation))
                .add(0.0D, player.getBbHeight() * 0.5D + bannerHeight + heightOffset, 0.0D);
        if (billboard) {
            double dx = cameraPosition.x - anchor.x;
            double dz = cameraPosition.z - anchor.z;
            yawDegrees = (float) Math.toDegrees(Math.atan2(dx, dz));
        } else {
            yawDegrees = (float) Math.toDegrees(Math.atan2(bodyForward.x, bodyForward.z));
        }

        int packedLight = settings.fullbright()
                ? LightTexture.FULL_BRIGHT
                : LevelRenderer.getLightColor(minecraft.level, BlockPos.containing(anchor.x, anchor.y, anchor.z));
        double gameTime = minecraft.level.getGameTime() + partialTick;

        poseStack.pushPose();
        poseStack.translate(
                anchor.x - cameraPosition.x,
                anchor.y - cameraPosition.y,
                anchor.z - cameraPosition.z
        );
        renderer.renderPortableWarBanner(
                portable.bannerSnapshot(0),
                settings,
                poseStack,
                bufferSource,
                packedLight,
                yawDegrees,
                modelScale,
                gameTime
        );
        poseStack.popPose();
        return true;
    }

    private static Placement placement(Minecraft minecraft, Player player, float partialTick, int ordinal, int distancePixels) {
        Vec3 forward = player.getViewVector(partialTick);
        if (forward.lengthSqr() < 1.0E-6D) forward = new Vec3(0.0D, 0.0D, 1.0D);
        forward = forward.normalize();
        Vec3 right = new Vec3(0.0D, 1.0D, 0.0D).cross(forward);
        if (right.lengthSqr() < 1.0E-6D) right = new Vec3(1.0D, 0.0D, 0.0D);
        right = right.normalize();

        double spread = ordinal <= 0 ? 0.0D : Math.min(0.45D, ordinal * 0.18D);
        Vec3 playerPosition = new Vec3(
                Mth.lerp(partialTick, player.xo, player.getX()),
                Mth.lerp(partialTick, player.yo, player.getY()),
                Mth.lerp(partialTick, player.zo, player.getZ())
        );
        // All regular handheld sources travel along the actual camera ray.  This deliberately
        // follows yaw and pitch instead of the player's body, so aiming changes immediately.
        Vec3 origin = playerPosition.add(0.0D, player.getEyeHeight(), 0.0D)
                .add(forward.scale(distancePixels / 16.0D))
                .add(right.scale(spread))
                // Fixed chassis renderers use a physical top and a source baseline.  Center the
                // portable visual on the crosshair instead of placing that baseline at eye level.
                .add(0.0D, -0.75D, 0.0D);
        BlockPos pos = BlockPos.containing(origin);
        float yaw = (float) Math.toDegrees(Math.atan2(forward.x, forward.z));
        return new Placement(origin, playerPosition, pos, Direction.SOUTH, yaw);
    }

    private static Vec3 horizontalForward(Player player, float partialTick) {
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180.0F);
        return new Vec3(-Mth.sin(bodyYaw), 0.0D, Mth.cos(bodyYaw));
    }

    private static Direction horizontalFacing(Player player) {
        Direction facing = player.getDirection();
        if (facing == Direction.UP || facing == Direction.DOWN) {
            return Direction.SOUTH;
        }
        return facing;
    }

    private static BlockHitResult surfaceTarget(Minecraft minecraft, Player player, float partialTick) {
        Vec3 eye;
        Vec3 look;
        // For the local owner the game camera is authoritative. This covers first person and
        // both third-person cameras, whose angle can differ from the body/head interpolation.
        if (player == minecraft.player) {
            var camera = minecraft.gameRenderer.getMainCamera();
            eye = camera.getPosition();
            var cameraLook = camera.getLookVector();
            look = new Vec3(cameraLook.x, cameraLook.y, cameraLook.z);
        } else {
            eye = new Vec3(
                    Mth.lerp(partialTick, player.xo, player.getX()),
                    Mth.lerp(partialTick, player.yo, player.getY()) + player.getEyeHeight(),
                    Mth.lerp(partialTick, player.zo, player.getZ())
            );
            look = player.getViewVector(partialTick);
        }
        HitResult hit = minecraft.level.clip(new ClipContext(
                eye, eye.add(look.scale(10.0D)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
        ));
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return minecraft.level.getBlockState(blockHit.getBlockPos())
                .isFaceSturdy(minecraft.level, blockHit.getBlockPos(), blockHit.getDirection()) ? blockHit : null;
    }

    /** Validates the complete projected image footprint against one continuous block face. */
    private static boolean surfaceFullySupportsImage(
            Minecraft minecraft,
            BlockHitResult target,
            ProjectionSettings settings,
            Vec3 cameraPosition
    ) {
        if (minecraft.level == null || settings == null || !settings.hasImage()) return false;
        ProjectionImageSizing.Size size = ProjectionImageSizing.size(
                settings.imageWidth(), settings.imageHeight(), settings.scalePixels());
        Vec3 normal = Vec3.atLowerCornerOf(target.getDirection().getNormal());
        Vec3 up = imageSurfaceUp(target.getDirection(), normal, cameraPosition, target.getLocation());
        Vec3 right = up.cross(normal);
        if (right.lengthSqr() < 1.0E-6D) return false;
        right = right.normalize();

        double halfWidth = size.widthPixels() / 32.0D;
        double halfHeight = size.heightPixels() / 32.0D;
        for (int column = 0; column < size.widthPixels(); column++) {
            double u = (column + 0.5D) / 16.0D - halfWidth;
            for (int row = 0; row < size.heightPixels(); row++) {
                double v = (row + 0.5D) / 16.0D - halfHeight;
                Vec3 point = target.getLocation().add(right.scale(u)).add(up.scale(v));
                BlockPos backing = BlockPos.containing(point.subtract(normal.scale(0.002D)));
                var state = minecraft.level.getBlockState(backing);
                if (!state.isCollisionShapeFullBlock(minecraft.level, backing)
                        || !state.isFaceSturdy(minecraft.level, backing, target.getDirection())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Vec3 imageSurfaceUp(
            Direction face,
            Vec3 normal,
            Vec3 cameraPosition,
            Vec3 surfacePoint
    ) {
        if (face.getAxis() != Direction.Axis.Y) {
            return new Vec3(0.0D, 1.0D, 0.0D);
        }
        Vec3 towardCamera = cameraPosition.subtract(surfacePoint);
        towardCamera = towardCamera.subtract(normal.scale(towardCamera.dot(normal)));
        if (towardCamera.lengthSqr() < 1.0E-6D) {
            return new Vec3(0.0D, 0.0D, -1.0D);
        }
        Vec3 up = towardCamera.normalize();
        return face == Direction.UP ? up.scale(-1.0D) : up;
    }

    private static ItemStack equippedProjector(Player player, UUID deviceId) {
        ItemStack main = player.getMainHandItem();
        if (matchesDevice(main, deviceId)) return main;
        ItemStack off = player.getOffhandItem();
        if (matchesDevice(off, deviceId)) return off;
        ItemStack shoulder = ClientShoulderEquipment.device(player.getUUID());
        return matchesDevice(shoulder, deviceId) ? shoulder : ItemStack.EMPTY;
    }

    private static boolean matchesDevice(ItemStack stack, UUID deviceId) {
        return stack.is(ModItems.MIRAGE_HAND_PROJECTOR.get())
                && deviceId != null
                && deviceId.equals(MirageHandProjectorItem.deviceId(stack));
    }

    private static void removeState(UUID ownerId, UUID deviceId) {
        Map<UUID, SyncedState> ownerStates = SERVER_STATES.get(ownerId);
        if (ownerStates == null) {
            return;
        }
        ownerStates.remove(deviceId);
        if (ownerStates.isEmpty()) {
            SERVER_STATES.remove(ownerId);
        }
    }

    private static void pruneStale(long now) {
        Iterator<Map.Entry<UUID, Map<UUID, SyncedState>>> ownerIterator = SERVER_STATES.entrySet().iterator();
        while (ownerIterator.hasNext()) {
            Map<UUID, SyncedState> states = ownerIterator.next().getValue();
            states.entrySet().removeIf(entry -> now - entry.getValue().lastSeenTick() > STALE_STATE_TICKS);
            if (states.isEmpty()) {
                ownerIterator.remove();
            }
        }
    }

    private record Placement(Vec3 anchor, Vec3 playerPosition, BlockPos blockPos, Direction facing, float yawDegrees) {
    }

    private record SyncedState(ItemStack stack, long lastSeenTick) {
    }
}
