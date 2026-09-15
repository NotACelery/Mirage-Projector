package celerbi.mirageprojector.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Unlock item for Mirage's dedicated Shoulder Device equipment slot. */
public final class ArmStrapItem extends Item {
    public ArmStrapItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.arm_strap.unlock"
        ).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.arm_strap.pouch"
        ).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.arm_strap.vanilla_free"
        ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
