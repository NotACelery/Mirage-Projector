package celerbi.mirageprojector.client;

import celerbi.mirageprojector.entity.HumanoidPosePreset;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public final class HumanoidPoseController {
    private static final Map<LivingEntity, HumanoidPosePreset> ACTIVE =
            Collections.synchronizedMap(new WeakHashMap<>());

    private HumanoidPoseController() {
    }

    public static void bind(LivingEntity entity, HumanoidPosePreset pose) {
        if (entity == null) {
            return;
        }
        if (pose == null) {
            ACTIVE.remove(entity);
        } else {
            ACTIVE.put(entity, pose);
        }
    }

    public static void applyToModel(LivingEntity entity, HumanoidModel<?> model) {
        HumanoidPosePreset pose = ACTIVE.get(entity);
        if (pose == null || model == null) {
            return;
        }

        boolean mainRight = entity.getMainArm() == HumanoidArm.RIGHT;
        Definition definition = definition(pose, mainRight);

        model.head.xRot += radians(definition.head.x);
        model.head.yRot += radians(definition.head.y);
        model.head.zRot += radians(definition.head.z);

        set(model.body, definition.body);
        set(model.rightArm, definition.rightArm);
        set(model.leftArm, definition.leftArm);
        set(model.rightLeg, definition.rightLeg);
        set(model.leftLeg, definition.leftLeg);
        model.hat.copyFrom(model.head);
    }

    public static float horizontalExtentMultiplier(HumanoidPosePreset pose) {
        return switch (pose == null ? HumanoidPosePreset.STANDING : pose) {
            case STANDING -> 0.58F;
            case GUARD -> 0.82F;
            case HERO -> 0.92F;
            case COMBAT -> 0.90F;
            case RAISED_MAIN_HAND, RAISED_OFF_HAND -> 0.78F;
            case DUAL_WIELD -> 1.0F;
            case DISPLAY -> 1.12F;
        };
    }

    public static float verticalExtentMultiplier(HumanoidPosePreset pose) {
        return switch (pose == null ? HumanoidPosePreset.STANDING : pose) {
            case RAISED_MAIN_HAND, RAISED_OFF_HAND, DUAL_WIELD -> 1.16F;
            default -> 1.0F;
        };
    }

    private static void set(ModelPart part, Rotation rotation) {
        part.xRot = radians(rotation.x);
        part.yRot = radians(rotation.y);
        part.zRot = radians(rotation.z);
    }

    private static float radians(float degrees) {
        return degrees * Mth.DEG_TO_RAD;
    }

    private static Definition definition(HumanoidPosePreset pose, boolean mainRight) {
        Rotation neutralHead = r(0, 0, 0);
        Rotation neutralBody = r(0, 0, 0);
        Rotation neutralRightLeg = r(0, 0, 0);
        Rotation neutralLeftLeg = r(0, 0, 0);

        return switch (pose) {
            case STANDING -> new Definition(
                    neutralHead, neutralBody,
                    r(0, 0, 2), r(0, 0, -2),
                    neutralRightLeg, neutralLeftLeg
            );
            case GUARD -> new Definition(
                    r(-4, 0, 0), r(0, 0, 0),
                    r(-58, -18, 12), r(-58, 18, -12),
                    r(8, 0, 2), r(-8, 0, -2)
            );
            case HERO -> new Definition(
                    r(-3, 0, 0), r(-3, 0, 0),
                    r(18, -12, 24), r(18, 12, -24),
                    r(-5, 0, 4), r(5, 0, -4)
            );
            case COMBAT -> new Definition(
                    r(-8, 0, 0), r(5, 0, 0),
                    r(-72, -24, 10), r(-42, 28, -16),
                    r(-20, 0, 5), r(24, 0, -5)
            );
            case RAISED_MAIN_HAND -> raised(mainRight);
            case RAISED_OFF_HAND -> raised(!mainRight);
            case DUAL_WIELD -> new Definition(
                    r(-8, 0, 0), r(0, 0, 0),
                    r(-118, -12, 8), r(-118, 12, -8),
                    r(-10, 0, 4), r(10, 0, -4)
            );
            case DISPLAY -> new Definition(
                    r(0, 0, 0), r(0, 0, 0),
                    r(-8, 0, 88), r(-8, 0, -88),
                    r(0, 0, 6), r(0, 0, -6)
            );
        };
    }

    private static Definition raised(boolean right) {
        Rotation raised = r(-150, 0, right ? 8 : -8);
        Rotation support = r(-28, right ? 22 : -22, right ? -8 : 8);
        return new Definition(
                r(-10, 0, 0), r(0, 0, 0),
                right ? raised : support,
                right ? support : raised,
                r(-8, 0, 3), r(8, 0, -3)
        );
    }

    private static Rotation r(float x, float y, float z) {
        return new Rotation(x, y, z);
    }

    private record Rotation(float x, float y, float z) {
    }

    private record Definition(
            Rotation head,
            Rotation body,
            Rotation rightArm,
            Rotation leftArm,
            Rotation rightLeg,
            Rotation leftLeg
    ) {
    }
}
