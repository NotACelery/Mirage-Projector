package celerbi.mirageprojector.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Dynamic leather panel behind the equipped Shoulder Strap inventory. */
public final class MirageEquipmentPanelWidget extends AbstractWidget {
    public static final int WIDTH = 96;
    public static final int EMPTY_HEIGHT = 50;
    public static final int BASE_HEIGHT = 136;
    public static final int EXPANDED_HEIGHT = 156;

    public MirageEquipmentPanelWidget(int x, int y) {
        super(x, y, WIDTH, EXPANDED_HEIGHT, Component.translatable("gui.mirage_projector.equipment.panel"));
        this.active = false;
    }

    public static int height(boolean strapPresent, boolean expanded) {
        if (!strapPresent) {
            return EMPTY_HEIGHT;
        }
        return expanded ? EXPANDED_HEIGHT : BASE_HEIGHT;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean strapPresent = ClientShoulderEquipment.localStrapPresent();
        boolean expanded = strapPresent && ClientShoulderEquipment.localExpansionInstalled();
        int visibleHeight = height(strapPresent, expanded);
        graphics.fill(getX(), getY(), getX() + width, getY() + visibleHeight, 0xE6211712);
        graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + visibleHeight - 1, 0xFF704C32);
        graphics.fill(getX() + 3, getY() + 3, getX() + width - 3, getY() + visibleHeight - 3, 0xE63A2A21);

        var font = Minecraft.getInstance().font;
        graphics.drawString(
                font,
                Component.translatable("gui.mirage_projector.equipment.shoulder_slot"),
                getX() + 6,
                getY() + 6,
                0xFFF1D8B8,
                false
        );
        if (!strapPresent) {
            return;
        }
        graphics.drawString(
                font,
                Component.translatable("gui.mirage_projector.equipment.upgrades"),
                getX() + 6,
                getY() + 43,
                0xFFE2C39F,
                false
        );
        graphics.drawString(
                font,
                Component.translatable("gui.mirage_projector.equipment.power_cells"),
                getX() + 6,
                getY() + 78,
                0xFFE2C39F,
                false
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
