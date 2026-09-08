package celerbi.mirageprojector.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Frozen visual scan serialized inside an Empty Scan Template after capture.
 *
 * <p>The source UUID is provenance only. Projection must not require the source
 * entity to remain loaded or alive. Editable equipment is split away from the
 * base entity snapshot so Humanoid/Horse projector workspaces can resolve each
 * equipment channel independently.</p>
 */
public final class EntityScanData {
    public static final String ROOT_KEY = "MirageEntityScan";
    public static final int DATA_VERSION = 3;
    public static final int MAX_ENTITY_NBT_BYTES = 256 * 1024;

    public static final EquipmentSlot[] HUMANOID_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND
    };

    public static final VirtualEquipmentSnapshots.Channel[] HORSE_CHANNELS = {
            VirtualEquipmentSnapshots.Channel.SADDLE,
            VirtualEquipmentSnapshots.Channel.BODY
    };

    private EntityScanData() {
    }

    public static boolean hasScan(ItemStack card) {
        return card != null
                && !card.isEmpty()
                && card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .contains(ROOT_KEY);
    }

    public static Optional<CompoundTag> copyRoot(ItemStack card) {
        if (card == null || card.isEmpty()) {
            return Optional.empty();
        }

        CompoundTag custom = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!custom.contains(ROOT_KEY)) {
            return Optional.empty();
        }
        return Optional.of(custom.getCompound(ROOT_KEY).copy());
    }

    public static Optional<View> read(ItemStack card) {
        return copyRoot(card).flatMap(EntityScanData::readRoot);
    }

    public static Optional<View> readRoot(CompoundTag root) {
        if (root == null || root.isEmpty()) {
            return Optional.empty();
        }

        ResourceLocation type = ResourceLocation.tryParse(root.getString("EntityType"));
        if (type == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(type)) {
            return Optional.empty();
        }

        UUID scanId = root.hasUUID("ScanId") ? root.getUUID("ScanId") : UUID.randomUUID();
        UUID sourceUuid = root.hasUUID("SourceUuid") ? root.getUUID("SourceUuid") : new UUID(0L, 0L);
        Kind kind = Kind.fromSerialized(root.getString("Kind"));
        String displayName = root.getString("DisplayName");
        String nameplateText = root.getString("NameplateText");
        boolean playerSource = root.contains("PlayerSource")
                ? root.getBoolean("PlayerSource")
                : type.equals(ResourceLocation.withDefaultNamespace("player"));
        CompoundTag playerProfile = root.contains("PlayerProfile")
                ? root.getCompound("PlayerProfile").copy()
                : new CompoundTag();
        String playerProfileName = playerProfile.contains("Name")
                ? playerProfile.getString("Name")
                : displayName;
        String playerTextureValue = playerProfile.getString("TextureValue");
        String playerTextureSignature = playerProfile.getString("TextureSignature");
        CompoundTag entityData = root.contains("EntityData") ? root.getCompound("EntityData").copy() : new CompoundTag();
        CompoundTag equipment = root.contains("Equipment") ? root.getCompound("Equipment").copy() : new CompoundTag();

        // dev.13 compatibility: player scans already carried DisplayName but did
        // not yet separate projector nameplate policy from the card label.
        if (nameplateText.isBlank() && playerSource) {
            nameplateText = displayName;
        }

        return Optional.of(new View(
                scanId,
                sourceUuid,
                type,
                kind,
                displayName,
                nameplateText,
                playerSource,
                playerProfileName,
                playerTextureValue,
                playerTextureSignature,
                entityData,
                equipment
        ));
    }

    public static Scan create(LivingEntity target) {
        HolderLookup.Provider registries = target.level().registryAccess();
        Kind kind = classify(target);
        ResourceLocation entityType = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        String displayName = target.getName().getString();
        String nameplateText = projectionNameplate(target);

        CompoundTag entityData = target.saveWithoutId(new CompoundTag());
        sanitizeCommon(entityData);

        CompoundTag equipment = new CompoundTag();
        if (kind == Kind.HUMANOID) {
            for (EquipmentSlot slot : HUMANOID_SLOTS) {
                saveEquipmentSlot(equipment, slot, target.getItemBySlot(slot), registries);
            }
            stripHumanoidEquipment(entityData);
        } else if (kind == Kind.HORSE) {
            saveHorseEquipment(equipment, target, entityData, registries);
            stripHorseEquipment(entityData);
        }

        CompoundTag root = new CompoundTag();
        root.putInt("Version", DATA_VERSION);
        root.putUUID("ScanId", UUID.randomUUID());
        root.putUUID("SourceUuid", target.getUUID());
        root.putString("EntityType", entityType.toString());
        root.putString("Kind", kind.serializedName());
        root.putString("DisplayName", displayName);
        root.putBoolean("PlayerSource", target instanceof Player);
        if (target instanceof Player player) {
            CompoundTag playerProfile = capturePlayerProfile(player);
            if (!playerProfile.isEmpty()) {
                root.put("PlayerProfile", playerProfile);
            }
        }
        if (!nameplateText.isBlank()) {
            root.putString("NameplateText", nameplateText);
        }
        root.put("EntityData", entityData);
        root.put("Equipment", equipment);

        int totalBytes = root.sizeInBytes();
        if (totalBytes > MAX_ENTITY_NBT_BYTES) {
            return Scan.tooLarge(totalBytes);
        }
        return Scan.success(root, kind, entityType, displayName, nameplateText);
    }

    public static void writeToCard(ItemStack card, Scan scan) {
        if (!scan.success() || scan.root() == null) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, card, custom -> custom.put(ROOT_KEY, scan.root().copy()));
    }

    public static Kind classify(LivingEntity entity) {
        if (entity instanceof Player
                || entity instanceof Zombie
                || entity instanceof AbstractSkeleton
                || entity instanceof AbstractPiglin) {
            return Kind.HUMANOID;
        }
        if (entity instanceof Horse) {
            return Kind.HORSE;
        }
        return Kind.GENERIC;
    }

    public static String projectionNameplate(LivingEntity entity) {
        if (entity instanceof Player) {
            return entity.getName().getString();
        }
        return entity.hasCustomName() && entity.getCustomName() != null
                ? entity.getCustomName().getString()
                : "";
    }

    public static ItemStack equipmentStack(
            CompoundTag equipment,
            EquipmentSlot slot,
            HolderLookup.Provider registries
    ) {
        if (equipment == null || !equipment.contains(slot.getSerializedName())) {
            return ItemStack.EMPTY;
        }
        return ItemStack.parseOptional(registries, equipment.getCompound(slot.getSerializedName()));
    }

    public static ItemStack equipmentStack(
            CompoundTag equipment,
            VirtualEquipmentSnapshots.Channel channel,
            HolderLookup.Provider registries
    ) {
        if (equipment == null || channel == null || !equipment.contains(channel.serializedName())) {
            return ItemStack.EMPTY;
        }
        return ItemStack.parseOptional(registries, equipment.getCompound(channel.serializedName()));
    }

    private static void saveHorseEquipment(
            CompoundTag equipment,
            LivingEntity target,
            CompoundTag entityData,
            HolderLookup.Provider registries
    ) {
        if (entityData.contains("SaddleItem")) {
            ItemStack saddle = ItemStack.parseOptional(registries, entityData.getCompound("SaddleItem"));
            if (!saddle.isEmpty()) {
                equipment.put(
                        VirtualEquipmentSnapshots.Channel.SADDLE.serializedName(),
                        saddle.copyWithCount(1).save(registries)
                );
            }
        }

        ItemStack body = target.getItemBySlot(EquipmentSlot.BODY);
        if (!body.isEmpty()) {
            equipment.put(
                    VirtualEquipmentSnapshots.Channel.BODY.serializedName(),
                    body.copyWithCount(1).save(registries)
            );
        }
    }

    private static void saveEquipmentSlot(
            CompoundTag equipment,
            EquipmentSlot slot,
            ItemStack stack,
            HolderLookup.Provider registries
    ) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        equipment.put(slot.getSerializedName(), stack.copyWithCount(1).save(registries));
    }

    private static CompoundTag capturePlayerProfile(Player player) {
        CompoundTag result = new CompoundTag();
        GameProfile profile = player.getGameProfile();
        if (profile == null) {
            return result;
        }

        String profileName = profile.getName();
        if (profileName != null && !profileName.isBlank()) {
            result.putString("Name", profileName);
        }

        Property texture = profile.getProperties().get("textures").stream().findFirst().orElse(null);
        if (texture != null && texture.value() != null && !texture.value().isBlank()) {
            result.putString("TextureValue", texture.value());
            if (texture.signature() != null && !texture.signature().isBlank()) {
                result.putString("TextureSignature", texture.signature());
            }
        }
        return result;
    }

    private static void sanitizeCommon(CompoundTag tag) {
        String[] keys = {
                "Pos", "Motion", "Rotation", "UUID", "UUIDMost", "UUIDLeast",
                "Passengers", "Leash", "PortalCooldown", "FallDistance", "Fire",
                "Air", "OnGround", "Invulnerable", "DeathTime", "HurtTime",
                "HurtByTimestamp", "Health", "AbsorptionAmount", "FallFlying",
                "Brain", "Gossips", "Offers", "Recipes", "Xp", "Inventory",
                "EnderItems", "SelectedItem", "ShoulderEntityLeft", "ShoulderEntityRight",
                "abilities", "recipeBook", "RootVehicle", "CustomName",
                "CustomNameVisible", "Tags", "Team"
        };
        for (String key : keys) {
            tag.remove(key);
        }
    }

    private static void stripHumanoidEquipment(CompoundTag tag) {
        tag.remove("ArmorItems");
        tag.remove("HandItems");
        tag.remove("ArmorDropChances");
        tag.remove("HandDropChances");
    }

    private static void stripHorseEquipment(CompoundTag tag) {
        tag.remove("SaddleItem");
        tag.remove("ArmorItem");
        tag.remove("BodyArmorItem");
    }

    public enum Kind {
        HUMANOID,
        HORSE,
        GENERIC;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Kind fromSerialized(String value) {
            if (value == null) {
                return GENERIC;
            }
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "humanoid" -> HUMANOID;
                case "horse" -> HORSE;
                default -> GENERIC;
            };
        }
    }

    public record View(
            UUID scanId,
            UUID sourceUuid,
            ResourceLocation entityType,
            Kind kind,
            String displayName,
            String nameplateText,
            boolean playerSource,
            String playerProfileName,
            String playerTextureValue,
            String playerTextureSignature,
            CompoundTag entityData,
            CompoundTag equipment
    ) {
        public boolean hasFrozenPlayerTexture() {
            return playerSource && playerTextureValue != null && !playerTextureValue.isBlank();
        }

        public boolean hasEquipment(String slotName) {
            return equipment.contains(slotName);
        }

        public boolean hasProjectionNameplate() {
            return !nameplateText.isBlank();
        }
    }

    public record Scan(
            boolean success,
            String error,
            int sizeBytes,
            CompoundTag root,
            Kind kind,
            ResourceLocation entityType,
            String displayName,
            String nameplateText
    ) {
        static Scan success(
                CompoundTag root,
                Kind kind,
                ResourceLocation entityType,
                String displayName,
                String nameplateText
        ) {
            return new Scan(true, "", 0, root, kind, entityType, displayName, nameplateText);
        }

        static Scan tooLarge(int sizeBytes) {
            return new Scan(false, "snapshot_too_large", sizeBytes, null, Kind.GENERIC, null, "", "");
        }
    }
}
