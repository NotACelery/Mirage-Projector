package celerbi.mirageprojector.client;

import celerbi.mirageprojector.network.PresentationRemoteActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Create-inspired, hold-RMB three-position controller overlay. */
public final class PresentationRemoteScreen extends Screen {
    private final InteractionHand hand;
    private int selectedDirection;
    private boolean completed;
    private int ticksOpen;

    public PresentationRemoteScreen(InteractionHand hand) {
        super(Component.translatable("gui.mirage_projector.presentation_remote.title"));
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
    }

    @Override
    protected void init() {
        selectedDirection = 0;
        completed = false;
        ticksOpen = 0;
        long window = minecraft.getWindow().getWindow();
        // GLFW expects raw window pixels, not GUI-scaled screen coordinates. Using the latter
        // offset the cursor on any non-1 GUI scale and could preselect Previous immediately.
        GLFW.glfwSetCursorPos(
                window,
                minecraft.getWindow().getWidth() * 0.5D,
                minecraft.getWindow().getHeight() * 0.5D
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        ticksOpen++;
        if (completed || minecraft == null || ticksOpen <= 1) {
            return;
        }
        long window = minecraft.getWindow().getWindow();
        if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_RELEASE) {
            commitAndClose(selectedDirection);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Keep the world fully visible, matching Mirage's lightweight in-world controls.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        selectedDirection = selectionFor(mouseX);
        int boxW = 246;
        int boxH = 78;
        int x = (width - boxW) / 2;
        int y = (height - boxH) / 2;
        graphics.fill(x, y, x + boxW, y + boxH, 0xD914171D);
        graphics.fill(x + 1, y + 1, x + boxW - 1, y + 2, 0xFF7A4B91);
        graphics.drawCenteredString(font, title, width / 2, y + 10, 0xFFF4F4F4);

        int segmentW = 68;
        int gap = 8;
        int startX = width / 2 - (segmentW * 3 + gap * 2) / 2;
        drawChoice(graphics, startX, y + 30, segmentW, "<--", selectedDirection == -1);
        drawChoice(graphics, startX + segmentW + gap, y + 30, segmentW, "-", selectedDirection == 0);
        drawChoice(graphics, startX + (segmentW + gap) * 2, y + 30, segmentW, "-->", selectedDirection == 1);
        graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.presentation_remote.hold_hint"),
                width / 2, y + 60, 0xFFAFA7B8);
    }

    private void drawChoice(GuiGraphics graphics, int x, int y, int w, String label, boolean selected) {
        graphics.fill(x, y, x + w, y + 22, selected ? 0xFF79519A : 0xFF252A33);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 21, selected ? 0xFF3A2649 : 0xFF171B22);
        graphics.drawCenteredString(font, Component.literal(label), x + w / 2, y + 7,
                selected ? 0xFFFFFFFF : 0xFFB9B3C0);
    }

    private int selectionFor(double mouseX) {
        double delta = mouseX - width * 0.5D;
        if (delta < -40.0D) return -1;
        if (delta > 40.0D) return 1;
        return 0;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !completed) {
            commitAndClose(selectionFor(mouseX));
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void commitAndClose(int action) {
        if (completed) {
            return;
        }
        completed = true;
        if (action != 0) {
            PacketDistributor.sendToServer(new PresentationRemoteActionPayload(hand, action));
        }
        super.onClose();
    }

    @Override
    public void onClose() {
        completed = true;
        super.onClose();
    }
}
