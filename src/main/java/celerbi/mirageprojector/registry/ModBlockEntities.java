package celerbi.mirageprojector.registry;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MirageProjector.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MirageProjectorBlockEntity>> MIRAGE_PROJECTOR =
            BLOCK_ENTITY_TYPES.register("mirage_projector", () ->
                    BlockEntityType.Builder.of(MirageProjectorBlockEntity::new, ModBlocks.MIRAGE_PROJECTOR.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
