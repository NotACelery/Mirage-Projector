package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.network.PortableProjectorStatePayload;
import celerbi.mirageprojector.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
import net.minecraft.world.phys.Vec3;

/**
 * Client-side renderer/HUD and synchronized-state cache for handheld Mirage projectors.
 *
 * <p>1.0.12 no longer assumes an active portable projector must be visible in a tracked hand.
 * The server publishes device custom-data state by stable projector UUID while vanilla entity
 * tracking continues to provide player position/orientation. This lets an explicitly enabled
 * projector keep rendering after it is moved into the owner's inventory.</p>
 */
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
            if (synced != null && !synced.isEmpty()) {
                for (SyncedState state : synced.values()) {
                    if (!MirageHandProjectorItem.emittingProjection(state.stack())) {
                        continue;
                    }
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
                continue;
            }

            // Pre-sync/local fallback keeps 1.0.11 held behavior responsive during login or packet latency.
            ItemStack main = player.getMainHandItem();
            if (main.is(ModItems.MIRAGE_HAND_PROJECTOR.get()) && MirageHandProjectorItem.emittingProjection(main)) {
                renderedAny |= renderProjector(
                        minecraft, poseStack, bufferSource, cameraPosition, partialTick, player, main, renderedForPlayer++
                );
            }
            ItemStack off = player.getOffhandItem();
            if (off.is(ModItems.MIRAGE_HAND_PROJECTOR.get()) && MirageHandProjectorItem.emittingProjection(off)) {
                renderedAny |= renderProjector(
                        minecraft, poseStack, bufferSource, cameraPosition, partialTick, player, off, renderedForPlayer
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
        Placement placement = placement(player, ordinal);
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

        int packedLight = LevelRenderer.getLightColor(minecraft.level, placement.blockPos());
        poseStack.pushPose();
        poseStack.translate(
                placement.blockPos().getX() - cameraPosition.x,
                placement.blockPos().getY() - cameraPosition.y,
                placement.blockPos().getZ() - cameraPosition.z
        );
        renderer.render(portable, partialTick, poseStack, bufferSource, packedLight, 0);
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
        BlockPos projectorPos = player.blockPosition();
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

        Vec3 playerPosition = new Vec3(
                Mth.lerp(partialTick, player.xo, player.getX()),
                Mth.lerp(partialTick, player.yo, player.getY()),
                Mth.lerp(partialTick, player.zo, player.getZ())
        );
        ProjectionSettings settings = portable.settings();
        float forwardModelScale = Math.max(1.0F / 16.0F, settings.scalePixels() / 40.0F);
        float modelScale = forwardModelScale * MirageHandProjectorItem.warBannerSizePercent(stack) / 100.0F;
        double bannerHeight = modelScale * 2.5D;
        double gap = 0.12D + MirageHandProjectorItem.warBannerHeightPixels(stack) / 16.0D;
        double stackSeparation = Math.max(0, ordinal) * 0.12D;
        Vec3 anchor = new Vec3(
                playerPosition.x,
                playerPosition.y + player.getBbHeight() + gap + bannerHeight + stackSeparation,
                playerPosition.z
        );

        float yawDegrees;
        if (MirageHandProjectorItem.warBannerFacing(stack)
                == MirageHandProjectorItem.WarBannerFacing.BILLBOARD) {
            double dx = cameraPosition.x - anchor.x;
            double dz = cameraPosition.z - anchor.z;
            yawDegrees = (float) Math.toDegrees(Math.atan2(dx, dz));
        } else {
            yawDegrees = -Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
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

    private static Placement placement(Player player, int ordinal) {
        Vec3 look = player.getLookAngle();
        if (look.lengthSqr() < 1.0E-6D) {
            look = new Vec3(0.0D, 0.0D, 1.0D);
        }
        look = look.normalize();

        Vec3 right = new Vec3(0.0D, 1.0D, 0.0D).cross(look);
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize();

        double spread = ordinal <= 0 ? 0.0D : Math.min(0.45D, ordinal * 0.18D);
        Vec3 origin = player.getEyePosition()
                .add(look.scale(1.10D))
                .add(right.scale(spread))
                .add(0.0D, -1.05D, 0.0D);
        BlockPos pos = BlockPos.containing(origin.x - 0.5D, origin.y, origin.z - 0.5D);
        return new Placement(pos, horizontalFacing(player));
    }

    private static Direction horizontalFacing(Player player) {
        Direction facing = player.getDirection();
        if (facing == Direction.UP || facing == Direction.DOWN) {
            return Direction.SOUTH;
        }
        return facing;
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

    private record Placement(BlockPos blockPos, Direction facing) {
    }

    private record SyncedState(ItemStack stack, long lastSeenTick) {
    }
}
