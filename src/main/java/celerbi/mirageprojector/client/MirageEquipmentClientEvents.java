package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.network.ShoulderEquipmentActionPayload;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Client inventory-panel and physical shoulder-device hooks. */
@EventBusSubscriber(modid = MirageProjector.MOD_ID, value = Dist.CLIENT)
public final class MirageEquipmentClientEvents {
    private static boolean equipmentPanelOpen;

    private MirageEquipmentClientEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> inventory)
                || (!(inventory instanceof InventoryScreen) && !(inventory instanceof CreativeModeInventoryScreen))) {
            return;
        }

        int guiLeft = inventory.getGuiLeft();
        int guiTop = inventory.getGuiTop();
        boolean creative = inventory instanceof CreativeModeInventoryScreen;

        // Always expand to the right. The left side is commonly occupied by potion effects,
        // recipe/utility overlays and other modded inventory extensions.
        int panelX = guiLeft + (creative ? 200 : 180);
        int panelY = guiTop + 3;

        // Survival places the toggle below the crafting result. Creative has no equivalent result
        // slot, so anchor the same compact control immediately beside the vanilla creative frame.
        int toggleX = guiLeft + (creative ? 178 : 156);
        int toggleY = guiTop + (creative ? 8 : 61);
        Button toggle = Button.builder(
                Component.literal(equipmentPanelOpen ? "›" : "‹"),
                button -> {
                    equipmentPanelOpen = !equipmentPanelOpen;
                    MinecraftScreenReinitializer.reinitialize(inventory);
                }
        ).bounds(toggleX, toggleY, 14, 18).build();
        toggle.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.translatable("gui.mirage_projector.equipment.toggle")
        ));
        event.addListener(toggle);

        if (!equipmentPanelOpen) {
            return;
        }

        boolean strapPresent = ClientShoulderEquipment.localStrapPresent();
        boolean expanded = strapPresent && ClientShoulderEquipment.localExpansionInstalled();
        event.addListener(new MirageEquipmentPanelWidget(panelX, panelY, strapPresent, expanded));

        // The folded-out panel always exposes exactly one socket first: the Shoulder Strap.
        event.addListener(new MirageEquipmentSlotWidget(
                panelX + 6,
                panelY + 20,
                ShoulderEquipmentActionPayload.Target.STRAP
        ));
        if (!strapPresent) {
            return;
        }

        // The strap itself unlocks the mounted device socket and its own pouch contents.
        event.addListener(new MirageEquipmentSlotWidget(
                panelX + 30,
                panelY + 20,
                ShoulderEquipmentActionPayload.Target.DEVICE
        ));

        int activeUpgrades = ClientShoulderEquipment.localActiveUpgradeSlots();
        for (int i = 0; i < activeUpgrades; i++) {
            event.addListener(new MirageEquipmentSlotWidget(
                    panelX + 6 + i * 24,
                    panelY + 58,
                    ShoulderEquipmentActionPayload.Target.upgrade(i)
            ));
        }

        int activeBatteries = ClientShoulderEquipment.localActiveBatterySlots();
        for (int i = 0; i < activeBatteries; i++) {
            int column = i % 3;
            int row = i / 3;
            event.addListener(new MirageEquipmentSlotWidget(
                    panelX + 6 + column * 24,
                    panelY + 94 + row * 22,
                    ShoulderEquipmentActionPayload.Target.battery(i)
            ));
        }
    }

    static void refreshIfInventoryOpen() {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.screen instanceof InventoryScreen inventory) {
            MinecraftScreenReinitializer.reinitialize(inventory);
        } else if (minecraft.screen instanceof CreativeModeInventoryScreen creative) {
            MinecraftScreenReinitializer.reinitialize(creative);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        ClientShoulderEquipment.renderMountedDevice(
                event.getEntity(),
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight()
        );
    }

    /** Keeps the event class free of protected Screen#init access. */
    private static final class MinecraftScreenReinitializer {
        private MinecraftScreenReinitializer() {
        }

        static void reinitialize(Screen screen) {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            screen.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        }
    }
}
