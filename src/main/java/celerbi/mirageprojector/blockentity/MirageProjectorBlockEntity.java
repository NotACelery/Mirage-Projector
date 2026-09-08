package celerbi.mirageprojector.blockentity;

import celerbi.mirageprojector.ProjectionCoreProfile;
import celerbi.mirageprojector.ProjectionPower;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.menu.MirageProjectorMenu;
import celerbi.mirageprojector.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class MirageProjectorBlockEntity extends BlockEntity implements MenuProvider {
    private ProjectionSettings settings = ProjectionSettings.DEFAULT;

    private final ItemStackHandler projectionItem = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
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

    public MirageProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIRAGE_PROJECTOR.get(), pos, state);
        // Compact starts with its intended low-power Glass Core. Old dev.7 worlds
        // without a serialized CoreItem migrate to the same state in loadAdditional.
        coreItem.setStackInSlot(0, new ItemStack(Blocks.GLASS));
    }

    public ProjectionSettings settings() {
        return settings;
    }

    public ItemStackHandler projectionItem() {
        return projectionItem;
    }

    public ItemStackHandler coreItem() {
        return coreItem;
    }

    public ItemStack projectedStack() {
        return projectionItem.getStackInSlot(0);
    }

    public ItemStack coreStack() {
        return coreItem.getStackInSlot(0);
    }

    public ProjectionCoreProfile coreProfile() {
        return ProjectionCoreProfile.fromStack(coreStack());
    }

    public ProjectionPower.Status powerStatus() {
        return ProjectionPower.evaluate(settings, coreProfile(), !projectedStack().isEmpty());
    }

    public void applySettings(ProjectionSettings newSettings) {
        // Do not clamp settings to the current core. Core swaps must never destroy a
        // carefully tuned Mirage configuration; insufficient configurations simply go
        // inactive until power/envelope requirements are satisfied again.
        settings = newSettings.sanitized();
        setChangedAndSync();
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public void writeMenuData(RegistryFriendlyByteBuf buffer) {
        ProjectionSettings s = settings;
        buffer.writeBlockPos(worldPosition);
        writeAsset(buffer, s.imageId(), s.imageWidth(), s.imageHeight());
        writeAsset(buffer, s.backImageId(), s.backImageWidth(), s.backImageHeight());
        buffer.writeVarInt(s.sourceMode().ordinal());
        buffer.writeVarInt(s.scalePixels());
        buffer.writeVarInt(s.liftPixels());
        buffer.writeBoolean(s.rotationEnabled());
        buffer.writeVarInt(s.rotationPeriodTicks());
        buffer.writeBoolean(s.clockwise());
        buffer.writeFloat(s.rotationOffsetDegrees());
        buffer.writeBoolean(s.floatingEnabled());
        buffer.writeVarInt(s.floatMode().ordinal());
        buffer.writeVarInt(s.floatAmplitudePixels());
        buffer.writeVarInt(s.floatCycleTicks());
        buffer.writeVarInt(s.floatIntervalDegrees());
        buffer.writeVarInt(s.backFaceMode().ordinal());
        buffer.writeBoolean(s.flipVertical());
        buffer.writeBoolean(s.debugChassisOverride());
    }

    private static void writeAsset(RegistryFriendlyByteBuf buffer, String id, int width, int height) {
        buffer.writeUtf(id, 128);
        buffer.writeVarInt(width);
        buffer.writeVarInt(height);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ProjectionSettings s = settings;
        tag.putString("ImageId", s.imageId());
        tag.putInt("ImageWidth", s.imageWidth());
        tag.putInt("ImageHeight", s.imageHeight());
        tag.putString("BackImageId", s.backImageId());
        tag.putInt("BackImageWidth", s.backImageWidth());
        tag.putInt("BackImageHeight", s.backImageHeight());
        tag.putInt("SourceMode", s.sourceMode().ordinal());
        tag.putInt("ScalePixels", s.scalePixels());
        tag.putInt("LiftPixels", s.liftPixels());
        tag.putBoolean("RotationEnabled", s.rotationEnabled());
        tag.putInt("RotationPeriodTicks", s.rotationPeriodTicks());
        tag.putBoolean("Clockwise", s.clockwise());
        tag.putFloat("RotationOffsetDegrees", s.rotationOffsetDegrees());
        tag.putBoolean("FloatingEnabled", s.floatingEnabled());
        tag.putInt("FloatMode", s.floatMode().ordinal());
        tag.putInt("FloatAmplitudePixels", s.floatAmplitudePixels());
        tag.putInt("FloatCycleTicks", s.floatCycleTicks());
        tag.putInt("FloatIntervalDegrees", s.floatIntervalDegrees());
        tag.putInt("BackFaceMode", s.backFaceMode().ordinal());
        tag.putBoolean("FlipVertical", s.flipVertical());
        tag.putBoolean("DebugChassisOverride", s.debugChassisOverride());
        tag.put("ProjectionItem", projectionItem.serializeNBT(registries));
        tag.put("CoreItem", coreItem.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ProjectionSettings defaults = ProjectionSettings.DEFAULT;
        settings = new ProjectionSettings(
                tag.contains("ImageId") ? tag.getString("ImageId") : defaults.imageId(),
                tag.contains("ImageWidth") ? tag.getInt("ImageWidth") : defaults.imageWidth(),
                tag.contains("ImageHeight") ? tag.getInt("ImageHeight") : defaults.imageHeight(),
                tag.contains("BackImageId") ? tag.getString("BackImageId") : defaults.backImageId(),
                tag.contains("BackImageWidth") ? tag.getInt("BackImageWidth") : defaults.backImageWidth(),
                tag.contains("BackImageHeight") ? tag.getInt("BackImageHeight") : defaults.backImageHeight(),
                ProjectionSettings.SourceMode.fromOrdinal(tag.contains("SourceMode") ? tag.getInt("SourceMode") : defaults.sourceMode().ordinal()),
                tag.contains("ScalePixels") ? tag.getInt("ScalePixels") : defaults.scalePixels(),
                tag.contains("LiftPixels") ? tag.getInt("LiftPixels") : defaults.liftPixels(),
                tag.contains("RotationEnabled") ? tag.getBoolean("RotationEnabled") : defaults.rotationEnabled(),
                tag.contains("RotationPeriodTicks") ? tag.getInt("RotationPeriodTicks") : defaults.rotationPeriodTicks(),
                tag.contains("Clockwise") ? tag.getBoolean("Clockwise") : defaults.clockwise(),
                tag.contains("RotationOffsetDegrees") ? tag.getFloat("RotationOffsetDegrees") : defaults.rotationOffsetDegrees(),
                tag.contains("FloatingEnabled") ? tag.getBoolean("FloatingEnabled") : defaults.floatingEnabled(),
                ProjectionSettings.FloatMode.fromOrdinal(tag.contains("FloatMode") ? tag.getInt("FloatMode") : defaults.floatMode().ordinal()),
                tag.contains("FloatAmplitudePixels") ? tag.getInt("FloatAmplitudePixels") : defaults.floatAmplitudePixels(),
                tag.contains("FloatCycleTicks") ? tag.getInt("FloatCycleTicks") : defaults.floatCycleTicks(),
                tag.contains("FloatIntervalDegrees") ? tag.getInt("FloatIntervalDegrees") : defaults.floatIntervalDegrees(),
                ProjectionSettings.BackFaceMode.fromOrdinal(tag.contains("BackFaceMode") ? tag.getInt("BackFaceMode") : defaults.backFaceMode().ordinal()),
                tag.contains("FlipVertical") ? tag.getBoolean("FlipVertical") : defaults.flipVertical(),
                tag.contains("DebugChassisOverride") ? tag.getBoolean("DebugChassisOverride") : defaults.debugChassisOverride()
        ).sanitized();

        if (tag.contains("ProjectionItem")) {
            projectionItem.deserializeNBT(registries, tag.getCompound("ProjectionItem"));
        } else {
            projectionItem.setStackInSlot(0, ItemStack.EMPTY);
        }

        if (tag.contains("CoreItem")) {
            coreItem.deserializeNBT(registries, tag.getCompound("CoreItem"));
            if (!coreItem.getStackInSlot(0).isEmpty() && !ProjectionCoreProfile.isCoreItem(coreItem.getStackInSlot(0))) {
                coreItem.setStackInSlot(0, ItemStack.EMPTY);
            }
        } else {
            // dev.7 -> dev.8 migration.
            coreItem.setStackInSlot(0, new ItemStack(Blocks.GLASS));
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
        return Component.translatable("container.mirage_projector.projector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MirageProjectorMenu(containerId, inventory, this);
    }
}
