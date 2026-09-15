package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.equipment.ShoulderEquipmentRuntime;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client request to interact with one Mirage Equipment slot using the vanilla carried stack. */
public record ShoulderEquipmentActionPayload(Target target) implements CustomPacketPayload {
    public static final Type<ShoulderEquipmentActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "shoulder_equipment_action")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ShoulderEquipmentActionPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ShoulderEquipmentActionPayload decode(RegistryFriendlyByteBuf buffer) {
                    int ordinal = buffer.readVarInt();
                    Target[] values = Target.values();
                    return new ShoulderEquipmentActionPayload(
                            ordinal >= 0 && ordinal < values.length ? values[ordinal] : Target.STRAP
                    );
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ShoulderEquipmentActionPayload payload) {
                    buffer.writeVarInt(payload.target().ordinal());
                }
            };

    @Override
    public Type<ShoulderEquipmentActionPayload> type() {
        return TYPE;
    }

    public static void handle(ShoulderEquipmentActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ShoulderEquipmentRuntime.handleInventoryClick(player, payload.target());
            }
        });
    }

    public enum Target {
        STRAP,
        DEVICE,
        BATTERY_0,
        BATTERY_1,
        BATTERY_2,
        BATTERY_3,
        BATTERY_4,
        BATTERY_5,
        BATTERY_6,
        BATTERY_7,
        BATTERY_8,
        UPGRADE_0,
        UPGRADE_1,
        UPGRADE_2;

        public boolean battery() {
            return ordinal() >= BATTERY_0.ordinal() && ordinal() <= BATTERY_8.ordinal();
        }

        public int batteryIndex() {
            return battery() ? ordinal() - BATTERY_0.ordinal() : -1;
        }

        public boolean upgrade() {
            return ordinal() >= UPGRADE_0.ordinal() && ordinal() <= UPGRADE_2.ordinal();
        }

        public int upgradeIndex() {
            return upgrade() ? ordinal() - UPGRADE_0.ordinal() : -1;
        }

        public static Target battery(int index) {
            return switch (index) {
                case 0 -> BATTERY_0;
                case 1 -> BATTERY_1;
                case 2 -> BATTERY_2;
                case 3 -> BATTERY_3;
                case 4 -> BATTERY_4;
                case 5 -> BATTERY_5;
                case 6 -> BATTERY_6;
                case 7 -> BATTERY_7;
                case 8 -> BATTERY_8;
                default -> throw new IllegalArgumentException("Invalid battery pouch index: " + index);
            };
        }

        public static Target upgrade(int index) {
            return switch (index) {
                case 0 -> UPGRADE_0;
                case 1 -> UPGRADE_1;
                case 2 -> UPGRADE_2;
                default -> throw new IllegalArgumentException("Invalid shoulder upgrade index: " + index);
            };
        }
    }
}
