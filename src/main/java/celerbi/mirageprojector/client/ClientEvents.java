package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.registry.ModBlockEntities;
import celerbi.mirageprojector.registry.ModMenus;
import celerbi.mirageprojector.registry.ModItems;
import celerbi.mirageprojector.item.GlowDustItem;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = MirageProjector.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.MIRAGE_PROJECTOR.get(), MirageProjectorScreen::new);
        event.register(ModMenus.ENTITY_PROJECTOR.get(), EntityProjectorScreen::new);
        event.register(ModMenus.IMAGE_PROJECTOR.get(), ImageProjectorScreen::new);
        event.register(ModMenus.ITEM_PROJECTOR.get(), ItemProjectorScreen::new);
        event.register(ModMenus.BANNER_PROJECTOR.get(), BannerProjectorScreen::new);
    }


    @SubscribeEvent
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
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.MIRAGE_PROJECTOR.get(), MirageProjectorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CORE_BOOSTER.get(), CoreBoosterRenderer::new);
    }
}
