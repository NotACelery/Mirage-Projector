package celerbi.mirageprojector.event;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.light.engine.MirageLightEngine;
import celerbi.mirageprojector.light.engine.MirageLightField;
import celerbi.mirageprojector.light.engine.MirageLightSource;
import celerbi.mirageprojector.light.engine.MirageLightWorld;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LightLayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Diagnostics for the dev.74 shadow light solver. `rebuild` only mutates the shadow cache. */
@EventBusSubscriber(modid = MirageProjector.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class MirageLightDebugCommands {
    private MirageLightDebugCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("miragelight")
                        .then(Commands.literal("stats").executes(context -> stats(context.getSource().getLevel(), context.getSource())))
                        .then(Commands.literal("probe").executes(context -> probe(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("rebuild").executes(context -> rebuild(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("axis")
                                .then(Commands.argument("direction", StringArgumentType.word())
                                        .executes(context -> axis(
                                                context.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(context, "direction")
                                        ))))
        );
    }

    private static int stats(ServerLevel level, net.minecraft.commands.CommandSourceStack source) {
        MirageLightWorld.WorldStats stats = MirageLightEngine.stats(level);
        source.sendSuccess(
                () -> Component.literal(
                        "MirageLight shadow: " + stats.sources() + " source(s), "
                                + stats.sections() + " section(s), " + stats.litCells() + " lit voxel(s)"
                ),
                false
        );
        return stats.sources();
    }

    private static int probe(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        int virtual = MirageLightEngine.virtualBlockLight(level, pos);
        int vanilla = level.getBrightness(LightLayer.BLOCK, pos);
        MirageLightSource source = nearestSource(level, pos);
        MirageLightField field = source == null ? null : MirageLightEngine.field(level, source.id());
        int nearestContribution = field == null ? 0 : field.visibleLevelAt(pos);
        String sourceText = source == null ? "none" : source.origin().toShortString();
        player.sendSystemMessage(Component.literal(
                "MirageLight @ " + pos.toShortString() + ": aggregate=" + virtual
                        + ", nearest=" + nearestContribution + " from " + sourceText
                        + ", vanilla=" + vanilla
        ));
        return virtual;
    }


    private static int rebuild(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        MirageLightSource source = nearestSource(level, player.blockPosition());
        if (source == null) {
            player.sendSystemMessage(Component.literal("No solved MirageLight source is loaded in this dimension."));
            return 0;
        }

        MirageLightWorld.UpdateResult result = MirageLightEngine.updateSource(level, source, true);
        MirageLightField field = MirageLightEngine.field(level, source.id());
        if (!result.rebuilt() || field == null || result.solveStats() == null) {
            player.sendSystemMessage(Component.literal("MirageLight rebuild did not produce a field."));
            return 0;
        }

        player.sendSystemMessage(Component.literal(
                "Rebuilt MirageLight source @ " + source.origin().toShortString()
                        + ": " + field.stats().litVoxels() + " voxels / " + field.sectionCount()
                        + " sections / " + String.format(java.util.Locale.ROOT, "%.3f", field.stats().solveMillis()) + "ms"
        ));
        return field.stats().litVoxels();
    }

    private static int axis(ServerPlayer player, String rawDirection) {
        Direction direction = direction(rawDirection);
        if (direction == null) {
            player.sendSystemMessage(Component.literal("Direction must be north/south/east/west/up/down."));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        MirageLightSource source = nearestSource(level, player.blockPosition());
        if (source == null) {
            player.sendSystemMessage(Component.literal("No solved MirageLight source is loaded in this dimension."));
            return 0;
        }
        MirageLightField field = MirageLightEngine.field(level, source.id());
        if (field == null) {
            player.sendSystemMessage(Component.literal("Nearest MirageLight source has no solved field."));
            return 0;
        }

        int radius = source.profile().maxRadius();
        StringBuilder sequence = new StringBuilder();
        for (int distance = 1; distance <= radius; distance++) {
            if (distance > 1) {
                sequence.append(' ');
            }
            sequence.append(field.visibleLevelAt(source.origin().relative(direction, distance)));
        }
        player.sendSystemMessage(Component.literal(
                "MirageLight " + direction.getSerializedName() + " from " + source.origin().toShortString()
                        + " [1.." + radius + "]: " + sequence
        ));
        MirageLightField.SolveStats solve = field.stats();
        player.sendSystemMessage(Component.literal(
                "solve=" + String.format(java.util.Locale.ROOT, "%.3f", solve.solveMillis()) + "ms, voxels="
                        + solve.litVoxels() + ", sections=" + field.sectionCount() + ", blockedEdges=" + solve.blockedEdges()
        ));
        return radius;
    }

    private static MirageLightSource nearestSource(ServerLevel level, BlockPos pos) {
        List<MirageLightSource> sources = MirageLightEngine.sources(level);
        return sources.stream()
                .min(Comparator.comparingLong(source -> squaredDistance(source.origin(), pos)))
                .orElse(null);
    }

    private static long squaredDistance(BlockPos a, BlockPos b) {
        long dx = (long) a.getX() - b.getX();
        long dy = (long) a.getY() - b.getY();
        long dz = (long) a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static Direction direction(String value) {
        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "north", "n" -> Direction.NORTH;
            case "south", "s" -> Direction.SOUTH;
            case "east", "e" -> Direction.EAST;
            case "west", "w" -> Direction.WEST;
            case "up", "u" -> Direction.UP;
            case "down", "d" -> Direction.DOWN;
            default -> null;
        };
    }
}
