package celerbi.mirageprojector.registry;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.block.MirageProjectorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MirageProjector.MOD_ID);

    public static final DeferredBlock<MirageProjectorBlock> MIRAGE_PROJECTOR = registerProjector("mirage_projector");
    public static final DeferredBlock<MirageProjectorBlock> MIRAGE_DISPLAY = registerProjector("mirage_display");
    public static final DeferredBlock<MirageProjectorBlock> WIDE_MIRAGE_PROJECTOR = registerProjector("wide_mirage_projector");
    public static final DeferredBlock<MirageProjectorBlock> TALL_MIRAGE_PROJECTOR = registerProjector("tall_mirage_projector");
    public static final DeferredBlock<MirageProjectorBlock> MIRAGE_FIELD_PROJECTOR = registerProjector("mirage_field_projector");
    public static final DeferredBlock<MirageProjectorBlock> MIRAGE_PRISM = registerProjector("mirage_prism");

    private ModBlocks() {
    }

    private static DeferredBlock<MirageProjectorBlock> registerProjector(String name) {
        return BLOCKS.register(
                name,
                () -> new MirageProjectorBlock(BlockBehaviour.Properties.of()
                        .strength(50.0F, 1200.0F)
                        .sound(SoundType.STONE)
                        .noOcclusion())
        );
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
