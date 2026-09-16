package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.compat.EasyMobFarmCompat;
import celerbi.mirageprojector.entity.EntityProjectionState;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.entity.VirtualEquipmentSnapshots;
import celerbi.mirageprojector.menu.ScanCodexMenu;
import celerbi.mirageprojector.network.LecternScanCodexActionPayload;
import celerbi.mirageprojector.network.OpenScanCodexPayload;
import celerbi.mirageprojector.network.ScanCodexActionPayload;
import celerbi.mirageprojector.registry.ModItems;
import celerbi.mirageprojector.scan.ScanCodexEntrySummary;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Scrollable Scan Codex library shared by handheld and vanilla-lectern modes. */
public final class ScanCodexScreen extends AbstractContainerScreen<ScanCodexMenu> {
    private static final int BOOK_WIDTH = 460;
    private static final int BOOK_HEIGHT = 312;
    private static final int BOOK_X = 14;
    private static final int BOOK_Y = 8;
    private static final int LEFT_PAGE_X = BOOK_X + 12;
    private static final int ROW_HEIGHT = 22;
    private static final int VISIBLE_ROWS = 11;
    private static final int LIST_X = BOOK_X + 246;
    private static final int LIST_Y = BOOK_Y + 42;
    private static final int LIST_WIDTH = 190;
    private static final int LIST_HEIGHT = ROW_HEIGHT * VISIBLE_ROWS;

    private static final int COVER = 0xF06B4634;
    private static final int COVER_EDGE = 0xFF3E2922;
    private static final int PAGE = 0xFFF0DFC0;
    private static final int PAGE_SHADE = 0xFFE8D3AE;
    private static final int PAGE_LINE = 0xFFB89B73;
    private static final int INK = 0xFF211713;
    private static final int MUTED_INK = 0xFF4C392F;
    private static final int ROW = 0x44B89B73;
    private static final int ROW_HOVER = 0x668C6E84;
    private static final int ROW_SELECTED = 0x998C5A7A;
    private static final int EXTENSION_PAGE = 0xFFF2E1C2;

    private static final Set<String> FARM_MOBS = Set.of(
            "cow", "pig", "sheep", "chicken", "rabbit", "horse", "donkey", "mule",
            "goat", "llama", "trader_llama", "mooshroom"
    );
    private static final Set<String> NETHER_MOBS = Set.of(
            "blaze", "ghast", "hoglin", "zoglin", "piglin", "piglin_brute", "zombified_piglin",
            "magma_cube", "strider", "wither_skeleton", "wither"
    );
    private static final Set<String> END_MOBS = Set.of(
            "ender_dragon", "enderman", "endermite", "shulker"
    );
    private static final Set<String> PASSIVE_MISC_MOBS = Set.of(
            "villager", "wandering_trader", "iron_golem", "snow_golem", "allay"
    );

    private OpenScanCodexPayload snapshot;
    private UUID selectedId;
    private Category category = Category.ALL;
    private int scrollRow;
    private boolean detailView;
    private boolean importPanelOpen;

    private EditBox searchBox;
    private Button backButton;
    private Button favoriteButton;
    private Button deleteButton;
    private Button duplicateButton;
    private Button importToggleButton;
    private Button importButton;
    private Button takeCodexButton;

    private final EntityProjectionPreviewRenderer previewRenderer = new EntityProjectionPreviewRenderer();
    private final EntityProjectionState previewState = new EntityProjectionState();

    public ScanCodexScreen(ScanCodexMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = menu.lecternMode() ? 620 : 488;
        imageHeight = menu.lecternMode() ? 420 : 330;
        inventoryLabelX = ScanCodexMenu.PLAYER_INV_X;
        inventoryLabelY = ScanCodexMenu.PLAYER_INV_Y - 13;
        snapshot = new OpenScanCodexPayload(
                menu.codexId(),
                new UUID(0L, 0L),
                false,
                List.of(),
                new CompoundTag()
        );
    }

    public UUID codexId() {
        return menu.codexId();
    }

    public boolean isLecternMode(net.minecraft.core.BlockPos pos) {
        return menu.lecternMode() && menu.lecternPos().equals(pos);
    }

