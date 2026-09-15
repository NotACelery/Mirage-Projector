package celerbi.mirageprojector.mixin;

import celerbi.mirageprojector.equipment.ShoulderEquipmentRuntime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Reserves the vanilla right shoulder while a Mirage Shoulder Device is mounted there. */
@Mixin(Player.class)
public abstract class PlayerShoulderReservationMixin {
    @Inject(method = "setEntityOnShoulder", at = @At("HEAD"), cancellable = true)
    private void mirage$protectReservedShoulder(
            CompoundTag entityCompound,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Player player = (Player) (Object) this;
        if (!ShoulderEquipmentRuntime.get(player).hasDevice()) {
            return;
        }
        // Vanilla fills left before right. Let the free left shoulder remain usable, but once
        // left is occupied do not allow a second shoulder-riding entity to claim Mirage's side.
        if (!player.getShoulderEntityLeft().isEmpty() && player.getShoulderEntityRight().isEmpty()) {
            cir.setReturnValue(false);
        }
    }
}
