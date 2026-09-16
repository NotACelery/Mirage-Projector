package celerbi.mirageprojector.item;

import celerbi.mirageprojector.light.device.PortableLightMode;
import celerbi.mirageprojector.menu.PortableDeviceMenu;
import celerbi.mirageprojector.menu.PortableDeviceSource;
import celerbi.mirageprojector.block.MirageFlashlightBeaconBlock;
import celerbi.mirageprojector.blockentity.MirageLightProjectorBlockEntity;
import celerbi.mirageprojector.registry.ModBlocks;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Handheld Mirage illumination device backed by one removable rechargeable cell.
 *
 * <p>The exact cell ItemStack is serialized inside the flashlight ItemStack so partial charge and
 * future rechargeable-media components survive insertion/extraction. A tiny duplicated summary
 * (cell-present + percent) is maintained beside it so held-device rendering/HUD code can decide
 * whether the flashlight emits without decoding nested registry-aware ItemStack data every frame.</p>
 */
public final class MirageFlashlightItem extends Item implements ShoulderRechargeableDevice {
    private static final String MODE_TAG = "MirageLanternMode";
    private static final String CELL_TAG = "MirageLanternCell";
    private static final String CELL_PRESENT_TAG = "MirageLanternCellPresent";
    private static final String CELL_PERCENT_TAG = "MirageLanternCellPercent";

    public MirageFlashlightItem(Properties properties) {
        super(properties);
    }

    /** Public rename while the legacy registry ID `mirage_lantern` stays stable for world compatibility. */
    @Override
    public String getDescriptionId() {
        return "item.mirage_projector.mirage_flashlight";
    }

    public static PortableLightMode mode(ItemStack flashlight) {
        CompoundTag tag = customTag(flashlight);
        if (!tag.contains(MODE_TAG)) {
            return PortableLightMode.OFF;
        }
        String serialized = tag.getString(MODE_TAG);
        for (PortableLightMode candidate : PortableLightMode.values()) {
            if (candidate.serializedName().equals(serialized)) {
                return candidate;
            }
        }
        return PortableLightMode.OFF;
    }

    public static void setMode(ItemStack flashlight, PortableLightMode mode) {
        if (flashlight == null || flashlight.isEmpty()) {
            return;
        }
        PortableLightMode safe = mode == null ? PortableLightMode.OFF : mode;
        CustomData.update(DataComponents.CUSTOM_DATA, flashlight, tag -> {
            if (safe == PortableLightMode.OFF) {
                tag.remove(MODE_TAG);
            } else {
                tag.putString(MODE_TAG, safe.serializedName());
            }
        });
        removeEmptyCustomData(flashlight);
    }

    public static ItemStack energyCell(ItemStack flashlight, HolderLookup.Provider registries) {
        if (flashlight == null || flashlight.isEmpty() || registries == null) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = customTag(flashlight);
        if (!tag.contains(CELL_TAG)) {
            return ItemStack.EMPTY;
        }
        ItemStack stored = ItemStack.parseOptional(registries, tag.getCompound(CELL_TAG));
        return RechargeableEnergyItem.isRechargeable(stored) ? stored.copyWithCount(1) : ItemStack.EMPTY;
    }

    public static boolean hasEnergyCell(ItemStack flashlight) {
        return customTag(flashlight).getBoolean(CELL_PRESENT_TAG);
    }

    public static int energyPercent(ItemStack flashlight) {
        return Mth.clamp(customTag(flashlight).getInt(CELL_PERCENT_TAG), 0, 100);
    }

    public static boolean emitting(ItemStack flashlight) {
        return flashlight != null
                && !flashlight.isEmpty()
                && hasEnergyCell(flashlight)
                && energyPercent(flashlight) > 0
                && mode(flashlight).emitsLight();
    }

