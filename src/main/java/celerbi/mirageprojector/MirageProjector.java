package celerbi.mirageprojector;

import celerbi.mirageprojector.blockentity.CoreBoosterBlockEntity;
import celerbi.mirageprojector.network.ModNetworking;
import celerbi.mirageprojector.registry.ModBlockEntities;
import celerbi.mirageprojector.registry.ModBlocks;
import celerbi.mirageprojector.registry.ModCreativeTabs;
import celerbi.mirageprojector.registry.ModItems;
import celerbi.mirageprojector.registry.ModMenus;
import celerbi.mirageprojector.registry.ModRecipeSerializers;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(MirageProjector.MOD_ID)
public final class MirageProjector {
    public static final String MOD_ID = "mirage_projector";

    public static final String NETWORK_PROTOCOL = "19";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MirageProjector(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        modEventBus.addListener(ModNetworking::registerPayloads);
        modEventBus.addListener(this::addCreativeTabContents);
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.MIRAGE_PROJECTOR.get());
            event.accept(ModItems.MIRAGE_DISPLAY.get());
            event.accept(ModItems.WIDE_MIRAGE_PROJECTOR.get());
            event.accept(ModItems.TALL_MIRAGE_PROJECTOR.get());
            event.accept(ModItems.MIRAGE_FIELD_PROJECTOR.get());
            event.accept(ModItems.MIRAGE_PRISM.get());
            event.accept(ModItems.CORE_BOOSTER.get());
            event.accept(CoreBoosterBlockEntity.stackForMaterial(CoreBoosterMaterial.GLASS));
            event.accept(CoreBoosterBlockEntity.stackForMaterial(CoreBoosterMaterial.QUARTZ));
            event.accept(CoreBoosterBlockEntity.stackForMaterial(CoreBoosterMaterial.AMETHYST));
            event.accept(CoreBoosterBlockEntity.stackForMaterial(CoreBoosterMaterial.DIAMOND));
            event.accept(CoreBoosterBlockEntity.stackForMaterial(CoreBoosterMaterial.NETHERITE));
            event.accept(ModItems.OBSIDIAN_SPIKE.get());
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.CRYING_OBSIDIAN_SHARD.get());
        }
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModItems.SMALL_CRYING_OBSIDIAN_BUD.get());
            event.accept(ModItems.MEDIUM_CRYING_OBSIDIAN_BUD.get());
            event.accept(ModItems.LARGE_CRYING_OBSIDIAN_BUD.get());
            event.accept(ModItems.CRYING_OBSIDIAN_CLUSTER.get());
        }
    }
}
