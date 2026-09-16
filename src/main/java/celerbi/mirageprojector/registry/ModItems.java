package celerbi.mirageprojector.registry;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.item.ShoulderStrapItem;
import celerbi.mirageprojector.item.CoreBoosterItem;
import celerbi.mirageprojector.item.CreativeBatteryItem;
import celerbi.mirageprojector.item.EntityScanCardItem;
import celerbi.mirageprojector.item.MirageHandProjectorItem;
import celerbi.mirageprojector.item.GlowDustItem;
import celerbi.mirageprojector.item.LightBatteryItem;
import celerbi.mirageprojector.item.MirageDebugHandbookItem;
import celerbi.mirageprojector.item.MirageLanternItem;
import celerbi.mirageprojector.item.PresentationRemoteItem;
import celerbi.mirageprojector.item.ScanCodexItem;
import celerbi.mirageprojector.item.ShoulderUpgradePatchItem;
import celerbi.mirageprojector.equipment.ShoulderUpgradeFamilies;
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
    public static final DeferredItem<BlockItem> MIRAGE_TABLE_PROJECTOR =
            ITEMS.registerSimpleBlockItem("mirage_table_projector", ModBlocks.MIRAGE_TABLE_PROJECTOR);
    public static final DeferredItem<BlockItem> MIRAGE_WALL_PROJECTOR =
            ITEMS.registerSimpleBlockItem("mirage_wall_projector", ModBlocks.MIRAGE_WALL_PROJECTOR);
    public static final DeferredItem<BlockItem> MIRAGE_LIGHT_PROJECTOR =
            ITEMS.registerSimpleBlockItem("mirage_light_projector", ModBlocks.MIRAGE_LIGHT_PROJECTOR);
    public static final DeferredItem<CoreBoosterItem> CORE_BOOSTER =
            ITEMS.register("core_booster", () -> new CoreBoosterItem(ModBlocks.CORE_BOOSTER.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> CHARGING_STATION =
            ITEMS.registerSimpleBlockItem("charging_station", ModBlocks.CHARGING_STATION);
    public static final DeferredItem<BlockItem> SMALL_CRYING_OBSIDIAN_BUD =
            ITEMS.registerSimpleBlockItem("small_crying_obsidian_bud", ModBlocks.SMALL_CRYING_OBSIDIAN_BUD);
    public static final DeferredItem<BlockItem> MEDIUM_CRYING_OBSIDIAN_BUD =
            ITEMS.registerSimpleBlockItem("medium_crying_obsidian_bud", ModBlocks.MEDIUM_CRYING_OBSIDIAN_BUD);
    public static final DeferredItem<BlockItem> LARGE_CRYING_OBSIDIAN_BUD =
            ITEMS.registerSimpleBlockItem("large_crying_obsidian_bud", ModBlocks.LARGE_CRYING_OBSIDIAN_BUD);
    public static final DeferredItem<BlockItem> CRYING_OBSIDIAN_CLUSTER =
            ITEMS.registerSimpleBlockItem("crying_obsidian_cluster", ModBlocks.CRYING_OBSIDIAN_CLUSTER);
    public static final DeferredItem<BlockItem> OBSIDIAN_SPIKE =
            ITEMS.registerSimpleBlockItem("obsidian_spike", ModBlocks.OBSIDIAN_SPIKE);
    public static final DeferredItem<Item> CRYING_OBSIDIAN_SHARD =
            ITEMS.registerSimpleItem("crying_obsidian_shard", new Item.Properties());
    public static final DeferredItem<GlowDustItem> GLOW_DUST =
            ITEMS.register("glow_dust", () -> new GlowDustItem(new Item.Properties()));
    public static final DeferredItem<LightBatteryItem> LIGHT_BATTERY =
            ITEMS.register("light_battery", () -> new LightBatteryItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<CreativeBatteryItem> CREATIVE_BATTERY =
            ITEMS.register("creative_battery", () -> new CreativeBatteryItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<MirageLanternItem> MIRAGE_LANTERN =
            ITEMS.register("mirage_lantern", () -> new MirageLanternItem(new Item.Properties().stacksTo(1)));
    // Registry ID intentionally stays `arm_strap` for world/save compatibility; public name is Shoulder Strap.
    public static final DeferredItem<ShoulderStrapItem> SHOULDER_STRAP =
            ITEMS.register("arm_strap", () -> new ShoulderStrapItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ShoulderUpgradePatchItem> AUTO_BATTERY_SWAP_PATCH =
            ITEMS.register("auto_battery_swap_patch", () -> new ShoulderUpgradePatchItem(
                    new Item.Properties().stacksTo(1),
                    ShoulderUpgradeFamilies.AUTO_BATTERY_SWAP,
                    null
            ));
    // Registry ID intentionally stays `battery_pouch_expansion_patch`; public name is Shoulder Strap Slot Expansion.
    public static final DeferredItem<ShoulderUpgradePatchItem> SHOULDER_STRAP_SLOT_EXPANSION =
            ITEMS.register("battery_pouch_expansion_patch", () -> new ShoulderUpgradePatchItem(
                    new Item.Properties().stacksTo(1),
                    ShoulderUpgradeFamilies.SHOULDER_STRAP_SLOT_EXPANSION,
                    "tooltip.mirage_projector.shoulder_strap_slot_expansion"
            ));
    public static final DeferredItem<MirageHandProjectorItem> MIRAGE_HAND_PROJECTOR =
            ITEMS.register("mirage_hand_projector", () -> new MirageHandProjectorItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<PresentationRemoteItem> PRESENTATION_REMOTE =
            ITEMS.register("presentation_remote", () -> new PresentationRemoteItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ScanCodexItem> SCAN_CODEX =
            ITEMS.register("scan_codex", () -> new ScanCodexItem(new Item.Properties()));
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
