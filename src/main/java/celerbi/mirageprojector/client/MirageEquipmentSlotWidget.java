package celerbi.mirageprojector.client;

import celerbi.mirageprojector.network.ShoulderEquipmentActionPayload;
import celerbi.mirageprojector.network.OpenPortableDeviceMenuPayload;
import celerbi.mirageprojector.menu.PortableDeviceSource;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

        if (isHovered() && !stack.isEmpty()) {
            // These are real item stacks, so expose their ordinary item tooltip instead of
            // replacing it with the slot label.
            graphics.renderTooltip(Minecraft.getInstance().font, stack, mouseX, mouseY);
        } else if (isHovered() && !unlocked) {
            graphics.renderTooltip(Minecraft.getInstance().font, tooltip(false), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (button == 1 && (target == ShoulderEquipmentActionPayload.Target.DEVICE
                || target == ShoulderEquipmentActionPayload.Target.STRAP)) {
            ItemStack device = ClientShoulderEquipment.localDevice();
            if (!device.isEmpty()) {
                PacketDistributor.sendToServer(new OpenPortableDeviceMenuPayload(PortableDeviceSource.SHOULDER));
                return true;
            }
        }
        if (button != 0 || !active) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack carried = ItemStack.EMPTY;
        if (minecraft.screen instanceof AbstractContainerScreen<?> containerScreen) {
            carried = containerScreen.getMenu().getCarried().copy();
        } else if (minecraft.player != null && minecraft.player.containerMenu != null) {
            carried = minecraft.player.containerMenu.getCarried().copy();
        }
        // Preserve the cursor that existed before this click for the server packet.  Creative
        // explicitly trusts that packet cursor; sending the optimistic pick-up below made the
        // server interpret an extraction as a swap with the very same stack, duplicating it.
        ItemStack carriedBeforeClick = carried.copy();

        // This panel is external to vanilla's Slot list.  Put a picked-up stack on the local
        // cursor immediately, then let the authoritative server correction confirm or undo it.
        // Without this bridge vanilla can process the following outside click as an empty-cursor
        // click and throw the just-extracted stack onto the ground.
        if (carried.isEmpty()) {
            ItemStack displayed = displayedStack();
            if (!displayed.isEmpty()) {
                if (minecraft.screen instanceof AbstractContainerScreen<?> containerScreen) {
                    containerScreen.getMenu().setCarried(displayed.copy());
                }
                if (minecraft.player != null && minecraft.player.containerMenu != null) {
                    minecraft.player.containerMenu.setCarried(displayed.copy());
                }
                carried = displayed;
            }
        }
        PacketDistributor.sendToServer(new ShoulderEquipmentActionPayload(target, carriedBeforeClick));
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
                    : ClientShoulderEquipment.localDevice();
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
        return Component.empty();
    }

    private static Component label(ShoulderEquipmentActionPayload.Target target) {
        if (target == ShoulderEquipmentActionPayload.Target.STRAP) {
            return Component.translatable("gui.mirage_projector.equipment.shoulder_slot");
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