    public static boolean insertEnergyCell(
            ItemStack flashlight,
            ItemStack source,
            HolderLookup.Provider registries
    ) {
        if (flashlight == null || flashlight.isEmpty() || hasEnergyCell(flashlight)
                || !RechargeableEnergyItem.isRechargeable(source) || registries == null) {
            return false;
        }
        writeEnergyCell(flashlight, RechargeableEnergyItem.normalizeForDevice(source), registries);
        return true;
    }

    public static ItemStack extractEnergyCell(ItemStack flashlight, HolderLookup.Provider registries) {
        ItemStack stored = energyCell(flashlight, registries);
        if (stored.isEmpty()) {
            clearEnergyCell(flashlight);
            return ItemStack.EMPTY;
        }
        clearEnergyCell(flashlight);
        return stored;
    }

    /** GUI/container-only battery replacement path. */
    public static void replaceEnergyCell(ItemStack flashlight, ItemStack cell, HolderLookup.Provider registries) {
        if (cell == null || cell.isEmpty()) {
            clearEnergyCell(flashlight);
        } else if (RechargeableEnergyItem.isRechargeable(cell)) {
            writeEnergyCell(flashlight, RechargeableEnergyItem.normalizeForDevice(cell), registries);
        }
    }

    public static Component hudComponent(ItemStack flashlight) {
        Component modeName = Component.translatable(mode(flashlight).displayTranslationKey());
        if (!hasEnergyCell(flashlight) || energyPercent(flashlight) <= 0) {
            return Component.translatable(
                    "hud.mirage_projector.flashlight.status_discharged",
                    modeName
            );
        }
        return Component.translatable(
                "hud.mirage_projector.flashlight.status",
                modeName,
                energyPercent(flashlight)
        );
    }

