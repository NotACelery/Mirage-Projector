package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.MirageEquipmentClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server correction for the cursor stack used by Mirage Equipment's external inventory panel. */
public record ShoulderEquipmentCursorPayload(ItemStack carried) implements CustomPacketPayload {
    public static final Type<ShoulderEquipmentCursorPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "shoulder_equipment_cursor")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ShoulderEquipmentCursorPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ShoulderEquipmentCursorPayload decode(RegistryFriendlyByteBuf buffer) {
                    return new ShoulderEquipmentCursorPayload(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ShoulderEquipmentCursorPayload payload) {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(
                            buffer,
                            payload.carried() == null ? ItemStack.EMPTY : payload.carried()
                    );
                }
            };

    public ShoulderEquipmentCursorPayload {
        carried = carried == null ? ItemStack.EMPTY : carried.copy();
    }

    @Override
    public Type<ShoulderEquipmentCursorPayload> type() {
        return TYPE;
    }

    public static void handle(ShoulderEquipmentCursorPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            ItemStack corrected = payload.carried().copy();
            if (minecraft.screen instanceof AbstractContainerScreen<?> containerScreen) {
                containerScreen.getMenu().setCarried(corrected.copy());
            }
            if (minecraft.player != null && minecraft.player.containerMenu != null) {
                minecraft.player.containerMenu.setCarried(corrected);
            }
            MirageEquipmentClientEvents.onEquipmentCursorSynchronized();
        });
    }
}
