package celerbi.mirageprojector.client;

import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.item.ScanCodexItem;
import celerbi.mirageprojector.network.OpenScanCodexPayload;
import celerbi.mirageprojector.network.ScanCodexActionPayload;
import celerbi.mirageprojector.scan.ScanCodexEntrySummary;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

/** Searchable metadata browser for the server-backed Mirage Scan Codex library. */
public final class ScanCodexScreen extends Screen {
    private static final int PANEL_WIDTH = 430;
    private static final int PANEL_HEIGHT = 286;
    private static final int LIST_WIDTH = 252;
    private static final int ROW_HEIGHT = 22;
    private static final int PAGE_SIZE = 8;

    private OpenScanCodexPayload snapshot;
    private UUID selectedId;
    private Filter filter = Filter.ALL;
    private int page;
    private EditBox searchBox;
    private Button filterButton;
    private Button favoriteButton;
    private Button prevButton;
    private Button nextButton;

    public ScanCodexScreen(OpenScanCodexPayload snapshot) {
        super(Component.translatable("gui.mirage_projector.scan_codex.title"));
        this.snapshot = snapshot;
        this.selectedId = ScanCodexItem.isZero(snapshot.selectedScanId()) ? null : snapshot.selectedScanId();
    }

    public UUID codexId() {
        return snapshot.codexId();
    }

    public void acceptSnapshot(OpenScanCodexPayload updated) {
        if (!snapshot.codexId().equals(updated.codexId())) {
            return;
        }
        snapshot = updated;
        selectedId = ScanCodexItem.isZero(updated.selectedScanId()) ? null : updated.selectedScanId();
        if (selectedId != null && snapshot.entries().stream().noneMatch(entry -> entry.scanId().equals(selectedId))) {
            selectedId = null;
        }
        updateButtons();
    }

