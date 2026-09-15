package celerbi.mirageprojector.client;

import celerbi.mirageprojector.MirageProjector;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Client-only mod-bus registration without deprecated explicit bus selectors. */
@Mod(value = MirageProjector.MOD_ID, dist = Dist.CLIENT)
public final class MirageProjectorClient {
    public MirageProjectorClient(IEventBus modEventBus) {
        modEventBus.addListener(ClientEvents::registerScreens);
        modEventBus.addListener(ClientEvents::registerItemColors);
        modEventBus.addListener(ClientEvents::registerRenderers);
    }
}
