package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.item.MirageFlashlightItem;
import celerbi.mirageprojector.menu.PortableDeviceMenu;
import celerbi.mirageprojector.network.PortableDeviceActionPayload;
import celerbi.mirageprojector.network.PortableDeviceImagePayload;
import celerbi.mirageprojector.network.PortableDeviceScalePayload;
import celerbi.mirageprojector.registry.ModItems;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Source-aware configuration screen shared by the Mirage Flashlight and Hand Projector. */
public final class PortableDeviceScreen extends AbstractContainerScreen<PortableDeviceMenu> {
    private static final int PROJECTOR_WIDTH = 360;
    private static final int PROJECTOR_HEIGHT = 332;
    private static final int FLASHLIGHT_WIDTH = 196;
    private static final int FLASHLIGHT_HEIGHT = 202;

    private static final int MODE_X = 14;
    private static final int MODE_Y = 42;
    private static final int MODE_W = 78;
    private static final int MODE_GAP = 6;
    private static final int CONTROL_X = 108;
    private static final int CONTROL_WIDTH = 238;
    private static final int CONTROL_Y = 82;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_GAP = 6;

    private Button projectionButton;
    private Button imageImportButton;
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
    private ScaleSlider scaleSlider;
    private Component status = Component.translatable("gui.mirage_projector.status.ready");

    public PortableDeviceScreen(PortableDeviceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = menu.projectorLayout() ? PROJECTOR_WIDTH : FLASHLIGHT_WIDTH;
        imageHeight = menu.projectorLayout() ? PROJECTOR_HEIGHT : FLASHLIGHT_HEIGHT;
        inventoryLabelX = menu.playerInvX();
        inventoryLabelY = menu.playerInvY() - 14;
    }

