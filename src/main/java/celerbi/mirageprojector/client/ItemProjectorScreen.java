package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.menu.ItemProjectorMenu;
import celerbi.mirageprojector.network.OpenProjectorWorkspacePayload;
import celerbi.mirageprojector.network.SetProjectionSourcePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ItemProjectorScreen extends AbstractContainerScreen<ItemProjectorMenu> {
    private static final int W = 360;
    private static final int H = 298;
    private static final int PREVIEW_X = 218;
    private static final int PREVIEW_Y = 82;
    private static final int PREVIEW_W = 120;
    private static final int PREVIEW_H = 80;

    private final ItemProjectionPreviewRenderer preview = new ItemProjectionPreviewRenderer();
    private Button modeButton;
    private Button backButton;

    public ItemProjectorScreen(ItemProjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = W;
        imageHeight = H;
        inventoryLabelX = ItemProjectorMenu.PLAYER_INV_X;
        inventoryLabelY = ItemProjectorMenu.PLAYER_INV_Y - 12;
    }

    @Override
    protected void init() {
        super.init();
        modeButton = addRenderableWidget(Button.builder(Component.empty(), button ->
                PacketDistributor.sendToServer(new SetProjectionSourcePayload(menu.projectorPos(), ProjectionSettings.SourceMode.ITEM))
        ).bounds(leftPos + imageWidth - 274, topPos + 34, 130, 18).build());
        modeButton.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.item.activate")));

        backButton = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.workspace.back"), button ->
                PacketDistributor.sendToServer(new OpenProjectorWorkspacePayload(menu.projectorPos()))
        ).bounds(leftPos + imageWidth - 138, topPos + 34, 126, 18).build());

        refreshModeButton();
    }

    private ProjectionSettings previewSettings() {
        if (menu.projector() != null) {
            return menu.projector().settings().withSourceMode(ProjectionSettings.SourceMode.ITEM);
        }
        return ProjectionSettings.DEFAULT.withSourceMode(ProjectionSettings.SourceMode.ITEM);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshModeButton();
    }

    private void refreshModeButton() {
        if (modeButton == null) {
            return;
        }
        boolean activeMode = currentProjectionEnabled() && currentSourceMode() == ProjectionSettings.SourceMode.ITEM;
        modeButton.active = !activeMode;
        modeButton.setMessage(activeMode
                ? Component.translatable("gui.mirage_projector.workspace.mode_active")
                : Component.translatable("gui.mirage_projector.workspace.use_mode", modeLabel()));
    }

    private Component modeLabel() {
        return Component.translatable("gui.mirage_projector.workspace.item_short");
    }

    private boolean currentProjectionEnabled() {
        return menu.projector() == null || menu.projector().projectionEnabled();
    }

    private ProjectionSettings.SourceMode currentSourceMode() {
        if (menu.projector() != null) {
            return menu.projector().settings().sourceMode();
        }
        return ProjectionSettings.SourceMode.ITEM;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF014171D);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 2, 0xFF6B4A7E);

        section(graphics, x + 12, y + 60, 336, 112);
        section(graphics, x + 12, y + 178, 336, 108);

        drawSlotFrame(graphics, x + ItemProjectorMenu.SNAPSHOT_X - 1, y + ItemProjectorMenu.SNAPSHOT_Y - 1);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotFrame(graphics,
                        x + ItemProjectorMenu.PLAYER_INV_X + col * 18 - 1,
                        y + ItemProjectorMenu.PLAYER_INV_Y + row * 18 - 1);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotFrame(graphics,
                    x + ItemProjectorMenu.PLAYER_INV_X + col * 18 - 1,
                    y + ItemProjectorMenu.PLAYER_INV_Y + 58 - 1);
        }

        preview.render(
                graphics,
                menu.snapshotStack(),
                menu.snapshotId(),
                previewSettings(),
                x + PREVIEW_X,
                y + PREVIEW_Y,
                PREVIEW_W,
                PREVIEW_H,
                mouseX,
                mouseY
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, fit(title.getString(), 150), 10, 9, 0xFFF4F4F4, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.item.snapshot_slot"), 20, 71, 0xFFC9CED7, false);
        graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.item.preview"), PREVIEW_X + PREVIEW_W / 2, 70, 0xFFD7B8F5);

        ItemStack stack = menu.snapshotStack();
        Component description = stack.isEmpty()
                ? Component.translatable("gui.mirage_projector.item.empty_hint_short")
                : Component.translatable("gui.mirage_projector.item.captured", stack.getHoverName());
        graphics.drawString(font, fit(description.getString(), 180), 20, 119, stack.isEmpty() ? 0xFF9CA3AF : 0xFF9DDBA8, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.item.virtual_notice_short"), 20, 129, 0xFF8FCFA0, false);
        graphics.drawString(font, Component.translatable("container.inventory"), ItemProjectorMenu.PLAYER_INV_X, ItemProjectorMenu.PLAYER_INV_Y - 12, 0xFFBEB8C8, false);
    }

    @Override
    public void removed() {
        preview.invalidate();
        super.removed();
    }

    private static void section(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0xA20B0E13);
        graphics.fill(x, y, x + 2, y + h, 0xFF4C3858);
    }

    private static void drawSlotFrame(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF5A5361);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF171A20);
    }

    private String fit(String value, int maxWidth) {
        if (value == null || value.isEmpty() || font.width(value) <= maxWidth) {
            return value == null ? "" : value;
        }
        String ellipsis = "…";
        int target = Math.max(0, maxWidth - font.width(ellipsis));
        int end = value.length();
        while (end > 0 && font.width(value.substring(0, end)) > target) {
            end--;
        }
        return value.substring(0, Math.max(0, end)) + ellipsis;
    }
}
