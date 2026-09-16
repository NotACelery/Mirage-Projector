package celerbi.mirageprojector.client;

import celerbi.mirageprojector.registry.ModBlockEntities;
import celerbi.mirageprojector.registry.ModMenus;
import celerbi.mirageprojector.registry.ModItems;
import celerbi.mirageprojector.menu.MirageProjectorMenu;
import celerbi.mirageprojector.ProjectionChassisProfile;
import celerbi.mirageprojector.item.GlowDustItem;
import celerbi.mirageprojector.item.LightBatteryItem;
import net.minecraft.util.FastColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

public final class ClientEvents {
    private ClientEvents() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.MIRAGE_PROJECTOR.get(), ClientEvents::createMirageProjectorScreen);
        event.register(ModMenus.ENTITY_PROJECTOR.get(), EntityProjectorScreen::new);
        event.register(ModMenus.IMAGE_PROJECTOR.get(), ImageProjectorScreen::new);
        event.register(ModMenus.ITEM_PROJECTOR.get(), ItemProjectorScreen::new);
        event.register(ModMenus.BANNER_PROJECTOR.get(), BannerProjectorScreen::new);
        event.register(ModMenus.CHARGING_STATION.get(), ChargingStationScreen::new);
        event.register(ModMenus.PORTABLE_DEVICE.get(), PortableDeviceScreen::new);
        event.register(ModMenus.MIRAGE_LIGHT_PROJECTOR.get(), MirageLightProjectorScreen::new);
        event.register(ModMenus.SCAN_CODEX.get(), ScanCodexScreen::new);
    }



    private static ResponsiveContainerScreen<MirageProjectorMenu> createMirageProjectorScreen(
            MirageProjectorMenu menu, Inventory inventory, Component title) {
        return menu.chassisProfile() == ProjectionChassisProfile.TABLE
                ? new MirageTableProjectorScreen(menu, inventory, title)
                : new MirageProjectorScreen(menu, inventory, title);
    }

    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex != 0) {
                return 0xFFFFFFFF;
            }
            float fraction = GlowDustItem.chargeFraction(stack);
            int red = Math.round(Mth.lerp(fraction, 90.0F, 255.0F));
            int green = Math.round(Mth.lerp(fraction, 78.0F, 236.0F));
            int blue = Math.round(Mth.lerp(fraction, 102.0F, 255.0F));
            return FastColor.ARGB32.color(255, red, green, blue);
        }, ModItems.GLOW_DUST.get());

        event.register((stack, tintIndex) -> {
            if (tintIndex != 1) {
                return 0xFFFFFFFF;
            }
            float fraction = LightBatteryItem.chargeFraction(stack);
            int red = Math.round(Mth.lerp(fraction, 78.0F, 211.0F));
            int green = Math.round(Mth.lerp(fraction, 67.0F, 142.0F));
            int blue = Math.round(Mth.lerp(fraction, 92.0F, 255.0F));
            return FastColor.ARGB32.color(255, red, green, blue);
        }, ModItems.LIGHT_BATTERY.get());
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.MIRAGE_PROJECTOR.get(), MirageProjectorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CORE_BOOSTER.get(), CoreBoosterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHARGING_STATION.get(), ChargingStationRenderer::new);
    }
}
