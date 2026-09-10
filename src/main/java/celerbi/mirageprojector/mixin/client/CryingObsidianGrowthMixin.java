package celerbi.mirageprojector.mixin.client;

import celerbi.mirageprojector.crying.CryingObsidianGrowthHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class CryingObsidianGrowthMixin {
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void mirageProjector$growCryingObsidianCrystal(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo ci
    ) {
        if (state.is(Blocks.CRYING_OBSIDIAN)) {
            CryingObsidianGrowthHooks.tryNucleate(level, pos, random);
        }
    }
}