    @Override
    protected void init() {
        super.init();
        ItemStack device = currentDevice();
        if (device.is(ModItems.MIRAGE_FLASHLIGHT.get())) {
            addRenderableWidget(Button.builder(
                    Component.translatable("gui.mirage_projector.portable_device.cycle_mode"),
                    button -> send(PortableDeviceActionPayload.Action.CYCLE_FLASHLIGHT_MODE)
            ).bounds(leftPos + 35, topPos + 70, 126, 20).build());
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

        scaleSlider = addRenderableWidget(new ScaleSlider(
                x, rowY(1), CONTROL_WIDTH, ROW_HEIGHT,
                currentScale(device),
                scale -> PacketDistributor.sendToServer(new PortableDeviceScalePayload(menu.source(), scale))
        ));

        imageImportButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.portable_device.import_image"),
                button -> openImagePicker()
        ).bounds(x, rowY(2), CONTROL_WIDTH, ROW_HEIGHT).build());

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
        ).bounds(x, rowY(4), 24, ROW_HEIGHT).build());
        warSizeUpButton = addRenderableWidget(Button.builder(
                Component.literal("+"),
                button -> send(PortableDeviceActionPayload.Action.WAR_BANNER_SIZE_UP)
        ).bounds(x + CONTROL_WIDTH - 24, rowY(4), 24, ROW_HEIGHT).build());

        warHeightDownButton = addRenderableWidget(Button.builder(
                Component.literal("−"),
                button -> send(PortableDeviceActionPayload.Action.WAR_BANNER_HEIGHT_DOWN)
        ).bounds(x, rowY(5), 24, ROW_HEIGHT).build());
        warHeightUpButton = addRenderableWidget(Button.builder(
                Component.literal("+"),
                button -> send(PortableDeviceActionPayload.Action.WAR_BANNER_HEIGHT_UP)
        ).bounds(x + CONTROL_WIDTH - 24, rowY(5), 24, ROW_HEIGHT).build());

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
        boolean image = projector && mode == ProjectionSettings.SourceMode.IMAGE;
        boolean banner = projector && mode == ProjectionSettings.SourceMode.BANNER;
        boolean warBanner = banner && MirageHandProjectorItem.warBannerActive(device);

        projectionButton.visible = projector;
        projectionButton.active = projector;
        projectionButton.setMessage(Component.translatable(
                MirageHandProjectorItem.projectionEnabled(device)
                        ? "gui.mirage_projector.portable_device.turn_off"
                        : "gui.mirage_projector.portable_device.turn_on"
        ));

        if (scaleSlider != null) {
            // War Banner owns a single, meaningful scale control (Size %). The generic pixel
            // scale belongs to planar projections and never affected the banner model.
            scaleSlider.visible = projector && !warBanner;
            scaleSlider.active = projector && !warBanner;
            scaleSlider.syncExternal(currentScale(device));
        }

        updateModeButton(imageModeButton, mode == ProjectionSettings.SourceMode.IMAGE);
        updateModeButton(itemModeButton, mode == ProjectionSettings.SourceMode.ITEM);
        updateModeButton(entityModeButton, mode == ProjectionSettings.SourceMode.ENTITY);
        updateModeButton(bannerModeButton, mode == ProjectionSettings.SourceMode.BANNER);

        setVisible(imageImportButton, image);
        setVisible(bannerPresentationButton, banner);
        if (banner) {
            bannerPresentationButton.setMessage(Component.translatable(
                    "gui.mirage_projector.portable_device.banner_presentation_lite",
                    Component.translatable(MirageHandProjectorItem.bannerPresentation(device).translationKey())
            ));
        }

        setVisible(warBannerFacingButton, warBanner);
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
            graphics.fill(x + 12, y + 68, x + 88, y + 112, 0xA20B0E13);
            graphics.fill(x + 12, y + 128, x + 88, y + 172, 0xA20B0E13);
            graphics.fill(x + 12, y + 188, x + 88, y + 232, 0xA20B0E13);
            slotFrame(graphics, x + menu.sourceX() - 1, y + menu.sourceY() - 1, 0xFF7954A0);
            slotFrame(graphics, x + menu.batteryX() - 1, y + menu.batteryY() - 1, 0xFF7954A0);
            slotFrame(graphics, x + menu.coreX() - 1, y + menu.coreY() - 1, 0xFF7954A0);
            renderModeOutline(graphics);
        } else {
            graphics.fill(x + 72, y + 22, x + 124, y + 64, 0xA20B0E13);
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

        if (device.getItem() instanceof MirageFlashlightItem) {
            graphics.drawString(font,
                    Component.translatable("gui.mirage_projector.portable_device.mode", Component.translatable(MirageFlashlightItem.mode(device).displayTranslationKey())),
                    10, 20, 0xFFD7B8F5, false);
            graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.portable_device.battery"), menu.batteryX() + 8, 27, 0xFFD7B8F5);
            return;
        }
        if (!(device.getItem() instanceof MirageHandProjectorItem)) {
            return;
        }

        Component state = Component.translatable(MirageHandProjectorItem.projectionEnabled(device)
                ? "gui.mirage_projector.portable_device.on"
                : "gui.mirage_projector.portable_device.off");
        graphics.drawString(font, state, imageWidth - 10 - font.width(state), 9, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.source_workspaces"), 14, 29, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.source"), 14, 69, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.geometry"), CONTROL_X, 69, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.battery"), 14, 129, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.core"), 14, 189, 0xFFD7B8F5, false);

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

    private int currentScale(ItemStack device) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !(device.getItem() instanceof MirageHandProjectorItem)) {
            return ProjectionSettings.DEFAULT.scalePixels();
        }
        return MirageHandProjectorItem.portableScalePixels(device, minecraft.level);
    }

    private void openImagePicker() {
        status = Component.translatable("gui.mirage_projector.status.selecting");
        Path selected = chooseImageFile(Component.translatable("gui.mirage_projector.portable_device.image_picker").getString());
        if (selected == null) {
            status = Component.translatable("gui.mirage_projector.status.cancelled");
            return;
        }
        status = Component.translatable("gui.mirage_projector.status.processing");
        CompletableFuture.supplyAsync(() -> {
            try {
                return ImageImporter.importFile(selected);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }).whenComplete((imported, throwable) -> Minecraft.getInstance().execute(() -> {
            if (throwable != null) {
                Throwable cause = throwable.getCause() == null ? throwable : throwable.getCause();
                status = Component.translatable("gui.mirage_projector.image.import_failed",
                        cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage());
                return;
            }
            ProjectionTextureCache.invalidate(imported.hash());
            ClientAssetTransport.uploadIfPresent(imported.hash());
            PacketDistributor.sendToServer(new PortableDeviceImagePayload(
                    menu.source(), imported.hash(), imported.width(), imported.height()));
            status = Component.translatable("gui.mirage_projector.portable_device.image_imported",
                    imported.width(), imported.height());
        }));
    }

    private Path chooseImageFile(String title) {
        return NativeImagePicker.choose(title, "the portable image picker");
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

    private static final class ScaleSlider extends AbstractSliderButton {
        private final java.util.function.IntConsumer setter;
        private int scalePixels;
        private int lastSent;

        private ScaleSlider(int x, int y, int width, int height, int scalePixels, java.util.function.IntConsumer setter) {
            super(x, y, width, height, Component.empty(), normalize(scalePixels));
            this.setter = setter;
            this.scalePixels = clamp(scalePixels);
            this.lastSent = this.scalePixels;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("gui.mirage_projector.portable_device.scale", scalePixels));
        }

        @Override
        protected void applyValue() {
            scalePixels = clamp(ProjectionSettings.DEBUG_MIN_SCALE_PIXELS
                    + (int)Math.round(value * (MirageHandProjectorItem.PORTABLE_MAX_SCALE_PIXELS - ProjectionSettings.DEBUG_MIN_SCALE_PIXELS)));
            if (scalePixels != lastSent) {
                lastSent = scalePixels;
                setter.accept(scalePixels);
            }
            updateMessage();
        }

        private void syncExternal(int scalePixels) {
            int safe = clamp(scalePixels);
            if (safe == this.scalePixels) {
                return;
            }
            this.scalePixels = safe;
            this.lastSent = safe;
            this.value = normalize(safe);
            updateMessage();
        }

        private static double normalize(int scalePixels) {
            return (clamp(scalePixels) - ProjectionSettings.DEBUG_MIN_SCALE_PIXELS)
                    / (double)(MirageHandProjectorItem.PORTABLE_MAX_SCALE_PIXELS - ProjectionSettings.DEBUG_MIN_SCALE_PIXELS);
        }

        private static int clamp(int scalePixels) {
            return Math.max(ProjectionSettings.DEBUG_MIN_SCALE_PIXELS,
                    Math.min(MirageHandProjectorItem.PORTABLE_MAX_SCALE_PIXELS, scalePixels));
        }
    }
}
