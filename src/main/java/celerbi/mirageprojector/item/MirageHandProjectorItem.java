package celerbi.mirageprojector.item;

import celerbi.mirageprojector.ImageSourceBank;
import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectionAssetRules;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.ProjectionSourceRegistry;
import celerbi.mirageprojector.block.MirageProjectorBlock;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.network.PortableProjectorStatePayload;
import celerbi.mirageprojector.menu.PortableDeviceMenu;
import celerbi.mirageprojector.menu.PortableDeviceSource;
import celerbi.mirageprojector.registry.ModBlocks;
import celerbi.mirageprojector.registry.ModItems;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/** Portable multi-source hologram projector with an internal Core and rechargeable cell. */
public final class MirageHandProjectorItem extends Item implements ShoulderRechargeableDevice {
    private static final String DEVICE_ID_TAG = "MirageHandProjectorId";
    private static final String PROFILE_TAG = "MirageHandProjectorState";
    private static final String PROFILE_PRESENT_TAG = "MirageHandProjectorProfilePresent";
    private static final String ACTIVE_TAG = "MirageHandProjectorActive";
    private static final String SOURCE_MODE_TAG = "MirageHandProjectorSource";
    private static final String IMAGE_PRESENTATION_TAG = "MirageHandProjectorImagePresentation";
    private static final String PROJECTION_DISTANCE_TAG = "MirageHandProjectorDistancePixels";
    private static final String SOURCE_COUNT_TAG = "MirageHandProjectorSourceCount";
    private static final String CELL_TAG = "MirageHandProjectorCell";
    private static final String CELL_PRESENT_TAG = "MirageHandProjectorCellPresent";
    private static final String CELL_PERCENT_TAG = "MirageHandProjectorCellPercent";
    private static final String CORE_TAG = "MirageHandProjectorCore";
    private static final String CORE_PRESENT_TAG = "MirageHandProjectorCorePresent";
    private static final String BANNER_PRESENTATION_TAG = "MirageHandProjectorBannerPresentation";
    private static final String WAR_BANNER_FACING_TAG = "MirageHandProjectorWarBannerFacing";
    private static final String WAR_BANNER_SIZE_TAG = "MirageHandProjectorWarBannerSize";
    private static final String WAR_BANNER_HEIGHT_TAG = "MirageHandProjectorWarBannerHeight";

    public static final int WAR_BANNER_DEFAULT_SIZE_PERCENT = 100;
    public static final int WAR_BANNER_MIN_SIZE_PERCENT = 0;
    public static final int WAR_BANNER_MAX_SIZE_PERCENT = 100;
    public static final int WAR_BANNER_SIZE_STEP_PERCENT = 5;
    public static final int WAR_BANNER_DEFAULT_HEIGHT_PIXELS = 0;
    public static final int WAR_BANNER_MIN_HEIGHT_PIXELS = -32;
    public static final int WAR_BANNER_MAX_HEIGHT_PIXELS = 32;
    public static final int WAR_BANNER_HEIGHT_STEP_PIXELS = 4;

    public static final int PORTABLE_MIN_SCALE_PIXELS = 10;
    public static final int PORTABLE_MAX_SCALE_PIXELS = 50;
    public static final int PORTABLE_DEFAULT_DISTANCE_PIXELS = 30;
    public static final int PORTABLE_MIN_DISTANCE_PIXELS = 16;
    public static final int PORTABLE_MAX_DISTANCE_PIXELS = 80;
    private static final int PORTABLE_MAX_LIFT_PIXELS = 12;
    private static final int PORTABLE_DEFAULT_LIFT_PIXELS = 6;
    private static final int PORTABLE_MAX_FLOAT_PIXELS = 4;
    private static final int PORTABLE_MAX_OPACITY_PERCENT = 100;
    private static final int MIN_DRAIN_PER_SECOND = 4;

    public MirageHandProjectorItem(Properties properties) {
        super(properties);
    }

    public static UUID deviceId(ItemStack projector) {
        CompoundTag tag = customTag(projector);
        return tag.hasUUID(DEVICE_ID_TAG) ? tag.getUUID(DEVICE_ID_TAG) : new UUID(0L, 0L);
    }

    public static UUID ensureDeviceId(ItemStack projector) {
        if (projector == null || projector.isEmpty()) {
            return new UUID(0L, 0L);
        }
        UUID existing = deviceId(projector);
        if (existing.getMostSignificantBits() != 0L || existing.getLeastSignificantBits() != 0L) {
            return existing;
        }
        UUID created = UUID.randomUUID();
        CustomData.update(DataComponents.CUSTOM_DATA, projector, tag -> tag.putUUID(DEVICE_ID_TAG, created));
        return created;
    }

    public static CompoundTag synchronizedCustomData(ItemStack projector) {
        return customTag(projector);
    }

    public static boolean hasProjectionProfile(ItemStack projector) {
        return customTag(projector).getBoolean(PROFILE_PRESENT_TAG);
    }

    public static boolean projectionEnabled(ItemStack projector) {
        return customTag(projector).getBoolean(ACTIVE_TAG);
    }

    public static void setProjectionEnabled(ItemStack projector, boolean enabled) {
        if (projector == null || projector.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, projector, tag -> {
            if (enabled) {
                tag.putBoolean(ACTIVE_TAG, true);
            } else {
                tag.remove(ACTIVE_TAG);
            }
        });
        removeEmptyCustomData(projector);
    }

    public static ProjectionSettings.SourceMode sourceMode(ItemStack projector) {
        ProjectionSettings.SourceMode mode = ProjectionSettings.SourceMode.parseOrDefault(
                customTag(projector).getString(SOURCE_MODE_TAG),
                ProjectionSettings.SourceMode.IMAGE
        );
        return mode == ProjectionSettings.SourceMode.ITEM ? ProjectionSettings.SourceMode.IMAGE : mode;
    }

