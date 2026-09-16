package celerbi.mirageprojector.item;

import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.network.OpenScanCodexPayload;
import celerbi.mirageprojector.menu.ScanCodexMenu;
import celerbi.mirageprojector.registry.ModItems;
import celerbi.mirageprojector.scan.ScanCodexSavedData;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/** Physical key/reference for a server-persistent Mirage entity-scan library. */
public final class ScanCodexItem extends Item {
    public static final String CODEX_ID_TAG = "MirageScanCodexId";
    public static final String SELECTED_SCAN_TAG = "MirageScanCodexSelected";
    private static final UUID ZERO_UUID = new UUID(0L, 0L);

    public ScanCodexItem(Properties properties) {
        super(properties.stacksTo(1));
    }


    public static boolean isCodex(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(ModItems.SCAN_CODEX.get());
    }

    public static UUID codexId(ItemStack codex) {
        CompoundTag tag = customTag(codex);
        return tag.hasUUID(CODEX_ID_TAG) ? tag.getUUID(CODEX_ID_TAG) : ZERO_UUID;
    }

    public static UUID ensureCodexId(ItemStack codex) {
        UUID existing = codexId(codex);
        if (!isZero(existing)) {
            return existing;
        }
        UUID created = UUID.randomUUID();
        CustomData.update(DataComponents.CUSTOM_DATA, codex, tag -> tag.putUUID(CODEX_ID_TAG, created));
        return created;
    }

    public static Optional<UUID> selectedScanId(ItemStack codex) {
        CompoundTag tag = customTag(codex);
        return tag.hasUUID(SELECTED_SCAN_TAG) ? Optional.of(tag.getUUID(SELECTED_SCAN_TAG)) : Optional.empty();
    }

    public static void setSelectedScanId(ItemStack codex, UUID scanId) {
        if (codex == null || codex.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, codex, tag -> {
            if (scanId == null || isZero(scanId)) {
                tag.remove(SELECTED_SCAN_TAG);
            } else {
                tag.putUUID(SELECTED_SCAN_TAG, scanId);
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ensureCodexId(stack);
            ScanCodexMenu.openHandheld(serverPlayer, stack);
            sendSnapshot(serverPlayer, stack, false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand
    ) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        return scanTarget(stack, player, target);
    }

    public InteractionResult scanTarget(ItemStack stack, Player player, LivingEntity target) {
        if (target.isPassenger() || target.isVehicle()) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.mirage_projector.scan.composite_rejected"),
                        true
                );
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }

        EntityScanData.Scan scan = EntityScanData.create(target);
        if (!scan.success()) {
            player.displayClientMessage(
                    Component.translatable("message.mirage_projector.scan.too_large", scan.sizeBytes() / 1024),
                    true
            );
            return InteractionResult.FAIL;
        }

        UUID codexId = ensureCodexId(stack);
        ScanCodexSavedData savedData = ScanCodexSavedData.get(serverPlayer.getServer());
        if (!savedData.canAddType(codexId, scan.entityType())) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.scan_codex.type_limit",
                    ScanCodexSavedData.MAX_SCANS_PER_ENTITY_TYPE,
                    target.getType().getDescription()
            ), true);
            return InteractionResult.FAIL;
        }
        UUID scanId = savedData.addScan(codexId, scan);
        if (isZero(scanId)) {
            player.displayClientMessage(Component.translatable("message.mirage_projector.scan_codex.failed"), true);
            return InteractionResult.FAIL;
        }
        setSelectedScanId(stack, scanId);
        player.displayClientMessage(
                Component.translatable("message.mirage_projector.scan_codex.saved", scan.displayName()),
                true
        );
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        tooltipComponents.add(Component.translatable("tooltip.mirage_projector.scan_codex.open")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.mirage_projector.scan_codex.scan")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("tooltip.mirage_projector.scan_codex.independent")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable("tooltip.mirage_projector.scan_codex.lectern")
                .withStyle(ChatFormatting.DARK_PURPLE));
        UUID id = codexId(stack);
        if (!isZero(id)) {
            tooltipComponents.add(Component.literal("Codex " + shortId(id)).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static ItemStack findOwnedCodex(ServerPlayer player, UUID wantedId) {
        if (player == null || wantedId == null || isZero(wantedId)) {
            return ItemStack.EMPTY;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.is(ModItems.SCAN_CODEX.get()) && wantedId.equals(codexId(candidate))) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void sendSnapshot(ServerPlayer player, ItemStack codex, boolean openScreen) {
        if (player == null || codex == null || !codex.is(ModItems.SCAN_CODEX.get())) {
            return;
        }
        UUID id = ensureCodexId(codex);
        ScanCodexSavedData data = ScanCodexSavedData.get(player.getServer());
        UUID selected = selectedScanId(codex).filter(scanId -> data.contains(id, scanId)).orElse(ZERO_UUID);
        if (isZero(selected)) {
            setSelectedScanId(codex, null);
        }
        CompoundTag selectedRoot = isZero(selected)
                ? new CompoundTag()
                : data.copyScanRoot(id, selected).orElseGet(CompoundTag::new);
        PacketDistributor.sendToPlayer(player, new OpenScanCodexPayload(
                id,
                selected,
                openScreen,
                data.summaries(id),
                selectedRoot
        ));
    }

    private static CompoundTag customTag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static boolean isZero(UUID id) {
        return id == null || (id.getMostSignificantBits() == 0L && id.getLeastSignificantBits() == 0L);
    }

    private static String shortId(UUID id) {
        String value = id.toString();
        return value.substring(0, Math.min(8, value.length())) + "…";
    }
}
