package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.equipment.ShoulderEquipment;
import celerbi.mirageprojector.equipment.ShoulderEquipmentRuntime;
import celerbi.mirageprojector.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = MirageProjector.MOD_ID)
public final class ShoulderEquipmentEvents {
    private ShoulderEquipmentEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ShoulderEquipmentRuntime.tick(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ShoulderEquipment equipment = ShoulderEquipmentRuntime.get(player);
            ShoulderEquipmentRuntime.broadcast(player, equipment);
            ShoulderEquipmentRuntime.syncOwnerInventory(player, equipment);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }
        Player original = event.getOriginal();
        Player replacement = event.getEntity();
        if (!replacement.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                || !original.hasData(ModAttachments.SHOULDER_EQUIPMENT)) {
            return;
        }

        ShoulderEquipment oldEquipment = original.getData(ModAttachments.SHOULDER_EQUIPMENT);
        ShoulderEquipment newEquipment = replacement.getData(ModAttachments.SHOULDER_EQUIPMENT);
        newEquipment.setStackInSlot(ShoulderEquipment.STRAP_SLOT, oldEquipment.strap().copy());
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                || !player.hasData(ModAttachments.SHOULDER_EQUIPMENT)) {
            return;
        }

        ShoulderEquipment equipment = player.getData(ModAttachments.SHOULDER_EQUIPMENT);
        // The mounted device is removed first because a live device blocks strap removal.
        drop(player, event, equipment.extractDevice());
        // Pouch cells + upgrades are already inside the strap ItemStack and travel with it.
        drop(player, event, equipment.extractItem(ShoulderEquipment.STRAP_SLOT, 1, false));
    }

    private static void drop(Player player, LivingDropsEvent event, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        event.getDrops().add(new ItemEntity(
                player.level(),
                player.getX(),
                player.getY() + 0.5D,
                player.getZ(),
                stack
        ));
    }
}