    public static ImagePresentation imagePresentation(ItemStack projector) {
        return ImagePresentation.parse(customTag(projector).getString(IMAGE_PRESENTATION_TAG));
    }

    public static void cycleImagePresentation(ItemStack projector) {
        if (projector == null || projector.isEmpty()) return;
        ImagePresentation next = imagePresentation(projector).next();
        CustomData.update(DataComponents.CUSTOM_DATA, projector, tag -> {
            if (next == ImagePresentation.NORMAL) tag.remove(IMAGE_PRESENTATION_TAG);
            else tag.putString(IMAGE_PRESENTATION_TAG, next.serializedName());
        });
        removeEmptyCustomData(projector);
    }

    /** Enables a modest static lift for a portable image; it is intentionally not a floating animation. */
    public static boolean toggleImageLift(ItemStack projector, Level level) {
        return updatePortableImageSettings(projector, level, settings -> settings.withLiftPixels(
                settings.liftPixels() > 0 ? 0 : PORTABLE_DEFAULT_LIFT_PIXELS
        ));
    }

    /** Portable images may spin, but never inherit the fixed projector's floating effect. */
    public static boolean toggleImageRotation(ItemStack projector, Level level) {
        return updatePortableImageSettings(projector, level,
                settings -> settings.withRotationEnabled(!settings.rotationEnabled()));
    }

    /** Sets the static facing angle of a portable entity projection. */
    public static boolean setPortableEntityRotation(ItemStack projector, int degrees, Level level) {
        return updatePortableEntitySettings(projector, level, settings -> settings.withPresentation(
                settings.scalePixels(), settings.liftPixels(), settings.rotationEnabled(),
                settings.rotationPeriodTicks(), settings.clockwise(), Mth.clamp(degrees, 0, 360),
                settings.floatingEnabled(), settings.floatMode(), settings.floatAmplitudePixels(),
                settings.floatCycleTicks(), settings.floatIntervalDegrees(), settings.fullbright(),
                settings.opacityPercent(), settings.tintRgb(), settings.debugChassisOverride()
        ));
    }

    /** Enables or disables the entity's automatic spin without changing its selected facing. */
    public static boolean togglePortableEntityRotation(ItemStack projector, Level level) {
        return updatePortableEntitySettings(projector, level,
                settings -> settings.withRotationEnabled(!settings.rotationEnabled()));
    }

    /** Cycles precisely the three planar face modes offered by the Hand Projector. */
    public static boolean cycleImageFaceMode(ItemStack projector, Level level) {
        return updatePortableImageSettings(projector, level, settings -> {
            ProjectionSettings.BackFaceMode next = switch (settings.backFaceMode()) {
                case FRONT -> ProjectionSettings.BackFaceMode.MIRRORED;
                case MIRRORED -> ProjectionSettings.BackFaceMode.READABLE;
                default -> ProjectionSettings.BackFaceMode.FRONT;
            };
            return settings.withBackFaceMode(next);
        });
    }

    public static int projectionDistancePixels(ItemStack projector) {
        return Mth.clamp(customTag(projector).contains(PROJECTION_DISTANCE_TAG)
                        ? customTag(projector).getInt(PROJECTION_DISTANCE_TAG)
                        : PORTABLE_DEFAULT_DISTANCE_PIXELS,
                PORTABLE_MIN_DISTANCE_PIXELS, PORTABLE_MAX_DISTANCE_PIXELS);
    }

    public static void setProjectionDistancePixels(ItemStack projector, int distancePixels) {
        if (projector == null || projector.isEmpty()) return;
        int safe = Mth.clamp(distancePixels, PORTABLE_MIN_DISTANCE_PIXELS, PORTABLE_MAX_DISTANCE_PIXELS);
        CustomData.update(DataComponents.CUSTOM_DATA, projector, tag -> {
            if (safe == PORTABLE_DEFAULT_DISTANCE_PIXELS) tag.remove(PROJECTION_DISTANCE_TAG);
            else tag.putInt(PROJECTION_DISTANCE_TAG, safe);
        });
        removeEmptyCustomData(projector);
    }

    public static int projectedSourceCount(ItemStack projector) {
        return Math.max(0, customTag(projector).getInt(SOURCE_COUNT_TAG));
    }

    /** Changes the active workspace without deleting stored sources. */
    public static boolean selectSourceMode(ItemStack projector, ProjectionSettings.SourceMode sourceMode, Level level) {
        if (projector == null || projector.isEmpty() || sourceMode == null || level == null
                || !ProjectionSourceRegistry.isCompatible(sourceMode, ProjectionChassisProfile.COMPACT)) {
            return false;
        }
        MirageProjectorBlockEntity portable = loadOrCreatePortableProjector(projector, level);
        if (portable == null || !portable.activateProjectionSource(sourceMode)) {
            return false;
        }
        commitPortableProfile(projector, portable, sourceMode, level.registryAccess());
        return true;
    }

    /** Snapshot shown in the Hand Projector's virtual source well. */
    public static ItemStack sourceSnapshot(ItemStack projector, Level level) {
        if (projector == null || projector.isEmpty() || level == null || !hasProjectionProfile(projector)) {
            return ItemStack.EMPTY;
        }
        MirageProjectorBlockEntity portable = createPortableProjector(projector, level, BlockPos.ZERO, Direction.SOUTH);
        if (portable == null) {
            return ItemStack.EMPTY;
        }
        ProjectionSettings.SourceMode sourceMode = sourceMode(projector);
        if (sourceMode == ProjectionSettings.SourceMode.ITEM) {
            return portable.projectedStack().copy();
        }
        if (sourceMode == ProjectionSettings.SourceMode.ENTITY) {
            return portable.stagedEntityCard().copy();
        }
        if (sourceMode == ProjectionSettings.SourceMode.BANNER) {
            return portable.bannerSnapshot(0).copy();
        }
        return ItemStack.EMPTY;
    }

