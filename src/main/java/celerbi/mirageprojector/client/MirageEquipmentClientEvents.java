package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.network.ShoulderEquipmentActionPayload;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Client inventory-panel and physical shoulder-device hooks. */
@EventBusSubscriber(modid = MirageProjector.MOD_ID, value = Dist.CLIENT)
public final class MirageEquipmentClientEvents {
    private static final int SLOT_STEP = MirageEquipmentSlotWidget.SLOT_SIZE;
    private static boolean equipmentPanelOpen;
    private static Screen equipmentScreen;
    private static MirageEquipmentPanelWidget equipmentPanel;
    private static final List<MirageEquipmentSlotWidget> equipmentSlots = new ArrayList<>();
    private static int capturedEquipmentButton = -1;
    private static int toggleX;
    private static int toggleY;
    private static boolean toggleReady;
    private static Field creativeSelectedTabField;

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
        toggleReady = false;

        int guiLeft = inventory.getGuiLeft();
        int guiTop = inventory.getGuiTop();
        int rightEdge = guiLeft + inventory.getXSize();
        boolean creative = inventory instanceof CreativeModeInventoryScreen;

        toggleX = rightEdge - 20;
        toggleY = guiTop + 6;
        toggleReady = true;
        int panelX = rightEdge + 5;
        int panelY = guiTop + 3;

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
                            panelX + 6 + i * SLOT_STEP,
                            panelY + 55,
                            ShoulderEquipmentActionPayload.Target.upgrade(i)
                    ));
                }

                int activeBatteries = ClientShoulderEquipment.localActiveBatterySlots();
                for (int i = 0; i < activeBatteries; i++) {
                    int column = i % 3;
                    int row = i / 3;
                    addEquipmentSlot(event, new MirageEquipmentSlotWidget(
                            panelX + 6 + column * SLOT_STEP,
                            panelY + 90 + row * SLOT_STEP,
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

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() != equipmentScreen) {
            return;
        }

        applyInventoryTabVisibility(event.getScreen());
        if (toggleReady && equipmentVisible(event.getScreen())) {
            var graphics = event.getGuiGraphics();
            boolean hovered = event.getMouseX() >= toggleX && event.getMouseX() < toggleX + 14
                    && event.getMouseY() >= toggleY && event.getMouseY() < toggleY + 18;
            graphics.fill(toggleX, toggleY, toggleX + 14, toggleY + 18, 0xFF202020);
            graphics.fill(toggleX + 1, toggleY + 1, toggleX + 13, toggleY + 17, 0xFFF0F0F0);
            graphics.fill(toggleX + 2, toggleY + 2, toggleX + 12, toggleY + 16,
                    hovered ? 0xFFAFAFAF : 0xFF8E8E8E);
            graphics.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font,
                    equipmentPanelOpen ? "‹" : "›", toggleX + 7, toggleY + 5, 0xFF202020);
        }
    }

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() != equipmentScreen || !equipmentVisible(event.getScreen())) {
            return;
        }
        if (toggleReady && event.getButton() == 0
                && event.getMouseX() >= toggleX && event.getMouseX() < toggleX + 14
                && event.getMouseY() >= toggleY && event.getMouseY() < toggleY + 18) {
            equipmentPanelOpen = !equipmentPanelOpen;
            MinecraftScreenReinitializer.reinitialize(event.getScreen());
            capturedEquipmentButton = event.getButton();
            event.setCanceled(true);
            return;
        }
        if (!equipmentPanelOpen) {
            return;
        }
        for (MirageEquipmentSlotWidget slot : equipmentSlots) {
            if (slot.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
                capturedEquipmentButton = event.getButton();
                event.setCanceled(true);
                return;
            }
        }
        if (insideEquipmentSafeZone(event.getScreen(), event.getMouseX(), event.getMouseY())) {
            capturedEquipmentButton = event.getButton();
            event.setCanceled(true);
        }
    }

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
        if (!visible && screen instanceof CreativeModeInventoryScreen && equipmentPanelOpen) {
            equipmentPanelOpen = false;
        }
        setVisible(equipmentPanel, visible && equipmentPanelOpen);
        for (MirageEquipmentSlotWidget slot : equipmentSlots) {
            setVisible(slot, visible && equipmentPanelOpen);
        }
    }

    private static boolean equipmentVisible(Screen screen) {
        if (screen instanceof InventoryScreen) {
            return true;
        }
        if (screen instanceof CreativeModeInventoryScreen) {
            return isCreativeSurvivalInventory();
        }
        return false;
    }

    private static boolean isCreativeSurvivalInventory() {
        var screen = net.minecraft.client.Minecraft.getInstance().screen;
        if (!(screen instanceof CreativeModeInventoryScreen creative)) {
            return false;
        }
        try {
            if (creativeSelectedTabField == null) {
                creativeSelectedTabField = CreativeModeInventoryScreen.class.getDeclaredField("selectedTab");
                creativeSelectedTabField.setAccessible(true);
            }
            Object selected = Modifier.isStatic(creativeSelectedTabField.getModifiers())
                    ? creativeSelectedTabField.get(null)
                    : creativeSelectedTabField.get(creative);
            Object inventoryTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.INVENTORY);
            return creative.isInventoryOpen() || selected == inventoryTab || inventoryTab.equals(selected);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return creative.isInventoryOpen();
        }
    }

    private static boolean insideEquipmentSafeZone(Screen screen, double mouseX, double mouseY) {
        if (!(screen instanceof AbstractContainerScreen<?> inventory) || equipmentPanel == null) {
            return false;
        }
        int minX = inventory.getGuiLeft() + inventory.getXSize() - 2;
        int maxX = equipmentPanel.getX() + equipmentPanel.getWidth();
        int minY = Math.min(inventory.getGuiTop(), equipmentPanel.getY());
        int maxY = Math.max(inventory.getGuiTop() + inventory.getYSize(), equipmentPanel.getY() + equipmentPanel.getHeight());
        return mouseX >= minX && mouseX < maxX && mouseY >= minY && mouseY < maxY;
    }

    private static void setVisible(AbstractWidget widget, boolean visible) {
        if (widget != null) {
            widget.visible = visible;
        }
    }

    private static void clearWidgetRefs() {
        equipmentScreen = null;
        equipmentPanel = null;
        equipmentSlots.clear();
        capturedEquipmentButton = -1;
        toggleReady = false;
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
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        ClientEntityScanner.poseScanningArm(event.getEntity(), event.getRenderer());
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        ClientEntityScanner.restoreScanningArm(event.getRenderer());
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
