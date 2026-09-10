package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.menu.BannerProjectorMenu;
import celerbi.mirageprojector.network.BannerWorkspaceActionPayload;
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

/** Dedicated Banner source workspace with virtual Plane/Prism face snapshots. */
public final class BannerProjectorScreen extends AbstractContainerScreen<BannerProjectorMenu> {
    private static final int W = 420;
    private static final int H = 300;

    public BannerProjectorScreen(BannerProjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = W;
        imageHeight = H;
        inventoryLabelX = BannerProjectorMenu.PLAYER_INV_X;
        inventoryLabelY = BannerProjectorMenu.PLAYER_INV_Y - 12;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.back_to_settings"), button ->
                PacketDistributor.sendToServer(new OpenProjectorWorkspacePayload(menu.projectorPos()))
        ).bounds(leftPos + 278, topPos + 8, 130, 18).build());

        Button activate = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.banner.activate"), button ->
                PacketDistributor.sendToServer(new SetProjectionSourcePayload(menu.projectorPos(), ProjectionSettings.SourceMode.BANNER))
        ).bounds(leftPos + 22, topPos + 136, 118, 20).build());
        activate.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.banner.activate")));

        Button clear = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.banner.clear_all"), button ->
                PacketDistributor.sendToServer(new BannerWorkspaceActionPayload(menu.projectorPos(), BannerWorkspaceActionPayload.Action.CLEAR_ALL))
        ).bounds(leftPos + 148, topPos + 136, 104, 20).build());
        clear.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.banner.clear_all")));

        if (menu.prism()) {
            Button sameAll = addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.banner.same_all"), button ->
                    PacketDistributor.sendToServer(new BannerWorkspaceActionPayload(menu.projectorPos(), BannerWorkspaceActionPayload.Action.COPY_PRIMARY_TO_ALL))
            ).bounds(leftPos + 260, topPos + 136, 138, 20).build());
            sameAll.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.banner.same_all")));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF014171D);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 2, 0xFF6B4A7E);

        section(graphics, x + 12, y + 34, 396, 132);
        section(graphics, x + 12, y + 174, 396, 114);

        int faceCount = menu.prism() ? BannerProjectorMenu.FACE_COUNT : 1;
        for (int face = 0; face < faceCount; face++) {
            int sx = menu.prism() ? BannerProjectorMenu.FACE_X[face] : BannerProjectorMenu.PLANE_FACE_X;
            drawSlotFrame(graphics, x + sx - 1, y + BannerProjectorMenu.FACE_Y - 1);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotFrame(graphics,
                        x + BannerProjectorMenu.PLAYER_INV_X + col * 18 - 1,
                        y + BannerProjectorMenu.PLAYER_INV_Y + row * 18 - 1);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotFrame(graphics,
                    x + BannerProjectorMenu.PLAYER_INV_X + col * 18 - 1,
                    y + BannerProjectorMenu.PLAYER_INV_Y + 58 - 1);
        }

        ItemStack primary = menu.bannerSnapshot(0);
        if (!primary.isEmpty()) {
            graphics.pose().pushPose();
            graphics.pose().translate(x + 344, y + 80, 80.0F);
            graphics.pose().scale(2.0F, 2.0F, 1.0F);
            graphics.renderItem(primary, -8, -8);
            graphics.pose().popPose();
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, fit(title.getString(), 250), 12, 10, 0xFFF4F4F4, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.banner.sources"), 22, 44, 0xFFD7B8F5, false);

        if (menu.prism()) {
            String[] labels = {"north", "east", "south", "west"};
            for (int face = 0; face < labels.length; face++) {
                Component label = Component.translatable("gui.mirage_projector.face." + labels[face]);
                graphics.drawCenteredString(font, label, BannerProjectorMenu.FACE_X[face] + 8, 84, 0xFFC9CED7);
            }
        } else {
            graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.banner.front"), BannerProjectorMenu.PLANE_FACE_X + 8, 84, 0xFFC9CED7);
        }

        graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.banner.preview"), 344, 48, 0xFFD7B8F5);
        ItemStack primary = menu.bannerSnapshot(0);
        Component previewName = primary.isEmpty()
                ? Component.translatable("gui.mirage_projector.banner.empty")
                : primary.getHoverName();
        graphics.drawCenteredString(font, fit(previewName.getString(), 112), 344, 112, primary.isEmpty() ? 0xFF8D8493 : 0xFF9DDBA8);

        graphics.drawString(font, Component.translatable("gui.mirage_projector.banner.virtual_notice"), 22, 104, 0xFF8FCFA0, false);
        graphics.drawString(font, Component.translatable("container.inventory"), BannerProjectorMenu.PLAYER_INV_X, BannerProjectorMenu.PLAYER_INV_Y - 12, 0xFFBEB8C8, false);
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
        if (value == null || value.isEmpty() || font.width(value) <= maxWidth) return value == null ? "" : value;
        String ellipsis = "…";
        int target = Math.max(0, maxWidth - font.width(ellipsis));
        int end = value.length();
        while (end > 0 && font.width(value.substring(0, end)) > target) end--;
        return value.substring(0, Math.max(0, end)) + ellipsis;
    }
}
