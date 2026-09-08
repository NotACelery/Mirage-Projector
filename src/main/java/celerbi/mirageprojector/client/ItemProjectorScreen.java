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

/** Small focused workspace for capturing and inspecting one virtual Item Snapshot. */
public final class ItemProjectorScreen extends AbstractContainerScreen<ItemProjectorMenu> {
    private static final int W = 250;
    private static final int H = 236;
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
        ).bounds(leftPos + 112, topPos + 6, 128, 18).build());

        Button activate = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.item.activate"), button ->
                PacketDistributor.sendToServer(new SetProjectionSourcePayload(menu.projectorPos(), ProjectionSettings.SourceMode.ITEM))
        ).bounds(leftPos + 16, topPos + 102, 92, 20).build());
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
        drawSlotFrame(graphics, x + ItemProjectorMenu.SNAPSHOT_X - 1, y + ItemProjectorMenu.SNAPSHOT_Y - 1);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            drawSlotFrame(graphics, x + ItemProjectorMenu.PLAYER_INV_X + col * 18 - 1, y + ItemProjectorMenu.PLAYER_INV_Y + row * 18 - 1);
        for (int col = 0; col < 9; col++)
            drawSlotFrame(graphics, x + ItemProjectorMenu.PLAYER_INV_X + col * 18 - 1, y + ItemProjectorMenu.PLAYER_INV_Y + 58 - 1);

        preview.render(graphics, menu.snapshotStack(), menu.snapshotId(), previewSettings(), x + 124, y + 35, 106, 78, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 10, 9, 0xFFF4F4F4, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.item.snapshot_slot"), 14, 45, 0xFFC9CED7, false);
        ItemStack stack = menu.snapshotStack();
        Component description = stack.isEmpty()
                ? Component.translatable("gui.mirage_projector.item.empty_hint")
                : Component.translatable("gui.mirage_projector.item.captured", stack.getHoverName());
        graphics.drawString(font, description, 14, 84, stack.isEmpty() ? 0xFF9CA3AF : 0xFF9DDBA8, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.item.virtual_notice"), 14, 94, 0xFF8FCFA0, false);
        graphics.drawString(font, Component.translatable("container.inventory"), ItemProjectorMenu.PLAYER_INV_X, ItemProjectorMenu.PLAYER_INV_Y - 12, 0xFFBEB8C8, false);
    }

    @Override
    public void removed() {
        preview.invalidate();
        super.removed();
    }

    private static void drawSlotFrame(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF5A5361);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF171A20);
    }
}
