package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.item.MirageLanternItem;
import celerbi.mirageprojector.menu.PortableDeviceMenu;
import celerbi.mirageprojector.network.PortableDeviceActionPayload;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Compact, source-aware battery/configuration GUI for the handheld and shoulder devices. */
public final class PortableDeviceScreen extends AbstractContainerScreen<PortableDeviceMenu> {
    private static final int PROJECTOR_WIDTH = 252;
    private static final int PROJECTOR_HEIGHT = 294;
    private static final int LANTERN_WIDTH = 196;
    private static final int LANTERN_HEIGHT = 184;

    private static final int MODE_X = 12;
    private static final int MODE_Y = 34;
    private static final int MODE_W = 54;
    private static final int MODE_GAP = 4;
    private static final int CONTROL_X = 96;
    private static final int CONTROL_WIDTH = 144;
    private static final int CONTROL_Y = 62;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_GAP = 4;

    private Button projectionButton;
    private Button copyButton;
    private Button imageModeButton;
    private Button itemModeButton;
    private Button entityModeButton;
    private Button bannerModeButton;
    private Button bannerPresentationButton;
    private Button warBannerFacingButton;
    private Button warSizeDownButton;
    private Button warSizeUpButton;
    private Button warHeightDownButton;
    private Button warHeightUpButton;

    public PortableDeviceScreen(PortableDeviceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = menu.projectorLayout() ? PROJECTOR_WIDTH : LANTERN_WIDTH;
        imageHeight = menu.projectorLayout() ? PROJECTOR_HEIGHT : LANTERN_HEIGHT;
        inventoryLabelX = menu.playerInvX();
        inventoryLabelY = menu.playerInvY() - 12;
    }

