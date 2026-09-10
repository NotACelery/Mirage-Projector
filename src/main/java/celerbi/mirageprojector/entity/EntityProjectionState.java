package celerbi.mirageprojector.entity;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class EntityProjectionState {
    private CompoundTag activeEntityScan = new CompoundTag();
    private HumanoidPosePreset humanoidPose = HumanoidPosePreset.STANDING;
    private HorsePosePreset horsePose = HorsePosePreset.IDLE;
    private GenericPosePreset genericPose = GenericPosePreset.IDLE;
    private final VirtualEquipmentSnapshots humanoidIncoming = new VirtualEquipmentSnapshots();
    private final VirtualEquipmentSnapshots humanoidProjected = new VirtualEquipmentSnapshots();
    private final VirtualEquipmentSnapshots horseIncoming = new VirtualEquipmentSnapshots();
    private final VirtualEquipmentSnapshots horseProjected = new VirtualEquipmentSnapshots();
    private final EnumSet<VirtualEquipmentSnapshots.Channel> hiddenEquipmentChannels =
            EnumSet.noneOf(VirtualEquipmentSnapshots.Channel.class);

    public boolean hasActiveEntity() {
        return EntityScanData.readRoot(activeEntityScan).isPresent();
    }

    public Optional<EntityScanData.View> activeEntity() {
        return EntityScanData.readRoot(activeEntityScan);
    }

    public EntityScanData.Kind activeKind() {
        return activeEntity().map(EntityScanData.View::kind).orElse(EntityScanData.Kind.GENERIC);
    }

    public String projectionNameplate() {
        return activeEntity().map(EntityScanData.View::projectionNameplateText).orElse("");
    }

    public HumanoidPosePreset humanoidPose() {
        return humanoidPose;
    }

    public HumanoidPosePreset cycleHumanoidPose() {
        humanoidPose = humanoidPose.next();
        return humanoidPose;
    }

    public void setHumanoidPose(HumanoidPosePreset pose) {
        humanoidPose = pose == null ? HumanoidPosePreset.STANDING : pose;
    }

    public HorsePosePreset horsePose() {
        return horsePose;
    }

    public HorsePosePreset cycleHorsePose() {
        horsePose = horsePose.next();
        return horsePose;
    }

    public void setHorsePose(HorsePosePreset pose) {
        horsePose = pose == null ? HorsePosePreset.IDLE : pose;
    }

    public GenericPosePreset genericPose() {
        return genericPose;
    }

    public GenericPosePreset cycleGenericPose() {
        genericPose = genericPose.next();
        return genericPose;
    }

    public void setGenericPose(GenericPosePreset pose) {
        genericPose = pose == null ? GenericPosePreset.IDLE : pose;
    }

    public boolean hasProjectedHumanoidEquipment() {
        return humanoidProjected.hasAny(
                VirtualEquipmentSnapshots.Channel.HEAD,
                VirtualEquipmentSnapshots.Channel.CHEST,
                VirtualEquipmentSnapshots.Channel.LEGS,
                VirtualEquipmentSnapshots.Channel.FEET,
                VirtualEquipmentSnapshots.Channel.MAIN_HAND,
                VirtualEquipmentSnapshots.Channel.OFF_HAND
        );
    }

    public boolean hasProjectedHorseEquipment() {
        return horseProjected.hasAny(
                VirtualEquipmentSnapshots.Channel.SADDLE,
                VirtualEquipmentSnapshots.Channel.BODY
        );
    }

    public boolean isEquipmentVisible(VirtualEquipmentSnapshots.Channel channel) {
        return channel != null && !hiddenEquipmentChannels.contains(channel);
    }

    public boolean setEquipmentVisible(VirtualEquipmentSnapshots.Channel channel, boolean visible) {
        if (channel == null) {
            return false;
        }
        boolean changed = visible
                ? hiddenEquipmentChannels.remove(channel)
                : hiddenEquipmentChannels.add(channel);
        return changed;
    }

    public boolean hasProjectedEquipment(VirtualEquipmentSnapshots.Channel channel) {
        if (channel == null) {
            return false;
        }
        return channel.humanoid()
                ? humanoidProjected.has(channel)
                : channel.horse() && horseProjected.has(channel);
    }

    public boolean toggleEquipmentVisible(VirtualEquipmentSnapshots.Channel channel) {
        if (!hasProjectedEquipment(channel)) {
            return isEquipmentVisible(channel);
        }
        boolean next = !isEquipmentVisible(channel);
        setEquipmentVisible(channel, next);
        return next;
    }

    public boolean hasVisibleProjectedHumanoidEquipment() {
        for (VirtualEquipmentSnapshots.Channel channel : HUMANOID_CHANNELS) {
            if (isEquipmentVisible(channel) && humanoidProjected.has(channel)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasVisibleProjectedHorseEquipment() {
        for (VirtualEquipmentSnapshots.Channel channel : EntityScanData.HORSE_CHANNELS) {
            if (isEquipmentVisible(channel) && horseProjected.has(channel)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasProjectedEntityContent() {
        return hasActiveEntity() || hasProjectedHumanoidEquipment() || hasProjectedHorseEquipment();
    }

    public CompoundTag activeEntityRootCopy() {
        return activeEntityScan.copy();
    }

    public VirtualEquipmentSnapshots humanoidIncoming() {
        return humanoidIncoming;
    }

    public VirtualEquipmentSnapshots humanoidProjected() {
        return humanoidProjected;
    }

    public VirtualEquipmentSnapshots horseIncoming() {
        return horseIncoming;
    }

    public VirtualEquipmentSnapshots horseProjected() {
        return horseProjected;
    }

    public int captureEquippedHumanoidLoadout(Player player) {
        if (player == null) {
            return 0;
        }

        int captured = 0;
        for (VirtualEquipmentSnapshots.Channel channel : HUMANOID_CHANNELS) {
            EquipmentSlot slot = channel.equipmentSlot();
            ItemStack stack = slot == null ? ItemStack.EMPTY : player.getItemBySlot(slot);
            VirtualEquipmentSnapshots.Snapshot projected = humanoidProjected.get(channel);
            if (!stack.isEmpty()
                    && (projected.stack().isEmpty() || !ItemStack.matches(projected.stack(), stack))) {
                humanoidIncoming.put(channel, stack);
                captured++;
            } else {

                humanoidIncoming.clear(channel);
            }
        }
        return captured;
    }

    public boolean importFromCard(ItemStack card, HolderLookup.Provider registries) {
        Optional<CompoundTag> root = EntityScanData.copyRoot(card);
        Optional<EntityScanData.View> view = EntityScanData.read(card);
        if (root.isEmpty() || view.isEmpty()) {
            return false;
        }

        EntityScanData.View scan = view.get();

        switch (scan.kind()) {
            case HUMANOID -> clearHorseWorkspace();
            case HORSE -> clearHumanoidWorkspace();
            case GENERIC -> {
                clearHumanoidWorkspace();
                clearHorseWorkspace();
            }
        }

        activeEntityScan = root.get();

        switch (scan.kind()) {
            case HUMANOID -> loadIncoming(
                    humanoidIncoming,
                    humanoidProjected,
                    scan.equipment(),
                    registries,
                    EntityScanData.HUMANOID_SLOTS
            );
            case HORSE -> loadIncoming(
                    horseIncoming,
                    horseProjected,
                    scan.equipment(),
                    registries,
                    EntityScanData.HORSE_CHANNELS
            );
            case GENERIC -> {

            }
        }
        return true;
    }

    public void onStagedCardRemoved(EntityScanData.Kind removedKind) {
        clearActiveEntityBody();
        if (removedKind == EntityScanData.Kind.HORSE) {
            clearHorseWorkspace();
        }
    }

    public ApplyResult applyIncoming(VirtualEquipmentSnapshots.Channel channel, boolean replaceExisting) {
        VirtualEquipmentSnapshots sourceSet = channel.humanoid() ? humanoidIncoming : horseIncoming;
        VirtualEquipmentSnapshots destinationSet = channel.humanoid() ? humanoidProjected : horseProjected;
        VirtualEquipmentSnapshots.Snapshot incoming = sourceSet.get(channel);

        if (incoming.stack().isEmpty()) {
            return ApplyResult.EMPTY_INCOMING;
        }
        if (destinationSet.visuallyEquals(channel, incoming)) {

            sourceSet.clear(channel);
            return ApplyResult.ALREADY_APPLIED;
        }
        if (destinationSet.has(channel) && !replaceExisting) {
            return ApplyResult.CONFLICT;
        }

        destinationSet.put(channel, incoming.snapshotId(), incoming.stack());
        sourceSet.clear(channel);
        return ApplyResult.APPLIED;
    }

    public void clearProjected(VirtualEquipmentSnapshots.Channel channel) {
        if (channel.humanoid()) {
            humanoidProjected.clear(channel);
        } else if (channel.horse()) {
            horseProjected.clear(channel);
        }
        hiddenEquipmentChannels.remove(channel);
    }

    public void clearActiveEntityBody() {
        activeEntityScan = new CompoundTag();
    }

    public void clearHumanoidWorkspace() {
        humanoidIncoming.clearAll();
        humanoidProjected.clearAll();
        for (VirtualEquipmentSnapshots.Channel channel : HUMANOID_CHANNELS) {
            hiddenEquipmentChannels.remove(channel);
        }
        humanoidPose = HumanoidPosePreset.STANDING;
    }

    public void clearHorseWorkspace() {
        horseIncoming.clearAll();
        horseProjected.clearAll();
        for (VirtualEquipmentSnapshots.Channel channel : EntityScanData.HORSE_CHANNELS) {
            hiddenEquipmentChannels.remove(channel);
        }
        horsePose = HorsePosePreset.IDLE;
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag root = new CompoundTag();
        if (!activeEntityScan.isEmpty()) {
            root.put("ActiveEntityScan", activeEntityScan.copy());
        }
        root.putString("HumanoidPose", humanoidPose.serializedName());
        root.putString("HorsePose", horsePose.serializedName());
        root.putString("GenericPose", genericPose.serializedName());
        root.put("HumanoidIncoming", humanoidIncoming.save(registries));
        root.put("HumanoidProjected", humanoidProjected.save(registries));
        root.put("HorseIncoming", horseIncoming.save(registries));
        root.put("HorseProjected", horseProjected.save(registries));
        CompoundTag visibility = new CompoundTag();
        for (VirtualEquipmentSnapshots.Channel channel : VirtualEquipmentSnapshots.Channel.values()) {
            visibility.putBoolean(channel.serializedName(), isEquipmentVisible(channel));
        }
        root.put("EquipmentVisibility", visibility);
        return root;
    }

    public void load(CompoundTag root, HolderLookup.Provider registries) {
        activeEntityScan = root != null && root.contains("ActiveEntityScan")
                ? root.getCompound("ActiveEntityScan").copy()
                : new CompoundTag();
        humanoidPose = root != null && root.contains("HumanoidPose")
                ? HumanoidPosePreset.fromSerializedName(root.getString("HumanoidPose"))
                : HumanoidPosePreset.STANDING;
        horsePose = root != null && root.contains("HorsePose")
                ? HorsePosePreset.fromSerializedName(root.getString("HorsePose"))
                : HorsePosePreset.IDLE;
        genericPose = root != null && root.contains("GenericPose")
                ? GenericPosePreset.fromSerializedName(root.getString("GenericPose"))
                : GenericPosePreset.IDLE;
        humanoidIncoming.load(root == null ? new CompoundTag() : root.getCompound("HumanoidIncoming"), registries);
        humanoidProjected.load(root == null ? new CompoundTag() : root.getCompound("HumanoidProjected"), registries);
        horseIncoming.load(root == null ? new CompoundTag() : root.getCompound("HorseIncoming"), registries);
        horseProjected.load(root == null ? new CompoundTag() : root.getCompound("HorseProjected"), registries);
        hiddenEquipmentChannels.clear();
        CompoundTag visibility = root != null && root.contains("EquipmentVisibility")
                ? root.getCompound("EquipmentVisibility")
                : new CompoundTag();
        for (VirtualEquipmentSnapshots.Channel channel : VirtualEquipmentSnapshots.Channel.values()) {
            if (visibility.contains(channel.serializedName())
                    && !visibility.getBoolean(channel.serializedName())
                    && hasProjectedEquipment(channel)) {
                hiddenEquipmentChannels.add(channel);
            }
        }
        pruneProjectedDuplicates(humanoidIncoming, humanoidProjected, HUMANOID_CHANNELS);
        pruneProjectedDuplicates(horseIncoming, horseProjected, EntityScanData.HORSE_CHANNELS);
        pruneIncompatibleWorkspaceForActiveEntity();
    }

    private void pruneIncompatibleWorkspaceForActiveEntity() {
        if (!hasActiveEntity()) {
            return;
        }
        switch (activeKind()) {
            case HUMANOID -> clearHorseWorkspace();
            case HORSE -> clearHumanoidWorkspace();
            case GENERIC -> {
                clearHumanoidWorkspace();
                clearHorseWorkspace();
            }
        }
    }

    private static void pruneProjectedDuplicates(
            VirtualEquipmentSnapshots incoming,
            VirtualEquipmentSnapshots projected,
            VirtualEquipmentSnapshots.Channel[] channels
    ) {
        for (VirtualEquipmentSnapshots.Channel channel : channels) {
            VirtualEquipmentSnapshots.Snapshot staged = incoming.get(channel);
            VirtualEquipmentSnapshots.Snapshot active = projected.get(channel);
            if (!staged.stack().isEmpty()
                    && !active.stack().isEmpty()
                    && ItemStack.matches(staged.stack(), active.stack())) {
                incoming.clear(channel);
            }
        }
    }

    private static void loadIncoming(
            VirtualEquipmentSnapshots destination,
            VirtualEquipmentSnapshots projected,
            CompoundTag equipment,
            HolderLookup.Provider registries,
            EquipmentSlot[] slots
    ) {
        destination.clearAll();
        for (EquipmentSlot slot : slots) {
            ItemStack stack = EntityScanData.equipmentStack(equipment, slot, registries);
            if (stack.isEmpty()) {
                continue;
            }
            VirtualEquipmentSnapshots.Channel channel = switch (slot) {
                case HEAD -> VirtualEquipmentSnapshots.Channel.HEAD;
                case CHEST -> VirtualEquipmentSnapshots.Channel.CHEST;
                case LEGS -> VirtualEquipmentSnapshots.Channel.LEGS;
                case FEET -> VirtualEquipmentSnapshots.Channel.FEET;
                case MAINHAND -> VirtualEquipmentSnapshots.Channel.MAIN_HAND;
                case OFFHAND -> VirtualEquipmentSnapshots.Channel.OFF_HAND;
                case BODY -> VirtualEquipmentSnapshots.Channel.BODY;
            };
            VirtualEquipmentSnapshots.Snapshot projectedSnapshot = projected.get(channel);
            if (!projectedSnapshot.stack().isEmpty() && ItemStack.matches(projectedSnapshot.stack(), stack)) {
                continue;
            }
            destination.put(channel, stack);
        }
    }

    private static void loadIncoming(
            VirtualEquipmentSnapshots destination,
            VirtualEquipmentSnapshots projected,
            CompoundTag equipment,
            HolderLookup.Provider registries,
            VirtualEquipmentSnapshots.Channel[] channels
    ) {
        destination.clearAll();
        for (VirtualEquipmentSnapshots.Channel channel : channels) {
            ItemStack stack = EntityScanData.equipmentStack(equipment, channel, registries);
            if (stack.isEmpty()) {
                continue;
            }
            VirtualEquipmentSnapshots.Snapshot projectedSnapshot = projected.get(channel);
            if (!projectedSnapshot.stack().isEmpty() && ItemStack.matches(projectedSnapshot.stack(), stack)) {
                continue;
            }
            destination.put(channel, stack);
        }
    }

    private static final VirtualEquipmentSnapshots.Channel[] HUMANOID_CHANNELS = {
            VirtualEquipmentSnapshots.Channel.HEAD,
            VirtualEquipmentSnapshots.Channel.CHEST,
            VirtualEquipmentSnapshots.Channel.LEGS,
            VirtualEquipmentSnapshots.Channel.FEET,
            VirtualEquipmentSnapshots.Channel.MAIN_HAND,
            VirtualEquipmentSnapshots.Channel.OFF_HAND
    };

    public enum ApplyResult {
        APPLIED,
        CONFLICT,
        EMPTY_INCOMING,
        ALREADY_APPLIED
    }
}
