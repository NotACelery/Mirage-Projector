package celerbi.mirageprojector.client;

import celerbi.mirageprojector.light.engine.MirageDynamicLightSnapshot;
import celerbi.mirageprojector.light.engine.MirageLightEngine;
import celerbi.mirageprojector.light.engine.MirageLightField;
import celerbi.mirageprojector.light.engine.MirageLightSource;
import celerbi.mirageprojector.light.engine.MirageLightSourceId;
import celerbi.mirageprojector.light.engine.MirageLightWorld;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

/**
 * Client runtime for rapidly moving Mirage light sources.
 *
 * <p>This manager deliberately never publishes DYNAMIC_VISUAL fields to the server/static
 * section channel. Consumers repeatedly submit their latest transform; the manager applies
 * update cadence, camera culling, stale-source cleanup and render-section invalidation.</p>
 */
public final class ClientDynamicMirageLightManager {
    private static final Map<MirageLightSourceId, Entry> ENTRIES = new HashMap<>();
    private static ClientLevel activeLevel;
    private static long lastProcessedTick = Long.MIN_VALUE;

    private ClientDynamicMirageLightManager() {
    }

    /**
     * Submit the latest transform for one moving emitter. Callers should submit at least once
     * per tick while the device/source is active; stale entries remove themselves safely.
     */
    public static void submit(MirageDynamicLightSnapshot snapshot) {
        if (snapshot == null || snapshot.id() == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        ensureLevel(level);
        long now = level.getGameTime();
        if (!snapshot.active()) {
            remove(snapshot.id());
            return;
        }
        Entry entry = ENTRIES.computeIfAbsent(snapshot.id(), ignored -> new Entry());
        entry.snapshot = snapshot;
        entry.lastSubmittedTick = now;
    }

    /** Explicit removal for a device that turns off before its stale timeout expires. */
    public static void remove(MirageLightSourceId sourceId) {
        if (sourceId == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Entry entry = ENTRIES.remove(sourceId);
        if (entry == null || level == null) {
            return;
        }
        removeInstalled(minecraft, level, sourceId, entry);
    }

    /**
     * Advance all submitted dynamic sources once per client game tick. Calling this from a
     * render event is safe; duplicate render stages in the same tick are ignored.
     */
    public static void tick(Minecraft minecraft, Vec3 cameraPosition) {
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        ClientLevel level = minecraft.level;
        ensureLevel(level);
        long now = level.getGameTime();
        if (lastProcessedTick == now) {
            return;
        }
        lastProcessedTick = now;
        Vec3 camera = cameraPosition == null ? Vec3.ZERO : cameraPosition;

        Iterator<Map.Entry<MirageLightSourceId, Entry>> iterator = ENTRIES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<MirageLightSourceId, Entry> mapEntry = iterator.next();
            MirageLightSourceId sourceId = mapEntry.getKey();
            Entry entry = mapEntry.getValue();
            MirageDynamicLightSnapshot snapshot = entry.snapshot;
            if (snapshot == null || now - entry.lastSubmittedTick > snapshot.staleAfterTicks()) {
                removeInstalled(minecraft, level, sourceId, entry);
                iterator.remove();
                continue;
            }

            double cull = snapshot.cullDistanceBlocks();
            if (camera.distanceToSqr(snapshot.position()) > cull * cull) {
                removeInstalled(minecraft, level, sourceId, entry);
                continue;
            }

            MirageLightSource candidate = snapshot.asVoxelSource();
            boolean firstInstall = entry.installedSource == null;
            boolean changed = !candidate.equals(entry.installedSource);
            MirageLightField installedField = MirageLightEngine.field(level, sourceId);
            boolean incomplete = installedField != null && installedField.stats().unloadedEdges() > 0;
            boolean cadenceReady = firstInstall || now - entry.lastSolveTick >= snapshot.refreshIntervalTicks();
            if (!firstInstall && (!cadenceReady || (!changed && !incomplete))) {
                continue;
            }

            MirageLightWorld.UpdateResult result = MirageLightEngine.updateSource(
                    level,
                    candidate,
                    incomplete && !changed
            );
            if (result.rebuilt()) {
                ClientMirageLightSync.invalidateSections(minecraft, result.changedSections());
                entry.installedSource = candidate;
                entry.lastSolveTick = now;
            } else if (firstInstall) {
                // A clipped/unavailable dynamic solve may legitimately produce an empty field.
                // Remember the candidate only if the engine actually holds it; otherwise retry.
                MirageLightField field = MirageLightEngine.field(level, sourceId);
                if (field != null) {
                    entry.installedSource = field.source();
                    entry.lastSolveTick = now;
                }
            }
        }
    }

    /** Remove only client-owned dynamic fields; the authoritative STATIC_WORLD mirror is untouched. */
    public static void resetSession() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = activeLevel != null ? activeLevel : minecraft.level;
        if (level != null) {
            for (Map.Entry<MirageLightSourceId, Entry> entry : ENTRIES.entrySet()) {
                removeInstalled(minecraft, level, entry.getKey(), entry.getValue());
            }
        }
        ENTRIES.clear();
        activeLevel = null;
        lastProcessedTick = Long.MIN_VALUE;
    }

    public static int submittedSourceCount() {
        return ENTRIES.size();
    }

    private static void ensureLevel(ClientLevel level) {
        if (activeLevel == level) {
            return;
        }
        if (activeLevel != null) {
            for (Map.Entry<MirageLightSourceId, Entry> entry : ENTRIES.entrySet()) {
                removeInstalled(Minecraft.getInstance(), activeLevel, entry.getKey(), entry.getValue());
            }
        }
        ENTRIES.clear();
        activeLevel = level;
        lastProcessedTick = Long.MIN_VALUE;
    }

    private static void removeInstalled(
            Minecraft minecraft,
            ClientLevel level,
            MirageLightSourceId sourceId,
            Entry entry
    ) {
        if (entry == null || entry.installedSource == null) {
            return;
        }
        Set<Long> dirty = new HashSet<>();
        MirageLightField previous = MirageLightEngine.field(level, sourceId);
        if (previous != null) {
            dirty.addAll(previous.sections().keySet());
        }
        MirageLightEngine.removeSource(level, sourceId);
        ClientMirageLightSync.invalidateSections(minecraft, dirty);
        entry.installedSource = null;
        entry.lastSolveTick = Long.MIN_VALUE;
    }

    private static final class Entry {
        private MirageDynamicLightSnapshot snapshot;
        private MirageLightSource installedSource;
        private long lastSubmittedTick = Long.MIN_VALUE;
        private long lastSolveTick = Long.MIN_VALUE;
    }
}
