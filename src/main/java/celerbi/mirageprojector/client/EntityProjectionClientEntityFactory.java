package celerbi.mirageprojector.client;

import celerbi.mirageprojector.entity.EntityProjectionState;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.entity.EquipmentSnapshotRules;
import celerbi.mirageprojector.entity.HorsePosePreset;
import celerbi.mirageprojector.entity.VirtualEquipmentSnapshots;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class EntityProjectionClientEntityFactory {
    private static final UUID BODYLESS_MANNEQUIN_UUID = UUID.fromString("9b36b3b0-570b-4a09-949e-f3457581698d");

    private EntityProjectionClientEntityFactory() {
    }

    public static LivingEntity create(
            EntityScanData.View scan,
            EntityProjectionState state,
            ClientLevel level
    ) {
        try {
            LivingEntity living;
            if (scan.playerSource()) {
                living = createScannedPlayer(scan, level);
            } else {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(scan.entityType());
                if (type == null) {
                    return null;
                }
                if (scan.kind() == EntityScanData.Kind.HORSE && type == EntityType.HORSE) {
                    living = new MirageProjectionHorse(level, state.horsePose());
                } else {
                    Entity created = type.create(level);
                    if (!(created instanceof LivingEntity createdLiving)) {
                        return null;
                    }
                    living = createdLiving;
                }
            }

            living.load(scan.entityData().copy());
            normalizeForProjection(living);
            if (!scan.playerSource() && scan.hadCustomName()) {
                String frozenName = scan.projectionNameplateText();
                if (!frozenName.isBlank()) {

                    living.setCustomName(Component.literal(frozenName));
                    living.setCustomNameVisible(false);
                }
            }
            if (scan.playerSource() && living instanceof MirageRemotePlayer player) {
                player.applyFrozenVisualState(scan);
            }
            if (scan.kind() == EntityScanData.Kind.GENERIC
                    && EntityScanData.supportsSittingPose(scan.entityType())
                    && living instanceof TamableAnimal tameable) {
                tameable.setInSittingPose(state.genericPose() == celerbi.mirageprojector.entity.GenericPosePreset.SITTING);
            }
            applyProjectedEquipment(living, state, scan.kind());
            return living;
        } catch (Throwable ignored) {

            return null;
        }
    }

    public static LivingEntity createEquippedItem(ItemStack stack, ClientLevel level) {
        EquipmentSlot slot = standaloneEquipmentSlot(stack);
        if (slot == null) {
            return null;
        }

        GameProfile profile = new GameProfile(BODYLESS_MANNEQUIN_UUID, "Mirage");
        MirageRemotePlayer mannequin = new MirageRemotePlayer(level, profile, null);
        normalizeForProjection(mannequin);
        mannequin.setInvisible(true);
        mannequin.setItemSlot(slot, stack.copyWithCount(1));
        return mannequin;
    }

    public static EquipmentSlot standaloneEquipmentSlot(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        EquipmentSlot slot = EquipmentSnapshotRules.equipmentSlot(stack);
        return switch (slot) {
            case HEAD, CHEST, LEGS, FEET -> slot;
            default -> null;
        };
    }

    public static LivingEntity createBodylessHumanoid(EntityProjectionState state, ClientLevel level) {
        if (!state.hasProjectedHumanoidEquipment()) {
            return null;
        }

        GameProfile profile = new GameProfile(BODYLESS_MANNEQUIN_UUID, "Mirage");
        MirageRemotePlayer mannequin = new MirageRemotePlayer(level, profile, null);
        normalizeForProjection(mannequin);
        mannequin.setInvisible(true);
        applyProjectedEquipment(mannequin, state, EntityScanData.Kind.HUMANOID);
        return mannequin;
    }

    public static void prepareVisualFrame(LivingEntity entity, int visualTick) {
        if (entity == null) {
            return;
        }

        int safeTick = Math.max(0, visualTick);
        entity.tickCount = safeTick;
        entity.yBodyRotO = entity.yBodyRot;
        entity.yHeadRotO = entity.yHeadRot;

        if (entity instanceof EnderDragon dragon) {
            prepareDragonVisualFrame(dragon, safeTick);
        }
    }

    private static void prepareDragonVisualFrame(EnderDragon dragon, int visualTick) {
        float flap = visualTick * 0.01F;
        dragon.oFlapTime = flap - 0.01F;
        dragon.flapTime = flap;
        dragon.dragonDeathTime = 0;
        dragon.nearestCrystal = null;

        if (dragon.posPointer < 0) {
            dragon.posPointer = 0;
        }
        for (double[] sample : dragon.positions) {
            sample[0] = 0.0D;
            sample[1] = 0.0D;
            sample[2] = 0.0D;
        }
    }

    public static String equipmentFingerprint(EntityProjectionState state, EntityScanData.Kind kind) {
        VirtualEquipmentSnapshots snapshots = switch (kind) {
            case HUMANOID -> state.humanoidProjected();
            case HORSE -> state.horseProjected();
            case GENERIC -> null;
        };
        if (kind == EntityScanData.Kind.GENERIC) {
            return "generic|pose:" + state.genericPose().serializedName();
        }
        if (snapshots == null) {
            return kind.name();
        }

        StringBuilder fingerprint = new StringBuilder(kind.name());
        if (kind == EntityScanData.Kind.HORSE) {
            fingerprint.append("|pose:").append(state.horsePose().serializedName());
        }
        VirtualEquipmentSnapshots.Channel[] channels = kind == EntityScanData.Kind.HORSE
                ? HORSE_CHANNELS
                : HUMANOID_CHANNELS;
        for (VirtualEquipmentSnapshots.Channel channel : channels) {
            fingerprint.append('|').append(channel.serializedName()).append(':')
                    .append(snapshots.get(channel).snapshotId());
        }
        return fingerprint.toString();
    }

    private static LivingEntity createScannedPlayer(EntityScanData.View scan, ClientLevel level) {
        GameProfile profile = new GameProfile(scan.sourceUuid(), safeProfileName(scan.playerProfileName()));
        if (scan.hasFrozenPlayerTexture()) {
            String signature = scan.playerTextureSignature();
            Property texture = new Property(
                    "textures",
                    scan.playerTextureValue(),
                    signature == null || signature.isBlank() ? null : signature
            );
            profile.getProperties().put("textures", texture);
        }
        PlayerSkin.Model frozenModel = switch (scan.playerSkinModel() == null ? "" : scan.playerSkinModel().toLowerCase(java.util.Locale.ROOT)) {
            case "slim" -> PlayerSkin.Model.SLIM;
            case "wide" -> PlayerSkin.Model.WIDE;
            default -> null;
        };
        return new MirageRemotePlayer(level, profile, frozenModel);
    }

    private static void normalizeForProjection(LivingEntity living) {
        living.setPos(0.0D, 0.0D, 0.0D);
        living.setDeltaMovement(Vec3.ZERO);
        living.setCustomName(null);
        living.setCustomNameVisible(false);
        living.setYRot(0.0F);
        living.setXRot(0.0F);
        living.yBodyRot = 0.0F;
        living.yBodyRotO = 0.0F;
        living.yHeadRot = 0.0F;
        living.yHeadRotO = 0.0F;

        if (living instanceof AbstractPiglin piglin) {
            piglin.setImmuneToZombification(true);
        }
        if (living instanceof Hoglin hoglin) {
            hoglin.setImmuneToZombification(true);
        }
    }

    private static void applyProjectedEquipment(
            LivingEntity entity,
            EntityProjectionState state,
            EntityScanData.Kind kind
    ) {
        VirtualEquipmentSnapshots snapshots = switch (kind) {
            case HUMANOID -> state.humanoidProjected();
            case HORSE -> state.horseProjected();
            case GENERIC -> null;
        };
        if (snapshots == null) {
            return;
        }

        VirtualEquipmentSnapshots.Channel[] channels = kind == EntityScanData.Kind.HORSE
                ? HORSE_CHANNELS
                : HUMANOID_CHANNELS;
        for (VirtualEquipmentSnapshots.Channel channel : channels) {
            ItemStack stack = snapshots.get(channel).stack().copy();
            if (channel == VirtualEquipmentSnapshots.Channel.SADDLE) {
                if (entity instanceof AbstractHorse horse) {
                    horse.getSlot(AbstractHorse.EQUIPMENT_SLOT_OFFSET).set(stack);
                }
                continue;
            }

            EquipmentSlot slot = channel.equipmentSlot();
            if (slot != null) {
                entity.setItemSlot(slot, stack);
            }
        }
    }

    private static String safeProfileName(String name) {
        String value = name == null || name.isBlank() ? "Mirage" : name;
        return value.length() <= 16 ? value : value.substring(0, 16);
    }

    private static final VirtualEquipmentSnapshots.Channel[] HUMANOID_CHANNELS = {
            VirtualEquipmentSnapshots.Channel.HEAD,
            VirtualEquipmentSnapshots.Channel.CHEST,
            VirtualEquipmentSnapshots.Channel.LEGS,
            VirtualEquipmentSnapshots.Channel.FEET,
            VirtualEquipmentSnapshots.Channel.MAIN_HAND,
            VirtualEquipmentSnapshots.Channel.OFF_HAND
    };

    private static final VirtualEquipmentSnapshots.Channel[] HORSE_CHANNELS = {
            VirtualEquipmentSnapshots.Channel.SADDLE,
            VirtualEquipmentSnapshots.Channel.BODY
    };

    private static final class MirageProjectionHorse extends Horse {
        private final HorsePosePreset pose;

        private MirageProjectionHorse(ClientLevel level, HorsePosePreset pose) {
            super(EntityType.HORSE, level);
            this.pose = pose == null ? HorsePosePreset.IDLE : pose;
        }

        @Override
        public boolean isSaddled() {
            return !getSlot(AbstractHorse.EQUIPMENT_SLOT_OFFSET).get().isEmpty();
        }

        @Override
        public float getStandAnim(float partialTick) {

            return pose == HorsePosePreset.REARING ? 1.0F : 0.0F;
        }
    }

    private static final class MirageRemotePlayer extends RemotePlayer {
        private final Supplier<PlayerSkin> frozenSkin;
        private final PlayerSkin.Model frozenModel;

        private MirageRemotePlayer(ClientLevel level, GameProfile profile, PlayerSkin.Model frozenModel) {
            super(level, profile);
            this.frozenModel = frozenModel;
            frozenSkin = Minecraft.getInstance().getSkinManager().lookupInsecure(profile);
        }

        private void applyFrozenVisualState(EntityScanData.View scan) {
            this.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) (scan.playerModelParts() & 0xFF));
            HumanoidArm arm = "left".equalsIgnoreCase(scan.playerMainArm())
                    ? HumanoidArm.LEFT
                    : HumanoidArm.RIGHT;
            setMainArm(arm);
        }

        @Override
        public PlayerSkin getSkin() {
            PlayerSkin resolved = frozenSkin.get();
            if (resolved == null || frozenModel == null || resolved.model() == frozenModel) {
                return resolved;
            }
            return new PlayerSkin(
                    resolved.texture(),
                    resolved.textureUrl(),
                    resolved.capeTexture(),
                    resolved.elytraTexture(),
                    frozenModel,
                    resolved.secure()
            );
        }

        @Override
        public boolean isInvisibleTo(Player player) {

            return true;
        }
    }
}
