package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.menu.ImageProjectorMenu;
import celerbi.mirageprojector.network.OpenProjectorWorkspacePayload;
import celerbi.mirageprojector.network.UpdateImageWorkspacePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/** Dedicated 2D source/face assignment workspace. Presentation controls live in the primary GUI. */
public final class ImageProjectorScreen extends AbstractContainerScreen<ImageProjectorMenu> {
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
    private ProjectionSettings.BackFaceMode backFaceMode;
    private boolean flipVertical;
    private boolean scanlines;
    private Component status = Component.translatable("gui.mirage_projector.status.ready");

    private Button frontButton;
    private Button backButton;
    private Button eastButton;
    private Button westButton;
    private Button frontClearButton;
    private Button backClearButton;
    private Button eastClearButton;
    private Button westClearButton;
    private Button backModeButton;
    private Button sameAllButton;
    private Button flipButton;
    private Button scanlinesButton;

    public ImageProjectorScreen(ImageProjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 416;
        imageHeight = 286;
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
        backFaceMode = s.backFaceMode();
        flipVertical = s.flipVertical();
        scanlines = s.scanlines();
    }

    @Override
    protected void init() {
        super.init();
        boolean prism = isPrism();

        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.back_to_settings"), button -> {
            apply(false);
            PacketDistributor.sendToServer(new OpenProjectorWorkspacePayload(menu.projectorPos()));
        }).bounds(leftPos + 278, topPos + 6, 126, 18).build());

        if (prism) {
            frontButton = addFaceButton(Face.NORTH, 18);
            eastButton = addFaceButton(Face.EAST, 115);
            backButton = addFaceButton(Face.SOUTH, 212);
            westButton = addFaceButton(Face.WEST, 309);

            frontClearButton = addClearButton(Face.NORTH, 88);
            eastClearButton = addClearButton(Face.EAST, 185);
            backClearButton = addClearButton(Face.SOUTH, 282);
            westClearButton = addClearButton(Face.WEST, 379);
        } else {
            frontButton = addRenderableWidget(Button.builder(frontImportLabel(), button -> openFilePicker(Face.NORTH))
                    .bounds(leftPos + 18, topPos + 178, 156, 20).build());
            frontClearButton = addRenderableWidget(Button.builder(Component.literal("×"), button -> clearFace(Face.NORTH))
                    .bounds(leftPos + 178, topPos + 178, 20, 20).build());

            backButton = addRenderableWidget(Button.builder(backImportLabel(), button -> openFilePicker(Face.SOUTH))
                    .bounds(leftPos + 218, topPos + 178, 156, 20).build());
            backClearButton = addRenderableWidget(Button.builder(Component.literal("×"), button -> clearFace(Face.SOUTH))
                    .bounds(leftPos + 378, topPos + 178, 20, 20).build());
        }

        backModeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            backFaceMode = switch (backFaceMode) {
                case MIRRORED -> ProjectionSettings.BackFaceMode.READABLE;
                case READABLE -> ProjectionSettings.BackFaceMode.INDEPENDENT;
                case INDEPENDENT -> ProjectionSettings.BackFaceMode.MIRRORED;
            };
            refreshLabels();
        }).bounds(leftPos + 18, topPos + 208, 180, 20).build());
        backModeButton.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.image.back_mode")));
        backModeButton.visible = !prism;

        sameAllButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.image.prism.same_all"),
                button -> copyNorthToAllFaces()
        ).bounds(leftPos + 18, topPos + 208, 180, 20).build());
        sameAllButton.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.image.prism.same_all")));
        sameAllButton.visible = prism;

        flipButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            flipVertical = !flipVertical;
            refreshLabels();
        }).bounds(leftPos + 218, topPos + 208, 86, 20).build());
        scanlinesButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            scanlines = !scanlines;
            refreshLabels();
        }).bounds(leftPos + 312, topPos + 208, 86, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.apply"), button -> apply(true))
                .bounds(leftPos + 18, topPos + 252, 180, 22).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.cancel"), button ->
                PacketDistributor.sendToServer(new OpenProjectorWorkspacePayload(menu.projectorPos()))
        ).bounds(leftPos + 218, topPos + 252, 180, 22).build());
        refreshLabels();
    }

    private Button addFaceButton(Face face, int localX) {
        return addRenderableWidget(Button.builder(faceImportLabel(face), button -> openFilePicker(face))
                .bounds(leftPos + localX, topPos + 164, 68, 20).build());
    }

    private Button addClearButton(Face face, int localX) {
        return addRenderableWidget(Button.builder(Component.literal("×"), button -> clearFace(face))
                .bounds(leftPos + localX, topPos + 164, 18, 20).build());
    }

    private boolean isPrism() {
        return menu.physicalFaceCount() == 4;
    }

    private void refreshLabels() {
        if (frontButton != null) frontButton.setMessage(isPrism() ? faceImportLabel(Face.NORTH) : frontImportLabel());
        if (eastButton != null) eastButton.setMessage(faceImportLabel(Face.EAST));
        if (backButton != null) backButton.setMessage(isPrism() ? faceImportLabel(Face.SOUTH) : backImportLabel());
        if (westButton != null) westButton.setMessage(faceImportLabel(Face.WEST));
        if (backModeButton != null) backModeButton.setMessage(Component.translatable("gui.mirage_projector.image.back_mode", backModeName()));
        if (flipButton != null) flipButton.setMessage(Component.translatable("gui.mirage_projector.image.flip", onOff(flipVertical)));
        if (scanlinesButton != null) scanlinesButton.setMessage(Component.translatable("gui.mirage_projector.image.scanlines", onOff(scanlines)));
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

    private void apply(boolean returnToMain) {
        uploadIfPresent(frontId);
        uploadIfPresent(backId);
        uploadIfPresent(eastId);
        uploadIfPresent(westId);
        PacketDistributor.sendToServer(new UpdateImageWorkspacePayload(
                menu.projectorPos(),
                frontId, frontWidth, frontHeight,
                backId, backWidth, backHeight,
                eastId, eastWidth, eastHeight,
                westId, westWidth, westHeight,
                backFaceMode, flipVertical, scanlines
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
        if (isPrism()) {
            renderFaceCard(graphics, x + 18, y + 34, 88, 126, faceLabel("north"), frontId, frontWidth, frontHeight, false, false);
            renderFaceCard(graphics, x + 115, y + 34, 88, 126, faceLabel("east"), eastId, eastWidth, eastHeight, false, false);
            renderFaceCard(graphics, x + 212, y + 34, 88, 126, faceLabel("south"), backId, backWidth, backHeight, false, false);
            renderFaceCard(graphics, x + 309, y + 34, 88, 126, faceLabel("west"), westId, westWidth, westHeight, false, false);
        } else {
            renderFaceCard(graphics, x + 18, y + 36, 180, 126, faceLabel("front"), frontId, frontWidth, frontHeight, false, false);
            renderFaceCard(
                    graphics,
                    x + 218,
                    y + 36,
                    180,
                    126,
                    backFaceLabel(),
                    effectiveBackId(),
                    effectiveBackWidth(),
                    effectiveBackHeight(),
                    backFaceMode == ProjectionSettings.BackFaceMode.MIRRORED,
                    backFaceMode == ProjectionSettings.BackFaceMode.INDEPENDENT && backId.isBlank()
            );
        }
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
            graphics.drawCenteredString(
                    font,
                    fallback
                            ? Component.translatable("gui.mirage_projector.image.uses_front")
                            : Component.translatable("gui.mirage_projector.image.empty"),
                    x + w / 2,
                    y + h / 2,
                    0xFF777080
            );
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
            graphics.pose().translate(
                    drawX + (mirrorHorizontal ? drawW : 0.0F),
                    drawY + (flipVertical ? drawH : 0.0F),
                    0.0F
            );
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
        }, () -> graphics.drawCenteredString(
                font,
                Component.translatable("gui.mirage_projector.image.loading"),
                x + w / 2,
                y + h / 2,
                0xFF8C8494
        ));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 10, 9, 0xFFF4F4F4, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.image.face_count", menu.physicalFaceCount()), 18, 22, 0xFF9FBED1, false);
        graphics.drawString(font, status, 18, 236, 0xFFE3D7FF, false);
    }

    private String effectiveBackId() {
        return backFaceMode == ProjectionSettings.BackFaceMode.INDEPENDENT && !backId.isBlank() ? backId : frontId;
    }

    private int effectiveBackWidth() {
        return backFaceMode == ProjectionSettings.BackFaceMode.INDEPENDENT && !backId.isBlank() ? backWidth : frontWidth;
    }

    private int effectiveBackHeight() {
        return backFaceMode == ProjectionSettings.BackFaceMode.INDEPENDENT && !backId.isBlank() ? backHeight : frontHeight;
    }

    private Component backFaceLabel() {
        return switch (backFaceMode) {
            case MIRRORED -> Component.translatable("gui.mirage_projector.image.face.back_mirror");
            case READABLE -> Component.translatable("gui.mirage_projector.image.face.back_readable");
            case INDEPENDENT -> faceLabel("back");
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

    private void openFilePicker(Face face) {
        status = Component.translatable("gui.mirage_projector.status.selecting");
        Path selected = chooseImageFile(face);
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
                status = Component.translatable(
                        "gui.mirage_projector.image.import_failed",
                        cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage()
                );
                return;
            }
            ProjectionTextureCache.invalidate(imported.hash());
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
            status = Component.translatable(
                    "gui.mirage_projector.image.imported",
                    faceLabel(isPrism() ? face.serializedName : face == Face.NORTH ? "front" : "back"),
                    imported.width(),
                    imported.height()
            );
            refreshLabels();
        }));
    }

    private Path chooseImageFile(Face face) {
        String pickerKey;
        if (isPrism()) {
            pickerKey = "gui.mirage_projector.image.picker." + face.serializedName;
        } else {
            pickerKey = face == Face.NORTH
                    ? "gui.mirage_projector.image.picker.front"
                    : "gui.mirage_projector.image.picker.back";
        }
        String title = Component.translatable(pickerKey).getString();
        String startPath = System.getProperty("user.home", "");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(4);
            filters.put(stack.UTF8("*.png"));
            filters.put(stack.UTF8("*.jpg"));
            filters.put(stack.UTF8("*.jpeg"));
            filters.put(stack.UTF8("*.webp"));
            filters.flip();
            String path = TinyFileDialogs.tinyfd_openFileDialog(
                    title,
                    startPath,
                    filters,
                    "PNG / JPG / JPEG / WebP",
                    false
            );
            return path == null || path.isBlank() ? null : Path.of(path);
        } catch (Throwable throwable) {
            MirageProjector.LOGGER.error("Could not open the native image picker", throwable);
            return null;
        }
    }

    private enum Face {
        NORTH("north"),
        EAST("east"),
        SOUTH("south"),
        WEST("west");

        private final String serializedName;

        Face(String serializedName) {
            this.serializedName = serializedName;
        }
    }
}