    /** Copies a source into the active workspace without consuming it. */
    public static boolean captureSourceSnapshot(ItemStack projector, ItemStack source, Level level) {
        if (projector == null || projector.isEmpty() || source == null || source.isEmpty() || level == null) {
            return false;
        }
        ProjectionSettings.SourceMode sourceMode = sourceMode(projector);
        if (sourceMode == ProjectionSettings.SourceMode.IMAGE) {
            return false;
        }
        MirageProjectorBlockEntity portable = loadOrCreatePortableProjector(projector, level);
        if (portable == null) {
            return false;
        }

        boolean accepted;
        if (sourceMode == ProjectionSettings.SourceMode.ITEM) {
            portable.captureProjectionSnapshot(source);
            accepted = true;
        } else if (sourceMode == ProjectionSettings.SourceMode.ENTITY) {
            if (!source.is(ModItems.ENTITY_SCAN_CARD.get()) || !EntityScanData.hasScan(source)) {
                return false;
            }
            portable.entityScanCard().setStackInSlot(0, source.copyWithCount(1));
            accepted = portable.entityProjectionState().hasProjectedEntityContent();
        } else if (sourceMode == ProjectionSettings.SourceMode.BANNER) {
            if (!(source.getItem() instanceof BannerItem)) {
                return false;
            }
            accepted = portable.captureBannerSnapshot(0, source);
        } else {
            accepted = false;
        }

        if (!accepted) {
            return false;
        }
        portable.activateProjectionSource(sourceMode);
        commitPortableProfile(projector, portable, sourceMode, level.registryAccess());
        return true;
    }

    public static boolean clearSourceSnapshot(ItemStack projector, Level level) {
        if (projector == null || projector.isEmpty() || level == null || !hasProjectionProfile(projector)) {
            return false;
        }
        ProjectionSettings.SourceMode sourceMode = sourceMode(projector);
        MirageProjectorBlockEntity portable = createPortableProjector(projector, level, BlockPos.ZERO, Direction.SOUTH);
        if (portable == null) {
            return false;
        }
        if (sourceMode == ProjectionSettings.SourceMode.ITEM) {
            portable.clearProjectionSnapshot();
        } else if (sourceMode == ProjectionSettings.SourceMode.ENTITY) {
            portable.entityScanCard().setStackInSlot(0, ItemStack.EMPTY);
        } else if (sourceMode == ProjectionSettings.SourceMode.BANNER) {
            portable.clearAllBannerSnapshots();
        } else {
            return false;
        }
        commitPortableProfile(projector, portable, sourceMode, level.registryAccess());
        return true;
    }

    public static boolean hasProjectedContent(ItemStack projector) {
        return hasProjectionProfile(projector) && projectedSourceCount(projector) > 0;
    }

    public static ItemStack energyCell(ItemStack projector, HolderLookup.Provider registries) {
        if (projector == null || projector.isEmpty() || registries == null) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = customTag(projector);
        if (!tag.contains(CELL_TAG)) {
            return ItemStack.EMPTY;
        }
        ItemStack stored = ItemStack.parseOptional(registries, tag.getCompound(CELL_TAG));
        return RechargeableEnergyItem.isRechargeable(stored) ? stored.copyWithCount(1) : ItemStack.EMPTY;
    }

    public static boolean hasEnergyCell(ItemStack projector) {
        return customTag(projector).getBoolean(CELL_PRESENT_TAG);
    }

    public static int energyPercent(ItemStack projector) {
        return Mth.clamp(customTag(projector).getInt(CELL_PERCENT_TAG), 0, 100);
    }

    public static ItemStack coreStack(ItemStack projector, HolderLookup.Provider registries) {
        if (projector == null || projector.isEmpty() || registries == null) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = customTag(projector);
        if (!tag.contains(CORE_TAG)) {
            return ItemStack.EMPTY;
        }
        ItemStack stored = ItemStack.parseOptional(registries, tag.getCompound(CORE_TAG));
        return ProjectionCoreProfile.isCoreItem(stored) ? stored.copyWithCount(1) : ItemStack.EMPTY;
    }

    public static boolean hasCore(ItemStack projector) {
        return customTag(projector).getBoolean(CORE_PRESENT_TAG);
    }

    public static ProjectionCoreProfile coreProfile(ItemStack projector, HolderLookup.Provider registries) {
        return ProjectionCoreProfile.fromStack(coreStack(projector, registries));
    }

    /** GUI/container-only physical Projection Core replacement path. */
    public static void replaceCore(ItemStack projector, ItemStack core, HolderLookup.Provider registries) {
        if (core == null || core.isEmpty()) {
            clearCore(projector);
            setProjectionEnabled(projector, false);
            return;
        }
        if (!ProjectionCoreProfile.isCoreItem(core) || registries == null) {
            return;
        }
        ItemStack stored = core.copyWithCount(1);
        CustomData.update(DataComponents.CUSTOM_DATA, projector, tag -> {
            tag.put(CORE_TAG, stored.save(registries));
            tag.putBoolean(CORE_PRESENT_TAG, true);
        });
    }

    private static void clearCore(ItemStack projector) {
        if (projector == null || projector.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, projector, tag -> {
            tag.remove(CORE_TAG);
            tag.remove(CORE_PRESENT_TAG);
        });
        removeEmptyCustomData(projector);
    }

    public static boolean emittingProjection(ItemStack projector) {
        return projector != null
                && !projector.isEmpty()
                && projectionEnabled(projector)
                && hasProjectedContent(projector)
                && hasCore(projector)
                && hasEnergyCell(projector)
                && energyPercent(projector) > 0;
    }

