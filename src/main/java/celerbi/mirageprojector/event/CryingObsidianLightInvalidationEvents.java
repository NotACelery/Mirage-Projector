package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.crying.CryingObsidianLightField;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Same-tick invalidation bridge for the Mature Cluster auxiliary-light field.
 *
 * BreakEvent is fired before the block is actually removed and a placement can still be
 * cancelled, so terrain edits are queued until LevelTickEvent.Post. At that point the
 * final world state is authoritative and every affected Mature source is rebuilt once.
 */
@EventBusSubscriber(modid = MirageProjector.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class CryingObsidianLightInvalidationEvents {
    private static final Map<ServerLevel, Set<BlockPos>> PENDING_CHANGES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private CryingObsidianLightInvalidationEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (event instanceof BlockEvent.EntityMultiPlaceEvent multiPlace) {
            multiPlace.getReplacedBlockSnapshots().forEach(snapshot -> queue(level, snapshot.getPos()));
        } else {
            queue(level, event.getPos());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!event.isCanceled() && event.getLevel() instanceof ServerLevel level) {
            queue(level, event.getPos());
        }
    }

    @SubscribeEvent
    public static void onLevelTickPost(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Set<BlockPos> changes = PENDING_CHANGES.remove(level);
        if (changes == null || changes.isEmpty()) {
            return;
        }
        CryingObsidianLightField.refreshSourcesNearNow(level, changes);
    }

    private static void queue(ServerLevel level, BlockPos pos) {
        PENDING_CHANGES.computeIfAbsent(level, ignored -> new HashSet<>()).add(pos.immutable());
    }
}