    @Override
    protected void init() {
        super.init();
        int left = left();
        int top = top();

        searchBox = new EditBox(
                font,
                left + 12,
                top + 30,
                154,
                18,
                Component.translatable("gui.mirage_projector.scan_codex.search")
        );
        searchBox.setMaxLength(80);
        searchBox.setHint(Component.translatable("gui.mirage_projector.scan_codex.search"));
        searchBox.setResponder(value -> {
            page = 0;
            updateButtons();
        });
        addRenderableWidget(searchBox);

        filterButton = addRenderableWidget(Button.builder(
                filter.label(),
                button -> {
                    filter = filter.next();
                    filterButton.setMessage(filter.label());
                    page = 0;
                    updateButtons();
                }
        ).bounds(left + 172, top + 29, 92, 20).build());

        prevButton = addRenderableWidget(Button.builder(
                Component.literal("<"),
                button -> {
                    page = Math.max(0, page - 1);
                    updateButtons();
                }
        ).bounds(left + 12, top + PANEL_HEIGHT - 31, 28, 20).build());

        nextButton = addRenderableWidget(Button.builder(
                Component.literal(">"),
                button -> {
                    page = Math.min(maxPage(filteredEntries()), page + 1);
                    updateButtons();
                }
        ).bounds(left + 44, top + PANEL_HEIGHT - 31, 28, 20).build());

        favoriteButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.scan_codex.favorite"),
                button -> selectedEntry().ifPresent(entry -> send(entry.scanId(), ScanCodexActionPayload.Action.TOGGLE_FAVORITE))
        ).bounds(left + LIST_WIDTH + 22, top + PANEL_HEIGHT - 56, 132, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.mirage_projector.scan_codex.close"),
                button -> onClose()
        ).bounds(left + LIST_WIDTH + 22, top + PANEL_HEIGHT - 31, 132, 20).build());

        updateButtons();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = left();
        int top = top();
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xF01A1517);
        graphics.fill(left + 2, top + 2, left + PANEL_WIDTH - 2, top + PANEL_HEIGHT - 2, 0xF02B2025);
        graphics.fill(left + LIST_WIDTH + 10, top + 23, left + LIST_WIDTH + 11, top + PANEL_HEIGHT - 10, 0xFF6A4C5E);

        graphics.drawString(font, title, left + 12, top + 10, 0xFFE9D9E7, false);
        graphics.drawString(
                font,
                Component.translatable("gui.mirage_projector.scan_codex.count", snapshot.entries().size()),
                left + 282,
                top + 11,
                0xFFBFAEC0,
                false
        );

        List<ScanCodexEntrySummary> filtered = filteredEntries();
        int maxPage = maxPage(filtered);
        page = Mth.clamp(page, 0, maxPage);
        int start = page * PAGE_SIZE;
        int rowTop = top + 56;
        for (int row = 0; row < PAGE_SIZE; row++) {
            int index = start + row;
            if (index >= filtered.size()) {
                break;
            }
            ScanCodexEntrySummary entry = filtered.get(index);
            int y = rowTop + row * ROW_HEIGHT;
            boolean selected = entry.scanId().equals(selectedId);
            boolean hovered = mouseX >= left + 12 && mouseX < left + LIST_WIDTH
                    && mouseY >= y && mouseY < y + ROW_HEIGHT - 2;
            int background = selected ? 0xCC594053 : hovered ? 0xAA43313F : 0x88332731;
            graphics.fill(left + 12, y, left + LIST_WIDTH, y + ROW_HEIGHT - 2, background);
            String star = entry.favorite() ? "★" : "☆";
            graphics.drawString(font, star, left + 17, y + 6, entry.favorite() ? 0xFFFFDA6A : 0xFF8E7E8A, false);
            graphics.drawString(font, trim(entry.displayName(), 25), left + 31, y + 4, 0xFFF2E8F0, false);
            graphics.drawString(font, trim(entry.entityType().toString(), 31), left + 31, y + 12, 0xFF9E8F9D, false);
        }

        graphics.drawString(
                font,
                Component.translatable("gui.mirage_projector.scan_codex.page", page + 1, maxPage + 1),
                left + 82,
                top + PANEL_HEIGHT - 25,
                0xFFBFAEC0,
                false
        );

        renderDetails(graphics, left, top);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = left();
            int top = top();
            List<ScanCodexEntrySummary> filtered = filteredEntries();
            int rowTop = top + 56;
            if (mouseX >= left + 12 && mouseX < left + LIST_WIDTH
                    && mouseY >= rowTop && mouseY < rowTop + PAGE_SIZE * ROW_HEIGHT) {
                int row = ((int) mouseY - rowTop) / ROW_HEIGHT;
                int index = page * PAGE_SIZE + row;
                if (index >= 0 && index < filtered.size()) {
                    ScanCodexEntrySummary entry = filtered.get(index);
                    selectedId = entry.scanId();
                    send(entry.scanId(), ScanCodexActionPayload.Action.SELECT);
                    updateButtons();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderDetails(GuiGraphics graphics, int left, int top) {
        int x = left + LIST_WIDTH + 22;
        int y = top + 32;
        var selected = selectedEntry();
        if (selected.isEmpty()) {
            graphics.drawString(
                    font,
                    Component.translatable("gui.mirage_projector.scan_codex.no_selection"),
                    x,
                    y,
                    0xFFA999A7,
                    false
            );
            return;
        }
        ScanCodexEntrySummary entry = selected.get();
        graphics.drawString(font, trim(entry.displayName(), 22), x, y, 0xFFFFFFFF, false);
        y += 17;
        graphics.drawString(font, trim(entry.entityType().toString(), 24), x, y, 0xFFB9AAB7, false);
        y += 15;
        graphics.drawString(font, categoryLabel(entry), x, y, 0xFFD5C3D1, false);
        y += 15;
        graphics.drawString(
                font,
                Component.translatable("gui.mirage_projector.scan_codex.equipment", entry.equipmentCount()),
                x,
                y,
                0xFFD5C3D1,
                false
        );
        y += 15;
        if (!entry.nameplateText().isBlank()) {
            graphics.drawString(
                    font,
                    Component.translatable("gui.mirage_projector.scan_codex.nameplate", trim(entry.nameplateText(), 20)),
                    x,
                    y,
                    0xFFD5C3D1,
                    false
            );
            y += 15;
        }
        String id = entry.scanId().toString();
        graphics.drawString(
                font,
                Component.literal("ID " + id.substring(0, 8) + "…"),
                x,
                y,
                0xFF806F7C,
                false
        );
        y += 18;
        graphics.drawString(
                font,
                Component.translatable(
                        entry.favorite()
                                ? "gui.mirage_projector.scan_codex.favorite_yes"
                                : "gui.mirage_projector.scan_codex.favorite_no"
                ),
                x,
                y,
                entry.favorite() ? 0xFFFFDA6A : 0xFF9A8995,
                false
        );
    }

    private List<ScanCodexEntrySummary> filteredEntries() {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        List<ScanCodexEntrySummary> result = new ArrayList<>();
        for (ScanCodexEntrySummary entry : snapshot.entries()) {
            if (!filter.accepts(entry)) {
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

    private java.util.Optional<ScanCodexEntrySummary> selectedEntry() {
        if (selectedId == null) {
            return java.util.Optional.empty();
        }
        return snapshot.entries().stream().filter(entry -> entry.scanId().equals(selectedId)).findFirst();
    }

    private void updateButtons() {
        List<ScanCodexEntrySummary> filtered = filteredEntries();
        int maxPage = maxPage(filtered);
        page = Mth.clamp(page, 0, maxPage);
        if (prevButton != null) {
            prevButton.active = page > 0;
        }
        if (nextButton != null) {
            nextButton.active = page < maxPage;
        }
        if (favoriteButton != null) {
            var selected = selectedEntry();
            favoriteButton.active = selected.isPresent();
            favoriteButton.setMessage(Component.translatable(
                    selected.isPresent() && selected.get().favorite()
                            ? "gui.mirage_projector.scan_codex.unfavorite"
                            : "gui.mirage_projector.scan_codex.favorite"
            ));
        }
    }

    private void send(UUID scanId, ScanCodexActionPayload.Action action) {
        PacketDistributor.sendToServer(new ScanCodexActionPayload(snapshot.codexId(), scanId, action));
    }

    private int left() {
        return (width - PANEL_WIDTH) / 2;
    }

    private int top() {
        return (height - PANEL_HEIGHT) / 2;
    }

    private static int maxPage(List<ScanCodexEntrySummary> entries) {
        return Math.max(0, (entries.size() - 1) / PAGE_SIZE);
    }

    private static String trim(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static Component categoryLabel(ScanCodexEntrySummary entry) {
        if (entry.playerSource()) {
            return Component.translatable("gui.mirage_projector.scan_codex.category.player");
        }
        return switch (entry.kind()) {
            case HUMANOID -> Component.translatable("gui.mirage_projector.scan_codex.category.humanoid");
            case HORSE -> Component.translatable("gui.mirage_projector.scan_codex.category.horse");
            case GENERIC -> Component.translatable("gui.mirage_projector.scan_codex.category.other");
        };
    }

    private enum Filter {
        ALL("gui.mirage_projector.scan_codex.filter.all"),
        FAVORITES("gui.mirage_projector.scan_codex.filter.favorites"),
        PLAYERS("gui.mirage_projector.scan_codex.filter.players"),
        HUMANOIDS("gui.mirage_projector.scan_codex.filter.humanoids"),
        HORSES("gui.mirage_projector.scan_codex.filter.horses"),
        OTHER("gui.mirage_projector.scan_codex.filter.other");

        private final String key;

        Filter(String key) {
            this.key = key;
        }

        Component label() {
            return Component.translatable(key);
        }

        Filter next() {
            Filter[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        boolean accepts(ScanCodexEntrySummary entry) {
            return switch (this) {
                case ALL -> true;
                case FAVORITES -> entry.favorite();
                case PLAYERS -> entry.playerSource();
                case HUMANOIDS -> !entry.playerSource() && entry.kind() == EntityScanData.Kind.HUMANOID;
                case HORSES -> entry.kind() == EntityScanData.Kind.HORSE;
                case OTHER -> !entry.playerSource() && entry.kind() == EntityScanData.Kind.GENERIC;
            };
        }
    }
}
