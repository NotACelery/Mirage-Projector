package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.menu.MirageProjectorMenu;
import celerbi.mirageprojector.network.OpenEntityWorkspacePayload;
import celerbi.mirageprojector.network.OpenImageWorkspacePayload;
import celerbi.mirageprojector.network.OpenItemWorkspacePayload;
import celerbi.mirageprojector.network.UpdateProjectorPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * Primary projector workspace: global presentation + Core only.
 * Source import/capture/editing is intentionally delegated to dedicated workspaces.
 */
public final class MirageProjectorScreen extends AbstractContainerScreen<MirageProjectorMenu> {
    private final ProjectionSettings base;
    private int scalePixels;
    private int liftPixels;
    private boolean rotationEnabled;
    private int rotationPeriodTicks;
    private boolean clockwise;
    private float rotationOffsetDegrees;
    private boolean floatingEnabled;
    private ProjectionSettings.FloatMode floatMode;
    private int floatAmplitudePixels;
    private int floatCycleTicks;
    private int floatIntervalDegrees;
    private boolean fullbright;
    private int transparencyPercent;
    private int tintRgb;
    private boolean debugChassisOverride;

    private Button rotationButton;
    private Button directionButton;
    private Button orientationButton;
    private Button floatingButton;
    private Button floatModeButton;
    private Button lightingButton;
    private Button tintButton;
    private Button debugButton;
    private IntSlider floatTimingSlider;
    private ProjectionClearance.Result clearance = ProjectionClearance.Result.UNKNOWN;
    private long lastClearanceTick = Long.MIN_VALUE;

