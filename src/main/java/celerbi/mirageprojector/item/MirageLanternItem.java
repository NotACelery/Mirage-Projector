package celerbi.mirageprojector.item;

import celerbi.mirageprojector.light.device.PortableLightMode;
import celerbi.mirageprojector.menu.PortableDeviceMenu;
import celerbi.mirageprojector.menu.PortableDeviceSource;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * Handheld Mirage illumination device backed by one removable rechargeable cell.
 *
 * <p>The exact cell ItemStack is serialized inside the lantern ItemStack so partial charge and
 * future rechargeable-media components survive insertion/extraction. A tiny duplicated summary
 * (cell-present + percent) is maintained beside it so held-device rendering/HUD code can decide
 * whether the lantern emits without decoding nested registry-aware ItemStack data every frame.</p>
 */
public final class MirageLanternItem extends Item implements ShoulderRechargeableDevice {
    private static final String MODE_TAG = "MirageLanternMode";
    private static final String CELL_TAG = "MirageLanternCell";
    private static final String CELL_PRESENT_TAG = "MirageLanternCellPresent";
    private static final String CELL_PERCENT_TAG = "MirageLanternCellPercent";

    public MirageLanternItem(Properties properties) {
        super(properties);
    }

    public static PortableLightMode mode(ItemStack lantern) {
        CompoundTag tag = customTag(lantern);
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

    public static void setMode(ItemStack lantern, PortableLightMode mode) {
        if (lantern == null || lantern.isEmpty()) {
            return;
        }
        PortableLightMode safe = mode == null ? PortableLightMode.OFF : mode;
        CustomData.update(DataComponents.CUSTOM_DATA, lantern, tag -> {
            if (safe == PortableLightMode.OFF) {
                tag.remove(MODE_TAG);
            } else {
                tag.putString(MODE_TAG, safe.serializedName());
            }
        });
        removeEmptyCustomData(lantern);
    }

    public static ItemStack energyCell(ItemStack lantern, HolderLookup.Provider registries) {
        if (lantern == null || lantern.isEmpty() || registries == null) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = customTag(lantern);
        if (!tag.contains(CELL_TAG)) {
            return ItemStack.EMPTY;
        }
        ItemStack stored = ItemStack.parseOptional(registries, tag.getCompound(CELL_TAG));
        return RechargeableEnergyItem.isRechargeable(stored) ? stored.copyWithCount(1) : ItemStack.EMPTY;
    }

    public static boolean hasEnergyCell(ItemStack lantern) {
        return customTag(lantern).getBoolean(CELL_PRESENT_TAG);
    }

    public static int energyPercent(ItemStack lantern) {
        return Mth.clamp(customTag(lantern).getInt(CELL_PERCENT_TAG), 0, 100);
    }

    public static boolean emitting(ItemStack lantern) {
        return lantern != null
                && !lantern.isEmpty()
                && hasEnergyCell(lantern)
                && energyPercent(lantern) > 0
                && mode(lantern).emitsLight();
    }

    public static boolean insertEnergyCell(
            ItemStack lantern,
            ItemStack source,
            HolderLookup.Provider registries
    ) {
        if (lantern == null || lantern.isEmpty() || hasEnergyCell(lantern)
                || !RechargeableEnergyItem.isRechargeable(source) || registries == null) {
            return false;
        }
        writeEnergyCell(lantern, RechargeableEnergyItem.normalizeForDevice(source), registries);
        return true;
    }

    public static ItemStack extractEnergyCell(ItemStack lantern, HolderLookup.Provider registries) {
        ItemStack stored = energyCell(lantern, registries);
        if (stored.isEmpty()) {
            clearEnergyCell(lantern);
            return ItemStack.EMPTY;
        }
        clearEnergyCell(lantern);
        return stored;
    }

    /** GUI/container-only battery replacement path. */
    public static void replaceEnergyCell(ItemStack lantern, ItemStack cell, HolderLookup.Provider registries) {
        if (cell == null || cell.isEmpty()) {
            clearEnergyCell(lantern);
        } else if (RechargeableEnergyItem.isRechargeable(cell)) {
            writeEnergyCell(lantern, RechargeableEnergyItem.normalizeForDevice(cell), registries);
        }
    }

    public static Component hudComponent(ItemStack lantern) {
        Component modeName = Component.translatable(mode(lantern).displayTranslationKey());
        if (!hasEnergyCell(lantern) || energyPercent(lantern) <= 0) {
            return Component.translatable(
                    "hud.mirage_projector.lantern.status_discharged",
                    modeName
            );
        }
        return Component.translatable(
                "hud.mirage_projector.lantern.status",
                modeName,
                energyPercent(lantern)
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack lantern = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(lantern, true);
        }

        if (player.isShiftKeyDown()) {
            PortableLightMode next = mode(lantern).next();
            setMode(lantern, next);
            player.displayClientMessage(Component.translatable(next.hudTranslationKey()), true);
            return InteractionResultHolder.sidedSuccess(lantern, false);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PortableDeviceMenu.open(
                    serverPlayer,
                    hand == InteractionHand.MAIN_HAND ? PortableDeviceSource.MAIN_HAND : PortableDeviceSource.OFF_HAND
            );
        }
        return InteractionResultHolder.sidedSuccess(lantern, false);
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
                "tooltip.mirage_projector.lantern.mode",
                Component.translatable(mode(stack).displayTranslationKey())
        ).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (!hasEnergyCell(stack) || energyPercent(stack) <= 0) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.mirage_projector.lantern.discharged"
            ).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltipComponents.add(Component.translatable(
                    "tooltip.mirage_projector.lantern.charge",
                    energyPercent(stack)
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.lantern.use"
        ).withStyle(ChatFormatting.GRAY));
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
            ItemStack lantern,
            ItemStack cell,
            HolderLookup.Provider registries
    ) {
        if (cell == null || cell.isEmpty() || !RechargeableEnergyItem.isRechargeable(cell)) {
            clearEnergyCell(lantern);
            return;
        }
        ItemStack stored = cell.copyWithCount(1);
        int percent = RechargeableEnergyItem.chargePercent(stored);
        CustomData.update(DataComponents.CUSTOM_DATA, lantern, tag -> {
            tag.put(CELL_TAG, stored.save(registries));
            tag.putBoolean(CELL_PRESENT_TAG, true);
            tag.putInt(CELL_PERCENT_TAG, percent);
        });
    }

    private static void clearEnergyCell(ItemStack lantern) {
        if (lantern == null || lantern.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, lantern, tag -> {
            tag.remove(CELL_TAG);
            tag.remove(CELL_PRESENT_TAG);
            tag.remove(CELL_PERCENT_TAG);
        });
        removeEmptyCustomData(lantern);
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
