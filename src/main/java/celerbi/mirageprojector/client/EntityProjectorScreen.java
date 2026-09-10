package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.entity.GenericPosePreset;
import celerbi.mirageprojector.entity.HorsePosePreset;
import celerbi.mirageprojector.entity.HumanoidPosePreset;
import celerbi.mirageprojector.entity.VirtualEquipmentSnapshots;
import celerbi.mirageprojector.menu.EntityProjectorMenu;
import celerbi.mirageprojector.network.EntityWorkspaceActionPayload;
import celerbi.mirageprojector.network.OpenProjectorWorkspacePayload;
import celerbi.mirageprojector.network.SetProjectionSourcePayload;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class EntityProjectorScreen extends AbstractContainerScreen<EntityProjectorMenu> {
    private static final int PANEL_WIDTH = 570;
    private static final int PANEL_HEIGHT = 438;

    private static final int APPLY_X = 52;
    private static final int CHANNEL_X = 86;
    private static final int PROJECTED_X = 326;
    private static final int VISIBILITY_X = 347;

    private static final int PREVIEW_X = 380;
    private static final int PREVIEW_Y = 30;
    private static final int PREVIEW_W = 180;
    private static final int PREVIEW_H = 240;

    private static final int ACTION_Y = 283;
    private static final int STATUS_Y = 308;

    private final Map<VirtualEquipmentSnapshots.Channel, Button> applyButtons =
            new EnumMap<>(VirtualEquipmentSnapshots.Channel.class);
    private final Map<VirtualEquipmentSnapshots.Channel, Button> visibilityButtons =
            new EnumMap<>(VirtualEquipmentSnapshots.Channel.class);
    private final EntityProjectionPreviewRenderer entityPreview = new EntityProjectionPreviewRenderer();

    private Button captureLoadoutButton;
    private Button returnGearButton;
    private Button poseButton;
    private Button confirmReplaceButton;
    private Button cancelReplaceButton;
    private VirtualEquipmentSnapshots.Channel pendingConflict;
    private Component status = Component.translatable("gui.mirage_projector.entity.ready");
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
        createApplyButtons();
        createVisibilityButtons();

        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.entity.use_projection"), button ->
                PacketDistributor.sendToServer(new SetProjectionSourcePayload(
                        menu.projectorPos(),
                        ProjectionSettings.SourceMode.ENTITY
                ))
        ).bounds(leftPos + 286, topPos + 6, 130, 18).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.mirage_projector.entity.projection_settings"), button ->
                PacketDistributor.sendToServer(new OpenProjectorWorkspacePayload(menu.projectorPos()))
        ).bounds(leftPos + 422, topPos + 6, 138, 18).build());

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
        ).bounds(leftPos + 18, topPos + ACTION_Y, 110, 20).build());
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
        ).bounds(leftPos + 134, topPos + ACTION_Y, 100, 20).build());
        returnGearButton.setTooltip(Tooltip.create(Component.translatable(
                "tooltip.mirage_projector.entity.return_gear"
        )));

        poseButton = addRenderableWidget(Button.builder(Component.empty(), button -> cyclePose())
                .bounds(leftPos + 240, topPos + ACTION_Y, 120, 20)
                .build());
        poseButton.setTooltip(Tooltip.create(Component.translatable(
                "tooltip.mirage_projector.entity.pose"
        )));

        confirmReplaceButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.entity.replace"),
                button -> {
                    if (pendingConflict != null) {
                        VirtualEquipmentSnapshots.Channel replaced = pendingConflict;
                        sendAction(EntityWorkspaceActionPayload.Action.REPLACE, replaced);
                        status = Component.translatable(
                                "gui.mirage_projector.entity.replaced_status",
                                channelComponent(replaced)
                        );
                    }
                    pendingConflict = null;
                    refreshConflictButtons();
                }
        ).bounds(leftPos + 240, topPos + ACTION_Y, 76, 20).build());

        cancelReplaceButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.cancel"),
                button -> {
                    pendingConflict = null;
                    status = Component.translatable("gui.mirage_projector.entity.replace_cancelled");
                    refreshConflictButtons();
                }
        ).bounds(leftPos + 322, topPos + ACTION_Y, 76, 20).build());

        updateModeWidgets(true);
        refreshActionableButtons();
        refreshVisibilityButtons();
        refreshConflictButtons();
        refreshPoseButton();
    }

    private void createApplyButtons() {
        for (VirtualEquipmentSnapshots.Channel channel : VirtualEquipmentSnapshots.Channel.values()) {
            Button button = addRenderableWidget(Button.builder(Component.literal("✓"), ignored -> apply(channel))
                    .bounds(leftPos + APPLY_X, topPos + localY(channel), 22, 18)
                    .build());
            button.setTooltip(Tooltip.create(Component.translatable(
                    "tooltip.mirage_projector.entity.apply"
            )));
            applyButtons.put(channel, button);
        }
    }

    private void createVisibilityButtons() {
        for (VirtualEquipmentSnapshots.Channel channel : VirtualEquipmentSnapshots.Channel.values()) {
            Button button = addRenderableWidget(Button.builder(Component.empty(), ignored -> toggleVisibility(channel))
                    .bounds(leftPos + VISIBILITY_X, topPos + localY(channel), 25, 18)
                    .build());
            visibilityButtons.put(channel, button);
        }
        refreshVisibilityButtons();
    }

    private void toggleVisibility(VirtualEquipmentSnapshots.Channel channel) {
        if (menu.projected(channel).stack().isEmpty()) {
            return;
        }
        boolean nextVisible = !menu.state().isEquipmentVisible(channel);
        sendAction(EntityWorkspaceActionPayload.Action.TOGGLE_VISIBILITY, channel);
        status = Component.translatable(
                nextVisible
                        ? "gui.mirage_projector.entity.visibility_shown_status"
                        : "gui.mirage_projector.entity.visibility_hidden_status",
                channelComponent(channel)
        );
    }

    private void refreshVisibilityButtons() {
        EntityScanData.Kind kind = menu.effectiveKind();
        for (Map.Entry<VirtualEquipmentSnapshots.Channel, Button> entry : visibilityButtons.entrySet()) {
            VirtualEquipmentSnapshots.Channel channel = entry.getKey();
            Button button = entry.getValue();
            boolean supported = switch (kind) {
                case HUMANOID -> channel.humanoid();
                case HORSE -> channel.horse();
                case GENERIC -> false;
            };
            boolean hasSnapshot = supported && !menu.projected(channel).stack().isEmpty();
            boolean visible = menu.state().isEquipmentVisible(channel);
            button.visible = supported;
            button.active = hasSnapshot;
            button.setMessage(Component.translatable(
                    visible
                            ? "gui.mirage_projector.entity.visibility.on"
                            : "gui.mirage_projector.entity.visibility.off"
            ));
            button.setTooltip(Tooltip.create(Component.translatable(
                    visible
                            ? "tooltip.mirage_projector.entity.visibility.hide"
                            : "tooltip.mirage_projector.entity.visibility.show",
                    channelComponent(channel)
            )));
        }
    }

    private void cyclePose() {
        EntityScanData.Kind kind = menu.effectiveKind();
        Component next;
        if (kind == EntityScanData.Kind.HORSE) {
            next = horsePoseName(menu.state().horsePose().next());
        } else if (kind == EntityScanData.Kind.HUMANOID) {
            next = humanoidPoseName(menu.state().humanoidPose().next());
        } else if (kind == EntityScanData.Kind.GENERIC && menu.supportsGenericSittingPose()) {
            next = genericPoseName(menu.state().genericPose().next());
        } else {
            return;
        }
        PacketDistributor.sendToServer(new EntityWorkspaceActionPayload(
                menu.projectorPos(),
                EntityWorkspaceActionPayload.Action.CYCLE_POSE,
                null
        ));
        status = Component.translatable("gui.mirage_projector.entity.pose_status", next);
    }

    private void apply(VirtualEquipmentSnapshots.Channel channel) {
        if (!menu.hasActionableIncoming(channel)) {
            status = Component.translatable(
                    "gui.mirage_projector.entity.nothing_staged",
                    channelComponent(channel)
            );
            return;
        }

        ItemStack incoming = menu.incoming(channel).stack();
        if (menu.hasConflict(channel)) {
            pendingConflict = channel;
            ItemStack current = menu.projected(channel).stack();
            status = Component.translatable(
                    "gui.mirage_projector.entity.replace_question",
                    Component.literal(truncate(current.getHoverName().getString(), 18)),
                    Component.literal(truncate(incoming.getHoverName().getString(), 18))
            );
            refreshConflictButtons();
            return;
        }

        sendAction(EntityWorkspaceActionPayload.Action.APPLY, channel);
        status = Component.translatable(
                "gui.mirage_projector.entity.applied_status",
                channelComponent(channel)
        );
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
        captureLoadoutButton.visible = kind == EntityScanData.Kind.HUMANOID;
        returnGearButton.visible = kind == EntityScanData.Kind.HUMANOID || kind == EntityScanData.Kind.HORSE;
        refreshConflictButtons();
    }

    private void refreshActionableButtons() {
        EntityScanData.Kind kind = menu.effectiveKind();
        for (Map.Entry<VirtualEquipmentSnapshots.Channel, Button> entry : applyButtons.entrySet()) {
            VirtualEquipmentSnapshots.Channel channel = entry.getKey();
            boolean supported = switch (kind) {
                case HUMANOID -> channel.humanoid();
                case HORSE -> channel.horse();
                case GENERIC -> false;
            };
            entry.getValue().visible = supported && menu.hasActionableIncoming(channel);
            entry.getValue().active = entry.getValue().visible;
        }

        if (pendingConflict != null && !menu.hasConflict(pendingConflict)) {
            pendingConflict = null;
            refreshConflictButtons();
        }
    }

    private void refreshConflictButtons() {
        boolean conflict = pendingConflict != null;
        if (confirmReplaceButton != null) {
            confirmReplaceButton.visible = conflict;
        }
        if (cancelReplaceButton != null) {
            cancelReplaceButton.visible = conflict;
        }
        if (poseButton != null) {
            poseButton.visible = !conflict
                    && (lastKind == EntityScanData.Kind.HUMANOID
                    || lastKind == EntityScanData.Kind.HORSE
                    || (lastKind == EntityScanData.Kind.GENERIC && menu.supportsGenericSittingPose()));
        }
    }

    private void refreshPoseButton() {
        if (poseButton == null) {
            return;
        }
        EntityScanData.Kind kind = menu.effectiveKind();
        boolean supported = kind == EntityScanData.Kind.HUMANOID
                || kind == EntityScanData.Kind.HORSE
                || (kind == EntityScanData.Kind.GENERIC && menu.supportsGenericSittingPose());
        poseButton.visible = pendingConflict == null && supported;
        if (!supported) {
            return;
        }
        Component pose = switch (kind) {
            case HUMANOID -> humanoidPoseName(menu.state().humanoidPose());
            case HORSE -> horsePoseName(menu.state().horsePose());
            case GENERIC -> genericPoseName(menu.state().genericPose());
        };
        poseButton.setMessage(Component.translatable("gui.mirage_projector.entity.pose", pose));
    }

    private static Component humanoidPoseName(HumanoidPosePreset pose) {
        return Component.translatable("gui.mirage_projector.entity.pose." + pose.serializedName());
    }

    private static Component horsePoseName(HorsePosePreset pose) {
        return Component.translatable("gui.mirage_projector.entity.horse_pose." + pose.serializedName());
    }

    private static Component genericPoseName(GenericPosePreset pose) {
        return Component.translatable("gui.mirage_projector.entity.generic_pose." + pose.serializedName());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateModeWidgets(false);
        refreshActionableButtons();
        refreshVisibilityButtons();
        refreshPoseButton();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF014171D);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 2, 0xFF6B4A7E);

        section(graphics, x + 10, y + 30, 360, 64);
        section(graphics, x + 10, y + 100, 360, 170);
        section(graphics, x + 10, y + 276, 550, 44);
        section(graphics, x + 10, y + 326, 550, 102);
        section(graphics, x + PREVIEW_X, y + PREVIEW_Y, PREVIEW_W, PREVIEW_H);

        drawSlotFrame(graphics, x + EntityProjectorMenu.CARD_X - 1, y + EntityProjectorMenu.CARD_Y - 1, 0xFF75658A);

        EntityScanData.Kind kind = menu.effectiveKind();
        if (kind == EntityScanData.Kind.HUMANOID) {
            for (VirtualEquipmentSnapshots.Channel channel : humanoidChannels()) {
                drawEquipmentFrames(graphics, x, y, channel);
            }
        } else if (kind == EntityScanData.Kind.HORSE) {
            for (VirtualEquipmentSnapshots.Channel channel : horseChannels()) {
                drawEquipmentFrames(graphics, x, y, channel);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotFrame(
                        graphics,
                        x + EntityProjectorMenu.PLAYER_INV_X + col * 18 - 1,
                        y + EntityProjectorMenu.PLAYER_INV_Y + row * 18 - 1,
                        0xFF5A5361
                );
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotFrame(
                    graphics,
                    x + EntityProjectorMenu.PLAYER_INV_X + col * 18 - 1,
                    y + EntityProjectorMenu.PLAYER_INV_Y + 58 - 1,
                    0xFF5A5361
            );
        }

        renderPreviewPanel(graphics, mouseX, mouseY);
    }

    private void drawEquipmentFrames(
            GuiGraphics graphics,
            int x,
            int y,
            VirtualEquipmentSnapshots.Channel channel
    ) {
        int rowY = y + localY(channel);
        drawSlotFrame(graphics, x + EntityProjectorMenu.STAGING_X - 1, rowY - 1, 0xFF536675);
        drawSlotFrame(
                graphics,
                x + PROJECTED_X - 1,
                rowY - 1,
                menu.hasConflict(channel) ? 0xFF9C5A52 : 0xFF53675C
        );
    }

    private void renderPreviewPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = leftPos + PREVIEW_X;
        int y = topPos + PREVIEW_Y;
        graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.entity.preview"), x + PREVIEW_W / 2, y + 8, 0xFFF2ECFF);

        var active = menu.state().activeEntity();
        boolean storedBodylessEquipment = active.isEmpty() && menu.state().hasProjectedHumanoidEquipment();
        boolean bodylessMannequin = storedBodylessEquipment && menu.state().hasVisibleProjectedHumanoidEquipment();
        if (active.isPresent() || bodylessMannequin) {
            ProjectionSettings previewSettings = menu.projector() == null
                    ? ProjectionSettings.DEFAULT
                    : menu.projector().settings();
            boolean rendered = entityPreview.render(
                    graphics,
                    menu.state(),
                    previewSettings,
                    x + 8,
                    y + 25,
                    PREVIEW_W - 16,
                    PREVIEW_H - 58,
                    mouseX,
                    mouseY
            );

            if (!rendered) {
                graphics.drawCenteredString(font, Component.translatable("gui.mirage_projector.entity.preview_unavailable"), x + PREVIEW_W / 2, y + 98, 0xFFFFC884);
            }

            String footer;
            if (active.isPresent()) {
                EntityScanData.View scan = active.get();
                footer = scan.hasProjectionNameplate()
                        ? scan.projectionNameplateText() + " · " + scan.entityType()
                        : scan.playerSource() && scan.hasFrozenPlayerTexture()
                        ? scan.displayName() + " · frozen skin"
                        : scan.displayName();
            } else {
                footer = Component.translatable("gui.mirage_projector.entity.virtual_mannequin").getString();
            }
            graphics.drawCenteredString(
                    font,
                    fit(footer, PREVIEW_W - 16),
                    x + PREVIEW_W / 2,
                    y + PREVIEW_H - 17,
                    0xFF9CA3AF
            );
            return;
        }

        graphics.drawCenteredString(
                font,
                Component.translatable(
                        storedBodylessEquipment
                                ? "gui.mirage_projector.entity.all_equipment_hidden"
                                : "gui.mirage_projector.entity.no_body"
                ),
                x + PREVIEW_W / 2,
                y + 96,
                0xFF9CA3AF
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, fit(title.getString(), 260), 10, 9, 0xFFF4F4F4, false);

        graphics.drawString(font, Component.translatable("gui.mirage_projector.entity.source_section"), 18, 37, 0xFFD7B8F5, false);
        ItemStack card = menu.cardStack();
        String source = card.isEmpty()
                ? Component.translatable("gui.mirage_projector.entity.scan_hint").getString()
                : card.getHoverName().getString();
        graphics.drawString(font, fit(source, 292), 58, 64, 0xFFD8C5EB, false);

        graphics.drawString(font, Component.translatable("gui.mirage_projector.entity.incoming"), 18, 106, 0xFFAFD8EE, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.entity.channel"), CHANNEL_X, 106, 0xFFC9CED7, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.entity.projected"), 286, 106, 0xFFB9E4C0, false);
        graphics.drawString(font, Component.translatable("gui.mirage_projector.entity.visibility"), 347, 106, 0xFFC9CED7, false);

        EntityScanData.Kind kind = menu.effectiveKind();
        if (kind == EntityScanData.Kind.HUMANOID) {
            for (VirtualEquipmentSnapshots.Channel channel : humanoidChannels()) {
                renderRow(graphics, channel);
            }
        } else if (kind == EntityScanData.Kind.HORSE) {
            for (VirtualEquipmentSnapshots.Channel channel : horseChannels()) {
                renderRow(graphics, channel);
            }
        } else {
            graphics.drawString(font, fit(Component.translatable("gui.mirage_projector.entity.generic_1").getString(), 330), 18, 136, 0xFFC4C9D3, false);
            graphics.drawString(font, fit(Component.translatable("gui.mirage_projector.entity.generic_2").getString(), 330), 18, 151, 0xFF8F98A8, false);
        }

        graphics.drawString(font, fit(status.getString(), 532), 18, STATUS_Y, pendingConflict == null ? 0xFFE7DCF5 : 0xFFFFB98E, false);
        graphics.drawString(font, Component.translatable("container.inventory"), EntityProjectorMenu.PLAYER_INV_X, EntityProjectorMenu.PLAYER_INV_Y - 12, 0xFFBEB8C8, false);
    }

    private void renderRow(GuiGraphics graphics, VirtualEquipmentSnapshots.Channel channel) {
        int y = localY(channel);
        graphics.drawString(font, fit(channelComponent(channel).getString(), 190), CHANNEL_X, y + 5, 0xFFC9CED7, false);

        ItemStack physical = menu.physicalStaging(channel);
        VirtualEquipmentSnapshots.Snapshot incoming = menu.incoming(channel);
        if (physical.isEmpty() && menu.hasActionableIncoming(channel) && !incoming.stack().isEmpty()) {
            graphics.renderFakeItem(incoming.stack(), EntityProjectorMenu.STAGING_X, y);
        }

        ItemStack projected = menu.projected(channel).stack();
        if (!projected.isEmpty()) {
            graphics.renderFakeItem(projected, PROJECTED_X, y);
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
                    : kind == EntityScanData.Kind.HUMANOID
                    ? humanoidChannels()
                    : new VirtualEquipmentSnapshots.Channel[0];
            for (VirtualEquipmentSnapshots.Channel channel : channels) {
                int x = leftPos + PROJECTED_X;
                int y = topPos + localY(channel);
                if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                    if (!menu.projected(channel).stack().isEmpty()) {
                        sendAction(EntityWorkspaceActionPayload.Action.CLEAR_PROJECTED, channel);
                        pendingConflict = null;
                        refreshConflictButtons();
                        status = Component.translatable(
                                "gui.mirage_projector.entity.cleared_status",
                                channelComponent(channel)
                        );
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

    private static Component channelComponent(VirtualEquipmentSnapshots.Channel channel) {
        return Component.translatable("gui.mirage_projector.entity.channel." + channel.serializedName());
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

    private static void section(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0xA20B0E13);
        graphics.fill(x, y, x + 2, y + h, 0xFF4C3858);
    }

    private static void drawSlotFrame(GuiGraphics graphics, int x, int y, int border) {
        graphics.fill(x, y, x + 18, y + 18, border);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF171A20);
    }

    private String fit(String value, int maxWidth) {
        if (value == null || value.isEmpty() || font.width(value) <= maxWidth) {
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

    private static String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value == null ? "" : value;
        }
        return value.substring(0, Math.max(1, max - 1)) + "…";
    }
}
