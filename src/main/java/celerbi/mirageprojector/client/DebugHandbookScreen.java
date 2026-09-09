package celerbi.mirageprojector.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * Development handbook using the vanilla book renderer as the page surface,
 * but presented as a larger centred manual with lightweight category tabs.
 *
 * <p>The tab model intentionally mirrors the useful part of guide-book mods:
 * one stable section per physical Mirage chassis instead of one long linear
 * document. No external guide-book dependency is required for the debug item.</p>
 */
public final class DebugHandbookScreen extends BookViewScreen {
    private static final int TAB_WIDTH = 58;
    private static final int TAB_HEIGHT = 18;
    private static final int TAB_GAP = 2;
    private static final int TAB_TEXT = 0xFFF4E9D0;
    private static final int TAB_TEXT_SELECTED = 0xFFFFFFFF;
    private static final int TAB_BG = 0xCC3A244A;
    private static final int TAB_BG_HOVER = 0xE04C3161;
    private static final int TAB_BG_SELECTED = 0xF06B3B83;
    private static final int SCREEN_DIM = 0x74000000;

    private static final Section[] SECTIONS = {
            new Section("general", "handbook.mirage_projector.section.general.title", new String[]{
                    "handbook.mirage_projector.section.general.1",
                    "handbook.mirage_projector.section.general.2",
                    "handbook.mirage_projector.section.general.3",
                    "handbook.mirage_projector.section.general.4",
                    "handbook.mirage_projector.section.general.5",
                    "handbook.mirage_projector.section.general.6",
                    "handbook.mirage_projector.section.general.7"
            }),
            new Section("compact", "handbook.mirage_projector.section.compact.title", new String[]{
                    "handbook.mirage_projector.section.compact.1",
                    "handbook.mirage_projector.section.compact.2",
                    "handbook.mirage_projector.section.compact.3"
            }),
            new Section("display", "handbook.mirage_projector.section.display.title", new String[]{
                    "handbook.mirage_projector.section.display.1",
                    "handbook.mirage_projector.section.display.2",
                    "handbook.mirage_projector.section.display.3"
            }),
            new Section("wide", "handbook.mirage_projector.section.wide.title", new String[]{
                    "handbook.mirage_projector.section.wide.1",
                    "handbook.mirage_projector.section.wide.2",
                    "handbook.mirage_projector.section.wide.3"
            }),
            new Section("tall", "handbook.mirage_projector.section.tall.title", new String[]{
                    "handbook.mirage_projector.section.tall.1",
                    "handbook.mirage_projector.section.tall.2",
                    "handbook.mirage_projector.section.tall.3"
            }),
            new Section("field", "handbook.mirage_projector.section.field.title", new String[]{
                    "handbook.mirage_projector.section.field.1",
                    "handbook.mirage_projector.section.field.2",
                    "handbook.mirage_projector.section.field.3"
            }),
            new Section("prism", "handbook.mirage_projector.section.prism.title", new String[]{
                    "handbook.mirage_projector.section.prism.1",
                    "handbook.mirage_projector.section.prism.2",
                    "handbook.mirage_projector.section.prism.3"
            })
    };

    private int selectedSection;

