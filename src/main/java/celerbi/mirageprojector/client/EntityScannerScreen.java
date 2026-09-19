package celerbi.mirageprojector.client;

import celerbi.mirageprojector.menu.EntityScannerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class EntityScannerScreen extends AbstractContainerScreen<EntityScannerMenu> {
    public EntityScannerScreen(EntityScannerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF17151D);
        graphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + imageHeight - 3, 0xFF292332);
        slotFrame(graphics, leftPos + 80, topPos + 28, 0xFF8E57B2);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                slotFrame(graphics, leftPos + 8 + column * 18, topPos + 84 + row * 18, 0xFF5B5266);
            }
        }
        for (int column = 0; column < 9; column++) {
            slotFrame(graphics, leftPos + 8 + column * 18, topPos + 142, 0xFF5B5266);
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0xFFE9D6FF, false);
        graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.entity_scanner.codex_slot"), imageWidth / 2, 58, 0xFFCDB4E8);
        graphics.drawString(font, playerInventoryTitle, 8, 72, 0xFFE9D6FF, false);
    }

    private static void slotFrame(GuiGraphics graphics, int x, int y, int border) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, border);
        graphics.fill(x, y, x + 16, y + 16, 0xFF121018);
    }
}
