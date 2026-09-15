package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.ClientShoulderEquipment;
import celerbi.mirageprojector.equipment.ShoulderEquipment;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Owner-only snapshot of the Shoulder Strap battery pouch and upgrade sockets. */
public record ShoulderEquipmentInventoryPayload(
        UUID ownerId,
        List<ItemStack> batteries,
        List<ItemStack> upgrades
) implements CustomPacketPayload {
    public static final Type<ShoulderEquipmentInventoryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "shoulder_equipment_inventory")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ShoulderEquipmentInventoryPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ShoulderEquipmentInventoryPayload decode(RegistryFriendlyByteBuf buffer) {
                    UUID owner = buffer.readUUID();
                    List<ItemStack> batteries = new ArrayList<>(ShoulderEquipment.EXPANDED_BATTERY_SLOTS);
                    for (int i = 0; i < ShoulderEquipment.EXPANDED_BATTERY_SLOTS; i++) {
                        batteries.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
                    }
                    List<ItemStack> upgrades = new ArrayList<>(ShoulderEquipment.EXPANDED_UPGRADE_SLOTS);
                    for (int i = 0; i < ShoulderEquipment.EXPANDED_UPGRADE_SLOTS; i++) {
                        upgrades.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
                    }
                    return new ShoulderEquipmentInventoryPayload(owner, batteries, upgrades);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ShoulderEquipmentInventoryPayload payload) {
                    buffer.writeUUID(payload.ownerId());
                    for (int i = 0; i < ShoulderEquipment.EXPANDED_BATTERY_SLOTS; i++) {
                        ItemStack stack = i < payload.batteries().size()
                                ? payload.batteries().get(i)
                                : ItemStack.EMPTY;
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
                    }
                    for (int i = 0; i < ShoulderEquipment.EXPANDED_UPGRADE_SLOTS; i++) {
                        ItemStack stack = i < payload.upgrades().size()
                                ? payload.upgrades().get(i)
                                : ItemStack.EMPTY;
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
                    }
                }
            };

    public ShoulderEquipmentInventoryPayload {
        batteries = copyFixed(batteries, ShoulderEquipment.EXPANDED_BATTERY_SLOTS);
        upgrades = copyFixed(upgrades, ShoulderEquipment.EXPANDED_UPGRADE_SLOTS);
    }

    @Override
    public Type<ShoulderEquipmentInventoryPayload> type() {
        return TYPE;
    }

    public static void handle(ShoulderEquipmentInventoryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientShoulderEquipment.acceptInventoryState(payload));
    }

    private static List<ItemStack> copyFixed(List<ItemStack> input, int size) {
        List<ItemStack> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ItemStack stack = input != null && i < input.size() ? input.get(i) : ItemStack.EMPTY;
            result.add(stack == null ? ItemStack.EMPTY : stack.copy());
        }
        return List.copyOf(result);
    }
}
