package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.network.ShoulderEquipmentActionPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
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
    private static Screen equipmentScreen;
    private static Button equipmentToggle;
    private static MirageEquipmentPanelWidget equipmentPanel;
    private static final List<MirageEquipmentSlotWidget> equipmentSlots = new ArrayList<>();
    private static int capturedEquipmentButton = -1;

    private MirageEquipmentClientEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof AbstractContainerScreen<?> inventory)
                || (!(inventory instanceof InventoryScreen) && !(inventory instanceof CreativeModeInventoryScreen))) {
            if (screen != equipmentScreen) {
                clearWidgetRefs();
            }
            return;
        }

        equipmentScreen = screen;
        equipmentSlots.clear();
        equipmentPanel = null;
        equipmentToggle = null;

        int guiLeft = inventory.getGuiLeft();
        int guiTop = inventory.getGuiTop();
        int rightEdge = guiLeft + inventory.getXSize();
        boolean creative = inventory instanceof CreativeModeInventoryScreen;

        // Keep a real gap from the vanilla frame; this also stops the control reading like part of
        // Creative's page arrows/trash controls.
        // Visually dock the extension to the vanilla frame instead of leaving the toggle/panel
        // floating in the empty screen area.
        int toggleX = rightEdge + 5;
        int toggleY = guiTop + (creative ? 10 : 61);
        int panelX = rightEdge + 24;
        int panelY = guiTop + 3;

        equipmentToggle = Button.builder(
                Component.literal(equipmentPanelOpen ? "›" : "‹"),
                button -> {
                    equipmentPanelOpen = !equipmentPanelOpen;
                    MinecraftScreenReinitializer.reinitialize(inventory);
                }
        ).bounds(toggleX, toggleY, 14, 18).build();
        equipmentToggle.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.translatable("gui.mirage_projector.equipment.toggle")
        ));
        event.addListener(equipmentToggle);

        if (equipmentPanelOpen) {
            boolean strapPresent = ClientShoulderEquipment.localStrapPresent();
            boolean expanded = strapPresent && ClientShoulderEquipment.localExpansionInstalled();
            equipmentPanel = new MirageEquipmentPanelWidget(panelX, panelY, strapPresent, expanded);
            event.addListener(equipmentPanel);

            addEquipmentSlot(event, new MirageEquipmentSlotWidget(
                    panelX + 6,
                    panelY + 20,
                    ShoulderEquipmentActionPayload.Target.STRAP
            ));
            if (strapPresent) {
                addEquipmentSlot(event, new MirageEquipmentSlotWidget(
                        panelX + 30,
                        panelY + 20,
                        ShoulderEquipmentActionPayload.Target.DEVICE
                ));

                int activeUpgrades = ClientShoulderEquipment.localActiveUpgradeSlots();
                for (int i = 0; i < activeUpgrades; i++) {
                    addEquipmentSlot(event, new MirageEquipmentSlotWidget(
                            panelX + 6 + i * 24,
                            panelY + 58,
                            ShoulderEquipmentActionPayload.Target.upgrade(i)
                    ));
                }

                int activeBatteries = ClientShoulderEquipment.localActiveBatterySlots();
                for (int i = 0; i < activeBatteries; i++) {
                    int column = i % 3;
                    int row = i / 3;
                    addEquipmentSlot(event, new MirageEquipmentSlotWidget(
                            panelX + 6 + column * 24,
                            panelY + 94 + row * 22,
                            ShoulderEquipmentActionPayload.Target.battery(i)
                    ));
                }
            }
        }

        applyInventoryTabVisibility(screen);
    }

    private static void addEquipmentSlot(ScreenEvent.Init.Post event, MirageEquipmentSlotWidget slot) {
        equipmentSlots.add(slot);
        event.addListener(slot);
    }

    /**
     * Creative's inventory tab is a page inside one persistent CreativeModeInventoryScreen. Update
     * visibility every frame so Mirage Equipment never leaks into Building Blocks/Search/etc. and
     * appears immediately when the player switches back to the inventory tab.
     */
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        if (event.getScreen() != equipmentScreen) {
            return;
        }

        // Creative keeps one screen instance while rebuilding its child widgets as tabs change.
        // If vanilla discarded our extension, rebuild the current screen once so the Mirage
        // Equipment toggle comes back when the Inventory tab is selected again.
        if (equipmentToggle != null && !event.getScreen().children().contains(equipmentToggle)) {
            MinecraftScreenReinitializer.reinitialize(event.getScreen());
            return;
        }
        applyInventoryTabVisibility(event.getScreen());
    }

    /**
     * The extension lives outside vanilla's container bounds. Intercept its slot clicks before
     * AbstractContainerScreen can interpret them as an outside click and drop the carried stack.
     */
    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() != equipmentScreen || !equipmentPanelOpen || !equipmentVisible(event.getScreen())) {
            return;
        }
        for (MirageEquipmentSlotWidget slot : equipmentSlots) {
            if (slot.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
                capturedEquipmentButton = event.getButton();
                event.setCanceled(true);
                return;
            }
        }
    }


    /**
     * A Mirage Equipment slot press is handled completely by its own server-authoritative packet.
     * Capture the matching release as well, even if the pointer has moved back over vanilla empty
     * space; otherwise AbstractContainerScreen interprets the release as an outside-container drop.
     */
    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getScreen() != equipmentScreen || capturedEquipmentButton < 0) {
            return;
        }
        if (event.getButton() == capturedEquipmentButton) {
            capturedEquipmentButton = -1;
            event.setCanceled(true);
        }
    }

    private static void applyInventoryTabVisibility(Screen screen) {
        boolean visible = equipmentVisible(screen);
        setVisible(equipmentToggle, visible);
        setVisible(equipmentPanel, visible && equipmentPanelOpen);
        for (MirageEquipmentSlotWidget slot : equipmentSlots) {
            setVisible(slot, visible && equipmentPanelOpen);
        }
    }

    private static boolean equipmentVisible(Screen screen) {
        if (screen instanceof InventoryScreen) {
            return true;
        }
        if (screen instanceof CreativeModeInventoryScreen creative) {
            return creative.isInventoryOpen();
        }
        return false;
    }

    private static void setVisible(AbstractWidget widget, boolean visible) {
        if (widget != null) {
            widget.visible = visible;
        }
    }

    private static void clearWidgetRefs() {
        equipmentScreen = null;
        equipmentToggle = null;
        equipmentPanel = null;
        equipmentSlots.clear();
        capturedEquipmentButton = -1;
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
