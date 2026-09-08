package celerbi.mirageprojector.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/** Temporary debug handbook. Every visible line is a translatable component resolved in the active language. */
public final class DebugHandbookScreen extends Screen {
    private static final String[][] PAGES = {
            {"handbook.mirage_projector.page.overview.title", "handbook.mirage_projector.page.overview.1", "handbook.mirage_projector.page.overview.2", "handbook.mirage_projector.page.overview.3", "handbook.mirage_projector.page.overview.4"},
            {"handbook.mirage_projector.page.core.title", "handbook.mirage_projector.page.core.1", "handbook.mirage_projector.page.core.2", "handbook.mirage_projector.page.core.3", "handbook.mirage_projector.page.core.4", "handbook.mirage_projector.page.core.5"},
            {"handbook.mirage_projector.page.image.title", "handbook.mirage_projector.page.image.1", "handbook.mirage_projector.page.image.2", "handbook.mirage_projector.page.image.3", "handbook.mirage_projector.page.image.4"},
            {"handbook.mirage_projector.page.item.title", "handbook.mirage_projector.page.item.1", "handbook.mirage_projector.page.item.2", "handbook.mirage_projector.page.item.3"},
            {"handbook.mirage_projector.page.entity.title", "handbook.mirage_projector.page.entity.1", "handbook.mirage_projector.page.entity.2", "handbook.mirage_projector.page.entity.3", "handbook.mirage_projector.page.entity.4", "handbook.mirage_projector.page.entity.5", "handbook.mirage_projector.page.entity.6"},
            {"handbook.mirage_projector.page.controls.title", "handbook.mirage_projector.page.controls.1", "handbook.mirage_projector.page.controls.2", "handbook.mirage_projector.page.controls.3", "handbook.mirage_projector.page.controls.4"},
            {"handbook.mirage_projector.page.dev.title", "handbook.mirage_projector.page.dev.1", "handbook.mirage_projector.page.dev.2", "handbook.mirage_projector.page.dev.3", "handbook.mirage_projector.page.dev.4"}
    };

    private int page;
    private Button previous;
    private Button next;

    private DebugHandbookScreen() {
        super(Component.translatable("item.mirage_projector.debug_handbook"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new DebugHandbookScreen());
    }

    @Override
    protected void init() {
        previous = addRenderableWidget(Button.builder(Component.literal("<"), button -> { if (page > 0) { page--; refresh(); } })
                .bounds(width / 2 - 88, height / 2 + 104, 36, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"), button -> { if (page + 1 < PAGES.length) { page++; refresh(); } })
                .bounds(width / 2 + 52, height / 2 + 104, 36, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 - 45, height / 2 + 104, 90, 20).build());
        refresh();
    }

    private void refresh() {
        if (previous != null) previous.active = page > 0;
        if (next != null) next.active = page + 1 < PAGES.length;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int panelX = width / 2 - 170;
        int panelY = height / 2 - 126;
        graphics.fill(panelX, panelY, panelX + 340, panelY + 246, 0xF0181511);
        graphics.fill(panelX + 2, panelY + 2, panelX + 338, panelY + 4, 0xFF9B774B);
        graphics.drawCenteredString(font, title, width / 2, panelY + 12, 0xFFF0E2C6);
        graphics.drawCenteredString(font, Component.translatable(PAGES[page][0]), width / 2, panelY + 31, 0xFFFFD79A);

        int y = panelY + 51;
        for (int i = 1; i < PAGES[page].length; i++) {
            List<FormattedCharSequence> lines = font.split(Component.translatable(PAGES[page][i]), 306);
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, panelX + 17, y, 0xFFE7E0D4, false);
                y += 11;
            }
            y += 5;
        }
        graphics.drawCenteredString(font, Component.translatable("handbook.mirage_projector.page_counter", page + 1, PAGES.length), width / 2, panelY + 226, 0xFF9E9485);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
