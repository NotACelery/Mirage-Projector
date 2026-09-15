package celerbi.mirageprojector.client;

import celerbi.mirageprojector.network.ShoulderEquipmentActionPayload;
import celerbi.mirageprojector.network.OpenPortableDeviceMenuPayload;
import celerbi.mirageprojector.menu.PortableDeviceSource;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Leather-framed slot widget for the Mirage Equipment panel. */
public final class MirageEquipmentSlotWidget extends AbstractWidget {
    public static final int SLOT_SIZE = 20;
    private static final int LEATHER_BORDER = 0xFF704C32;
    private static final int LEATHER_LIGHT = 0xFF9B6A45;
    private static final int SLOT_DARK = 0xFF1B1B1B;
    private static final int SLOT_INNER = 0xFF8B8B8B;
    private static final int LOCKED_INNER = 0xFF48413B;

    private final ShoulderEquipmentActionPayload.Target target;

    public MirageEquipmentSlotWidget(int x, int y, ShoulderEquipmentActionPayload.Target target) {
        super(x, y, SLOT_SIZE, SLOT_SIZE, label(target));
        this.target = target;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean unlocked = unlocked();
        this.active = unlocked;

        int border = isHovered() ? LEATHER_LIGHT : LEATHER_BORDER;
        graphics.fill(getX(), getY(), getX() + width, getY() + height, SLOT_DARK);
        graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, border);
        graphics.fill(
                getX() + 3,
                getY() + 3,
                getX() + width - 3,
                getY() + height - 3,
                unlocked ? SLOT_INNER : LOCKED_INNER
        );

        ItemStack stack = displayedStack();
        if (!stack.isEmpty()) {
            graphics.renderItem(stack, getX() + 2, getY() + 2);
            graphics.renderItemDecorations(Minecraft.getInstance().font, stack, getX() + 2, getY() + 2);
        } else if (!unlocked) {
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    "×",
                    getX() + width / 2,
                    getY() + 6,
                    0xFFB9A99B
            );
        }

        if (isHovered()) {
            graphics.renderTooltip(
                    Minecraft.getInstance().font,
                    tooltip(unlocked),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (button == 1 && target == ShoulderEquipmentActionPayload.Target.DEVICE) {
            ItemStack device = ClientShoulderEquipment.localDevice();
            if (!device.isEmpty()) {
                PacketDistributor.sendToServer(new OpenPortableDeviceMenuPayload(PortableDeviceSource.SHOULDER));
                return true;
            }
        }
        if (button != 0 || !active) {
            return false;
        }
        PacketDistributor.sendToServer(new ShoulderEquipmentActionPayload(target));
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    private boolean unlocked() {
        if (target == ShoulderEquipmentActionPayload.Target.STRAP) {
            return true;
        }
        if (!ClientShoulderEquipment.localStrapPresent()) {
            return false;
        }
        if (target.battery()) {
            return target.batteryIndex() < ClientShoulderEquipment.localActiveBatterySlots();
        }
        if (target.upgrade()) {
            return target.upgradeIndex() < ClientShoulderEquipment.localActiveUpgradeSlots();
        }
        return true;
    }

    private ItemStack displayedStack() {
        if (target == ShoulderEquipmentActionPayload.Target.STRAP) {
            return ClientShoulderEquipment.localStrapPresent()
                    ? new ItemStack(ModItems.SHOULDER_STRAP.get())
                    : ItemStack.EMPTY;
        }
        if (target == ShoulderEquipmentActionPayload.Target.DEVICE) {
            return ClientShoulderEquipment.localDevice();
        }
        if (target.battery()) {
            return ClientShoulderEquipment.localBattery(target.batteryIndex());
        }
        if (target.upgrade()) {
            return ClientShoulderEquipment.localUpgrade(target.upgradeIndex());
        }
        return ItemStack.EMPTY;
    }

    private Component tooltip(boolean unlocked) {
        if (!unlocked) {
            if (!ClientShoulderEquipment.localStrapPresent()) {
                return Component.translatable("gui.mirage_projector.equipment.requires_strap");
            }
            return Component.translatable("gui.mirage_projector.equipment.requires_expansion");
        }
        return getMessage();
    }

    private static Component label(ShoulderEquipmentActionPayload.Target target) {
        if (target == ShoulderEquipmentActionPayload.Target.STRAP) {
            return Component.translatable("gui.mirage_projector.equipment.strap_slot");
        }
        if (target == ShoulderEquipmentActionPayload.Target.DEVICE) {
            return Component.translatable("gui.mirage_projector.equipment.shoulder_slot");
        }
        if (target.battery()) {
            return Component.translatable("gui.mirage_projector.equipment.battery_slot", target.batteryIndex() + 1);
        }
        return Component.translatable("gui.mirage_projector.equipment.upgrade_slot", target.upgradeIndex() + 1);
    }
}
