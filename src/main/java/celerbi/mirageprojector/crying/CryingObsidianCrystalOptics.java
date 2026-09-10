package celerbi.mirageprojector.crying;

import celerbi.mirageprojector.block.CoreBoosterBlock;
import celerbi.mirageprojector.block.CryingObsidianCrystalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CryingObsidianCrystalOptics {
    private CryingObsidianCrystalOptics() {
    }

    public static float incomingBeaconFraction(Level level, BlockPos crystalPos) {
        float transmission = 1.0F;

        for (int y = crystalPos.getY() - 1; y >= level.getMinBuildHeight(); y--) {
            BlockPos scanPos = new BlockPos(crystalPos.getX(), y, crystalPos.getZ());
            BlockState state = level.getBlockState(scanPos);

            if (state.getBlock() instanceof CryingObsidianCrystalBlock crystal) {
                transmission *= crystal.stage().verticalTransmission();
                if (transmission <= 0.0001F) {
                    return 0.0F;
                }
            }

            if (level.getBlockEntity(scanPos) instanceof BeaconBlockEntity beacon) {
                return beacon.getBeamSections().isEmpty() ? 0.0F : transmission;
            }
        }

        return 0.0F;
    }

    public static BeaconRelayState relayStateBelow(Level level, BlockPos crystalPos) {
        int beaconY = Integer.MIN_VALUE;
        for (int y = crystalPos.getY() - 1; y >= level.getMinBuildHeight(); y--) {
            BlockPos scanPos = new BlockPos(crystalPos.getX(), y, crystalPos.getZ());
            if (level.getBlockEntity(scanPos) instanceof BeaconBlockEntity beacon) {
                if (beacon.getBeamSections().isEmpty()) {
                    return BeaconRelayState.BASE;
                }
                beaconY = y;
                break;
            }
        }
        if (beaconY == Integer.MIN_VALUE) {
            return BeaconRelayState.BASE;
        }

        BeaconRelayState relay = BeaconRelayState.BASE;
        for (int y = beaconY + 1; y < crystalPos.getY(); y++) {
            BlockPos scanPos = new BlockPos(crystalPos.getX(), y, crystalPos.getZ());
            BlockState state = level.getBlockState(scanPos);
            if (state.getBlock() instanceof CoreBoosterBlock && state.hasProperty(CoreBoosterBlock.MATERIAL)) {
                relay = relay.apply(state.getValue(CoreBoosterBlock.MATERIAL));
            }
        }
        return relay;
    }

    public static boolean isBeaconEnergized(Level level, BlockPos crystalPos) {
        return incomingBeaconFraction(level, crystalPos) > 0.0001F;
    }
}
