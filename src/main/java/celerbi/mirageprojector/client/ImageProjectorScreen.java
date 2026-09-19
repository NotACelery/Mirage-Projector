package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ImageSourceBank;
import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.menu.ImageProjectorMenu;
import celerbi.mirageprojector.network.OpenImageWorkspacePayload;
import celerbi.mirageprojector.network.OpenProjectorWorkspacePayload;
import celerbi.mirageprojector.network.UpdateImageWorkspacePayload;
import celerbi.mirageprojector.network.SetProjectionSourcePayload;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ImageProjectorScreen extends ResponsiveContainerScreen<ImageProjectorMenu> {
    private String frontId;
    private int frontWidth;
    private int frontHeight;
    private String backId;
    private int backWidth;
    private int backHeight;
    private String eastId;
    private int eastWidth;
    private int eastHeight;
    private String westId;
    private int westWidth;
    private int westHeight;
    private final ImageSourceBank sourceBank;
    private int selectedBankSlot;
    private int wallSlideIndex;
    private ProjectionSettings.ImageLayoutMode imageLayoutMode;
    private ProjectionSettings.BackFaceMode backFaceMode;
    private boolean flipVertical;
    private boolean scanlines;
    private boolean automaticPresentationEnabled;
    private int automaticPresentationIntervalSeconds;
    private Component status = Component.translatable("gui.mirage_projector.status.ready");

    private Button workspaceModeButton;
    private Button workspaceBackButton;
    private Button layoutModeButton;
    private Button frontButton;
    private Button backButton;
    private Button eastButton;
    private Button westButton;
    private Button backModeButton;
    private Button sameAllButton;
    private Button flipButton;
    private Button scanlinesButton;
    private Button bankImportButton;
    private Button bankClearButton;
    private Button bankCopyButton;
    private Button wallMoveUpButton;
    private Button wallMoveDownButton;
    private Button wallSetCurrentButton;
    private Button wallPreviousButton;
    private Button wallNextButton;
    private Button automaticPresentationButton;
    private SecondsSlider automaticPresentationSlider;
    private boolean modeActivated;

    private int mapX;
    private int mapY;
    private int mapCell;
    private int mapWidth;
    private int mapHeight;

    public ImageProjectorScreen(ImageProjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        if (menu.supportsMultiSourceLayout() || menu.isPresentationDeck()) {
            imageWidth = 520;
            imageHeight = 438;
        } else {
            imageWidth = 416;
            imageHeight = 314;
        }
        ProjectionSettings s = menu.initialSettings();
        frontId = s.imageId();
        frontWidth = s.imageWidth();
        frontHeight = s.imageHeight();
        backId = s.backImageId();
        backWidth = s.backImageWidth();
        backHeight = s.backImageHeight();
        eastId = s.eastImageId();
        eastWidth = s.eastImageWidth();
        eastHeight = s.eastImageHeight();
        westId = s.westImageId();
        westWidth = s.westImageWidth();
        westHeight = s.westImageHeight();
        sourceBank = menu.initialImageBank();
        wallSlideIndex = sourceBank.normalizePresentIndex(menu.initialWallSlideIndex());
        if (wallSlideIndex < 0) {
            wallSlideIndex = 0;
        }
        selectedBankSlot = wallSlideIndex;
        imageLayoutMode = menu.supportsMultiSourceLayout() ? s.imageLayoutMode() : ProjectionSettings.ImageLayoutMode.SINGLE;
        backFaceMode = s.backFaceMode();
        if (imageLayoutMode == ProjectionSettings.ImageLayoutMode.MULTI
                && backFaceMode != ProjectionSettings.BackFaceMode.FRONT
                && backFaceMode != ProjectionSettings.BackFaceMode.MIRRORED
                && backFaceMode != ProjectionSettings.BackFaceMode.READABLE) {

            backFaceMode = ProjectionSettings.BackFaceMode.FRONT;
        }
        flipVertical = s.flipVertical();
        scanlines = s.scanlines();
        automaticPresentationEnabled = menu.initialAutomaticPresentationEnabled();
        automaticPresentationIntervalSeconds = menu.initialAutomaticPresentationIntervalSeconds();
    }

    @Override
    protected void init() {
        super.init();
        workspaceModeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            apply(false);
            PacketDistributor.sendToServer(new SetProjectionSourcePayload(menu.projectorPos(), ProjectionSettings.SourceMode.IMAGE));
            // Reopening this same container recenters Minecraft's cursor. Activating the mode
            // only needs a server state update; keep the player in the current workspace.
            modeActivated = true;
            button.active = false;
            button.setMessage(Component.translatable("gui.mirage_projector.workspace.mode_active"));
        }).bounds(leftPos + imageWidth - 274, topPos + 34, 130, 18).build());

        workspaceBackButton = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.workspace.back"), button -> {
            apply(false);
            PacketDistributor.sendToServer(new OpenProjectorWorkspacePayload(menu.projectorPos()));
        }).bounds(leftPos + imageWidth - 138, topPos + 34, 126, 18).build());

        if (!menu.isPresentationDeck() && menu.supportsMultiSourceLayout()) {
            layoutModeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                ProjectionSettings.ImageLayoutMode next = imageLayoutMode == ProjectionSettings.ImageLayoutMode.SINGLE
                        ? ProjectionSettings.ImageLayoutMode.MULTI
                        : ProjectionSettings.ImageLayoutMode.SINGLE;

                if (next == ProjectionSettings.ImageLayoutMode.MULTI
                        && !sourceBank.hasAny(menu.imageLayoutSlots())
                        && frontId != null && !frontId.isBlank() && frontWidth > 0 && frontHeight > 0) {
                    sourceBank.set(0, frontId, frontWidth, frontHeight);
                } else if (next == ProjectionSettings.ImageLayoutMode.SINGLE
                        && (frontId == null || frontId.isBlank() || frontWidth <= 0 || frontHeight <= 0)) {
                    ImageSourceBank.Asset first = sourceBank.get(0);
                    if (first.present()) {
                        frontId = first.id();
                        frontWidth = first.width();
                        frontHeight = first.height();
                    }
                }

                imageLayoutMode = next;
                if (imageLayoutMode == ProjectionSettings.ImageLayoutMode.MULTI
                        && backFaceMode != ProjectionSettings.BackFaceMode.FRONT
                        && backFaceMode != ProjectionSettings.BackFaceMode.MIRRORED
                        && backFaceMode != ProjectionSettings.BackFaceMode.READABLE) {
                    backFaceMode = ProjectionSettings.BackFaceMode.FRONT;
                }
                apply(false);
                PacketDistributor.sendToServer(new OpenImageWorkspacePayload(menu.projectorPos()));
            }).bounds(leftPos + 18, topPos + 58, 210, 20).build());
            layoutModeButton.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.image.layout_mode")));
        }

        if (menu.isPresentationDeck()) {
            initWallPresentation();
        } else if (isMultiSource()) {
            initMultiSource();
        } else {
            initFaceWorkspace();
        }
        refreshLabels();
        refreshModeButton();
    }

    private void initWallPresentation() {
        mapCell = 50;
        mapWidth = mapCell * 3;
        mapHeight = mapCell * 3;
        mapX = leftPos + 24;
        mapY = topPos + 104;

        bankImportButton = addRenderableWidget(Button.builder(Component.empty(), button -> openBankFilePicker(selectedBankSlot))
                .bounds(leftPos + 216, topPos + 258, 164, 20).build());
        bankClearButton = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.image.bank.clear"), button -> {
            boolean clearingCurrent = selectedBankSlot == wallSlideIndex;
            sourceBank.clear(selectedBankSlot);
            if (clearingCurrent) {
                int normalized = sourceBank.normalizePresentIndex(wallSlideIndex);
                wallSlideIndex = normalized < 0 ? 0 : normalized;
            }
            syncWallFrontAsset();
            status = Component.translatable("gui.mirage_projector.image.bank.cleared", selectedBankSlot + 1);
            refreshLabels();
        }).bounds(leftPos + 388, topPos + 258, 104, 20).build());

        wallMoveUpButton = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.wall.move_up"), button -> moveSelectedWallSlide(-1))
                .bounds(leftPos + 24, topPos + 274, 78, 20).build());
        wallMoveDownButton = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.wall.move_down"), button -> moveSelectedWallSlide(1))
                .bounds(leftPos + 108, topPos + 274, 78, 20).build());
        wallSetCurrentButton = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.wall.set_current"), button -> {
            if (sourceBank.get(selectedBankSlot).present()) {
                wallSlideIndex = selectedBankSlot;
                syncWallFrontAsset();
                apply(false);
                status = Component.translatable("gui.mirage_projector.wall.current_slide", wallSlideIndex + 1);
                refreshLabels();
            }
        }).bounds(leftPos + 216, topPos + 286, 164, 20).build());

        wallPreviousButton = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.wall.previous"), button -> stepWallSlide(-1))
                .bounds(leftPos + 216, topPos + 314, 80, 20).build());
        wallNextButton = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.wall.next"), button -> stepWallSlide(1))
                .bounds(leftPos + 300, topPos + 314, 80, 20).build());

        flipButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            flipVertical = !flipVertical;
            refreshLabels();
        }).bounds(leftPos + 388, topPos + 286, 104, 20).build());
        scanlinesButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            scanlines = !scanlines;
            refreshLabels();
        }).bounds(leftPos + 388, topPos + 314, 104, 20).build());

        automaticPresentationButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            automaticPresentationEnabled = !automaticPresentationEnabled;
            refreshLabels();
        }).bounds(leftPos + 216, topPos + 342, 164, 20).build());
        automaticPresentationSlider = addRenderableWidget(new SecondsSlider(
                leftPos + 388, topPos + 342, 104, 20,
                automaticPresentationIntervalSeconds, seconds -> automaticPresentationIntervalSeconds = seconds
        ));
        automaticPresentationSlider.setTooltip(Tooltip.create(Component.translatable(
                "tooltip.mirage_projector.presentation.interval"
        )));

        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.apply"), button -> apply(true))
                .bounds(leftPos + 24, topPos + 382, 226, 22).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.cancel"), button ->
                PacketDistributor.sendToServer(new OpenProjectorWorkspacePayload(menu.projectorPos()))
        ).bounds(leftPos + 270, topPos + 382, 222, 22).build());
    }

    private void moveSelectedWallSlide(int delta) {
        int target = selectedBankSlot + delta;
        if (target < 0 || target >= ImageSourceBank.PERSISTED_COMPAT_SLOTS) {
            return;
        }
        sourceBank.swap(selectedBankSlot, target);
        if (wallSlideIndex == selectedBankSlot) {
            wallSlideIndex = target;
        } else if (wallSlideIndex == target) {
            wallSlideIndex = selectedBankSlot;
        }
        selectedBankSlot = target;
        syncWallFrontAsset();
        status = Component.translatable("gui.mirage_projector.wall.reordered", selectedBankSlot + 1);
        refreshLabels();
    }

    private void stepWallSlide(int direction) {
        int next = sourceBank.nextPresentIndex(wallSlideIndex, direction);
        if (next < 0) {
            status = Component.translatable("gui.mirage_projector.wall.no_slides");
            return;
        }
        wallSlideIndex = next;
        selectedBankSlot = next;
        syncWallFrontAsset();
        apply(false);
        status = Component.translatable("gui.mirage_projector.wall.current_slide", wallSlideIndex + 1);
        refreshLabels();
    }

    private void syncWallFrontAsset() {
        int normalized = sourceBank.normalizePresentIndex(wallSlideIndex);
        if (normalized >= 0) {
            wallSlideIndex = normalized;
            ImageSourceBank.Asset active = sourceBank.get(normalized);
            frontId = active.id();
            frontWidth = active.width();
            frontHeight = active.height();
        } else {
            frontId = "";
            frontWidth = 0;
            frontHeight = 0;
        }
        backId = ""; backWidth = 0; backHeight = 0;
        eastId = ""; eastWidth = 0; eastHeight = 0;
        westId = ""; westWidth = 0; westHeight = 0;
        imageLayoutMode = ProjectionSettings.ImageLayoutMode.SINGLE;
        backFaceMode = ProjectionSettings.BackFaceMode.FRONT;
    }

    private void initFaceWorkspace() {
        boolean prism = isPrism();
        int ox = faceOffsetX();
        int oy = faceOffsetY();
        if (prism) {
            frontButton = addFaceButton(Face.NORTH, 18 + ox, oy);
            eastButton = addFaceButton(Face.EAST, 115 + ox, oy);
            backButton = addFaceButton(Face.SOUTH, 212 + ox, oy);
            westButton = addFaceButton(Face.WEST, 309 + ox, oy);
            addClearButton(Face.NORTH, 88 + ox, oy);
            addClearButton(Face.EAST, 185 + ox, oy);
            addClearButton(Face.SOUTH, 282 + ox, oy);
            addClearButton(Face.WEST, 379 + ox, oy);
        } else {
            frontButton = addRenderableWidget(Button.builder(frontImportLabel(), button -> openFilePicker(Face.NORTH))
                    .bounds(leftPos + 18 + ox, topPos + 178 + oy, 156, 20).build());
            addRenderableWidget(Button.builder(Component.literal("×"), button -> clearFace(Face.NORTH))
                    .bounds(leftPos + 178 + ox, topPos + 178 + oy, 20, 20).build());
            backButton = addRenderableWidget(Button.builder(backImportLabel(), button -> openFilePicker(Face.SOUTH))
                    .bounds(leftPos + 218 + ox, topPos + 178 + oy, 156, 20).build());
            addRenderableWidget(Button.builder(Component.literal("×"), button -> clearFace(Face.SOUTH))
                    .bounds(leftPos + 378 + ox, topPos + 178 + oy, 20, 20).build());
        }

        backModeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            backFaceMode = switch (backFaceMode) {
                case FRONT -> ProjectionSettings.BackFaceMode.BACK;
                case BACK -> ProjectionSettings.BackFaceMode.MIRRORED;
                case MIRRORED -> ProjectionSettings.BackFaceMode.READABLE;
                case READABLE -> ProjectionSettings.BackFaceMode.INDEPENDENT;
                case INDEPENDENT -> ProjectionSettings.BackFaceMode.FRONT;
            };
            refreshLabels();
        }).bounds(leftPos + 18 + ox, topPos + 208 + oy, 180, 20).build());
        backModeButton.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.image.back_mode")));
        backModeButton.visible = !prism;

        sameAllButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.image.prism.same_all"),
                button -> copyNorthToAllFaces()
        ).bounds(leftPos + 18 + ox, topPos + 208 + oy, 180, 20).build());
        sameAllButton.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.image.prism.same_all")));
        sameAllButton.visible = prism;

        flipButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            flipVertical = !flipVertical;
            refreshLabels();
        }).bounds(leftPos + 218 + ox, topPos + 208 + oy, 86, 20).build());
        scanlinesButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            scanlines = !scanlines;
            refreshLabels();
        }).bounds(leftPos + 312 + ox, topPos + 208 + oy, 86, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.apply"), button -> apply(true))
                .bounds(leftPos + 18 + ox, topPos + 252 + oy, 180, 22).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.cancel"), button ->
                PacketDistributor.sendToServer(new OpenProjectorWorkspacePayload(menu.projectorPos()))
        ).bounds(leftPos + 218 + ox, topPos + 252 + oy, 180, 22).build());
    }

    private void initMultiSource() {
        int columns = menu.imageLayoutColumns();
        int rows = menu.imageLayoutRows();
        mapCell = Math.max(24, Math.min(47, Math.min(188 / columns, 188 / rows)));
        mapWidth = mapCell * columns;
        mapHeight = mapCell * rows;
        mapX = leftPos + 22 + (190 - mapWidth) / 2;
        mapY = topPos + 102 + (190 - mapHeight) / 2;

        bankImportButton = addRenderableWidget(Button.builder(Component.empty(), button -> openBankFilePicker(selectedBankSlot))
                .bounds(leftPos + 236, topPos + 266, 176, 20).build());
        bankClearButton = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.image.bank.clear"), button -> {
            sourceBank.clear(selectedBankSlot);
            status = Component.translatable("gui.mirage_projector.image.bank.cleared", selectedBankSlot + 1);
            refreshLabels();
        }).bounds(leftPos + 420, topPos + 266, 78, 20).build());
        bankCopyButton = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.image.bank.copy_empty"), button -> {
            if (!sourceBank.get(selectedBankSlot).present()) {
                status = Component.translatable("gui.mirage_projector.image.bank.source_required");
                return;
            }
            sourceBank.copySlotToEmpty(selectedBankSlot, menu.imageLayoutSlots());
            status = Component.translatable("gui.mirage_projector.image.bank.copied_empty");
            refreshLabels();
        }).bounds(leftPos + 22, topPos + 342, 190, 20).build());

        backModeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            backFaceMode = switch (backFaceMode) {
                case FRONT -> ProjectionSettings.BackFaceMode.MIRRORED;
                case MIRRORED -> ProjectionSettings.BackFaceMode.READABLE;
                case READABLE -> ProjectionSettings.BackFaceMode.FRONT;
                case BACK, INDEPENDENT -> ProjectionSettings.BackFaceMode.FRONT;
            };
            refreshLabels();
        }).bounds(leftPos + 236, topPos + 342, 262, 20).build());
        backModeButton.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.image.bank.back_mode")));
        flipButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            flipVertical = !flipVertical;
            refreshLabels();
        }).bounds(leftPos + 236, topPos + 328, 127, 20).build());
        scanlinesButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            scanlines = !scanlines;
            refreshLabels();
        }).bounds(leftPos + 371, topPos + 328, 127, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.apply"), button -> apply(true))
                .bounds(leftPos + 22, topPos + 380, 230, 22).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.cancel"), button ->
                PacketDistributor.sendToServer(new OpenProjectorWorkspacePayload(menu.projectorPos()))
        ).bounds(leftPos + 268, topPos + 380, 230, 22).build());
    }

    private Button addFaceButton(Face face, int localX, int localYShift) {
        return addRenderableWidget(Button.builder(faceImportLabel(face), button -> openFilePicker(face))
                .bounds(leftPos + localX, topPos + 164 + localYShift, 68, 20).build());
    }

    private void addClearButton(Face face, int localX, int localYShift) {
        addRenderableWidget(Button.builder(Component.literal("×"), button -> clearFace(face))
                .bounds(leftPos + localX, topPos + 164 + localYShift, 18, 20).build());
    }

    private int faceOffsetX() {
        return menu.supportsMultiSourceLayout() ? (imageWidth - 416) / 2 : 0;
    }

    private int faceOffsetY() {
        return (menu.supportsMultiSourceLayout() ? 30 : 0) + 28;
    }

    private boolean isPrism() {
        return menu.physicalFaceCount() == 4;
    }

    private boolean isMultiSource() {
        return menu.supportsMultiSourceLayout()
                && imageLayoutMode == ProjectionSettings.ImageLayoutMode.MULTI;
    }

    private void refreshLabels() {
        if (layoutModeButton != null) {
            layoutModeButton.setMessage(Component.translatable("gui.mirage_projector.image.layout_mode", Component.translatable(imageLayoutMode == ProjectionSettings.ImageLayoutMode.MULTI ? "gui.mirage_projector.image.layout.multi" : "gui.mirage_projector.image.layout.single")));
        }
        if (frontButton != null) {
            frontButton.setMessage(isPrism() ? faceImportLabel(Face.NORTH) : frontImportLabel());
        }
        if (eastButton != null) {
            eastButton.setMessage(faceImportLabel(Face.EAST));
        }
        if (backButton != null) {
            backButton.setMessage(isPrism() ? faceImportLabel(Face.SOUTH) : backImportLabel());
        }
        if (westButton != null) {
            westButton.setMessage(faceImportLabel(Face.WEST));
        }
        if (backModeButton != null) {
            Component mode = isMultiSource()
                    ? Component.translatable("gui.mirage_projector.image.bank.back_mode", multiBackModeName())
                    : Component.translatable("gui.mirage_projector.image.back_mode", backModeName());
            backModeButton.setMessage(mode);
        }
        if (flipButton != null) {
            flipButton.setMessage(Component.translatable("gui.mirage_projector.image.flip", onOff(flipVertical)));
        }
        if (scanlinesButton != null) {
            scanlinesButton.setMessage(Component.translatable("gui.mirage_projector.image.scanlines", onOff(scanlines)));
        }
        if (bankImportButton != null) {
            bankImportButton.setMessage(Component.translatable(
                    sourceBank.get(selectedBankSlot).present()
                            ? "gui.mirage_projector.image.bank.replace"
                            : "gui.mirage_projector.image.bank.import",
                    selectedBankSlot + 1
            ));
        }
        if (bankClearButton != null) {
            bankClearButton.active = sourceBank.get(selectedBankSlot).present();
        }
        if (bankCopyButton != null) {
            bankCopyButton.active = sourceBank.get(selectedBankSlot).present();
        }
        if (wallMoveUpButton != null) {
            wallMoveUpButton.active = selectedBankSlot > 0;
        }
        if (wallMoveDownButton != null) {
            wallMoveDownButton.active = selectedBankSlot < ImageSourceBank.PERSISTED_COMPAT_SLOTS - 1;
        }
        if (wallSetCurrentButton != null) {
            wallSetCurrentButton.active = sourceBank.get(selectedBankSlot).present() && selectedBankSlot != wallSlideIndex;
        }
        boolean hasWallSlides = sourceBank.hasAny(ImageSourceBank.PERSISTED_COMPAT_SLOTS);
        if (wallPreviousButton != null) wallPreviousButton.active = hasWallSlides;
        if (wallNextButton != null) wallNextButton.active = hasWallSlides;
        if (automaticPresentationButton != null) {
            automaticPresentationButton.setMessage(Component.translatable(
                    "gui.mirage_projector.presentation.automatic", onOff(automaticPresentationEnabled)
            ));
            automaticPresentationButton.active = hasWallSlides;
        }
        if (automaticPresentationSlider != null) {
            automaticPresentationSlider.active = hasWallSlides && automaticPresentationEnabled;
        }
    }

    private Component frontImportLabel() {
        return Component.translatable(frontId.isBlank() ? "gui.mirage_projector.import_front" : "gui.mirage_projector.replace_front");
    }

    private Component backImportLabel() {
        return Component.translatable(backId.isBlank() ? "gui.mirage_projector.import_back" : "gui.mirage_projector.replace_back");
    }

    private Component faceImportLabel(Face face) {
        return Component.translatable(faceAssetId(face).isBlank()
                ? "gui.mirage_projector.image.prism.import"
                : "gui.mirage_projector.image.prism.replace");
    }

    private Component backModeName() {
        return Component.translatable("gui.mirage_projector.image.back_mode." + backFaceMode.name().toLowerCase());
    }

    private Component multiBackModeName() {
        return backModeName();
    }

    private static Component onOff(boolean value) {
        return Component.translatable(value ? "gui.mirage_projector.on" : "gui.mirage_projector.off");
    }

    private void copyNorthToAllFaces() {
        if (frontId.isBlank() || frontWidth <= 0 || frontHeight <= 0) {
            status = Component.translatable("gui.mirage_projector.image.prism.north_required");
            return;
        }
        eastId = frontId;
        eastWidth = frontWidth;
        eastHeight = frontHeight;
        backId = frontId;
        backWidth = frontWidth;
        backHeight = frontHeight;
        westId = frontId;
        westWidth = frontWidth;
        westHeight = frontHeight;
        backFaceMode = ProjectionSettings.BackFaceMode.INDEPENDENT;
        status = Component.translatable("gui.mirage_projector.image.prism.copied_all");
        refreshLabels();
    }

    private void clearFace(Face face) {
        switch (face) {
            case NORTH -> {
                frontId = "";
                frontWidth = 0;
                frontHeight = 0;
            }
            case EAST -> {
                eastId = "";
                eastWidth = 0;
                eastHeight = 0;
            }
            case SOUTH -> {
                backId = "";
                backWidth = 0;
                backHeight = 0;
            }
            case WEST -> {
                westId = "";
                westWidth = 0;
                westHeight = 0;
            }
        }
        status = Component.translatable("gui.mirage_projector.image.face_cleared", faceLabel(face.serializedName));
        refreshLabels();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshModeButton();
    }

    private void refreshModeButton() {
        if (workspaceModeButton == null) {
            return;
        }
        boolean activeMode = modeActivated || (currentProjectionEnabled() && currentSourceMode() == ProjectionSettings.SourceMode.IMAGE);
        workspaceModeButton.active = !activeMode;
        workspaceModeButton.setMessage(activeMode
                ? Component.translatable("gui.mirage_projector.workspace.mode_active")
                : Component.translatable("gui.mirage_projector.workspace.use_mode", modeLabel()));
    }

    private Component modeLabel() {
        return Component.translatable("gui.mirage_projector.workspace.image_short");
    }

    private boolean currentProjectionEnabled() {
        return menu.initialProjectionEnabled();
    }

    private ProjectionSettings.SourceMode currentSourceMode() {
        return menu.initialSettings().sourceMode();
    }

    private void apply(boolean returnToMain) {
        uploadIfPresent(frontId);
        uploadIfPresent(backId);
        uploadIfPresent(eastId);
        uploadIfPresent(westId);
        if (menu.isPresentationDeck()) {
            syncWallFrontAsset();
            for (int slot = 0; slot < ImageSourceBank.PERSISTED_COMPAT_SLOTS; slot++) {
                uploadIfPresent(sourceBank.get(slot).id());
            }
        } else if (menu.supportsMultiSourceLayout()) {
            for (int slot = 0; slot < menu.imageLayoutSlots(); slot++) {
                uploadIfPresent(sourceBank.get(slot).id());
            }
        }
        PacketDistributor.sendToServer(new UpdateImageWorkspacePayload(
                menu.projectorPos(),
                frontId, frontWidth, frontHeight,
                backId, backWidth, backHeight,
                eastId, eastWidth, eastHeight,
                westId, westWidth, westHeight,
                sourceBank.copy(),
                imageLayoutMode, backFaceMode, flipVertical, scanlines, wallSlideIndex,
                automaticPresentationEnabled, automaticPresentationIntervalSeconds
        ));
        if (returnToMain) {
            PacketDistributor.sendToServer(new OpenProjectorWorkspacePayload(menu.projectorPos()));
        }
    }

    private static void uploadIfPresent(String assetId) {
        if (assetId != null && !assetId.isBlank()) {
            ClientAssetTransport.uploadIfPresent(assetId);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF014171D);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 2, 0xFF6B4A7E);
        if (menu.isPresentationDeck()) {
            renderWallPresentationWorkspace(graphics);
        } else if (isMultiSource()) {
            renderMultiSourceWorkspace(graphics);
        } else if (isPrism()) {
            int ox = faceOffsetX();
            int oy = faceOffsetY();
            renderFaceCard(graphics, x + 18 + ox, y + 34 + oy, 88, 126, faceLabel("north"), frontId, frontWidth, frontHeight, false, false);
            renderFaceCard(graphics, x + 115 + ox, y + 34 + oy, 88, 126, faceLabel("east"), eastId, eastWidth, eastHeight, false, false);
            renderFaceCard(graphics, x + 212 + ox, y + 34 + oy, 88, 126, faceLabel("south"), backId, backWidth, backHeight, false, false);
            renderFaceCard(graphics, x + 309 + ox, y + 34 + oy, 88, 126, faceLabel("west"), westId, westWidth, westHeight, false, false);
        } else {
            int ox = faceOffsetX();
            int oy = faceOffsetY();
            renderFaceCard(graphics, x + 18 + ox, y + 36 + oy, 180, 126, frontFaceLabel(), frontId, frontWidth, frontHeight, false, false);
            renderFaceCard(graphics, x + 218 + ox, y + 36 + oy, 180, 126, backFaceLabel(), effectiveBackId(), effectiveBackWidth(), effectiveBackHeight(),
                    backFaceMode == ProjectionSettings.BackFaceMode.MIRRORED,
                    false);
        }
    }

    private void renderWallPresentationWorkspace(GuiGraphics graphics) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x + 18, y + 82, x + 196, y + 262, 0xFF0B0F14);
        graphics.fill(x + 208, y + 82, x + 502, y + 250, 0xFF0B0F14);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.wall.playlist"), x + 24, y + 86, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.wall.preview"), x + 216, y + 86, 0xFFD7B8F5, false);

        for (int slot = 0; slot < ImageSourceBank.PERSISTED_COMPAT_SLOTS; slot++) {
            int column = slot % 3;
            int row = slot / 3;
            int sx = mapX + column * mapCell;
            int sy = mapY + row * mapCell;
            boolean selected = slot == selectedBankSlot;
            boolean current = slot == wallSlideIndex && sourceBank.get(slot).present();
            int border = current ? 0xFFB47CFF : selected ? 0xFFF0E2FF : 0xFF5C5168;
            graphics.fill(sx, sy, sx + mapCell - 3, sy + mapCell - 3, border);
            graphics.fill(sx + 2, sy + 2, sx + mapCell - 5, sy + mapCell - 5, 0xFF10151C);
            ImageSourceBank.Asset asset = sourceBank.get(slot);
            if (asset.present()) {
                renderThumbnail(graphics, asset, sx + 4, sy + 4, mapCell - 12, mapCell - 12);
            } else {
                graphics.drawCenteredString(font, Component.literal("+"), sx + (mapCell - 3) / 2, sy + 19, 0xFF716A79);
            }
            graphics.drawString(font, Component.literal(Integer.toString(slot + 1)), sx + 4, sy + 4, 0xFFFFFFFF, true);
            if (current) {
                graphics.drawString(font, Component.literal("▶"), sx + mapCell - 16, sy + 4, 0xFFD8B7FF, true);
            }
        }

        ImageSourceBank.Asset active = sourceBank.get(sourceBank.normalizePresentIndex(wallSlideIndex));
        renderFaceCard(graphics, x + 216, y + 102, 278, 140,
                Component.translatable("gui.mirage_projector.wall.current_slide", wallSlideIndex + 1),
                active.id(), active.width(), active.height(), false, false);
    }

    private void renderMultiSourceWorkspace(GuiGraphics graphics) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x + 16, y + 92, x + 218, y + 294, 0xFF0B0F14);
        graphics.fill(x + 224, y + 92, x + 504, y + 256, 0xFF0B0F14);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.image.bank.layout"), x + 22, y + 96, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.image.bank.selected", selectedBankSlot + 1), x + 236, y + 96, 0xFFD7B8F5, false);

        int columns = menu.imageLayoutColumns();
        int rows = menu.imageLayoutRows();
        for (int slot = 0; slot < menu.imageLayoutSlots(); slot++) {
            int column = slot % columns;
            int row = slot / columns;
            int sx = mapX + column * mapCell;
            int sy = mapY + row * mapCell;
            boolean selected = slot == selectedBankSlot;
            int border = selected ? 0xFFF0E2FF : 0xFF5C5168;
            graphics.fill(sx, sy, sx + mapCell - 2, sy + mapCell - 2, border);
            graphics.fill(sx + 2, sy + 2, sx + mapCell - 4, sy + mapCell - 4, 0xFF10151C);
            ImageSourceBank.Asset asset = sourceBank.get(slot);
            if (asset.present()) {
                renderThumbnail(graphics, asset, sx + 4, sy + 4, mapCell - 10, mapCell - 10);
            } else {
                graphics.drawCenteredString(font, Component.literal("+"), sx + (mapCell - 2) / 2, sy + (mapCell - 2) / 2 - 4, 0xFF716A79);
            }
            graphics.drawString(font, Component.literal(Integer.toString(slot + 1)), sx + 4, sy + 4, selected ? 0xFFFFFFFF : 0xFFBEB4C8, true);
        }

        ImageSourceBank.Asset selected = sourceBank.get(selectedBankSlot);
        renderFaceCard(graphics, x + 236, y + 110, 256, 138,
                Component.translatable("gui.mirage_projector.image.bank.source", selectedBankSlot + 1),
                selected.id(), selected.width(), selected.height(), false, false);
    }

    private void renderThumbnail(GuiGraphics graphics, ImageSourceBank.Asset asset, int x, int y, int w, int h) {
        ProjectionTextureCache.get(asset.id()).ifPresent(texture -> {
            float scale = Math.min(w / (float) asset.width(), h / (float) asset.height());
            float drawW = asset.width() * scale;
            float drawH = asset.height() * scale;
            float drawX = x + (w - drawW) * 0.5F;
            float drawY = y + (h - drawH) * 0.5F;
            graphics.flush();
            graphics.pose().pushPose();
            graphics.pose().translate(drawX, drawY, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.blit(texture, 0, 0, asset.width(), asset.height(), 0.0F, 0.0F,
                    asset.width(), asset.height(), asset.width(), asset.height());
            graphics.pose().popPose();
            graphics.flush();
        });
    }

    private void renderFaceCard(
            GuiGraphics graphics,
            int x,
            int y,
            int w,
            int h,
            Component label,
            String assetId,
            int sourceW,
            int sourceH,
            boolean mirrorHorizontal,
            boolean fallback
    ) {
        graphics.fill(x, y, x + w, y + h, 0xFF514759);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF0C1015);
        graphics.drawCenteredString(font, label, x + w / 2, y + 7, fallback ? 0xFF817A89 : 0xFFD5C8E4);
        if (assetId == null || assetId.isBlank() || sourceW <= 0 || sourceH <= 0) {
            graphics.drawCenteredString(font, fallback
                    ? Component.translatable("gui.mirage_projector.image.uses_front")
                    : Component.translatable("gui.mirage_projector.image.empty"), x + w / 2, y + h / 2, 0xFF777080);
            return;
        }
        ProjectionTextureCache.get(assetId).ifPresentOrElse(texture -> {
            float availableW = w - 12.0F;
            float availableH = h - 34.0F;
            float scale = Math.min(availableW / sourceW, availableH / sourceH);
            float drawW = sourceW * scale;
            float drawH = sourceH * scale;
            float drawX = x + (w - drawW) * 0.5F;
            float drawY = y + 24 + (availableH - drawH) * 0.5F;
            graphics.flush();
            graphics.pose().pushPose();
            graphics.pose().translate(drawX + (mirrorHorizontal ? drawW : 0.0F), drawY + (flipVertical ? drawH : 0.0F), 0.0F);
            graphics.pose().scale(mirrorHorizontal ? -scale : scale, flipVertical ? -scale : scale, 1.0F);
            graphics.blit(texture, 0, 0, sourceW, sourceH, 0.0F, 0.0F, sourceW, sourceH, sourceW, sourceH);
            graphics.pose().popPose();
            graphics.flush();
            if (scanlines) {
                int minY = Math.round(drawY);
                int maxY = Math.round(drawY + drawH);
                for (int line = minY + 1; line < maxY; line += 3) {
                    graphics.fill(Math.round(drawX), line, Math.round(drawX + drawW), line + 1, 0x66000000);
                }
            }
        }, () -> graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.image.loading"), x + w / 2, y + h / 2, 0xFF8C8494));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, GuiText.fit(font, title.getString(), 220), 10, 9, 0xFFF4F4F4, false);
        if (menu.isPresentationDeck()) {
            graphics.drawString(font, Component.translatable("gui.mirage_projector.wall.summary",
                    sourceBank.countPresent(ImageSourceBank.PERSISTED_COMPAT_SLOTS), ImageSourceBank.PERSISTED_COMPAT_SLOTS),
                    216, 62, 0xFF9FBED1, false);
            graphics.drawString(font, GuiText.fit(font, status.getString(), imageWidth - 44), 22, 367, 0xFFE3D7FF, false);
        } else if (isMultiSource()) {
            graphics.drawString(font, Component.translatable("gui.mirage_projector.image.bank.summary",
                    menu.chassisProfile().displayName(), menu.imageLayoutColumns(), menu.imageLayoutRows(), menu.imageLayoutSlots()),
                    244, 62, 0xFF9FBED1, false);
            graphics.drawString(font, GuiText.fit(font, status.getString(), imageWidth - 44), 22, 362, 0xFFE3D7FF, false);
        } else {
            int ox = faceOffsetX();
            int oy = faceOffsetY();
            graphics.drawString(font, GuiText.fit(font, Component.translatable("gui.mirage_projector.image.face_count", menu.physicalFaceCount()).getString(), 380), 18 + ox, 22 + oy, 0xFF9FBED1, false);
            graphics.drawString(font, GuiText.fit(font, status.getString(), 380), 18 + ox, 236 + oy, 0xFFE3D7FF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.isPresentationDeck() && button == 0 && mouseX >= mapX && mouseY >= mapY
                && mouseX < mapX + mapWidth && mouseY < mapY + mapHeight) {
            int column = (int) ((mouseX - mapX) / mapCell);
            int row = (int) ((mouseY - mapY) / mapCell);
            int slot = row * 3 + column;
            if (slot >= 0 && slot < ImageSourceBank.PERSISTED_COMPAT_SLOTS) {
                selectedBankSlot = slot;
                status = Component.translatable("gui.mirage_projector.image.bank.selected_status", slot + 1);
                refreshLabels();
                return true;
            }
        }
        if (isMultiSource() && button == 0 && mouseX >= mapX && mouseY >= mapY
                && mouseX < mapX + mapWidth && mouseY < mapY + mapHeight) {
            int column = (int) ((mouseX - mapX) / mapCell);
            int row = (int) ((mouseY - mapY) / mapCell);
            int slot = row * menu.imageLayoutColumns() + column;
            if (slot >= 0 && slot < menu.imageLayoutSlots()) {
                selectedBankSlot = slot;
                status = Component.translatable("gui.mirage_projector.image.bank.selected_status", slot + 1);
                refreshLabels();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private String effectiveBackId() {
        return switch (backFaceMode) {
            case MIRRORED, READABLE -> frontId;
            case FRONT, BACK, INDEPENDENT -> backId;
        };
    }

    private int effectiveBackWidth() {
        return switch (backFaceMode) {
            case MIRRORED, READABLE -> frontWidth;
            case FRONT, BACK, INDEPENDENT -> backWidth;
        };
    }

    private int effectiveBackHeight() {
        return switch (backFaceMode) {
            case MIRRORED, READABLE -> frontHeight;
            case FRONT, BACK, INDEPENDENT -> backHeight;
        };
    }

    private Component frontFaceLabel() {
        return backFaceMode == ProjectionSettings.BackFaceMode.BACK
                ? Component.translatable("gui.mirage_projector.image.face.front_off")
                : faceLabel("front");
    }

    private Component backFaceLabel() {
        return switch (backFaceMode) {
            case FRONT -> Component.translatable("gui.mirage_projector.image.face.back_off");
            case BACK -> faceLabel("back");
            case MIRRORED -> Component.translatable("gui.mirage_projector.image.face.back_mirror");
            case READABLE -> Component.translatable("gui.mirage_projector.image.face.back_readable");
            case INDEPENDENT -> Component.translatable("gui.mirage_projector.image.face.back_independent");
        };
    }

    private static Component faceLabel(String face) {
        return Component.translatable("gui.mirage_projector.image.face." + face);
    }

    private String faceAssetId(Face face) {
        return switch (face) {
            case NORTH -> frontId;
            case EAST -> eastId;
            case SOUTH -> backId;
            case WEST -> westId;
        };
    }

    private void openBankFilePicker(int slot) {
        status = Component.translatable("gui.mirage_projector.status.selecting");
        Path selected = chooseImageFile(Component.translatable("gui.mirage_projector.image.picker.slot", slot + 1).getString());
        importSelectedFile(selected, imported -> {
            sourceBank.set(slot, imported.hash(), imported.width(), imported.height());
            if (menu.isPresentationDeck() && !sourceBank.get(wallSlideIndex).present()) {
                wallSlideIndex = slot;
            }
            if (menu.isPresentationDeck()) {
                syncWallFrontAsset();
            }
            status = Component.translatable("gui.mirage_projector.image.bank.imported", slot + 1, imported.width(), imported.height());
            refreshLabels();
        });
    }

    private void openFilePicker(Face face) {
        status = Component.translatable("gui.mirage_projector.status.selecting");
        String pickerKey;
        if (isPrism()) {
            pickerKey = "gui.mirage_projector.image.picker." + face.serializedName;
        } else {
            pickerKey = face == Face.NORTH ? "gui.mirage_projector.image.picker.front" : "gui.mirage_projector.image.picker.back";
        }
        Path selected = chooseImageFile(Component.translatable(pickerKey).getString());
        importSelectedFile(selected, imported -> {
            switch (face) {
                case NORTH -> {
                    frontId = imported.hash();
                    frontWidth = imported.width();
                    frontHeight = imported.height();
                }
                case EAST -> {
                    eastId = imported.hash();
                    eastWidth = imported.width();
                    eastHeight = imported.height();
                }
                case SOUTH -> {
                    backId = imported.hash();
                    backWidth = imported.width();
                    backHeight = imported.height();
                    backFaceMode = ProjectionSettings.BackFaceMode.INDEPENDENT;
                }
                case WEST -> {
                    westId = imported.hash();
                    westWidth = imported.width();
                    westHeight = imported.height();
                }
            }
            status = Component.translatable("gui.mirage_projector.image.imported",
                    faceLabel(isPrism() ? face.serializedName : face == Face.NORTH ? "front" : "back"), imported.width(), imported.height());
            refreshLabels();
        });
    }

    private void importSelectedFile(Path selected, java.util.function.Consumer<ImageImporter.ImportedImage> onSuccess) {
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
            onSuccess.accept(imported);
        }));
    }

    private Path chooseImageFile(String title) {
        return NativeImagePicker.choose(title, "the native image picker");
    }

    @Override
    protected void onContentScrolled(int deltaY) {
        mapY += deltaY;
    }

    private static final class SecondsSlider extends AbstractSliderButton {
        private static final int MIN_SECONDS = 1;
        private static final int MAX_SECONDS = 120;
        private final java.util.function.IntConsumer setter;
        private int seconds;

        private SecondsSlider(int x, int y, int width, int height, int seconds, java.util.function.IntConsumer setter) {
            super(x, y, width, height, Component.empty(), normalize(seconds));
            this.setter = setter;
            this.seconds = clamp(seconds);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("gui.mirage_projector.presentation.interval", seconds));
        }

        @Override
        protected void applyValue() {
            seconds = clamp(MIN_SECONDS + (int) Math.round(value * (MAX_SECONDS - MIN_SECONDS)));
            setter.accept(seconds);
            updateMessage();
        }

        private static double normalize(int seconds) {
            return (clamp(seconds) - MIN_SECONDS) / (double) (MAX_SECONDS - MIN_SECONDS);
        }

        private static int clamp(int seconds) {
            return Math.max(MIN_SECONDS, Math.min(MAX_SECONDS, seconds));
        }
    }

    private enum Face {
        NORTH("north"), EAST("east"), SOUTH("south"), WEST("west");
        private final String serializedName;
        Face(String serializedName) {
            this.serializedName = serializedName;
        }
    }
}
