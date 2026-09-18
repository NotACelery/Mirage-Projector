package celerbi.mirageprojector.scan;

import celerbi.mirageprojector.entity.EntityScanData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Server-authoritative storage for physical Mirage Scan Codices.
 *
 * <p>The Codex ItemStack carries only a stable library UUID and selected scan UUID. Full frozen
 * entity snapshots live here in Overworld SavedData so a large scan library is not replicated as
 * ItemStack custom data every time the player's inventory synchronizes.</p>
 */
public final class ScanCodexSavedData extends SavedData {
    public static final int DATA_VERSION = 1;
    public static final int MAX_SCANS_PER_ENTITY_TYPE = 25;
    private static final String DATA_NAME = "mirage_projector_scan_codices";
    private static final SavedData.Factory<ScanCodexSavedData> FACTORY =
            new SavedData.Factory<>(ScanCodexSavedData::new, ScanCodexSavedData::load);

    private final Map<UUID, CodexLibrary> codices = new LinkedHashMap<>();

    public static ScanCodexSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public UUID addScan(UUID codexId, EntityScanData.Scan scan) {
        if (scan == null || !scan.success() || scan.root() == null) {
            return new UUID(0L, 0L);
        }
        return addScanRoot(codexId, scan.root());
    }

    /**
     * Imports one already-frozen Mirage scan root into a Codex.
     *
     * <p>This is used by physical Entity Scan Cards handed between players. The snapshot is copied;
     * player cards can retain multiple skin snapshots of the same player. Non-player imports are
     * rejected by {@link ScanCodexImportService} when the source entity is already present.</p>
     */
    public UUID addScanRoot(UUID codexId, CompoundTag sourceRoot) {
        if (codexId == null || sourceRoot == null || sourceRoot.isEmpty()
                || sourceRoot.sizeInBytes() > EntityScanData.MAX_ENTITY_NBT_BYTES) {
            return new UUID(0L, 0L);
        }
        CompoundTag root = sourceRoot.copy();
        Optional<EntityScanData.View> parsed = EntityScanData.readRoot(root);
        if (parsed.isEmpty()) {
            return new UUID(0L, 0L);
        }

        CodexLibrary library = codices.computeIfAbsent(codexId, ignored -> new CodexLibrary());
        ResourceLocation entityType = parsed.get().entityType();
        if (countForType(library, entityType) >= MAX_SCANS_PER_ENTITY_TYPE) {
            return new UUID(0L, 0L);
        }
        UUID scanId = parsed.get().scanId();
        root.putUUID("ScanId", scanId);
        while (library.entries.containsKey(scanId)) {
            scanId = UUID.randomUUID();
            root.putUUID("ScanId", scanId);
        }
        library.entries.put(scanId, new StoredScan(root, false));
        setDirty();
        return scanId;
    }

    public boolean canAddType(UUID codexId, ResourceLocation entityType) {
        if (codexId == null || entityType == null) {
            return false;
        }
        CodexLibrary library = codices.get(codexId);
        return library == null || countForType(library, entityType) < MAX_SCANS_PER_ENTITY_TYPE;
    }

    public int countForType(UUID codexId, ResourceLocation entityType) {
        CodexLibrary library = codices.get(codexId);
        return library == null || entityType == null ? 0 : countForType(library, entityType);
    }

    public boolean delete(UUID codexId, UUID scanId) {
        if (codexId == null || scanId == null) {
            return false;
        }
        CodexLibrary library = codices.get(codexId);
        if (library == null || library.entries.remove(scanId) == null) {
            return false;
        }
        if (library.entries.isEmpty()) {
            codices.remove(codexId);
        }
        setDirty();
        return true;
    }

    public boolean toggleFavorite(UUID codexId, UUID scanId) {
        StoredScan stored = stored(codexId, scanId);
        if (stored == null) {
            return false;
        }
        stored.favorite = !stored.favorite;
        setDirty();
        return true;
    }

    public boolean contains(UUID codexId, UUID scanId) {
        return stored(codexId, scanId) != null;
    }

    public boolean containsNonPlayerSource(UUID codexId, UUID sourceUuid) {
        if (codexId == null || sourceUuid == null) {
            return false;
        }
        CodexLibrary library = codices.get(codexId);
        if (library == null) {
            return false;
        }
        for (StoredScan stored : library.entries.values()) {
            Optional<EntityScanData.View> view = EntityScanData.readRoot(stored.root);
            if (view.isPresent() && !view.get().playerSource() && sourceUuid.equals(view.get().sourceUuid())) {
                return true;
            }
        }
        return false;
    }