    private DebugHandbookScreen() {
        super(new BookAccess(buildPages(SECTIONS[0])));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new DebugHandbookScreen());
    }

    /**
     * Keep Esc as the close gesture and reserve the area below/right of the
     * enlarged book for the category treatment rather than the vanilla 200px
     * Done button.
     */
    @Override
    protected void createMenuControls() {
    }

    /**
     * BookViewScreen normally positions its 192px texture close to the top of
     * the screen. We keep its page renderer and buttons but move/scale that
     * logical 192x192 surface around the actual screen centre.
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, SCREEN_DIM);

        float scale = bookScale();
        int logicalMouseX = Mth.floor(inverseMouseX(mouseX, scale));
        int logicalMouseY = Mth.floor(inverseMouseY(mouseY, scale));

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(this.width * 0.5F, this.height * 0.5F, 0.0F);
        poseStack.scale(scale, scale, 1.0F);
        poseStack.translate(-this.width * 0.5F, -baseBookCenterY(), 0.0F);
        super.render(guiGraphics, logicalMouseX, logicalMouseY, partialTick);
        poseStack.popPose();

        renderSectionTabs(guiGraphics, mouseX, mouseY, scale);
    }

    /** BookViewScreen calls this from render(); the local dim is drawn above. */
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int clickedSection = sectionAt(mouseX, mouseY, bookScale());
            if (clickedSection >= 0) {
                selectSection(clickedSection);
                return true;
            }
        }

        float scale = bookScale();
        return super.mouseClicked(
                inverseMouseX(mouseX, scale),
                inverseMouseY(mouseY, scale),
                button
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void selectSection(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= SECTIONS.length || sectionIndex == selectedSection) {
            return;
        }
        selectedSection = sectionIndex;
        setBookAccess(new BookAccess(buildPages(SECTIONS[selectedSection])));
        setPage(0);
    }

    private void renderSectionTabs(GuiGraphics guiGraphics, int mouseX, int mouseY, float scale) {
        int startX = tabX(scale);
        int startY = tabStartY();

        for (int i = 0; i < SECTIONS.length; i++) {
            int x = startX + (i == selectedSection ? -4 : 0);
            int y = startY + i * (TAB_HEIGHT + TAB_GAP);
            boolean hovered = mouseX >= x && mouseX < x + TAB_WIDTH
                    && mouseY >= y && mouseY < y + TAB_HEIGHT;

            int background = i == selectedSection
                    ? TAB_BG_SELECTED
                    : hovered ? TAB_BG_HOVER : TAB_BG;
            guiGraphics.fill(x, y, x + TAB_WIDTH, y + TAB_HEIGHT, background);
            guiGraphics.fill(x, y, x + 2, y + TAB_HEIGHT, 0xFF201329);

            Component label = Component.translatable("handbook.mirage_projector.tab." + SECTIONS[i].id());
            int textColor = i == selectedSection ? TAB_TEXT_SELECTED : TAB_TEXT;
            guiGraphics.drawString(this.font, label, x + 6, y + 5, textColor, false);
        }
    }

    private int sectionAt(double mouseX, double mouseY, float scale) {
        int startX = tabX(scale);
        int startY = tabStartY();
        for (int i = 0; i < SECTIONS.length; i++) {
            int x = startX + (i == selectedSection ? -4 : 0);
            int y = startY + i * (TAB_HEIGHT + TAB_GAP);
            if (mouseX >= x && mouseX < x + TAB_WIDTH
                    && mouseY >= y && mouseY < y + TAB_HEIGHT) {
                return i;
            }
        }
        return -1;
    }

    private int tabX(float scale) {
        int bookRight = Mth.floor(this.width * 0.5F + IMAGE_WIDTH * scale * 0.5F);
        return Math.min(bookRight - 3, this.width - TAB_WIDTH - 4);
    }

    private int tabStartY() {
        int totalHeight = SECTIONS.length * TAB_HEIGHT + (SECTIONS.length - 1) * TAB_GAP;
        return (this.height - totalHeight) / 2;
    }

    private float bookScale() {
        float vertical = (this.height - 42.0F) / IMAGE_HEIGHT;
        float horizontal = (this.width - TAB_WIDTH - 28.0F) / IMAGE_WIDTH;
        return Mth.clamp(Math.min(vertical, horizontal), 1.20F, 1.80F);
    }

    private static float baseBookCenterY() {
        // Vanilla BookViewScreen draws BOOK_LOCATION at y=2.
        return 2.0F + IMAGE_HEIGHT * 0.5F;
    }

    private double inverseMouseX(double mouseX, float scale) {
        return this.width * 0.5D + (mouseX - this.width * 0.5D) / scale;
    }

    private double inverseMouseY(double mouseY, float scale) {
        return baseBookCenterY() + (mouseY - this.height * 0.5D) / scale;
    }

    /** One explanatory paragraph per physical page keeps translations safe. */
    private static List<Component> buildPages(Section section) {
        Component heading = Component.translatable(section.titleKey()).copy()
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
        List<Component> pages = new ArrayList<>(section.pageKeys().length);
        for (String pageKey : section.pageKeys()) {
            MutableComponent page = Component.empty()
                    .append(heading.copy())
                    .append("\n\n")
                    .append(Component.translatable(pageKey));
            pages.add(page);
        }
        return List.copyOf(pages);
    }

    private record Section(String id, String titleKey, String[] pageKeys) {
    }
}
