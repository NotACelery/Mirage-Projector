package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.entity.HumanoidPosePreset;
import celerbi.mirageprojector.entity.VirtualEquipmentSnapshots;
import celerbi.mirageprojector.menu.EntityProjectorMenu;
import celerbi.mirageprojector.network.EntityWorkspaceActionPayload;
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

import java.util.EnumMap;
import java.util.Map;

/** Entity/Humanoid workspace with staged equipment conflict resolution and live 3D inspection. */
public final class EntityProjectorScreen extends AbstractContainerScreen<EntityProjectorMenu> {
    private static final int PANEL_WIDTH = 370;
    private static final int PANEL_HEIGHT = 330;
    private static final int PROJECTED_X = 278;
    private static final int APPLY_X = 63;
    private static final int PREVIEW_GAP = 8;
    private static final int PREVIEW_PREFERRED_WIDTH = 150;
    private static final int PREVIEW_MIN_WIDTH = 68;
    private static final int PREVIEW_HEIGHT = 218;
    private static final int SCREEN_MARGIN = 4;

    private final Map<VirtualEquipmentSnapshots.Channel, Button> applyButtons =
            new EnumMap<>(VirtualEquipmentSnapshots.Channel.class);
    private final EntityProjectionPreviewRenderer entityPreview = new EntityProjectionPreviewRenderer();

    private Button captureLoadoutButton;
    private Button returnGearButton;
    private Button poseButton;
    private Button confirmReplaceButton;
    private Button cancelReplaceButton;
    private VirtualEquipmentSnapshots.Channel pendingConflict;
    private Component status = Component.literal("Entity workspace ready");
    private EntityScanData.Kind lastKind;

    public EntityProjectorScreen(EntityProjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
        inventoryLabelX = EntityProjectorMenu.PLAYER_INV_X;
        inventoryLabelY = EntityProjectorMenu.PLAYER_INV_Y - 12;
    }