    public Optional<CompoundTag> copyScanRoot(UUID codexId, UUID scanId) {
        StoredScan stored = stored(codexId, scanId);
        return stored == null ? Optional.empty() : Optional.of(stored.root.copy());
    }

    public List<ScanCodexEntrySummary> summaries(UUID codexId) {
        CodexLibrary library = codices.get(codexId);
        if (library == null || library.entries.isEmpty()) {
            return List.of();
        }
        List<ScanCodexEntrySummary> result = new ArrayList<>(library.entries.size());
        for (StoredScan stored : library.entries.values()) {
            EntityScanData.readRoot(stored.root).ifPresent(view -> result.add(new ScanCodexEntrySummary(
                    view.scanId(),
                    view.entityType(),
                    view.kind(),
                    view.displayName(),
                    view.projectionNameplateText(),
                    view.playerSource(),
                    view.hadCustomName(),
                    stored.favorite,
                    view.playerSource()
                            ? 0
                            : (stored.root.contains("Equipment")
                            ? stored.root.getCompound("Equipment").getAllKeys().size()
                            : 0)
            )));
        }
        return List.copyOf(result);
    }

    private static int countForType(CodexLibrary library, ResourceLocation entityType) {
        int count = 0;
        for (StoredScan stored : library.entries.values()) {
            Optional<EntityScanData.View> view = EntityScanData.readRoot(stored.root);
            if (view.isPresent() && entityType.equals(view.get().entityType())) {
                count++;
            }
        }
        return count;
    }

    private StoredScan stored(UUID codexId, UUID scanId) {
        if (codexId == null || scanId == null) {
            return null;
        }
        CodexLibrary library = codices.get(codexId);
        return library == null ? null : library.entries.get(scanId);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Version", DATA_VERSION);
        ListTag codexList = new ListTag();
        for (Map.Entry<UUID, CodexLibrary> codexEntry : codices.entrySet()) {
            CompoundTag codexTag = new CompoundTag();
            codexTag.putUUID("CodexId", codexEntry.getKey());
            ListTag scans = new ListTag();
            for (StoredScan stored : codexEntry.getValue().entries.values()) {
                CompoundTag scanTag = new CompoundTag();
                scanTag.putBoolean("Favorite", stored.favorite);
                scanTag.put("Scan", stored.root.copy());
                scans.add(scanTag);
            }
            codexTag.put("Entries", scans);
            codexList.add(codexTag);
        }
        tag.put("Codices", codexList);
        return tag;
    }

    private static ScanCodexSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        ScanCodexSavedData data = new ScanCodexSavedData();
        if (tag == null || !tag.contains("Codices", Tag.TAG_LIST)) {
            return data;
        }
        ListTag codexList = tag.getList("Codices", Tag.TAG_COMPOUND);
        for (int i = 0; i < codexList.size(); i++) {
            CompoundTag codexTag = codexList.getCompound(i);
            if (!codexTag.hasUUID("CodexId")) {
                continue;
            }
            UUID codexId = codexTag.getUUID("CodexId");
            CodexLibrary library = new CodexLibrary();
            ListTag scans = codexTag.getList("Entries", Tag.TAG_COMPOUND);
            for (int j = 0; j < scans.size(); j++) {
                CompoundTag scanTag = scans.getCompound(j);
                if (!scanTag.contains("Scan", Tag.TAG_COMPOUND)) {
                    continue;
                }
                CompoundTag root = scanTag.getCompound("Scan").copy();
                Optional<EntityScanData.View> parsed = EntityScanData.readRoot(root);
                if (parsed.isEmpty()) {
                    continue;
                }
                UUID scanId = parsed.get().scanId();
                if (library.entries.containsKey(scanId)) {
                    continue;
                }
                library.entries.put(scanId, new StoredScan(root, scanTag.getBoolean("Favorite")));
            }
            data.codices.put(codexId, library);
        }
        return data;
    }

    private static final class CodexLibrary {
        private final LinkedHashMap<UUID, StoredScan> entries = new LinkedHashMap<>();
    }

    private static final class StoredScan {
        private final CompoundTag root;
        private boolean favorite;

        private StoredScan(CompoundTag root, boolean favorite) {
            this.root = root;
            this.favorite = favorite;
        }
    }
}
