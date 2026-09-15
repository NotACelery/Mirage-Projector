package celerbi.mirageprojector.client;

import celerbi.mirageprojector.blockentity.ChargingStationBlockEntity;
import celerbi.mirageprojector.item.RechargeableEnergyItem;
import celerbi.mirageprojector.menu.ChargingStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Roomy cutter-style queue -> charger -> output interface with compatibility space above inventory. */
public final class ChargingStationScreen extends AbstractContainerScreen<ChargingStationMenu> {
    public ChargingStationScreen(ChargingStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 270;
        imageHeight = 220;
        inventoryLabelX = ChargingStationMenu.PLAYER_INV_X;
        inventoryLabelY = ChargingStationMenu.PLAYER_INV_Y - 14;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF014171D);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 3, 0xFF72528D);

        section(graphics, x + 12, y + 29, 246, 70);
        section(graphics, x + 42, y + 116, 186, 96);

        for (int i=0;i<ChargingStationBlockEntity.INPUT_COUNT;i++)
            drawSlotFrame(graphics, x + ChargingStationMenu.INPUT_X + i*18 - 1, y + ChargingStationMenu.INPUT_Y - 1, 0xFF4C3858);
        drawSlotFrame(graphics, x + ChargingStationMenu.CHARGING_X - 1, y + ChargingStationMenu.CHARGING_Y - 1, 0xFF7954A0);
        for (int i=0;i<ChargingStationBlockEntity.OUTPUT_COUNT;i++)
            drawSlotFrame(graphics, x + ChargingStationMenu.OUTPUT_X + i*18 - 1, y + ChargingStationMenu.OUTPUT_Y - 1, 0xFF42634E);

        drawArrow(graphics, x + 101, y + 60, x + 120, y + 67);
        drawArrow(graphics, x + 151, y + 60, x + 172, y + 67);

        for (int row=0;row<3;row++) for (int col=0;col<9;col++)
            drawSlotFrame(graphics, x + ChargingStationMenu.PLAYER_INV_X + col*18 - 1,
                    y + ChargingStationMenu.PLAYER_INV_Y + row*18 - 1, 0xFF4D4A52);
        for (int col=0;col<9;col++)
            drawSlotFrame(graphics, x + ChargingStationMenu.PLAYER_INV_X + col*18 - 1,
                    y + ChargingStationMenu.PLAYER_INV_Y + 58 - 1, 0xFF4D4A52);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 10, 9, 0xFFF4F4F4, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.charging_station.queue"), 20, 39, 0xFFC9CED7, false);
        graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.charging_station.active"), 135, 39, 0xFFD7B8F5);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.charging_station.output"), 178, 39, 0xFFB5D9BF, false);
        graphics.drawString(font, Component.translatable("container.inventory"), inventoryLabelX, inventoryLabelY, 0xFFBEB8C8, false);

        ItemStack active = menu.activeChargingStack();
        Component status;
        int color;
        if (active.isEmpty()) {
            status = Component.translatable("gui.mirage_projector.charging_station.idle"); color = 0xFF8F96A3;
        } else if (RechargeableEnergyItem.isFull(active)) {
            status = Component.translatable("gui.mirage_projector.charging_station.full_waiting"); color = 0xFF91D5A3;
        } else {
            status = Component.translatable("gui.mirage_projector.charging_station.progress", RechargeableEnergyItem.chargePercent(active)); color = 0xFFD2B3ED;
        }
        graphics.drawCenteredString(font, status, 135, 83, color);
    }

    private static void section(GuiGraphics g,int x,int y,int w,int h){g.fill(x,y,x+w,y+h,0xA20B0E13);g.fill(x,y,x+2,y+h,0xFF4C3858);}
    private static void drawSlotFrame(GuiGraphics g,int x,int y,int b){g.fill(x,y,x+18,y+18,b);g.fill(x+1,y+1,x+17,y+17,0xFF171A20);}
    private static void drawArrow(GuiGraphics g,int x1,int y1,int x2,int y2){int m=(y1+y2)/2;g.fill(x1,m-1,x2-2,m+1,0xFF7B7085);g.fill(x2-4,y1,x2,y2,0xFF7B7085);}
}
