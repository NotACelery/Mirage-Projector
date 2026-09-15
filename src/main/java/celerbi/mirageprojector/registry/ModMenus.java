package celerbi.mirageprojector.registry;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.menu.BannerProjectorMenu;
import celerbi.mirageprojector.menu.ChargingStationMenu;
import celerbi.mirageprojector.menu.EntityProjectorMenu;
import celerbi.mirageprojector.menu.ImageProjectorMenu;
import celerbi.mirageprojector.menu.ItemProjectorMenu;
import celerbi.mirageprojector.menu.MirageProjectorMenu;
import celerbi.mirageprojector.menu.PortableDeviceMenu;
import celerbi.mirageprojector.menu.MirageLightProjectorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MirageProjector.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<MirageProjectorMenu>> MIRAGE_PROJECTOR =
            MENUS.register("mirage_projector", () -> IMenuTypeExtension.create(MirageProjectorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<EntityProjectorMenu>> ENTITY_PROJECTOR =
            MENUS.register("entity_projector", () -> IMenuTypeExtension.create(EntityProjectorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ImageProjectorMenu>> IMAGE_PROJECTOR =
            MENUS.register("image_projector", () -> IMenuTypeExtension.create(ImageProjectorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ItemProjectorMenu>> ITEM_PROJECTOR =
            MENUS.register("item_projector", () -> IMenuTypeExtension.create(ItemProjectorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BannerProjectorMenu>> BANNER_PROJECTOR =
            MENUS.register("banner_projector", () -> IMenuTypeExtension.create(BannerProjectorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ChargingStationMenu>> CHARGING_STATION =
            MENUS.register("charging_station", () -> IMenuTypeExtension.create(ChargingStationMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<PortableDeviceMenu>> PORTABLE_DEVICE =
            MENUS.register("portable_device", () -> IMenuTypeExtension.create(PortableDeviceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MirageLightProjectorMenu>> MIRAGE_LIGHT_PROJECTOR =
            MENUS.register("mirage_light_projector", () -> IMenuTypeExtension.create(MirageLightProjectorMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
