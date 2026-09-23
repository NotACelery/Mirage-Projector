package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.item.MirageFlashlightItem;
import celerbi.mirageprojector.menu.PortableDeviceMenu;
import celerbi.mirageprojector.network.PortableDeviceActionPayload;
import celerbi.mirageprojector.network.PortableDeviceDistancePayload;
import celerbi.mirageprojector.network.PortableDeviceEntityRotationPayload;
import celerbi.mirageprojector.network.PortableDeviceWarBannerPayload;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Source-aware configuration screen shared by the Mirage Flashlight and Hand Projector. */
public final class PortableDeviceScreen extends AbstractContainerScreen<PortableDeviceMenu> {
    private static final int PROJECTOR_WIDTH = 360;
    private static final int PROJECTOR_HEIGHT = 380;
    private static final int FLASHLIGHT_WIDTH = 196;
    private static final int FLASHLIGHT_HEIGHT = 202;

    private static final int MODE_X = 14;
    private static final int MODE_Y = 42;
    private static final int MODE_W = 106;
    private static final int MODE_GAP = 8;
    private static final int SLOT_PANEL_RIGHT = 62;
    private static final int PANEL_X = 72;
    private static final int CONTROL_X = 72;
    private static final int CONTROL_WIDTH = 154;
    private static final int PREVIEW_X = 236;
    private static final int PREVIEW_WIDTH = 112;
    private static final int GENERAL_CONTROL_Y = 90;
    private static final int SOURCE_CONTROL_Y = 166;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_GAP = 6;

    private Button projectionButton;
    private Button imageImportButton;
    private Button imagePresentationButton;
    private Button imageLiftButton;
    private Button imageRotationButton;
    private Button imageFaceModeButton;
    private Button imageModeButton;
    private Button entityModeButton;
    private Button entityRotationButton;
    private Button bannerModeButton;
    private Button bannerPresentationButton;
    private Button warBannerFacingButton;
    private ScaleSlider scaleSlider;
    private DistanceSlider distanceSlider;
    private EntityRotationSlider entityRotationSlider;
    private WarBannerSizeSlider warBannerSizeSlider;
    private WarBannerHeightSlider warBannerHeightSlider;
    private final EntityProjectionPreviewRenderer entityPreview = new EntityProjectionPreviewRenderer();
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
        entityModeButton = addModeButton(1, "gui.mirage_projector.workspace.entity_short", PortableDeviceActionPayload.Action.SELECT_ENTITY);
        bannerModeButton = addModeButton(2, "gui.mirage_projector.workspace.banner_short", PortableDeviceActionPayload.Action.SELECT_BANNER);

        int x = leftPos + CONTROL_X;
        projectionButton = addRenderableWidget(Button.builder(
                Component.empty(),
                button -> send(PortableDeviceActionPayload.Action.TOGGLE_PROJECTOR)
        ).bounds(leftPos + imageWidth - 72, topPos + 7, 60, ROW_HEIGHT).build());

        distanceSlider = addRenderableWidget(new DistanceSlider(
                x, generalRowY(0), CONTROL_WIDTH, ROW_HEIGHT,
                currentDistance(device),
                distance -> PacketDistributor.sendToServer(new PortableDeviceDistancePayload(menu.source(), distance))
        ));

        scaleSlider = addRenderableWidget(new ScaleSlider(
                x, generalRowY(1), CONTROL_WIDTH, ROW_HEIGHT,
                currentScale(device), currentMaxScale(device),
                scale -> PacketDistributor.sendToServer(new PortableDeviceScalePayload(menu.source(), scale))
        ));

        imageImportButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.portable_device.import_image"),
                button -> openImagePicker()
        ).bounds(x, sourceRowY(0), CONTROL_WIDTH, ROW_HEIGHT).build());

        imagePresentationButton = addRenderableWidget(Button.builder(
                Component.empty(), button -> send(PortableDeviceActionPayload.Action.CYCLE_IMAGE_PRESENTATION)
        ).bounds(x, sourceRowY(1), CONTROL_WIDTH, ROW_HEIGHT).build());

        int imageToggleWidth = (CONTROL_WIDTH - 6) / 2;
        imageLiftButton = addRenderableWidget(Button.builder(
                Component.empty(), button -> send(PortableDeviceActionPayload.Action.TOGGLE_IMAGE_LIFT)
        ).bounds(x, sourceRowY(2), imageToggleWidth, ROW_HEIGHT).build());
        imageRotationButton = addRenderableWidget(Button.builder(
                Component.empty(), button -> send(PortableDeviceActionPayload.Action.TOGGLE_IMAGE_ROTATION)
        ).bounds(x + imageToggleWidth + 6, sourceRowY(2), imageToggleWidth, ROW_HEIGHT).build());
        imageFaceModeButton = addRenderableWidget(Button.builder(
                Component.empty(), button -> send(PortableDeviceActionPayload.Action.CYCLE_IMAGE_FACE_MODE)
        ).bounds(x, sourceRowY(3), CONTROL_WIDTH, ROW_HEIGHT).build());

        entityRotationSlider = addRenderableWidget(new EntityRotationSlider(
                x, sourceRowY(0), CONTROL_WIDTH, ROW_HEIGHT, currentEntityRotation(device),
                degrees -> PacketDistributor.sendToServer(new PortableDeviceEntityRotationPayload(menu.source(), degrees))
        ));
        entityRotationButton = addRenderableWidget(Button.builder(
                Component.empty(), button -> send(PortableDeviceActionPayload.Action.TOGGLE_ENTITY_ROTATION)
        ).bounds(x, sourceRowY(1), CONTROL_WIDTH, ROW_HEIGHT).build());

        bannerPresentationButton = addRenderableWidget(Button.builder(
                Component.empty(),
                button -> send(PortableDeviceActionPayload.Action.CYCLE_BANNER_PRESENTATION)
        ).bounds(x, sourceRowY(0), CONTROL_WIDTH, ROW_HEIGHT).build());

        warBannerFacingButton = addRenderableWidget(Button.builder(
                Component.empty(),
                button -> send(PortableDeviceActionPayload.Action.CYCLE_WAR_BANNER_FACING)
        ).bounds(x, sourceRowY(1), CONTROL_WIDTH, ROW_HEIGHT).build());

        warBannerSizeSlider = addRenderableWidget(new WarBannerSizeSlider(
                x, sourceRowY(2), CONTROL_WIDTH, ROW_HEIGHT, MirageHandProjectorItem.warBannerSizePercent(device),
                value -> PacketDistributor.sendToServer(new PortableDeviceWarBannerPayload(
                        menu.source(), PortableDeviceWarBannerPayload.Parameter.SIZE, value))
        ));
        warBannerHeightSlider = addRenderableWidget(new WarBannerHeightSlider(
                x, sourceRowY(3), CONTROL_WIDTH, ROW_HEIGHT, MirageHandProjectorItem.warBannerHeightPixels(device),
                value -> PacketDistributor.sendToServer(new PortableDeviceWarBannerPayload(
                        menu.source(), PortableDeviceWarBannerPayload.Parameter.HEIGHT, value))
        ));

        updateProjectorControls();
    }

    private Button addModeButton(int index, String translationKey, PortableDeviceActionPayload.Action action) {
        return addRenderableWidget(Button.builder(Component.translatable(translationKey), button -> send(action))
                .bounds(leftPos + MODE_X + index * (MODE_W + MODE_GAP), topPos + MODE_Y, MODE_W, 18)
                .build());
    }

    private int generalRowY(int row) {
        return topPos + GENERAL_CONTROL_Y + row * (ROW_HEIGHT + ROW_GAP);
    }

    private int sourceRowY(int row) {
        return topPos + SOURCE_CONTROL_Y + row * (ROW_HEIGHT + ROW_GAP);
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
        boolean entity = projector && mode == ProjectionSettings.SourceMode.ENTITY;
        boolean banner = projector && mode == ProjectionSettings.SourceMode.BANNER;
        boolean warBanner = banner && MirageHandProjectorItem.warBannerActive(device);

        projectionButton.visible = projector;
        projectionButton.active = projector;
        projectionButton.setMessage(Component.translatable(
                MirageHandProjectorItem.projectionEnabled(device)
                        ? "gui.mirage_projector.portable_device.turn_off_short"
                        : "gui.mirage_projector.portable_device.turn_on_short"
        ));

        if (scaleSlider != null) {
            scaleSlider.visible = projector && !warBanner;
            scaleSlider.active = projector && !warBanner;
            scaleSlider.syncExternal(currentScale(device), currentMaxScale(device));
        }
        if (distanceSlider != null) {
            distanceSlider.visible = projector;
            distanceSlider.active = projector;
            distanceSlider.syncExternal(currentDistance(device));
        }

        updateModeButton(imageModeButton, mode == ProjectionSettings.SourceMode.IMAGE);
        updateModeButton(entityModeButton, mode == ProjectionSettings.SourceMode.ENTITY);
        updateModeButton(bannerModeButton, mode == ProjectionSettings.SourceMode.BANNER);

        setVisible(imageImportButton, image);
        setVisible(imagePresentationButton, image);
        setVisible(imageLiftButton, image);
        setVisible(imageRotationButton, image);
        setVisible(imageFaceModeButton, image);
        if (image) {
            imagePresentationButton.setMessage(Component.translatable(
                    MirageHandProjectorItem.imagePresentation(device).translationKey()));
            ProjectionSettings settings = portableSettings(device);
            imageLiftButton.setMessage(Component.translatable(
                    "gui.mirage_projector.portable_device.image_lift",
                    Component.translatable(settings.liftPixels() > 0
                            ? "gui.mirage_projector.on" : "gui.mirage_projector.off")));
            imageRotationButton.setMessage(Component.translatable(
                    "gui.mirage_projector.rotation",
                    Component.translatable(settings.rotationEnabled()
                            ? "gui.mirage_projector.on" : "gui.mirage_projector.off")));
            imageFaceModeButton.setMessage(Component.translatable(imageFaceModeKey(settings.backFaceMode())));
        }
        if (entityRotationSlider != null) {
            entityRotationSlider.visible = entity;
            entityRotationSlider.active = entity;
        }
        setVisible(entityRotationButton, entity);
        if (entity) {
            ProjectionSettings settings = portableSettings(device);
            entityRotationSlider.syncExternal(currentEntityRotation(device));
            entityRotationButton.setMessage(Component.translatable(
                    "gui.mirage_projector.portable_device.entity_auto_rotation",
                    Component.translatable(settings.rotationEnabled()
                            ? "gui.mirage_projector.on" : "gui.mirage_projector.off")));
        }
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

        if (warBannerSizeSlider != null) {
            warBannerSizeSlider.visible = warBanner;
            warBannerSizeSlider.active = warBanner;
            warBannerSizeSlider.syncExternal(MirageHandProjectorItem.warBannerSizePercent(device));
        }
        if (warBannerHeightSlider != null) {
            warBannerHeightSlider.visible = warBanner;
            warBannerHeightSlider.active = warBanner;
            warBannerHeightSlider.syncExternal(MirageHandProjectorItem.warBannerHeightPixels(device));
        }
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
            graphics.fill(x + 12, y + 68, x + SLOT_PANEL_RIGHT, y + 122, 0xA20B0E13);
            graphics.fill(x + PANEL_X - 10, y + 68, x + CONTROL_X + CONTROL_WIDTH + 10, y + 136, 0xA20B0E13);
            graphics.fill(x + PANEL_X - 10, y + 144, x + CONTROL_X + CONTROL_WIDTH + 10, y + 258, 0xA20B0E13);
            graphics.fill(x + 12, y + 126, x + SLOT_PANEL_RIGHT, y + 178, 0xA20B0E13);
            graphics.fill(x + 12, y + 184, x + SLOT_PANEL_RIGHT, y + 236, 0xA20B0E13);
            graphics.fill(x + PREVIEW_X, y + 68, x + PREVIEW_X + PREVIEW_WIDTH, y + 258, 0xA20B0E13);
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

        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.source_workspaces"), 14, 29, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.source"), 16, 76, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.general"), CONTROL_X, 76, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.source_options"), CONTROL_X, 152, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.battery"), 16, 134, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.core"), 16, 192, 0xFFD7B8F5, false);
        graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.portable_device.preview"),
                PREVIEW_X + PREVIEW_WIDTH / 2, 76, 0xFFD7B8F5);

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderWorkspacePreview(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderWorkspacePreview(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.projectorLayout()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        ItemStack device = currentDevice();
        if (!(device.getItem() instanceof MirageHandProjectorItem)) return;
        MirageProjectorBlockEntity portable = MirageHandProjectorItem.createPortableProjector(
                device, minecraft.level, BlockPos.ZERO, Direction.SOUTH);
        if (portable == null) return;

        int x = leftPos + PREVIEW_X + 5;
        int y = topPos + 90;
        int width = PREVIEW_WIDTH - 10;
        int height = 160;
        ProjectionSettings.SourceMode mode = MirageHandProjectorItem.sourceMode(device);
        if (mode == ProjectionSettings.SourceMode.ENTITY) {
            entityPreview.render(graphics, portable.entityProjectionState(), portable.settings(),
                    x, y, width, height, mouseX, mouseY);
        } else if (mode == ProjectionSettings.SourceMode.IMAGE) {
            renderImagePreview(graphics, portable, x, y, width, height);
        } else {
            renderBannerPreview(graphics, portable.bannerSnapshot(0), x, y, width, height);
        }
    }

    private void renderImagePreview(GuiGraphics graphics, MirageProjectorBlockEntity portable, int x, int y, int width, int height) {
        var image = portable.imageSourceBank().get(0);
        if (!image.present()) return;
        ProjectionTextureCache.get(image.id()).ifPresent(texture -> {
            float scale = Math.min(width / (float) image.width(), height / (float) image.height());
            float drawWidth = image.width() * scale;
            float drawHeight = image.height() * scale;
            graphics.flush();
            graphics.pose().pushPose();
            graphics.pose().translate(x + (width - drawWidth) * 0.5F, y + (height - drawHeight) * 0.5F, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.blit(texture, 0, 0, image.width(), image.height(), 0.0F, 0.0F,
                    image.width(), image.height(), image.width(), image.height());
            graphics.pose().popPose();
            graphics.flush();
        });
    }

    private void renderBannerPreview(GuiGraphics graphics, ItemStack banner, int x, int y, int width, int height) {
        if (banner.isEmpty()) return;
        graphics.pose().pushPose();
        graphics.pose().translate(x + width / 2.0F - 40.0F, y + height / 2.0F - 40.0F, 0.0F);
        graphics.pose().scale(5.0F, 5.0F, 1.0F);
        graphics.renderItem(banner, 0, 0);
        graphics.pose().popPose();
    }

    private int currentScale(ItemStack device) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !(device.getItem() instanceof MirageHandProjectorItem)) {
            return ProjectionSettings.DEFAULT.scalePixels();
        }
        return MirageHandProjectorItem.portableScalePixels(device, minecraft.level);
    }

    private ProjectionSettings portableSettings(ItemStack device) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return ProjectionSettings.DEFAULT;
        MirageProjectorBlockEntity portable = MirageHandProjectorItem.createPortableProjector(
                device, minecraft.level, BlockPos.ZERO, Direction.SOUTH);
        return portable == null ? ProjectionSettings.DEFAULT : portable.settings();
    }

    private int currentEntityRotation(ItemStack device) {
        float degrees = portableSettings(device).rotationOffsetDegrees();
        return Math.floorMod(Math.round(degrees), 360);
    }

    private static String imageFaceModeKey(ProjectionSettings.BackFaceMode mode) {
        return switch (mode) {
            case MIRRORED -> "gui.mirage_projector.image.back_mode.mirrored";
            case READABLE -> "gui.mirage_projector.image.back_mode.readable";
            default -> "gui.mirage_projector.image.back_mode.front";
        };
    }

    private int currentMaxScale(ItemStack device) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? MirageHandProjectorItem.PORTABLE_MIN_SCALE_PIXELS
                : MirageHandProjectorItem.portableMaxScalePixels(device, minecraft.level);
    }

    private int currentDistance(ItemStack device) {
        return device.getItem() instanceof MirageHandProjectorItem
                ? MirageHandProjectorItem.projectionDistancePixels(device)
                : MirageHandProjectorItem.PORTABLE_DEFAULT_DISTANCE_PIXELS;
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
        private int maxScale;
        private int scalePixels;
        private int lastSent;

        private ScaleSlider(int x, int y, int width, int height, int scalePixels, int maxScale, java.util.function.IntConsumer setter) {
            super(x, y, width, height, Component.empty(), 0.0D);
            this.setter = setter;
            this.maxScale = clampMax(maxScale);
            this.scalePixels = clamp(scalePixels);
            this.lastSent = this.scalePixels;
            this.value = normalize(this.scalePixels);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("gui.mirage_projector.portable_device.scale", scalePixels));
        }

        @Override
        protected void applyValue() {
            scalePixels = clamp(MirageHandProjectorItem.PORTABLE_MIN_SCALE_PIXELS
                    + (int)Math.round(value * (maxScale - MirageHandProjectorItem.PORTABLE_MIN_SCALE_PIXELS)));
            if (scalePixels != lastSent) {
                lastSent = scalePixels;
                setter.accept(scalePixels);
            }
            updateMessage();
        }

        private void syncExternal(int scalePixels, int maxScale) {
            this.maxScale = clampMax(maxScale);
            int safe = clamp(scalePixels);
            this.scalePixels = safe;
            this.lastSent = safe;
            this.value = normalize(safe);
            updateMessage();
        }

        private double normalize(int scalePixels) {
            int range = maxScale - MirageHandProjectorItem.PORTABLE_MIN_SCALE_PIXELS;
            return range <= 0 ? 0.0D : (clamp(scalePixels) - MirageHandProjectorItem.PORTABLE_MIN_SCALE_PIXELS) / (double) range;
        }

        private int clamp(int scalePixels) {
            return Math.max(MirageHandProjectorItem.PORTABLE_MIN_SCALE_PIXELS, Math.min(maxScale, scalePixels));
        }

        private static int clampMax(int maxScale) {
            return Math.max(MirageHandProjectorItem.PORTABLE_MIN_SCALE_PIXELS,
                    Math.min(MirageHandProjectorItem.PORTABLE_MAX_SCALE_PIXELS, maxScale));
        }
    }

    private static final class DistanceSlider extends AbstractSliderButton {
        private final java.util.function.IntConsumer setter;
        private int distancePixels;
        private int lastSent;

        private DistanceSlider(int x, int y, int width, int height, int distancePixels, java.util.function.IntConsumer setter) {
            super(x, y, width, height, Component.empty(), normalize(distancePixels));
            this.setter = setter;
            this.distancePixels = clamp(distancePixels);
            this.lastSent = this.distancePixels;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("gui.mirage_projector.portable_device.distance", distancePixels));
        }

        @Override
        protected void applyValue() {
            distancePixels = clamp(MirageHandProjectorItem.PORTABLE_MIN_DISTANCE_PIXELS
                    + (int) Math.round(value * (MirageHandProjectorItem.PORTABLE_MAX_DISTANCE_PIXELS
                    - MirageHandProjectorItem.PORTABLE_MIN_DISTANCE_PIXELS)));
            if (distancePixels != lastSent) {
                lastSent = distancePixels;
                setter.accept(distancePixels);
            }
            updateMessage();
        }

        private void syncExternal(int distancePixels) {
            int safe = clamp(distancePixels);
            if (safe == this.distancePixels) return;
            this.distancePixels = safe;
            this.lastSent = safe;
            this.value = normalize(safe);
            updateMessage();
        }

        private static double normalize(int distancePixels) {
            return (clamp(distancePixels) - MirageHandProjectorItem.PORTABLE_MIN_DISTANCE_PIXELS)
                    / (double) (MirageHandProjectorItem.PORTABLE_MAX_DISTANCE_PIXELS
                    - MirageHandProjectorItem.PORTABLE_MIN_DISTANCE_PIXELS);
        }

        private static int clamp(int distancePixels) {
            return Math.max(MirageHandProjectorItem.PORTABLE_MIN_DISTANCE_PIXELS,
                    Math.min(MirageHandProjectorItem.PORTABLE_MAX_DISTANCE_PIXELS, distancePixels));
        }
    }

    private static final class EntityRotationSlider extends AbstractSliderButton {
        private final java.util.function.IntConsumer setter;
        private int degrees;
        private int lastSent;

        private EntityRotationSlider(int x, int y, int width, int height, int degrees, java.util.function.IntConsumer setter) {
            super(x, y, width, height, Component.empty(), normalize(degrees));
            this.setter = setter;
            this.degrees = clamp(degrees);
            this.lastSent = this.degrees;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("gui.mirage_projector.portable_device.entity_rotation", degrees));
        }

        @Override
        protected void applyValue() {
            degrees = clamp((int) Math.round(value * 360.0D));
            if (degrees != lastSent) {
                lastSent = degrees;
                setter.accept(degrees);
            }
            updateMessage();
        }

        private void syncExternal(int degrees) {
            int safe = clamp(degrees);
            if (safe == this.degrees) return;
            this.degrees = safe;
            this.lastSent = safe;
            this.value = normalize(safe);
            updateMessage();
        }

        private static double normalize(int degrees) {
            return clamp(degrees) / 360.0D;
        }

        private static int clamp(int degrees) {
            return Math.max(0, Math.min(360, degrees));
        }
    }

    private static final class WarBannerSizeSlider extends AbstractSliderButton {
        private final java.util.function.IntConsumer setter;
        private int percent;
        private int lastSent;

        private WarBannerSizeSlider(
                int x,
                int y,
                int width,
                int height,
                int percent,
                java.util.function.IntConsumer setter
        ) {
            super(x, y, width, height, Component.empty(), normalize(percent));
            this.setter = setter;
            this.percent = clamp(percent);
            this.lastSent = this.percent;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("gui.mirage_projector.portable_device.war_banner_size_value", percent));
        }

        @Override
        protected void applyValue() {
            percent = clamp((int) Math.round(value * 100.0D));
            if (percent != lastSent) {
                lastSent = percent;
                setter.accept(percent);
            }
            updateMessage();
        }

        private void syncExternal(int percent) {
            int safe = clamp(percent);
            if (safe == this.percent) {
                return;
            }
            this.percent = safe;
            this.lastSent = safe;
            this.value = normalize(safe);
            updateMessage();
        }

        private static double normalize(int percent) {
            return clamp(percent) / 100.0D;
        }

        private static int clamp(int percent) {
            return Math.max(0, Math.min(100, percent));
        }
    }

    private static final class WarBannerHeightSlider extends AbstractSliderButton {
        private final java.util.function.IntConsumer setter;
        private int pixels;
        private int lastSent;

        private WarBannerHeightSlider(
                int x,
                int y,
                int width,
                int height,
                int pixels,
                java.util.function.IntConsumer setter
        ) {
            super(x, y, width, height, Component.empty(), normalize(pixels));
            this.setter = setter;
            this.pixels = clamp(pixels);
            this.lastSent = this.pixels;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("gui.mirage_projector.portable_device.war_banner_height_value", pixels));
        }

        @Override
        protected void applyValue() {
            pixels = clamp(-32 + (int) Math.round(value * 64.0D));
            if (pixels != lastSent) {
                lastSent = pixels;
                setter.accept(pixels);
            }
            updateMessage();
        }

        private void syncExternal(int pixels) {
            int safe = clamp(pixels);
            if (safe == this.pixels) {
                return;
            }
            this.pixels = safe;
            this.lastSent = safe;
            this.value = normalize(safe);
            updateMessage();
        }

        private static double normalize(int pixels) {
            return (clamp(pixels) + 32) / 64.0D;
        }

        private static int clamp(int pixels) {
            return Math.max(-32, Math.min(32, pixels));
        }
    }
}
