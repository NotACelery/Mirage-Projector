package celerbi.mirageprojector;

/**
 * Device-agnostic energy boundary for projection power evaluation.
 *
 * <p>1.0 fixed projectors adapt their installed Projection Core through this interface.
 * 1.1 portable devices may provide Glow Dust charge or another backend without inventing a
 * fake Core slot or changing ProjectionPower's budget semantics.</p>
 */
public interface ProjectionEnergySource {
    boolean available();

    int basePower();

    float outputMultiplier();

    default int effectiveCapacity(ProjectionChassisProfile chassis) {
        ProjectionChassisProfile safeChassis = chassis == null ? ProjectionChassisProfile.COMPACT : chassis;
        if (!available()) {
            return 0;
        }
        return Math.max(1, (int) Math.floor(
                Math.max(0, basePower())
                        * safeChassis.powerMultiplier()
                        * Math.max(0.0F, outputMultiplier())
        ));
    }

    static ProjectionEnergySource fromCore(ProjectionCoreProfile core) {
        ProjectionCoreProfile safe = core == null ? ProjectionCoreProfile.NONE : core;
        return new ProjectionEnergySource() {
            @Override
            public boolean available() {
                return safe.present();
            }

            @Override
            public int basePower() {
                return safe.basePower();
            }

            @Override
            public float outputMultiplier() {
                return safe.amplificationMultiplier();
            }

            @Override
            public String toString() {
                return "core:" + safe.name().toLowerCase();
            }
        };
    }

    static ProjectionEnergySource fixed(int basePower, float outputMultiplier) {
        int safeBase = Math.max(0, basePower);
        float safeMultiplier = Math.max(0.0F, outputMultiplier);
        return new ProjectionEnergySource() {
            @Override
            public boolean available() {
                return safeBase > 0 && safeMultiplier > 0.0F;
            }

            @Override
            public int basePower() {
                return safeBase;
            }

            @Override
            public float outputMultiplier() {
                return safeMultiplier;
            }
        };
    }
}
