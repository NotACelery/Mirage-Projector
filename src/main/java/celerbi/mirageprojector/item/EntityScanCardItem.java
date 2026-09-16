package celerbi.mirageprojector.item;

import celerbi.mirageprojector.entity.EntityScanData;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Passive container for one frozen entity snapshot copied from a Scan Codex. */
public final class EntityScanCardItem extends Item {
    public EntityScanCardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        return EntityScanData.read(stack)
                .<Component>map(scan -> Component.translatable(
                        "item.mirage_projector.entity_scan_card.scanned",
                        scan.displayName()
                ))
                .orElseGet(() -> Component.translatable("item.mirage_projector.entity_scan_card.empty"));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        EntityScanData.read(stack).ifPresentOrElse(scan -> {
            tooltipComponents.add(Component.literal(scan.displayName()).withStyle(ChatFormatting.AQUA));
            tooltipComponents.add(Component.literal(scan.entityType().toString()).withStyle(ChatFormatting.GRAY));
            if (scan.hasProjectionNameplate()) {
                tooltipComponents.add(Component.translatable(
                        "tooltip.mirage_projector.scan_card.nameplate",
                        scan.projectionNameplateText()
                ).withStyle(ChatFormatting.GRAY));
            }
            String id = scan.scanId().toString();
            tooltipComponents.add(Component.literal("Snapshot " + id.substring(0, 8) + "…")
                    .withStyle(ChatFormatting.DARK_GRAY));
            tooltipComponents.add(Component.translatable("tooltip.mirage_projector.scan_card.frozen")
                    .withStyle(ChatFormatting.GREEN));
            tooltipComponents.add(Component.translatable("tooltip.mirage_projector.scan_card.clear_recipe")
                    .withStyle(ChatFormatting.GRAY));
        }, () -> {
            tooltipComponents.add(Component.translatable("tooltip.mirage_projector.scan_card.empty")
                    .withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("tooltip.mirage_projector.scan_card.duplicate_only")
                    .withStyle(ChatFormatting.DARK_GRAY));
        });
    }
}
