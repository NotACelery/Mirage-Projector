package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.crying.CryingObsidianLightField;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Same-tick geometry invalidation bridge for the authoritative Mature Cluster field.
 *
 * Mutations are queued until LevelTickEvent.Post so the solver sees final block states.
 * Player placement/breaking, fluid block formation, crop/feature growth, piston paths and explosions all feed
 * the same coalesced queue; a source is still rebuilt at most once for the final tick.
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFluidPlace(BlockEvent.FluidPlaceBlockEvent event) {
        if (!event.isCanceled() && event.getLevel() instanceof ServerLevel level) {
            queue(level, event.getPos());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFeatureGrowth(BlockGrowFeatureEvent event) {
        if (!event.isCanceled() && event.getLevel() instanceof ServerLevel level) {
            // Feature placement happens after this event returns. The queued origin is
            // enough to select nearby Mirage sources; the actual solve is deferred to
            // LevelTickEvent.Post and therefore sees the completed tree/fungus geometry.
            queue(level, event.getPos());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCropGrowth(CropGrowEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            queue(level, event.getPos());
        }
    }

    @SubscribeEvent
    public static void onPistonPost(PistonEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos piston = event.getPos();
        Direction direction = event.getDirection();
        queue(level, piston);
        // Vanilla pistons can affect a chain of at most 12 movable blocks. Queue the
        // complete possible path; HashSet coalescing makes overlapping piston events cheap.
        for (int step = 1; step <= 13; step++) {
            queue(level, piston.relative(direction, step));
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel level) {
            event.getAffectedBlocks().forEach(pos -> queue(level, pos));
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
