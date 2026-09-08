package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.menu.MirageProjectorMenu;
import celerbi.mirageprojector.network.UpdateProjectorPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.IntConsumer;

public final class MirageProjectorScreen extends AbstractContainerScreen<MirageProjectorMenu> {
    private String imageId;
    private int importedWidth;
    private int importedHeight;
    private String backImageId;
    private int backImportedWidth;
    private int backImportedHeight;
    private ProjectionSettings.SourceMode sourceMode;
    private int scalePixels;
    private int liftPixels;
    private boolean rotationEnabled;
    private int rotationPeriodTicks;
    private boolean clockwise;
    private float rotationOffsetDegrees;
    private boolean floatingEnabled;
    private ProjectionSettings.FloatMode floatMode;
    private int floatAmplitudePixels;
    private int floatCycleTicks;
    private int floatIntervalDegrees;
    private ProjectionSettings.BackFaceMode backFaceMode;
    private boolean flipVertical;
    private boolean debugChassisOverride;

    private Button frontImportButton;
    private Button backImportButton;
    private Button clearFrontButton;
    private Button clearBackButton;
    private Button sourceButton;
    private Button rotationButton;
    private Button directionButton;
    private Button floatingButton;
    private Button floatModeButton;
    private Button backFaceButton;
    private Button flipButton;
    private Button debugButton;
    private Component status = Component.translatable("gui.mirage_projector.status.ready");
    private ProjectionClearance.Result clearance = ProjectionClearance.Result.UNKNOWN;
    private long lastClearanceTick = Long.MIN_VALUE;

