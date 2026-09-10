package celerbi.mirageprojector.crying;

import celerbi.mirageprojector.block.CryingObsidianCrystalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CryingObsidianCrystalOptics {
    private CryingObsidianCrystalOptics() {
    }

    /**
     * Returns the fraction of an active Beacon beam that reaches this crystal from below.
     * The target crystal itself is not included; only lower crystals attenuate the input.
     */
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

    public static boolean isBeaconEnergized(Level level, BlockPos crystalPos) {
        return incomingBeaconFraction(level, crystalPos) > 0.0001F;
    }
}
