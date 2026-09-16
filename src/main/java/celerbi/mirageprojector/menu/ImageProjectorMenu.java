package celerbi.mirageprojector.menu;

import celerbi.mirageprojector.ImageSourceBank;
import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.ProjectionSettings;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public final class ImageProjectorMenu extends AbstractContainerMenu {
    private final BlockPos projectorPos;
    private final ProjectionSettings initialSettings;
    private final ProjectionChassisProfile chassisProfile;
    private final ImageSourceBank initialImageBank;
    private final int initialWallSlideIndex;
    private final boolean initialAutomaticPresentationEnabled;
    private final int initialAutomaticPresentationIntervalSeconds;
    private final boolean initialProjectionEnabled;
    @Nullable
    private final MirageProjectorBlockEntity projector;

    public ImageProjectorMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        super(ModMenus.IMAGE_PROJECTOR.get(), containerId);
        projectorPos = buffer.readBlockPos();
        initialSettings = ProjectionSettings.read(buffer);
        chassisProfile = readChassis(buffer.readVarInt());
        initialImageBank = ImageSourceBank.read(buffer);
        initialWallSlideIndex = buffer.readVarInt();
        initialAutomaticPresentationEnabled = buffer.readBoolean();
        initialAutomaticPresentationIntervalSeconds = buffer.readVarInt();
        initialProjectionEnabled = buffer.readBoolean();
        projector = inventory.player.level().getBlockEntity(projectorPos) instanceof MirageProjectorBlockEntity be ? be : null;
    }

    public ImageProjectorMenu(int containerId, Inventory inventory, MirageProjectorBlockEntity projector) {
        super(ModMenus.IMAGE_PROJECTOR.get(), containerId);
        this.projectorPos = projector.getBlockPos();
        this.initialSettings = projector.settings();
        this.chassisProfile = projector.chassisProfile();
        this.initialImageBank = projector.imageSourceBank().copy();
        this.initialWallSlideIndex = Math.max(0, projector.wallSlideIndex());
        this.initialAutomaticPresentationEnabled = projector.automaticPresentationEnabled();
        this.initialAutomaticPresentationIntervalSeconds = projector.automaticPresentationIntervalSeconds();
        this.initialProjectionEnabled = projector.projectionEnabled();
        this.projector = projector;
    }

    private static ProjectionChassisProfile readChassis(int ordinal) {
        ProjectionChassisProfile[] values = ProjectionChassisProfile.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ProjectionChassisProfile.COMPACT;
    }

    public BlockPos projectorPos() {
        return projectorPos;
    }

    public ProjectionSettings initialSettings() {
        return initialSettings;
    }

    public ProjectionChassisProfile chassisProfile() {
        return chassisProfile;
    }

    public ImageSourceBank initialImageBank() {
        return initialImageBank.copy();
    }

    public int initialWallSlideIndex() {
        return initialWallSlideIndex;
    }

    public boolean initialAutomaticPresentationEnabled() {
        return initialAutomaticPresentationEnabled;
    }

    public int initialAutomaticPresentationIntervalSeconds() {
        return Math.max(1, Math.min(120, initialAutomaticPresentationIntervalSeconds));
    }

    public boolean initialProjectionEnabled() { return initialProjectionEnabled; }

    public boolean isPresentationDeck() {
        return chassisProfile.supportsPresentationDeck();
    }

    public boolean isWallPresentation() {
        return chassisProfile == ProjectionChassisProfile.WALL;
    }

    public boolean supportsMultiSourceLayout() {
        return chassisProfile.supportsMultiSourceImageLayout();
    }

    public boolean hasMultiSourceLayout() {
        return supportsMultiSourceLayout()
                && initialSettings.imageLayoutMode() == ProjectionSettings.ImageLayoutMode.MULTI;
    }

    public int imageLayoutColumns() {
        return chassisProfile.imageLayoutColumns();
    }

    public int imageLayoutRows() {
        return chassisProfile.imageLayoutRows();
    }

    public int imageLayoutSlots() {
        return chassisProfile.imageLayoutSlots();
    }

    @Nullable
    public MirageProjectorBlockEntity projector() {
        return projector;
    }

    public int physicalFaceCount() {
        return chassisProfile.geometry() == ProjectionChassisProfile.Geometry.PRISM ? 4 : 2;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(
                projectorPos.getX() + 0.5D,
                projectorPos.getY() + 0.5D,
                projectorPos.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
