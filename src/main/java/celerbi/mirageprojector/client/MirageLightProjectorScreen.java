package celerbi.mirageprojector.client;

import celerbi.mirageprojector.menu.MirageLightProjectorMenu;
import celerbi.mirageprojector.network.LightProjectorActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/** Proper configuration GUI for the placed Mirage Light Projector. */
public final class MirageLightProjectorScreen extends AbstractContainerScreen<MirageLightProjectorMenu> {
    public MirageLightProjectorScreen(MirageLightProjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 196;
        imageHeight = 184;
        inventoryLabelX = MirageLightProjectorMenu.PLAYER_INV_X;
        inventoryLabelY = MirageLightProjectorMenu.PLAYER_INV_Y - 12;
    }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.light_projector.cycle_mode"),
                button -> PacketDistributor.sendToServer(new LightProjectorActionPayload(
                        menu.projectorPos(), LightProjectorActionPayload.Action.CYCLE_MODE))
        ).bounds(leftPos + 66, topPos + 41, 112, 20).build());
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF014171D);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 3, 0xFF72528D);
        graphics.fill(x + 12, y + 25, x + 58, y + 76, 0xA20B0E13);
        slot(graphics, x + MirageLightProjectorMenu.BATTERY_X - 1, y + MirageLightProjectorMenu.BATTERY_Y - 1, 0xFF7954A0);
        graphics.fill(x + 10, y + 83, x + imageWidth - 10, y + imageHeight - 8, 0xA20B0E13);
        for (int row=0; row<3; row++) for (int col=0; col<9; col++) slot(graphics,
                x + MirageLightProjectorMenu.PLAYER_INV_X + col*18 - 1,
                y + MirageLightProjectorMenu.PLAYER_INV_Y + row*18 - 1, 0xFF4D4A52);
        for (int col=0; col<9; col++) slot(graphics,
                x + MirageLightProjectorMenu.PLAYER_INV_X + col*18 - 1,
                y + MirageLightProjectorMenu.PLAYER_INV_Y + 58 - 1, 0xFF4D4A52);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 10, 9, 0xFFF4F4F4, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.battery"), 18, 31, 0xFFD7B8F5, false);
        if (menu.projector() != null) graphics.drawString(font,
                Component.translatable("gui.mirage_projector.portable_device.mode", Component.translatable(menu.projector().mode().displayTranslationKey())),
                66, 18, 0xFFD7B8F5, false);
        graphics.drawString(font, Component.translatable("container.inventory"), inventoryLabelX, inventoryLabelY, 0xFFBEB8C8, false);
    }

    private static void slot(GuiGraphics graphics, int x, int y, int border) {
        graphics.fill(x,y,x+18,y+18,border); graphics.fill(x+1,y+1,x+17,y+17,0xFF171A20);
    }
}
