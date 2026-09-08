package celerbi.mirageprojector.item;

import celerbi.mirageprojector.entity.EntityScanData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Non-stackable paper scan medium.
 *
 * <p>Its empty state is presented as an Empty Scan Template. Sneak-use on one
 * LivingEntity overwrites the template with a frozen visual scan; the source
 * entity and all of its equipment remain untouched.</p>
 */
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
                .orElseGet(() -> Component.translatable("item.mirage_projector.empty_scan_template"));
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand
    ) {
        // Normal right-click remains vanilla. The early NeoForge interaction
        // event handles sneak-use before an entity such as a horse can consume
        // the action; this override remains as a compatibility fallback.
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        return scanTarget(stack, player, target);
    }

    /**
     * Performs one frozen visual scan. Callers are responsible for deciding
     * whether the current interaction gesture should invoke Mirage scanning.
     */
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

        EntityScanData.Scan scan = EntityScanData.create(target);
        if (!scan.success()) {
            player.displayClientMessage(
                    Component.translatable("message.mirage_projector.scan.too_large", scan.sizeBytes() / 1024),
                    true
            );
            return InteractionResult.FAIL;
        }

        boolean replaced = EntityScanData.hasScan(stack);
        EntityScanData.writeToCard(stack, scan);
        player.displayClientMessage(
                Component.translatable(
                        replaced
                                ? "message.mirage_projector.scan.replaced"
                                : "message.mirage_projector.scan.success",
                        scan.displayName()
                ),
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
        EntityScanData.read(stack).ifPresentOrElse(scan -> {
            tooltipComponents.add(Component.literal(scan.displayName()).withStyle(ChatFormatting.AQUA));
            tooltipComponents.add(Component.literal(scan.entityType().toString()).withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.literal("Mode: " + switch (scan.kind()) {
                case HUMANOID -> "Humanoid Entity";
                case HORSE -> "Horse Entity";
                case GENERIC -> "Entity";
            }).withStyle(ChatFormatting.DARK_GRAY));
            if (scan.hasProjectionNameplate()) {
                tooltipComponents.add(Component.translatable(
                        "tooltip.mirage_projector.scan_card.nameplate",
                        scan.nameplateText()
                ).withStyle(ChatFormatting.GRAY));
            }
            String id = scan.scanId().toString();
            tooltipComponents.add(Component.literal("Snapshot " + id.substring(0, 8) + "…").withStyle(ChatFormatting.DARK_GRAY));
            tooltipComponents.add(Component.translatable("tooltip.mirage_projector.scan_card.frozen").withStyle(ChatFormatting.GREEN));
            tooltipComponents.add(Component.translatable("tooltip.mirage_projector.scan_card.overwrite").withStyle(ChatFormatting.GRAY));
        }, () -> {
            tooltipComponents.add(Component.translatable("tooltip.mirage_projector.scan_card.empty").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("tooltip.mirage_projector.scan_card.use").withStyle(ChatFormatting.DARK_GRAY));
        });
    }
}
