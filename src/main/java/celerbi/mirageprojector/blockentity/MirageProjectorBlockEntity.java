package celerbi.mirageprojector.blockentity;

import celerbi.mirageprojector.ImageSourceBank;
import celerbi.mirageprojector.EndResonanceGeometry;
import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.ProjectionSourceRegistry;
import celerbi.mirageprojector.ProjectorStateTransfer;
import celerbi.mirageprojector.SpecialResonanceProfile;
import celerbi.mirageprojector.WallProjectionSurface;
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
import celerbi.mirageprojector.registry.ModItems;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class MirageProjectorBlockEntity extends BlockEntity implements MenuProvider {
    private static final Map<UUID, Long> END_RESONANCE_COOLDOWNS = new HashMap<>();
    private ProjectionSettings settings = ProjectionSettings.DEFAULT;
    private boolean projectionEnabled = true;
    private final ImageSourceBank imageSourceBank = new ImageSourceBank();
    // Historical name kept in network/helper aliases, but since 1.0.26 this is the active slide
    // index for every presentation-capable chassis (Table + Wall/Data-show).
    private int wallSlideIndex;
    private boolean automaticPresentationEnabled;
    private int automaticPresentationIntervalTicks = 10 * 20;
    private int automaticPresentationElapsedTicks;
    @Nullable
    private UUID presentationLinkId;
    private transient WallProjectionSurface.Result cachedWallProjection = WallProjectionSurface.Result.invalid(WallProjectionSurface.Failure.NO_IMAGE);
    private transient long cachedWallProjectionTick = Long.MIN_VALUE;

    /**
     * Opaque per-source extension payloads. Built-in 1.0 sources continue using their
     * established fields, while future/addon sources may persist namespaced data here.
     * Unknown payloads are intentionally round-tripped even when their provider is absent.
     */
    private CompoundTag projectionSourcePayloads = new CompoundTag();

    /** Complete suspended projector-facing state while a special resonance catalyst owns runtime. */
    private CompoundTag endResonanceRestoreSnapshot = new CompoundTag();
    private boolean loadingCoreState;

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
            if (!loadingCoreState) {
                handleCoreChanged();
            }
            setChangedAndSync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return acceptsCoreStack(stack);
        }
    };

    private final ItemStackHandler presentationRemote = new ItemStackHandler(1) {
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
            return chassisProfile() == ProjectionChassisProfile.WALL
                    && stack != null && !stack.isEmpty() && stack.is(ModItems.PRESENTATION_REMOTE.get());
        }
    };

    @Nullable
    private ItemStack pendingPackedPlayerBreakDrop;

    public MirageProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIRAGE_PROJECTOR.get(), pos, state);
        if (MirageProjectorBlock.chassisProfile(state) == ProjectionChassisProfile.TABLE) {
            // A table projector is primarily a stationary presentation surface. Rotation remains
            // available, but new tables start still instead of immediately spinning their source.
            settings = settings.withRotationEnabled(false);
        }
    }

    public ProjectionSettings settings() {
        return settings;
    }

    public boolean projectionEnabled() {
        return projectionEnabled;
    }

    public SpecialResonanceProfile specialResonanceProfile() {
        SpecialResonanceProfile profile = SpecialResonanceProfile.fromStack(coreStack());
        return profile.supportedBy(chassisProfile()) ? profile : SpecialResonanceProfile.NONE;
    }

    public boolean acceptsCoreStack(ItemStack stack) {
        if (ProjectionCoreProfile.isCoreItem(stack)) {
            return true;
        }
        return SpecialResonanceProfile.fromStack(stack).supportedBy(chassisProfile());
    }

    public boolean endResonanceActive() {
        return specialResonanceProfile() == SpecialResonanceProfile.END_RESONANCE;
    }

    /** Normal projection controls are suspended while the special resonance owns the chassis. */
    public boolean endResonanceLocksControls() {
        return endResonanceActive();
    }

    public boolean hasEndResonanceRestoreSnapshot() {
        return !endResonanceRestoreSnapshot.isEmpty();
    }

    public CompoundTag endResonanceRestoreSnapshotCopy() {
        return endResonanceRestoreSnapshot.copy();
    }

    public boolean setProjectionEnabled(boolean enabled) {
        if (!enabled && endResonanceLocksControls()) {
            return false;
        }
        if (projectionEnabled == enabled) {
            return true;
        }
        projectionEnabled = enabled;
        setChangedAndSync();
        return true;
    }

    public boolean activateProjectionSource(ProjectionSettings.SourceMode sourceMode) {
        if (sourceMode == null || endResonanceLocksControls()
                || !ProjectionSourceRegistry.isCompatible(sourceMode, chassisProfile())) {
            return false;
        }
        boolean changedMode = settings.sourceMode() != sourceMode;
        settings = settings.withSourceMode(sourceMode);
        // A Table is a presentation surface by default.  Motion remains a deliberate per-mode
        // choice, but it must not leak from one source family into the next one.
        if (changedMode && chassisProfile() == ProjectionChassisProfile.TABLE) {
            settings = settings.withPresentation(
                    settings.scalePixels(), settings.liftPixels(), false,
                    settings.rotationPeriodTicks(), settings.clockwise(), settings.rotationOffsetDegrees(),
                    false, settings.floatMode(), settings.floatAmplitudePixels(),
                    settings.floatCycleTicks(), settings.floatIntervalDegrees(), settings.fullbright(),
                    settings.opacityPercent(), settings.tintRgb(), settings.debugChassisOverride()
            );
        }
        projectionEnabled = true;
        setChangedAndSync();
        return true;
    }

    public ImageSourceBank imageSourceBank() {
        return imageSourceBank;
    }

    public CompoundTag projectionSourcePayload(ProjectionSettings.SourceMode source) {
        if (source == null || !projectionSourcePayloads.contains(source.serializedName())) {
            return new CompoundTag();
        }
        return projectionSourcePayloads.getCompound(source.serializedName()).copy();
    }

    public void setProjectionSourcePayload(ProjectionSettings.SourceMode source, CompoundTag payload) {
        if (endResonanceLocksControls() || source == null) {
            return;
        }
        if (payload == null || payload.isEmpty()) {
            projectionSourcePayloads.remove(source.serializedName());
        } else {
            projectionSourcePayloads.put(source.serializedName(), payload.copy());
        }
        setChangedAndSync();
    }

    public void replaceImageSourceBank(ImageSourceBank bank) {
        applyImageWorkspace(settings, bank, wallSlideIndex);
    }

    public void applyImageWorkspace(ProjectionSettings newSettings, ImageSourceBank bank) {
        applyImageWorkspace(newSettings, bank, wallSlideIndex);
    }

    public void applyImageWorkspace(ProjectionSettings newSettings, ImageSourceBank bank, int requestedWallSlideIndex) {
        applyImageWorkspace(newSettings, bank, requestedWallSlideIndex, automaticPresentationEnabled, automaticPresentationIntervalSeconds());
    }

    public void applyImageWorkspace(
            ProjectionSettings newSettings, ImageSourceBank bank, int requestedWallSlideIndex,
            boolean automaticPresentation, int automaticIntervalSeconds
    ) {
        if (endResonanceLocksControls()) {
            return;
        }
        ProjectionSettings next = newSettings.sanitized();
        if (!chassisProfile().supportsMultiSourceImageLayout()
                && next.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI) {
            next = next.withImageLayoutMode(ProjectionSettings.ImageLayoutMode.SINGLE);
        }
        imageSourceBank.clearAll();
        if (bank != null) {
            for (int i = 0; i < ImageSourceBank.PERSISTED_COMPAT_SLOTS; i++) {
                imageSourceBank.set(i, bank.get(i));
            }
        }

        if (supportsPresentationDeck()) {
            // Presentation-capable chassis own all nine compatibility slots. Legacy single-image worlds
            // are promoted into slot 0 so upgrading into the Wall chassis never loses the source.
            if (!imageSourceBank.hasAny(ImageSourceBank.PERSISTED_COMPAT_SLOTS) && next.hasImage()) {
                imageSourceBank.set(0, next.imageId(), next.imageWidth(), next.imageHeight());
            }
            wallSlideIndex = imageSourceBank.normalizePresentIndex(requestedWallSlideIndex);
            ImageSourceBank.Asset active = activeWallImageFromBank(next);
            // Editing the image deck intentionally selects Image, but the deck itself is shared
            // by Table and Wall.  Table may subsequently project Entity or Banner; only Wall is
            // image-only by chassis contract.
            next = next.withSourceMode(ProjectionSettings.SourceMode.IMAGE)
                    .withImageLayoutMode(ProjectionSettings.ImageLayoutMode.SINGLE);
            if (chassisProfile() == ProjectionChassisProfile.WALL) {
                next = next.withBackFaceMode(ProjectionSettings.BackFaceMode.FRONT);
            }
            if (active.present()) {
                next = next.withImage(active.id(), active.width(), active.height());
            }
        }
        boolean previousAutomatic = automaticPresentationEnabled;
        int previousIntervalTicks = automaticPresentationIntervalTicks();
        automaticPresentationEnabled = supportsPresentationDeck() && automaticPresentation;
        automaticPresentationIntervalTicks = Math.max(20, Math.min(2400, automaticIntervalSeconds * 20));
        // Slide selection/reordering/import is independent from the playback clock.  Preserve
        // elapsed time when the automatic configuration itself did not change; only toggling
        // automatic playback or changing its interval starts a fresh countdown.
        if (previousAutomatic != automaticPresentationEnabled
                || previousIntervalTicks != automaticPresentationIntervalTicks()) {
            automaticPresentationElapsedTicks = 0;
        }
        settings = next;
        invalidateWallProjectionCache();
        setChangedAndSync();
    }

    public int wallSlideIndex() {
        return imageSourceBank.normalizePresentIndex(wallSlideIndex);
    }

    public ImageSourceBank.Asset activeWallImage() {
        return activeWallImageFromBank(settings);
    }

    private ImageSourceBank.Asset activeWallImageFromBank(ProjectionSettings fallbackSettings) {
        int normalized = imageSourceBank.normalizePresentIndex(wallSlideIndex);
        if (normalized >= 0) {
            return imageSourceBank.get(normalized);
        }
        ProjectionSettings safe = fallbackSettings == null ? ProjectionSettings.DEFAULT : fallbackSettings;
        return safe.hasImage()
                ? new ImageSourceBank.Asset(safe.imageId(), safe.imageWidth(), safe.imageHeight())
                : ImageSourceBank.Asset.EMPTY;
    }

    public void setWallSlideIndex(int requestedIndex) {
        // Manual selection must not disturb the automatic-presentation cadence.
        setPresentationSlideIndex(requestedIndex, false);
    }

    public boolean supportsPresentationDeck() {
        return chassisProfile().supportsPresentationDeck();
    }

    public boolean automaticPresentationEnabled() {
        return supportsPresentationDeck() && automaticPresentationEnabled;
    }

    public int automaticPresentationIntervalTicks() {
        return Math.max(20, Math.min(2400, automaticPresentationIntervalTicks));
    }

    public int automaticPresentationIntervalSeconds() {
        return Math.max(1, automaticPresentationIntervalTicks() / 20);
    }

    public int automaticPresentationElapsedTicks() {
        return Math.max(0, automaticPresentationElapsedTicks);
    }

    public void configureAutomaticPresentation(boolean enabled, int intervalSeconds) {
        if (endResonanceLocksControls()) {
            return;
        }
        automaticPresentationEnabled = supportsPresentationDeck() && enabled;
        automaticPresentationIntervalTicks = Math.max(20, Math.min(2400, intervalSeconds * 20));
        automaticPresentationElapsedTicks = 0;
        setChangedAndSync();
    }

    public boolean stepPresentationSlide(int direction, boolean resetTimer) {
        if (endResonanceLocksControls() || !supportsPresentationDeck()) {
            return false;
        }
        int next = imageSourceBank.nextPresentIndex(wallSlideIndex, direction);
        if (next < 0) {
            return false;
        }
        setPresentationSlideIndex(next, resetTimer);
        return true;
    }

    public void setPresentationSlideIndex(int requestedIndex, boolean resetTimer) {
        if (endResonanceLocksControls()) {
            return;
        }
        int normalized = imageSourceBank.normalizePresentIndex(requestedIndex);
        if (normalized < 0) {
            wallSlideIndex = 0;
            if (resetTimer) automaticPresentationElapsedTicks = 0;
            invalidateWallProjectionCache();
            setChangedAndSync();
            return;
        }
        wallSlideIndex = normalized;
        ImageSourceBank.Asset active = imageSourceBank.get(normalized);
        if (active.present()) {
            settings = settings.withSourceMode(ProjectionSettings.SourceMode.IMAGE)
                    .withImage(active.id(), active.width(), active.height())
                    .withImageLayoutMode(ProjectionSettings.ImageLayoutMode.SINGLE);
            if (chassisProfile() == ProjectionChassisProfile.WALL) {
                settings = settings.withBackFaceMode(ProjectionSettings.BackFaceMode.FRONT);
            }
        }
        if (resetTimer) automaticPresentationElapsedTicks = 0;
        invalidateWallProjectionCache();
        setChangedAndSync();
    }

    public UUID ensurePresentationLinkId() {
        if (presentationLinkId == null) {
            presentationLinkId = UUID.randomUUID();
            setChangedAndSync();
        }
        return presentationLinkId;
    }

    @Nullable
    public UUID presentationLinkId() {
        return presentationLinkId;
    }

    public ItemStackHandler presentationRemote() {
        return presentationRemote;
    }

    public boolean hasDockedPresentationRemote() {
        return !presentationRemote.getStackInSlot(0).isEmpty();
    }

    public boolean insertPresentationRemote(ItemStack source, ServerLevel level) {
        if (chassisProfile() != ProjectionChassisProfile.WALL || source == null || source.isEmpty()
                || !source.is(ModItems.PRESENTATION_REMOTE.get()) || hasDockedPresentationRemote()) {
            return false;
        }
        UUID linkId = ensurePresentationLinkId();
        // Bind the actual source stack before copying it into the dock. This matters in Creative:
        // the held stack is not consumed there, so binding only the docked copy made the projector
        // say "paired" while the controller in the player's hand still said "unbound".
        celerbi.mirageprojector.item.PresentationRemoteItem.bind(
                source, level.dimension(), worldPosition, linkId
        );
        ItemStack docked = source.copyWithCount(1);
        presentationRemote.setStackInSlot(0, docked);
        return true;
    }

    public ItemStack extractPresentationRemote(ServerLevel level) {
        ItemStack result = presentationRemote.extractItem(0, 1, false);
        if (!result.isEmpty()) {
            celerbi.mirageprojector.item.PresentationRemoteItem.bind(
                    result, level.dimension(), worldPosition, ensurePresentationLinkId()
            );
        }
        return result;
    }

    public boolean unpairPresentationRemote(ServerPlayer player) {
        if (chassisProfile() != ProjectionChassisProfile.WALL || presentationLinkId == null) {
            return false;
        }
        UUID oldLink = presentationLinkId;
        boolean playerAlreadyHasLinkedCopy = false;

        // In Creative the held source remote remains with the player. In Survival this also
        // invalidates any second copy someone obtained legitimately before an unpair.
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (!candidate.is(ModItems.PRESENTATION_REMOTE.get())) {
                continue;
            }
            var binding = celerbi.mirageprojector.item.PresentationRemoteItem.binding(candidate);
            if (binding.isPresent() && oldLink.equals(binding.get().linkId())) {
                playerAlreadyHasLinkedCopy = true;
                celerbi.mirageprojector.item.PresentationRemoteItem.unbind(candidate);
            }
        }

        ItemStack docked = presentationRemote.extractItem(0, 1, false);
        if (!docked.isEmpty()) {
            celerbi.mirageprojector.item.PresentationRemoteItem.unbind(docked);
            // Avoid handing Creative players an extra duplicate when the original bound remote
            // is already in their hotbar/inventory. Survival always gets the physical docked item.
            if (!player.isCreative() || !playerAlreadyHasLinkedCopy) {
                if (!player.addItem(docked)) {
                    player.drop(docked, false);
                }
            }
        }

        // Invalidates any other old copy immediately. A newly docked remote receives a fresh ID.
        presentationLinkId = null;
        setChangedAndSync();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MirageProjectorBlockEntity projector) {
        if (level instanceof ServerLevel serverLevel && projector.endResonanceActive()) {
            tickEndResonanceTransfer(serverLevel, projector);
        }
        if (!projector.supportsPresentationDeck() || !projector.automaticPresentationEnabled
                || !projector.projectionEnabled()
                || !projector.settings.sourceMode().equals(ProjectionSettings.SourceMode.IMAGE)
                || projector.imageSourceBank.countPresent(ImageSourceBank.PERSISTED_COMPAT_SLOTS) < 2) {
            return;
        }
        projector.automaticPresentationElapsedTicks++;
        if (projector.automaticPresentationElapsedTicks < projector.automaticPresentationIntervalTicks()) {
            return;
        }
        projector.automaticPresentationElapsedTicks = 0;
        projector.stepPresentationSlide(1, false);
    }

    private static void tickEndResonanceTransfer(ServerLevel origin, MirageProjectorBlockEntity projector) {
        long now = origin.getGameTime();
        for (ServerPlayer player : origin.getEntitiesOfClass(ServerPlayer.class, EndResonanceGeometry.resonanceBounds(projector))) {
            if (END_RESONANCE_COOLDOWNS.getOrDefault(player.getUUID(), Long.MIN_VALUE) > now) continue;
            ResourceKey<Level> targetKey = origin.dimension() == Level.END ? Level.OVERWORLD : Level.END;
            ServerLevel target = origin.getServer().getLevel(targetKey);
            if (target == null) continue;
            Vec3 destination = resonanceDestination(target, targetKey);
            END_RESONANCE_COOLDOWNS.put(player.getUUID(), now + 80L);
            player.teleportTo(target, destination.x, destination.y, destination.z,
                    player.getYRot(), player.getXRot());
        }
        for (Entity entity : origin.getEntitiesOfClass(Entity.class, EndResonanceGeometry.resonanceBounds(projector),
                entity -> !(entity instanceof Player) && entity.canChangeDimensions(origin, origin.getServer().getLevel(
                        origin.dimension() == Level.END ? Level.OVERWORLD : Level.END)))) {
            if (END_RESONANCE_COOLDOWNS.getOrDefault(entity.getUUID(), Long.MIN_VALUE) > now) continue;
            ResourceKey<Level> targetKey = origin.dimension() == Level.END ? Level.OVERWORLD : Level.END;
            ServerLevel target = origin.getServer().getLevel(targetKey);
            if (target == null) continue;
            END_RESONANCE_COOLDOWNS.put(entity.getUUID(), now + 80L);
            Vec3 destination = resonanceDestination(target, targetKey);
            entity.changeDimension(new DimensionTransition(target, destination, entity.getDeltaMovement(),
                    entity.getYRot(), entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND));
        }
        END_RESONANCE_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() <= now - 20L);
    }

    private static Vec3 resonanceDestination(ServerLevel target, ResourceKey<Level> targetKey) {
        BlockPos spawn = targetKey == Level.END
                ? new BlockPos(100, 50, 0)
                : target.getSharedSpawnPos();
        int safeY = targetKey == Level.END
                ? spawn.getY()
                : target.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
        return new Vec3(spawn.getX() + 0.5D, safeY, spawn.getZ() + 0.5D);
    }

    public WallProjectionSurface.Result wallProjectionSurface() {
        if (chassisProfile() != ProjectionChassisProfile.WALL || level == null) {
            return WallProjectionSurface.Result.invalid(WallProjectionSurface.Failure.NO_WALL);
        }
        long tick = level.getGameTime();
        if (cachedWallProjectionTick != Long.MIN_VALUE && tick - cachedWallProjectionTick >= 0L
                && tick - cachedWallProjectionTick <= 4L) {
            return cachedWallProjection;
        }
        Direction facing = getBlockState().hasProperty(MirageProjectorBlock.FACING)
                ? getBlockState().getValue(MirageProjectorBlock.FACING)
                : Direction.NORTH;
        cachedWallProjection = WallProjectionSurface.resolve(
                level, worldPosition, facing, settings, activeWallImage(), coreProfile()
        );
        cachedWallProjectionTick = tick;
        return cachedWallProjection;
    }

    private void invalidateWallProjectionCache() {
        cachedWallProjectionTick = Long.MIN_VALUE;
        cachedWallProjection = WallProjectionSurface.Result.invalid(WallProjectionSurface.Failure.NO_IMAGE);
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
        if (endResonanceLocksControls()) {
            return;
        }
        if (source == null || source.isEmpty()) {
            clearProjectionSnapshot();
            return;
        }

        projectionSnapshotId = UUID.randomUUID();
        projectionSnapshot.setStackInSlot(0, source.copyWithCount(1));
        setChangedAndSync();
    }

    public void clearProjectionSnapshot() {
        if (endResonanceLocksControls()) {
            return;
        }
        projectionSnapshotId = null;
        projectionSnapshot.setStackInSlot(0, ItemStack.EMPTY);
        setChangedAndSync();
    }

    public boolean captureBannerSnapshot(int face, ItemStack source) {
        if (endResonanceLocksControls() || !bannerFaceAvailable(face) || source == null || source.isEmpty()
                || !(source.getItem() instanceof BannerItem)) {
            return false;
        }
        bannerSnapshots.setStackInSlot(face, source.copyWithCount(1));
        setChangedAndSync();
        return true;
    }

    public void clearBannerSnapshot(int face) {
        if (endResonanceLocksControls() || !bannerFaceAvailable(face)) {
            return;
        }
        bannerSnapshots.setStackInSlot(face, ItemStack.EMPTY);
        setChangedAndSync();
    }

    public boolean copyPrimaryBannerToAllFaces() {
        if (endResonanceLocksControls() || chassisProfile().geometry() != ProjectionChassisProfile.Geometry.PRISM) {
            return false;
        }
        ItemStack primary = bannerSnapshot(0);
        if (primary.isEmpty()) {
            return false;
        }
        for (int face = 1; face < bannerSnapshots.getSlots(); face++) {
            bannerSnapshots.setStackInSlot(face, primary.copyWithCount(1));
        }
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
        if (endResonanceLocksControls()) {
            return;
        }
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
        if (chassisProfile() == ProjectionChassisProfile.WALL) {
            WallProjectionSurface.Result surface = wallProjectionSurface();
            if (surface.powerStatus() != null) {
                return surface.powerStatus();
            }
        }
        return ProjectionPower.evaluate(settings, coreProfile(), chassisProfile(), hasProjectedSourceContent(), projectedSourceCount());
    }

    public int projectedSourceCount() {
        return ProjectionSourceRegistry.contentCount(settings.sourceMode(), this, settings);
    }

    public boolean hasProjectedSourceContent() {
        return ProjectionSourceRegistry.hasContent(settings.sourceMode(), this, settings);
    }

    public EntityProjectionState.ApplyResult applyEntityEquipment(
            VirtualEquipmentSnapshots.Channel channel,
            boolean replaceExisting
    ) {
        if (endResonanceLocksControls()) {
            return EntityProjectionState.ApplyResult.EMPTY_INCOMING;
        }
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
            setChangedAndSync();
        }
        return result;
    }

    public void clearProjectedEntityEquipment(VirtualEquipmentSnapshots.Channel channel) {
        if (endResonanceLocksControls()) {
            return;
        }
        entityProjectionState.clearProjected(channel);
        setChangedAndSync();
    }

    public boolean toggleProjectedEntityEquipmentVisibility(VirtualEquipmentSnapshots.Channel channel) {
        if (endResonanceLocksControls() || channel == null) {
            return false;
        }
        boolean visible = entityProjectionState.toggleEquipmentVisible(channel);
        setChangedAndSync();
        return visible;
    }

    public int captureEquippedHumanoidLoadout(Player player) {
        if (endResonanceLocksControls()) {
            return 0;
        }
        int captured = entityProjectionState.captureEquippedHumanoidLoadout(player);
        setChangedAndSync();
        return captured;
    }

    public HumanoidPosePreset cycleHumanoidPose() {
        if (endResonanceLocksControls()) {
            return entityProjectionState.humanoidPose();
        }
        HumanoidPosePreset pose = entityProjectionState.cycleHumanoidPose();
        setChangedAndSync();
        return pose;
    }

    public boolean togglePlayerAllLayers() {
        if (endResonanceLocksControls()) {
            return entityProjectionState.playerAllLayers();
        }
        boolean allLayers = entityProjectionState.togglePlayerAllLayers();
        setChangedAndSync();
        return allLayers;
    }

    public HorsePosePreset cycleHorsePose() {
        if (endResonanceLocksControls()) {
            return entityProjectionState.horsePose();
        }
        HorsePosePreset pose = entityProjectionState.cycleHorsePose();
        setChangedAndSync();
        return pose;
    }

    public GenericPosePreset cycleGenericPose() {
        if (endResonanceLocksControls()) {
            return entityProjectionState.genericPose();
        }
        GenericPosePreset pose = entityProjectionState.cycleGenericPose();
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
        if (endResonanceLocksControls()) {
            return;
        }
        settings = normalizeSettingsForChassis(newSettings);
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
            if (level != null) {
                entityProjectionState.importFromCard(card, level.registryAccess());
            }
        });
        setChangedAndSync();
    }

    private void handleCoreChanged() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (endResonanceActive()) {
            if (endResonanceRestoreSnapshot.isEmpty()) {
                endResonanceRestoreSnapshot = captureProjectionFacingState(level.registryAccess());
            }
            return;
        }
        if (!endResonanceRestoreSnapshot.isEmpty()) {
            restoreProjectionFacingState(level.registryAccess());
        }
    }

    private CompoundTag captureProjectionFacingState(HolderLookup.Provider registries) {
        CompoundTag snapshot = saveWithoutMetadata(registries);
        snapshot.remove("CoreItem");
        snapshot.remove("EndResonanceRestoreSnapshot");
        return snapshot;
    }

    private void restoreProjectionFacingState(HolderLookup.Provider registries) {
        if (endResonanceRestoreSnapshot.isEmpty()) {
            return;
        }
        CompoundTag merged = saveWithoutMetadata(registries);
        merged.remove("EndResonanceRestoreSnapshot");
        for (String key : endResonanceRestoreSnapshot.getAllKeys()) {
            if (endResonanceRestoreSnapshot.get(key) != null) {
                merged.put(key, endResonanceRestoreSnapshot.get(key).copy());
            }
        }
        // The physical catalyst belongs to the current slot state, not the suspended snapshot.
        merged.put("CoreItem", coreItem.serializeNBT(registries));

        loadingCoreState = true;
        endResonanceRestoreSnapshot = new CompoundTag();
        try {
            loadAdditional(merged, registries);
        } finally {
            loadingCoreState = false;
            endResonanceRestoreSnapshot = new CompoundTag();
        }
        setChangedAndSync();
    }

    /** State used for Creative clone/pick-block: never copies the unique special catalyst. */
    public CompoundTag stateForCreativeClone(HolderLookup.Provider registries) {
        CompoundTag state = endResonanceActive() && !endResonanceRestoreSnapshot.isEmpty()
                ? endResonanceRestoreSnapshot.copy()
                : saveWithoutMetadata(registries);
        state.remove("CoreItem");
        state.remove("EndResonanceRestoreSnapshot");
        return state;
    }

    public void preparePackedPlayerBreak(HolderLookup.Provider registries) {
        pendingPackedPlayerBreakDrop = ProjectorStateTransfer.packPlacedProjector(this, registries);
    }

    public boolean hasPendingPackedPlayerBreakDrop() {
        return pendingPackedPlayerBreakDrop != null && !pendingPackedPlayerBreakDrop.isEmpty();
    }

    public ItemStack copyPendingPackedPlayerBreakDrop() {
        return hasPendingPackedPlayerBreakDrop() ? pendingPackedPlayerBreakDrop.copy() : ItemStack.EMPTY;
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
        invalidateWallProjectionCache();
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public void writeMenuData(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(worldPosition);
        settings.write(buffer);
        buffer.writeBoolean(projectionEnabled);
        buffer.writeVarInt(chassisProfile().ordinal());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        settings.save(tag);
        tag.putBoolean("ProjectionEnabled", projectionEnabled);
        if (!projectionSourcePayloads.isEmpty()) {
            tag.put("ProjectionSourcePayloads", projectionSourcePayloads.copy());
        }
        tag.put("ImageSourceBank", imageSourceBank.save());
        if (supportsPresentationDeck()) {
            tag.putInt("PresentationSlideIndex", Math.max(0, wallSlideIndex()));
            tag.putBoolean("AutomaticPresentation", automaticPresentationEnabled);
            tag.putInt("AutomaticPresentationIntervalTicks", automaticPresentationIntervalTicks());
            tag.putInt("AutomaticPresentationElapsedTicks", Math.max(0, automaticPresentationElapsedTicks));
        }
        if (presentationLinkId != null) {
            tag.putUUID("PresentationLinkId", presentationLinkId);
        }
        if (hasDockedPresentationRemote()) {
            tag.put("PresentationRemote", presentationRemote.serializeNBT(registries));
        }
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
        if (!endResonanceRestoreSnapshot.isEmpty()) {
            tag.put("EndResonanceRestoreSnapshot", endResonanceRestoreSnapshot.copy());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        settings = normalizeSettingsForChassis(ProjectionSettings.load(tag));
        projectionEnabled = !tag.contains("ProjectionEnabled") || tag.getBoolean("ProjectionEnabled");
        projectionSourcePayloads = tag.contains("ProjectionSourcePayloads")
                ? tag.getCompound("ProjectionSourcePayloads").copy()
                : new CompoundTag();
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
        if (supportsPresentationDeck()) {
            if (!imageSourceBank.hasAny(ImageSourceBank.PERSISTED_COMPAT_SLOTS) && settings.hasImage()) {
                imageSourceBank.set(0, settings.imageId(), settings.imageWidth(), settings.imageHeight());
            }
            int savedSlideIndex = tag.contains("PresentationSlideIndex")
                    ? tag.getInt("PresentationSlideIndex")
                    : (tag.contains("WallSlideIndex") ? tag.getInt("WallSlideIndex") : 0);
            wallSlideIndex = imageSourceBank.normalizePresentIndex(savedSlideIndex);
            ImageSourceBank.Asset active = activeWallImageFromBank(settings);
            // The presentation deck owns image assets for both chassis, not the active source
            // for Table.  Forcing Image here runs on every client sync as well as world load,
            // which made a successfully activated Table Entity/Banner source immediately look
            // like Image again.  Wall still has its image-only normalization below.
            settings = settings.withImageLayoutMode(ProjectionSettings.ImageLayoutMode.SINGLE);
            if (chassisProfile() == ProjectionChassisProfile.WALL) {
                settings = settings.withSourceMode(ProjectionSettings.SourceMode.IMAGE)
                        .withBackFaceMode(ProjectionSettings.BackFaceMode.FRONT);
            }
            if (active.present() && settings.sourceMode().equals(ProjectionSettings.SourceMode.IMAGE)) {
                settings = settings.withImage(active.id(), active.width(), active.height());
            }
            automaticPresentationEnabled = tag.getBoolean("AutomaticPresentation");
            automaticPresentationIntervalTicks = Math.max(20, Math.min(2400,
                    tag.contains("AutomaticPresentationIntervalTicks") ? tag.getInt("AutomaticPresentationIntervalTicks") : 200));
            automaticPresentationElapsedTicks = Math.max(0, Math.min(automaticPresentationIntervalTicks,
                    tag.contains("AutomaticPresentationElapsedTicks") ? tag.getInt("AutomaticPresentationElapsedTicks") : 0));
        } else {
            wallSlideIndex = 0;
            automaticPresentationEnabled = false;
            automaticPresentationElapsedTicks = 0;
        }
        presentationLinkId = tag.hasUUID("PresentationLinkId") ? tag.getUUID("PresentationLinkId") : null;
        presentationRemote.setStackInSlot(0, ItemStack.EMPTY);
        if (tag.contains("PresentationRemote")) {
            presentationRemote.deserializeNBT(registries, tag.getCompound("PresentationRemote"));
            ItemStack remote = presentationRemote.getStackInSlot(0);
            if (!remote.isEmpty() && !remote.is(ModItems.PRESENTATION_REMOTE.get())) {
                presentationRemote.setStackInSlot(0, ItemStack.EMPTY);
            }
        }
        invalidateWallProjectionCache();
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

        endResonanceRestoreSnapshot = tag.contains("EndResonanceRestoreSnapshot")
                ? tag.getCompound("EndResonanceRestoreSnapshot").copy()
                : new CompoundTag();
        loadingCoreState = true;
        try {
            if (tag.contains("CoreItem")) {
                coreItem.deserializeNBT(registries, tag.getCompound("CoreItem"));
                ItemStack loadedCore = coreItem.getStackInSlot(0);
                // Preserve a special catalyst even if legacy/state-transfer data put it into an
                // incompatible chassis. It remains inactive but can still be extracted safely.
                if (!loadedCore.isEmpty()
                        && !ProjectionCoreProfile.isCoreItem(loadedCore)
                        && !SpecialResonanceProfile.fromStack(loadedCore).present()) {
                    coreItem.setStackInSlot(0, ItemStack.EMPTY);
                }
            } else {
                coreItem.setStackInSlot(0, ItemStack.EMPTY);
            }
        } finally {
            loadingCoreState = false;
        }
    }

    private ProjectionSettings normalizeSettingsForChassis(ProjectionSettings candidate) {
        ProjectionSettings safe = candidate == null ? ProjectionSettings.DEFAULT : candidate.sanitized();
        if (!ProjectionSourceRegistry.isCompatible(safe.sourceMode(), chassisProfile())) {
            safe = safe.withSourceMode(ProjectionSettings.SourceMode.IMAGE);
        }
        return safe;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && endResonanceActive() && endResonanceRestoreSnapshot.isEmpty()) {
            endResonanceRestoreSnapshot = captureProjectionFacingState(level.registryAccess());
            setChangedAndSync();
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
            case TABLE -> "container.mirage_projector.table";
            case WALL -> "container.mirage_projector.wall";
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
