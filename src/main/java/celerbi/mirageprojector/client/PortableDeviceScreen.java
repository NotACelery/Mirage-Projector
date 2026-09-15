package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.item.MirageLanternItem;
import celerbi.mirageprojector.menu.PortableDeviceMenu;
import celerbi.mirageprojector.menu.PortableDeviceSource;
import celerbi.mirageprojector.network.PortableDeviceActionPayload;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Real battery/configuration GUI for the handheld and shoulder-mounted portable devices. */
public final class PortableDeviceScreen extends AbstractContainerScreen<PortableDeviceMenu> {
    private static final int WIDTH = 196;
    private static final int HEIGHT = 274;

    public PortableDeviceScreen(PortableDeviceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        inventoryLabelX = PortableDeviceMenu.PLAYER_INV_X;
        inventoryLabelY = PortableDeviceMenu.PLAYER_INV_Y - 12;
    }

    @Override
    protected void init() {
        super.init();
        ItemStack device = currentDevice();
        int x = leftPos + 66;
        int y = topPos + 28;
        int w = 112;

        if (device.is(ModItems.MIRAGE_LANTERN.get())) {
            addRenderableWidget(Button.builder(
                    Component.translatable("gui.mirage_projector.portable_device.cycle_mode"),
                    button -> send(PortableDeviceActionPayload.Action.CYCLE_LANTERN_MODE)
            ).bounds(x, y, w, 20).build());
        } else if (device.is(ModItems.MIRAGE_HAND_PROJECTOR.get())) {
            addRenderableWidget(Button.builder(
                    Component.translatable("gui.mirage_projector.portable_device.toggle_projection"),
                    button -> send(PortableDeviceActionPayload.Action.TOGGLE_PROJECTOR)
            ).bounds(x, y, w, 20).build());
            addRenderableWidget(Button.builder(
                    Component.translatable("gui.mirage_projector.portable_device.copy_target"),
                    button -> send(PortableDeviceActionPayload.Action.COPY_TARGET_PROJECTOR)
            ).bounds(x, y + 24, w, 20).build());

            boolean banner = MirageHandProjectorItem.sourceMode(device) == ProjectionSettings.SourceMode.BANNER;
            if (banner) {
                addRenderableWidget(Button.builder(
                        Component.translatable(
                                "gui.mirage_projector.portable_device.banner_presentation",
                                Component.translatable(MirageHandProjectorItem.bannerPresentation(device).translationKey())
                        ),
                        button -> send(PortableDeviceActionPayload.Action.CYCLE_BANNER_PRESENTATION)
                ).bounds(x, y + 48, w, 20).build());
                if (MirageHandProjectorItem.warBannerActive(device)) {
                    addRenderableWidget(Button.builder(
                            Component.translatable(
                                    "gui.mirage_projector.portable_device.war_banner_facing",
                                    Component.translatable(MirageHandProjectorItem.warBannerFacing(device).translationKey())
                            ),
                            button -> send(PortableDeviceActionPayload.Action.CYCLE_WAR_BANNER_FACING)
                    ).bounds(x, y + 72, w, 20).build());
                    addRenderableWidget(Button.builder(Component.literal("Size -"), b -> send(PortableDeviceActionPayload.Action.WAR_BANNER_SIZE_DOWN))
                            .bounds(x, y + 96, 54, 20).build());
                    addRenderableWidget(Button.builder(Component.literal("Size +"), b -> send(PortableDeviceActionPayload.Action.WAR_BANNER_SIZE_UP))
                            .bounds(x + 58, y + 96, 54, 20).build());
                    addRenderableWidget(Button.builder(Component.literal("Height -"), b -> send(PortableDeviceActionPayload.Action.WAR_BANNER_HEIGHT_DOWN))
                            .bounds(x, y + 120, 54, 20).build());
                    addRenderableWidget(Button.builder(Component.literal("Height +"), b -> send(PortableDeviceActionPayload.Action.WAR_BANNER_HEIGHT_UP))
                            .bounds(x + 58, y + 120, 54, 20).build());
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF014171D);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 3, 0xFF72528D);
        graphics.fill(x + 12, y + 25, x + 58, y + 80, 0xA20B0E13);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.portable_device.battery"), x + 18, y + 31, 0xFFD7B8F5, false);
        slotFrame(graphics, x + PortableDeviceMenu.BATTERY_X - 1, y + PortableDeviceMenu.BATTERY_Y - 1, 0xFF7954A0);

        graphics.fill(x + 10, y + PortableDeviceMenu.PLAYER_INV_Y - 17, x + imageWidth - 10, y + imageHeight - 8, 0xA20B0E13);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slotFrame(graphics,
                        x + PortableDeviceMenu.PLAYER_INV_X + col * 18 - 1,
                        y + PortableDeviceMenu.PLAYER_INV_Y + row * 18 - 1,
                        0xFF4D4A52);
            }
        }
        for (int col = 0; col < 9; col++) {
            slotFrame(graphics,
                    x + PortableDeviceMenu.PLAYER_INV_X + col * 18 - 1,
                    y + PortableDeviceMenu.PLAYER_INV_Y + 58 - 1,
                    0xFF4D4A52);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, currentDevice().getHoverName(), 10, 9, 0xFFF4F4F4, false);
        graphics.drawString(font, Component.translatable("container.inventory"), inventoryLabelX, inventoryLabelY, 0xFFBEB8C8, false);
        ItemStack device = currentDevice();
        if (device.getItem() instanceof MirageLanternItem) {
            graphics.drawString(font,
                    Component.translatable("gui.mirage_projector.portable_device.mode", Component.translatable(MirageLanternItem.mode(device).displayTranslationKey())),
                    66, 12, 0xFFD7B8F5, false);
        } else if (device.getItem() instanceof MirageHandProjectorItem) {
            Component state = Component.translatable(MirageHandProjectorItem.projectionEnabled(device)
                    ? "gui.mirage_projector.portable_device.on"
                    : "gui.mirage_projector.portable_device.off");
            graphics.drawString(font, state, 66, 12, 0xFFD7B8F5, false);
        }
    }

    private ItemStack currentDevice() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return ItemStack.EMPTY;
        return switch (menu.source()) {
            case MAIN_HAND -> minecraft.player.getMainHandItem();
            case OFF_HAND -> minecraft.player.getOffhandItem();
            case SHOULDER -> ClientShoulderEquipment.localDevice();
        };
    }

    private void send(PortableDeviceActionPayload.Action action) {
        PacketDistributor.sendToServer(new PortableDeviceActionPayload(menu.source(), action));
    }

    private static void slotFrame(GuiGraphics graphics, int x, int y, int border) {
        graphics.fill(x, y, x + 18, y + 18, border);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF171A20);
    }
}