    @Override
    protected void init() {
        super.init();
        layoutComposite();
        createApplyButtons();

        addRenderableWidget(Button.builder(Component.literal("Use this projection"), button ->
                PacketDistributor.sendToServer(new SetProjectionSourcePayload(menu.projectorPos(), ProjectionSettings.SourceMode.ENTITY))
        ).bounds(leftPos + 102, topPos + 4, 122, 18).build());

        addRenderableWidget(Button.builder(Component.literal("Projection settings..."), button ->
                PacketDistributor.sendToServer(new OpenProjectorWorkspacePayload(menu.projectorPos()))
        ).bounds(leftPos + 232, topPos + 4, 126, 18).build());

        captureLoadoutButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.entity.capture_loadout"),
                button -> {
                    PacketDistributor.sendToServer(new EntityWorkspaceActionPayload(
                            menu.projectorPos(),
                            EntityWorkspaceActionPayload.Action.CAPTURE_EQUIPPED,
                            null
                    ));
                    status = Component.translatable("gui.mirage_projector.entity.capture_loadout_status");
                }
        ).bounds(leftPos + 20, topPos + 197, 92, 20).build());
        captureLoadoutButton.setTooltip(Tooltip.create(Component.translatable(
                "tooltip.mirage_projector.entity.capture_loadout"
        )));

        returnGearButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.entity.return_gear"),
                button -> {
                    PacketDistributor.sendToServer(new EntityWorkspaceActionPayload(
                            menu.projectorPos(),
                            EntityWorkspaceActionPayload.Action.RETURN_STAGING,
                            null
                    ));
                    status = Component.translatable("gui.mirage_projector.entity.return_gear_status");
                }
        ).bounds(leftPos + 116, topPos + 197, 80, 20).build());
        returnGearButton.setTooltip(Tooltip.create(Component.translatable(
                "tooltip.mirage_projector.entity.return_gear"
        )));

        poseButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            HumanoidPosePreset next = menu.state().humanoidPose().next();
            PacketDistributor.sendToServer(new EntityWorkspaceActionPayload(
                    menu.projectorPos(),
                    EntityWorkspaceActionPayload.Action.CYCLE_POSE,
                    null
            ));
            status = Component.translatable(
                    "gui.mirage_projector.entity.pose_status",
                    poseName(next)
            );
        }).bounds(leftPos + 202, topPos + 197, 150, 20).build());
        poseButton.setTooltip(Tooltip.create(Component.translatable(
                "tooltip.mirage_projector.entity.pose"
        )));

        confirmReplaceButton = addRenderableWidget(Button.builder(Component.literal("Replace"), button -> {
            if (pendingConflict != null) {
                sendAction(EntityWorkspaceActionPayload.Action.REPLACE, pendingConflict);
                status = Component.literal("Projected " + channelName(pendingConflict) + " replaced");
            }
            pendingConflict = null;
            refreshConflictButtons();
        }).bounds(leftPos + 202, topPos + 197, 72, 20).build());

        cancelReplaceButton = addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> {
            pendingConflict = null;
            status = Component.literal("Replacement cancelled");
            refreshConflictButtons();
        }).bounds(leftPos + 280, topPos + 197, 72, 20).build());

        refreshConflictButtons();
        updateModeWidgets(true);
        refreshPoseButton();
    }

    private void layoutComposite() {
        int preferredTotal = imageWidth + PREVIEW_GAP + PREVIEW_PREFERRED_WIDTH;
        if (width >= preferredTotal + SCREEN_MARGIN * 2) {
            leftPos = (width - preferredTotal) / 2;
            return;
        }

        int minimumTotal = imageWidth + PREVIEW_GAP + PREVIEW_MIN_WIDTH;
        if (width >= minimumTotal + SCREEN_MARGIN * 2) {
            leftPos = SCREEN_MARGIN;
        }
    }

    private int previewPanelWidth() {
        int available = width - (leftPos + imageWidth + PREVIEW_GAP) - SCREEN_MARGIN;
        return available < PREVIEW_MIN_WIDTH ? 0 : Math.min(PREVIEW_PREFERRED_WIDTH, available);
    }

    private void createApplyButtons() {
        for (VirtualEquipmentSnapshots.Channel channel : VirtualEquipmentSnapshots.Channel.values()) {
            int y = localY(channel);
            Button button = addRenderableWidget(Button.builder(Component.literal("✓"), ignored -> apply(channel))
                    .bounds(leftPos + APPLY_X, topPos + y, 20, 18)
                    .build());
            button.setTooltip(Tooltip.create(Component.literal(
                    "Apply this incoming snapshot to the matching projected slot. Existing projected equipment requires confirmation."
            )));
            applyButtons.put(channel, button);
        }
    }

    private void apply(VirtualEquipmentSnapshots.Channel channel) {
        ItemStack incoming = menu.incoming(channel).stack();
        if (incoming.isEmpty()) {
            status = Component.literal("Nothing is staged for " + channelName(channel));
            return;
        }

        if (menu.hasConflict(channel)) {
            pendingConflict = channel;
            ItemStack current = menu.projected(channel).stack();
            status = Component.literal("Replace " + truncate(current.getHoverName().getString(), 20)
                    + " with " + truncate(incoming.getHoverName().getString(), 20) + "?");
            refreshConflictButtons();
            return;
        }

        sendAction(EntityWorkspaceActionPayload.Action.APPLY, channel);
        status = Component.literal("Applied " + channelName(channel));
    }

    private void sendAction(
            EntityWorkspaceActionPayload.Action action,
            VirtualEquipmentSnapshots.Channel channel
    ) {
        PacketDistributor.sendToServer(new EntityWorkspaceActionPayload(menu.projectorPos(), action, channel));
    }

    private void updateModeWidgets(boolean force) {
        EntityScanData.Kind kind = menu.effectiveKind();
        if (!force && kind == lastKind) {
            return;
        }
        lastKind = kind;
        pendingConflict = null;
        refreshConflictButtons();

        for (Map.Entry<VirtualEquipmentSnapshots.Channel, Button> entry : applyButtons.entrySet()) {
            VirtualEquipmentSnapshots.Channel channel = entry.getKey();
            entry.getValue().visible = switch (kind) {
                case HUMANOID -> channel.humanoid();
                case HORSE -> channel.horse();
                case GENERIC -> false;
            };
        }
        // Keep this visible even in Generic context: a context switch may hide
        // physical Humanoid staging rows, but must never hide the escape hatch
        // that safely returns those real items.
        captureLoadoutButton.visible = kind == EntityScanData.Kind.HUMANOID;
        returnGearButton.visible = true;
    }

    private void refreshConflictButtons() {
        boolean visible = pendingConflict != null;
        if (confirmReplaceButton != null) {
            confirmReplaceButton.visible = visible;
        }
        if (cancelReplaceButton != null) {
            cancelReplaceButton.visible = visible;
        }
        if (poseButton != null) {
            poseButton.visible = !visible && lastKind == EntityScanData.Kind.HUMANOID;
        }
    }

    private void refreshPoseButton() {
        if (poseButton != null) {
            poseButton.setMessage(Component.translatable(
                    "gui.mirage_projector.entity.pose",
                    poseName(menu.state().humanoidPose())
            ));
        }
    }

    private static Component poseName(HumanoidPosePreset pose) {
        return Component.translatable("gui.mirage_projector.entity.pose." + pose.serializedName());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateModeWidgets(false);
        refreshPoseButton();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xE014171D);
        graphics.fill(x + 7, y + 23, x + imageWidth - 7, y + 221, 0xFF20252D);
        graphics.fill(x + 7, y + 224, x + imageWidth - 7, y + imageHeight - 7, 0xFF171A20);

        drawSlotFrame(graphics, x + EntityProjectorMenu.CARD_X, y + EntityProjectorMenu.CARD_Y, 0xFF75658A);

        EntityScanData.Kind kind = menu.effectiveKind();
        if (kind == EntityScanData.Kind.HUMANOID) {
            for (VirtualEquipmentSnapshots.Channel channel : humanoidChannels()) {
                int rowY = y + localY(channel);
                drawSlotFrame(graphics, x + EntityProjectorMenu.STAGING_X, rowY, 0xFF536675);
                drawSlotFrame(graphics, x + PROJECTED_X, rowY, menu.hasConflict(channel) ? 0xFF9C5A52 : 0xFF53675C);
            }
        } else if (kind == EntityScanData.Kind.HORSE) {
            for (VirtualEquipmentSnapshots.Channel channel : horseChannels()) {
                int rowY = y + localY(channel);
                drawSlotFrame(graphics, x + EntityProjectorMenu.STAGING_X, rowY, 0xFF536675);
                drawSlotFrame(graphics, x + PROJECTED_X, rowY, menu.hasConflict(channel) ? 0xFF9C5A52 : 0xFF53675C);
            }
        }

        renderPreviewPanel(graphics, mouseX, mouseY);
    }

    private void renderPreviewPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int panelW = previewPanelWidth();
        int panelH = PREVIEW_HEIGHT;
        if (panelW == 0) {
            return;
        }
        int x = leftPos + imageWidth + PREVIEW_GAP;
        int y = topPos + 24;

        int previewChars = Math.max(6, panelW / 6);
        graphics.fill(x, y, x + panelW, y + panelH, 0xE014171D);
        graphics.fill(x + 4, y + 18, x + panelW - 4, y + panelH - 4, 0xFF20252D);
        graphics.drawCenteredString(font, panelW < 90 ? "Preview" : "3D Preview", x + panelW / 2, y + 6, 0xFFF2ECFF);

        var active = menu.state().activeEntity();
        boolean bodylessMannequin = active.isEmpty() && menu.state().hasProjectedHumanoidEquipment();
        if (active.isPresent() || bodylessMannequin) {
            int viewportX = x + 7;
            int viewportY = y + 23;
            int viewportW = panelW - 14;
            int viewportH = panelH - 58;
            ProjectionSettings previewSettings = menu.projector() == null
                    ? ProjectionSettings.DEFAULT
                    : menu.projector().settings();
            boolean rendered = entityPreview.render(
                    graphics,
                    menu.state(),
                    previewSettings,
                    viewportX,
                    viewportY,
                    viewportW,
                    viewportH,
                    mouseX,
                    mouseY
            );

            if (!rendered) {
                graphics.drawCenteredString(font, "Preview unavailable", x + panelW / 2, y + 91, 0xFFFFC884);
            }

            if (active.isPresent()) {
                EntityScanData.View scan = active.get();
                if (scan.hasProjectionNameplate()) {
                    graphics.fill(x + 9, y + panelH - 31, x + panelW - 9, y + panelH - 17, 0xB0000000);
                    graphics.drawCenteredString(
                            font,
                            truncate(scan.nameplateText(), previewChars),
                            x + panelW / 2,
                            y + panelH - 28,
                            ProjectionRenderBuffers.tintedArgb(previewSettings, 0xF2F2F2)
                    );
                }
                String footer = scan.playerSource() && scan.hasFrozenPlayerTexture()
                        ? scan.entityType() + " · frozen skin"
                        : scan.hasProjectionNameplate()
                        ? scan.entityType().toString()
                        : scan.displayName();
                graphics.drawCenteredString(
                        font,
                        truncate(footer, previewChars),
                        x + panelW / 2,
                        y + panelH - 13,
                        0xFF9CA3AF
                );
            } else {
                graphics.drawCenteredString(
                        font,
                        truncate("Virtual humanoid mannequin", previewChars),
                        x + panelW / 2,
                        y + panelH - 13,
                        0xFF9CA3AF
                );
            }
            return;
        }

        graphics.drawCenteredString(font, "No entity body", x + panelW / 2, y + 91, 0xFF9CA3AF);
        graphics.drawCenteredString(font, "Apply projected gear", x + panelW / 2, y + 107, 0xFF9CA3AF);
        graphics.drawCenteredString(font, "to create the mannequin", x + panelW / 2, y + 119, 0xFF9CA3AF);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 10, 8, 0xFFF4F4F4, false);
        EntityScanData.Kind kind = menu.effectiveKind();
        graphics.drawString(font, "Incoming / staging", 18, 25, 0xFFAFD8EE, false);
        graphics.drawCenteredString(font, "Entity source", EntityProjectorMenu.CARD_X + 8, 25, 0xFFD7B8F5);
        graphics.drawString(font, "Projected / active", 251, 25, 0xFFB9E4C0, false);

        if (kind == EntityScanData.Kind.HUMANOID) {
            for (VirtualEquipmentSnapshots.Channel channel : humanoidChannels()) {
                renderRow(graphics, channel);
            }
        } else if (kind == EntityScanData.Kind.HORSE) {
            for (VirtualEquipmentSnapshots.Channel channel : horseChannels()) {
                renderRow(graphics, channel);
            }
        } else {
            graphics.drawString(font, "Generic Entity: visual state remains inside the frozen entity snapshot.", 18, 83, 0xFFC4C9D3, false);
            graphics.drawString(font, "Editable armor slots are not fabricated for unsupported equipment models.", 18, 97, 0xFF8F98A8, false);
        }

        ItemStack card = menu.cardStack();
        String source = card.isEmpty() ? "Drop a scanned template here" : truncate(card.getHoverName().getString(), 26);
        graphics.drawCenteredString(font, source, EntityProjectorMenu.CARD_X + 8, EntityProjectorMenu.CARD_Y + 22, 0xFFD8C5EB);

        graphics.drawString(font, status, 18, 181, pendingConflict == null ? 0xFFE7DCF5 : 0xFFFFB98E, false);
        graphics.drawString(font, "Inventory", EntityProjectorMenu.PLAYER_INV_X, EntityProjectorMenu.PLAYER_INV_Y - 12, 0xFFBEB8C8, false);

        if (kind == EntityScanData.Kind.HORSE && menu.projector() != null && menu.projector().hasPhysicalHorseStaging()) {
            graphics.drawString(font, "Return horse staging gear before removing its scan card.", 18, 211, 0xFFFFB98E, false);
        }
    }

    private void renderRow(GuiGraphics graphics, VirtualEquipmentSnapshots.Channel channel) {
        int y = localY(channel);
        graphics.drawString(font, channelName(channel), 88, y + 5, 0xFFC9CED7, false);

        ItemStack physical = menu.physicalStaging(channel);
        VirtualEquipmentSnapshots.Snapshot incoming = menu.incoming(channel);
        if (physical.isEmpty() && !incoming.stack().isEmpty()) {
            graphics.renderFakeItem(incoming.stack(), EntityProjectorMenu.STAGING_X + 1, y + 1);
        }

        ItemStack projected = menu.projected(channel).stack();
        if (!projected.isEmpty()) {
            graphics.renderFakeItem(projected, PROJECTED_X + 1, y + 1);
        }
    }

    @Override
    public void removed() {
        entityPreview.invalidate();
        super.removed();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            EntityScanData.Kind kind = menu.effectiveKind();
            VirtualEquipmentSnapshots.Channel[] channels = kind == EntityScanData.Kind.HORSE
                    ? horseChannels()
                    : kind == EntityScanData.Kind.HUMANOID ? humanoidChannels() : new VirtualEquipmentSnapshots.Channel[0];
            for (VirtualEquipmentSnapshots.Channel channel : channels) {
                int x = leftPos + PROJECTED_X;
                int y = topPos + localY(channel);
                if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                    if (!menu.projected(channel).stack().isEmpty()) {
                        sendAction(EntityWorkspaceActionPayload.Action.CLEAR_PROJECTED, channel);
                        pendingConflict = null;
                        refreshConflictButtons();
                        status = Component.literal("Cleared projected " + channelName(channel));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static int localY(VirtualEquipmentSnapshots.Channel channel) {
        return switch (channel) {
            case HEAD -> EntityProjectorMenu.HUMANOID_FIRST_Y;
            case CHEST -> EntityProjectorMenu.HUMANOID_FIRST_Y + EntityProjectorMenu.ROW_STEP;
            case LEGS -> EntityProjectorMenu.HUMANOID_FIRST_Y + EntityProjectorMenu.ROW_STEP * 2;
            case FEET -> EntityProjectorMenu.HUMANOID_FIRST_Y + EntityProjectorMenu.ROW_STEP * 3;
            case MAIN_HAND -> EntityProjectorMenu.HUMANOID_FIRST_Y + EntityProjectorMenu.ROW_STEP * 4;
            case OFF_HAND -> EntityProjectorMenu.HUMANOID_FIRST_Y + EntityProjectorMenu.ROW_STEP * 5;
            case SADDLE -> EntityProjectorMenu.HORSE_FIRST_Y;
            case BODY -> EntityProjectorMenu.HORSE_FIRST_Y + EntityProjectorMenu.ROW_STEP;
        };
    }

    private static String channelName(VirtualEquipmentSnapshots.Channel channel) {
        return switch (channel) {
            case HEAD -> "Head";
            case CHEST -> "Chest";
            case LEGS -> "Legs";
            case FEET -> "Feet";
            case MAIN_HAND -> "Main Hand";
            case OFF_HAND -> "Off Hand";
            case SADDLE -> "Saddle";
            case BODY -> "Body Armor";
        };
    }

    private static VirtualEquipmentSnapshots.Channel[] humanoidChannels() {
        return new VirtualEquipmentSnapshots.Channel[]{
                VirtualEquipmentSnapshots.Channel.HEAD,
                VirtualEquipmentSnapshots.Channel.CHEST,
                VirtualEquipmentSnapshots.Channel.LEGS,
                VirtualEquipmentSnapshots.Channel.FEET,
                VirtualEquipmentSnapshots.Channel.MAIN_HAND,
                VirtualEquipmentSnapshots.Channel.OFF_HAND
        };
    }

    private static VirtualEquipmentSnapshots.Channel[] horseChannels() {
        return new VirtualEquipmentSnapshots.Channel[]{
                VirtualEquipmentSnapshots.Channel.SADDLE,
                VirtualEquipmentSnapshots.Channel.BODY
        };
    }

    private static void drawSlotFrame(GuiGraphics graphics, int x, int y, int border) {
        graphics.fill(x, y, x + 18, y + 18, border);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF171A20);
    }

    private static String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value == null ? "" : value;
        }
        return value.substring(0, Math.max(1, max - 1)) + "…";
    }
}
