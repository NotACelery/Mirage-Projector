package celerbi.mirageprojector.mixin;

import celerbi.mirageprojector.block.CryingObsidianCrystalBlock;
import celerbi.mirageprojector.crying.CryingObsidianGrowthHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class CryingObsidianRandomTickStateMixin {
    @Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
    private void mirageProjector$enableCryingObsidianGrowthTicks(CallbackInfoReturnable<Boolean> cir) {
        BlockState state = (BlockState) (Object) this;
        if (state.is(Blocks.CRYING_OBSIDIAN)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void mirageProjector$growCryingObsidianCrystal(
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo ci
    ) {
        BlockState state = (BlockState) (Object) this;
        if (state.is(Blocks.CRYING_OBSIDIAN)) {
            CryingObsidianGrowthHooks.tryNucleate(level, pos, random);
        }
    }
    @Inject(
            method = "getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void mirageProjector$suppressBeaconLightUnderMatureCluster(
            BlockGetter level,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        BlockState state = (BlockState) (Object) this;
        if (!state.is(Blocks.BEACON)) {
            return;
        }
        BlockState above = level.getBlockState(pos.above());
        if (above.getBlock() instanceof CryingObsidianCrystalBlock crystal
                && crystal.stage().isMature()
                && above.getValue(CryingObsidianCrystalBlock.ENERGIZED)) {
            cir.setReturnValue(0);
        }
    }

}
