package celerbi.mirageprojector.client;

import celerbi.mirageprojector.light.engine.MirageLightEngine;
import celerbi.mirageprojector.light.engine.MirageLightField;
import celerbi.mirageprojector.light.engine.MirageLightSource;
import celerbi.mirageprojector.network.MirageLightSourceSyncPayload;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

/** Client source application, chunk-arrival rebuild coalescing and section rerendering. */
public final class ClientMirageLightSync {
    private static final Set<Long> PENDING_GEOMETRY_CHUNKS = new HashSet<>();

    private ClientMirageLightSync() {
    }

    public static void apply(MirageLightSourceSyncPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Set<Long> dirtySections = new HashSet<>();
        switch (payload.action()) {
            case CLEAR -> {
                dirtySections.addAll(MirageLightEngine.sectionKeys(level));
                MirageLightEngine.clear(level);
                PENDING_GEOMETRY_CHUNKS.clear();
            }
            case REMOVE -> {
                addFieldSections(dirtySections, MirageLightEngine.field(level, payload.sourceId()));
                MirageLightEngine.removeSource(level, payload.sourceId());
            }
            case UPSERT -> {
                addFieldSections(dirtySections, MirageLightEngine.field(level, payload.sourceId()));
                MirageLightSource source = payload.source();
                MirageLightEngine.updateSource(level, source, true);
                addFieldSections(dirtySections, MirageLightEngine.field(level, source.id()));
            }
        }
        markSectionsDirty(minecraft, dirtySections);
    }

    /** Chunk load/unload events are batched until ClientTickEvent.Post. */
    public static void queueChunkGeometryChanged(ChunkPos chunkPos) {
        if (chunkPos != null) {
            PENDING_GEOMETRY_CHUNKS.add(chunkPos.toLong());
        }
    }

    public static void flushChunkGeometryChanges() {
        if (PENDING_GEOMETRY_CHUNKS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            PENDING_GEOMETRY_CHUNKS.clear();
            return;
        }

        Set<Long> changed = new HashSet<>(PENDING_GEOMETRY_CHUNKS);
        PENDING_GEOMETRY_CHUNKS.clear();
        List<MirageLightSource> sources = MirageLightEngine.sources(level);
        if (sources.isEmpty()) {
            return;
        }

        Set<Long> dirtySections = new HashSet<>();
        for (MirageLightSource source : sources) {
            boolean impacted = false;
            for (long packedChunk : changed) {
                if (MirageLightEngine.sourceTouchesChunk(source, new ChunkPos(packedChunk))) {
                    impacted = true;
                    break;
                }
            }
            if (!impacted) {
                continue;
            }
            addFieldSections(dirtySections, MirageLightEngine.field(level, source.id()));
            MirageLightEngine.updateSource(level, source, true);
            addFieldSections(dirtySections, MirageLightEngine.field(level, source.id()));
        }
        markSectionsDirty(minecraft, dirtySections);
    }

    public static void resetSession() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            Set<Long> dirty = new HashSet<>(MirageLightEngine.sectionKeys(minecraft.level));
            MirageLightEngine.clear(minecraft.level);
            markSectionsDirty(minecraft, dirty);
        }
        PENDING_GEOMETRY_CHUNKS.clear();
    }

    private static void addFieldSections(Set<Long> destination, MirageLightField field) {
        if (field != null) {
            destination.addAll(field.sections().keySet());
        }
    }

    private static void markSectionsDirty(Minecraft minecraft, Set<Long> dirtySections) {
        if (minecraft.levelRenderer == null || dirtySections.isEmpty()) {
            return;
        }
        for (long packedSection : dirtySections) {
            minecraft.levelRenderer.setSectionDirty(
                    SectionPos.x(packedSection),
                    SectionPos.y(packedSection),
                    SectionPos.z(packedSection)
            );
        }
    }
}
