package celerbi.mirageprojector.blockentity;

import celerbi.mirageprojector.ImageSourceBank;
import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.ProjectorStateTransfer;
import celerbi.mirageprojector.block.MirageProjectorBlock;
import celerbi.mirageprojector.entity.EntityProjectionState;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.entity.EquipmentSnapshotRules;
import celerbi.mirageprojector.entity.GenericPosePreset;
import celerbi.mirageprojector.entity.HorsePosePreset;
import celerbi.mirageprojector.entity.HumanoidPosePreset;
import celerbi.mirageprojector.entity.VirtualEquipmentSnapshots;
import celerbi.mirageprojector.item.EntityScanCardItem;
import celerbi.mirageprojector.menu.MirageProjectorMenu;
import celerbi.mirageprojector.registry.ModBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class MirageProjectorBlockEntity extends BlockEntity implements MenuProvider {
    private ProjectionSettings settings = ProjectionSettings.DEFAULT;
    private final ImageSourceBank imageSourceBank = new ImageSourceBank();

    private final ItemStackHandler projectionSnapshot = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    @Nullable
    private UUID projectionSnapshotId;

    private final ItemStackHandler bannerSnapshots = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof BannerItem;
        }
    };

    private final ItemStackHandler legacyProjectionReturnItem = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    private final ItemStackHandler entityScanCard = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            handleEntityScanCardChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof EntityScanCardItem && EntityScanData.hasScan(stack);
        }
    };

    private final EntityProjectionState entityProjectionState = new EntityProjectionState();
    private EntityScanData.Kind stagedEntityCardKind = EntityScanData.Kind.GENERIC;
    private boolean loadingEntityProjectionState;

    private final ItemStackHandler humanoidStagingItems = new ItemStackHandler(EntityScanData.HUMANOID_SLOTS.length) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            EquipmentSlot target = EntityScanData.HUMANOID_SLOTS[slot];
            if (target == EquipmentSlot.MAINHAND || target == EquipmentSlot.OFFHAND) {
                return !stack.isEmpty();
            }
            return EquipmentSnapshotRules.fitsHumanoid(stack, target);
        }
    };

    private final ItemStackHandler horseStagingItems = new ItemStackHandler(EntityScanData.HORSE_CHANNELS.length) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            VirtualEquipmentSnapshots.Channel target = EntityScanData.HORSE_CHANNELS[slot];
            return EquipmentSnapshotRules.fitsHorse(stack, target);
        }
    };

    private final ItemStackHandler coreItem = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return ProjectionCoreProfile.isCoreItem(stack);
        }
    };

    @Nullable
    private ItemStack pendingPackedPlayerBreakDrop;

    public MirageProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIRAGE_PROJECTOR.get(), pos, state);

        if (MirageProjectorBlock.chassisProfile(state) == ProjectionChassisProfile.COMPACT) {
            coreItem.setStackInSlot(0, new ItemStack(Blocks.GLASS));
        }
    }

    public ProjectionSettings settings() {
        return settings;
    }

    public ImageSourceBank imageSourceBank() {
        return imageSourceBank;
    }

    public void replaceImageSourceBank(ImageSourceBank bank) {
        applyImageWorkspace(settings.withSourceMode(ProjectionSettings.SourceMode.IMAGE), bank);
    }

    public void applyImageWorkspace(ProjectionSettings newSettings, ImageSourceBank bank) {
        ProjectionSettings next = newSettings.sanitized().withSourceMode(ProjectionSettings.SourceMode.IMAGE);
        if (!chassisProfile().supportsMultiSourceImageLayout()
                && next.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI) {
            next = next.withImageLayoutMode(ProjectionSettings.ImageLayoutMode.SINGLE);
        }
        settings = next;
        imageSourceBank.clearAll();
        if (bank != null) {

            for (int i = 0; i < ImageSourceBank.PERSISTED_COMPAT_SLOTS; i++) {
                imageSourceBank.set(i, bank.get(i));
            }
        }
        setChangedAndSync();
    }

    public ProjectionChassisProfile chassisProfile() {
        return MirageProjectorBlock.chassisProfile(getBlockState());
    }

    public ItemStackHandler projectionSnapshot() {
        return projectionSnapshot;
    }

    public ItemStackHandler bannerSnapshots() {
        return bannerSnapshots;
    }

    public ItemStack bannerSnapshot(int face) {
        return face >= 0 && face < bannerSnapshots.getSlots()
                ? bannerSnapshots.getStackInSlot(face)
                : ItemStack.EMPTY;
    }

    public boolean bannerFaceAvailable(int face) {
        if (face < 0 || face >= bannerSnapshots.getSlots()) {
            return false;
        }
        return face == 0 || chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM;
    }

    public ItemStackHandler entityScanCard() {
        return entityScanCard;
    }

    public EntityProjectionState entityProjectionState() {
        return entityProjectionState;
    }

    public ItemStack stagedEntityCard() {
        return entityScanCard.getStackInSlot(0);
    }

    public EntityScanData.Kind stagedEntityCardKind() {
        return stagedEntityCardKind;
    }

    public ItemStackHandler humanoidStagingItems() {
        return humanoidStagingItems;
    }

    public ItemStackHandler horseStagingItems() {
        return horseStagingItems;
    }

    public ItemStackHandler coreItem() {
        return coreItem;
    }

    public ItemStack projectedStack() {
        return projectionSnapshot.getStackInSlot(0);
    }

    @Nullable
    public UUID projectionSnapshotId() {
        return projectionSnapshotId;
    }

    public void captureProjectionSnapshot(ItemStack source) {
        if (source == null || source.isEmpty()) {
            clearProjectionSnapshot();
            return;
        }

        projectionSnapshotId = UUID.randomUUID();
        projectionSnapshot.setStackInSlot(0, source.copyWithCount(1));
        settings = settings.withSourceMode(ProjectionSettings.SourceMode.ITEM);
        setChangedAndSync();
    }

    public void clearProjectionSnapshot() {
        projectionSnapshotId = null;
        projectionSnapshot.setStackInSlot(0, ItemStack.EMPTY);
        setChangedAndSync();
    }

    public boolean captureBannerSnapshot(int face, ItemStack source) {
        if (!bannerFaceAvailable(face) || source == null || source.isEmpty()
                || !(source.getItem() instanceof BannerItem)) {
            return false;
        }
        bannerSnapshots.setStackInSlot(face, source.copyWithCount(1));
        settings = settings.withSourceMode(ProjectionSettings.SourceMode.BANNER);
        setChangedAndSync();
        return true;
    }

    public void clearBannerSnapshot(int face) {
        if (!bannerFaceAvailable(face)) {
            return;
        }
        bannerSnapshots.setStackInSlot(face, ItemStack.EMPTY);
        setChangedAndSync();
    }

    public boolean copyPrimaryBannerToAllFaces() {
        if (chassisProfile().geometry() != ProjectionChassisProfile.Geometry.PRISM) {
            return false;
        }
        ItemStack primary = bannerSnapshot(0);
        if (primary.isEmpty()) {
            return false;
        }
        for (int face = 1; face < bannerSnapshots.getSlots(); face++) {
            bannerSnapshots.setStackInSlot(face, primary.copyWithCount(1));
        }
        settings = settings.withSourceMode(ProjectionSettings.SourceMode.BANNER);
        setChangedAndSync();
        return true;
    }

    public boolean hasAnyBannerSnapshot() {
        int count = chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM
                ? bannerSnapshots.getSlots() : 1;
        for (int face = 0; face < count; face++) {
            if (!bannerSnapshots.getStackInSlot(face).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void clearAllBannerSnapshots() {
        for (int face = 0; face < bannerSnapshots.getSlots(); face++) {
            bannerSnapshots.setStackInSlot(face, ItemStack.EMPTY);
        }
        setChangedAndSync();
    }

    public boolean hasLegacyProjectionReturnItem() {
        return !legacyProjectionReturnItem.getStackInSlot(0).isEmpty();
    }

    public ItemStack extractLegacyProjectionReturnItem() {
        return legacyProjectionReturnItem.extractItem(0, 1, false);
    }

    public ItemStack coreStack() {
        return coreItem.getStackInSlot(0);
    }

    public ProjectionCoreProfile coreProfile() {
        return ProjectionCoreProfile.fromStack(coreStack());
    }

    public ProjectionPower.Status powerStatus() {
        return ProjectionPower.evaluate(settings, coreProfile(), chassisProfile(), hasProjectedSourceContent(), projectedSourceCount());
    }

    public int projectedSourceCount() {
        return switch (settings.sourceMode()) {
            case IMAGE -> {
                if (chassisProfile().supportsMultiSourceImageLayout() && settings.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI) {
                    yield imageSourceBank.countPresent(chassisProfile().imageLayoutSlots());
                }
                if (chassisProfile().geometry() != ProjectionChassisProfile.Geometry.PRISM) {
                    yield hasPlaneImageContent(settings) ? 1 : 0;
                }
                int count = 0;
                if (settings.hasImage()) {
                    count++;
                }
                if (settings.hasEastImage()) {
                    count++;
                }
                if (settings.hasBackImage()) {
                    count++;
                }
                if (settings.hasWestImage()) {
                    count++;
                }
                yield count;
            }
            case ITEM -> projectedStack().isEmpty() ? 0 : 1;
            case ENTITY -> entityProjectionState.hasProjectedEntityContent() ? 1 : 0;
            case BANNER -> {
                int limit = chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM ? 4 : 1;
                int count = 0;
                for (int face = 0; face < limit; face++) {
                    if (!bannerSnapshot(face).isEmpty()) {
                        count++;
                    }
                }
                yield count;
            }
        };
    }

    public boolean hasProjectedSourceContent() {
        return switch (settings.sourceMode()) {
            case IMAGE -> chassisProfile().supportsMultiSourceImageLayout() && settings.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI
                    ? imageSourceBank.hasAny(chassisProfile().imageLayoutSlots())
                    : chassisProfile().geometry() == ProjectionChassisProfile.Geometry.PRISM
                    ? settings.hasAnyImage()
                    : hasPlaneImageContent(settings);
            case ITEM -> !projectedStack().isEmpty();
            case ENTITY -> entityProjectionState.hasProjectedEntityContent();
            case BANNER -> hasAnyBannerSnapshot();
        };
    }

    private static boolean hasPlaneImageContent(ProjectionSettings settings) {
        return switch (settings.backFaceMode()) {
            case FRONT, MIRRORED, READABLE -> settings.hasImage();
            case BACK -> settings.hasBackImage();
            case INDEPENDENT -> settings.hasImage() || settings.hasBackImage();
        };
    }

    public EntityProjectionState.ApplyResult applyEntityEquipment(
            VirtualEquipmentSnapshots.Channel channel,
            boolean replaceExisting
    ) {
        ItemStack staged = physicalStagingStack(channel);
        if (!staged.isEmpty()) {
            VirtualEquipmentSnapshots incoming = channel.humanoid()
                    ? entityProjectionState.humanoidIncoming()
                    : entityProjectionState.horseIncoming();
            incoming.put(channel, staged);
        }

        EntityProjectionState.ApplyResult result = entityProjectionState.applyIncoming(channel, replaceExisting);
        if (result == EntityProjectionState.ApplyResult.APPLIED
                || result == EntityProjectionState.ApplyResult.ALREADY_APPLIED) {
            settings = settings.withSourceMode(ProjectionSettings.SourceMode.ENTITY);
            setChangedAndSync();
        }
        return result;
    }

    public void clearProjectedEntityEquipment(VirtualEquipmentSnapshots.Channel channel) {
        entityProjectionState.clearProjected(channel);
        setChangedAndSync();
    }

    public boolean toggleProjectedEntityEquipmentVisibility(VirtualEquipmentSnapshots.Channel channel) {
        if (channel == null) {
            return false;
        }
        boolean visible = entityProjectionState.toggleEquipmentVisible(channel);
        setChangedAndSync();
        return visible;
    }

    public int captureEquippedHumanoidLoadout(Player player) {
        int captured = entityProjectionState.captureEquippedHumanoidLoadout(player);
        setChangedAndSync();
        return captured;
    }

    public HumanoidPosePreset cycleHumanoidPose() {
        HumanoidPosePreset pose = entityProjectionState.cycleHumanoidPose();
        settings = settings.withSourceMode(ProjectionSettings.SourceMode.ENTITY);
        setChangedAndSync();
        return pose;
    }

    public HorsePosePreset cycleHorsePose() {
        HorsePosePreset pose = entityProjectionState.cycleHorsePose();
        settings = settings.withSourceMode(ProjectionSettings.SourceMode.ENTITY);
        setChangedAndSync();
        return pose;
    }

    public GenericPosePreset cycleGenericPose() {
        GenericPosePreset pose = entityProjectionState.cycleGenericPose();
        settings = settings.withSourceMode(ProjectionSettings.SourceMode.ENTITY);
        setChangedAndSync();
        return pose;
    }

    public boolean hasPhysicalHorseStaging() {
        for (int slot = 0; slot < horseStagingItems.getSlots(); slot++) {
            if (!horseStagingItems.getStackInSlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int returnPhysicalStagingTo(Player player) {
        int returned = returnHandlerToPlayer(humanoidStagingItems, player);
        returned += returnHandlerToPlayer(horseStagingItems, player);
        if (returned > 0) {
            setChangedAndSync();
        }
        return returned;
    }

    public boolean returnPhysicalStagingChannelTo(
            VirtualEquipmentSnapshots.Channel channel,
            Player player
    ) {
        ItemStackHandler handler;
        int slot;
        if (channel.humanoid()) {
            handler = humanoidStagingItems;
            slot = humanoidIndex(channel);
        } else if (channel.horse()) {
            handler = horseStagingItems;
            slot = horseIndex(channel);
        } else {
            return false;
        }

        boolean returned = returnSlotToPlayer(handler, slot, player);
        if (returned) {
            setChangedAndSync();
        }
        return returned;
    }

    private ItemStack physicalStagingStack(VirtualEquipmentSnapshots.Channel channel) {
        if (channel.humanoid()) {
            return humanoidStagingItems.getStackInSlot(humanoidIndex(channel));
        }
        if (channel.horse()) {
            return horseStagingItems.getStackInSlot(horseIndex(channel));
        }
        return ItemStack.EMPTY;
    }

    private static int humanoidIndex(VirtualEquipmentSnapshots.Channel channel) {
        return switch (channel) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            case MAIN_HAND -> 4;
            case OFF_HAND -> 5;
            default -> throw new IllegalArgumentException("Not a Humanoid staging channel: " + channel);
        };
    }

    private static int horseIndex(VirtualEquipmentSnapshots.Channel channel) {
        return switch (channel) {
            case SADDLE -> 0;
            case BODY -> 1;
            default -> throw new IllegalArgumentException("Not a Horse staging channel: " + channel);
        };
    }

    private static int returnHandlerToPlayer(ItemStackHandler handler, Player player) {
        int returned = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (returnSlotToPlayer(handler, slot, player)) {
                returned++;
            }
        }
        return returned;
    }

    private static boolean returnSlotToPlayer(ItemStackHandler handler, int slot, Player player) {
        ItemStack moving = handler.extractItem(slot, handler.getSlotLimit(slot), false);
        if (moving.isEmpty()) {
            return false;
        }

        player.getInventory().add(moving);
        if (!moving.isEmpty()) {

            player.drop(moving, false);
        }
        return true;
    }

    public void applySettings(ProjectionSettings newSettings) {
        settings = newSettings.sanitized();
        setChangedAndSync();
    }

    private void handleEntityScanCardChanged() {
        if (loadingEntityProjectionState) {
            return;
        }

        ItemStack card = stagedEntityCard();
        if (card.isEmpty() || !EntityScanData.hasScan(card)) {
            entityProjectionState.onStagedCardRemoved(stagedEntityCardKind);
            stagedEntityCardKind = EntityScanData.Kind.GENERIC;
            setChangedAndSync();
            return;
        }

        EntityScanData.read(card).ifPresent(scan -> {
            if (stagedEntityCardKind == EntityScanData.Kind.HORSE && scan.kind() != EntityScanData.Kind.HORSE) {
                entityProjectionState.onStagedCardRemoved(stagedEntityCardKind);
            }
            stagedEntityCardKind = scan.kind();
            settings = settings.withSourceMode(ProjectionSettings.SourceMode.ENTITY);
            if (level != null) {
                entityProjectionState.importFromCard(card, level.registryAccess());
            }
        });
        setChangedAndSync();
    }

    public void preparePackedPlayerBreak(HolderLookup.Provider registries) {
        pendingPackedPlayerBreakDrop = ProjectorStateTransfer.packPlacedProjector(this, registries);
    }

    public boolean hasPendingPackedPlayerBreakDrop() {
        return pendingPackedPlayerBreakDrop != null && !pendingPackedPlayerBreakDrop.isEmpty();
    }

    public ItemStack takePendingPackedPlayerBreakDrop() {
        if (!hasPendingPackedPlayerBreakDrop()) {
            pendingPackedPlayerBreakDrop = null;
            return ItemStack.EMPTY;
        }
        ItemStack result = pendingPackedPlayerBreakDrop;
        pendingPackedPlayerBreakDrop = null;
        return result;
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public void writeMenuData(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(worldPosition);
        settings.write(buffer);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        settings.save(tag);
        tag.put("ImageSourceBank", imageSourceBank.save());
        tag.put("ProjectionSnapshot", projectionSnapshot.serializeNBT(registries));
        tag.put("BannerSnapshots", bannerSnapshots.serializeNBT(registries));
        if (projectionSnapshotId != null) {
            tag.putUUID("ProjectionSnapshotId", projectionSnapshotId);
        }
        if (hasLegacyProjectionReturnItem()) {
            tag.put("LegacyProjectionReturnItem", legacyProjectionReturnItem.serializeNBT(registries));
        }
        if (!stagedEntityCard().isEmpty()) {
            tag.put("EntityScanCard", entityScanCard.serializeNBT(registries));
        }
        tag.put("EntityProjectionState", entityProjectionState.save(registries));
        tag.put("HumanoidStagingItems", humanoidStagingItems.serializeNBT(registries));
        tag.put("HorseStagingItems", horseStagingItems.serializeNBT(registries));
        tag.put("CoreItem", coreItem.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        settings = ProjectionSettings.load(tag);
        if (!chassisProfile().supportsMultiSourceImageLayout()
                && settings.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI) {
            settings = settings.withImageLayoutMode(ProjectionSettings.ImageLayoutMode.SINGLE);
        }
        imageSourceBank.clearAll();
        if (tag.contains("ImageSourceBank")) {
            imageSourceBank.load(tag.getCompound("ImageSourceBank"));
        } else if (chassisProfile().supportsMultiSourceImageLayout() && settings.hasImage()) {

            imageSourceBank.set(0, settings.imageId(), settings.imageWidth(), settings.imageHeight());
        }

        if (!tag.contains("ImageLayoutMode") && !settings.hasImage()) {
            ImageSourceBank.Asset legacySlot = imageSourceBank.get(0);
            if (legacySlot.present()) {
                settings = settings.withImage(legacySlot.id(), legacySlot.width(), legacySlot.height())
                        .withImageLayoutMode(ProjectionSettings.ImageLayoutMode.SINGLE);
            }
        }
        projectionSnapshotId = null;
        projectionSnapshot.setStackInSlot(0, ItemStack.EMPTY);
        for (int face = 0; face < bannerSnapshots.getSlots(); face++) {
            bannerSnapshots.setStackInSlot(face, ItemStack.EMPTY);
        }
        if (tag.contains("BannerSnapshots")) {
            bannerSnapshots.deserializeNBT(registries, tag.getCompound("BannerSnapshots"));
            for (int face = 0; face < bannerSnapshots.getSlots(); face++) {
                ItemStack stack = bannerSnapshots.getStackInSlot(face);
                if (!stack.isEmpty() && !(stack.getItem() instanceof BannerItem)) {
                    bannerSnapshots.setStackInSlot(face, ItemStack.EMPTY);
                } else if (!stack.isEmpty() && stack.getCount() != 1) {
                    bannerSnapshots.setStackInSlot(face, stack.copyWithCount(1));
                }
            }
        }
        legacyProjectionReturnItem.setStackInSlot(0, ItemStack.EMPTY);
        stagedEntityCardKind = EntityScanData.Kind.GENERIC;
        loadingEntityProjectionState = true;
        entityScanCard.setStackInSlot(0, ItemStack.EMPTY);
        entityProjectionState.load(
                tag.contains("EntityProjectionState") ? tag.getCompound("EntityProjectionState") : new CompoundTag(),
                registries
        );
        if (tag.contains("EntityScanCard")) {
            entityScanCard.deserializeNBT(registries, tag.getCompound("EntityScanCard"));
            EntityScanData.read(stagedEntityCard()).ifPresent(scan -> stagedEntityCardKind = scan.kind());
        }
        if (tag.contains("HumanoidStagingItems")) {
            humanoidStagingItems.deserializeNBT(registries, tag.getCompound("HumanoidStagingItems"));
        }
        if (tag.contains("HorseStagingItems")) {
            horseStagingItems.deserializeNBT(registries, tag.getCompound("HorseStagingItems"));
        }
        loadingEntityProjectionState = false;

        if (tag.contains("ProjectionSnapshot")) {
            projectionSnapshot.deserializeNBT(registries, tag.getCompound("ProjectionSnapshot"));
            if (!projectionSnapshot.getStackInSlot(0).isEmpty()) {
                projectionSnapshotId = tag.hasUUID("ProjectionSnapshotId")
                        ? tag.getUUID("ProjectionSnapshotId")
                        : UUID.randomUUID();
            }
            if (tag.contains("LegacyProjectionReturnItem")) {
                legacyProjectionReturnItem.deserializeNBT(registries, tag.getCompound("LegacyProjectionReturnItem"));
            }
        } else if (tag.contains("ProjectionItem")) {

            ItemStackHandler oldPhysicalSlot = new ItemStackHandler(1);
            oldPhysicalSlot.deserializeNBT(registries, tag.getCompound("ProjectionItem"));
            ItemStack oldStack = oldPhysicalSlot.getStackInSlot(0);
            if (!oldStack.isEmpty()) {
                legacyProjectionReturnItem.setStackInSlot(0, oldStack.copyWithCount(1));
                projectionSnapshot.setStackInSlot(0, oldStack.copyWithCount(1));
                projectionSnapshotId = UUID.randomUUID();
            }
        }

        if (tag.contains("CoreItem")) {
            coreItem.deserializeNBT(registries, tag.getCompound("CoreItem"));
            if (!coreItem.getStackInSlot(0).isEmpty() && !ProjectionCoreProfile.isCoreItem(coreItem.getStackInSlot(0))) {
                coreItem.setStackInSlot(0, ItemStack.EMPTY);
            }
        } else if (chassisProfile() == ProjectionChassisProfile.COMPACT) {

            coreItem.setStackInSlot(0, new ItemStack(Blocks.GLASS));
        } else {
            coreItem.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        String key = switch (chassisProfile()) {
            case DISPLAY -> "container.mirage_projector.display";
            case WIDE -> "container.mirage_projector.wide";
            case TALL -> "container.mirage_projector.tall";
            case FIELD -> "container.mirage_projector.field";
            case PRISM -> "container.mirage_projector.prism";
            default -> "container.mirage_projector.projector";
        };
        return Component.translatable(key);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MirageProjectorMenu(containerId, inventory, this);
    }
}