    public static int portableScalePixels(ItemStack projector, Level level) {
        if (level == null) {
            return Mth.clamp(ProjectionSettings.DEFAULT.scalePixels(), PORTABLE_MIN_SCALE_PIXELS, PORTABLE_MAX_SCALE_PIXELS);
        }
        MirageProjectorBlockEntity portable = loadOrCreatePortableProjector(projector, level);
        return portable == null
                ? PORTABLE_MIN_SCALE_PIXELS
                : Mth.clamp(portable.settings().scalePixels(), PORTABLE_MIN_SCALE_PIXELS,
                portableMaxScalePixels(projector, level));
    }

    public static int portableMaxScalePixels(ItemStack projector, Level level) {
        if (level == null) return PORTABLE_MIN_SCALE_PIXELS;
        return portableMaxScalePixels(coreProfile(projector, level.registryAccess()));
    }

    private static int portableMaxScalePixels(ProjectionCoreProfile core) {
        if (core == ProjectionCoreProfile.IMPROVED_NETHERITE) {
            return PORTABLE_MAX_SCALE_PIXELS;
        }
        float output = core == null ? 0.0F : core.materialOutput();
        // The compact chassis otherwise retains its existing 10–30 px progression.
        return Mth.clamp(Math.round(output / 6.4F), PORTABLE_MIN_SCALE_PIXELS, 30);
    }

    public static boolean setPortableScale(ItemStack projector, int scalePixels, Level level) {
        if (projector == null || projector.isEmpty() || level == null) {
            return false;
        }
        MirageProjectorBlockEntity portable = loadOrCreatePortableProjector(projector, level);
        if (portable == null) {
            return false;
        }
        int clamped = Mth.clamp(scalePixels, PORTABLE_MIN_SCALE_PIXELS, portableMaxScalePixels(projector, level));
        portable.applySettings(portable.settings().withScalePixels(clamped));
        commitPortableProfile(projector, portable, sourceMode(projector), level.registryAccess());
        return true;
    }

    public static boolean setImageSource(ItemStack projector, String assetId, int width, int height, Level level) {
        if (projector == null || projector.isEmpty() || level == null
                || !ProjectionAssetRules.isValidAssetId(assetId) || width <= 0 || height <= 0) {
            return false;
        }
        MirageProjectorBlockEntity portable = loadOrCreatePortableProjector(projector, level);
        if (portable == null) {
            return false;
        }
        portable.imageSourceBank().set(0, assetId, width, height);
        portable.applySettings(portable.settings()
                .withSourceMode(ProjectionSettings.SourceMode.IMAGE)
                .withImageLayoutMode(ProjectionSettings.ImageLayoutMode.SINGLE)
                .withImage(assetId, width, height));
        portable.activateProjectionSource(ProjectionSettings.SourceMode.IMAGE);
        commitPortableProfile(projector, portable, ProjectionSettings.SourceMode.IMAGE, level.registryAccess());
        return true;
    }

    public static BannerPresentation bannerPresentation(ItemStack projector) {
        CompoundTag tag = customTag(projector);
        return BannerPresentation.parse(tag.getString(BANNER_PRESENTATION_TAG));
    }

    public static WarBannerFacing warBannerFacing(ItemStack projector) {
        CompoundTag tag = customTag(projector);
        return WarBannerFacing.parse(tag.getString(WAR_BANNER_FACING_TAG));
    }

    public static int warBannerSizePercent(ItemStack projector) {
        CompoundTag tag = customTag(projector);
        int value = tag.contains(WAR_BANNER_SIZE_TAG)
                ? tag.getInt(WAR_BANNER_SIZE_TAG)
                : WAR_BANNER_DEFAULT_SIZE_PERCENT;
        return Mth.clamp(value, WAR_BANNER_MIN_SIZE_PERCENT, WAR_BANNER_MAX_SIZE_PERCENT);
    }

    public static int warBannerHeightPixels(ItemStack projector) {
        CompoundTag tag = customTag(projector);
        int value = tag.contains(WAR_BANNER_HEIGHT_TAG)
                ? tag.getInt(WAR_BANNER_HEIGHT_TAG)
                : WAR_BANNER_DEFAULT_HEIGHT_PIXELS;
        return Mth.clamp(value, WAR_BANNER_MIN_HEIGHT_PIXELS, WAR_BANNER_MAX_HEIGHT_PIXELS);
    }

    public static boolean warBannerActive(ItemStack projector) {
        return sourceMode(projector) == ProjectionSettings.SourceMode.BANNER
                && bannerPresentation(projector) == BannerPresentation.WAR_BANNER;
    }

    public static void cycleBannerPresentation(ItemStack projector) {
        if (projector == null || projector.isEmpty()
                || sourceMode(projector) != ProjectionSettings.SourceMode.BANNER) {
            return;
        }
        setBannerPresentation(projector, bannerPresentation(projector).next());
    }

    public static void cycleWarBannerFacing(ItemStack projector) {
        if (projector == null || projector.isEmpty() || !warBannerActive(projector)) {
            return;
        }
        setWarBannerFacing(projector, warBannerFacing(projector).next());
    }

    public static void adjustWarBannerSize(ItemStack projector, int deltaPercent) {
        setWarBannerSize(projector, warBannerSizePercent(projector) + deltaPercent);
    }

    public static void adjustWarBannerHeight(ItemStack projector, int deltaPixels) {
        setWarBannerHeight(projector, warBannerHeightPixels(projector) + deltaPixels);
    }

