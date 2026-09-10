package celerbi.mirageprojector.registry;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.block.CoreBoosterBlock;
import celerbi.mirageprojector.block.CryingObsidianCrystalBlock;
import celerbi.mirageprojector.block.CryingObsidianLightNodeBlock;
import celerbi.mirageprojector.block.LegacyImprovedCoreBlock;
import celerbi.mirageprojector.block.MirageProjectorBlock;
import celerbi.mirageprojector.block.ObsidianSpikeBlock;
import celerbi.mirageprojector.crying.CryingObsidianCrystalStage;
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

    public static final DeferredBlock<CoreBoosterBlock> CORE_BOOSTER = BLOCKS.register(
            "core_booster",
            () -> new CoreBoosterBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 9.0F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .requiresCorrectToolForDrops())
    );

    public static final DeferredBlock<LegacyImprovedCoreBlock> LEGACY_IMPROVED_GLASS_CORE = registerLegacyImprovedCore("improved_glass_core");
    public static final DeferredBlock<LegacyImprovedCoreBlock> LEGACY_IMPROVED_QUARTZ_CORE = registerLegacyImprovedCore("improved_quartz_core");
    public static final DeferredBlock<LegacyImprovedCoreBlock> LEGACY_IMPROVED_AMETHYST_CORE = registerLegacyImprovedCore("improved_amethyst_core");
    public static final DeferredBlock<LegacyImprovedCoreBlock> LEGACY_IMPROVED_DIAMOND_CORE = registerLegacyImprovedCore("improved_diamond_core");
    public static final DeferredBlock<LegacyImprovedCoreBlock> LEGACY_IMPROVED_NETHERITE_CORE = registerLegacyImprovedCore("improved_netherite_core");

    public static final DeferredBlock<CryingObsidianCrystalBlock> SMALL_CRYING_OBSIDIAN_BUD =
            registerCrystal("small_crying_obsidian_bud", CryingObsidianCrystalStage.SMALL, 3.0F, 4.0F);
    public static final DeferredBlock<CryingObsidianCrystalBlock> MEDIUM_CRYING_OBSIDIAN_BUD =
            registerCrystal("medium_crying_obsidian_bud", CryingObsidianCrystalStage.MEDIUM, 4.0F, 3.0F);
    public static final DeferredBlock<CryingObsidianCrystalBlock> LARGE_CRYING_OBSIDIAN_BUD =
            registerCrystal("large_crying_obsidian_bud", CryingObsidianCrystalStage.LARGE, 5.0F, 3.0F);
    public static final DeferredBlock<CryingObsidianCrystalBlock> CRYING_OBSIDIAN_CLUSTER =
            registerCrystal("crying_obsidian_cluster", CryingObsidianCrystalStage.MATURE, 7.0F, 3.0F);
    public static final DeferredBlock<CryingObsidianLightNodeBlock> CRYING_LIGHT_NODE = BLOCKS.register(
            "crying_light_node",
            () -> new CryingObsidianLightNodeBlock(BlockBehaviour.Properties.of()
                    .air()
                    .replaceable()
                    .noCollission()
                    .noOcclusion()
                    .noLootTable()
                    .lightLevel(state -> state.getValue(CryingObsidianLightNodeBlock.LIGHT_LEVEL)))
    );

    public static final DeferredBlock<ObsidianSpikeBlock> OBSIDIAN_SPIKE = BLOCKS.register(
            "obsidian_spike",
            () -> new ObsidianSpikeBlock(BlockBehaviour.Properties.of()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .noCollission()
                    .noOcclusion())
    );

    private ModBlocks() {
    }

    private static DeferredBlock<CryingObsidianCrystalBlock> registerCrystal(
            String name,
            CryingObsidianCrystalStage stage,
            float height,
            float aabbOffset
    ) {
        return BLOCKS.register(
                name,
                () -> new CryingObsidianCrystalBlock(
                        stage,
                        height,
                        aabbOffset,
                        BlockBehaviour.Properties.of()
                                .strength(1.5F)
                                .sound(SoundType.AMETHYST_CLUSTER)
                                .noOcclusion()
                                .randomTicks()
                                .emissiveRendering((state, level, pos) -> state.getValue(CryingObsidianCrystalBlock.ENERGIZED))
                                .lightLevel(state -> state.getValue(CryingObsidianCrystalBlock.ENERGIZED)
                                        ? stage.vanillaPoweredLight()
                                        : 0)
                )
        );
    }

    private static DeferredBlock<LegacyImprovedCoreBlock> registerLegacyImprovedCore(String name) {
        return BLOCKS.register(
                name,
                () -> new LegacyImprovedCoreBlock(BlockBehaviour.Properties.of()
                        .strength(3.0F, 9.0F)
                        .sound(SoundType.GLASS)
                        .noOcclusion())
        );
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
