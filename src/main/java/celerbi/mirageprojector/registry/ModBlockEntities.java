package celerbi.mirageprojector.registry;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.blockentity.CoreBoosterBlockEntity;
import celerbi.mirageprojector.blockentity.MirageProjectorBlockEntity;
import celerbi.mirageprojector.blockentity.ImprovedCoreBlockEntity;
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
                    BlockEntityType.Builder.of(
                            MirageProjectorBlockEntity::new,
                            ModBlocks.MIRAGE_PROJECTOR.get(),
                            ModBlocks.MIRAGE_DISPLAY.get(),
                            ModBlocks.WIDE_MIRAGE_PROJECTOR.get(),
                            ModBlocks.TALL_MIRAGE_PROJECTOR.get(),
                            ModBlocks.MIRAGE_FIELD_PROJECTOR.get(),
                            ModBlocks.MIRAGE_PRISM.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoreBoosterBlockEntity>> CORE_BOOSTER =
            BLOCK_ENTITY_TYPES.register("core_booster", () ->
                    BlockEntityType.Builder.of(
                            CoreBoosterBlockEntity::new,
                            ModBlocks.CORE_BOOSTER.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImprovedCoreBlockEntity>> IMPROVED_CORE =
            BLOCK_ENTITY_TYPES.register("improved_core", () ->
                    BlockEntityType.Builder.of(
                            ImprovedCoreBlockEntity::new,
                            ModBlocks.IMPROVED_GLASS_CORE.get(),
                            ModBlocks.IMPROVED_QUARTZ_CORE.get(),
                            ModBlocks.IMPROVED_AMETHYST_CORE.get(),
                            ModBlocks.IMPROVED_DIAMOND_CORE.get(),
                            ModBlocks.IMPROVED_NETHERITE_CORE.get()
                    ).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