    public static void setWarBannerSize(ItemStack projector, int percent) {
        if (projector == null || projector.isEmpty() || !warBannerActive(projector)) {
            return;
        }
        int value = Mth.clamp(
                percent, WAR_BANNER_MIN_SIZE_PERCENT, WAR_BANNER_MAX_SIZE_PERCENT
        );
        CustomData.update(DataComponents.CUSTOM_DATA, projector, tag -> tag.putInt(WAR_BANNER_SIZE_TAG, value));
    }

    public static void setWarBannerHeight(ItemStack projector, int pixels) {
        if (projector == null || projector.isEmpty() || !warBannerActive(projector)) {
            return;
        }
        int value = Mth.clamp(pixels, WAR_BANNER_MIN_HEIGHT_PIXELS, WAR_BANNER_MAX_HEIGHT_PIXELS);
        CustomData.update(DataComponents.CUSTOM_DATA, projector, tag -> tag.putInt(WAR_BANNER_HEIGHT_TAG, value));
    }

    public static void publishState(Player player, ItemStack projector) {
        syncState(player, projector);
    }

    private static void setBannerPresentation(ItemStack projector, BannerPresentation presentation) {
        CustomData.update(DataComponents.CUSTOM_DATA, projector, tag -> {
            if (presentation == BannerPresentation.FORWARD) {
                tag.remove(BANNER_PRESENTATION_TAG);
            } else {
                tag.putString(BANNER_PRESENTATION_TAG, presentation.serializedName());
            }
        });
        removeEmptyCustomData(projector);
    }

    private static void setWarBannerFacing(ItemStack projector, WarBannerFacing facing) {
        CustomData.update(DataComponents.CUSTOM_DATA, projector, tag -> {
            if (facing == WarBannerFacing.BILLBOARD) {
                tag.remove(WAR_BANNER_FACING_TAG);
            } else {
                tag.putString(WAR_BANNER_FACING_TAG, facing.serializedName());
            }
        });
        removeEmptyCustomData(projector);
    }

    @Nullable
    public static MirageProjectorBlockEntity createPortableProjector(
            ItemStack stack,
            Level level,
            BlockPos pos,
            Direction facing
    ) {
        if (!hasProjectionProfile(stack)) {
            return null;
        }
        CompoundTag profile = customTag(stack).getCompound(PROFILE_TAG);
        if (profile.isEmpty()) {
            return null;
        }

        MirageProjectorBlockEntity projector = new MirageProjectorBlockEntity(
                pos,
                portableBlockState(facing)
        );
        projector.setLevel(level);
        projector.loadCustomOnly(profile.copy(), level.registryAccess());
        projector.coreItem().setStackInSlot(0, coreStack(stack, level.registryAccess()));
        normalizePortableProjector(projector);
        projector.setProjectionEnabled(projectionEnabled(stack));
        return projector;
    }

    public static Component hudComponent(ItemStack projector) {
        if (!hasProjectionProfile(projector)) {
            return Component.translatable("hud.mirage_projector.hand_projector.no_profile");
        }
        Component sourceName = sourceDisplayName(sourceMode(projector));
        if (!hasProjectedContent(projector)) {
            return Component.translatable(
                    "hud.mirage_projector.hand_projector.no_source",
                    sourceName
            );
        }
        if (!hasCore(projector)) {
            return Component.translatable(
                    "hud.mirage_projector.hand_projector.no_core",
                    sourceName
            );
        }
        if (!hasEnergyCell(projector) || energyPercent(projector) <= 0) {
            return Component.translatable(
                    "hud.mirage_projector.hand_projector.discharged",
                    sourceName
            );
        }
        return Component.translatable(
                "hud.mirage_projector.hand_projector.status",
                projectionEnabled(projector)
                        ? Component.translatable("hud.mirage_projector.hand_projector.state_active")
                        : Component.translatable("hud.mirage_projector.hand_projector.state_standby"),
                sourceName,
                energyPercent(projector)
        );
    }

    @Nullable
    private static MirageProjectorBlockEntity loadOrCreatePortableProjector(ItemStack projector, Level level) {
        MirageProjectorBlockEntity portable = createPortableProjector(projector, level, BlockPos.ZERO, Direction.SOUTH);
        if (portable != null) {
            return portable;
        }
        portable = new MirageProjectorBlockEntity(BlockPos.ZERO, portableBlockState(Direction.SOUTH));
        portable.setLevel(level);
        portable.applySettings(ProjectionSettings.DEFAULT.withSourceMode(sourceMode(projector)));
        portable.coreItem().setStackInSlot(0, coreStack(projector, level.registryAccess()));
        normalizePortableProjector(portable);
        return portable;
    }

    private static void commitPortableProfile(
            ItemStack handProjector,
            MirageProjectorBlockEntity portable,
            ProjectionSettings.SourceMode sourceMode,
            HolderLookup.Provider registries
    ) {
        normalizePortableProjector(portable);
        portable.applySettings(portable.settings().withSourceMode(sourceMode));
        int sourceCount = portable.projectedSourceCount();
        ItemStack physicalCore = portable.coreItem().getStackInSlot(0).copy();
        portable.coreItem().setStackInSlot(0, ItemStack.EMPTY);
        CompoundTag portableState = portable.saveCustomOnly(registries);
        portableState.remove("CoreItem");
        portable.coreItem().setStackInSlot(0, physicalCore);
        CustomData.update(DataComponents.CUSTOM_DATA, handProjector, tag -> {
            tag.put(PROFILE_TAG, portableState);
            tag.putBoolean(PROFILE_PRESENT_TAG, true);
            tag.putString(SOURCE_MODE_TAG, sourceMode.serializedName());
            tag.putInt(SOURCE_COUNT_TAG, sourceCount);
            if (sourceCount <= 0) {
                tag.remove(ACTIVE_TAG);
            }
        });
        removeEmptyCustomData(handProjector);
    }

