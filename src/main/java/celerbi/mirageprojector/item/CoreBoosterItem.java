package celerbi.mirageprojector.item;

import celerbi.mirageprojector.CoreBoosterMaterial;
import celerbi.mirageprojector.blockentity.CoreBoosterBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public final class CoreBoosterItem extends BlockItem {
    public CoreBoosterItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        CoreBoosterMaterial material = CoreBoosterBlockEntity.materialFromStack(stack);
        if (material.present()) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.mirage_projector.core_booster.loaded",
                    material.displayComponent()
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipComponents.add(Component.translatable(
                    "tooltip.mirage_projector.core_booster.amplification",
                    "1.50"
            ).withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable(
                    "tooltip.mirage_projector.core_booster.empty"
            ).withStyle(ChatFormatting.GRAY));
        }
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.core_booster.insert"
        ).withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.core_booster.extract"
        ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
