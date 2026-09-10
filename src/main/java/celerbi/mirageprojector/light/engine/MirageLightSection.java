package celerbi.mirageprojector.light.engine;

import java.util.Arrays;
import net.minecraft.core.BlockPos;

/**
 * Compact per-source field storage for one 16x16x16 chunk section.
 * Fixed-point energy is stored as an unsigned byte (0..255); the current Cluster tops out at 39.
 */
public final class MirageLightSection {
    public static final int SIZE = 16 * 16 * 16;

    private final byte[] energy = new byte[SIZE];
    private int nonZeroCount;

    void setMaxEnergy(BlockPos pos, int energyUnits) {
        setMaxEnergy(index(pos), energyUnits);
    }

    void setMaxEnergy(int index, int energyUnits) {
        int safe = Math.max(0, Math.min(255, energyUnits));
        int current = Byte.toUnsignedInt(energy[index]);
        if (safe <= current) {
            return;
        }
        if (current == 0 && safe > 0) {
            nonZeroCount++;
        }
        energy[index] = (byte) safe;
    }

    public int energyAt(BlockPos pos) {
        return energyAtIndex(index(pos));
    }

    public int energyAtIndex(int index) {
        return Byte.toUnsignedInt(energy[index]);
    }

    public int nonZeroCount() {
        return nonZeroCount;
    }

    public byte[] copyEnergy() {
        return Arrays.copyOf(energy, energy.length);
    }

    static int index(BlockPos pos) {
        return index(pos.getX(), pos.getY(), pos.getZ());
    }

    static int index(int x, int y, int z) {
        return ((y & 15) << 8) | ((z & 15) << 4) | (x & 15);
    }
}
