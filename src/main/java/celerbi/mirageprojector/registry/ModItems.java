package celerbi.mirageprojector.registry;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.item.EntityScanCardItem;
import celerbi.mirageprojector.item.MirageDebugHandbookItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MirageProjector.MOD_ID);

    public static final DeferredItem<BlockItem> MIRAGE_PROJECTOR =
            ITEMS.registerSimpleBlockItem("mirage_projector", ModBlocks.MIRAGE_PROJECTOR);
    public static final DeferredItem<BlockItem> MIRAGE_DISPLAY =
            ITEMS.registerSimpleBlockItem("mirage_display", ModBlocks.MIRAGE_DISPLAY);
    public static final DeferredItem<BlockItem> WIDE_MIRAGE_PROJECTOR =
            ITEMS.registerSimpleBlockItem("wide_mirage_projector", ModBlocks.WIDE_MIRAGE_PROJECTOR);
    public static final DeferredItem<BlockItem> TALL_MIRAGE_PROJECTOR =
            ITEMS.registerSimpleBlockItem("tall_mirage_projector", ModBlocks.TALL_MIRAGE_PROJECTOR);
    public static final DeferredItem<BlockItem> MIRAGE_FIELD_PROJECTOR =
            ITEMS.registerSimpleBlockItem("mirage_field_projector", ModBlocks.MIRAGE_FIELD_PROJECTOR);
    public static final DeferredItem<BlockItem> MIRAGE_PRISM =
            ITEMS.registerSimpleBlockItem("mirage_prism", ModBlocks.MIRAGE_PRISM);
    public static final DeferredItem<EntityScanCardItem> ENTITY_SCAN_CARD =
            ITEMS.register("entity_scan_card", () -> new EntityScanCardItem(new Item.Properties()));
    public static final DeferredItem<MirageDebugHandbookItem> DEBUG_HANDBOOK =
            ITEMS.register("debug_handbook", () -> new MirageDebugHandbookItem(new Item.Properties()));

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
