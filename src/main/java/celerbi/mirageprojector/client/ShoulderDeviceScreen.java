package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.item.MirageLanternItem;
import celerbi.mirageprojector.network.ShoulderDeviceControlPayload;
import celerbi.mirageprojector.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Compact configuration screen for the device mounted in the Shoulder Slot. */
public final class ShoulderDeviceScreen extends Screen {
    private static final int PANEL_WIDTH = 236;
    private static final int BASE_PANEL_HEIGHT = 126;
    private static final int BANNER_PANEL_HEIGHT = 226;

    private final Screen parent;
    private Button presentationButton;
    private Button facingButton;
    private Button sizeDownButton;
    private Button sizeUpButton;
    private Button heightDownButton;
    private Button heightUpButton;

    public ShoulderDeviceScreen(Screen parent) {
        super(Component.translatable("gui.mirage_projector.shoulder_device.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        ItemStack device = ClientShoulderEquipment.localDevice();
        int panelHeight = panelHeight(device);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - panelHeight) / 2;

        if (device.is(ModItems.MIRAGE_LANTERN.get())) {
            addRenderableWidget(Button.builder(
                    Component.translatable("gui.mirage_projector.shoulder_device.cycle_mode"),
                    button -> send(ShoulderDeviceControlPayload.Action.CYCLE_LANTERN_MODE)
            ).bounds(left + 20, top + 58, PANEL_WIDTH - 40, 20).build());
        } else if (device.is(ModItems.MIRAGE_HAND_PROJECTOR.get())) {
            addRenderableWidget(Button.builder(
                    Component.translatable("gui.mirage_projector.shoulder_device.toggle_projection"),
                    button -> send(ShoulderDeviceControlPayload.Action.TOGGLE_PROJECTOR)
            ).bounds(left + 20, top + 58, PANEL_WIDTH - 40, 20).build());

            if (MirageHandProjectorItem.sourceMode(device) == ProjectionSettings.SourceMode.BANNER) {
                presentationButton = addRenderableWidget(Button.builder(
                        presentationMessage(device),
                        button -> send(ShoulderDeviceControlPayload.Action.CYCLE_BANNER_PRESENTATION)
                ).bounds(left + 20, top + 84, PANEL_WIDTH - 40, 20).build());

                facingButton = addRenderableWidget(Button.builder(
                        facingMessage(device),
                        button -> send(ShoulderDeviceControlPayload.Action.CYCLE_WAR_BANNER_FACING)
                ).bounds(left + 20, top + 108, PANEL_WIDTH - 40, 20).build());

                sizeDownButton = addRenderableWidget(Button.builder(
                        Component.literal("-"),
                        button -> send(ShoulderDeviceControlPayload.Action.WAR_BANNER_SIZE_DOWN)
                ).bounds(left + 128, top + 134, 28, 20).build());
                sizeUpButton = addRenderableWidget(Button.builder(
                        Component.literal("+"),
                        button -> send(ShoulderDeviceControlPayload.Action.WAR_BANNER_SIZE_UP)
                ).bounds(left + 188, top + 134, 28, 20).build());

                heightDownButton = addRenderableWidget(Button.builder(
                        Component.literal("-"),
                        button -> send(ShoulderDeviceControlPayload.Action.WAR_BANNER_HEIGHT_DOWN)
                ).bounds(left + 128, top + 160, 28, 20).build());
                heightUpButton = addRenderableWidget(Button.builder(
                        Component.literal("+"),
                        button -> send(ShoulderDeviceControlPayload.Action.WAR_BANNER_HEIGHT_UP)
                ).bounds(left + 188, top + 160, 28, 20).build());
            }
        }

        addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.shoulder_device.back"),
                button -> onClose()
        ).bounds(left + 20, top + panelHeight - 36, PANEL_WIDTH - 40, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ItemStack device = ClientShoulderEquipment.localDevice();
        int panelHeight = panelHeight(device);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - panelHeight) / 2;
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(left, top, left + PANEL_WIDTH, top + panelHeight, 0xE0181514);
        graphics.fill(left + 2, top + 2, left + PANEL_WIDTH - 2, top + panelHeight - 2, 0xE02C211B);
        graphics.drawCenteredString(font, title, width / 2, top + 10, 0xFFF0D8C4);

        if (!device.isEmpty()) {
            graphics.renderItem(device, left + 20, top + 28);
            graphics.drawString(font, device.getHoverName(), left + 43, top + 32, 0xFFFFFFFF, false);
            graphics.drawString(font, status(device), left + 43, top + 43, 0xFFCDB8A7, false);
        } else {
            graphics.drawCenteredString(
                    font,
                    Component.translatable("gui.mirage_projector.shoulder_device.empty"),
                    width / 2,
                    top + 36,
                    0xFFB6A9A0
            );
        }

        updateBannerControls(device);
        if (presentationButton != null) {
            graphics.drawString(
                    font,
                    Component.translatable(
                            "gui.mirage_projector.shoulder_device.war_banner_size",
                            MirageHandProjectorItem.warBannerSizePercent(device)
                    ),
                    left + 20,
                    top + 140,
                    0xFFCDB8A7,
                    false
            );
            graphics.drawString(
                    font,
                    Component.translatable(
                            "gui.mirage_projector.shoulder_device.war_banner_height",
                            MirageHandProjectorItem.warBannerHeightPixels(device)
                    ),
                    left + 20,
                    top + 166,
                    0xFFCDB8A7,
                    false
            );
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private void updateBannerControls(ItemStack device) {
        if (presentationButton == null) {
            return;
        }
        presentationButton.setMessage(presentationMessage(device));
        boolean warBanner = MirageHandProjectorItem.warBannerActive(device);
        facingButton.active = warBanner;
        facingButton.setMessage(facingMessage(device));
        sizeDownButton.active = warBanner
                && MirageHandProjectorItem.warBannerSizePercent(device) > MirageHandProjectorItem.WAR_BANNER_MIN_SIZE_PERCENT;
        sizeUpButton.active = warBanner
                && MirageHandProjectorItem.warBannerSizePercent(device) < MirageHandProjectorItem.WAR_BANNER_MAX_SIZE_PERCENT;
        heightDownButton.active = warBanner
                && MirageHandProjectorItem.warBannerHeightPixels(device) > MirageHandProjectorItem.WAR_BANNER_MIN_HEIGHT_PIXELS;
        heightUpButton.active = warBanner
                && MirageHandProjectorItem.warBannerHeightPixels(device) < MirageHandProjectorItem.WAR_BANNER_MAX_HEIGHT_PIXELS;

    }

    private static Component presentationMessage(ItemStack device) {
        return Component.translatable(
                "gui.mirage_projector.shoulder_device.banner_presentation",
                Component.translatable(MirageHandProjectorItem.bannerPresentation(device).translationKey())
        );
    }

    private static Component facingMessage(ItemStack device) {
        return Component.translatable(
                "gui.mirage_projector.shoulder_device.war_banner_facing",
                Component.translatable(MirageHandProjectorItem.warBannerFacing(device).translationKey())
        );
    }

    private static Component status(ItemStack device) {
        if (device.getItem() instanceof MirageLanternItem) {
            return MirageLanternItem.hudComponent(device);
        }
        if (device.getItem() instanceof MirageHandProjectorItem) {
            return MirageHandProjectorItem.hudComponent(device);
        }
        return Component.empty();
    }

    private static int panelHeight(ItemStack device) {
        return device.is(ModItems.MIRAGE_HAND_PROJECTOR.get())
                && MirageHandProjectorItem.sourceMode(device) == ProjectionSettings.SourceMode.BANNER
                ? BANNER_PANEL_HEIGHT
                : BASE_PANEL_HEIGHT;
    }

    private static void send(ShoulderDeviceControlPayload.Action action) {
        PacketDistributor.sendToServer(new ShoulderDeviceControlPayload(action));
    }
}