    public MirageProjectorScreen(MirageProjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 416;
        imageHeight = 468;
        inventoryLabelX = MirageProjectorMenu.PLAYER_INV_X;
        inventoryLabelY = MirageProjectorMenu.PLAYER_INV_Y - 12;
        base = menu.initialSettings();
        scalePixels = base.scalePixels();
        liftPixels = base.liftPixels();
        rotationEnabled = base.rotationEnabled();
        rotationPeriodTicks = base.rotationPeriodTicks();
        clockwise = base.clockwise();
        rotationOffsetDegrees = base.rotationOffsetDegrees();
        floatingEnabled = base.floatingEnabled();
        floatMode = base.floatMode();
        floatAmplitudePixels = base.floatAmplitudePixels();
        floatCycleTicks = base.floatCycleTicks();
        floatIntervalDegrees = base.floatIntervalDegrees();
        fullbright = base.fullbright();
        transparencyPercent = base.transparencyPercent();
        tintRgb = base.tintRgb();
        debugChassisOverride = base.debugChassisOverride();
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos;
        int y = topPos;
        int wide = imageWidth - 24;
        int half = (wide - 8) / 2;

        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.workspace.image"), button -> {
            saveSettings();
            PacketDistributor.sendToServer(new OpenImageWorkspacePayload(menu.projectorPos()));
        }).bounds(x + 12, y + 30, 122, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.workspace.item"), button -> {
            saveSettings();
            PacketDistributor.sendToServer(new OpenItemWorkspacePayload(menu.projectorPos()));
        }).bounds(x + 147, y + 30, 122, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.workspace.entity"), button -> {
            saveSettings();
            PacketDistributor.sendToServer(new OpenEntityWorkspacePayload(menu.projectorPos()));
        }).bounds(x + 282, y + 30, 122, 20).build());

        IntSlider scale = addRenderableWidget(new IntSlider(
                x + 12, y + 76, half, 20,
                ProjectionSettings.DEBUG_MIN_SCALE_PIXELS, ProjectionSettings.DEBUG_MAX_SCALE_PIXELS, scalePixels,
                value -> { scalePixels = value; updateClearance(true); },
                value -> Component.translatable("gui.mirage_projector.scale", value).getString()
        ));
        scale.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.scale")));

        IntSlider lift = addRenderableWidget(new IntSlider(
                x + 20 + half, y + 76, half, 20,
                0, ProjectionSettings.DEBUG_MAX_LIFT_PIXELS, liftPixels,
                value -> { liftPixels = value; updateClearance(true); },
                value -> Component.translatable("gui.mirage_projector.lift", value).getString()
        ));
        lift.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.lift")));

        rotationButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            rotationEnabled = !rotationEnabled; refreshLabels();
        }).bounds(x + 12, y + 118, half, 20).build());
        addRenderableWidget(new IntSlider(
                x + 20 + half, y + 118, half, 20,
                5, 1200, rotationPeriodTicks,
                value -> rotationPeriodTicks = value,
                value -> Component.translatable("gui.mirage_projector.rotation_period", String.format(Locale.ROOT, "%.2f", value / 20.0D)).getString()
        ));

        directionButton = addRenderableWidget(Button.builder(Component.empty(), button -> { clockwise = !clockwise; refreshLabels(); })
                .bounds(x + 12, y + 142, half, 20).build());
        orientationButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            rotationOffsetDegrees = wrap(rotationOffsetDegrees + 90.0F); refreshLabels();
        }).bounds(x + 20 + half, y + 142, half, 20).build());

        floatingButton = addRenderableWidget(Button.builder(Component.empty(), button -> { floatingEnabled = !floatingEnabled; refreshLabels(); })
                .bounds(x + 12, y + 176, half, 20).build());
        addRenderableWidget(new IntSlider(
                x + 20 + half, y + 176, half, 20,
                0, ProjectionSettings.DEBUG_MAX_FLOAT_PIXELS, floatAmplitudePixels,
                value -> { floatAmplitudePixels = value; updateClearance(true); },
                value -> Component.translatable("gui.mirage_projector.float_amplitude", value).getString()
        ));

        floatModeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            floatMode = floatMode == ProjectionSettings.FloatMode.TIME
                    ? ProjectionSettings.FloatMode.ROTATION_SYNCED : ProjectionSettings.FloatMode.TIME;
            refreshLabels(); refreshFloatTimingSlider();
        }).bounds(x + 12, y + 200, half, 20).build());
        floatTimingSlider = addRenderableWidget(new IntSlider(
                x + 20 + half, y + 200, half, 20,
                5, 1200, floatCycleTicks,
                value -> floatCycleTicks = value,
                value -> Component.translatable("gui.mirage_projector.float_cycle", String.format(Locale.ROOT, "%.2f", value / 20.0D)).getString()
        ));

        lightingButton = addRenderableWidget(Button.builder(Component.empty(), button -> { fullbright = !fullbright; refreshLabels(); })
                .bounds(x + 12, y + 234, half, 20).build());
        IntSlider ghost = addRenderableWidget(new IntSlider(
                x + 20 + half, y + 234, half, 20,
                0, 90, transparencyPercent,
                value -> transparencyPercent = value,
                value -> Component.translatable("gui.mirage_projector.ghost", value).getString()
        ));
        ghost.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.ghost")));

        tintButton = addRenderableWidget(Button.builder(Component.empty(), button -> { tintRgb = nextTint(tintRgb); refreshLabels(); })
                .bounds(x + 12, y + 258, half, 20).build());
        debugButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            if (minecraft != null && minecraft.player != null && minecraft.player.isCreative()) {
                debugChassisOverride = !debugChassisOverride; refreshLabels(); updateClearance(true);
            }
        }).bounds(x + 20 + half, y + 258, half, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.apply"), button -> {
            saveSettings(); onClose();
        }).bounds(x + 12, y + 438, half, 22).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.cancel"), button -> onClose())
                .bounds(x + 20 + half, y + 438, half, 22).build());

        refreshLabels();
        refreshFloatTimingSlider();
        updateClearance(true);
    }

    private void refreshFloatTimingSlider() {
        if (floatTimingSlider == null) return;
        if (floatMode == ProjectionSettings.FloatMode.TIME) {
            floatTimingSlider.reconfigure(5, 1200, floatCycleTicks,
                    value -> floatCycleTicks = value,
                    value -> Component.translatable("gui.mirage_projector.float_cycle", String.format(Locale.ROOT, "%.2f", value / 20.0D)).getString());
        } else {
            floatTimingSlider.reconfigure(1, 360, floatIntervalDegrees,
                    value -> floatIntervalDegrees = value,
                    value -> Component.translatable("gui.mirage_projector.float_leg", value).getString());
        }
    }

    private void refreshLabels() {
        if (rotationButton != null) rotationButton.setMessage(Component.translatable("gui.mirage_projector.rotation", onOff(rotationEnabled)));
        if (directionButton != null) directionButton.setMessage(Component.translatable("gui.mirage_projector.direction", Component.translatable(clockwise ? "gui.mirage_projector.clockwise" : "gui.mirage_projector.counterclockwise")));
        if (orientationButton != null) orientationButton.setMessage(Component.translatable("gui.mirage_projector.orientation", Math.round(rotationOffsetDegrees)));
        if (floatingButton != null) floatingButton.setMessage(Component.translatable("gui.mirage_projector.floating", onOff(floatingEnabled)));
        if (floatModeButton != null) floatModeButton.setMessage(Component.translatable("gui.mirage_projector.float_mode", Component.translatable(floatMode == ProjectionSettings.FloatMode.TIME ? "gui.mirage_projector.float_mode.time" : "gui.mirage_projector.float_mode.rotation")));
        if (lightingButton != null) lightingButton.setMessage(Component.translatable("gui.mirage_projector.lighting", Component.translatable(fullbright ? "gui.mirage_projector.lighting.fullbright" : "gui.mirage_projector.lighting.world")));
        if (tintButton != null) tintButton.setMessage(Component.translatable("gui.mirage_projector.tint", tintName(tintRgb)));
        if (debugButton != null) {
            debugButton.visible = minecraft != null && minecraft.player != null && minecraft.player.isCreative();
            debugButton.setMessage(Component.translatable("gui.mirage_projector.debug", onOff(debugChassisOverride)));
        }
    }

    private Component onOff(boolean value) { return Component.translatable(value ? "gui.mirage_projector.on" : "gui.mirage_projector.off"); }

    private ProjectionSettings buildSettings() {
        return base.withPresentation(
                scalePixels, liftPixels, rotationEnabled, rotationPeriodTicks, clockwise, rotationOffsetDegrees,
                floatingEnabled, floatMode, floatAmplitudePixels, floatCycleTicks, floatIntervalDegrees,
                fullbright, 100 - transparencyPercent, tintRgb, debugChassisOverride
        );
    }

    private void saveSettings() {
        PacketDistributor.sendToServer(new UpdateProjectorPayload(menu.projectorPos(), buildSettings()));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF014171D);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 2, 0xFF6B4A7E);
        section(graphics, x + 8, y + 20, imageWidth - 16, 40);
        section(graphics, x + 8, y + 62, imageWidth - 16, 40);
        section(graphics, x + 8, y + 104, imageWidth - 16, 56);
        section(graphics, x + 8, y + 162, imageWidth - 16, 56);
        section(graphics, x + 8, y + 220, imageWidth - 16, 60);
        section(graphics, x + 8, y + 282, imageWidth - 16, 56);
        drawSlotFrame(graphics, x + MirageProjectorMenu.CORE_SLOT_X - 1, y + MirageProjectorMenu.CORE_SLOT_Y - 1);
        ProjectionCoreProfile coreVisual = menu.coreProfile();
        if (coreVisual.present()) {
            drawSlotFrame(graphics, x + 183, y + 299);
            graphics.renderFakeItem(coreVisual.visualStack(), x + 184, y + 300);
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            drawSlotFrame(graphics, x + MirageProjectorMenu.PLAYER_INV_X + col * 18 - 1, y + MirageProjectorMenu.PLAYER_INV_Y + row * 18 - 1);
        for (int col = 0; col < 9; col++)
            drawSlotFrame(graphics, x + MirageProjectorMenu.PLAYER_INV_X + col * 18 - 1, y + MirageProjectorMenu.PLAYER_INV_Y + 58 - 1);
    }

    private static void section(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0x8A070A0E);
        graphics.fill(x, y, x + 2, y + h, 0xFF4C3858);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 10, 8, 0xFFF4F4F4, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.section.sources"), 12, 20, 0xFFBFA5D1, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.current_source", sourceName(base.sourceMode())), 12, 52, 0xFF9FBED1, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.section.geometry"), 12, 64, 0xFFBFA5D1, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.section.rotation"), 12, 106, 0xFFBFA5D1, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.section.floating"), 12, 164, 0xFFBFA5D1, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.section.appearance"), 12, 222, 0xFFBFA5D1, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.section.core"), 12, 284, 0xFFBFA5D1, false);

        ProjectionCoreProfile core = menu.coreProfile();
        ProjectionPower.Status power = ProjectionPower.evaluate(buildSettings(), core, menu.chassisProfile(), !menu.projectedItemStack().isEmpty());
        int infoX = 50;
        if (core.present()) {
            graphics.drawString(font, core.displayComponent(), infoX, 296, 0xFFD8E7FF, false);
            graphics.drawString(font, Component.translatable("gui.mirage_projector.core.power", core.power()), infoX, 307, 0xFF9FDBA9, false);
            graphics.drawString(font, Component.translatable("gui.mirage_projector.core.limits_compact", core.maxScalePixels(), core.maxLiftPixels(), core.maxFloatPixels()), infoX, 318, 0xFF9CA3AF, false);
        } else {
            graphics.drawString(font, Component.translatable("gui.mirage_projector.core.empty"), infoX, 300, 0xFFFFA0A0, false);
            graphics.drawString(font, Component.translatable("gui.mirage_projector.core.empty_hint"), infoX, 312, 0xFF9CA3AF, false);
        }
        int barX = 205, barY = 321, barW = 190;
        graphics.fill(barX, barY, barX + barW, barY + 8, 0xFF252A31);
        int fill = Math.round(barW * power.fillRatio());
        graphics.fill(barX, barY, barX + fill, barY + 8, power.active() ? 0xFF8FD19A : 0xFFFF8A73);
        graphics.drawString(font, Component.literal(power.usedPower() + " / " + power.availablePower() + " PU"), barX, 330, power.active() ? 0xFF8FD19A : 0xFFFFA87A, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.chassis", menu.chassisProfile().displayName()), 300, 296, 0xFFBFD8FF, false);

        if (clearance.known()) {
            Component c = clearance.clear()
                    ? Component.translatable("gui.mirage_projector.clearance.clear")
                    : Component.translatable("gui.mirage_projector.clearance.blocked", clearance.blockedBlocks());
            graphics.drawString(font, c, 205, 308, clearance.clear() ? 0xFF8FD19A : 0xFFFFA87A, false);
        }
        graphics.drawString(font, Component.translatable("container.inventory"), MirageProjectorMenu.PLAYER_INV_X, MirageProjectorMenu.PLAYER_INV_Y - 12, 0xFFBEB8C8, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderEmptyCoreTooltip(graphics, mouseX, mouseY);
    }

    private void renderEmptyCoreTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.coreStack().isEmpty()) return;
        int x = leftPos + MirageProjectorMenu.CORE_SLOT_X;
        int y = topPos + MirageProjectorMenu.CORE_SLOT_Y;
        if (mouseX < x || mouseX >= x + 18 || mouseY < y || mouseY >= y + 18) return;
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.mirage_projector.core.accepted"));
        for (ProjectionCoreProfile profile : ProjectionCoreProfile.values()) {
            if (!profile.present()) continue;
            lines.add(Component.translatable("tooltip.mirage_projector.core.entry", profile.displayComponent(), profile.power(), profile.maxScalePixels(), profile.maxLiftPixels(), profile.maxFloatPixels()));
        }
        graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateClearance(false);
    }

    private void updateClearance(boolean force) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) { clearance = ProjectionClearance.Result.UNKNOWN; return; }
        long tick = mc.level.getGameTime();
        if (!force && lastClearanceTick != Long.MIN_VALUE && tick - lastClearanceTick < 10) return;
        lastClearanceTick = tick;
        clearance = ProjectionClearance.scan(
                mc.level,
                menu.projectorPos(),
                buildSettings(),
                menu.chassisProfile(),
                !menu.projectedItemStack().isEmpty(),
                menu.activeHumanoidPose()
        );
        ProjectionClearancePreviewRenderer.show(menu.projectorPos(), clearance);
    }

    @Override public void onClose() { ProjectionClearancePreviewRenderer.clear(); super.onClose(); }
    @Override public void removed() { ProjectionClearancePreviewRenderer.clear(); super.removed(); }

    private static Component sourceName(ProjectionSettings.SourceMode mode) {
        return Component.translatable("gui.mirage_projector.source." + mode.name().toLowerCase());
    }

    private static int nextTint(int current) {
        int[] presets = {0xFFFFFF, 0x8FE8FF, 0xC7A4FF, 0xFF9BD7, 0xFFD27A, 0x9CFFB1, 0xFF9A9A};
        for (int i = 0; i < presets.length; i++) if ((current & 0xFFFFFF) == presets[i]) return presets[(i + 1) % presets.length];
        return presets[0];
    }

    private static Component tintName(int rgb) {
        String key = switch (rgb & 0xFFFFFF) {
            case 0xFFFFFF -> "white"; case 0x8FE8FF -> "cyan"; case 0xC7A4FF -> "amethyst";
            case 0xFF9BD7 -> "rose"; case 0xFFD27A -> "amber"; case 0x9CFFB1 -> "green"; case 0xFF9A9A -> "red";
            default -> null;
        };
        return key == null ? Component.literal(String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF))
                : Component.translatable("gui.mirage_projector.tint." + key);
    }

    private static float wrap(float degrees) {
        float wrapped = degrees % 360.0F;
        return wrapped < 0.0F ? wrapped + 360.0F : wrapped;
    }

    private static void drawSlotFrame(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF5A5361);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF171A20);
    }

    private static final class IntSlider extends AbstractSliderButton {
        private int min;
        private int max;
        private IntConsumer setter;
        private Function<Integer, String> formatter;
        private int current;

        private IntSlider(int x, int y, int width, int height, int min, int max, int current, IntConsumer setter, Function<Integer, String> formatter) {
            super(x, y, width, height, Component.empty(), normalize(current, min, max));
            this.min = min; this.max = max; this.setter = setter; this.formatter = formatter; this.current = clamp(current, min, max); updateMessage();
        }

        void reconfigure(int min, int max, int current, IntConsumer setter, Function<Integer, String> formatter) {
            this.min = min; this.max = max; this.setter = setter; this.formatter = formatter; this.current = clamp(current, min, max);
            this.value = normalize(this.current, min, max); updateMessage();
        }

        @Override protected void updateMessage() { if (formatter != null) setMessage(Component.literal(formatter.apply(current))); }
        @Override protected void applyValue() { current = min + (int)Math.round(value * (max - min)); current = clamp(current, min, max); setter.accept(current); updateMessage(); }
        private static double normalize(int value, int min, int max) { return max <= min ? 0.0D : (clamp(value, min, max) - min) / (double)(max - min); }
        private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    }
}