    private static boolean updatePortableImageSettings(
            ItemStack projector,
            Level level,
            java.util.function.UnaryOperator<ProjectionSettings> update
    ) {
        if (projector == null || projector.isEmpty() || level == null
                || sourceMode(projector) != ProjectionSettings.SourceMode.IMAGE) {
            return false;
        }
        MirageProjectorBlockEntity portable = loadOrCreatePortableProjector(projector, level);
        if (portable == null) return false;
        portable.applySettings(update.apply(portable.settings()).sanitized());
        commitPortableProfile(projector, portable, ProjectionSettings.SourceMode.IMAGE, level.registryAccess());
        return true;
    }

    private static boolean updatePortableEntitySettings(
            ItemStack projector,
            Level level,
            java.util.function.UnaryOperator<ProjectionSettings> update
    ) {
        if (projector == null || projector.isEmpty() || level == null
                || sourceMode(projector) != ProjectionSettings.SourceMode.ENTITY) {
            return false;
        }
        MirageProjectorBlockEntity portable = loadOrCreatePortableProjector(projector, level);
        if (portable == null) return false;
        portable.applySettings(update.apply(portable.settings()).sanitized());
        commitPortableProfile(projector, portable, ProjectionSettings.SourceMode.ENTITY, level.registryAccess());
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack projector = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(projector, true);
        }

        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                PortableDeviceMenu.open(
                        serverPlayer,
                        hand == InteractionHand.MAIN_HAND ? PortableDeviceSource.MAIN_HAND : PortableDeviceSource.OFF_HAND
                );
            }
            return InteractionResultHolder.sidedSuccess(projector, false);
        }

        if (!hasProjectionProfile(projector)) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.hand_projector.no_profile"
            ), true);
            return InteractionResultHolder.sidedSuccess(projector, false);
        }
        if (!hasProjectedContent(projector)) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.hand_projector.no_source"
            ), true);
            return InteractionResultHolder.sidedSuccess(projector, false);
        }
        if (!hasCore(projector)) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.hand_projector.no_core"
            ), true);
            return InteractionResultHolder.sidedSuccess(projector, false);
        }
        if (!hasEnergyCell(projector) || energyPercent(projector) <= 0) {
            player.displayClientMessage(hudComponent(projector), true);
            return InteractionResultHolder.sidedSuccess(projector, false);
        }
        if (!portablePowerAvailable(projector, level.registryAccess())) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.hand_projector.overloaded"
            ), true);
            return InteractionResultHolder.sidedSuccess(projector, false);
        }

        boolean enabled = !projectionEnabled(projector);
        ensureDeviceId(projector);
        setProjectionEnabled(projector, enabled);
        syncState(player, projector);
        player.displayClientMessage(hudComponent(projector), true);
        return InteractionResultHolder.sidedSuccess(projector, false);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide && player instanceof ServerPlayer serverPlayer) {
            PortableDeviceMenu.open(
                    serverPlayer,
                    context.getHand() == InteractionHand.MAIN_HAND ? PortableDeviceSource.MAIN_HAND : PortableDeviceSource.OFF_HAND
            );
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        serverTickPortable(stack, level, player);
    }

    @Override
    public void serverTickShoulder(ItemStack stack, ServerPlayer player) {
        serverTickPortable(stack, player.level(), player);
    }

    @Override
    public ItemStack shoulderEnergyCell(ItemStack device, HolderLookup.Provider registries) {
        return energyCell(device, registries);
    }

    @Override
    public boolean shoulderInsertEnergyCell(
            ItemStack device,
            ItemStack source,
            HolderLookup.Provider registries
    ) {
        return insertEnergyCell(device, source, registries);
    }

    @Override
    public ItemStack shoulderExtractEnergyCell(ItemStack device, HolderLookup.Provider registries) {
        return extractEnergyCell(device, registries);
    }

    private static void serverTickPortable(ItemStack stack, Level level, Player player) {
        ensureDeviceId(stack);
        if (level.getGameTime() % 20L != 0L) {
            return;
        }

        if (projectionEnabled(stack)) {
            syncState(player, stack);
        }
        if (!projectionEnabled(stack)) {
            return;
        }
        if (!hasProjectedContent(stack) || !hasCore(stack) || !hasEnergyCell(stack) || energyPercent(stack) <= 0) {
            setProjectionEnabled(stack, false);
            syncState(player, stack);
            return;
        }

        ItemStack cell = energyCell(stack, level.registryAccess());
        RechargeableEnergyItem energy = RechargeableEnergyItem.fromStack(cell);
        if (energy == null) {
            clearEnergyCell(stack);
            return;
        }

        int before = energy.storedCharge(cell);
        if (before <= 0) {
            writeEnergyCell(stack, cell, level.registryAccess());
            return;
        }

        int drain = portableDrainPerSecond(stack, level.registryAccess());
        if (drain <= 0) {
            player.displayClientMessage(Component.translatable(
                    "message.mirage_projector.hand_projector.overloaded"
            ), true);
            setProjectionEnabled(stack, false);
            syncState(player, stack);
            return;
        }

        energy.consumeStoredCharge(cell, drain);
        writeEnergyCell(stack, cell, level.registryAccess());
        syncState(player, stack);
        if (before > 0 && energy.storedCharge(cell) <= 0) {
            player.displayClientMessage(hudComponent(stack), true);
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        tooltipComponents.add(Component.translatable(
                "tooltip.mirage_projector.hand_projector.use"
        ).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return hasEnergyCell(stack) && energyPercent(stack) < 100;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * energyPercent(stack) / 100.0F);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float fraction = energyPercent(stack) / 100.0F;
        int red = Math.round(Mth.lerp(fraction, 82.0F, 190.0F));
        int green = Math.round(Mth.lerp(fraction, 52.0F, 122.0F));
        int blue = Math.round(Mth.lerp(fraction, 114.0F, 255.0F));
        return (red << 16) | (green << 8) | blue;
    }

    private static boolean insertEnergyCell(
            ItemStack projector,
            ItemStack source,
            HolderLookup.Provider registries
    ) {
        if (projector == null || projector.isEmpty() || hasEnergyCell(projector)
                || !RechargeableEnergyItem.isRechargeable(source) || registries == null) {
            return false;
        }
        writeEnergyCell(projector, RechargeableEnergyItem.normalizeForDevice(source), registries);
        return true;
    }

    private static ItemStack extractEnergyCell(ItemStack projector, HolderLookup.Provider registries) {
        ItemStack stored = energyCell(projector, registries);
        if (stored.isEmpty()) {
            clearEnergyCell(projector);
            return ItemStack.EMPTY;
        }
        clearEnergyCell(projector);
        return stored;
    }

    /** GUI/container-only battery replacement path. */
    public static void replaceEnergyCell(ItemStack projector, ItemStack cell, HolderLookup.Provider registries) {
        if (cell == null || cell.isEmpty()) {
            clearEnergyCell(projector);
        } else if (RechargeableEnergyItem.isRechargeable(cell)) {
            writeEnergyCell(projector, RechargeableEnergyItem.normalizeForDevice(cell), registries);
        }
    }

    private static void writeEnergyCell(
            ItemStack projector,
            ItemStack cell,
            HolderLookup.Provider registries
    ) {
        if (cell == null || cell.isEmpty() || !RechargeableEnergyItem.isRechargeable(cell)) {
            clearEnergyCell(projector);
            return;
        }
        ItemStack stored = cell.copyWithCount(1);
        int percent = RechargeableEnergyItem.chargePercent(stored);
        CustomData.update(DataComponents.CUSTOM_DATA, projector, tag -> {
            tag.put(CELL_TAG, stored.save(registries));
            tag.putBoolean(CELL_PRESENT_TAG, true);
            tag.putInt(CELL_PERCENT_TAG, percent);
        });
    }

    private static void clearEnergyCell(ItemStack projector) {
        if (projector == null || projector.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, projector, tag -> {
            tag.remove(CELL_TAG);
            tag.remove(CELL_PRESENT_TAG);
            tag.remove(CELL_PERCENT_TAG);
        });
        removeEmptyCustomData(projector);
    }

    public static boolean portablePowerAvailable(ItemStack projector, HolderLookup.Provider registries) {
        if (!hasCore(projector) || !hasEnergyCell(projector) || energyPercent(projector) <= 0) {
            return false;
        }
        MirageProjectorBlockEntity portable = createPortableProjectorForEvaluation(projector, registries);
        if (portable == null) {
            return false;
        }
        ProjectionPower.Status status = ProjectionPower.evaluate(
                portable.settings(),
                coreProfile(projector, registries),
                ProjectionChassisProfile.COMPACT,
                portable.hasProjectedSourceContent(),
                portable.projectedSourceCount()
        );
        return status.active();
    }

    private static int portableDrainPerSecond(ItemStack projector, HolderLookup.Provider registries) {
        if (!hasCore(projector) || !hasEnergyCell(projector) || energyPercent(projector) <= 0) {
            return 0;
        }
        MirageProjectorBlockEntity portable = createPortableProjectorForEvaluation(projector, registries);
        if (portable == null) {
            return 0;
        }
        ProjectionPower.Status status = ProjectionPower.evaluate(
                portable.settings(),
                coreProfile(projector, registries),
                ProjectionChassisProfile.COMPACT,
                portable.hasProjectedSourceContent(),
                portable.projectedSourceCount()
        );
        return status.active() ? Math.max(MIN_DRAIN_PER_SECOND, status.usedPower()) : 0;
    }

    @Nullable
    private static MirageProjectorBlockEntity createPortableProjectorForEvaluation(
            ItemStack projector,
            HolderLookup.Provider registries
    ) {
        if (!hasProjectionProfile(projector) || registries == null) {
            return null;
        }
        CompoundTag profile = customTag(projector).getCompound(PROFILE_TAG);
        if (profile.isEmpty()) {
            return null;
        }
        MirageProjectorBlockEntity portable = new MirageProjectorBlockEntity(
                BlockPos.ZERO,
                portableBlockState(Direction.SOUTH)
        );
        portable.loadCustomOnly(profile.copy(), registries);
        portable.coreItem().setStackInSlot(0, coreStack(projector, registries));
        normalizePortableProjector(portable);
        portable.setProjectionEnabled(projectionEnabled(projector));
        return portable;
    }

    private static void normalizePortableProjector(MirageProjectorBlockEntity projector) {
        ProjectionSettings settings = projector.settings().sanitized();

        if (settings.sourceMode() == ProjectionSettings.SourceMode.ITEM) {
            projector.clearProjectionSnapshot();
            settings = settings.withSourceMode(ProjectionSettings.SourceMode.IMAGE);
        }

        int scale = Mth.clamp(settings.scalePixels(), PORTABLE_MIN_SCALE_PIXELS,
                portableMaxScalePixels(projector.coreProfile()));
        int lift = Math.min(settings.liftPixels(), PORTABLE_MAX_LIFT_PIXELS);
        int floatAmplitude = Math.min(settings.floatAmplitudePixels(), Math.min(PORTABLE_MAX_FLOAT_PIXELS, lift));
        boolean imageSource = settings.sourceMode() == ProjectionSettings.SourceMode.IMAGE;
        settings = settings.withPresentation(
                scale,
                lift,
                settings.rotationEnabled(),
                settings.rotationPeriodTicks(),
                settings.clockwise(),
                settings.rotationOffsetDegrees(),
                imageSource ? false : settings.floatingEnabled(),
                settings.floatMode(),
                imageSource ? 0 : floatAmplitude,
                settings.floatCycleTicks(),
                settings.floatIntervalDegrees(),
                settings.fullbright(),
                imageSource ? PORTABLE_MAX_OPACITY_PERCENT : Math.min(settings.opacityPercent(), PORTABLE_MAX_OPACITY_PERCENT),
                settings.tintRgb(),
                false
        ).sanitized();

        if (settings.sourceMode() == ProjectionSettings.SourceMode.IMAGE) {
            settings = settings.withImageLayoutMode(ProjectionSettings.ImageLayoutMode.SINGLE);
            if (!settings.hasImage()) {
                ImageSourceBank.Asset primary = projector.imageSourceBank().get(0);
                if (primary.present()) {
                    settings = settings.withImage(primary.id(), primary.width(), primary.height());
                }
            }
        }
        projector.applySettings(settings);

        if (settings.sourceMode() == ProjectionSettings.SourceMode.BANNER) {
            ItemStack selected = ItemStack.EMPTY;
            for (int face = 0; face < 4; face++) {
                ItemStack candidate = projector.bannerSnapshot(face);
                if (!candidate.isEmpty()) {
                    selected = candidate.copyWithCount(1);
                    break;
                }
            }
            projector.clearAllBannerSnapshots();
            if (!selected.isEmpty()) {
                projector.captureBannerSnapshot(0, selected);
            }
        }

        projector.setProjectionEnabled(true);
    }

    private static Component sourceDisplayName(ProjectionSettings.SourceMode sourceMode) {
        return ProjectionSourceRegistry.definition(sourceMode)
                .map(definition -> Component.translatable(definition.translationKey()))
                .orElse(Component.literal(sourceMode.serializedName()));
    }

    private static Direction portableFacing(@Nullable Direction requested) {
        if (requested == null) {
            return Direction.SOUTH;
        }
        Direction horizontal = requested.getAxis().isHorizontal() ? requested : Direction.SOUTH;
        return horizontal == Direction.UP || horizontal == Direction.DOWN ? Direction.SOUTH : horizontal;
    }

    private static net.minecraft.world.level.block.state.BlockState portableBlockState(Direction facing) {
        return ModBlocks.MIRAGE_PROJECTOR.get().defaultBlockState()
                .setValue(MirageProjectorBlock.FACING, portableFacing(facing));
    }

    private static void syncState(Player player, ItemStack projector) {
        if (!(player instanceof ServerPlayer owner) || owner.getServer() == null) {
            return;
        }
        UUID id = ensureDeviceId(projector);
        boolean active = projectionEnabled(projector);
        CompoundTag state = active ? customTag(projector) : new CompoundTag();
        PortableProjectorStatePayload payload = new PortableProjectorStatePayload(
                owner.getUUID(),
                id,
                active,
                state
        );
        for (ServerPlayer target : owner.getServer().getPlayerList().getPlayers()) {
            if (target.level().dimension().equals(owner.level().dimension())) {
                PacketDistributor.sendToPlayer(target, payload);
            }
        }
    }

    public enum BannerPresentation {
        FORWARD("forward", "gui.mirage_projector.portable_device.banner_presentation.forward"),
        WAR_BANNER("war_banner", "gui.mirage_projector.portable_device.banner_presentation.war_banner");

        private final String serializedName;
        private final String translationKey;

        BannerPresentation(String serializedName, String translationKey) {
            this.serializedName = serializedName;
            this.translationKey = translationKey;
        }

        public String serializedName() {
            return serializedName;
        }

        public String translationKey() {
            return translationKey;
        }

        public BannerPresentation next() {
            return this == FORWARD ? WAR_BANNER : FORWARD;
        }

        public static BannerPresentation parse(String value) {
            if (WAR_BANNER.serializedName.equals(value)) {
                return WAR_BANNER;
            }
            return FORWARD;
        }
    }

    public enum WarBannerFacing {
        DIRECTIONAL("directional", "gui.mirage_projector.portable_device.war_banner_facing.directional"),
        BILLBOARD("billboard", "gui.mirage_projector.portable_device.war_banner_facing.billboard");

        private final String serializedName;
        private final String translationKey;

        WarBannerFacing(String serializedName, String translationKey) {
            this.serializedName = serializedName;
            this.translationKey = translationKey;
        }

        public String serializedName() {
            return serializedName;
        }

        public String translationKey() {
            return translationKey;
        }

        public WarBannerFacing next() {
            return this == BILLBOARD ? DIRECTIONAL : BILLBOARD;
        }

        public static WarBannerFacing parse(String value) {
            if (DIRECTIONAL.serializedName.equals(value)) {
                return DIRECTIONAL;
            }
            return BILLBOARD;
        }
    }

    /** A Hand Projector image is always a single plane; this only selects its placement. */
    public enum ImagePresentation {
        NORMAL("normal", "gui.mirage_projector.portable_device.image_presentation.normal"),
        BILLBOARD("billboard", "gui.mirage_projector.portable_device.image_presentation.billboard"),
        SURFACE("surface", "gui.mirage_projector.portable_device.image_presentation.surface");

        private final String serializedName;
        private final String translationKey;

        ImagePresentation(String serializedName, String translationKey) {
            this.serializedName = serializedName;
            this.translationKey = translationKey;
        }

        public String serializedName() { return serializedName; }
        public String translationKey() { return translationKey; }

        public ImagePresentation next() {
            ImagePresentation[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        public static ImagePresentation parse(String value) {
            for (ImagePresentation presentation : values()) {
                if (presentation.serializedName.equals(value)) return presentation;
            }
            return NORMAL;
        }
    }

    private static CompoundTag customTag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void removeEmptyCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.copyTag().isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
    }
}
