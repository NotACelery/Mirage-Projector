package celerbi.mirageprojector.registry;

import celerbi.mirageprojector.MirageProjector;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MirageProjector.MOD_ID);

    public static final DeferredItem<BlockItem> MIRAGE_PROJECTOR =
            ITEMS.registerSimpleBlockItem("mirage_projector", ModBlocks.MIRAGE_PROJECTOR);

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