    /**
     * Sneak + RMB on the top of a supporting block places the flashlight temporarily in-world.
     * The world form owns the same cell/mode and returns them to the handheld item when broken.
     */
    @Override
    public net.minecraft.world.InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown() || context.getClickedFace() != Direction.UP) {
            return net.minecraft.world.InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos target = context.getClickedPos().above();
        if (!level.getBlockState(target).canBeReplaced()) {
            return net.minecraft.world.InteractionResult.PASS;
        }

        BlockState placedState = ModBlocks.MIRAGE_FLASHLIGHT_BEACON.get().defaultBlockState()
                .setValue(MirageFlashlightBeaconBlock.FACING, player.getDirection());
        if (!placedState.canSurvive(level, target)) {
            return net.minecraft.world.InteractionResult.FAIL;
        }
        if (level.isClientSide) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        if (!level.setBlock(target, placedState, Block.UPDATE_ALL)) {
            return net.minecraft.world.InteractionResult.FAIL;
        }
        if (!(level.getBlockEntity(target) instanceof MirageLightProjectorBlockEntity placed)) {
            level.removeBlock(target, false);
            return net.minecraft.world.InteractionResult.FAIL;
        }

        ItemStack flashlight = context.getItemInHand();
        placed.setMode(mode(flashlight));
        ItemStack cell = energyCell(flashlight, level.registryAccess());
        if (!cell.isEmpty()) {
            placed.setEnergyCell(cell);
        }
        level.gameEvent(player, GameEvent.BLOCK_PLACE, target);
        if (!player.getAbilities().instabuild) {
            flashlight.shrink(1);
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack flashlight = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(flashlight, true);
        }

        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                PortableDeviceMenu.open(
                        serverPlayer,
                        hand == InteractionHand.MAIN_HAND ? PortableDeviceSource.MAIN_HAND : PortableDeviceSource.OFF_HAND
                );
            }
            return InteractionResultHolder.sidedSuccess(flashlight, false);
        }

        PortableLightMode next = mode(flashlight).next();
        setMode(flashlight, next);
        player.displayClientMessage(Component.translatable(next.hudTranslationKey()), true);
        return InteractionResultHolder.sidedSuccess(flashlight, false);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || !(entity instanceof Player player) || level.getGameTime() % 20L != 0L) {
            return;
        }
        if (player.getMainHandItem() != stack && player.getOffhandItem() != stack) {
            return;
        }
        drainOneSecond(stack, level, player);
    }

    @Override
    public void serverTickShoulder(ItemStack stack, net.minecraft.server.level.ServerPlayer player) {
        if (player.level().getGameTime() % 20L == 0L) {
            drainOneSecond(stack, player.level(), player);
        }
    }

    @Override
    public ItemStack shoulderEnergyCell(ItemStack device, HolderLookup.Provider registries) {
        return energyCell(device, registries);
    }

    @Override
    public boolean shoulderInsertEnergyCell(
            ItemStack device,
            ItemStack source,
            HolderLookup.Provider registries
    ) {
        return insertEnergyCell(device, source, registries);
    }

    @Override
    public ItemStack shoulderExtractEnergyCell(ItemStack device, HolderLookup.Provider registries) {
        return extractEnergyCell(device, registries);
    }

    private static void drainOneSecond(ItemStack stack, Level level, Player player) {
        PortableLightMode mode = mode(stack);
        if (!mode.emitsLight() || !hasEnergyCell(stack)) {
            return;
        }

        ItemStack cell = energyCell(stack, level.registryAccess());
        RechargeableEnergyItem energy = RechargeableEnergyItem.fromStack(cell);
        if (energy == null) {
            clearEnergyCell(stack);
            return;
        }

        int before = energy.storedCharge(cell);
        if (before <= 0) {
            writeEnergyCell(stack, cell, level.registryAccess());
            return;
        }

        energy.consumeStoredCharge(cell, mode.chargePerSecond());
        writeEnergyCell(stack, cell, level.registryAccess());
        if (before > 0 && energy.storedCharge(cell) <= 0) {
            player.displayClientMessage(hudComponent(stack), true);
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.flashlight.mode",
                Component.translatable(mode(stack).displayTranslationKey())
        ).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (!hasEnergyCell(stack) || energyPercent(stack) <= 0) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.mirage_projector.flashlight.discharged"
            ).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltipComponents.add(Component.translatable(
                    "tooltip.mirage_projector.flashlight.charge",
                    energyPercent(stack)
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.flashlight.use"
        ).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.flashlight.place"
        ).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return hasEnergyCell(stack) && energyPercent(stack) < 100;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * energyPercent(stack) / 100.0F);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float fraction = energyPercent(stack) / 100.0F;
        int red = Math.round(Mth.lerp(fraction, 86.0F, 194.0F));
        int green = Math.round(Mth.lerp(fraction, 64.0F, 124.0F));
        int blue = Math.round(Mth.lerp(fraction, 100.0F, 255.0F));
        return (red << 16) | (green << 8) | blue;
    }

    private static void writeEnergyCell(
            ItemStack flashlight,
            ItemStack cell,
            HolderLookup.Provider registries
    ) {
        if (cell == null || cell.isEmpty() || !RechargeableEnergyItem.isRechargeable(cell)) {
            clearEnergyCell(flashlight);
            return;
        }
        ItemStack stored = cell.copyWithCount(1);
        int percent = RechargeableEnergyItem.chargePercent(stored);
        CustomData.update(DataComponents.CUSTOM_DATA, flashlight, tag -> {
            tag.put(CELL_TAG, stored.save(registries));
            tag.putBoolean(CELL_PRESENT_TAG, true);
            tag.putInt(CELL_PERCENT_TAG, percent);
        });
    }

    private static void clearEnergyCell(ItemStack flashlight) {
        if (flashlight == null || flashlight.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, flashlight, tag -> {
            tag.remove(CELL_TAG);
            tag.remove(CELL_PRESENT_TAG);
            tag.remove(CELL_PERCENT_TAG);
        });
        removeEmptyCustomData(flashlight);
    }

    private static CompoundTag customTag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void removeEmptyCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.copyTag().isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
    }
}
