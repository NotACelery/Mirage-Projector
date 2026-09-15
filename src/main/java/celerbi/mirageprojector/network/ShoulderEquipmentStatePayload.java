package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.ClientShoulderEquipment;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server-authoritative snapshot of one player's Mirage shoulder equipment. */
public record ShoulderEquipmentStatePayload(
        UUID ownerId,
        boolean strapPresent,
        ItemStack shoulderDevice
) implements CustomPacketPayload {
    public static final Type<ShoulderEquipmentStatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "shoulder_equipment_state")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ShoulderEquipmentStatePayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ShoulderEquipmentStatePayload decode(RegistryFriendlyByteBuf buffer) {
                    return new ShoulderEquipmentStatePayload(
                            buffer.readUUID(),
                            buffer.readBoolean(),
                            ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)
                    );
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ShoulderEquipmentStatePayload payload) {
                    buffer.writeUUID(payload.ownerId());
                    buffer.writeBoolean(payload.strapPresent());
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload.shoulderDevice());
                }
            };

    public ShoulderEquipmentStatePayload {
        shoulderDevice = shoulderDevice == null ? ItemStack.EMPTY : shoulderDevice.copy();
    }

    @Override
    public Type<ShoulderEquipmentStatePayload> type() {
        return TYPE;
    }

    public static void handle(ShoulderEquipmentStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientShoulderEquipment.acceptServerState(payload));
    }
}
