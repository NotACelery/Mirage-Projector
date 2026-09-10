package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.menu.MirageProjectorMenu;
import celerbi.mirageprojector.network.OpenBannerWorkspacePayload;
import celerbi.mirageprojector.network.OpenEntityWorkspacePayload;
import celerbi.mirageprojector.network.OpenImageWorkspacePayload;
import celerbi.mirageprojector.network.OpenItemWorkspacePayload;
import celerbi.mirageprojector.network.UpdateProjectorPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

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
    private IntSlider scaleSlider;
    private IntSlider liftSlider;
    private IntSlider floatAmplitudeSlider;
    private IntSlider floatTimingSlider;
    private int scaleLimit;
    private int liftLimit;
    private int floatLimit;
    private ProjectionCoreProfile observedCore = ProjectionCoreProfile.NONE;
    private ProjectionClearance.Result clearance = ProjectionClearance.Result.UNKNOWN;
    private long lastClearanceTick = Long.MIN_VALUE;

    public MirageProjectorScreen(MirageProjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 416;
        imageHeight = 500;
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

        int sourceButtonWidth = 94;
        int sourceGap = 5;
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.workspace.image_short"), button -> {
            saveSettings();
            PacketDistributor.sendToServer(new OpenImageWorkspacePayload(menu.projectorPos()));
        }).bounds(x + 12, y + 30, sourceButtonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.workspace.item_short"), button -> {
            saveSettings();
            PacketDistributor.sendToServer(new OpenItemWorkspacePayload(menu.projectorPos()));
        }).bounds(x + 12 + sourceButtonWidth + sourceGap, y + 30, sourceButtonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.workspace.entity_short"), button -> {
            saveSettings();
            PacketDistributor.sendToServer(new OpenEntityWorkspacePayload(menu.projectorPos()));
        }).bounds(x + 12 + (sourceButtonWidth + sourceGap) * 2, y + 30, sourceButtonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.workspace.banner_short"), button -> {
            saveSettings();
            PacketDistributor.sendToServer(new OpenBannerWorkspacePayload(menu.projectorPos()));
        }).bounds(x + 12 + (sourceButtonWidth + sourceGap) * 3, y + 30, sourceButtonWidth, 20).build());

        scaleSlider = addRenderableWidget(new IntSlider(
                x + 12, y + 76, half, 20,
                ProjectionSettings.DEBUG_MIN_SCALE_PIXELS, ProjectionSettings.DEBUG_MAX_SCALE_PIXELS, scalePixels,
                value -> {
                    scalePixels = value;
                    refreshDynamicLimits(true);
                    updateClearance(true);
                },
                value -> Component.translatable("gui.mirage_projector.scale", value).getString()
        ));
        scaleSlider.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.scale.dynamic")));

        liftSlider = addRenderableWidget(new IntSlider(
                x + 20 + half, y + 76, half, 20,
                0, ProjectionSettings.DEBUG_MAX_LIFT_PIXELS, liftPixels,
                value -> {
                    liftPixels = value;
                    refreshDynamicLimits(true);
                    updateClearance(true);
                },
                value -> Component.translatable("gui.mirage_projector.lift", value).getString()
        ));
        liftSlider.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.lift.dynamic")));

        rotationButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            rotationEnabled = !rotationEnabled;
            refreshLabels();
            refreshDynamicLimits(true);
        }).bounds(x + 12, y + 118, half, 20).build());
        addRenderableWidget(new IntSlider(
                x + 20 + half, y + 118, half, 20,
                5, 1200, rotationPeriodTicks,
                value -> rotationPeriodTicks = value,
                value -> Component.translatable("gui.mirage_projector.rotation_period", String.format(Locale.ROOT, "%.2f", value / 20.0D)).getString()
        ));

        directionButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            clockwise = !clockwise;
            refreshLabels();
        })
                .bounds(x + 12, y + 142, half, 20).build());
        orientationButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            rotationOffsetDegrees = wrap(rotationOffsetDegrees + 90.0F);
            refreshLabels();
        }).bounds(x + 20 + half, y + 142, half, 20).build());

        floatingButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            floatingEnabled = !floatingEnabled;
            refreshLabels();
            refreshDynamicLimits(true);
        })
                .bounds(x + 12, y + 176, half, 20).build());
        floatAmplitudeSlider = addRenderableWidget(new IntSlider(
                x + 20 + half, y + 176, half, 20,
                0, ProjectionSettings.DEBUG_MAX_FLOAT_PIXELS, floatAmplitudePixels,
                value -> {
                    floatAmplitudePixels = value;
                    refreshDynamicLimits(true);
                    updateClearance(true);
                },
                value -> Component.translatable("gui.mirage_projector.float_amplitude", value).getString()
        ));
        floatAmplitudeSlider.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.float.dynamic")));

        floatModeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            floatMode = floatMode == ProjectionSettings.FloatMode.TIME
                    ? ProjectionSettings.FloatMode.ROTATION_SYNCED : ProjectionSettings.FloatMode.TIME;
            refreshLabels();
            refreshFloatTimingSlider();
            refreshDynamicLimits(true);
        }).bounds(x + 12, y + 200, half, 20).build());
        floatTimingSlider = addRenderableWidget(new IntSlider(
                x + 20 + half, y + 200, half, 20,
                5, 1200, floatCycleTicks,
                value -> floatCycleTicks = value,
                value -> Component.translatable("gui.mirage_projector.float_cycle", String.format(Locale.ROOT, "%.2f", value / 20.0D)).getString()
        ));

        lightingButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            fullbright = !fullbright;
            refreshLabels();
            refreshDynamicLimits(true);
        })
                .bounds(x + 12, y + 234, half, 20).build());
        IntSlider ghost = addRenderableWidget(new IntSlider(
                x + 20 + half, y + 234, half, 20,
                0, 90, transparencyPercent,
                value -> {
                    transparencyPercent = value;
                    refreshDynamicLimits(true);
                },
                value -> Component.translatable("gui.mirage_projector.ghost", value).getString()
        ));
        ghost.setTooltip(Tooltip.create(Component.translatable("tooltip.mirage_projector.ghost")));

        tintButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            tintRgb = nextTint(tintRgb);
            refreshLabels();
        })
                .bounds(x + 12, y + 258, half, 20).build());
        debugButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            if (minecraft != null && minecraft.player != null && minecraft.player.isCreative()) {
                debugChassisOverride = !debugChassisOverride;
                refreshLabels();
                refreshDynamicLimits(true);
                updateClearance(true);
            }
        }).bounds(x + 20 + half, y + 258, half, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.apply"), button -> {
            saveSettings();
            onClose();
        }).bounds(x + 12, y + 470, half, 22).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.cancel"), button -> onClose())
                .bounds(x + 20 + half, y + 470, half, 22).build());

        refreshLabels();
        refreshFloatTimingSlider();
        observedCore = menu.coreProfile();
        refreshDynamicLimits(true);
        updateClearance(true);
    }

    private void refreshDynamicLimits(boolean clampValues) {
        ProjectionCoreProfile core = menu.coreProfile();
        ProjectionChassisProfile chassis = menu.chassisProfile();
        boolean hasContent = menu.hasProjectedSourceContent();
        int sourceCount = menu.projectedSourceCount();

        if (clampValues) {

            ProjectionSettings current = buildSettings();
            scalePixels = clamp(scalePixels, ProjectionSettings.DEBUG_MIN_SCALE_PIXELS,
                    ProjectionPower.maximumStructuralScale(current, core, chassis, hasContent));
            liftPixels = clamp(liftPixels, 0,
                    ProjectionPower.maximumStructuralLift(buildSettings(), core, chassis));
            if (floatingEnabled) {
                floatAmplitudePixels = Math.min(floatAmplitudePixels, liftPixels);
            }
            floatAmplitudePixels = clamp(floatAmplitudePixels, 0,
                    ProjectionPower.maximumStructuralFloat(buildSettings(), core, chassis));

            if (core.present() && !ProjectionPower.evaluate(
                    buildSettings(), core, chassis, hasContent, sourceCount).active()) {
                int feasibleFloat = ProjectionPower.maximumFeasibleFloat(
                        buildSettings(), core, chassis, hasContent, sourceCount);
                floatAmplitudePixels = clamp(floatAmplitudePixels, 0, feasibleFloat);

                if (!ProjectionPower.evaluate(buildSettings(), core, chassis, hasContent, sourceCount).active()) {
                    int feasibleLift = ProjectionPower.maximumFeasibleLift(
                            buildSettings(), core, chassis, hasContent, sourceCount);
                    liftPixels = clamp(liftPixels, 0, feasibleLift);
                    if (floatingEnabled) {
                        floatAmplitudePixels = Math.min(floatAmplitudePixels, liftPixels);
                    }
                }

                if (!ProjectionPower.evaluate(buildSettings(), core, chassis, hasContent, sourceCount).active()) {
                    int feasibleScale = ProjectionPower.maximumFeasibleScale(
                            buildSettings(), core, chassis, hasContent, sourceCount);
                    scalePixels = clamp(scalePixels, ProjectionSettings.DEBUG_MIN_SCALE_PIXELS, feasibleScale);
                }
            }
        }

        ProjectionSettings finalSettings = buildSettings();
        scaleLimit = core.present()
                ? ProjectionPower.maximumFeasibleScale(finalSettings, core, chassis, hasContent, sourceCount)
                : ProjectionPower.maximumStructuralScale(finalSettings, core, chassis, hasContent);
        liftLimit = core.present()
                ? ProjectionPower.maximumFeasibleLift(finalSettings, core, chassis, hasContent, sourceCount)
                : ProjectionPower.maximumStructuralLift(finalSettings, core, chassis);
        floatLimit = core.present()
                ? ProjectionPower.maximumFeasibleFloat(finalSettings, core, chassis, hasContent, sourceCount)
                : ProjectionPower.maximumStructuralFloat(finalSettings, core, chassis);

        scaleLimit = Math.max(ProjectionSettings.DEBUG_MIN_SCALE_PIXELS, scaleLimit);
        liftLimit = Math.max(0, liftLimit);
        floatLimit = Math.max(0, floatLimit);

        if (scaleSlider != null) scaleSlider.reconfigure(
                ProjectionSettings.DEBUG_MIN_SCALE_PIXELS, scaleLimit, scalePixels,
                value -> {
                    scalePixels = value;
                    refreshDynamicLimits(true);
                    updateClearance(true);
                },
                value -> Component.translatable("gui.mirage_projector.scale", value).getString());
        if (liftSlider != null) liftSlider.reconfigure(
                0, liftLimit, liftPixels,
                value -> {
                    liftPixels = value;
                    refreshDynamicLimits(true);
                    updateClearance(true);
                },
                value -> Component.translatable("gui.mirage_projector.lift", value).getString());
        if (floatAmplitudeSlider != null) floatAmplitudeSlider.reconfigure(
                0, floatLimit, floatAmplitudePixels,
                value -> {
                    floatAmplitudePixels = value;
                    refreshDynamicLimits(true);
                    updateClearance(true);
                },
                value -> Component.translatable("gui.mirage_projector.float_amplitude", value).getString());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(Math.max(min, max), value));
    }

    private void refreshFloatTimingSlider() {
        if (floatTimingSlider == null) {
            return;
        }
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
        if (rotationButton != null) {
            rotationButton.setMessage(Component.translatable("gui.mirage_projector.rotation", onOff(rotationEnabled)));
        }
        if (directionButton != null) {
            directionButton.setMessage(Component.translatable("gui.mirage_projector.direction", Component.translatable(clockwise ? "gui.mirage_projector.clockwise" : "gui.mirage_projector.counterclockwise")));
        }
        if (orientationButton != null) {
            orientationButton.setMessage(Component.translatable("gui.mirage_projector.orientation", Math.round(rotationOffsetDegrees)));
        }
        if (floatingButton != null) {
            floatingButton.setMessage(Component.translatable("gui.mirage_projector.floating", onOff(floatingEnabled)));
        }
        if (floatModeButton != null) {
            floatModeButton.setMessage(Component.translatable("gui.mirage_projector.float_mode", Component.translatable(floatMode == ProjectionSettings.FloatMode.TIME ? "gui.mirage_projector.float_mode.time" : "gui.mirage_projector.float_mode.rotation")));
        }
        if (lightingButton != null) {
            lightingButton.setMessage(Component.translatable("gui.mirage_projector.lighting", Component.translatable(fullbright ? "gui.mirage_projector.lighting.fullbright" : "gui.mirage_projector.lighting.world")));
        }
        if (tintButton != null) {
            tintButton.setMessage(Component.translatable("gui.mirage_projector.tint", tintName(tintRgb)));
        }
        if (debugButton != null) {
            debugButton.visible = minecraft != null && minecraft.player != null && minecraft.player.isCreative();
            debugButton.setMessage(Component.translatable("gui.mirage_projector.debug", onOff(debugChassisOverride)));
        }
    }

    private Component onOff(boolean value) {
        return Component.translatable(value ? "gui.mirage_projector.on" : "gui.mirage_projector.off");
    }

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
        section(graphics, x + 8, y + 282, imageWidth - 16, 88);
        drawSlotFrame(graphics, x + MirageProjectorMenu.CORE_SLOT_X - 1, y + MirageProjectorMenu.CORE_SLOT_Y - 1);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotFrame(graphics, x + MirageProjectorMenu.PLAYER_INV_X + col * 18 - 1, y + MirageProjectorMenu.PLAYER_INV_Y + row * 18 - 1);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotFrame(graphics, x + MirageProjectorMenu.PLAYER_INV_X + col * 18 - 1, y + MirageProjectorMenu.PLAYER_INV_Y + 58 - 1);
        }
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
        ProjectionChassisProfile chassis = menu.chassisProfile();
        ProjectionPower.Status power = ProjectionPower.evaluate(
                buildSettings(), core, chassis, menu.hasProjectedSourceContent(), menu.projectedSourceCount());
        ProjectionPower.Dimensions dimensions = ProjectionPower.dimensions(
                buildSettings(), menu.hasProjectedSourceContent(), chassis);

        ProjectionPower.Breakdown breakdown = ProjectionPower.calculateBreakdown(
                buildSettings(), menu.hasProjectedSourceContent(), chassis, menu.projectedSourceCount());
        ProjectionPower.Overdrive overdrive = ProjectionPower.overdrive(
                buildSettings(), chassis, menu.hasProjectedSourceContent());

        int coreTextX = 50;
        if (core.present()) {
            graphics.drawString(font, fitText(Component.translatable("gui.mirage_projector.power.core_output",
                    core.displayComponent(), core.basePower()).getString(), 342), coreTextX, 294, 0xFFD8E7FF, false);
        } else {
            graphics.drawString(font, Component.translatable("gui.mirage_projector.core.empty"), coreTextX, 294, 0xFFFFA0A0, false);
        }

        graphics.drawString(font, fitText(Component.translatable("gui.mirage_projector.power.multipliers",
                chassis.displayName(), String.format(Locale.ROOT, "%.2f", chassis.powerMultiplier()),
                String.format(Locale.ROOT, "%.2f", core.amplificationMultiplier())).getString(), 342),
                coreTextX, 305, 0xFFBFD8FF, false);

        Component loadMetric = Component.translatable("gui.mirage_projector.power.effective_load",
                power.availablePower(), power.usedPower(), power.availablePower());
        String loadText = loadMetric.getString();
        if (breakdown.ghostSavings() > 0) {
            loadText += " · " + Component.translatable("gui.mirage_projector.power.ghost_saving", breakdown.ghostSavings()).getString();
        }
        graphics.drawString(font, fitText(loadText, 342), coreTextX, 316,
                power.active() ? 0xFF9FDBA9 : 0xFFFFA87A, false);

        int barX = coreTextX;
        int barY = 328;
        int barWidth = 342;
        int barHeight = 5;
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF252A33);
        int filled = power.availablePower() <= 0
                ? 0
                : (int) Math.round(barWidth * Math.min(1.0D, power.usedPower() / (double) power.availablePower()));
        if (filled > 0) {
            graphics.fill(barX, barY, barX + filled, barY + barHeight,
                    power.usedPower() <= power.availablePower() ? 0xFF66B879 : 0xFFE07A5F);
        }
        graphics.fill(imageWidth - 26, 284, imageWidth - 14, 296, 0xFF252A33);
        graphics.drawString(font, "?", imageWidth - 22, 286, 0xFFD7C3E7, false);

        Component sizeMetric;
        if (dimensions.empty()) {
            sizeMetric = Component.translatable("gui.mirage_projector.limit.scale", scalePixels, scaleLimit);
        } else if (chassis.geometry() == ProjectionChassisProfile.Geometry.PRISM
                && buildSettings().sourceMode() == ProjectionSettings.SourceMode.IMAGE) {
            sizeMetric = Component.translatable("gui.mirage_projector.power.size_prism_adaptive",
                    dimensions.widthPixels(), dimensions.heightPixels());
        } else {
            sizeMetric = Component.translatable("gui.mirage_projector.power.size_nominal",
                    dimensions.widthPixels(), dimensions.heightPixels(),
                    chassis.nominalWidthPixels(), chassis.nominalHeightPixels());
        }
        graphics.drawString(font, fitText(sizeMetric.getString(), 210), 20, 338, 0xFFBFD8FF, false);
        graphics.drawString(font, fitText(Component.translatable("gui.mirage_projector.power.scale_effective",
                scalePixels, scaleLimit).getString(), 170), 230, 338, 0xFFBFD8FF, false);
        graphics.drawString(font, fitText(Component.translatable("gui.mirage_projector.power.motion_effective",
                liftPixels, liftLimit, chassis.nominalLiftPixels(),
                floatAmplitudePixels, floatLimit, chassis.nominalFloatPixels()).getString(), 376),
                20, 349, 0xFFBFD8FF, false);

        Component validation;
        int validationColor;
        if (!power.active()) {
            validation = Component.translatable("gui.mirage_projector.status.power_blocked", powerFailure(power.failure()));
            validationColor = 0xFFFF8A73;
        } else if (clearance.known() && !clearance.clear()) {
            validation = Component.translatable(
                    "gui.mirage_projector.status.clearance_blocked",
                    clearance.blockedBlocks(), clearanceEnvelopeSummary(clearance));
            validationColor = 0xFFFF8A73;
        } else if (!dimensions.empty()) {
            validation = Component.translatable(
                    "gui.mirage_projector.status.ready_dimensions", dimensions.widthPixels(), dimensions.heightPixels());
            validationColor = 0xFF8FD19A;
        } else {
            validation = Component.translatable("gui.mirage_projector.status.no_projection");
            validationColor = 0xFF9CA3AF;
        }
        String validationText = validation.getString();
        if (overdrive.active() && power.active()) {
            validationText = Component.translatable("gui.mirage_projector.power.overdrive_status",
                    String.format(Locale.ROOT, "%.2f", overdrive.maximumRatio()), validation).getString();
        }
        graphics.drawString(font, fitText(validationText, 376), 20, 360, validationColor, false);
        graphics.drawString(font, Component.translatable("container.inventory"), MirageProjectorMenu.PLAYER_INV_X, MirageProjectorMenu.PLAYER_INV_Y - 12, 0xFFBEB8C8, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderEmptyCoreTooltip(graphics, mouseX, mouseY);
        renderPowerBreakdownTooltip(graphics, mouseX, mouseY);
    }

    private void renderPowerBreakdownTooltip(GuiGraphics graphics, int mouseX, int mouseY) {

        int minX = leftPos + imageWidth - 26;
        int maxX = leftPos + imageWidth - 14;
        int minY = topPos + 284;
        int maxY = topPos + 296;
        if (mouseX < minX || mouseX >= maxX || mouseY < minY || mouseY >= maxY) {
            return;
        }

        ProjectionCoreProfile core = menu.coreProfile();
        ProjectionChassisProfile chassis = menu.chassisProfile();
        ProjectionSettings current = buildSettings();
        ProjectionPower.Breakdown b = ProjectionPower.calculateBreakdown(
                current, menu.hasProjectedSourceContent(), chassis, menu.projectedSourceCount());
        ProjectionPower.Status status = ProjectionPower.evaluate(
                current, core, chassis, menu.hasProjectedSourceContent(), menu.projectedSourceCount());

        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.mirage_projector.power.title"));
        lines.add(Component.translatable("tooltip.mirage_projector.power.capacity_formula",
                core.basePower(), String.format(Locale.ROOT, "%.2f", chassis.powerMultiplier()),
                String.format(Locale.ROOT, "%.2f", core.amplificationMultiplier()), status.availablePower()));
        lines.add(Component.translatable("tooltip.mirage_projector.power.base", b.baseCost()));
        lines.add(Component.translatable("tooltip.mirage_projector.power.geometry", b.geometryCost()));
        lines.add(Component.translatable("tooltip.mirage_projector.power.source", b.sourceCost()));
        lines.add(Component.translatable("tooltip.mirage_projector.power.lift", b.liftCost()));
        lines.add(Component.translatable("tooltip.mirage_projector.power.float", b.floatCost()));
        lines.add(Component.translatable("tooltip.mirage_projector.power.features", b.featureCost()));
        if (b.ghostSavings() > 0) {
            lines.add(Component.translatable("tooltip.mirage_projector.power.ghost", b.ghostSavings()));
        }
        lines.add(Component.translatable("tooltip.mirage_projector.power.total", b.totalPower(), status.availablePower()));
        graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
    }

    private void renderEmptyCoreTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.coreStack().isEmpty()) {
            return;
        }
        int x = leftPos + MirageProjectorMenu.CORE_SLOT_X;
        int y = topPos + MirageProjectorMenu.CORE_SLOT_Y;
        if (mouseX < x || mouseX >= x + 18 || mouseY < y || mouseY >= y + 18) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.mirage_projector.core.accepted"));
        java.util.Arrays.stream(ProjectionCoreProfile.values())
                .filter(ProjectionCoreProfile::present)
                .sorted(java.util.Comparator
                        .comparingDouble(ProjectionCoreProfile::materialOutput)
                        .thenComparingInt(ProjectionCoreProfile::basePower))
                .forEach(profile -> lines.add(Component.translatable(
                        "tooltip.mirage_projector.core.entry", profile.displayComponent(),
                        profile.basePower(), String.format(Locale.ROOT, "%.2f", profile.amplificationMultiplier()))));
        graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ProjectionCoreProfile currentCore = menu.coreProfile();
        if (currentCore != observedCore) {
            observedCore = currentCore;
            refreshDynamicLimits(true);
            updateClearance(true);
        } else {
            updateClearance(false);
        }
    }

    private void updateClearance(boolean force) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            clearance = ProjectionClearance.Result.UNKNOWN;
            return;
        }
        long tick = mc.level.getGameTime();
        if (!force && lastClearanceTick != Long.MIN_VALUE && tick - lastClearanceTick < 10) {
            return;
        }
        lastClearanceTick = tick;
        clearance = ProjectionClearance.scan(
                mc.level,
                menu.projectorPos(),
                buildSettings(),
                menu.chassisProfile(),
                menu.hasProjectedSourceContent(),
                menu.activeHumanoidPose(),
                menu.projector() == null ? null : menu.projector().entityProjectionState()
        );
        ProjectionClearancePreviewRenderer.show(menu.projectorPos(), clearance);
    }

    private static Component powerFailure(ProjectionPower.Failure failure) {
        return Component.translatable("gui.mirage_projector.power.failure."
                + (failure == null ? ProjectionPower.Failure.NONE : failure).name().toLowerCase(Locale.ROOT));
    }

    private static String clearanceEnvelopeSummary(ProjectionClearance.Result result) {
        if (result == null || !result.hasEnvelope()) {
            return "?";
        }
        int x = Math.max(1, (int) Math.ceil(result.envelope().getXsize() * 16.0D));
        int y = Math.max(1, (int) Math.ceil(result.envelope().getYsize() * 16.0D));
        int z = Math.max(1, (int) Math.ceil(result.envelope().getZsize() * 16.0D));
        return x + "×" + y + "×" + z + " px";
    }

    private String fitText(String value, int maxWidth) {
        if (value == null || font.width(value) <= maxWidth) {
            return value == null ? "" : value;
        }
        String ellipsis = "…";
        int target = Math.max(0, maxWidth - font.width(ellipsis));
        int end = value.length();
        while (end > 0 && font.width(value.substring(0, end)) > target) {
            end--;
        }
        return value.substring(0, Math.max(0, end)) + ellipsis;
    }

    @Override
    public void onClose() {
        ProjectionClearancePreviewRenderer.clear();
        super.onClose();
    }

    @Override
    public void removed() {
        ProjectionClearancePreviewRenderer.clear();
        super.removed();
    }

    private static Component sourceName(ProjectionSettings.SourceMode mode) {
        return Component.translatable("gui.mirage_projector.source." + mode.name().toLowerCase());
    }

    private static int nextTint(int current) {
        int[] presets = {0xFFFFFF, 0x8FE8FF, 0xC7A4FF, 0xFF9BD7, 0xFFD27A, 0x9CFFB1, 0xFF9A9A};
        for (int i = 0; i < presets.length; i++) {
            if ((current & 0xFFFFFF) == presets[i]) {
                return presets[(i + 1) % presets.length];
            }
        }
        return presets[0];
    }

    private static Component tintName(int rgb) {
        String key = switch (rgb & 0xFFFFFF) {
            case 0xFFFFFF -> "white";
            case 0x8FE8FF -> "cyan";
            case 0xC7A4FF -> "amethyst";
            case 0xFF9BD7 -> "rose";
            case 0xFFD27A -> "amber";
            case 0x9CFFB1 -> "green";
            case 0xFF9A9A -> "red";
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
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.formatter = formatter;
            this.current = clamp(current, min, max);
            updateMessage();
        }

        void reconfigure(int min, int max, int current, IntConsumer setter, Function<Integer, String> formatter) {
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.formatter = formatter;
            this.current = clamp(current, min, max);
            this.value = normalize(this.current, min, max);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (formatter != null) {
                setMessage(Component.literal(formatter.apply(current)));
            }
        }

        @Override
        protected void applyValue() {
            current = min + (int) Math.round(value * (max - min));
            current = clamp(current, min, max);
            setter.accept(current);
            updateMessage();
        }

        private static double normalize(int value, int min, int max) {
            return max <= min ? 0.0D : (clamp(value, min, max) - min) / (double) (max - min);
        }

        private static int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
