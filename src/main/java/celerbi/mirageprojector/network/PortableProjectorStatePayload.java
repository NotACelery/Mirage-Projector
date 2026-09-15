package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.ClientHeldProjectors;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Lightweight state publication for an inventory-resident active handheld projector.
 *
 * <p>Player movement is still vanilla-tracked. Mirage only publishes the portable device ID and
 * serialized ItemStack custom-data state so remote clients do not need access to another player's
 * arbitrary inventory contents.</p>
 */
public record PortableProjectorStatePayload(
        UUID ownerId,
        UUID deviceId,
        boolean active,
        CompoundTag customData
) implements CustomPacketPayload {
    public static final Type<PortableProjectorStatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "portable_projector_state")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PortableProjectorStatePayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public PortableProjectorStatePayload decode(RegistryFriendlyByteBuf buffer) {
                    UUID ownerId = buffer.readUUID();
                    UUID deviceId = buffer.readUUID();
                    boolean active = buffer.readBoolean();
                    CompoundTag tag = buffer.readNbt();
                    return new PortableProjectorStatePayload(
                            ownerId,
                            deviceId,
                            active,
                            tag == null ? new CompoundTag() : tag
                    );
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, PortableProjectorStatePayload payload) {
                    buffer.writeUUID(payload.ownerId());
                    buffer.writeUUID(payload.deviceId());
                    buffer.writeBoolean(payload.active());
                    buffer.writeNbt(payload.customData());
                }
            };

    public PortableProjectorStatePayload {
        customData = customData == null ? new CompoundTag() : customData.copy();
    }

    @Override
    public Type<PortableProjectorStatePayload> type() {
        return TYPE;
    }

    public static void handle(PortableProjectorStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientHeldProjectors.acceptServerState(payload));
    }
}
