package celerbi.mirageprojector;

import celerbi.mirageprojector.network.ModNetworking;
import celerbi.mirageprojector.registry.ModBlockEntities;
import celerbi.mirageprojector.registry.ModBlocks;
import celerbi.mirageprojector.registry.ModCreativeTabs;
import celerbi.mirageprojector.registry.ModItems;
import celerbi.mirageprojector.registry.ModMenus;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(MirageProjector.MOD_ID)
public final class MirageProjector {
    public static final String MOD_ID = "mirage_projector";
    /** Network compatibility token for the current packet/schema family. */
    public static final String NETWORK_PROTOCOL = "18";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MirageProjector(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
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
        }
    }
}