    public void acceptSnapshot(OpenScanCodexPayload updated) {
        if (!menu.codexId().equals(updated.codexId())) {
            return;
        }
        snapshot = updated;
        selectedId = isZero(updated.selectedScanId()) ? null : updated.selectedScanId();
        if (selectedId != null && snapshot.entries().stream().noneMatch(e -> e.scanId().equals(selectedId))) {
            selectedId = null;
            detailView = false;
        }
        rebuildPreview(updated.selectedScanRoot());
        clampScroll();
        updateControls();
    }

    @Override
    protected void init() {
        super.init();
        int bx = leftPos + BOOK_X;
        int by = topPos + BOOK_Y;

        searchBox = new EditBox(
                font,
                bx + 18,
                by + 42,
                190,
                18,
                Component.translatable("gui.mirage_projector.scan_codex.search")
        );
        searchBox.setMaxLength(80);
        searchBox.setHint(Component.translatable("gui.mirage_projector.scan_codex.search"));
        searchBox.setBordered(false);
        searchBox.setTextColor(INK);
        searchBox.setResponder(value -> {
            scrollRow = 0;
            clampScroll();
        });
        addRenderableWidget(searchBox);

        backButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.back"),
                button -> leaveDetail()
        ).bounds(bx + 14, by + 15, 52, 18).build());

        favoriteButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.scan_codex.favorite"),
                button -> selectedEntry().ifPresent(entry -> sendEntryAction(
                        entry.scanId(),
                        ScanCodexActionPayload.Action.TOGGLE_FAVORITE,
                        LecternScanCodexActionPayload.Action.TOGGLE_FAVORITE
                ))
        ).bounds(bx + 250, by + 260, 90, 20).build());

        deleteButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.scan_codex.delete"),
                button -> selectedEntry().ifPresent(entry -> {
                    UUID deleting = entry.scanId();
                    requestReturnDuplicateCard();
                    detailView = false;
                    previewRenderer.invalidate();
                    menu.setDuplicatePanelOpen(false);
                    sendEntryAction(
                            deleting,
                            ScanCodexActionPayload.Action.DELETE,
                            LecternScanCodexActionPayload.Action.DELETE
                    );
                    updateControls();
                })
        ).bounds(bx + 346, by + 260, 90, 20).build());

        if (menu.lecternMode()) {
            duplicateButton = addRenderableWidget(Button.builder(
                    Component.translatable("gui.mirage_projector.scan_codex.duplicate"),
                    button -> selectedEntry().ifPresent(entry -> sendLectern(
                            entry.scanId(),
                            LecternScanCodexActionPayload.Action.DUPLICATE
                    ))
            ).bounds(leftPos + 488, topPos + 205, 112, 20).build());

            importToggleButton = addRenderableWidget(Button.builder(
                    Component.translatable("gui.mirage_projector.scan_codex.import.toggle"),
                    button -> {
                        boolean closing = importPanelOpen;
                        importPanelOpen = !importPanelOpen;
                        if (closing) {
                            requestReturnImportCard();
                        }
                        menu.setImportPanelOpen(importPanelOpen);
                        updateControls();
                    }
            ).bounds(bx + BOOK_WIDTH - 91, by + 15, 78, 18).build());

            importButton = addRenderableWidget(Button.builder(
                    Component.translatable("gui.mirage_projector.scan_codex.import.action"),
                    button -> sendLectern(
                            new UUID(0L, 0L),
                            LecternScanCodexActionPayload.Action.IMPORT_CARD
                    )
            ).bounds(leftPos + 488, topPos + 136, 112, 20).build());

            takeCodexButton = addRenderableWidget(Button.builder(
                    Component.translatable("gui.mirage_projector.scan_codex.take_codex"),
                    button -> {
                        sendLectern(new UUID(0L, 0L), LecternScanCodexActionPayload.Action.TAKE_CODEX);
                        onClose();
                    }
            ).bounds(leftPos + 488, topPos + 294, 112, 20).build());
        }

        OpenScanCodexPayload cached = ClientScanCodex.latest();
        if (cached != null && cached.codexId().equals(menu.codexId())) {
            acceptSnapshot(cached);
        } else {
            updateControls();
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Intentional: physical Codex overlays the live world without vanilla blur/dimming.
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Intentionally empty. render() owns the single Codex canvas pass so the book remains
        // visible even though renderBackground() is suppressed to avoid vanilla blur/dimming.
    }

    private void renderCodexCanvas(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBook(graphics);
        if (detailView) {
            renderDetail(graphics, mouseX, mouseY);
        } else {
            renderLibrary(graphics, mouseX, mouseY);
        }

        if (menu.lecternMode()) {
            renderLecternInventory(graphics);
            if (detailView) {
                renderDuplicateExtension(graphics);
            } else if (importPanelOpen) {
                renderImportExtension(graphics);
            } else {
                renderLecternHomeExtension(graphics);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.lecternMode()) {
            graphics.drawString(
                    font,
                    Component.translatable("container.inventory"),
                    inventoryLabelX,
                    inventoryLabelY,
                    MUTED_INK,
                    false
            );
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // renderBackground() is deliberately a no-op, therefore the parchment/book must be
        // rendered explicitly exactly once before widgets and slots. Keeping renderBg() empty
        // prevents the historical double-text/double-canvas regression.
        renderCodexCanvas(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderBook(GuiGraphics graphics) {
        int x = leftPos + BOOK_X;
        int y = topPos + BOOK_Y;
        graphics.fill(x, y, x + BOOK_WIDTH, y + BOOK_HEIGHT, COVER_EDGE);
        graphics.fill(x + 3, y + 3, x + BOOK_WIDTH - 3, y + BOOK_HEIGHT - 3, COVER);
        graphics.fill(x + 12, y + 7, x + 222, y + BOOK_HEIGHT - 7, PAGE);
        graphics.fill(x + 238, y + 7, x + 448, y + BOOK_HEIGHT - 7, PAGE_SHADE);
        graphics.fill(x + 225, y + 7, x + 235, y + BOOK_HEIGHT - 7, 0xFF8B684A);
        graphics.fill(x + 226, y + 7, x + 228, y + BOOK_HEIGHT - 7, 0x55614A38);
        graphics.fill(x + 232, y + 7, x + 234, y + BOOK_HEIGHT - 7, 0x55FFF0D5);

        if (!detailView) {
            graphics.drawString(font, title, x + 18, y + 17, INK, false);
            // Search field belongs to the parchment rather than rendering as a floating black
            // vanilla textbox over the live world.
            graphics.fill(x + 17, y + 38, x + 211, y + 64, PAGE_LINE);
            graphics.fill(x + 19, y + 40, x + 209, y + 62, 0xFFF7E9CD);
            graphics.drawString(
                    font,
                    Component.translatable("gui.mirage_projector.scan_codex.count", snapshot.entries().size()),
                    x + 244,
                    y + 17,
                    MUTED_INK,
                    false
            );
            renderTabs(graphics, x, y);
        }
    }

    private void renderTabs(GuiGraphics graphics, int x, int y) {
        Category[] values = Category.values();
        int tabWidth = 91;
        int tabHeight = 16;
        int gap = 4;
        for (int i = 0; i < values.length; i++) {
            int row = i / 2;
            int col = i % 2;
            int tx = x + 18 + col * (tabWidth + gap);
            int ty = y + 68 + row * 18;
            int bg = values[i] == category ? 0xFF8C5A7A : 0xFFB89B73;
            graphics.fill(tx, ty, tx + tabWidth, ty + tabHeight, bg);
            graphics.drawCenteredString(font, values[i].label(), tx + tabWidth / 2, ty + 4, INK);
        }
    }

    private void renderLibrary(GuiGraphics graphics, int mouseX, int mouseY) {
        List<ScanCodexEntrySummary> filtered = filteredEntries();
        clampScroll(filtered.size());
        int x = leftPos + LIST_X;
        int y = topPos + LIST_Y;
        int width = LIST_WIDTH;

        graphics.fill(x, y - 5, x + width, y - 4, PAGE_LINE);
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = scrollRow + row;
            if (index >= filtered.size()) {
                break;
            }
            ScanCodexEntrySummary entry = filtered.get(index);
            int ry = y + row * ROW_HEIGHT;
            boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= ry && mouseY < ry + ROW_HEIGHT - 2;
            boolean selected = entry.scanId().equals(selectedId);
            graphics.fill(x, ry, x + width, ry + ROW_HEIGHT - 2,
                    selected ? ROW_SELECTED : hovered ? ROW_HOVER : ROW);

            String star = entry.favorite() ? "★" : "☆";
            graphics.drawString(font, star, x + 6, ry + 6,
                    entry.favorite() ? 0xFF9B721B : MUTED_INK, false);
            graphics.drawString(font, trim(entry.displayName(), 36), x + 22, ry + 4, INK, false);
            String subtitle = !entry.nameplateText().isBlank()
                    ? entry.nameplateText()
                    : entry.entityType().toString();
            graphics.drawString(font, trim(subtitle, 47), x + 22, ry + 12, MUTED_INK, false);
        }

        int max = Math.max(0, filtered.size() - VISIBLE_ROWS);
        if (max > 0) {
            int barX = x + width - 4;
            int barTop = y;
            int barHeight = LIST_HEIGHT - 2;
            graphics.fill(barX, barTop, barX + 3, barTop + barHeight, 0x55392B24);
            int thumb = Math.max(18, Math.round(barHeight * (VISIBLE_ROWS / (float) filtered.size())));
            int travel = barHeight - thumb;
            int thumbY = barTop + Math.round(travel * (scrollRow / (float) max));
            graphics.fill(barX, thumbY, barX + 3, thumbY + thumb, 0xFF8C5A7A);
        }

        if (filtered.isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    Component.translatable("gui.mirage_projector.scan_codex.no_results"),
                    x + width / 2,
                    y + 62,
                    MUTED_INK
            );
        }
    }

    private void renderDetail(GuiGraphics graphics, int mouseX, int mouseY) {
        Optional<ScanCodexEntrySummary> selected = selectedEntry();
        if (selected.isEmpty()) {
            return;
        }
        ScanCodexEntrySummary entry = selected.get();
        int bx = leftPos + BOOK_X;
        int by = topPos + BOOK_Y;

        String titleText = !entry.nameplateText().isBlank() ? entry.nameplateText() : entry.displayName();
        graphics.drawCenteredString(font, trim(titleText, 32), bx + 116, by + 20, INK);
        graphics.drawString(font, trim(entry.entityType().toString(), 28), bx + 245, by + 21, MUTED_INK, false);

        // Give the entity almost the full left page. The viewport is intentionally taller and
        // starts slightly lower so tall mobs keep their head inside the page instead of clipping.
        graphics.fill(bx + 18, by + 38, bx + 220, by + 246, 0x33B89B73);
        previewRenderer.render(
                graphics,
                previewState,
                ProjectionSettings.DEFAULT,
                bx + 20,
                by + 46,
                198,
                196,
                mouseX,
                mouseY
        );

        int x = bx + 248;
        int y = by + 48;
        graphics.drawString(font, Component.translatable("gui.mirage_projector.scan_codex.detail.appearance"), x, y, INK, false);
        y += 17;
        graphics.drawString(font, Component.translatable("gui.mirage_projector.scan_codex.detail.category", categoryOf(entry).label()), x, y, MUTED_INK, false);
        y += 14;
        if (!entry.nameplateText().isBlank()) {
            graphics.drawString(font,
                    Component.translatable("gui.mirage_projector.scan_codex.nameplate", trim(entry.nameplateText(), 20)),
                    x, y, INK, false);
            y += 14;
        }
        graphics.drawString(font,
                Component.translatable("gui.mirage_projector.scan_codex.equipment", entry.equipmentCount()),
                x, y, INK, false);
        y += 18;
        renderEquipment(graphics, bx + 248, y);

        String id = entry.scanId().toString();
        graphics.drawString(font, Component.literal("ID " + id.substring(0, 8) + "…"), bx + 248, by + 238, MUTED_INK, false);
    }

    private void renderEquipment(GuiGraphics graphics, int x, int y) {
        Optional<EntityScanData.View> view = EntityScanData.readRoot(snapshot.selectedScanRoot());
        if (view.isEmpty() || minecraft == null || minecraft.player == null) {
            return;
        }
        EntityScanData.View scan = view.get();
        List<ItemStack> stacks = new ArrayList<>();
        if (scan.kind() == EntityScanData.Kind.HORSE) {
            for (VirtualEquipmentSnapshots.Channel channel : EntityScanData.HORSE_CHANNELS) {
                ItemStack stack = EntityScanData.equipmentStack(scan.equipment(), channel, minecraft.level.registryAccess());
                if (!stack.isEmpty()) stacks.add(stack);
            }
        } else {
            for (EquipmentSlot slot : EntityScanData.HUMANOID_SLOTS) {
                ItemStack stack = EntityScanData.equipmentStack(scan.equipment(), slot, minecraft.level.registryAccess());
                if (!stack.isEmpty()) stacks.add(stack);
            }
        }
        if (stacks.isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.mirage_projector.scan_codex.detail.no_equipment"), x, y + 5, MUTED_INK, false);
            return;
        }
        for (int i = 0; i < Math.min(stacks.size(), 6); i++) {
            int sx = x + (i % 3) * 28;
            int sy = y + (i / 3) * 28;
            slotFrame(graphics, sx, sy, 0xFF8C6E84);
            graphics.renderItem(stacks.get(i), sx + 1, sy + 1);
        }
    }

    private void renderDuplicateExtension(GuiGraphics graphics) {
        int x = leftPos + 482;
        int y = topPos + 140;
        graphics.fill(x, y, x + 126, y + 100, COVER_EDGE);
        graphics.fill(x + 2, y + 2, x + 124, y + 98, EXTENSION_PAGE);
        graphics.drawCenteredString(font,
                Component.translatable("gui.mirage_projector.scan_codex.duplicate.extension"),
                x + 63, y + 12, INK);
        extensionSlotFrame(graphics,
                leftPos + ScanCodexMenu.DUPLICATE_SLOT_X,
                topPos + ScanCodexMenu.DUPLICATE_SLOT_Y,
                0xFF8C5A7A);
        graphics.drawString(font,
                fitText(Component.translatable("gui.mirage_projector.scan_codex.duplicate.card_hint").getString(), 82),
                x + 38, y + 37, MUTED_INK, false);
    }

    private void renderImportExtension(GuiGraphics graphics) {
        int x = leftPos + 482;
        int y = topPos + 60;
        graphics.fill(x, y, x + 126, y + 112, COVER_EDGE);
        graphics.fill(x + 2, y + 2, x + 124, y + 110, EXTENSION_PAGE);
        graphics.drawCenteredString(font,
                Component.translatable("gui.mirage_projector.scan_codex.import.extension"),
                x + 63, y + 10, INK);
        extensionSlotFrame(graphics,
                leftPos + ScanCodexMenu.IMPORT_SLOT_X,
                topPos + ScanCodexMenu.IMPORT_SLOT_Y,
                0xFF6C8A58);
        graphics.drawString(font,
                fitText(Component.translatable("gui.mirage_projector.scan_codex.import.card_hint").getString(), 82),
                x + 38, y + 32, MUTED_INK, false);
        graphics.drawString(font,
                fitText(Component.translatable("gui.mirage_projector.scan_codex.import.consumed_hint").getString(), 108),
                x + 8, y + 58, 0xFF8D4E3B, false);
        if (menu.easyMobFarmAvailable()) {
            graphics.drawString(font,
                    fitText(Component.translatable("gui.mirage_projector.scan_codex.import.easy_mob_farm_hint").getString(), 108),
                    x + 8, y + 71, MUTED_INK, false);
        }
    }

    private void renderLecternHomeExtension(GuiGraphics graphics) {
        int x = leftPos + 482;
        int y = topPos + 270;
        graphics.fill(x, y, x + 126, y + 58, COVER_EDGE);
        graphics.fill(x + 2, y + 2, x + 124, y + 56, EXTENSION_PAGE);
        graphics.drawCenteredString(
                font,
                Component.translatable("gui.mirage_projector.scan_codex.lectern_actions"),
                x + 63, y + 10, INK
        );
    }

    private void renderLecternInventory(GuiGraphics graphics) {
        int panelX = leftPos + 151;
        int panelY = topPos + 322;
        graphics.fill(panelX, panelY, panelX + 186, panelY + 92, 0xE8E8D3AE);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slotFrame(graphics,
                        leftPos + ScanCodexMenu.PLAYER_INV_X + col * 18 - 1,
                        topPos + ScanCodexMenu.PLAYER_INV_Y + row * 18 - 1,
                        0xFF8C7967);
            }
        }
        for (int col = 0; col < 9; col++) {
            slotFrame(graphics,
                    leftPos + ScanCodexMenu.PLAYER_INV_X + col * 18 - 1,
                    topPos + ScanCodexMenu.PLAYER_INV_Y + 58 - 1,
                    0xFF8C7967);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!detailView && button == 0 && clickTab(mouseX, mouseY)) {
            return true;
        }
        if (!detailView && button == 0 && clickEntry(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!detailView && insideList(mouseX, mouseY)) {
            int max = Math.max(0, filteredEntries().size() - VISIBLE_ROWS);
            if (max > 0 && scrollY != 0.0D) {
                scrollRow = Math.max(0, Math.min(max, scrollRow + (scrollY < 0 ? 1 : -1)));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean clickTab(double mouseX, double mouseY) {
        int x = leftPos + BOOK_X;
        int y = topPos + BOOK_Y;
        Category[] values = Category.values();
        int tabWidth = 91;
        int gap = 4;
        for (int i = 0; i < values.length; i++) {
            int row = i / 2;
            int col = i % 2;
            int tx = x + 18 + col * (tabWidth + gap);
            int ty = y + 68 + row * 18;
            if (mouseX >= tx && mouseX < tx + tabWidth && mouseY >= ty && mouseY < ty + 16) {
                category = values[i];
                scrollRow = 0;
                clampScroll();
                return true;
            }
        }
        return false;
    }

    private boolean clickEntry(double mouseX, double mouseY) {
        if (!insideList(mouseX, mouseY)) {
            return false;
        }
        int y = topPos + LIST_Y;
        int row = ((int) mouseY - y) / ROW_HEIGHT;
        List<ScanCodexEntrySummary> filtered = filteredEntries();
        int index = scrollRow + row;
        if (row < 0 || row >= VISIBLE_ROWS || index < 0 || index >= filtered.size()) {
            return false;
        }
        ScanCodexEntrySummary entry = filtered.get(index);
        if (importPanelOpen) {
            requestReturnImportCard();
        }
        selectedId = entry.scanId();
        detailView = true;
        importPanelOpen = false;
        menu.setImportPanelOpen(false);
        menu.setDuplicatePanelOpen(menu.lecternMode());
        sendEntryAction(
                entry.scanId(),
                ScanCodexActionPayload.Action.SELECT,
                LecternScanCodexActionPayload.Action.SELECT
        );
        updateControls();
        return true;
    }

    private boolean insideList(double mouseX, double mouseY) {
        int x = leftPos + LIST_X;
        int y = topPos + LIST_Y;
        int width = LIST_WIDTH;
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + LIST_HEIGHT;
    }

    private void leaveDetail() {
        requestReturnDuplicateCard();
        detailView = false;
        menu.setDuplicatePanelOpen(false);
        updateControls();
    }

    private void requestReturnDuplicateCard() {
        if (menu.lecternMode() && !menu.duplicateCard().isEmpty()) {
            sendLectern(new UUID(0L, 0L), LecternScanCodexActionPayload.Action.RETURN_DUPLICATE_CARD);
        }
    }

    private void requestReturnImportCard() {
        if (menu.lecternMode() && !menu.importCard().isEmpty()) {
            sendLectern(new UUID(0L, 0L), LecternScanCodexActionPayload.Action.RETURN_IMPORT_CARD);
        }
    }

    private List<ScanCodexEntrySummary> filteredEntries() {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        List<ScanCodexEntrySummary> result = new ArrayList<>();
        for (ScanCodexEntrySummary entry : snapshot.entries()) {
            if (!category.accepts(entry)) {
                continue;
            }
            if (!query.isBlank()) {
                String haystack = (entry.displayName() + " " + entry.nameplateText() + " " + entry.entityType())
                        .toLowerCase(Locale.ROOT);
                if (!haystack.contains(query)) {
                    continue;
                }
            }
            result.add(entry);
        }
        return result;
    }

    private Optional<ScanCodexEntrySummary> selectedEntry() {
        if (selectedId == null) {
            return Optional.empty();
        }
        return snapshot.entries().stream().filter(entry -> entry.scanId().equals(selectedId)).findFirst();
    }

    private void rebuildPreview(CompoundTag root) {
        previewRenderer.invalidate();
        previewState.clearActiveEntityBody();
        previewState.clearHumanoidWorkspace();
        previewState.clearHorseWorkspace();
        if (root == null || root.isEmpty() || minecraft == null || minecraft.player == null) {
            return;
        }
        ItemStack temp = new ItemStack(ModItems.ENTITY_SCAN_CARD.get());
        if (!EntityScanData.writeRootToCard(temp, root)) {
            return;
        }
        if (!previewState.importFromCard(temp, minecraft.level.registryAccess())) {
            return;
        }
        for (VirtualEquipmentSnapshots.Channel channel : VirtualEquipmentSnapshots.Channel.values()) {
            previewState.applyIncoming(channel, true);
        }
    }

    private void updateControls() {
        Optional<ScanCodexEntrySummary> selected = selectedEntry();
        if (searchBox != null) searchBox.visible = !detailView;
        if (backButton != null) backButton.visible = detailView;
        if (favoriteButton != null) {
            favoriteButton.visible = detailView;
            favoriteButton.active = detailView && selected.isPresent();
            favoriteButton.setMessage(Component.translatable(
                    selected.isPresent() && selected.get().favorite()
                            ? "gui.mirage_projector.scan_codex.unfavorite"
                            : "gui.mirage_projector.scan_codex.favorite"
            ));
        }
        if (deleteButton != null) {
            deleteButton.visible = detailView;
            deleteButton.active = detailView && selected.isPresent();
        }

        menu.setDuplicatePanelOpen(menu.lecternMode() && detailView);
        if (duplicateButton != null) {
            duplicateButton.visible = detailView;
            ItemStack card = menu.duplicateCard();
            duplicateButton.active = selected.isPresent()
                    && card.is(ModItems.ENTITY_SCAN_CARD.get())
                    && !EntityScanData.hasScan(card);
        }

        boolean canImport = menu.lecternMode() && !detailView;
        if (importToggleButton != null) {
            importToggleButton.visible = canImport;
            importToggleButton.setMessage(Component.translatable(
                    importPanelOpen
                            ? "gui.mirage_projector.scan_codex.import.hide"
                            : "gui.mirage_projector.scan_codex.import.toggle"
            ));
        }
        if (!canImport) {
            importPanelOpen = false;
        }
        menu.setImportPanelOpen(canImport && importPanelOpen);
        if (importButton != null) {
            importButton.visible = canImport && importPanelOpen;
            ItemStack card = menu.importCard();
            importButton.active = importButton.visible
                    && ((card.is(ModItems.ENTITY_SCAN_CARD.get()) && EntityScanData.hasScan(card))
                    || EasyMobFarmCompat.isCaptureCard(card));
        }
        if (takeCodexButton != null) {
            takeCodexButton.visible = menu.lecternMode() && !detailView && !importPanelOpen;
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateControls();
    }

    private void sendEntryAction(
            UUID scanId,
            ScanCodexActionPayload.Action handheldAction,
            LecternScanCodexActionPayload.Action lecternAction
    ) {
        if (menu.lecternMode()) {
            sendLectern(scanId, lecternAction);
        } else {
            PacketDistributor.sendToServer(new ScanCodexActionPayload(menu.codexId(), scanId, handheldAction));
        }
    }

    private void sendLectern(UUID scanId, LecternScanCodexActionPayload.Action action) {
        if (!menu.lecternMode()) {
            return;
        }
        PacketDistributor.sendToServer(new LecternScanCodexActionPayload(
                menu.lecternPos(),
                menu.codexId(),
                scanId,
                action
        ));
    }

    private void clampScroll() {
        clampScroll(filteredEntries().size());
    }

    private void clampScroll(int size) {
        scrollRow = Math.max(0, Math.min(Math.max(0, size - VISIBLE_ROWS), scrollRow));
    }

    private static Category categoryOf(ScanCodexEntrySummary entry) {
        if (entry.playerSource()) return Category.PLAYERS;
        String path = entry.entityType().getPath();
        if (NETHER_MOBS.contains(path)) return Category.NETHER;
        if (END_MOBS.contains(path)) return Category.END;
        if (FARM_MOBS.contains(path)) return Category.FARM;
        if (PASSIVE_MISC_MOBS.contains(path)) return Category.PASSIVE;
        var type = BuiltInRegistries.ENTITY_TYPE.get(entry.entityType());
        if (type != null) {
            MobCategory mobCategory = type.getCategory();
            if (mobCategory == MobCategory.MONSTER) return Category.HOSTILE;
            if (mobCategory == MobCategory.WATER_AMBIENT
                    || mobCategory == MobCategory.WATER_CREATURE
                    || mobCategory == MobCategory.UNDERGROUND_WATER_CREATURE
                    || mobCategory == MobCategory.AXOLOTLS) return Category.WATER;
            if (mobCategory == MobCategory.CREATURE || mobCategory == MobCategory.AMBIENT) return Category.PASSIVE;
        }
        return Category.OTHER;
    }

    private static void slotFrame(GuiGraphics graphics, int x, int y, int border) {
        graphics.fill(x, y, x + 18, y + 18, border);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFFF6E7CB);
    }

    private static void extensionSlotFrame(GuiGraphics graphics, int slotX, int slotY, int border) {
        // Two-pixel breathing room around the vanilla 16px item render keeps paper/card art from
        // visually touching the extension-page frame.
        graphics.fill(slotX - 2, slotY - 2, slotX + 18, slotY + 18, border);
        graphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFFF6E7CB);
    }

    private String fitText(String value, int maxWidth) {
        if (value == null || font.width(value) <= maxWidth) return value == null ? "" : value;
        String ellipsis = "…";
        int target = Math.max(0, maxWidth - font.width(ellipsis));
        int end = value.length();
        while (end > 0 && font.width(value.substring(0, end)) > target) end--;
        return value.substring(0, Math.max(0, end)) + ellipsis;
    }

    private static String trim(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static boolean isZero(UUID id) {
        return id == null || (id.getMostSignificantBits() == 0L && id.getLeastSignificantBits() == 0L);
    }

    private enum Category {
        ALL("gui.mirage_projector.scan_codex.tab.all"),
        FAVORITES("gui.mirage_projector.scan_codex.tab.favorites"),
        HOSTILE("gui.mirage_projector.scan_codex.tab.hostile"),
        PASSIVE("gui.mirage_projector.scan_codex.tab.passive"),
        FARM("gui.mirage_projector.scan_codex.tab.farm"),
        NETHER("gui.mirage_projector.scan_codex.tab.nether"),
        END("gui.mirage_projector.scan_codex.tab.end"),
        WATER("gui.mirage_projector.scan_codex.tab.water"),
        PLAYERS("gui.mirage_projector.scan_codex.tab.players"),
        OTHER("gui.mirage_projector.scan_codex.tab.other");

        private final String key;

        Category(String key) {
            this.key = key;
        }

        Component label() {
            return Component.translatable(key);
        }

        boolean accepts(ScanCodexEntrySummary entry) {
            if (this == ALL) return true;
            if (this == FAVORITES) return entry.favorite();
            return categoryOf(entry) == this;
        }
    }
}
