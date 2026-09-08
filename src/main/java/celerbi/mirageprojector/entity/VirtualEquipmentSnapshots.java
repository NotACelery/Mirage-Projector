package celerbi.mirageprojector.entity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Render-only equipment snapshots used by Entity/Humanoid workspaces.
 *
 * <p>Every stored stack is one-count and owns a Mirage snapshot UUID. These
 * entries are not inventories and must never be exposed as obtainable items.</p>
 */
public final class VirtualEquipmentSnapshots {
    private final EnumMap<Channel, Snapshot> snapshots = new EnumMap<>(Channel.class);

    public Snapshot get(Channel channel) {
        return snapshots.getOrDefault(channel, Snapshot.EMPTY);
    }

    public boolean has(Channel channel) {
        return !get(channel).stack().isEmpty();
    }

    public boolean hasAny(Channel... channels) {
        if (channels == null) {
            return false;
        }
        for (Channel channel : channels) {
            if (channel != null && has(channel)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return snapshots.isEmpty();
    }

    public void put(Channel channel, ItemStack source) {
        if (source == null || source.isEmpty()) {
            clear(channel);
            return;
        }
        snapshots.put(channel, new Snapshot(UUID.randomUUID(), source.copyWithCount(1)));
    }

    public void put(Channel channel, UUID snapshotId, ItemStack source) {
        if (source == null || source.isEmpty()) {
            clear(channel);
            return;
        }
        snapshots.put(channel, new Snapshot(snapshotId == null ? UUID.randomUUID() : snapshotId, source.copyWithCount(1)));
    }

    public void clear(Channel channel) {
        snapshots.remove(channel);
    }

    public void clearAll() {
        snapshots.clear();
    }

    public boolean visuallyEquals(Channel channel, Snapshot other) {
        Snapshot current = get(channel);
        if (current.stack().isEmpty() || other == null || other.stack().isEmpty()) {
            return current.stack().isEmpty() && (other == null || other.stack().isEmpty());
        }
        return ItemStack.matches(current.stack(), other.stack());
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag root = new CompoundTag();
        for (Map.Entry<Channel, Snapshot> entry : snapshots.entrySet()) {
            Snapshot snapshot = entry.getValue();
            if (snapshot.stack().isEmpty()) {
                continue;
            }

            CompoundTag saved = new CompoundTag();
            saved.putUUID("SnapshotId", snapshot.snapshotId());
            saved.put("Stack", snapshot.stack().copyWithCount(1).save(registries));
            root.put(entry.getKey().serializedName(), saved);
        }
        return root;
    }

    public void load(CompoundTag root, HolderLookup.Provider registries) {
        clearAll();
        if (root == null || root.isEmpty()) {
            return;
        }

        for (Channel channel : Channel.values()) {
            if (!root.contains(channel.serializedName())) {
                continue;
            }
            CompoundTag saved = root.getCompound(channel.serializedName());
            if (!saved.contains("Stack")) {
                continue;
            }

            ItemStack stack = ItemStack.parseOptional(registries, saved.getCompound("Stack"));
            if (stack.isEmpty()) {
                continue;
            }
            UUID id = saved.hasUUID("SnapshotId") ? saved.getUUID("SnapshotId") : UUID.randomUUID();
            put(channel, id, stack);
        }
    }

    public enum Channel {
        HEAD("head", EquipmentSlot.HEAD),
        CHEST("chest", EquipmentSlot.CHEST),
        LEGS("legs", EquipmentSlot.LEGS),
        FEET("feet", EquipmentSlot.FEET),
        MAIN_HAND("main_hand", EquipmentSlot.MAINHAND),
        OFF_HAND("off_hand", EquipmentSlot.OFFHAND),
        SADDLE("saddle", null),
        BODY("body", EquipmentSlot.BODY);

        private final String serializedName;
        @Nullable
        private final EquipmentSlot equipmentSlot;

        Channel(String serializedName, @Nullable EquipmentSlot equipmentSlot) {
            this.serializedName = serializedName;
            this.equipmentSlot = equipmentSlot;
        }

        public String serializedName() {
            return serializedName;
        }

        @Nullable
        public EquipmentSlot equipmentSlot() {
            return equipmentSlot;
        }

        public boolean humanoid() {
            return switch (this) {
                case HEAD, CHEST, LEGS, FEET, MAIN_HAND, OFF_HAND -> true;
                case SADDLE, BODY -> false;
            };
        }

        public boolean horse() {
            return this == SADDLE || this == BODY;
        }
    }

    public record Snapshot(UUID snapshotId, ItemStack stack) {
        public static final Snapshot EMPTY = new Snapshot(new UUID(0L, 0L), ItemStack.EMPTY);

        public Snapshot {
            stack = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        }
    }
}
