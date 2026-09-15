package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.item.GlowDustItem;
import celerbi.mirageprojector.item.LightBatteryItem;
import celerbi.mirageprojector.item.RechargeableEnergyItem;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Charge-preserving crafting rules for rechargeable media. */
@EventBusSubscriber(modid = MirageProjector.MOD_ID)
public final class RechargeableCraftingEvents {
    private RechargeableCraftingEvents() {
    }

    /**
     * Any future Light Battery recipe that consumes exactly five Glow Dust media inherits the
     * average charge of those five ingredients. The other recipe ingredients remain intentionally
     * unfrozen; this hook therefore does not invent the final survival recipe.
     */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        if (result == null || !result.is(ModItems.LIGHT_BATTERY.get())) {
            return;
        }

        Container grid = event.getInventory();
        long totalGlowCharge = 0L;
        int glowMedia = 0;
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack ingredient = grid.getItem(slot);
            if (ingredient.is(ModItems.GLOW_DUST.get()) || ingredient.is(Items.GLOWSTONE_DUST)) {
                RechargeableEnergyItem energy = RechargeableEnergyItem.fromStack(ingredient);
                if (energy == null) {
                    continue;
                }
                totalGlowCharge += Math.round(
                        energy.storedChargeFraction(ingredient) * GlowDustItem.MAX_CHARGE
                );
                glowMedia++;
            }
        }
        if (glowMedia != 5) {
            return;
        }

        RechargeableEnergyItem battery = RechargeableEnergyItem.fromStack(result);
        if (!(battery instanceof LightBatteryItem)) {
            return;
        }
        double averageFraction = totalGlowCharge / (double) (5 * GlowDustItem.MAX_CHARGE);
        int resultingCharge = (int) Math.round(averageFraction * LightBatteryItem.MAX_CHARGE);
        battery.setStoredCharge(result, resultingCharge);
    }
}