    public MirageProjectorScreen(MirageProjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 416;
        imageHeight = 466;

        ProjectionSettings s = menu.initialSettings();
        imageId = s.imageId();
        importedWidth = s.imageWidth();
        importedHeight = s.imageHeight();
        backImageId = s.backImageId();
        backImportedWidth = s.backImageWidth();
        backImportedHeight = s.backImageHeight();
        sourceMode = s.sourceMode();
        scalePixels = s.scalePixels();
        liftPixels = s.liftPixels();
        rotationEnabled = s.rotationEnabled();
        rotationPeriodTicks = s.rotationPeriodTicks();
        clockwise = s.clockwise();
        rotationOffsetDegrees = s.rotationOffsetDegrees();
        floatingEnabled = s.floatingEnabled();
        floatMode = s.floatMode();
        floatAmplitudePixels = s.floatAmplitudePixels();
        floatCycleTicks = s.floatCycleTicks();
        floatIntervalDegrees = s.floatIntervalDegrees();
        backFaceMode = s.backFaceMode();
        flipVertical = s.flipVertical();
        debugChassisOverride = s.debugChassisOverride();
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;
        int wide = imageWidth - 24;
        int half = (wide - 8) / 2;

        frontImportButton = addRenderableWidget(Button.builder(
                imageId.isBlank()
                        ? Component.translatable("gui.mirage_projector.import_front")
                        : Component.translatable("gui.mirage_projector.replace_front"),
                button -> openFilePicker(FaceTarget.FRONT)
        ).bounds(x + 12, y + 27, half - 24, 20).build());
        clearFrontButton = addRenderableWidget(Button.builder(Component.literal("×"), button -> {
            imageId = "";
            importedWidth = 0;
            importedHeight = 0;
            frontImportButton.setMessage(Component.translatable("gui.mirage_projector.import_front"));
            status = Component.literal("Front image cleared");
            updateClearance(true);
            refreshClearButtons();
        }).bounds(x + 12 + half - 20, y + 27, 20, 20).build());

        backImportButton = addRenderableWidget(Button.builder(
                backImageId.isBlank()
                        ? Component.translatable("gui.mirage_projector.import_back")
                        : Component.translatable("gui.mirage_projector.replace_back"),
                button -> openFilePicker(FaceTarget.BACK)
        ).bounds(x + 20 + half, y + 27, half - 24, 20).build());
        clearBackButton = addRenderableWidget(Button.builder(Component.literal("×"), button -> {
            backImageId = "";
            backImportedWidth = 0;
            backImportedHeight = 0;
            backImportButton.setMessage(Component.translatable("gui.mirage_projector.import_back"));
            status = Component.literal("Back image cleared");
            updateClearance(true);
            refreshClearButtons();
        }).bounds(x + 20 + half + half - 20, y + 27, 20, 20).build());

        IntSlider scaleSlider = addRenderableWidget(new IntSlider(
                x + 12, y + 54, half, 20,
                ProjectionSettings.DEBUG_MIN_SCALE_PIXELS,
                ProjectionSettings.DEBUG_MAX_SCALE_PIXELS,
                scalePixels,
                value -> scalePixels = value,
                value -> "Scale: " + value + " px"
        ));
        scaleSlider.setTooltip(Tooltip.create(Component.literal(
                "Largest projection dimension. 16 px = 1 Minecraft block; effective maximum is Core + chassis limited."
        )));

        IntSlider liftSlider = addRenderableWidget(new IntSlider(
                x + 20 + half, y + 54, half, 20,
                0,
                ProjectionSettings.DEBUG_MAX_LIFT_PIXELS,
                liftPixels,
                value -> liftPixels = value,
                value -> "Lift: " + value + " px"
        ));
        liftSlider.setTooltip(Tooltip.create(Component.literal(
                "Vertical distance from the pedestal top to the Mirage base. Its effective limit comes from the Core and chassis."
        )));

        IntSlider rotationSlider = addRenderableWidget(new IntSlider(
                x + 12, y + 80, half, 20,
                5,
                1200,
                rotationPeriodTicks,
                value -> rotationPeriodTicks = value,
                value -> String.format(Locale.ROOT, "360°: %.2fs", value / 20.0D)
        ));
        rotationSlider.setTooltip(Tooltip.create(Component.literal(
                "Time required for one complete 360° rotation."
        )));

        IntSlider floatSlider = addRenderableWidget(new IntSlider(
                x + 20 + half, y + 80, half, 20,
                0,
                ProjectionSettings.DEBUG_MAX_FLOAT_PIXELS,
                floatAmplitudePixels,
                value -> floatAmplitudePixels = value,
                value -> "Float: " + value + " px"
        ));
        floatSlider.setTooltip(Tooltip.create(Component.literal(
                "Vertical bob amplitude in Minecraft pixels. It cannot exceed Lift, the Core limit, or the chassis limit."
        )));

        IntSlider floatCycleSlider = addRenderableWidget(new IntSlider(
                x + 12, y + 106, half, 20,
                5,
                1200,
                floatCycleTicks,
                value -> floatCycleTicks = value,
                value -> String.format(Locale.ROOT, "Float cycle: %.2fs", value / 20.0D)
        ));
        floatCycleSlider.setTooltip(Tooltip.create(Component.literal(
                "Time for one complete down-and-return floating cycle while Float mode is Time."
        )));

        IntSlider floatLegSlider = addRenderableWidget(new IntSlider(
                x + 20 + half, y + 106, half, 20,
                1,
                360,
                floatIntervalDegrees,
                value -> floatIntervalDegrees = value,
                value -> "Float leg: " + value + "°"
        ));
        floatLegSlider.setTooltip(Tooltip.create(Component.literal(
                "Rotation-synced mode: degrees of rotation required to reach the next vertical endpoint before reversing direction."
        )));

        rotationButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            rotationEnabled = !rotationEnabled;
            refreshToggleLabels();
        }).bounds(x + 12, y + 138, half, 20).build());

        directionButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            clockwise = !clockwise;
            refreshToggleLabels();
        }).bounds(x + 20 + half, y + 138, half, 20).build());

        floatingButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            floatingEnabled = !floatingEnabled;
            refreshToggleLabels();
        }).bounds(x + 12, y + 164, half, 20).build());

        floatModeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            floatMode = floatMode == ProjectionSettings.FloatMode.TIME
                    ? ProjectionSettings.FloatMode.ROTATION_SYNCED
                    : ProjectionSettings.FloatMode.TIME;
            refreshToggleLabels();
        }).bounds(x + 20 + half, y + 164, half, 20).build());

        backFaceButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            backFaceMode = switch (backFaceMode) {
                case MIRRORED -> ProjectionSettings.BackFaceMode.READABLE;
                case READABLE -> ProjectionSettings.BackFaceMode.INDEPENDENT;
                case INDEPENDENT -> ProjectionSettings.BackFaceMode.MIRRORED;
            };
            refreshToggleLabels();
        }).bounds(x + 12, y + 190, half, 20).build());

        flipButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            flipVertical = !flipVertical;
            refreshToggleLabels();
        }).bounds(x + 20 + half, y + 190, half, 20).build());

        addRenderableWidget(Button.builder(Component.literal("+90°"), button -> {
            rotationOffsetDegrees = wrap(rotationOffsetDegrees + 90.0F);
            status = Component.literal("Orientation: " + Math.round(rotationOffsetDegrees) + "°");
        }).bounds(x + 12, y + 216, half, 20).build());

        addRenderableWidget(Button.builder(Component.literal("+180°"), button -> {
            rotationOffsetDegrees = wrap(rotationOffsetDegrees + 180.0F);
            status = Component.literal("Orientation: " + Math.round(rotationOffsetDegrees) + "°");
        }).bounds(x + 20 + half, y + 216, half, 20).build());

        sourceButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            sourceMode = sourceMode == ProjectionSettings.SourceMode.IMAGE
                    ? ProjectionSettings.SourceMode.ITEM
                    : ProjectionSettings.SourceMode.IMAGE;
            refreshToggleLabels();
            updateClearance(true);
        }).bounds(x + 12, y + 242, 230, 20).build());

        debugButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            if (minecraft != null && minecraft.player != null && minecraft.player.isCreative()) {
                debugChassisOverride = !debugChassisOverride;
                refreshToggleLabels();
                updateClearance(true);
            } else {
                status = Component.literal("Debug chassis override requires Creative Mode");
            }
        }).bounds(x + 250, y + 242, 118, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.apply"), button -> applyAndClose())
                .bounds(x + 12, y + 434, half, 22).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.cancel"), button -> onClose())
                .bounds(x + 20 + half, y + 434, half, 22).build());

        refreshToggleLabels();
        refreshClearButtons();
        updateClearance(true);
    }

    private void refreshClearButtons() {
        if (clearFrontButton != null) {
            clearFrontButton.active = !imageId.isBlank();
        }
        if (clearBackButton != null) {
            clearBackButton.active = !backImageId.isBlank();
        }
    }

    private void refreshToggleLabels() {
        if (rotationButton != null) {
            if (sourceButton != null) {
                sourceButton.setMessage(Component.literal("Projection source: "
                        + (sourceMode == ProjectionSettings.SourceMode.IMAGE ? "Image" : "Item")));
            }
            rotationButton.setMessage(Component.literal("Rotation: " + onOff(rotationEnabled)));
            directionButton.setMessage(Component.literal("Direction: " + (clockwise ? "Clockwise" : "Counter-clockwise")));
            floatingButton.setMessage(Component.literal("Floating: " + onOff(floatingEnabled)));
            floatModeButton.setMessage(Component.literal("Float mode: " + (floatMode == ProjectionSettings.FloatMode.TIME ? "Time" : "Rotation synced")));
            backFaceButton.setMessage(Component.literal("Back: " + switch (backFaceMode) {
                case MIRRORED -> "Mirrored";
                case READABLE -> "Readable";
                case INDEPENDENT -> "Independent";
            }));
            flipButton.setMessage(Component.literal("Vertical flip: " + onOff(flipVertical)));
            if (debugButton != null) {
                debugButton.setMessage(Component.literal("Debug chassis: " + onOff(debugChassisOverride)));
            }
        }
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    private static float wrap(float degrees) {
        float wrapped = degrees % 360.0F;
        return wrapped < 0.0F ? wrapped + 360.0F : wrapped;
    }

    private void openFilePicker(FaceTarget target) {
        status = Component.translatable("gui.mirage_projector.status.selecting");

        Path selected = chooseImageFile(target);
        if (selected == null) {
            status = Component.translatable("gui.mirage_projector.status.cancelled");
            return;
        }

        status = Component.translatable("gui.mirage_projector.status.processing");
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return ImageImporter.importFile(selected);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                })
                .whenComplete((imported, throwable) -> Minecraft.getInstance().execute(() -> {
                    if (throwable != null) {
                        Throwable cause = throwable.getCause() == null ? throwable : throwable.getCause();
                        String message = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
                        status = Component.literal("Import failed: " + message);
                        return;
                    }

                    ProjectionTextureCache.invalidate(imported.hash());
                    sourceMode = ProjectionSettings.SourceMode.IMAGE;
                    if (target == FaceTarget.FRONT) {
                        imageId = imported.hash();
                        importedWidth = imported.width();
                        importedHeight = imported.height();
                        if (frontImportButton != null) {
                            frontImportButton.setMessage(Component.translatable("gui.mirage_projector.replace_front"));
                        }
                    } else {
                        backImageId = imported.hash();
                        backImportedWidth = imported.width();
                        backImportedHeight = imported.height();
                        backFaceMode = ProjectionSettings.BackFaceMode.INDEPENDENT;
                        if (backImportButton != null) {
                            backImportButton.setMessage(Component.translatable("gui.mirage_projector.replace_back"));
                        }
                        refreshToggleLabels();
                    }

                    refreshClearButtons();
                    refreshToggleLabels();
                    updateClearance(true);
                    status = Component.literal((target == FaceTarget.FRONT ? "Front" : "Back")
                            + " imported " + imported.width() + "×" + imported.height()
                            + " · " + (imported.normalizedBytes() / 1024L) + " KiB");
                }));
    }

    private static Path chooseImageFile(FaceTarget target) {
        Minecraft minecraft = Minecraft.getInstance();
        String title = target == FaceTarget.FRONT
                ? "Import Mirage front image"
                : "Import Mirage back image";
        String startPath = System.getProperty("user.home", "");
        if (startPath.isBlank() && minecraft.gameDirectory != null) {
            startPath = minecraft.gameDirectory.getAbsolutePath();
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(3);
            filters.put(stack.UTF8("*.png"));
            filters.put(stack.UTF8("*.jpg"));
            filters.put(stack.UTF8("*.jpeg"));
            filters.flip();

            String path = TinyFileDialogs.tinyfd_openFileDialog(
                    title,
                    startPath,
                    filters,
                    "PNG / JPG / JPEG",
                    false
            );
            return path == null || path.isBlank() ? null : Path.of(path);
        } catch (Throwable throwable) {
            MirageProjector.LOGGER.error("Could not open the native image picker", throwable);
            return null;
        }
    }

    private ProjectionSettings buildSettings() {
        return new ProjectionSettings(
                imageId,
                importedWidth,
                importedHeight,
                backImageId,
                backImportedWidth,
                backImportedHeight,
                sourceMode,
                scalePixels,
                liftPixels,
                rotationEnabled,
                rotationPeriodTicks,
                clockwise,
                rotationOffsetDegrees,
                floatingEnabled,
                floatMode,
                floatAmplitudePixels,
                floatCycleTicks,
                floatIntervalDegrees,
                backFaceMode,
                flipVertical,
                debugChassisOverride
        ).sanitized();
    }

    private void applyAndClose() {
        if (!imageId.isBlank()) {
            ClientAssetTransport.uploadIfPresent(imageId);
        }
        if (!backImageId.isBlank()) {
            ClientAssetTransport.uploadIfPresent(backImageId);
        }
        PacketDistributor.sendToServer(new UpdateProjectorPayload(menu.projectorPos(), buildSettings()));
        onClose();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        graphics.fill(left, top, left + imageWidth, top + imageHeight, 0xEE11151A);
        graphics.fill(left + 1, top + 1, left + imageWidth - 1, top + 2, 0xFF764C92);
        graphics.fill(left + 1, top + imageHeight - 2, left + imageWidth - 1, top + imageHeight - 1, 0xFF443052);
        graphics.fill(left + 11, top + 270, left + imageWidth - 11, top + 336, 0x88000000);

        drawSlotFrame(graphics, left + MirageProjectorMenu.CORE_SLOT_X - 1, top + MirageProjectorMenu.CORE_SLOT_Y - 1);
        drawSlotFrame(graphics, left + MirageProjectorMenu.PROJECTED_ITEM_SLOT_X - 1, top + MirageProjectorMenu.PROJECTED_ITEM_SLOT_Y - 1);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotFrame(graphics, left + MirageProjectorMenu.PLAYER_INV_X + col * 18 - 1,
                        top + MirageProjectorMenu.PLAYER_INV_Y + row * 18 - 1);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotFrame(graphics, left + MirageProjectorMenu.PLAYER_INV_X + col * 18 - 1,
                    top + MirageProjectorMenu.PLAYER_INV_Y + 58 - 1);
        }
    }

    private static void drawSlotFrame(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF5A5361);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF171A20);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 12, 9, 0xFFF4F4F4, false);

        String frontSource = imageId.isBlank()
                ? "Front: no image — empty image state uses the vanilla book"
                : "Front: " + importedWidth + "×" + importedHeight + " · " + shortId(imageId);
        String backSource = backImageId.isBlank()
                ? "Back: no independent image (front face is the fallback)"
                : "Back: " + backImportedWidth + "×" + backImportedHeight + " · " + shortId(backImageId);

        graphics.drawString(font, frontSource, 12, 273, 0xFFBEB8C8, false);
        graphics.drawString(font, backSource, 12, 284, 0xFFBEB8C8, false);
        graphics.drawString(font, status, 12, 295, 0xFFE3D7FF, false);

        ProjectionCoreProfile core = menu.coreProfile();
        ProjectionPower.Status power = ProjectionPower.evaluate(buildSettings(), core, !menu.projectedItemStack().isEmpty());
        String coreText = "Core: " + core.displayName()
                + " · Scale≤" + core.maxScalePixels() + "px"
                + " Lift≤" + core.maxLiftPixels() + "px"
                + " Float≤" + core.maxFloatPixels() + "px";
        graphics.drawString(font, coreText, 12, 306, core.present() ? 0xFFBFD8FF : 0xFFFF8E8E, false);

        int barX = 12;
        int barY = 318;
        int barW = 138;
        graphics.fill(barX, barY, barX + barW, barY + 7, 0xFF242931);
        int fill = Math.round(barW * power.fillRatio());
        int fillColor = power.active() ? 0xFF8FD19A : 0xFFFF8A73;
        graphics.fill(barX, barY, barX + fill, barY + 7, fillColor);
        graphics.drawString(font, power.summary(), barX + barW + 8, 317, power.active() ? 0xFF8FD19A : 0xFFFFA87A, false);

        if (clearance.known()) {
            String clearanceText = clearance.clear()
                    ? "Clearance: clear"
                    : "Clearance: " + clearance.blockedBlocks() + " block(s) intersect the projection envelope";
            graphics.drawString(font, clearanceText, 12, 328, clearance.clear() ? 0xFF8FD19A : 0xFFFFA87A, false);
        }

        graphics.drawString(font, "Core", 350, MirageProjectorMenu.CORE_SLOT_Y + 5, 0xFFBEB8C8, false);
        graphics.drawString(font, "Item", 350, MirageProjectorMenu.PROJECTED_ITEM_SLOT_Y + 5, 0xFFBEB8C8, false);
        graphics.drawString(font, "Inventory", MirageProjectorMenu.PLAYER_INV_X, 339, 0xFFBEB8C8, false);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateClearance(false);
    }

    private void updateClearance(boolean force) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            clearance = ProjectionClearance.Result.UNKNOWN;
            return;
        }
        long tick = minecraft.level.getGameTime();
        if (!force && lastClearanceTick != Long.MIN_VALUE && tick - lastClearanceTick < 10) {
            return;
        }
        lastClearanceTick = tick;
        boolean hasProjectedItem = !menu.projectedItemStack().isEmpty();
        clearance = ProjectionClearance.scan(minecraft.level, menu.projectorPos(), buildSettings(), hasProjectedItem);
    }

    private static String shortId(String id) {
        return id.substring(0, Math.min(12, id.length())) + "…";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private enum FaceTarget {
        FRONT,
        BACK
    }

    private static final class IntSlider extends AbstractSliderButton {
        private final int min;
        private final int max;
        private final IntConsumer setter;
        private final Function<Integer, String> formatter;
        private int current;

        private IntSlider(
                int x,
                int y,
                int width,
                int height,
                int min,
                int max,
                int current,
                IntConsumer setter,
                Function<Integer, String> formatter
        ) {
            super(x, y, width, height, Component.empty(), normalize(min, max, current));
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.formatter = formatter;
            this.current = clamp(current, min, max);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(formatter == null ? Integer.toString(current) : formatter.apply(current)));
        }

        @Override
        protected void applyValue() {
            current = min + (int) Math.round(value * (max - min));
            current = clamp(current, min, max);
            setter.accept(current);
            updateMessage();
        }

        private static double normalize(int min, int max, int value) {
            if (max <= min) {
                return 0.0D;
            }
            int clamped = clamp(value, min, max);
            return (clamped - min) / (double) (max - min);
        }

        private static int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