    @Override
    protected void init() {
        super.init();
        ItemStack device = currentDevice();
        if (device.is(ModItems.MIRAGE_LANTERN.get())) {
            addRenderableWidget(Button.builder(
                    Component.translatable("gui.mirage_projector.portable_device.cycle_mode"),
                    button -> send(PortableDeviceActionPayload.Action.CYCLE_LANTERN_MODE)
            ).bounds(leftPos + 58, topPos + 39, 126, 20).build());
            return;
        }
        if (!device.is(ModItems.MIRAGE_HAND_PROJECTOR.get())) {
            return;
        }

        imageModeButton = addModeButton(0, "gui.mirage_projector.workspace.image_short", PortableDeviceActionPayload.Action.SELECT_IMAGE);
        itemModeButton = addModeButton(1, "gui.mirage_projector.workspace.item_short", PortableDeviceActionPayload.Action.SELECT_ITEM);
        entityModeButton = addModeButton(2, "gui.mirage_projector.workspace.entity_short", PortableDeviceActionPayload.Action.SELECT_ENTITY);
        bannerModeButton = addModeButton(3, "gui.mirage_projector.workspace.banner_short", PortableDeviceActionPayload.Action.SELECT_BANNER);

        int x = leftPos + CONTROL_X;
        projectionButton = addRenderableWidget(Button.builder(
                Component.empty(),
                button -> send(PortableDeviceActionPayload.Action.TOGGLE_PROJECTOR)
        ).bounds(x, rowY(0), CONTROL_WIDTH, ROW_HEIGHT).build());

        copyButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.portable_device.copy_target"),
                button -> send(PortableDeviceActionPayload.Action.COPY_TARGET_PROJECTOR)
        ).bounds(x, rowY(1), CONTROL_WIDTH, ROW_HEIGHT).build());

        bannerPresentationButton = addRenderableWidget(Button.builder(
                Component.empty(),
                button -> send(PortableDeviceActionPayload.Action.CYCLE_BANNER_PRESENTATION)
        ).bounds(x, rowY(2), CONTROL_WIDTH, ROW_HEIGHT).build());

        warBannerFacingButton = addRenderableWidget(Button.builder(
                Component.empty(),
                button -> send(PortableDeviceActionPayload.Action.CYCLE_WAR_BANNER_FACING)
        ).bounds(x, rowY(3), CONTROL_WIDTH, ROW_HEIGHT).build());

        warSizeDownButton = addRenderableWidget(Button.builder(
                Component.literal("−"),
                button -> send(PortableDeviceActionPayload.Action.WAR_BANNER_SIZE_DOWN)
        ).bounds(x, rowY(4), 22, ROW_HEIGHT).build());
        warSizeUpButton = addRenderableWidget(Button.builder(
                Component.literal("+"),
                button -> send(PortableDeviceActionPayload.Action.WAR_BANNER_SIZE_UP)
        ).bounds(x + CONTROL_WIDTH - 22, rowY(4), 22, ROW_HEIGHT).build());

        warHeightDownButton = addRenderableWidget(Button.builder(
                Component.literal("−"),
                button -> send(PortableDeviceActionPayload.Action.WAR_BANNER_HEIGHT_DOWN)
        ).bounds(x, rowY(5), 22, ROW_HEIGHT).build());
        warHeightUpButton = addRenderableWidget(Button.builder(
                Component.literal("+"),
                button -> send(PortableDeviceActionPayload.Action.WAR_BANNER_HEIGHT_UP)
        ).bounds(x + CONTROL_WIDTH - 22, rowY(5), 22, ROW_HEIGHT).build());

        updateProjectorControls();
    }

    private Button addModeButton(int index, String translationKey, PortableDeviceActionPayload.Action action) {
        return addRenderableWidget(Button.builder(Component.translatable(translationKey), button -> send(action))
                .bounds(leftPos + MODE_X + index * (MODE_W + MODE_GAP), topPos + MODE_Y, MODE_W, 18)
                .build());
    }

    private int rowY(int row) {
        return topPos + CONTROL_Y + row * (ROW_HEIGHT + ROW_GAP);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateProjectorControls();
    }

    private void updateProjectorControls() {
        if (projectionButton == null) {
            return;
        }
        ItemStack device = currentDevice();
        boolean projector = device.is(ModItems.MIRAGE_HAND_PROJECTOR.get());
        ProjectionSettings.SourceMode mode = MirageHandProjectorItem.sourceMode(device);
        boolean banner = projector && mode == ProjectionSettings.SourceMode.BANNER;
        boolean warBanner = banner && MirageHandProjectorItem.warBannerActive(device);

        projectionButton.visible = projector;
        projectionButton.active = projector;
        projectionButton.setMessage(Component.translatable(
                MirageHandProjectorItem.projectionEnabled(device)
                        ? "gui.mirage_projector.portable_device.turn_off"
                        : "gui.mirage_projector.portable_device.turn_on"
        ));

        copyButton.visible = projector;
        copyButton.active = projector;

        updateModeButton(imageModeButton, mode == ProjectionSettings.SourceMode.IMAGE);
        updateModeButton(itemModeButton, mode == ProjectionSettings.SourceMode.ITEM);
        updateModeButton(entityModeButton, mode == ProjectionSettings.SourceMode.ENTITY);
        updateModeButton(bannerModeButton, mode == ProjectionSettings.SourceMode.BANNER);

        bannerPresentationButton.visible = banner;
        bannerPresentationButton.active = banner;
        if (banner) {
            bannerPresentationButton.setMessage(Component.translatable(
                    "gui.mirage_projector.portable_device.banner_presentation_lite",
                    Component.translatable(MirageHandProjectorItem.bannerPresentation(device).translationKey())
            ));
        }

        warBannerFacingButton.visible = warBanner;
        warBannerFacingButton.active = warBanner;
        if (warBanner) {
            warBannerFacingButton.setMessage(Component.translatable(
                    "gui.mirage_projector.portable_device.war_banner_facing_lite",
                    Component.translatable(MirageHandProjectorItem.warBannerFacing(device).translationKey())
            ));
        }

        setVisible(warSizeDownButton, warBanner);
        setVisible(warSizeUpButton, warBanner);
        setVisible(warHeightDownButton, warBanner);
        setVisible(warHeightUpButton, warBanner);
    }

    private static void updateModeButton(Button button, boolean selected) {
        if (button != null) {
            button.visible = true;
            button.active = !selected;
        }
    }

    private static void setVisible(Button button, boolean visible) {
        if (button != null) {
            button.visible = visible;
            button.active = visible;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF014171D);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 3, 0xFF72528D);

        if (menu.projectorLayout()) {
            graphics.fill(x + 12, y + 58, x + 88, y + 102, 0xA20B0E13);
            slotFrame(graphics, x + menu.batteryX() - 1, y + menu.batteryY() - 1, 0xFF7954A0);
            slotFrame(graphics, x + menu.sourceX() - 1, y + menu.sourceY() - 1, 0xFF7954A0);
            renderModeOutline(graphics);
        } else {
            graphics.fill(x + 10, y + 27, x + 54, y + 83, 0xA20B0E13);
            slotFrame(graphics, x + menu.batteryX() - 1, y + menu.batteryY() - 1, 0xFF7954A0);
        }

        int inventoryPanelTop = menu.playerInvY() - 16;
        graphics.fill(x + menu.playerInvX() - 8, y + inventoryPanelTop,
                x + menu.playerInvX() + 170, y + imageHeight - 8, 0xA20B0E13);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slotFrame(graphics,
                        x + menu.playerInvX() + col * 18 - 1,
                        y + menu.playerInvY() + row * 18 - 1,
                        0xFF4D4A52);
            }
        }
        for (int col = 0; col < 9; col++) {
            slotFrame(graphics,
                    x + menu.playerInvX() + col * 18 - 1,
                    y + menu.playerInvY() + 58 - 1,
                    0xFF4D4A52);
        }
    }

    private void renderModeOutline(GuiGraphics graphics) {
        ItemStack device = currentDevice();
        ProjectionSettings.SourceMode mode = MirageHandProjectorItem.sourceMode(device);
        Button selected = mode == ProjectionSettings.SourceMode.IMAGE ? imageModeButton
                : mode == ProjectionSettings.SourceMode.ITEM ? itemModeButton
                : mode == ProjectionSettings.SourceMode.ENTITY ? entityModeButton
                : mode == ProjectionSettings.SourceMode.BANNER ? bannerModeButton
                : null;
        if (selected == null) return;
        int x0 = selected.getX() - 1;
        int y0 = selected.getY() - 1;
        int x1 = selected.getX() + selected.getWidth() + 1;
        int y1 = selected.getY() + selected.getHeight() + 1;
        graphics.fill(x0, y0, x1, y0 + 1, 0xFFD7B8F5);
        graphics.fill(x0, y1 - 1, x1, y1, 0xFFD7B8F5);
        graphics.fill(x0, y0, x0 + 1, y1, 0xFFD7B8F5);
        graphics.fill(x1 - 1, y0, x1, y1, 0xFFD7B8F5);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack device = currentDevice();
        graphics.drawString(font, device.getHoverName(), 10, 9, 0xFFF4F4F4, false);
        graphics.drawString(font, Component.translatable("container.inventory"), inventoryLabelX, inventoryLabelY, 0xFFBEB8C8, false);

        if (device.getItem() instanceof MirageLanternItem) {
            graphics.drawString(font,
                    Component.translatable("gui.mirage_projector.portable_device.mode", Component.translatable(MirageLanternItem.mode(device).displayTranslationKey())),
                    10, 20, 0xFFD7B8F5, false);
            graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.portable_device.battery"), 39, 32, 0xFFD7B8F5);
            return;
        }
        if (!(device.getItem() instanceof MirageHandProjectorItem)) {
            return;
        }

        Component state = Component.translatable(MirageHandProjectorItem.projectionEnabled(device)
                ? "gui.mirage_projector.portable_device.on"
                : "gui.mirage_projector.portable_device.off");
        graphics.drawString(font, state, 10, 20, 0xFFD7B8F5, false);
        graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.portable_device.battery"), menu.batteryX() + 8, 59, 0xFFD7B8F5);
        graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.portable_device.source"), menu.sourceX() + 8, 59, 0xFFD7B8F5);

        ProjectionSettings.SourceMode mode = MirageHandProjectorItem.sourceMode(device);
        Component sourceHint = mode == ProjectionSettings.SourceMode.IMAGE
                ? Component.translatable("gui.mirage_projector.portable_device.source_image_hint")
                : Component.translatable("gui.mirage_projector.portable_device.source_snapshot_hint");
        graphics.drawString(font, fit(sourceHint.getString(), 78), 12, 93, 0xFF9CA3AF, false);

        if (MirageHandProjectorItem.warBannerActive(device)) {
            int labelCenter = CONTROL_X + CONTROL_WIDTH / 2;
            graphics.drawCenteredString(font,
                    Component.translatable("gui.mirage_projector.portable_device.war_banner_size_value", MirageHandProjectorItem.warBannerSizePercent(device)),
                    labelCenter, CONTROL_Y + 4 * (ROW_HEIGHT + ROW_GAP) + 5, 0xFFD7B8F5);
            graphics.drawCenteredString(font,
                    Component.translatable("gui.mirage_projector.portable_device.war_banner_height_value", MirageHandProjectorItem.warBannerHeightPixels(device)),
                    labelCenter, CONTROL_Y + 5 * (ROW_HEIGHT + ROW_GAP) + 5, 0xFFD7B8F5);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private ItemStack currentDevice() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return ItemStack.EMPTY;
        return switch (menu.source()) {
            case MAIN_HAND -> minecraft.player.getMainHandItem();
            case OFF_HAND -> minecraft.player.getOffhandItem();
            case SHOULDER -> ClientShoulderEquipment.localDevice();
        };
    }

    private void send(PortableDeviceActionPayload.Action action) {
        PacketDistributor.sendToServer(new PortableDeviceActionPayload(menu.source(), action));
    }

    private static void slotFrame(GuiGraphics graphics, int x, int y, int border) {
        graphics.fill(x, y, x + 18, y + 18, border);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF171A20);
    }

    private String fit(String value, int maxWidth) {
        if (value == null || font.width(value) <= maxWidth) return value == null ? "" : value;
        String ellipsis = "…";
        int target = Math.max(0, maxWidth - font.width(ellipsis));
        int end = value.length();
        while (end > 0 && font.width(value.substring(0, end)) > target) end--;
        return value.substring(0, Math.max(0, end)) + ellipsis;
    }
}
