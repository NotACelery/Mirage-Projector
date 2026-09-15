package celerbi.mirageprojector.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Simple one-per-family Shoulder Strap patch. */
public final class ShoulderUpgradePatchItem extends Item implements ShoulderUpgrade {
    private final ResourceLocation family;
    private final String tooltipKey;

    public ShoulderUpgradePatchItem(Properties properties, ResourceLocation family, String tooltipKey) {
        super(properties);
        this.family = family;
        this.tooltipKey = tooltipKey;
    }

    @Override
    public ResourceLocation shoulderUpgradeFamily(ItemStack stack) {
        return family;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        if (tooltipKey != null && !tooltipKey.isBlank()) {
            tooltipComponents.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
        }
    }
}
