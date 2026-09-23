package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.equipment.ShoulderEquipment;
import celerbi.mirageprojector.network.ShoulderEquipmentActionPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Shoulder equipment overlay and input handling. */
@EventBusSubscriber(modid = MirageProjector.MOD_ID, value = Dist.CLIENT)
public final class MirageEquipmentClientEvents {
    private static final int SLOT_STEP = MirageEquipmentSlotWidget.SLOT_SIZE;
    private static final int TOGGLE_SIZE = 14;
    private static boolean equipmentPanelOpen;
    private static Screen equipmentScreen;
    private static MirageEquipmentPanelWidget equipmentPanel;
    private static final List<MirageEquipmentSlotWidget> equipmentSlots = new ArrayList<>();
    private static int capturedEquipmentButton = -1;
    private static boolean awaitingEquipmentCursor;
    private static int toggleX;
    private static int toggleY;

    private MirageEquipmentClientEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?> inventory && equipmentVisible(inventory)) {
            bindEquipmentScreen(inventory);
        }
    }

    private static void bindEquipmentScreen(AbstractContainerScreen<?> inventory) {
        equipmentScreen = inventory;
        equipmentSlots.clear();
        equipmentPanel = null;

        int guiLeft = inventory.getGuiLeft();
        int guiTop = inventory.getGuiTop();
        int rightEdge = guiLeft + inventory.getXSize();
        int panelX = rightEdge + 5;
        int panelY = guiTop + 3;
        positionToggle(inventory);
        equipmentPanel = new MirageEquipmentPanelWidget(panelX, panelY);
        addEquipmentSlot(new MirageEquipmentSlotWidget(panelX + 6, panelY + 20,
                ShoulderEquipmentActionPayload.Target.STRAP));
        addEquipmentSlot(new MirageEquipmentSlotWidget(panelX + 30, panelY + 20,
                ShoulderEquipmentActionPayload.Target.DEVICE));
        for (int i = 0; i < ShoulderEquipment.EXPANDED_UPGRADE_SLOTS; i++) {
            addEquipmentSlot(new MirageEquipmentSlotWidget(panelX + 6 + i * SLOT_STEP, panelY + 55,
                    ShoulderEquipmentActionPayload.Target.upgrade(i)));
        }
        for (int i = 0; i < ShoulderEquipment.EXPANDED_BATTERY_SLOTS; i++) {
            int column = i % 4;
            int row = i / 4;
            addEquipmentSlot(new MirageEquipmentSlotWidget(panelX + 6 + column * SLOT_STEP,
                    panelY + 90 + row * SLOT_STEP, ShoulderEquipmentActionPayload.Target.battery(i)));
        }

        applyInventoryTabVisibility(inventory);
    }

    private static void addEquipmentSlot(MirageEquipmentSlotWidget slot) {
        equipmentSlots.add(slot);
    }

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() == equipmentScreen) {
            clearWidgetRefs();
        }
    }

    @SubscribeEvent
    public static void onContainerForeground(ContainerScreenEvent.Render.Foreground event) {
        AbstractContainerScreen<?> inventory = event.getContainerScreen();
        if (!equipmentVisible(inventory)) {
            return;
        }
        if (equipmentScreen != inventory || equipmentPanel == null) {
            bindEquipmentScreen(inventory);
        }
        applyInventoryTabVisibility(inventory);
        var graphics = event.getGuiGraphics();
        graphics.pose().pushPose();
        graphics.pose().translate(-inventory.getGuiLeft(), -inventory.getGuiTop(), 200.0F);
        if (equipmentPanelOpen) {
            equipmentPanel.render(graphics, event.getMouseX(), event.getMouseY(), 0.0F);
            for (MirageEquipmentSlotWidget slot : equipmentSlots) {
                slot.render(graphics, event.getMouseX(), event.getMouseY(), 0.0F);
            }
        }
        renderToggle(graphics);
        graphics.pose().popPose();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> inventory) || !equipmentVisible(inventory)) {
            return;
        }
        if (equipmentScreen != inventory || equipmentPanel == null) {
            bindEquipmentScreen(inventory);
        }
        if (awaitingEquipmentCursor) {
            event.setCanceled(true);
            return;
        }
        if (event.getButton() == 0 && insideToggle(event.getMouseX(), event.getMouseY())) {
            togglePanel(event.getScreen());
            capturedEquipmentButton = 0;
            event.setCanceled(true);
            return;
        }
        if (!equipmentPanelOpen) {
            return;
        }
        for (MirageEquipmentSlotWidget slot : equipmentSlots) {
            if (slot.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
                awaitingEquipmentCursor = true;
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getScreen() != equipmentScreen) {
            return;
        }
        boolean releasedCapturedButton = event.getButton() == capturedEquipmentButton;
        boolean releasedOverEquipment = equipmentPanelOpen
                && insideEquipmentSafeZone(event.getScreen(), event.getMouseX(), event.getMouseY());
        if (releasedCapturedButton || releasedOverEquipment) {
            if (releasedCapturedButton) {
                capturedEquipmentButton = -1;
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (event.getScreen() != equipmentScreen) {
            return;
        }
        boolean draggingCapturedButton = event.getMouseButton() == capturedEquipmentButton;
        boolean draggingOverEquipment = equipmentPanelOpen
                && insideEquipmentSafeZone(event.getScreen(), event.getMouseX(), event.getMouseY());
        if (draggingCapturedButton || draggingOverEquipment || awaitingEquipmentCursor) {
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
            setVisible(slot, visible && equipmentPanelOpen && slot.visibleForCurrentState());
        }
    }

    private static boolean equipmentVisible(Screen screen) {
        if (screen instanceof InventoryScreen) {
            return true;
        }
        if (screen instanceof CreativeModeInventoryScreen) {
            return true;
        }
        return false;
    }

    private static boolean insideEquipmentSafeZone(Screen screen, double mouseX, double mouseY) {
        if (!(screen instanceof AbstractContainerScreen<?> inventory) || equipmentPanel == null) {
            return false;
        }
        int minX = inventory.getGuiLeft() + inventory.getXSize() - 2;
        int maxX = equipmentPanel.getX() + equipmentPanel.getWidth();
        int minY = Math.min(inventory.getGuiTop(), equipmentPanel.getY());
        int maxY = Math.max(inventory.getGuiTop() + inventory.getYSize(), equipmentPanel.getY() + visiblePanelHeight());
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
        awaitingEquipmentCursor = false;
    }

    public static void onEquipmentCursorSynchronized() {
        awaitingEquipmentCursor = false;
    }

    static void refreshIfInventoryOpen() {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.screen instanceof AbstractContainerScreen<?> inventory) {
            positionToggle(inventory);
            applyInventoryTabVisibility(minecraft.screen);
        }
    }

    private static void positionToggle(AbstractContainerScreen<?> inventory) {
        int rightEdge = inventory.getGuiLeft() + inventory.getXSize();
        int panelX = rightEdge + 5;
        toggleX = equipmentPanelOpen ? panelX + MirageEquipmentPanelWidget.WIDTH + 5
                : panelX;
        toggleY = inventory.getGuiTop() + 5;
    }

    private static void togglePanel(Screen screen) {
        equipmentPanelOpen = !equipmentPanelOpen;
        if (screen instanceof AbstractContainerScreen<?> inventory) {
            positionToggle(inventory);
        }
        applyInventoryTabVisibility(screen);
    }

    private static boolean insideToggle(double mouseX, double mouseY) {
        return mouseX >= toggleX && mouseX < toggleX + TOGGLE_SIZE
                && mouseY >= toggleY && mouseY < toggleY + TOGGLE_SIZE;
    }

    private static int visiblePanelHeight() {
        if (!ClientShoulderEquipment.localStrapPresent()) {
            return MirageEquipmentPanelWidget.EMPTY_HEIGHT;
        }
        return ClientShoulderEquipment.localExpansionInstalled()
                ? MirageEquipmentPanelWidget.EXPANDED_HEIGHT
                : MirageEquipmentPanelWidget.BASE_HEIGHT;
    }

    private static void renderToggle(net.minecraft.client.gui.GuiGraphics graphics) {
        int border = 0xFF1B1B1B;
        int fill = 0xFF8B8B8B;
        graphics.fill(toggleX, toggleY, toggleX + TOGGLE_SIZE, toggleY + TOGGLE_SIZE, border);
        graphics.fill(toggleX + 1, toggleY + 1, toggleX + TOGGLE_SIZE - 1, toggleY + TOGGLE_SIZE - 1, fill);
        graphics.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font,
                equipmentPanelOpen ? "‹" : "›", toggleX + TOGGLE_SIZE / 2, toggleY + 3, 0xFFFFFFFF);
    }
}
