package celerbi.mirageprojector.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Container-screen base that keeps tall projector GUIs usable on smaller effective GUI heights.
 *
 * <p>The complete container moves as one unit, so vanilla slot hitboxes remain aligned with the
 * rendered inventory while buttons and custom widgets are shifted by the same delta. A scrollbar is
 * shown only when the configured panel height cannot fit inside the current scaled window.</p>
 */
public abstract class ResponsiveContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private static final int VIEW_MARGIN = 8;
    private static final int SCROLL_STEP = 34;
    private static final int TRACK_WIDTH = 5;
    private static final int MIN_THUMB_HEIGHT = 24;

    private int contentScroll;
    private boolean draggingScrollbar;

    protected ResponsiveContainerScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int max = maxContentScroll();
        contentScroll = Math.max(0, Math.min(max, contentScroll));
        if (max > 0) {
            topPos = VIEW_MARGIN - contentScroll;
        }
    }

    protected void onContentScrolled(int deltaY) {
    }

    protected final boolean hasResponsiveScroll() {
        return maxContentScroll() > 0;
    }

    private int viewportHeight() {
        return Math.max(1, height - VIEW_MARGIN * 2);
    }

    private int maxContentScroll() {
        return Math.max(0, imageHeight - viewportHeight());
    }

    private void setContentScroll(int requested) {
        int max = maxContentScroll();
        int next = Math.max(0, Math.min(max, requested));
        if (next == contentScroll && (max == 0 || topPos == VIEW_MARGIN - next)) {
            return;
        }

        int oldTop = topPos;
        contentScroll = next;
        int newTop = max > 0 ? VIEW_MARGIN - contentScroll : (height - imageHeight) / 2;
        int delta = newTop - oldTop;
        topPos = newTop;

        if (delta != 0) {
            for (GuiEventListener child : children()) {
                if (child instanceof AbstractWidget widget) {
                    widget.setY(widget.getY() + delta);
                }
            }
            onContentScrolled(delta);
        }
    }

    private int scrollbarX() {
        return leftPos + imageWidth - 7;
    }

    private int thumbHeight() {
        int viewport = viewportHeight();
        return Math.max(MIN_THUMB_HEIGHT, Math.min(viewport,
                (int) Math.round(viewport * (viewport / (double) imageHeight))));
    }

    private int thumbY() {
        int max = maxContentScroll();
        int viewport = viewportHeight();
        int thumb = thumbHeight();
        int travel = Math.max(0, viewport - thumb);
        if (max <= 0 || travel <= 0) {
            return VIEW_MARGIN;
        }
        return VIEW_MARGIN + (int) Math.round(travel * (contentScroll / (double) max));
    }

    private boolean overScrollbar(double mouseX, double mouseY) {
        if (!hasResponsiveScroll()) {
            return false;
        }
        int x = scrollbarX();
        return mouseX >= x - 2 && mouseX < x + TRACK_WIDTH + 2
                && mouseY >= VIEW_MARGIN && mouseY < height - VIEW_MARGIN;
    }

    private void scrollFromPointer(double mouseY) {
        int max = maxContentScroll();
        if (max <= 0) {
            return;
        }
        int viewport = viewportHeight();
        int thumb = thumbHeight();
        int travel = Math.max(1, viewport - thumb);
        double relative = mouseY - VIEW_MARGIN - thumb / 2.0D;
        double fraction = Math.max(0.0D, Math.min(1.0D, relative / travel));
        setContentScroll((int) Math.round(max * fraction));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (hasResponsiveScroll() && mouseX >= leftPos && mouseX < leftPos + imageWidth) {
            int delta = (int) Math.round(-scrollY * SCROLL_STEP);
            if (delta != 0) {
                setContentScroll(contentScroll + delta);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && overScrollbar(mouseX, mouseY)) {
            draggingScrollbar = true;
            scrollFromPointer(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggingScrollbar) {
            scrollFromPointer(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderResponsiveScrollbar(graphics);
    }

    private void renderResponsiveScrollbar(GuiGraphics graphics) {
        if (!hasResponsiveScroll()) {
            return;
        }
        int x = scrollbarX();
        int bottom = height - VIEW_MARGIN;
        int thumbY = thumbY();
        int thumbHeight = thumbHeight();

        graphics.fill(x, VIEW_MARGIN, x + TRACK_WIDTH, bottom, 0xC0181B21);
        graphics.fill(x + 1, thumbY, x + TRACK_WIDTH - 1, thumbY + thumbHeight, 0xFF8B6A9C);
        graphics.fill(x + 1, thumbY, x + TRACK_WIDTH - 1, thumbY + 1, 0xFFC7A9D5);
    }
}
