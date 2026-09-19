package celerbi.mirageprojector.item;

import celerbi.mirageprojector.menu.EntityScannerMenu;
import celerbi.mirageprojector.network.EntityScannerProgressPayload;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.registry.ModItems;
import celerbi.mirageprojector.scan.ScanCodexSavedData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class EntityScannerItem extends Item {
    private static final String CODEX_TAG = "MirageScannerCodex";
    private static final int SCAN_TICKS = 30;
    private static final int COMPLETION_COOLDOWN_TICKS = 20;
    private static final double SCAN_REACH = 4.0D;
    private static final ResourceLocation MOVEMENT_SLOWDOWN_ID = ResourceLocation.fromNamespaceAndPath(
            "mirage_projector", "entity_scanner_slowdown"
    );
    private static final Map<UUID, ScanSession> SESSIONS = new HashMap<>();
    private static final Map<UUID, Long> COMPLETION_COOLDOWNS = new HashMap<>();

    public EntityScannerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack scanner = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(scanner);
        }
        if (player.isShiftKeyDown() && !level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            EntityScannerMenu.open(serverPlayer, hand, scanner);
            return InteractionResultHolder.success(scanner);
        }
        return beginScanning(player, null).consumesAction()
                ? InteractionResultHolder.consume(scanner)
                : InteractionResultHolder.fail(scanner);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack scanner, Player player, LivingEntity target, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        return beginScanning(player, target);
    }

    public static InteractionResult beginScanning(Player player) {
        return beginScanning(player, null);
    }

    public static InteractionResult beginScanning(Player player, LivingEntity target) {
        ItemStack scanner = player.getMainHandItem();
        if (!scanner.is(ModItems.ENTITY_SCANNER.get())) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer && isCoolingDown(serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer
                && containedCodex(scanner, serverPlayer).isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mirage_projector.entity_scanner.no_codex"), true);
            return InteractionResult.FAIL;
        }
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer && target != null) {
            if (isDifferentTarget(serverPlayer, target)) {
                clear(serverPlayer);
            }
            if (alreadyRegistered(serverPlayer, scanner, target)) {
                player.displayClientMessage(Component.translatable(
                        "message.mirage_projector.entity_scanner.already_registered"
                ), true);
                return InteractionResult.FAIL;
            }
        }
        player.startUsingItem(InteractionHand.MAIN_HAND);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack scanner, int remainingUseDuration) {
        if (level.isClientSide || !(user instanceof ServerPlayer player)) {
            return;
        }
        ItemStack codex = containedCodex(scanner, player);
        if (codex.isEmpty()) {
            pause(player);
            player.stopUsingItem();
            return;
        }

        LivingEntity target = aimedLivingEntity(player);
        ScanSession previous = SESSIONS.get(player.getUUID());
        if (target == null) {
            pause(player);
            return;
        }
        if (previous != null && !previous.targetId().equals(target.getUUID())) {
            clear(player);
            previous = null;
        }
        if (alreadyRegistered(player, scanner, target)) {
            clear(player);
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.entity_scanner.already_registered"
            ), true);
            player.stopUsingItem();
            return;
        }
        ScanSession current = previous == null
                ? new ScanSession(target.getUUID(), 0, false)
                : previous;
        int progress = Math.min(SCAN_TICKS, current.progress() + 1);
        SESSIONS.put(player.getUUID(), new ScanSession(target.getUUID(), progress, true));
        applySlowdown(player);
        sendProgress(player, true, progress);
        if (progress < SCAN_TICKS) {
            return;
        }
        ScanCodexItem.captureTarget(codex, player, target);
        clear(player);
        COMPLETION_COOLDOWNS.put(player.getUUID(), player.serverLevel().getGameTime() + COMPLETION_COOLDOWN_TICKS);
        player.stopUsingItem();
    }

    @Override
    public void releaseUsing(ItemStack scanner, Level level, LivingEntity user, int timeLeft) {
        if (!level.isClientSide && user instanceof ServerPlayer player) {
            pause(player);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72_000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    public static void tick(ServerPlayer player) {
        if (COMPLETION_COOLDOWNS.getOrDefault(player.getUUID(), Long.MIN_VALUE) <= player.serverLevel().getGameTime()) {
            COMPLETION_COOLDOWNS.remove(player.getUUID());
        }
        ScanSession session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return;
        }
        if (!(player.serverLevel().getEntity(session.targetId()) instanceof LivingEntity target) || !target.isAlive()) {
            clear(player);
        }
    }

    public static void clearSession(ServerPlayer player) {
        clear(player);
        COMPLETION_COOLDOWNS.remove(player.getUUID());
    }

    private static LivingEntity aimedLivingEntity(ServerPlayer player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(SCAN_REACH));
        HitResult blockHit = player.pick(SCAN_REACH, 1.0F, false);
        double maximumDistance = blockHit.getType() == HitResult.Type.MISS
                ? SCAN_REACH * SCAN_REACH
                : start.distanceToSqr(blockHit.getLocation());
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D),
                entity -> entity instanceof LivingEntity && entity.isPickable(),
                maximumDistance
        );
        if (entityHit == null || !(entityHit.getEntity() instanceof LivingEntity target)) {
            return null;
        }
        return target.isAlive() && player.distanceToSqr(target) <= SCAN_REACH * SCAN_REACH ? target : null;
    }

    private static void pause(ServerPlayer player) {
        ScanSession session = SESSIONS.get(player.getUUID());
        removeSlowdown(player);
        if (session != null && session.active()) {
            SESSIONS.put(player.getUUID(), new ScanSession(session.targetId(), session.progress(), false));
            sendProgress(player, false, session.progress());
        }
    }

    private static void clear(ServerPlayer player) {
        SESSIONS.remove(player.getUUID());
        removeSlowdown(player);
        sendProgress(player, false, 0);
    }

    private static boolean isDifferentTarget(ServerPlayer player, LivingEntity target) {
        ScanSession session = SESSIONS.get(player.getUUID());
        return session != null && !session.targetId().equals(target.getUUID());
    }

    private static boolean isCoolingDown(ServerPlayer player) {
        return COMPLETION_COOLDOWNS.getOrDefault(player.getUUID(), Long.MIN_VALUE) > player.serverLevel().getGameTime();
    }

    private static boolean alreadyRegistered(ServerPlayer player, ItemStack scanner, LivingEntity target) {
        if (target instanceof Player) {
            return false;
        }
        ItemStack codex = containedCodex(scanner, player);
        if (codex.isEmpty()) {
            return false;
        }
        EntityScanData.Scan candidate = EntityScanData.create(target);
        if (!candidate.success() || candidate.root() == null) {
            return false;
        }
        return ScanCodexSavedData.get(player.getServer()).containsNonPlayerSourceWithEquipment(
                ScanCodexItem.ensureCodexId(codex),
                target.getUUID(),
                candidate.root().getCompound("Equipment")
        );
    }

    private static void applySlowdown(ServerPlayer player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null || attribute.getModifier(MOVEMENT_SLOWDOWN_ID) != null) {
            return;
        }
        attribute.addTransientModifier(new AttributeModifier(
                MOVEMENT_SLOWDOWN_ID,
                -0.5D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        ));
    }

    private static void removeSlowdown(ServerPlayer player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(MOVEMENT_SLOWDOWN_ID);
        }
    }

    private static void sendProgress(ServerPlayer player, boolean active, int progress) {
        PacketDistributor.sendToPlayer(player, new EntityScannerProgressPayload(active, progress, SCAN_TICKS));
    }

    private record ScanSession(UUID targetId, int progress, boolean active) {
    }

    public static ItemStack containedCodex(ItemStack scanner, ServerPlayer player) {
        CompoundTag tag = scanner.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(CODEX_TAG) ? ItemStack.parseOptional(player.registryAccess(), tag.getCompound(CODEX_TAG)) : ItemStack.EMPTY;
    }

    public static void setContainedCodex(ItemStack scanner, ItemStack codex, ServerPlayer player) {
        CustomData.update(DataComponents.CUSTOM_DATA, scanner, tag -> {
            if (codex == null || codex.isEmpty()) tag.remove(CODEX_TAG);
            else tag.put(CODEX_TAG, codex.copyWithCount(1).save(player.registryAccess()));
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.mirage_projector.entity_scanner.scan"));
        lines.add(Component.translatable("tooltip.mirage_projector.entity_scanner.configure"));
    }
}
