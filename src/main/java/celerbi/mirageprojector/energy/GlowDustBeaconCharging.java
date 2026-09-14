package celerbi.mirageprojector.energy;

import celerbi.mirageprojector.block.CryingObsidianCrystalBlock;
import celerbi.mirageprojector.blockentity.CoreBoosterBlockEntity;
import celerbi.mirageprojector.item.GlowDustItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

/**
 * Shared server/client rules for charging Glow Dust from an active Beacon beam.
 *
 * <p>Every actively charging Dust consumes 20 percentage points of the outgoing
 * beam. A clear beam therefore supports at most five simultaneous charging Dust
 * cells in one vertical path. Crystal transmission composes with that attenuation.</p>
 */
public final class GlowDustBeaconCharging {
    public static final float TRANSMISSION_COST = 0.20F;
    public static final int MAX_CLEAR_PATH_CHARGERS = 5;
    public static final int CHARGE_INTERVAL_TICKS = 10;
    public static final int CHARGE_PER_INTERVAL = 10;

    private GlowDustBeaconCharging() {
    }

    public static float attenuationAfterDust(float incoming) {
        return Math.max(0.0F, incoming - TRANSMISSION_COST);
    }

    public static boolean canChargeAt(Level level, BlockPos boosterPos, CoreBoosterBlockEntity booster) {
        return booster != null
                && booster.hasChargingDust()
                && !GlowDustItem.isFull(booster.chargingDust())
                && incomingTransmission(level, boosterPos) > 0.0001F;
    }

    public static float incomingTransmission(Level level, BlockPos targetPos) {
        if (level == null || targetPos == null) {
            return 0.0F;
        }

        BeaconAnchor anchor = findBeaconBelow(level, targetPos);
        if (anchor == null) {
            return 0.0F;
        }

        float transmission = 1.0F;
        for (int y = anchor.pos().getY() + 1; y < targetPos.getY(); y++) {
            BlockPos scanPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());

            if (level.getBlockEntity(scanPos) instanceof CoreBoosterBlockEntity booster
                    && booster.hasChargingDust()
                    && !GlowDustItem.isFull(booster.chargingDust())
                    && transmission > 0.0001F) {
                transmission = attenuationAfterDust(transmission);
            }

            if (level.getBlockState(scanPos).getBlock() instanceof CryingObsidianCrystalBlock crystal) {
                transmission *= crystal.stage().verticalTransmission();
            }

            if (transmission <= 0.0001F) {
                return 0.0F;
            }
        }
        return transmission;
    }

    public static boolean activeBeaconReaches(Level level, BlockPos targetPos) {
        return findBeaconBelow(level, targetPos) != null;
    }

    public static void scheduleCrystalRecheckAbove(ServerLevel level, BlockPos origin) {
        if (level == null || origin == null) {
            return;
        }
        for (int y = origin.getY() + 1; y < level.getMaxBuildHeight(); y++) {
            BlockPos scanPos = new BlockPos(origin.getX(), y, origin.getZ());
            var state = level.getBlockState(scanPos);
            if (state.getBlock() instanceof CryingObsidianCrystalBlock) {
                level.scheduleTick(scanPos, state.getBlock(), 1);
            }
        }
    }

    private static BeaconAnchor findBeaconBelow(Level level, BlockPos targetPos) {
        for (int y = targetPos.getY() - 1; y >= level.getMinBuildHeight(); y--) {
            BlockPos scanPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
            if (!(level.getBlockEntity(scanPos) instanceof BeaconBlockEntity beacon)) {
                continue;
            }
            if (beacon.getBeamSections().isEmpty()) {
                return null;
            }
            int beamHeight = 0;
            for (BeaconBlockEntity.BeaconBeamSection section : beacon.getBeamSections()) {
                beamHeight += section.getHeight();
            }
            int requiredHeight = targetPos.getY() - y;
            if (beamHeight < requiredHeight) {
                return null;
            }
            return new BeaconAnchor(scanPos);
        }
        return null;
    }

    private record BeaconAnchor(BlockPos pos) {
    }
}
