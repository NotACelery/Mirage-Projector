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

/** Source-specific menu for image/face assignment. No physical inventory is stored here. */
public final class ImageProjectorMenu extends AbstractContainerMenu {
    private final BlockPos projectorPos;
    private final ProjectionSettings initialSettings;
    private final ProjectionChassisProfile chassisProfile;
    private final ImageSourceBank initialImageBank;
    @Nullable
    private final MirageProjectorBlockEntity projector;

    public ImageProjectorMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        super(ModMenus.IMAGE_PROJECTOR.get(), containerId);
        projectorPos = buffer.readBlockPos();
        initialSettings = ProjectionSettings.read(buffer);
        chassisProfile = readChassis(buffer.readVarInt());
        initialImageBank = ImageSourceBank.read(buffer);
        projector = inventory.player.level().getBlockEntity(projectorPos) instanceof MirageProjectorBlockEntity be ? be : null;
    }

    public ImageProjectorMenu(int containerId, Inventory inventory, MirageProjectorBlockEntity projector) {
        super(ModMenus.IMAGE_PROJECTOR.get(), containerId);
        this.projectorPos = projector.getBlockPos();
        this.initialSettings = projector.settings();
        this.chassisProfile = projector.chassisProfile();
        this.initialImageBank = projector.imageSourceBank().copy();
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

    public boolean hasMultiSourceLayout() {
        return chassisProfile.hasMultiSourceImageLayout();
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
