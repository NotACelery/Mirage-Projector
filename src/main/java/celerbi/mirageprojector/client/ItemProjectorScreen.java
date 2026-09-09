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

/** Focused Item Snapshot workspace laid out around a vanilla-width inventory grid. */
public final class ItemProjectorScreen extends AbstractContainerScreen<ItemProjectorMenu> {
    private static final int W = 360;
    private static final int H = 270;
    private static final int PREVIEW_X = 218;
    private static final int PREVIEW_Y = 40;
    private static final int PREVIEW_W = 120;
    private static final int PREVIEW_H = 94;

    private final ItemProjectionPreviewRenderer preview = new ItemProjectionPreviewRenderer();

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
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.back_to_settings"), button ->
                PacketDistributor.sendToServer(new OpenProjectorWorkspacePayload(menu.projectorPos()))
        ).bounds(leftPos + 218, topPos + 6, 130, 18).build());

        Button activate = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.item.activate"), button ->
                PacketDistributor.sendToServer(new SetProjectionSourcePayload(menu.projectorPos(), ProjectionSettings.SourceMode.ITEM))
        ).bounds(leftPos + 20, topPos + 112, 118, 20).build());
        activate.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.item.activate")));
    }

    private ProjectionSettings previewSettings() {
        if (menu.projector() != null) {
            return menu.projector().settings().withSourceMode(ProjectionSettings.SourceMode.ITEM);
        }
        return ProjectionSettings.DEFAULT.withSourceMode(ProjectionSettings.SourceMode.ITEM);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF014171D);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 2, 0xFF6B4A7E);

        section(graphics, x + 12, y + 32, 336, 112);
        section(graphics, x + 12, y + 150, 336, 108);

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
        graphics.drawString(font, fit(title.getString(), 196), 10, 9, 0xFFF4F4F4, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.item.snapshot_slot"), 20, 43, 0xFFC9CED7, false);
        graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.item.preview"), PREVIEW_X + PREVIEW_W / 2, 43, 0xFFD7B8F5);

        ItemStack stack = menu.snapshotStack();
        Component description = stack.isEmpty()
                ? Component.translatable("gui.mirage_projector.item.empty_hint_short")
                : Component.translatable("gui.mirage_projector.item.captured", stack.getHoverName());
        graphics.drawString(font, fit(description.getString(), 180), 20, 91, stack.isEmpty() ? 0xFF9CA3AF : 0xFF9DDBA8, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.item.virtual_notice_short"), 20, 101, 0xFF8FCFA0, false);
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
